package com.project.LoanBookingApplication.entity;

import com.project.LoanBookingApplication.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications", indexes = {
    @Index(name = "idx_loan_applications_offer_id", columnList = "offer_id"),
    @Index(name = "idx_loan_applications_loan_request_id", columnList = "loan_request_id")
})
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

    @ManyToOne
    @JoinColumn(name = "loan_request_id", nullable = false)
    private LoanRequest loanRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(nullable = false)
    private Double emi;

    @Column(nullable = false)
    private Double interestRate;

    @Column(nullable = false)
    private Double loanAmount;

    @Column(nullable = false)
    private Integer tenure;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime expiredAt;

    @Version
    private Integer version;
}

// Indexes on offer_id, loan_request_id is enough for now