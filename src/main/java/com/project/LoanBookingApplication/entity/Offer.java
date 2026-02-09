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

    private Integer minTenure;
    private Integer maxTenure;
    private Double interestRate;

    private Double minIncome;
    private Integer minCreditScore;
    private Double maxAmount;

    @Enumerated(EnumType.STRING)
    private LoanType loanType;

    @Enumerated(EnumType.STRING)
    private OfferStatus status;
}
