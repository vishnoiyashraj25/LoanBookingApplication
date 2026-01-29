package com.project.LoanBookingApplication.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest{
    private String userName;
    private String email;
    private String phoneNumber;
    private String aadharNumber;
    private String panNumber;
    private Integer age;
    private String employeeType;
    private Double monthlyIncome;
    private Integer creditScore;
}
