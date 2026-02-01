package com.project.LoanBookingApplication.Service;


import com.project.LoanBookingApplication.DTO.OfferRequest;
import com.project.LoanBookingApplication.DTO.OfferResponseDTO;
import com.project.LoanBookingApplication.Entity.*;
import com.project.LoanBookingApplication.Exception.ResourceNotFoundException;
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
    public OfferResponseDTO createOffer(@RequestBody OfferRequest offerRequest){
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
        Offer savedOffer = offerRepository.save(offer);
        return mapToResponse(savedOffer);
    }

//    public Offer getOffer(@PathVariable  Long offer_id){
//        return offerRepository.findById(offer_id).orElseThrow();
//    }

    public List<OfferResponseDTO> getAllOffers(
            Long offerId,
            String lenderName,
            LenderType lenderType,
            LoanType loanType,
            OfferStatus status
    ) {

        List<Offer> offers = offerRepository.findAll();

        if (offerId != null) {
            offers = offers.stream()
                    .filter(o -> o.getOfferId().equals(offerId))
                    .toList();
        }

        if (lenderName != null) {
            offers = offers.stream()
                    .filter(o -> o.getLender().getLenderName().equalsIgnoreCase(lenderName))
                    .toList();
        }

        if (lenderType != null) {
            offers = offers.stream()
                    .filter(o -> o.getLender().getLenderType() == lenderType)
                    .toList();
        }

        if (loanType != null) {
            offers = offers.stream()
                    .filter(o -> o.getLoanType() == loanType)
                    .toList();
        }

        if (status != null) {
            offers = offers.stream()
                    .filter(o -> o.getStatus() == status)
                    .toList();
        }

        if (offers.isEmpty()) {
            throw new ResourceNotFoundException("No Offers Found");
        }

        return offers.stream()
                .map(this::mapToResponse)
                .toList();
    }


    private OfferResponseDTO mapToResponse(Offer offer) {
        OfferResponseDTO dto = new OfferResponseDTO();

        dto.setOfferId(offer.getOfferId());

        dto.setLenderName(offer.getLender().getLenderName());
        dto.setLenderType(offer.getLender().getLenderType());

        dto.setMinTenure(offer.getMinTenure());
        dto.setMaxTenure(offer.getMaxTenure());
        dto.setInterestRate(offer.getInterestRate());

        dto.setMinIncome(offer.getMinIncome());
        dto.setMinCreditScore(offer.getMinCreditScore());
        dto.setMaxAmount(offer.getMaxAmount());

        dto.setLoanType(offer.getLoanType());
        dto.setStatus(offer.getStatus());

        return dto;
    }

}
