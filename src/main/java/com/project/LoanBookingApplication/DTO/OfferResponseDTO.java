package com.project.LoanBookingApplication.DTO;

import com.project.LoanBookingApplication.Entity.LenderType;
import com.project.LoanBookingApplication.Entity.LoanType;
import com.project.LoanBookingApplication.Entity.OfferStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfferResponseDTO {

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
