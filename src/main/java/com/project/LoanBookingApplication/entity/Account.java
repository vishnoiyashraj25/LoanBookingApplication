package com.project.LoanBookingApplication.entity;

import com.project.LoanBookingApplication.enums.AccountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "accounts" , indexes = @Index(name = "idx_accounts_user_id", columnList = "user_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(unique = true, nullable = false, length = 18)
    private String accountNumber;

    @Column(nullable = false)
    private String bank;

    @Column(nullable = false, length = 11)
    private String ifsc;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;
}






