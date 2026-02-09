package com.project.LoanBookingApplication.repository;

import com.project.LoanBookingApplication.entity.LoanRequest;
import com.project.LoanBookingApplication.enums.RequestStatus;
import com.project.LoanBookingApplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanRequestRepository extends JpaRepository<LoanRequest,Long>{
    boolean existsByUserAndRequestStatus(User user, RequestStatus requestStatus);
    Optional<LoanRequest>findByUser(User user);
    Optional<LoanRequest> findFirstByUserAndRequestStatus(User user, RequestStatus requestStatus);
}
