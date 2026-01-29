package com.project.LoanBookingApplication.Controller;

import com.project.LoanBookingApplication.Entity.User;
import com.project.LoanBookingApplication.Service.KYCService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/kyc/verify")
public class KYCController {

    private final KYCService kycService;
    public KYCController (KYCService kycService){
        this.kycService = kycService;
    }
    @PostMapping("/{userid}")
    public User verifyKYC(@PathVariable Long userid){
        return kycService.verifyKYC(userid);
    }
}
