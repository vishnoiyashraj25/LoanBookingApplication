package com.project.LoanBookingApplication.kafka.Consumer;

import com.project.LoanBookingApplication.enums.ApplicationStatus;
import com.project.LoanBookingApplication.entity.LoanApplication;
import com.project.LoanBookingApplication.entity.LoanRequest;
import com.project.LoanBookingApplication.enums.RequestStatus;
import com.project.LoanBookingApplication.event.LoanApprovedEvent;
import com.project.LoanBookingApplication.exception.ResourceNotFoundException;
import com.project.LoanBookingApplication.repository.LoanApplicationRepository;
import com.project.LoanBookingApplication.repository.LoanRequestRepository;
import com.project.LoanBookingApplication.service.LoanService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanApprovedConsumer {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanService loanService;
    private final LoanRequestRepository loanRequestRepository;

    public LoanApprovedConsumer(
            LoanApplicationRepository loanApplicationRepository,
            LoanService loanService , LoanRequestRepository loanRequestRepository) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanService = loanService;
        this.loanRequestRepository = loanRequestRepository;
    }

    @Transactional
    @KafkaListener(topics = "${kafka.topic.loan-approved}", groupId = "${kafka.group.loan-group}")
    public void handleLoanApproved(LoanApprovedEvent event) {
        LoanApplication application = loanApplicationRepository.findById(event.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        LoanRequest loanRequest = application.getLoanRequest();

        try {
            loanService.processApprovedLoan(application);
            loanRequest.setRequestStatus(RequestStatus.DONE);
            application.setStatus(ApplicationStatus.APPROVED);
            loanRequest.setErrorMessage(null);

        } catch (Exception e) {
            loanRequest.setRequestStatus(RequestStatus.REJECTED);
            application.setStatus(ApplicationStatus.REJECTED);
            loanRequest.setErrorMessage(e.getMessage());
        } finally {
            loanRequestRepository.save(loanRequest);
            loanApplicationRepository.save(application);
        }
    }

}
