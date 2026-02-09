package com.project.LoanBookingApplication.repository;

import com.project.LoanBookingApplication.entity.*;
import com.project.LoanBookingApplication.enums.LenderType;
import com.project.LoanBookingApplication.enums.LoanType;
import com.project.LoanBookingApplication.enums.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface OfferRepository extends JpaRepository<Offer,Long> {
    List<Offer>findByOfferId(Long offerId);
    List<Offer> findByLender_LenderName(String lenderName);
    List<Offer>findByLender_LenderType(LenderType lenderType);
    List<Offer>findByLoanType(LoanType loanType);
    List<Offer>findByStatus(OfferStatus status);

}
