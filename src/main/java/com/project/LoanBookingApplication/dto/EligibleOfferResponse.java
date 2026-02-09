package com.project.LoanBookingApplication.dto;
import com.project.LoanBookingApplication.enums.LenderType;
import com.project.LoanBookingApplication.enums.LoanType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EligibleOfferResponse {

    private Long offerId;
    private Long lenderId;
    private String lenderName;
    private LenderType lenderType;
    private Integer tenure;
    private Double interestRate;
    private Double LoanAmount;
    private LoanType loanType;
    private Double emi;
}
