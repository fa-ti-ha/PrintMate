package com.printmate.pricing;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Standard pricing strategy: Flat rate per billable page with no volume discount.
 */
@Component
@Order(100) // Lower priority than BulkDiscount
public class StandardPricingStrategy implements PricingStrategy {

    public static final String STRATEGY_NAME = "STANDARD";

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    @Override
    public boolean supports(int billablePages) {
        return true; // Default fallback strategy
    }

    @Override
    public PricingQuote calculate(int selectedPageCount, int copies, BigDecimal pricePerPage) {
        int billablePages = selectedPageCount * copies;
        BigDecimal subtotal = pricePerPage.multiply(BigDecimal.valueOf(billablePages)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal;

        return new PricingQuote(
                selectedPageCount,
                copies,
                billablePages,
                pricePerPage.setScale(2, RoundingMode.HALF_UP),
                subtotal,
                discount,
                total,
                STRATEGY_NAME
        );
    }
}

