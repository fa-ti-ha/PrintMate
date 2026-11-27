package com.printmate.payment;

public enum PaymentStatus {
    UNPAID("Unpaid", "badge bg-warning text-dark"),
    PAID("Paid", "badge bg-success");

    private final String displayName;
    private final String badgeClass;

    PaymentStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}

