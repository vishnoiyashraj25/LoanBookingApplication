package com.project.LoanBookingApplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@EnableCaching
@SpringBootApplication
public class LoanBookingApplication {

	public static void main(String[] args) {
        System.out.println("hello");
        SpringApplication.run(LoanBookingApplication.class, args);
	}
}