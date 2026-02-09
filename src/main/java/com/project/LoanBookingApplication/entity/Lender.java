package com.project.LoanBookingApplication.entity;

import com.project.LoanBookingApplication.enums.LenderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lenders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lender {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lenderId;

    @Column(unique = true, nullable = false)
    private String lenderName;
    @Enumerated(EnumType.STRING)
    private LenderType lenderType;

//    @Enumerated(EnumType.STRING)
//    private LenderStatus lenderStatus;
}
