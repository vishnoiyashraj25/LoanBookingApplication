package com.project.LoanBookingApplication.Controller;


import com.project.LoanBookingApplication.DTO.PaymentRequest;
import com.project.LoanBookingApplication.DTO.PaymentResponse;
import com.project.LoanBookingApplication.Entity.Payment;
import com.project.LoanBookingApplication.Service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/payment")
public class PaymentController {
        private final PaymentService paymentService;
        public PaymentController(PaymentService paymentService){
            this.paymentService = paymentService;
        }
        @PostMapping("/{loan_number}")
        public PaymentResponse createPayment(@PathVariable String loan_number, @RequestBody PaymentRequest paymentRequest){
            return paymentService.createPayment(loan_number,paymentRequest);
        }

    @GetMapping
    public List<PaymentResponse> getPayments(
            @RequestParam(required = false) String loanNumber,
            @RequestParam(required = false) Long emiId,
            @RequestParam(required = false) String status) {

        return paymentService.getPayments(loanNumber, emiId, status);
    }
}
