package com.project.LoanBookingApplication.Repository;

import com.project.LoanBookingApplication.Entity.Lender;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LenderRepository extends JpaRepository<Lender, Long> {

}
