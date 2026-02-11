package com.project.LoanBookingApplication.controller;


import com.project.LoanBookingApplication.dto.PaymentRequest;
import com.project.LoanBookingApplication.dto.PaymentResponse;
import com.project.LoanBookingApplication.service.PaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("api/payment")
public class PaymentController {
        private final PaymentService paymentService;
        public PaymentController(PaymentService paymentService){
            this.paymentService = paymentService;
        }
        @PostMapping("/{loanNumber}")
        public PaymentResponse createPayment(
                @PathVariable @NotBlank(message = "Loan number is required") @Size(min = 1, max = 30) String loanNumber,
                @Valid @RequestBody PaymentRequest paymentRequest){
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
