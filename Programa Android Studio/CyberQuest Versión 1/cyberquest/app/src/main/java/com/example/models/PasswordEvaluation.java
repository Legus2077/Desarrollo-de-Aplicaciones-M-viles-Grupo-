package com.example.models;

public class PasswordEvaluation {
    private final boolean hasMinLength;
    private final boolean hasUppercase;
    private final boolean hasNumber;
    private final boolean hasSpecialChar;
    private final int percentage;
    private final String label;
    private final int colorRes;

    public PasswordEvaluation(boolean hasMinLength, boolean hasUppercase, boolean hasNumber,
                              boolean hasSpecialChar, int percentage, String label, int colorRes) {
        this.hasMinLength = hasMinLength;
        this.hasUppercase = hasUppercase;
        this.hasNumber = hasNumber;
        this.hasSpecialChar = hasSpecialChar;
        this.percentage = percentage;
        this.label = label;
        this.colorRes = colorRes;
    }

    public boolean isHasMinLength() {
        return hasMinLength;
    }

    public boolean isHasUppercase() {
        return hasUppercase;
    }

    public boolean isHasNumber() {
        return hasNumber;
    }

    public boolean isHasSpecialChar() {
        return hasSpecialChar;
    }

    public int getPercentage() {
        return percentage;
    }

    public String getLabel() {
        return label;
    }

    public int getColorRes() {
        return colorRes;
    }
}
