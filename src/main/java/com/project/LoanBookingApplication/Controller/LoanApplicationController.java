package com.project.LoanBookingApplication.Controller;

import com.project.LoanBookingApplication.DTO.LoanApplicationResponse;
import com.project.LoanBookingApplication.Entity.*;
import com.project.LoanBookingApplication.Service.LoanApplicationService;
import com.project.LoanBookingApplication.Service.LoanService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @PathVariable Long loanRequestId,
            @PathVariable Long offerId) {

        return loanApplicationService.selectOffer(loanRequestId, offerId);
    }

    @PutMapping("/application/{id}/approve")
    public String updateStatus(@PathVariable Long id) {

        return loanApplicationService.updateStatus(id);
    }

    @GetMapping
    public List<LoanApplicationResponse> getApplication(@RequestParam (required = false)ApplicationStatus status, @RequestParam(required = false) String lenderName, @RequestParam(required = false)String panNumber, @RequestParam(required = false)LenderType lenderType){
        return loanApplicationService.getApplication(status,lenderName,panNumber,lenderType);
    }

    @GetMapping("/loans")
    public List<Loan>getLoan(){
        return loanService.getLoan();
    }
}
