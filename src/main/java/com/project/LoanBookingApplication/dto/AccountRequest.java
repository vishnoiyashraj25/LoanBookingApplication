package com.project.LoanBookingApplication.dto;

import com.project.LoanBookingApplication.enums.AccountType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Account number cannot be empty")
    @Pattern(
            regexp = "\\d{9,18}",
            message = "Account number must be 9–18 digits"
    )
    private String accountNumber;

    @NotBlank(message = "Bank name is required")
    private String bank;

    @NotBlank(message = "IFSC is required")
    @Pattern(
            regexp = "^[A-Z]{4}0[A-Z0-9]{6}$",
            message = "Invalid IFSC format"
    )
    private String ifsc;

    @NotNull(message = "Account type is required")
    private AccountType type;
}
