package com.printmate.pricing;

public enum PaperSize {
    A4("A4 (210 x 297 mm)"),
    A3("A3 (297 x 420 mm)");

    private final String displayName;

    PaperSize(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

