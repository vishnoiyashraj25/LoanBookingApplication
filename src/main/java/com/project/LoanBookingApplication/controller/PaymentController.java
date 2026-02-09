package com.project.LoanBookingApplication.controller;


import com.project.LoanBookingApplication.dto.PaymentRequest;
import com.project.LoanBookingApplication.dto.PaymentResponse;
import com.project.LoanBookingApplication.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/payment")
public class PaymentController {
        private final PaymentService paymentService;
        public PaymentController(PaymentService paymentService){
            this.paymentService = paymentService;
        }
        @PostMapping("/{loanNumber}")
        public PaymentResponse createPayment(@PathVariable String loanNumber, @Valid  @RequestBody PaymentRequest paymentRequest){
            return paymentService.createPayment(loanNumber,paymentRequest);
        }

    @GetMapping
    public List<PaymentResponse> getPayments(
            @RequestParam(required = false) String loanNumber,
            @RequestParam(required = false) Long emiId,
            @RequestParam(required = false) String status) {

        return paymentService.getPayments(loanNumber, emiId, status);
    }
}
