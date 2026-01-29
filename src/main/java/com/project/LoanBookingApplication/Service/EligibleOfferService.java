package com.project.LoanBookingApplication.Service;

import com.project.LoanBookingApplication.DTO.OfferResponse;
import com.project.LoanBookingApplication.Entity.LoanRequest;
import com.project.LoanBookingApplication.Entity.Offer;
import com.project.LoanBookingApplication.Entity.OfferStatus;
import com.project.LoanBookingApplication.Entity.User;
import com.project.LoanBookingApplication.Repository.LoanRequestRepository;
import com.project.LoanBookingApplication.Repository.OfferRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EligibleOfferService {

    private final LoanRequestRepository loanRequestRepository;
    private final OfferRepository offerRepository;

    public EligibleOfferService(LoanRequestRepository loanRequestRepository,
                                OfferRepository offerRepository) {
        this.loanRequestRepository = loanRequestRepository;
        this.offerRepository = offerRepository;
    }

    public List<OfferResponse> getOffers(Long requestId) {

        LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Loan request not found"));

        User user = loanRequest.getUser();
        List<Offer> offers = offerRepository.findAll();

        List<OfferResponse> responses = new ArrayList<>();

        for (Offer offer : offers) {
            if (offer.getStatus() != OfferStatus.ACTIVE)
                continue;

            if (offer.getLoanType() != loanRequest.getLoanType())
                continue;

            if (loanRequest.getTenure() < offer.getMinTenure()
                    || loanRequest.getTenure() > offer.getMaxTenure())
                continue;

            if (loanRequest.getAmount() > offer.getMaxAmount())
                continue;

            if (user.getMonthlyIncome() < offer.getMinIncome())
                continue;

            if (user.getCreditScore() < offer.getMinCreditScore())
                continue;
            Double emi = calculateEmi(
                    loanRequest.getAmount(),
                    offer.getInterestRate(),
                    loanRequest.getTenure()
            );
            OfferResponse response = new OfferResponse();
            response.setOfferId(offer.getOfferId());
            response.setLenderId(offer.getLender().getLenderId());
            response.setTenure(loanRequest.getTenure());
            response.setInterestRate(offer.getInterestRate());
            response.setLoanAmount(loanRequest.getAmount());
            response.setLoanType(offer.getLoanType());
            response.setEmi(emi);

            responses.add(response);
        }

        return responses;
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
}

