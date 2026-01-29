package com.project.LoanBookingApplication.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@Table(name = "emi_schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmiSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loan_number", nullable = false)
    private Loan loan;

    private Double amount;
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private EmiStatus status;
}
