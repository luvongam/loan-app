package com.jamlech.loanapp.services;

import com.jamlech.loanapp.entities.*;

import com.jamlech.loanapp.repositories.LoanRepository;

import com.jamlech.loanapp.repositories.RepaymentScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoanService {
    private final LoanRepository loanRepository;
    private final CustomerService customerService;
    private final RepaymentScheduleRepository repaymentScheduleRepository;

    public LoanService(LoanRepository loanRepository, CustomerService customerService, RepaymentScheduleRepository repaymentScheduleRepository) {
        this.loanRepository = loanRepository;
        this.customerService = customerService;

        this.repaymentScheduleRepository = repaymentScheduleRepository;
    }
    public Loan createLoan(Long customerId, Loan loan) {
        Customer customer=customerService.getCustomer(customerId);
        loan.setCustomer(customer);
        loan.setStatus(LoanStatus.DISBURSED);
        Loan savedLoan=loanRepository.save(loan);
//        generate repayment schedule
        List<RepaymentSchedule> repaymentSchedule = generateRepaymentSchedule(savedLoan);
        repaymentSchedule.forEach(schedule-> {
            schedule.setLoan(savedLoan);
            schedule.setStatus(PaymentStatus.PENDING);
        });
        repaymentScheduleRepository.saveAll(repaymentSchedule);

        return savedLoan;
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

        Loan updatedLoan=loanRepository.save(loan);

//        update repayment schedule
        repaymentScheduleRepository.deleteAll(
                repaymentScheduleRepository.findByLoanId(loanId)
        );
        List<RepaymentSchedule> repaymentSchedule = generateRepaymentSchedule(
                updatedLoan
        );
        repaymentSchedule.forEach(schedule-> {
            schedule.setLoan(updatedLoan);
            schedule.setStatus(PaymentStatus.PENDING);
        });
        repaymentScheduleRepository.saveAll(repaymentSchedule);

        return updatedLoan;
    }
    public void deleteLoan(Long loanId,Long customerId) {

        Loan loan = getLoan(loanId,customerId);
        repaymentScheduleRepository.deleteAll(
                repaymentScheduleRepository.findByLoanId(loanId)
        );
        loanRepository.delete(loan);
    }
    public  void markInstallmentPaid(Long scheduleId){
        RepaymentSchedule repaymentSchedule =
                repaymentScheduleRepository.findById(scheduleId).orElseThrow(()->
                        new RuntimeException("Repayment schedule not found"));

        repaymentSchedule.setStatus(PaymentStatus.PAID);
        repaymentScheduleRepository.save(repaymentSchedule);

//        check if all installment are paid
        Loan loan =repaymentSchedule.getLoan();
        List<RepaymentSchedule> repaymentSchedules =
                repaymentScheduleRepository.findByLoanId(loan.getId());
        boolean allPaid = repaymentSchedules.stream().allMatch(
                schedule->schedule.getStatus().equals(PaymentStatus.PAID)
        );
        if (allPaid){
            loan.setStatus(LoanStatus.PAID);
            loanRepository.save(loan);
        }

    }
    public List<RepaymentSchedule> getRepaymentSchedule(Long customerId,Long loanId) {
        Loan loan =getLoan(loanId,customerId);
        Long id =loan.getId();
        return repaymentScheduleRepository.findByLoanId(id);
    }
    public  LoanStatistics getLoanStatistics(){
        List<Loan> disbursedLoans=loanRepository.findByStatus(LoanStatus.DISBURSED);
        List<Loan> paidLoans=loanRepository.findByStatus(LoanStatus.PAID);

        LoanStatistics statistics = new LoanStatistics();
//        disbursed loans
        statistics.setTotalDisbursedLoans(disbursedLoans.size());
        statistics.setTotalDisbursedAmount(disbursedLoans.stream()
                .mapToDouble(Loan::getPrincipalAmount)
                .sum());

//        paid loans
        statistics.setTotalPaidLoans(paidLoans.size());
        statistics.setTotalPaidAmount(paidLoans.stream()
                .mapToDouble(loan->
                        loan.getRepaymentSchedules()
                                .stream()
                                .filter(schedule->
                                        schedule.getStatus().equals(PaymentStatus.PAID))
                                .mapToDouble(RepaymentSchedule::getTotalPayment)
                .sum())
                .sum());

//        outstanding balances
        statistics.setTotalOutstandingBalance(disbursedLoans.stream()
                .mapToDouble(loan->loan.getRepaymentSchedules().stream()
                        .filter(schedule->schedule.getStatus()==PaymentStatus.PENDING)
                        .mapToDouble(RepaymentSchedule::getRemainingBalance)
                        .findFirst()
                        .orElse(0.0))
                .sum());

        List<Loan> allLoans = loanRepository.findAll();
        double totalInstallment = allLoans.stream().mapToInt(
                loan->loan.getRepaymentSchedules().size()
        ).sum();
        double paidInstallment=allLoans.stream().flatMap(loan ->
                loan.getRepaymentSchedules().stream()).filter(
                        schedule->schedule.getStatus()==
                                PaymentStatus.PAID
        ).count();
        statistics.setRepaymentCompletionPercentage(totalInstallment>0 ?
                (paidInstallment/totalInstallment) * 100 : 0.0);
        return statistics;

    }






    private List<RepaymentSchedule> generateRepaymentSchedule(Loan loan) {
        List<RepaymentSchedule> schedules = new ArrayList<>();
        double principalAmount = loan.getPrincipalAmount();
        double annualRate = loan.getInterestRate();
        int repaymentPeriod = loan.getRepaymentPeriod();
        LoanFrequency frequency = loan.getFrequency();

        // Validate inputs
        if (principalAmount <= 0) {
            throw new IllegalArgumentException("Principal amount must be positive.");
        }
        if (repaymentPeriod <= 0) {
            throw new IllegalArgumentException("Repayment period must be positive.");
        }
        if (annualRate < 0) {
            throw new IllegalArgumentException("Interest rate cannot be negative.");
        }

        // Calculate periods per year based on frequency
        int periodsPerYear = switch (frequency) {
            case WEEKS -> 52;
            case MONTHS -> 12;
            case YEARS -> 1;
            default -> throw new IllegalArgumentException("Invalid frequency: " + frequency);
        };

        // Calculate simple interest
        double totalInterest = principalAmount * annualRate * (repaymentPeriod / (double) periodsPerYear);
        double periodicPrincipal = principalAmount / repaymentPeriod;
        double periodicInterest = totalInterest / repaymentPeriod;
        double periodicPayment = periodicPrincipal + periodicInterest;

        double remainingBalance = principalAmount;
        LocalDate startDate = loan.getStartDate() != null ? loan.getStartDate() : LocalDate.now();

        for (int i = 1; i <= repaymentPeriod; i++) {
            // Adjust final payment to account for rounding errors
            if (i == repaymentPeriod) {
                periodicPrincipal = remainingBalance; // Ensure remaining balance is paid off
                periodicPayment = periodicPrincipal + periodicInterest;
            }

            // Update remaining balance
            remainingBalance -= periodicPrincipal;

            // Calculate due date
            LocalDate dueDate = switch (frequency) {
                case WEEKS -> startDate.plusWeeks(i);
                case MONTHS -> startDate.plusMonths(i);
                case YEARS -> startDate.plusYears(i);
                default -> throw new IllegalArgumentException("Invalid repayment frequency.");
            };

            // Create repayment entry
            RepaymentSchedule repaymentSchedule = new RepaymentSchedule();
            repaymentSchedule.setInstallmentNumber(i);
            repaymentSchedule.setDueDate(dueDate);
            repaymentSchedule.setPrincipalComponent(Math.round(periodicPrincipal * 100.0) / 100.0);
            repaymentSchedule.setInterestComponent(Math.round(periodicInterest * 100.0) / 100.0);
            repaymentSchedule.setTotalPayment(Math.round(periodicPayment * 100.0) / 100.0);
            repaymentSchedule.setRemainingBalance(Math.round(remainingBalance * 100.0) / 100.0);

            schedules.add(repaymentSchedule);
        }

        return schedules;
    }


//    private List<RepaymentSchedule> generateRepaymentSchedule(Loan loan) {
//        List<RepaymentSchedule> schedules = new ArrayList<>();
//        double principalAmount = loan.getPrincipalAmount();
//        double annualRate = loan.getInterestRate();
//        int repaymentPeriod = loan.getRepaymentPeriod();
//        LoanFrequency frequency = loan.getFrequency();
//
//        // Validate inputs
//        if (principalAmount <= 0) {
//            throw new IllegalArgumentException("Principal amount must be positive.");
//        }
//        if (repaymentPeriod <= 0) {
//            throw new IllegalArgumentException("Repayment period must be positive.");
//        }
//        if (annualRate < 0) {
//            throw new IllegalArgumentException("Interest rate cannot be negative.");
//        }
//
////        calculate thr periods per year  based on frequency
//        int periodsPerYear = switch (frequency) {
//            case WEEKS -> 52;
//            case MONTHS -> 12;
//            case YEARS -> 1;
//            default -> throw new IllegalArgumentException("Invalid frequency: " + frequency);
//        };
////        calculate the periodic rate
//        double periodicRate = annualRate / periodsPerYear;
//
//
////        calculate fixed periodic payment using amortization formula
//        double periodicPayment;
//        if (annualRate ==0){
//            periodicPayment=principalAmount/repaymentPeriod;
//        }
//        else {
//            periodicPayment=principalAmount * (
//                    periodicRate*Math.pow(1+ periodicRate,repaymentPeriod)
//                    ) / (Math.pow(1+periodicRate,repaymentPeriod)-1);
//        }
//        double remainingBalance=principalAmount;
//        LocalDate startDate = loan.getStartDate() != null ? loan.getStartDate():LocalDate.now();
//
//
//
//        for (int i = 1; i <= repaymentPeriod; i++) {
//            //            interest per components
//            double interestComponent = remainingBalance * periodicRate;
//            double principalComponent =periodicPayment - interestComponent;
//
//            if (i==repaymentPeriod){
//                principalComponent=remainingBalance;
//                periodicPayment =principalAmount+interestComponent;
//            }
//
//            remainingBalance-=principalComponent;
////            adjust due date
//            LocalDate dueDate = switch (frequency) {
//                case WEEKS -> startDate.plusWeeks(i);
//                case MONTHS -> startDate.plusMonths(i);
//                case YEARS -> startDate.plusYears(i);
//                default -> throw new IllegalArgumentException("Invalid repayment frequency.");
//            };
////            create repayment entry
//            RepaymentSchedule repaymentSchedule =new RepaymentSchedule();
//            repaymentSchedule.setInstallmentNumber(i);
//            repaymentSchedule.setDueDate(dueDate);
//            repaymentSchedule.setPrincipalComponent(
//                    Math.round(principalComponent*100.0)/100.0
//            );
//            repaymentSchedule.setInterestComponent(
//                    Math.round(interestComponent*100.0)/100.0);
//            repaymentSchedule.setTotalPayment(
//                    Math.round(periodicPayment*100.0)/100.0
//            );
//            repaymentSchedule.setRemainingBalance(
//                    Math.round(remainingBalance*100.0)/100.0);
//            schedules.add(repaymentSchedule);
//        }
//        return schedules;
//    }








}
