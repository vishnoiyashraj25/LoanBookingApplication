package com.project.LoanBookingApplication.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {

    @NotBlank(message = "User Name is required")
    private String userName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Invalid Indian phone number"
    )
    private String phoneNumber;

    @NotBlank(message = "Aadhar number is required")
    @Pattern(
            regexp = "\\d{12}",
            message = "Aadhar must be 12 digits"
    )
    private String aadharNumber;

    @NotBlank(message = "PAN number is required")
    @Pattern(
            regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}",
            message = "Invalid PAN format"
    )
    private String panNumber;

    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 75, message = "Age must be at most 75")
    private Integer age;

    @NotBlank(message = "Employee Type is required")
    private String employeeType;

    @NotNull(message = "Monthy Income is required")
    @Positive(message = "Income must be positive")
    private Double monthlyIncome;

    @NotNull(message = "Credit score is required")
    @Min(value = 300, message = "Credit score must be between 300 and 900")
    @Max(value = 900, message = "Credit score must be between 300 and 900")
    private Integer creditScore;
}
