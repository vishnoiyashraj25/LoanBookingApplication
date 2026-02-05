package com.project.LoanBookingApplication.Repository;

import com.project.LoanBookingApplication.Entity.LoanRequest;
import com.project.LoanBookingApplication.Entity.RequestStatus;
import com.project.LoanBookingApplication.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoanRequestRepository extends JpaRepository<LoanRequest,Long>{
    boolean existsByUserAndRequestStatus(User user, RequestStatus requestStatus);
    LoanRequest findByUser(User user);
    Optional<LoanRequest> findFirstByUserAndRequestStatus(User user, RequestStatus requestStatus);
}
