package com.jamlech.loanapp.controllers;

import com.jamlech.loanapp.entities.Loan;
import com.jamlech.loanapp.services.LoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PutMapping("/{customerId}/{loanId}")
    public Loan updateLoan(
            @PathVariable Long customerId,
            @PathVariable Long loanId,
            @RequestBody Loan loanDetails) {
        return loanService.updateLoan(loanId,customerId, loanDetails);
    }

    @DeleteMapping("/{customerId}/{loanId}")
    public void deleteLoan(
            @PathVariable Long customerId,
            @PathVariable Long loanId) {
        loanService.deleteLoan(loanId,customerId);
    }

}
