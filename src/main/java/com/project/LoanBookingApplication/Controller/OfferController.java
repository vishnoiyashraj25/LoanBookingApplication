package com.project.LoanBookingApplication.Controller;


import com.project.LoanBookingApplication.DTO.OfferRequest;
import com.project.LoanBookingApplication.Entity.Offer;
import com.project.LoanBookingApplication.Service.OfferService;
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
    public Offer createOffer(@RequestBody OfferRequest offerRequest){
        return offerService.createOffer(offerRequest);
    }

    @GetMapping("/{offer_id}")
    public Offer getOffer(@PathVariable Long offer_id){
        return offerService.getOffer(offer_id);
    }

    @GetMapping
    public List<Offer> getAllOffers(){
        return offerService.getAllOffers();
    }
}
