package com.project.LoanBookingApplication.kafka.Consumer;

import com.project.LoanBookingApplication.event.LoanApprovedEvent;
import com.project.LoanBookingApplication.service.LoanApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Thin Kafka consumer: only receives the event and delegates to the service.
 * All business logic (create Loan/EMI, update statuses) lives in LoanApplicationService.
 */
@Service
public class LoanApprovedConsumer {

    private static final Logger log = LoggerFactory.getLogger(LoanApprovedConsumer.class);

    private final LoanApplicationService loanApplicationService;

    public LoanApprovedConsumer(LoanApplicationService loanApplicationService) {
        this.loanApplicationService = loanApplicationService;
    }

    @KafkaListener(topics = "${kafka.topic.loan-approved}", groupId = "${kafka.group.loan-group}")
    public void handleLoanApproved(LoanApprovedEvent event) {
        try {
            loanApplicationService.processApprovedApplication(event.getApplicationId());
        } catch (Exception e) {
            log.error("Failed to process approved loan application id={}: {}", event.getApplicationId(), e.getMessage(), e);
            throw e;
        }
    }
}
