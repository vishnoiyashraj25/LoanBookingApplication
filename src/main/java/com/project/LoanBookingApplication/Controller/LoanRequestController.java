package com.project.LoanBookingApplication.Controller;

import com.project.LoanBookingApplication.DTO.LoanRequestDTO;
import com.project.LoanBookingApplication.DTO.OfferRequest;
import com.project.LoanBookingApplication.DTO.OfferResponse;
import com.project.LoanBookingApplication.Entity.LoanRequest;
import com.project.LoanBookingApplication.Entity.User;
import com.project.LoanBookingApplication.Service.EligibleOfferService;
import com.project.LoanBookingApplication.Service.LoanRequestService;
//import com.project.LoanBookingApplication.Service.OfferService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan")
public class LoanRequestController {
    private final LoanRequestService loanRequestService;
    private final EligibleOfferService eligibleOfferService;
    public LoanRequestController(LoanRequestService loanRequestService, EligibleOfferService eligibleOfferService){
        this.loanRequestService = loanRequestService;
        this.eligibleOfferService = eligibleOfferService;
    }

    @PostMapping("/request")
    public LoanRequest requestLoan(@Valid @RequestBody LoanRequestDTO loanRequestDTO){
        return loanRequestService.requestLoan(loanRequestDTO);
    }
    @GetMapping("/offers/{requestid}")
    public List<OfferResponse> getOffers(@PathVariable Long requestid){
        return eligibleOfferService.getOffers(requestid);
    }
}

// push push