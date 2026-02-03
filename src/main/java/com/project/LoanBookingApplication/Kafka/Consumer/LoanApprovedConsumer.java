package com.project.LoanBookingApplication.Kafka.Consumer;

import com.project.LoanBookingApplication.Entity.LoanApplication;
import com.project.LoanBookingApplication.Entity.LoanRequest;
import com.project.LoanBookingApplication.Entity.RequestStatus;
import com.project.LoanBookingApplication.Event.LoanApprovedEvent;
import com.project.LoanBookingApplication.Repository.LoanApplicationRepository;
import com.project.LoanBookingApplication.Repository.LoanRequestRepository;
import com.project.LoanBookingApplication.Service.LoanService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

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

    @KafkaListener(topics = "loan-approved", groupId = "loan-group")
    public void handleLoanApproved(LoanApprovedEvent event) {

        Long applicationId = event.getApplicationId();

        LoanApplication application =
                loanApplicationRepository.findById(applicationId)
                        .orElseThrow();

//        loanService.createLoan(application);
//        LoanRequest loanRequest = application.getLoanRequest();
//        loanRequest.setRequestStatus(RequestStatus.DONE);
//        loanRequestRepository.save(loanRequest);
        loanService.processApprovedLoan(application);
        System.out.println("Loan + EMI created for application: " + applicationId);
    }
}
