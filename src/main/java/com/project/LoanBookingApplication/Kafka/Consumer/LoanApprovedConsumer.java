package com.project.LoanBookingApplication.Kafka.Consumer;

import com.project.LoanBookingApplication.Entity.LoanApplication;
import com.project.LoanBookingApplication.Event.LoanApprovedEvent;
import com.project.LoanBookingApplication.Repository.LoanApplicationRepository;
import com.project.LoanBookingApplication.Service.LoanService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class LoanApprovedConsumer {

    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanService loanService;

    public LoanApprovedConsumer(
            LoanApplicationRepository loanApplicationRepository,
            LoanService loanService) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanService = loanService;
    }

    @KafkaListener(topics = "loan-approved", groupId = "loan-group")
    public void handleLoanApproved(LoanApprovedEvent event) {

        Long applicationId = event.getApplicationId();

        LoanApplication application =
                loanApplicationRepository.findById(applicationId)
                        .orElseThrow();

        // 🔥 heavy work happens here
        loanService.createLoan(application);

        System.out.println("Loan + EMI created for application: " + applicationId);
    }
}
