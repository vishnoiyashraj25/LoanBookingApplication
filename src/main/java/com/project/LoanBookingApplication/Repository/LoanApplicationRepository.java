package com.project.LoanBookingApplication.Repository;

import com.project.LoanBookingApplication.Entity.ApplicationStatus;
import com.project.LoanBookingApplication.Entity.LoanApplication;
import com.project.LoanBookingApplication.Entity.LoanRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication>findByStatus(ApplicationStatus status);
    List<LoanApplication>findByOffer_Lender_LenderName(String lenderName);
    List<LoanApplication>findByLoanRequest_User_PanNumber(String panNumber);
    boolean existsByLoanRequest(LoanRequest loanRequest);

}
