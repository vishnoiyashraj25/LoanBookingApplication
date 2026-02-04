package com.project.LoanBookingApplication.Service;


import com.project.LoanBookingApplication.DTO.PaymentRequest;
import com.project.LoanBookingApplication.DTO.PaymentResponse;
import com.project.LoanBookingApplication.Entity.*;
import com.project.LoanBookingApplication.Repository.EmiRepository;
import com.project.LoanBookingApplication.Repository.LoanRepository;
import com.project.LoanBookingApplication.Repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final EmiRepository emiRepository;
    private final LoanRepository loanRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          EmiRepository emiRepository,
                          LoanRepository loanRepository) {
        this.paymentRepository = paymentRepository;
        this.emiRepository = emiRepository;
        this.loanRepository = loanRepository;
    }

    PaymentResponse mapToDto(Payment payment) {
        PaymentResponse dto = new PaymentResponse();
        dto.setId(payment.getId());
        dto.setLoanNumber(payment.getLoan().getLoanNumber());
        dto.setEmiId(payment.getEmiSchedule().getId());
        dto.setAmount(payment.getAmount());
        dto.setTransactionId(payment.getTransactionId());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setReceivedAt(payment.getReceivedAt());
        return dto;
    }


    public PaymentResponse createPayment(String loan_number, PaymentRequest paymentRequest) {

        Loan loan = loanRepository.findById(loan_number)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        EmiSchedule emiSchedule =
                emiRepository.findFirstByLoanAndStatusOrderByDueDateAsc(
                        loan,
                        EmiStatus.PENDING
                );

        if (emiSchedule == null) {
            throw new RuntimeException("No pending EMI for this loan");
        }

        Payment payment = new Payment();
        payment.setLoan(loan);
        payment.setEmiSchedule(emiSchedule);
        payment.setAmount(emiSchedule.getAmount());
        payment.setTransactionId(paymentRequest.getTransactionId());
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setReceivedAt(LocalDateTime.now());
        emiSchedule.setStatus(EmiStatus.PAID);
        emiRepository.save(emiSchedule);
        loan.setDuesAmount(loan.getDuesAmount() - emiSchedule.getAmount());
        if(loan.getDuesAmount()==0){
            loan.setStatus(LoanStatus.Closed);
        }
        loanRepository.save(loan);
        Payment savedpayment = paymentRepository.save(payment);
        return mapToDto(savedpayment);
    }

    public List<PaymentResponse> getPayments(String loanNumber, Long emiId, String statusStr) {

        List<Payment> payments = paymentRepository.findAll();

        if (loanNumber != null) {
            payments = payments.stream()
                    .filter(p -> p.getLoan().getLoanNumber().equalsIgnoreCase(loanNumber))
                    .toList();
        }

        if (emiId != null) {
            payments = payments.stream()
                    .filter(p -> p.getEmiSchedule().getId().equals(emiId))
                    .toList();
        }

        if (statusStr != null) {
            PaymentStatus status = PaymentStatus.valueOf(statusStr.toUpperCase());
            payments = payments.stream()
                    .filter(p -> p.getPaymentStatus() == status)
                    .toList();
        }

        return payments.stream()
                .map(this::mapToDto)
                .toList();
    }

}

