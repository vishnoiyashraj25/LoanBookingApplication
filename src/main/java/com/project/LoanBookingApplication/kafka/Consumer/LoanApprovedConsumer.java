package com.project.LoanBookingApplication.kafka.Consumer;

import com.project.LoanBookingApplication.event.LoanApprovedEvent;
import com.project.LoanBookingApplication.service.LoanApplicationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer with manual ack. Flow:
 * Success or BusinessException (reject) → ack() → commit offset.
 * Other exception → no ack → DefaultErrorHandler retries (3x, 2s) → then DLT → commit → next.
 */
@Service
public class LoanApprovedConsumer {

    private final LoanApplicationService loanApplicationService;

    public LoanApprovedConsumer(LoanApplicationService loanApplicationService) {
        this.loanApplicationService = loanApplicationService;
    }

    @KafkaListener(topics = "${kafka.topic.loan-approved}", groupId = "${kafka.group.loan-group}")
    public void handleLoanApproved(LoanApprovedEvent event, Acknowledgment ack) {
        loanApplicationService.processApprovedApplication(event.getApplicationId());
        ack.acknowledge();
    }
}
