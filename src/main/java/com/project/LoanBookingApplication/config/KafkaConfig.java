package com.project.LoanBookingApplication.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

/**
 * Kafka error handling: retry then DLT.
 * Flow: Exception → no ack → DefaultErrorHandler → Retry 1,2,3 → still failing
 *       → DeadLetterPublishingRecoverer → send to &lt;topic&gt;.DLT → commit offset → next record.
 */
@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler errorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate) {

        // Failed records go to <topic>.DLT (e.g. loan-approved.DLT)
        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(kafkaTemplate);

        // Exponential backoff: 3 retries with 2s initial interval, 2x multiplier (2s, 4s, 8s)
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(2000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(16000L);

        return new DefaultErrorHandler(recoverer, backOff);
    }
}
