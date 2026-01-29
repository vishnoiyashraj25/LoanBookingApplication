package com.project.LoanBookingApplication.Repository;

import com.project.LoanBookingApplication.Entity.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferRepository extends JpaRepository<Offer,Long> {
}
