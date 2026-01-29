package com.project.LoanBookingApplication.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Loan {

    @Id
    @Column(name = "loan_number", length = 30, nullable = false)
    private String loanNumber;

    @ManyToOne
    @JoinColumn(name = "lender_id", nullable = false)
    private Lender lender;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "loan_application_id", nullable = false)
    private LoanApplication loanApplication;

    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private LoanType loanType;
    private Double emi;
    private Double interest;
    private Double duesAmount;

    @ManyToOne
    @JoinColumn(name = "disbursement_acc_id")
    private Account disbursementAccount;
}
