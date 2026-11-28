package com.printmate.pricing;

import com.printmate.common.exception.InvalidOperationException;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingServiceUnitTest {

    @Mock
    private PricingRuleRepository rules;

    private PricingService service;

    @BeforeEach
    void setUp() {
        service = new PricingServiceImpl(rules,
                List.of(new BulkDiscountPricingStrategy(50, 0.10), new StandardPricingStrategy()),
                new SimpleMeterRegistry(), 100);
    }

    @Test
    void shopRateAndDiscountBoundaryDetermineTheChosenStrategy() {
        PricingRule rule = new PricingRule(ColorMode.COLOR, PaperSize.A3, PrintSides.DOUBLE_SIDED,
                new BigDecimal("5.00"), true, "admin@example.com");
        when(rules.findByColorModeAndPaperSizeAndPrintSidesAndActiveTrue(
                ColorMode.COLOR, PaperSize.A3, PrintSides.DOUBLE_SIDED)).thenReturn(Optional.of(rule));

        PricingQuote belowThreshold = service.calculateQuote(ColorMode.COLOR, PaperSize.A3,
                PrintSides.DOUBLE_SIDED, 1, 49, 50, 1);
        PricingQuote atThreshold = service.calculateQuote(ColorMode.COLOR, PaperSize.A3,
                PrintSides.DOUBLE_SIDED, 1, 50, 50, 1);

        assertAll(
                () -> assertEquals("STANDARD", belowThreshold.appliedStrategy()),
                () -> assertEquals(new BigDecimal("245.00"), belowThreshold.totalPrice()),
                () -> assertEquals("BULK_DISCOUNT", atThreshold.appliedStrategy()),
                () -> assertEquals(new BigDecimal("25.00"), atThreshold.discountAmount()),
                () -> assertEquals(new BigDecimal("225.00"), atThreshold.totalPrice()));
    }

    @Test
    void overflowingBillablePageCountIsRejectedBeforeLookingUpARate() {
        InvalidOperationException error = assertThrows(InvalidOperationException.class,
                () -> service.calculateQuote(ColorMode.BLACK_AND_WHITE, PaperSize.A4,
                        PrintSides.SINGLE_SIDED, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, 100));

        assertTrue(error.getMessage().contains("page count is too large"));
        verifyNoInteractions(rules);
    }

    @Test
    void priceWithFractionalPaisaCannotChangeAShopRate() {
        assertThrows(InvalidOperationException.class,
                () -> service.updateRulePrice(1L, new BigDecimal("3.001"), "admin@example.com"));

        verifyNoInteractions(rules);
    }
}
