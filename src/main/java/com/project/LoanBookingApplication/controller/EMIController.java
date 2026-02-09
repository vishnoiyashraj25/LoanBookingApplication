package com.project.LoanBookingApplication.controller;

import com.project.LoanBookingApplication.dto.EmiResponse;
import com.project.LoanBookingApplication.service.EmiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/emi")
public class EMIController {

    private final EmiService emiService;
    public EMIController(EmiService emiService){
        this.emiService = emiService;
    }

    @GetMapping
    public List<EmiResponse> getEMI(@RequestParam(required = false) Long id, @RequestParam(required = false) String loanNumber){
        return emiService.getEMI(id,loanNumber);
    }
}
