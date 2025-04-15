package com.jamlech.loanapp.services;

import com.jamlech.loanapp.entities.Customer;
import com.jamlech.loanapp.entities.Loan;
import com.jamlech.loanapp.repositories.LoanRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanService {
    private final LoanRepository loanRepository;
    private final CustomerService customerService;

    public LoanService(LoanRepository loanRepository, CustomerService customerService) {
        this.loanRepository = loanRepository;
        this.customerService = customerService;
    }
    public Loan createLoan(Long customerId, Loan loan) {
        Customer customer=customerService.getCustomer(customerId);
        loan.setCustomer(customer);
        return loanRepository.save(loan);
    }
    public List<Loan> getLoansByCustomer(Long customerId) {
        Customer customer=customerService.getCustomer(customerId);
        return customer.getLoans();
    }
    public Loan getLoan(Long loanId,Long customerId) {
        Customer customer=customerService.getCustomer(customerId);
        return customer.getLoans().stream().filter(loan1 -> loan1.getId().equals(loanId)).findFirst().orElseThrow(
                () -> new RuntimeException("Loan not found")
        );
    }
    public Loan updateLoan(Long loanId,Long customerId, Loan loanDetails) {

        Loan loan = getLoan(loanId,customerId);
        loan.setPrincipalAmount(loanDetails.getPrincipalAmount());
        loan.setRepaymentPeriod(loanDetails.getRepaymentPeriod());
        loan.setInterestRate(loanDetails.getInterestRate());
        loan.setFrequency(loanDetails.getFrequency());
        loan.setUpdatedAt(LocalDateTime.now());
        return loanRepository.save(loan);
    }
    public void deleteLoan(Long loanId,Long customerId) {
        Loan loan = getLoan(loanId,customerId);
        loanRepository.delete(loan);
    }


}
