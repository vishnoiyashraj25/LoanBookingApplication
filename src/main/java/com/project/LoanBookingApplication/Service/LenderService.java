package com.project.LoanBookingApplication.Service;


import com.project.LoanBookingApplication.DTO.LenderRequest;
import com.project.LoanBookingApplication.Entity.Lender;
import com.project.LoanBookingApplication.Entity.LenderStatus;
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
    public Lender getLender(Long lenderid){
        return lenderRepository.findById(lenderid).orElseThrow();
    }

    public List<Lender> getAllLenders(){
            return lenderRepository.findAll();
    }

}
