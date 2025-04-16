package com.jamlech.loanapp.entities;

public enum LoanFrequency {
    MONTHS,
    WEEKS,
    YEARS;

    public boolean equalsIgnoreCase(LoanFrequency frequency) {
        return this.name().equalsIgnoreCase(frequency.name());
    }
}
