package com.project.LoanBookingApplication.DTO;

import com.project.LoanBookingApplication.Entity.AccountType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountRequest {
    private Long userId;
    private String accountNumber;
    private String bank;
    private String ifsc;
    private AccountType type;
}
