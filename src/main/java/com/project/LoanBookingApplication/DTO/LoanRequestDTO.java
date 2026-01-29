package com.project.LoanBookingApplication.DTO;

import com.project.LoanBookingApplication.Entity.LoanType;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class LoanRequestDTO {
    private Long userid;
    private Double amount;
    private Integer tenure;
    private LoanType loanType;
}
