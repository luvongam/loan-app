package com.jamlech.loanapp.entities;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoanStatistics {
    private int totalDisbursedLoans;
    private double totalDisbursedAmount;
    private int totalPaidLoans;
    private double totalPaidAmount;
    private double totalOutstandingBalance;
    private double repaymentCompletionPercentage;
}
