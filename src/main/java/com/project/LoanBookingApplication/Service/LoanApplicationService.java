package com.project.LoanBookingApplication.Service;

import com.project.LoanBookingApplication.Entity.*;
import com.project.LoanBookingApplication.Repository.LoanApplicationRepository;
import com.project.LoanBookingApplication.Repository.LoanRequestRepository;
import com.project.LoanBookingApplication.Repository.OfferRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanApplicationService {

    private final LoanRequestRepository loanRequestRepository;
    private final OfferRepository offerRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanService loanService;

    public LoanApplicationService(
            LoanRequestRepository loanRequestRepository,
            OfferRepository offerRepository,
            LoanApplicationRepository loanApplicationRepository,
            LoanService loanService) {

        this.loanRequestRepository = loanRequestRepository;
        this.offerRepository = offerRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanService = loanService;
    }

    public LoanApplication selectOffer(Long loanRequestId, Long offerId) {

        LoanRequest loanRequest = loanRequestRepository.findById(loanRequestId)
                .orElseThrow(() -> new RuntimeException("Loan request not found"));

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));

        LoanApplication application = new LoanApplication();
        application.setLoanRequest(loanRequest);
        application.setOffer(offer);
        application.setLoanAmount(loanRequest.getAmount());
        application.setTenure(loanRequest.getTenure());
        application.setInterestRate(offer.getInterestRate());
        application.setStatus(ApplicationStatus.PENDING);
        application.setCreatedAt(LocalDateTime.now());
        application.setExpiredAt(LocalDateTime.now().plusDays(7));
        Double emi = calculateEmi(
                loanRequest.getAmount(),
                offer.getInterestRate(),
                loanRequest.getTenure()
        );

        application.setEmi(emi);

        return loanApplicationRepository.save(application);
    }

    private Double calculateEmi(Double principal, Double annualRate, Integer tenure) {

        double monthlyRate = annualRate / 12 / 100;

        if (monthlyRate == 0) {
            return Math.round((principal / tenure) * 100.0) / 100.0;
        }

        double numerator = principal * monthlyRate * Math.pow(1 + monthlyRate, tenure);
        double denominator = Math.pow(1 + monthlyRate, tenure) - 1;

        return Math.round((numerator / denominator) * 100.0) / 100.0;
    }

    public List<EmiSchedule> updateStatus(Long applicationId) {

        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        application.setStatus(ApplicationStatus.APPROVED);
        loanApplicationRepository.save(application);
        return loanService.createLoan(application);


    }
}
