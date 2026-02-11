package com.project.LoanBookingApplication.controller;

import com.project.LoanBookingApplication.dto.LoanApplicationResponse;
import com.project.LoanBookingApplication.dto.LoanResponse;
import com.project.LoanBookingApplication.dto.LoanStatusResponse;
import com.project.LoanBookingApplication.enums.ApplicationStatus;
import com.project.LoanBookingApplication.enums.LenderType;
import com.project.LoanBookingApplication.service.LoanApplicationService;
import com.project.LoanBookingApplication.service.LoanService;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/loan")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;
    private final LoanService loanService;


    public LoanApplicationController(LoanApplicationService loanApplicationService, LoanService loanService) {
        this.loanApplicationService = loanApplicationService;
        this.loanService = loanService;
    }


    @PostMapping("/{loanRequestId}/offer/{offerId}")
    public LoanApplicationResponse selectOffer(
            @PathVariable @Positive(message = "Loan request ID must be positive") Long loanRequestId,
            @PathVariable @Positive(message = "Offer ID must be positive") Long offerId) {

        return loanApplicationService.selectOffer(loanRequestId, offerId);
    }

    @PutMapping("/application/{id}/submit")
    public LoanStatusResponse updateStatus(
            @PathVariable @Positive(message = "Application ID must be positive") Long id) {

        return loanApplicationService.updateStatus(id);
    }

    @GetMapping("/application/{id}/status")
    public Map<String, String> getLoanStatus(
            @PathVariable @Positive(message = "Application ID must be positive") Long id){
        return loanApplicationService.getLoanStatus(id);
    }

    @GetMapping
    public List<LoanApplicationResponse> getApplication(@RequestParam (required = false) ApplicationStatus status, @RequestParam(required = false) String lenderName, @RequestParam(required = false)String panNumber, @RequestParam(required = false) LenderType lenderType){
        return loanApplicationService.getApplication(status,lenderName,panNumber,lenderType);
    }

    @GetMapping("/loans")
    public List<LoanResponse> getAllLoans() {
        return loanService.getAllLoans();
    }

}
