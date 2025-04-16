package com.jamlech.loanapp.repositories;

import com.jamlech.loanapp.entities.Loan;
import com.jamlech.loanapp.entities.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan>findByCustomerId(Long customerId);
    List<Loan> findByStatus(LoanStatus status);



}
