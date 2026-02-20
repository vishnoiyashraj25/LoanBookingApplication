package com.project.LoanBookingApplication.repository;

import com.project.LoanBookingApplication.entity.Loan;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Loan> findByLoanNumber(String loanNumber);

}
