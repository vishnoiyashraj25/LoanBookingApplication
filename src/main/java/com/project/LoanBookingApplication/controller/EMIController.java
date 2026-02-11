package com.project.LoanBookingApplication.controller;

import com.project.LoanBookingApplication.dto.EmiResponse;
import com.project.LoanBookingApplication.service.EmiService;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/emi")
public class EMIController {

    private final EmiService emiService;
    public EMIController(EmiService emiService){
        this.emiService = emiService;
    }

    @GetMapping
    public List<EmiResponse> getEMI(
            @RequestParam(required = false) @Positive(message = "EMI ID must be positive when provided") Long id,
            @RequestParam(required = false) @Size(min = 1, max = 30) String loanNumber){
        return emiService.getEMI(id, loanNumber);
    }
}
