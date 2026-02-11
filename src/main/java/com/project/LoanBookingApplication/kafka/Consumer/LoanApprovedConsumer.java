package com.project.LoanBookingApplication.kafka.Consumer;

import com.project.LoanBookingApplication.event.LoanApprovedEvent;
import com.project.LoanBookingApplication.service.LoanApplicationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Thin Kafka consumer: only receives the event and delegates to the service.
 * All business logic (create Loan/EMI, update statuses) lives in LoanApplicationService.
 */
@Service
public class LoanApprovedConsumer {

    private final LoanApplicationService loanApplicationService;

    public LoanApprovedConsumer(LoanApplicationService loanApplicationService) {
        this.loanApplicationService = loanApplicationService;
    }

    @KafkaListener(topics = "${kafka.topic.loan-approved}", groupId = "${kafka.group.loan-group}")
    public void handleLoanApproved(LoanApprovedEvent event) {
        loanApplicationService.processApprovedApplication(event.getApplicationId());
    }
}
