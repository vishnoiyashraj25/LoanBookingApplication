package com.project.LoanBookingApplication.dto;


import com.project.LoanBookingApplication.enums.LoanType;
import com.project.LoanBookingApplication.enums.RequestStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoanRequestResponse {

    private Long id;
    private String userName;
    private String panNumber;
    private Double amount;
    private Integer tenure;
    private LoanType loanType;
    private RequestStatus requestStatus;
}
