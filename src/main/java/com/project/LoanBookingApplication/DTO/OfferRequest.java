package com.project.LoanBookingApplication.DTO;

import com.project.LoanBookingApplication.Entity.LoanType;
import com.project.LoanBookingApplication.Entity.OfferStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfferRequest {

    private Long lenderId;
    private Integer minTenure;
    private Integer maxTenure;
    private Double interestRate;

    private Double minIncome;
    private Integer minCreditScore;
    private Double maxAmount;

    private LoanType loanType;
    private OfferStatus status;
}
