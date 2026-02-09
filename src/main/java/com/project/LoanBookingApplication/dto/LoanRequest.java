package com.project.LoanBookingApplication.dto;

import com.project.LoanBookingApplication.enums.LoanType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoanRequest {

    @NotNull(message = "User ID is required")
    private Long userid;

    @NotNull(message = "Loan amount is required")
    @Positive(message = "Loan amount must be positive")
    @Max(value = 10000000, message = "Loan amount too large")
    private Double amount;

    @NotNull(message = "Tenure is required")
    @Min(value = 1, message = "Minimum tenure is 1 month")
    @Max(value = 360, message = "Maximum tenure is 360 months")
    private Integer tenure;

    @NotNull(message = "Loan type is required")
    private LoanType loanType;
}
