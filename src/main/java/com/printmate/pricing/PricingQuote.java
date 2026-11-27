package com.printmate.pricing;

import java.math.BigDecimal;

public record PricingQuote(
        int selectedPageCount,
        int copies,
        int billablePages,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal totalPrice,
        String appliedStrategy
) {
}

