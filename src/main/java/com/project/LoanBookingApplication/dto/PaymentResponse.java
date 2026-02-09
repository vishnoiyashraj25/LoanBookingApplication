package com.project.LoanBookingApplication.dto;


import com.project.LoanBookingApplication.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class PaymentResponse {
    private Long id;
    private String loanNumber;
    private Long emiId;
    private Double amount;
    private String transactionId;
    private PaymentStatus paymentStatus;
    private LocalDateTime receivedAt;
}
