package com.jamlech.loanapp.repositories;

import com.jamlech.loanapp.entities.RepaymentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface RepaymentScheduleRepository extends JpaRepository<RepaymentSchedule,Long> {
    List<RepaymentSchedule> findByLoanId(Long loanId);
}
