package com.project.LoanBookingApplication.entity;

import com.project.LoanBookingApplication.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loan_number", nullable = false)
    private Loan loan;

    @ManyToOne
    @JoinColumn(name = "emi_id", nullable = false)
    private EmiSchedule emiSchedule;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false, length = 50)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private LocalDateTime receivedAt;
}

