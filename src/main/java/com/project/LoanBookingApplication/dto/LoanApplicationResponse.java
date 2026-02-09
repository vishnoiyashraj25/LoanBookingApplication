package com.project.LoanBookingApplication.dto;


import com.project.LoanBookingApplication.enums.ApplicationStatus;
import com.project.LoanBookingApplication.enums.LenderType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LoanApplicationResponse {
    private Long id;
    private String userName;
    private String panNumber;
    private String lenderName;
    private LenderType lenderType;
    private ApplicationStatus status;
    private Double emi;
    private Double interestRate;
    private Double loanAmount;
    private Integer tenure;
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
}
