package com.project.LoanBookingApplication.entity;

import com.project.LoanBookingApplication.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @OneToOne
    @JoinColumn(name = "loan_request_id", nullable = false, unique = true)
    private LoanRequest loanRequest;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private Double emi;
    private Double interestRate;
    private Double loanAmount;
    private Integer tenure;

    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;

    @Version
    private Integer version;
}
