package com.project.LoanBookingApplication.Kafka.Producer;

import com.project.LoanBookingApplication.Event.LoanApprovedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoanEventProducer {

    private static final String TOPIC = "loan-approved";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public LoanEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendLoanApprovedEvent(Long applicationId) {
        LoanApprovedEvent event = new LoanApprovedEvent(applicationId);
        kafkaTemplate.send(TOPIC, event);
    }
}