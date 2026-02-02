package com.project.LoanBookingApplication.Repository;

import com.project.LoanBookingApplication.Entity.LoanRequest;
import com.project.LoanBookingApplication.Entity.RequestStatus;
import com.project.LoanBookingApplication.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanRequestRepository extends JpaRepository<LoanRequest,Long>{
    boolean existsByUserAndRequestStatus(User user, RequestStatus requestStatus);
}
