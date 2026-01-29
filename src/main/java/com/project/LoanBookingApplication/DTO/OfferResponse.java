package com.project.LoanBookingApplication.DTO;
import com.project.LoanBookingApplication.Entity.LoanType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OfferResponse {

    private Long offerId;
    private Long lenderId;
    private Integer tenure;
    private Double interestRate;
    private Double LoanAmount;
    private LoanType loanType;
    private Double emi;
}
