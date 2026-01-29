package com.project.LoanBookingApplication.Repository;

import com.project.LoanBookingApplication.Entity.LoanRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRequestRepository extends JpaRepository<LoanRequest,Long> {
}
