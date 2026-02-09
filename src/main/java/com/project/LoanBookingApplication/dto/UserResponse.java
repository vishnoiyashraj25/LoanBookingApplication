package com.project.LoanBookingApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long userId;
    private String userName;
    private String email;
    private String phoneNumber;

    private Boolean kycVerified;
    private Integer age;

    private String employeeType;
    private Double monthlyIncome;
    private Integer creditScore;
}
