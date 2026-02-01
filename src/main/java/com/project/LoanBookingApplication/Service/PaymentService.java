package com.project.LoanBookingApplication.Service;


import com.project.LoanBookingApplication.DTO.PaymentRequest;
import com.project.LoanBookingApplication.Entity.*;
import com.project.LoanBookingApplication.Repository.EmiRepository;
import com.project.LoanBookingApplication.Repository.LoanRepository;
import com.project.LoanBookingApplication.Repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    public Payment createPayment(String loan_number, PaymentRequest paymentRequest) {

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
        return paymentRepository.save(payment);
    }
}

