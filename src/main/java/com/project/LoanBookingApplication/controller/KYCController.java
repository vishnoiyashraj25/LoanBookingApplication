package com.project.LoanBookingApplication.controller;

import com.project.LoanBookingApplication.entity.User;
import com.project.LoanBookingApplication.service.KYCService;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/kyc/verify")
public class KYCController {

    private final KYCService kycService;
    public KYCController (KYCService kycService){
        this.kycService = kycService;
    }
    @PostMapping("/{userid}")
    public User verifyKYC(@PathVariable @Positive(message = "User ID must be positive") Long userid){
        return kycService.verifyKYC(userid);
    }
}
