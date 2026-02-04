package com.project.LoanBookingApplication.Kafka.Consumer;

import com.project.LoanBookingApplication.Entity.ApplicationStatus;
import com.project.LoanBookingApplication.Entity.LoanApplication;
import com.project.LoanBookingApplication.Entity.LoanRequest;
import com.project.LoanBookingApplication.Entity.RequestStatus;
import com.project.LoanBookingApplication.Event.LoanApprovedEvent;
import com.project.LoanBookingApplication.Repository.LoanApplicationRepository;
import com.project.LoanBookingApplication.Repository.LoanRequestRepository;
import com.project.LoanBookingApplication.Service.LoanService;
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
    @KafkaListener(topics = "loan-approved", groupId = "loan-group")
    public void handleLoanApproved(LoanApprovedEvent event) {

        LoanApplication application = loanApplicationRepository.findById(event.getApplicationId())
                .orElseThrow(() -> new RuntimeException("Application not found"));

        LoanRequest loanRequest = application.getLoanRequest();

        try {
            loanService.processApprovedLoan(application);
            loanRequest.setRequestStatus(RequestStatus.DONE);
            application.setStatus(ApplicationStatus.APPROVED);
            loanRequest.setErrorMessage(null);

        } catch (Exception e) {
            loanRequest.setRequestStatus(RequestStatus.REJECTED);
            application.setStatus(ApplicationStatus.PENDING);
            loanRequest.setErrorMessage(e.getMessage());
        } finally {
            loanRequestRepository.save(loanRequest);
            loanApplicationRepository.save(application);
        }
    }

}
