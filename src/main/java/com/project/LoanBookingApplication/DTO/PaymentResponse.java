package com.project.LoanBookingApplication.DTO;


import com.project.LoanBookingApplication.Entity.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class PaymentResponse {
    private Long id;
    private String loan_number;
    private Double amount;
    private String transactionId;
    private PaymentStatus status;
    private LocalDateTime receivedAt;
}
