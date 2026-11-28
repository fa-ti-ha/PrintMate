package com.printmate.pricing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Bulk discount pricing strategy: Applies a percentage discount when billable pages exceed threshold.
 */
@Component
@Order(10) // High priority
public class BulkDiscountPricingStrategy implements PricingStrategy {

    public static final String STRATEGY_NAME = "BULK_DISCOUNT";

    private final int threshold;
    private final BigDecimal discountRate;
    private final DiscountPolicyService policies;

    @Autowired
    public BulkDiscountPricingStrategy(DiscountPolicyService policies) {
        this.policies = policies;
        this.threshold = 50;
        this.discountRate = new BigDecimal("0.10");
    }

    public BulkDiscountPricingStrategy(
            int threshold, double discountRate) {
        this.threshold = threshold;
        this.discountRate = BigDecimal.valueOf(discountRate);
        this.policies = null;
    }

    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }

    @Override
    public boolean supports(int billablePages) {
        var settings = settings();
        return settings.isEnabled() && billablePages >= settings.threshold();
    }

    @Override
    public PricingQuote calculate(int selectedPageCount, int copies, BigDecimal pricePerPage) {
        int billablePages = selectedPageCount * copies;
        var settings = settings();
        boolean applies = settings.isEnabled() && billablePages >= settings.threshold();
        BigDecimal subtotal = pricePerPage.multiply(BigDecimal.valueOf(billablePages)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = subtotal.multiply(applies ? settings.rate() : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);

        return new PricingQuote(
                selectedPageCount,
                copies,
                billablePages,
                pricePerPage.setScale(2, RoundingMode.HALF_UP),
                subtotal,
                discount,
                total,
                applies ? STRATEGY_NAME : StandardPricingStrategy.STRATEGY_NAME
        );
    }

    public int getThreshold() {
        return settings().threshold();
    }

    public BigDecimal getDiscountRate() {
        return settings().rate();
    }

    private DiscountPolicyService.Settings settings() {
        return policies != null ? policies.getSettings() : new DiscountPolicyService.Settings(threshold, discountRate.movePointRight(2));
    }
}

