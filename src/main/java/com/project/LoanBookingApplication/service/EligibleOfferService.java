package com.project.LoanBookingApplication.service;

import com.project.LoanBookingApplication.dto.EligibleOfferResponse;
import com.project.LoanBookingApplication.entity.*;
import com.project.LoanBookingApplication.enums.OfferStatus;
import com.project.LoanBookingApplication.enums.RequestStatus;
import com.project.LoanBookingApplication.exception.ConflictException;
import com.project.LoanBookingApplication.exception.ResourceNotFoundException;
import com.project.LoanBookingApplication.repository.LoanRequestRepository;
import com.project.LoanBookingApplication.repository.OfferRepository;
import com.project.LoanBookingApplication.util.EmiCalculator;
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
    
    public List<EligibleOfferResponse> getOffers(Long requestId) {

        LoanRequest loanRequest = loanRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan request not found"));

        if (loanRequest.getRequestStatus() == RequestStatus.REJECTED) {
            throw new ConflictException("Loan request is closed");
        }

        if (loanRequest.getRequestStatus() == RequestStatus.COMPLETED) {
            throw new ConflictException("Loan request is already proceeded");
        }
        if(loanRequest.getRequestStatus() == RequestStatus.INPROCESS){
            throw new ConflictException("Loan request is already in progress");
        }

        User user = loanRequest.getUser();
        List<Offer> offers = offerRepository.findAll();

        List<EligibleOfferResponse> responses = new ArrayList<>();

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
            Double emi = EmiCalculator.calculateEmi(
                    loanRequest.getAmount(),
                    offer.getInterestRate(),
                    loanRequest.getTenure()
            );

            EligibleOfferResponse response = new EligibleOfferResponse();
            response.setOfferId(offer.getOfferId());
            response.setLenderId(offer.getLender().getLenderId());
            response.setTenure(loanRequest.getTenure());
            response.setInterestRate(offer.getInterestRate());
            response.setLoanAmount(loanRequest.getAmount());
            response.setLoanType(offer.getLoanType());
            response.setEmi(emi);
            response.setLenderName(offer.getLender().getLenderName());
            response.setLenderType(offer.getLender().getLenderType());

            responses.add(response);
        }

        return responses;
    }

}

