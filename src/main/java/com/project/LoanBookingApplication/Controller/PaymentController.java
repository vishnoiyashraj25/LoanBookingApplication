package com.project.LoanBookingApplication.Controller;


import com.project.LoanBookingApplication.DTO.PaymentRequest;
import com.project.LoanBookingApplication.Entity.Payment;
import com.project.LoanBookingApplication.Service.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/payment")
public class PaymentController {
        private final PaymentService paymentService;
        public PaymentController(PaymentService paymentService){
            this.paymentService = paymentService;
        }
        @PostMapping("/{loan_number}")
        public Payment createPayment(@PathVariable String loan_number, @RequestBody PaymentRequest paymentRequest){
            return paymentService.createPayment(loan_number,paymentRequest);
        }

}
