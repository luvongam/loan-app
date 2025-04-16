package com.jamlech.loanapp.controllers;

import com.jamlech.loanapp.entities.Loan;
import com.jamlech.loanapp.entities.RepaymentSchedule;
import com.jamlech.loanapp.services.LoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }
    @PostMapping("/{customerId}")
    public ResponseEntity<Loan >createLoan(
            @PathVariable Long customerId,
            @RequestBody Loan loan) {
        Loan newLoan = loanService.createLoan(customerId, loan);
        return ResponseEntity.ok(newLoan);
    }
    @GetMapping("/{customerId}")
    public ResponseEntity<List<Loan>> getLoansByCustomer(
            @PathVariable Long customerId) {
        List<Loan> loans = loanService.getLoansByCustomer(customerId);
        return ResponseEntity.ok(loans);
    }
    @GetMapping("/{customerId}/{loanId}/repayment-schedule")
    public ResponseEntity<List<RepaymentSchedule>> getRepaymentSchedule(
            @PathVariable Long customerId,
            @PathVariable Long loanId
    ){
        List<RepaymentSchedule>  repaymentScheduleList =loanService
                .getRepaymentSchedule(
                customerId,loanId);
        return ResponseEntity.ok(repaymentScheduleList);
    }
    @PostMapping("/{customerId}/{loanId}/repayment-schedule/{scheduleId}/pay")
    public ResponseEntity<Void> markInstallmentPaid(
            @PathVariable Long customerId,
            @PathVariable long loanId,
            @PathVariable Long scheduleId) {
        loanService.markInstallmentPaid(scheduleId);
        return ResponseEntity.ok().build();
    }


    @PutMapping("/{customerId}/{loanId}")
    public ResponseEntity <Loan> updateLoan(
            @PathVariable Long customerId,
            @PathVariable Long loanId,
            @RequestBody Loan loanDetails) {
        try{
            Loan updatedLoan = loanService.updateLoan(loanId, customerId, loanDetails);
            return ResponseEntity.ok(updatedLoan);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{customerId}/{loanId}")
    public ResponseEntity<Void> deleteLoan(
            @PathVariable Long customerId,
            @PathVariable Long loanId) {
        loanService.deleteLoan(loanId,customerId);
        return ResponseEntity.noContent().build();
    }
//    @GetMapping("/statistics")
//    public ResponseEntity<Map<String ,Double>> getLoanStatistics() {
//        Map<String, Double> stats = loanService.getLoanStatistics();
//        return ResponseEntity.ok(stats);
//    }

}
