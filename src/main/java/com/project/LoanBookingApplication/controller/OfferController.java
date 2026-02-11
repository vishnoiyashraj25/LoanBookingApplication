package com.project.LoanBookingApplication.controller;


import com.project.LoanBookingApplication.dto.OfferRequest;
import com.project.LoanBookingApplication.dto.OfferResponse;
import com.project.LoanBookingApplication.enums.LenderType;
import com.project.LoanBookingApplication.enums.LoanType;
import com.project.LoanBookingApplication.enums.OfferStatus;
import com.project.LoanBookingApplication.service.OfferService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/offers")
public class OfferController {
    private final OfferService offerService;
    public OfferController(OfferService offerService){
        this.offerService = offerService;
    }
    @PostMapping
    public OfferResponse createOffer(@Valid @RequestBody OfferRequest offerRequest){
        return offerService.createOffer(offerRequest);
    }

    @GetMapping
    public List<OfferResponse> getAllOffers(
            @RequestParam(required = false) @Positive(message = "Offer ID must be positive when provided") Long offerId,
            @RequestParam(required = false) String lenderName,
            @RequestParam(required = false) LenderType lenderType,
            @RequestParam(required = false) LoanType loanType,
            @RequestParam(required = false) OfferStatus status
    ) throws Exception {

        return offerService.getAllOffersJson(
                offerId, lenderName, lenderType, loanType, status
        );

    }
}
