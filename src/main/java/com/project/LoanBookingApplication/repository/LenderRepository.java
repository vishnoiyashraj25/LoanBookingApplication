package com.project.LoanBookingApplication.repository;

import com.project.LoanBookingApplication.entity.Lender;
import com.project.LoanBookingApplication.enums.LenderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LenderRepository extends JpaRepository<Lender, Long> {

        List<Lender>findByLenderName(String lenderName);
        List<Lender>findByLenderType(LenderType lenderType);
        List<Lender>findByLenderId(Long lenderId);


}
