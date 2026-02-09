package com.project.LoanBookingApplication.dto;

import com.project.LoanBookingApplication.enums.LenderType;
import com.project.LoanBookingApplication.enums.LoanType;
import com.project.LoanBookingApplication.enums.OfferStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfferResponse {

    private Long offerId;
    private String lenderName;
    private LenderType lenderType;

    private Integer minTenure;
    private Integer maxTenure;
    private Double interestRate;

    private Double minIncome;
    private Integer minCreditScore;
    private Double maxAmount;

    private LoanType loanType;
    private OfferStatus status;
}
