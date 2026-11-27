package com.printmate.pricing;

import java.math.BigDecimal;

/**
 * Strategy Pattern: Defines the contract for different pricing calculation algorithms.
 */
public interface PricingStrategy {
    String getStrategyName();
    boolean supports(int billablePages);
    PricingQuote calculate(int selectedPageCount, int copies, BigDecimal pricePerPage);
}

