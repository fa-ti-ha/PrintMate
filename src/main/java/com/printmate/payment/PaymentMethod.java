package com.printmate.payment;

public enum PaymentMethod {
    CASH("Cash on Pickup");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

