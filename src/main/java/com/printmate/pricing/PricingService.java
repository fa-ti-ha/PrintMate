package com.printmate.pricing;

import java.math.BigDecimal;
import java.util.List;

public interface PricingService {
    void validatePageRangeAndCopies(int pageStart, int pageEnd, int actualPdfPageCount, int copies);
    PricingRule getActiveRule(ColorMode colorMode, PaperSize paperSize, PrintSides printSides);
    PricingQuote calculateQuote(ColorMode colorMode, PaperSize paperSize, PrintSides printSides,
                                int pageStart, int pageEnd, int actualPdfPageCount, int copies);
    List<PricingRule> getAllRules();
    PricingRule updateRulePrice(Long id, BigDecimal newPrice, String adminEmail);
    PricingRule getRule(Long id);
    PricingRule createRule(ColorMode colorMode, PaperSize paperSize, PrintSides printSides, BigDecimal price, String adminEmail);
    void deleteRule(Long id);
}
