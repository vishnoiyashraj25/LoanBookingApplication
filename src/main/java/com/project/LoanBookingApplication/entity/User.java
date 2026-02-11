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

    @Column(nullable = false)
    private String userName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false, length = 10)
    private String phoneNumber;

    @Column(unique = true, nullable = false, length = 12)
    private String aadharNumber;

    @Column(unique = true, nullable = false, length = 10)
    private String panNumber;

    @Column(nullable = true) // null = not yet verified
    private Boolean kycVerified;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false, length = 50)
    private String employeeType;

    @Column(nullable = false)
    private Double monthlyIncome;

    @Column(nullable = false)
    private Integer creditScore;
}

// EmployeeType..