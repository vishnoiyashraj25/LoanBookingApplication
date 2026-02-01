package com.project.LoanBookingApplication.DTO;


import com.project.LoanBookingApplication.Entity.AccountType;
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
