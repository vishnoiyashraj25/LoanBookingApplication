package com.project.LoanBookingApplication.Repository;

import com.project.LoanBookingApplication.Entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfferRepository extends JpaRepository<Offer,Long> {
    List<Offer>findByOfferId(Long offerId);
    List<Offer> findByLender_LenderName(String lenderName);
    List<Offer>findByLender_LenderType(LenderType lenderType);
    List<Offer>findByLoanType(LoanType loanType);
    List<Offer>findByStatus(OfferStatus status);

}
