package com.printmate.pricing;

public enum ColorMode {
    BLACK_AND_WHITE("Black & White"),
    COLOR("Color");

    private final String displayName;

    ColorMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

