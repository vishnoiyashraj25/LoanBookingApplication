package com.project.LoanBookingApplication.repository;

import com.project.LoanBookingApplication.entity.EmiSchedule;
import com.project.LoanBookingApplication.enums.EmiStatus;
import com.project.LoanBookingApplication.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmiRepository extends JpaRepository<EmiSchedule,Long> {
    EmiSchedule findFirstByLoanAndStatusOrderByDueDateAsc(
            Loan loan,
            EmiStatus status
    );
}
