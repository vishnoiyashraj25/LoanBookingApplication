package com.project.LoanBookingApplication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {
    @NotBlank(message = "Transaction ID is required")
    @Size(min = 6, max = 50, message = "Transaction ID length invalid")
    private String transactionId;
}
