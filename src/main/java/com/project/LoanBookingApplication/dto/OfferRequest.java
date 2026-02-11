package com.project.LoanBookingApplication.dto;

import com.project.LoanBookingApplication.enums.LoanType;
import com.project.LoanBookingApplication.enums.OfferStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfferRequest {

    @NotNull(message = "Lender ID is required")
    @Positive(message = "Lender ID must be positive")
    private Long lenderId;

    @NotNull(message = "Minimum tenure is required")
    @Min(value = 1, message = "Minimum tenure must be at least 1 month")
    private Integer minTenure;

    @NotNull(message = "Maximum tenure is required")
    @Min(value = 1, message = "Maximum tenure must be at least 1 month")
    @Max(value = 360, message = "Maximum tenure must be at most 360 months")
    private Integer maxTenure;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.1", message = "Interest rate too low")
    @DecimalMax(value = "50.0", message = "Interest rate too high")
    private Double interestRate;

    @NotNull(message = "Minimum Income is required")
    @Positive
    private Double minIncome;

    @NotNull(message = "Minimum Credit Score is required")
    @Min(300)
    @Max(900)
    private Integer minCreditScore;

    @NotNull(message = "Maximum Loan Amount is required")
    @Positive
    private Double maxAmount;

    @NotNull(message = "Loan Type is required")
    private LoanType loanType;

    @NotNull(message = "Offer status is required")
    private OfferStatus status;

    @AssertTrue(message = "Minimum tenure must be less than or equal to maximum tenure")
    public boolean isTenureRangeValid() {
        if (minTenure == null || maxTenure == null) return true;
        return minTenure <= maxTenure;
    }
}
