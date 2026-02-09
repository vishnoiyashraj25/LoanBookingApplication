package com.project.LoanBookingApplication.kafka.Producer;

import com.project.LoanBookingApplication.event.LoanApprovedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoanEventProducer {

    @Value("${kafka.topic.loan-approved}")
    private String loanApprovedTopic;


    private final KafkaTemplate<String, Object> kafkaTemplate;

    public LoanEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendLoanApprovedEvent(Long applicationId) {
        LoanApprovedEvent event = new LoanApprovedEvent(applicationId);
        kafkaTemplate.send(loanApprovedTopic, event);
    }
}