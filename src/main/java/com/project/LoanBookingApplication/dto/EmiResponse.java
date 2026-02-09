package com.project.LoanBookingApplication.dto;

import com.project.LoanBookingApplication.enums.EmiStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class EmiResponse {
    private Long id;
    private String loanNumber;
    private Double amount;
    private LocalDate dueDate;
    private EmiStatus status;
}
