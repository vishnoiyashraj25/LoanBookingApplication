package com.project.LoanBookingApplication.Service;

import com.project.LoanBookingApplication.Entity.*;
import com.project.LoanBookingApplication.Repository.AccountRepository;
import com.project.LoanBookingApplication.Repository.LoanRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;
    private final EmiService emiService;

    public LoanService(LoanRepository loanRepository, AccountRepository accountRepository, EmiService emiService) {
        this.loanRepository = loanRepository;
        this.accountRepository = accountRepository;
        this.emiService = emiService;
    }

    private String generateLoanNumber(LoanApplication application) {
        return "LN-" +
                application.getOffer().getLender().getLenderId() + "-" +
                System.currentTimeMillis();
    }

    public List<EmiSchedule> createLoan(LoanApplication application) {

        Loan loan = new Loan();
        loan.setLoanApplication(application);
        User user = application.getLoanRequest().getUser();
        loan.setUser(application.getLoanRequest().getUser());
        loan.setLender(application.getOffer().getLender());
        loan.setLoanType(application.getOffer().getLoanType());
        loan.setStatus(LoanStatus.Active);
        loan.setStartDate(LocalDate.now());
        loan.setEndDate(LocalDate.now().plusMonths(application.getTenure()));
        loan.setDuesAmount(application.getLoanAmount());
        loan.setEmi(application.getEmi());
        loan.setInterest(application.getInterestRate());
        loan.setLoanNumber(generateLoanNumber(application));
//        Account account = accountRepository.findById(user.getUserId()).orElseThrow();
        Account account = accountRepository.findByUser(user);
        if (account == null) {
            throw new IllegalStateException("User account not found for userId: " + user.getUserId());
        }

//        loan.setDisbursementAccount(account);
        loan.setDisbursementAccount(account);
        loanRepository.save(loan);

        return emiService.createEMI(loan);
    }
}

