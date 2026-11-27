package com.printmate.order;

public enum OrderStatus {
    PENDING("Pending Review", "bg-warning text-dark"),
    ACCEPTED("Accepted", "bg-info text-dark"),
    PRINTING("Printing", "bg-primary"),
    READY_FOR_PICKUP("Ready for Pickup", "bg-success"),
    COMPLETED("Completed", "bg-secondary"),
    REJECTED("Rejected", "bg-danger"),
    CANCELLED("Cancelled", "bg-dark");

    private final String displayName;
    private final String badgeClass;

    OrderStatus(String displayName, String badgeClass) {
        this.displayName = displayName;
        this.badgeClass = badgeClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == REJECTED || this == CANCELLED;
    }

    public static java.util.List<OrderStatus> activeStatuses() {
        return java.util.List.of(PENDING, ACCEPTED, PRINTING, READY_FOR_PICKUP);
    }
}

