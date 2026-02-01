package com.project.LoanBookingApplication.DTO;


import com.project.LoanBookingApplication.Entity.ApplicationStatus;
import com.project.LoanBookingApplication.Entity.LenderType;
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
