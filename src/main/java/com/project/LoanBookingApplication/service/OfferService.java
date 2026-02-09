package com.project.LoanBookingApplication.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.LoanBookingApplication.dto.OfferRequest;
import com.project.LoanBookingApplication.dto.OfferResponse;
import com.project.LoanBookingApplication.entity.*;
import com.project.LoanBookingApplication.enums.LenderType;
import com.project.LoanBookingApplication.enums.LoanType;
import com.project.LoanBookingApplication.enums.OfferStatus;
import com.project.LoanBookingApplication.exception.ResourceNotFoundException;
import com.project.LoanBookingApplication.repository.LenderRepository;
import com.project.LoanBookingApplication.repository.OfferRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OfferService {

    private final OfferRepository offerRepository;
    private final LenderRepository lenderRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    public OfferService(OfferRepository offerRepository, LenderRepository lenderRepository) {
        this.offerRepository = offerRepository;
        this.lenderRepository = lenderRepository;
    }

    @CacheEvict(value = "offers", allEntries = true)
    public OfferResponse createOffer(OfferRequest offerRequest) {

        Offer offer = new Offer();

        Lender lender = lenderRepository
                .findById(offerRequest.getLenderId())
                .orElseThrow();

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

    @Cacheable(
            value = "offers",
            key = "#offerId + '-' + #lenderName + '-' + #lenderType + '-' + #loanType + '-' + #status"
    )
    public List<OfferResponse> getAllOffersJson(
            Long offerId,
            String lenderName,
            LenderType lenderType,
            LoanType loanType,
            OfferStatus status
    ) throws JsonProcessingException {

        List<Offer> offers = offerRepository.findAll();

        if (offerId != null)
            offers = offers.stream().filter(o -> o.getOfferId().equals(offerId)).toList();

        if (lenderName != null)
            offers = offers.stream().filter(o -> o.getLender().getLenderName().equalsIgnoreCase(lenderName)).toList();

        if (lenderType != null)
            offers = offers.stream().filter(o -> o.getLender().getLenderType() == lenderType).toList();

        if (loanType != null)
            offers = offers.stream().filter(o -> o.getLoanType() == loanType).toList();

        if (status != null)
            offers = offers.stream().filter(o -> o.getStatus() == status).toList();

        if (offers.isEmpty())
            throw new ResourceNotFoundException("No Offers Found");

        List<OfferResponse> dtoList =
                offers.stream().map(this::mapToResponse).toList();


        String json =  mapper.writeValueAsString(dtoList);
        return parseJson(json);
    }

    public List<OfferResponse> parseJson(String json) throws JsonProcessingException {
        return mapper.readValue(json, new TypeReference<List<OfferResponse>>() {});
    }

    private OfferResponse mapToResponse(Offer offer) {
        OfferResponse dto = new OfferResponse();

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
