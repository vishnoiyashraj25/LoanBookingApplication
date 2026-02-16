package com.project.LoanBookingApplication.service;
import com.project.LoanBookingApplication.dto.EmiResponse;
import com.project.LoanBookingApplication.dto.LoanResponse;
import com.project.LoanBookingApplication.entity.*;
import com.project.LoanBookingApplication.enums.LoanStatus;
import com.project.LoanBookingApplication.enums.RequestStatus;
import com.project.LoanBookingApplication.exception.ResourceNotFoundException;
import com.project.LoanBookingApplication.repository.AccountRepository;
import com.project.LoanBookingApplication.repository.LoanRepository;
import com.project.LoanBookingApplication.repository.LoanRequestRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;
    private final EmiService emiService;
    private final LoanRequestRepository loanRequestRepository;

    public LoanService(LoanRepository loanRepository, AccountRepository accountRepository, EmiService emiService, LoanRequestRepository loanRequestRepository) {
        this.loanRepository = loanRepository;
        this.accountRepository = accountRepository;
        this.emiService = emiService;
        this.loanRequestRepository = loanRequestRepository;
    }

    private String generateLoanNumber(LoanApplication application) {
        return "LN-" +
                application.getOffer().getLender().getLenderId() + "-" +
                System.currentTimeMillis();
    }

    @Transactional
    public List<EmiResponse> createLoan(LoanApplication application) {

        Loan loan = new Loan();
        loan.setLoanApplication(application);
        User user = application.getLoanRequest().getUser();
        loan.setUser(application.getLoanRequest().getUser());
        loan.setLender(application.getOffer().getLender());
        loan.setLoanType(application.getOffer().getLoanType());
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setStartDate(LocalDate.now());
        loan.setEndDate(LocalDate.now().plusMonths(application.getTenure()));
        loan.setDuesAmount(application.getLoanAmount());
        loan.setEmi(application.getEmi());
        loan.setInterest(application.getInterestRate());
        loan.setLoanNumber(generateLoanNumber(application));
        Account account = accountRepository.findByUser(user);
        if (account == null) {
            throw new ResourceNotFoundException("User account not found for userId: " + user.getUserId());
        }
        loan.setDisbursementAccount(account);
        loanRepository.save(loan);

        return emiService.createEMI(loan);
    }

    public void processApprovedLoan(LoanApplication application) {
        createLoan(application);

//        LoanRequest req = application.getLoanRequest();
//        req.setRequestStatus(RequestStatus.DONE);
//        loanRequestRepository.save(req);
    }

    LoanResponse mapToDto(Loan loan) {
        LoanResponse dto = new LoanResponse();
        dto.setLoanNumber(loan.getLoanNumber());
        dto.setLenderName(loan.getLender().getLenderName());
        dto.setUserName(loan.getUser().getUserName());
        dto.setLoanApplicationId(String.valueOf(loan.getLoanApplication().getId()));
        dto.setStatus(loan.getStatus());
        dto.setStartDate(loan.getStartDate());
        dto.setLoanType(loan.getLoanType());
        dto.setEmi(loan.getEmi());
        dto.setInterest(loan.getInterest());
        dto.setDuesAmount(loan.getDuesAmount());
        dto.setDisbursementAccountNumber(loan.getDisbursementAccount().getAccountNumber());
        return dto;
    }


    public List<LoanResponse> getAllLoans() {
        return loanRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

}

