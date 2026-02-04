package com.project.LoanBookingApplication.DTO;

import com.project.LoanBookingApplication.Entity.LoanStatus;
import com.project.LoanBookingApplication.Entity.LoanType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class LoanResponse {
    private String loanNumber;
    private String lenderName;
    private String userName;
    private String loanApplicationId;
    private LoanStatus status;
    private LocalDate startDate;
    private LoanType loanType;
    private Double emi;
    private Double interest;
    private Double duesAmount;
    private String disbursementAccountNumber;
}
