package com.project.LoanBookingApplication.Controller;


import com.project.LoanBookingApplication.DTO.OfferRequest;
import com.project.LoanBookingApplication.DTO.OfferResponseDTO;
import com.project.LoanBookingApplication.Entity.*;
import com.project.LoanBookingApplication.Service.OfferService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/offers")
public class OfferController {
    private final OfferService offerService;
    public OfferController(OfferService offerService){
        this.offerService = offerService;
    }
    @PostMapping
    public OfferResponseDTO createOffer(@Valid @RequestBody OfferRequest offerRequest){
        return offerService.createOffer(offerRequest);
    }

//    @GetMapping("/{offer_id}")
//    public Offer getOffer(@PathVariable Long offer_id){
//        return offerService.getOffer(offer_id);
//    }

    @GetMapping
    public List<OfferResponseDTO> getAllOffers(@RequestParam(required = false) Long offerId, @RequestParam(required = false)String lenderName, @RequestParam(required = false)LenderType lenderType, @RequestParam(required = false)LoanType loanType, @RequestParam(required = false) OfferStatus status){
        return offerService.getAllOffers(offerId,lenderName,lenderType,loanType,status);
    }
}
