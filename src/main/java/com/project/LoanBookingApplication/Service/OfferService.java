package com.project.LoanBookingApplication.Service;


import com.project.LoanBookingApplication.DTO.OfferRequest;
import com.project.LoanBookingApplication.Entity.Lender;
import com.project.LoanBookingApplication.Entity.Offer;
import com.project.LoanBookingApplication.Repository.LenderRepository;
import com.project.LoanBookingApplication.Repository.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
public class OfferService {

    private final OfferRepository offerRepository;
    private final LenderRepository lenderRepository;
    public OfferService(OfferRepository offerRepository, LenderRepository lenderRepository){
        this.offerRepository = offerRepository;
        this.lenderRepository = lenderRepository;
    }
    public Offer createOffer(@RequestBody OfferRequest offerRequest){
        Offer offer = new Offer();
        Lender lender = lenderRepository.findById(offerRequest.getLenderId()).orElseThrow();
        offer.setLender(lender);
        offer.setInterestRate(offerRequest.getInterestRate());
        offer.setLoanType(offerRequest.getLoanType());
        offer.setStatus(offerRequest.getStatus());
        offer.setMaxTenure(offerRequest.getMaxTenure());
        offer.setMinTenure(offerRequest.getMinTenure());
        offer.setMaxAmount(offerRequest.getMaxAmount());
        offer.setMinIncome(offerRequest.getMinIncome());
        offer.setMinCreditScore(offerRequest.getMinCreditScore());
        return offerRepository.save(offer);
    }

    public Offer getOffer(@PathVariable  Long offer_id){
        return offerRepository.findById(offer_id).orElseThrow();
    }

    public List<Offer> getAllOffers(){
        return offerRepository.findAll();
    }
}
