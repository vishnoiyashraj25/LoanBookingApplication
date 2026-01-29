package com.project.LoanBookingApplication.Service;


import com.project.LoanBookingApplication.Entity.EmiSchedule;
import com.project.LoanBookingApplication.Entity.EmiStatus;
import com.project.LoanBookingApplication.Entity.Loan;
import com.project.LoanBookingApplication.Repository.EmiRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmiService {

    private final EmiRepository emiRepository;
    public EmiService(EmiRepository emiRepository){
        this.emiRepository = emiRepository;
    }
    public List<EmiSchedule> createEMI(Loan loan){

        List<EmiSchedule> schedules = new ArrayList<>();

        LocalDate dueDate = loan.getStartDate().plusMonths(1);

        for (int i = 1; i <= loan.getLoanApplication().getTenure(); i++) {

            EmiSchedule emi = new EmiSchedule();
            emi.setLoan(loan);
            emi.setAmount(loan.getEmi());
            emi.setDueDate(dueDate);
            emi.setStatus(EmiStatus.PENDING);

            schedules.add(emi);
            dueDate = dueDate.plusMonths(1);
        }

        emiRepository.saveAll(schedules);
        return schedules;

    }
}
