package com.project.LoanBookingApplication.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String userName;
    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String phoneNumber;

    @Column(unique = true)
    private String aadharNumber;

    @Column(unique = true)
    private String panNumber;

    private Boolean kycVerified;
    private Integer age;

    private String employeeType;
    private Double monthlyIncome;
    private Integer creditScore;
}

// EmployeeType..