package com.project.LoanBookingApplication.repository;

import com.project.LoanBookingApplication.enums.ApplicationStatus;
import com.project.LoanBookingApplication.entity.LoanApplication;
import com.project.LoanBookingApplication.entity.LoanRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication>findByStatus(ApplicationStatus status);
    List<LoanApplication>findByOffer_Lender_LenderName(String lenderName);
    List<LoanApplication>findByLoanRequest_User_PanNumber(String panNumber);
    boolean existsByLoanRequest(LoanRequest loanRequest);

}
