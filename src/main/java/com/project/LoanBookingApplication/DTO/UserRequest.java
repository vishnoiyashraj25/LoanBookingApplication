package com.project.LoanBookingApplication.DTO;

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

    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Invalid Indian phone number"
    )
    private String phoneNumber;

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

    @Min(18)
    @Max(75)
    private Integer age;

    @NotBlank(message = "Employee Type is required")
    private String employeeType;

    @NotNull
    @Positive(message = "Income must be positive")
    private Double monthlyIncome;

    @Min(300)
    @Max(900)
    private Integer creditScore;
}
