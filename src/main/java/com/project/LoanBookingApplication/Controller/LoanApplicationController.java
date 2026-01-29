package com.project.LoanBookingApplication.Controller;

import com.project.LoanBookingApplication.Entity.EmiSchedule;
import com.project.LoanBookingApplication.Entity.LoanApplication;
import com.project.LoanBookingApplication.Service.LoanApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loan")
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    public LoanApplicationController(LoanApplicationService loanApplicationService) {
        this.loanApplicationService = loanApplicationService;
    }

    @PostMapping("/{loanRequestId}/offer/{offerId}")
    public LoanApplication selectOffer(
            @PathVariable Long loanRequestId,
            @PathVariable Long offerId) {

        return loanApplicationService.selectOffer(loanRequestId, offerId);
    }

    @PutMapping("/application/{id}/approve")
    public List<EmiSchedule> updateStatus(@PathVariable Long id) {

        return loanApplicationService.updateStatus(id);
    }
}
