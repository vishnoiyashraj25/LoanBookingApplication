package com.project.LoanBookingApplication.controller;
import com.project.LoanBookingApplication.dto.LoanRequest;
import com.project.LoanBookingApplication.dto.LoanRequestResponse;
import com.project.LoanBookingApplication.dto.EligibleOfferResponse;
import com.project.LoanBookingApplication.enums.RequestStatus;
import com.project.LoanBookingApplication.service.EligibleOfferService;
import com.project.LoanBookingApplication.service.LoanRequestService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Validated
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
    public LoanRequestResponse createLoanRequest(@Valid @RequestBody LoanRequest loanRequest){
        return loanRequestService.createLoanRequest(loanRequest);
    }

    @GetMapping("/offers/{requestId}")
    public List<EligibleOfferResponse> getOffers(
            @PathVariable @Positive(message = "Request ID must be positive") Long requestId){
        return eligibleOfferService.getOffers(requestId);
    }

    @GetMapping("/requests")
    public List<LoanRequestResponse> getLoanRequest(
    ){
        return loanRequestService.getLoanRequest();
    }
}
