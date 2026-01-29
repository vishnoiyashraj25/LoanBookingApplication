package com.project.LoanBookingApplication.Repository;

import com.project.LoanBookingApplication.Entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
}
