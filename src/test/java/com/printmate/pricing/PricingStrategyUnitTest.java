package com.printmate.pricing;

import com.printmate.common.exception.InvalidOperationException;
import com.printmate.common.exception.ResourceNotFoundException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PricingStrategyUnitTest {

    @Mock
    private PricingRuleRepository pricingRuleRepository;

    private StandardPricingStrategy standardStrategy;
    private BulkDiscountPricingStrategy bulkDiscountStrategy;
    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        standardStrategy = new StandardPricingStrategy();
        bulkDiscountStrategy = new BulkDiscountPricingStrategy(50, 0.10); // 50 threshold, 10% discount
        pricingService = new PricingServiceImpl(
                pricingRuleRepository,
                List.of(bulkDiscountStrategy, standardStrategy),
                new SimpleMeterRegistry(),
                100
        );
    }

    @Test
    @DisplayName("Unit Test 1: Black & white standard quotation calculation")
    void testBlackAndWhiteQuotationCalculation() {
        PricingQuote quote = standardStrategy.calculate(5, 2, new BigDecimal("3.00"));

        assertEquals(5, quote.selectedPageCount());
        assertEquals(2, quote.copies());
        assertEquals(10, quote.billablePages());
        assertEquals(new BigDecimal("3.00"), quote.unitPrice());
        assertEquals(new BigDecimal("30.00"), quote.subtotal());
        assertEquals(new BigDecimal("0.00"), quote.discountAmount());
        assertEquals(new BigDecimal("30.00"), quote.totalPrice());
        assertEquals(StandardPricingStrategy.STRATEGY_NAME, quote.appliedStrategy());
    }

    @Test
    @DisplayName("Unit Test 2: Color printing quotation calculation")
    void testColorQuotationCalculation() {
        PricingQuote quote = standardStrategy.calculate(4, 1, new BigDecimal("10.00"));

        assertEquals(4, quote.billablePages());
        assertEquals(new BigDecimal("10.00"), quote.unitPrice());
        assertEquals(new BigDecimal("40.00"), quote.subtotal());
        assertEquals(new BigDecimal("40.00"), quote.totalPrice());
    }

    @Test
    @DisplayName("Unit Test 3: Paper size pricing (A4 vs A3)")
    void testPaperSizePricing() {
        // A4 rule rate: 3.00, A3 rule rate: 6.00
        PricingQuote quoteA4 = standardStrategy.calculate(10, 1, new BigDecimal("3.00"));
        PricingQuote quoteA3 = standardStrategy.calculate(10, 1, new BigDecimal("6.00"));

        assertEquals(new BigDecimal("30.00"), quoteA4.totalPrice());
        assertEquals(new BigDecimal("60.00"), quoteA3.totalPrice());
        assertTrue(quoteA3.totalPrice().compareTo(quoteA4.totalPrice()) > 0);
    }

    @Test
    @DisplayName("Unit Test 4: Single-sided and double-sided pricing")
    void testSingleAndDoubleSidedPricing() {
        // Single: 3.00, Double: 5.00
        PricingQuote singleQuote = standardStrategy.calculate(10, 1, new BigDecimal("3.00"));
        PricingQuote doubleQuote = standardStrategy.calculate(10, 1, new BigDecimal("5.00"));

        assertEquals(new BigDecimal("30.00"), singleQuote.totalPrice());
        assertEquals(new BigDecimal("50.00"), doubleQuote.totalPrice());
    }

    @Test
    @DisplayName("Unit Test 5: Bulk discount calculation when billable pages reach 50 threshold")
    void testBulkDiscountCalculation() {
        // 50 pages * 1 copy = 50 billable pages. 50 * 3.00 = 150.00. 10% discount = 15.00. Total = 135.00.
        assertTrue(bulkDiscountStrategy.supports(50));
        PricingQuote quote = bulkDiscountStrategy.calculate(50, 1, new BigDecimal("3.00"));

        assertEquals(50, quote.billablePages());
        assertEquals(new BigDecimal("150.00"), quote.subtotal());
        assertEquals(new BigDecimal("15.00"), quote.discountAmount());
        assertEquals(new BigDecimal("135.00"), quote.totalPrice());
        assertEquals(BulkDiscountPricingStrategy.STRATEGY_NAME, quote.appliedStrategy());
    }

    @Test
    @DisplayName("Unit Test 6: Invalid page range rejection")
    void testInvalidPageRangeRejection() {
        // pageStart < 1
        assertThrows(InvalidOperationException.class, () ->
                pricingService.validatePageRangeAndCopies(0, 5, 10, 1));

        // pageEnd < pageStart
        assertThrows(InvalidOperationException.class, () ->
                pricingService.validatePageRangeAndCopies(5, 3, 10, 1));

        // pageEnd > actualPdfPageCount
        assertThrows(InvalidOperationException.class, () ->
                pricingService.validatePageRangeAndCopies(1, 15, 10, 1));
    }

    @Test
    @DisplayName("Unit Test 7: Invalid copy-count rejection")
    void testInvalidCopyCountRejection() {
        // copies < 1
        assertThrows(InvalidOperationException.class, () ->
                pricingService.validatePageRangeAndCopies(1, 5, 10, 0));

        // copies > maxCopies (100)
        assertThrows(InvalidOperationException.class, () ->
                pricingService.validatePageRangeAndCopies(1, 5, 10, 101));
    }

    @Test
    @DisplayName("Unit Test 8: Missing pricing rule throws ResourceNotFoundException")
    void testMissingPricingRule() {
        when(pricingRuleRepository.findByColorModeAndPaperSizeAndPrintSidesAndActiveTrue(
                ColorMode.COLOR, PaperSize.A3, PrintSides.DOUBLE_SIDED))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                pricingService.calculateQuote(ColorMode.COLOR, PaperSize.A3, PrintSides.DOUBLE_SIDED, 1, 5, 10, 1));
    }
}
