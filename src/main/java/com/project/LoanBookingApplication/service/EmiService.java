package com.project.LoanBookingApplication.service;


import com.project.LoanBookingApplication.dto.EmiResponse;
import com.project.LoanBookingApplication.entity.EmiSchedule;
import com.project.LoanBookingApplication.enums.EmiStatus;
import com.project.LoanBookingApplication.entity.Loan;
import com.project.LoanBookingApplication.exception.ResourceNotFoundException;
import com.project.LoanBookingApplication.repository.EmiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmiService {

    private final EmiRepository emiRepository;
    public EmiService(EmiRepository emiRepository){
        this.emiRepository = emiRepository;
    }
    
    public List<EmiResponse> createEMI(Loan loan){

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

        List<EmiSchedule>emiSchedules = emiRepository.saveAll(schedules);
        return emiSchedules.stream()
                .map(this::mapToResponse)
                .toList();

    }

    public List<EmiResponse> getEMI(Long id, String loanNumber) {

        List<EmiSchedule> emiSchedules = emiRepository.findAll();

        if (id != null) {
            emiSchedules = emiSchedules.stream()
                    .filter(e -> e.getId().equals(id))
                    .toList();
        }

        if (loanNumber != null) {
            emiSchedules = emiSchedules.stream()
                    .filter(e -> e.getLoan().getLoanNumber().equals(loanNumber))
                    .toList();
        }

        if (emiSchedules.isEmpty()) {
            throw new ResourceNotFoundException("No emi exists");
        }

        return emiSchedules.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private EmiResponse mapToResponse(EmiSchedule emiSchedule) {
        EmiResponse emiResponse = new EmiResponse();
        emiResponse.setId(emiSchedule.getId());
        emiResponse.setLoanNumber(emiSchedule.getLoan().getLoanNumber());
        emiResponse.setAmount(emiSchedule.getAmount());
        emiResponse.setDueDate(emiSchedule.getDueDate());
        emiResponse.setStatus(emiSchedule.getStatus());
        return emiResponse;
    }

}
