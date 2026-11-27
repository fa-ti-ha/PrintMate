package com.printmate.pricing;

public enum PrintSides {
    SINGLE_SIDED("Single-sided"),
    DOUBLE_SIDED("Double-sided (Duplex)");

    private final String displayName;

    PrintSides(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

