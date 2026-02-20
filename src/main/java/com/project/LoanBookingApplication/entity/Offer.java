package com.project.LoanBookingApplication.entity;

import com.project.LoanBookingApplication.enums.LoanType;
import com.project.LoanBookingApplication.enums.OfferStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "offers", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"lender_id", "loan_type", "min_tenure", "max_tenure", "interest_rate", "max_amount"})
})

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long offerId;

    @ManyToOne
    @JoinColumn(name = "lender_id", nullable = false)
    private Lender lender;

    @Column(nullable = false)
    private Integer minTenure;

    @Column(nullable = false)
    private Integer maxTenure;

    @Column(nullable = false)
    private Double interestRate;

    @Column(nullable = false)
    private Double minIncome;

    @Column(nullable = false)
    private Integer minCreditScore;

    @Column(nullable = false)
    private Double maxAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanType loanType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OfferStatus status;
}
