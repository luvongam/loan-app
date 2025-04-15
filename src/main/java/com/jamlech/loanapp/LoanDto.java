package com.jamlech.loanapp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record LoanDto(
        @Positive
        double principalAmount,
        @Positive
        double interestRate,
        @Positive
        int repaymentPeriod,
        @NotBlank
        String frequency) {
}
