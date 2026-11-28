package com.printmate.pricing;

import com.printmate.common.exception.InvalidOperationException;
import com.printmate.common.exception.ResourceNotFoundException;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PricingServiceImpl implements PricingService {

    private final PricingRuleRepository pricingRuleRepository;
    private final List<PricingStrategy> pricingStrategies;
    private final MeterRegistry meterRegistry;
    private final int maxCopies;

    public PricingServiceImpl(PricingRuleRepository pricingRuleRepository,
                              List<PricingStrategy> pricingStrategies,
                              MeterRegistry meterRegistry,
                              @Value("${printmate.max-copies:100}") int maxCopies) {
        this.pricingRuleRepository = pricingRuleRepository;
        this.pricingStrategies = pricingStrategies;
        this.meterRegistry = meterRegistry;
        this.maxCopies = maxCopies;
    }

    @Override
    public void validatePageRangeAndCopies(int pageStart, int pageEnd, int actualPdfPageCount, int copies) {
        if (pageStart < 1) {
            throw new InvalidOperationException("Starting page must be at least 1.");
        }
        if (pageEnd < pageStart) {
            throw new InvalidOperationException("Ending page (" + pageEnd + ") cannot be less than starting page (" + pageStart + ").");
        }
        if (pageEnd > actualPdfPageCount) {
            throw new InvalidOperationException("Ending page (" + pageEnd + ") exceeds total PDF page count (" + actualPdfPageCount + ").");
        }
        if (copies < 1) {
            throw new InvalidOperationException("Number of copies must be at least 1.");
        }
        if (copies > maxCopies) {
            throw new InvalidOperationException("Number of copies cannot exceed maximum allowed (" + maxCopies + ").");
        }
        if ((long) (pageEnd - pageStart + 1) * copies > Integer.MAX_VALUE) {
            throw new InvalidOperationException("The selected page count is too large.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PricingRule getActiveRule(ColorMode colorMode, PaperSize paperSize, PrintSides printSides) {
        return pricingRuleRepository.findByColorModeAndPaperSizeAndPrintSidesAndActiveTrue(colorMode, paperSize, printSides)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("No active pricing rule found for %s, %s, %s",
                                colorMode.getDisplayName(), paperSize.getDisplayName(), printSides.getDisplayName())));
    }

    @Override
    @Transactional(readOnly = true)
    public PricingQuote calculateQuote(ColorMode colorMode, PaperSize paperSize, PrintSides printSides,
                                       int pageStart, int pageEnd, int actualPdfPageCount, int copies) {
        validatePageRangeAndCopies(pageStart, pageEnd, actualPdfPageCount, copies);

        PricingRule rule = getActiveRule(colorMode, paperSize, printSides);
        int selectedPageCount = pageEnd - pageStart + 1;
        int billablePages = selectedPageCount * copies;

        PricingStrategy chosenStrategy = pricingStrategies.stream()
                .filter(strategy -> strategy.supports(billablePages))
                .findFirst()
                .orElseThrow(() -> new InvalidOperationException("No suitable pricing strategy found."));

        PricingQuote quote = chosenStrategy.calculate(selectedPageCount, copies, rule.getPricePerPage());
        if (quote.totalPrice().compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new InvalidOperationException("This print job exceeds the maximum order amount. Split it into smaller orders.");
        }

        if (meterRegistry != null) {
            meterRegistry.counter("printmate.quotes.calculated",
                    "color_mode", colorMode.name(),
                    "paper_size", paperSize.name(),
                    "strategy", quote.appliedStrategy()
            ).increment();
        }

        return quote;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PricingRule> getAllRules() {
        return pricingRuleRepository.findAllByOrderByPaperSizeAscColorModeAsc();
    }

    @Override
    @Transactional
    public PricingRule updateRulePrice(Long id, BigDecimal newPrice, String adminEmail) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0 || newPrice.scale() > 2 || newPrice.precision() - newPrice.scale() > 8) {
            throw new InvalidOperationException("Price per page must be greater than zero.");
        }
        PricingRule rule = pricingRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pricing rule with ID " + id + " not found."));

        rule.setPricePerPage(newPrice);
        rule.setUpdatedBy(adminEmail);
        return pricingRuleRepository.save(rule);
    }

    @Override
    @Transactional(readOnly = true)
    public PricingRule getRule(Long id) {
        return pricingRuleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pricing rule not found: " + id));
    }

    @Override
    @Transactional
    public PricingRule createRule(ColorMode colorMode, PaperSize paperSize, PrintSides printSides, BigDecimal price, String adminEmail) {
        if (price == null || price.signum() <= 0 || price.scale() > 2 || price.precision() - price.scale() > 8)
            throw new InvalidOperationException("Enter a positive price with at most two decimal places.");
        return pricingRuleRepository.saveAndFlush(new PricingRule(colorMode, paperSize, printSides, price, true, adminEmail));
    }

    @Override
    @Transactional
    public void deleteRule(Long id) {
        pricingRuleRepository.delete(getRule(id));
    }
}
