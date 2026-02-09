package com.project.LoanBookingApplication.dto;


import com.project.LoanBookingApplication.enums.AccountType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountResponse {

    private Long id;
    private String userName;
    private String panNumber;
    private String accountNumber;
    private String bank;
    private String ifsc;
    private AccountType accountType;
}
