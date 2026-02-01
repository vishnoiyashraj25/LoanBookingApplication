package com.project.LoanBookingApplication.Repository;

import com.project.LoanBookingApplication.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByUserId(Long userId);

    List<User> findByEmployeeType(String employeeType);

    List<User> findByKycVerified(Boolean kycVerified);

    List<User> findByUserIdAndEmployeeType(Long userId, String employeeType);

    List<User> findByUserIdAndKycVerified(Long userId, Boolean kycVerified);

    List<User> findByEmployeeTypeAndKycVerified(String employeeType, Boolean kycVerified);

    List<User> findByUserIdAndEmployeeTypeAndKycVerified(
            Long userId,
            String employeeType,
            Boolean kycVerified
    );

}

