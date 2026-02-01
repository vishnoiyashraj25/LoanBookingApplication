package com.project.LoanBookingApplication.Repository;

import com.project.LoanBookingApplication.Entity.Lender;
import com.project.LoanBookingApplication.Entity.LenderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LenderRepository extends JpaRepository<Lender, Long> {

        List<Lender>findByLenderName(String lenderName);
        List<Lender>findByLenderType(LenderType lenderType);
        List<Lender>findByLenderId(Long lenderId);


}
