package com.project.LoanBookingApplication.Repository;

import com.project.LoanBookingApplication.Entity.EmiSchedule;
import com.project.LoanBookingApplication.Entity.EmiStatus;
import com.project.LoanBookingApplication.Entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmiRepository extends JpaRepository<EmiSchedule,Long> {
    EmiSchedule findFirstByLoanAndStatusOrderByDueDateAsc(
            Loan loan,
            EmiStatus status
    );
}
