package com.project.LoanBookingApplication.Service;


import com.project.LoanBookingApplication.DTO.LenderRequest;
import com.project.LoanBookingApplication.Entity.Lender;
import com.project.LoanBookingApplication.Entity.LenderStatus;
import com.project.LoanBookingApplication.Entity.LenderType;
import com.project.LoanBookingApplication.Exception.ResourceNotFoundException;
import com.project.LoanBookingApplication.Repository.LenderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LenderService {

    private final LenderRepository lenderRepository;

    public LenderService(LenderRepository lenderRepository){
        this.lenderRepository = lenderRepository;
    }

    public Lender registerLender(LenderRequest request){
        Lender lender = new Lender();
        lender.setLenderName(request.getLenderName());
        lender.setLenderType(request.getLenderType());

        return lenderRepository.save(lender);
    }
//    public Lender getLender(Long lenderid){
//        return lenderRepository.findById(lenderid).orElseThrow();
//    }

    public List<Lender> getAllLenders(Long lenderId, String lenderName, LenderType lenderType) {

        List<Lender> lenders = lenderRepository.findAll();

        if (lenderId != null) {
            lenders = lenders.stream()
                    .filter(l -> l.getLenderId().equals(lenderId))
                    .toList();
        }

        if (lenderName != null) {
            lenders = lenders.stream()
                    .filter(l -> l.getLenderName().equalsIgnoreCase(lenderName))
                    .toList();
        }

        if (lenderType != null) {
            lenders = lenders.stream()
                    .filter(l -> l.getLenderType() == lenderType)
                    .toList();
        }

        if (lenders.isEmpty()) {
            throw new ResourceNotFoundException("No Lender found");
        }

        return lenders;
    }

}
