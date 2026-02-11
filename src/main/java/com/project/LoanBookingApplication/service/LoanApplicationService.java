package com.project.LoanBookingApplication.service;

import com.project.LoanBookingApplication.dto.LoanApplicationResponse;
import com.project.LoanBookingApplication.dto.LoanStatusResponse;
import com.project.LoanBookingApplication.entity.*;
import com.project.LoanBookingApplication.enums.ApplicationStatus;
import com.project.LoanBookingApplication.enums.LenderType;
import com.project.LoanBookingApplication.enums.RequestStatus;
import com.project.LoanBookingApplication.exception.ResourceNotFoundException;
import com.project.LoanBookingApplication.kafka.Producer.LoanEventProducer;
import com.project.LoanBookingApplication.repository.AccountRepository;
import com.project.LoanBookingApplication.repository.LoanApplicationRepository;
import com.project.LoanBookingApplication.repository.LoanRequestRepository;
import com.project.LoanBookingApplication.repository.OfferRepository;
import com.project.LoanBookingApplication.util.EmiCalculator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoanApplicationService {

    private final LoanRequestRepository loanRequestRepository;
    private final OfferRepository offerRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanService loanService;
    private final LoanEventProducer loanEventProducer;
    private final AccountRepository accountRepository;

    public LoanApplicationService(
            LoanRequestRepository loanRequestRepository,
            OfferRepository offerRepository,
            LoanApplicationRepository loanApplicationRepository,
            LoanService loanService, LoanEventProducer loanEventProducer,AccountRepository accountRepository) {

        this.loanRequestRepository = loanRequestRepository;
        this.offerRepository = offerRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanService = loanService;
        this.loanEventProducer = loanEventProducer;
        this.accountRepository = accountRepository;
    }

    private LoanApplicationResponse mapToResponse(LoanApplication application) {

        LoanApplicationResponse response = new LoanApplicationResponse();

        response.setId(application.getId());

        response.setUserName(
                application.getLoanRequest().getUser().getUserName()
        );
        response.setPanNumber(
                application.getLoanRequest().getUser().getPanNumber()
        );

        response.setLenderName(
                application.getOffer().getLender().getLenderName()
        );
        response.setLenderType(
                application.getOffer().getLender().getLenderType()
        );

        response.setStatus(application.getStatus());
        response.setEmi(application.getEmi());
        response.setInterestRate(application.getInterestRate());
        response.setLoanAmount(application.getLoanAmount());
        response.setTenure(application.getTenure());
        response.setCreatedAt(application.getCreatedAt());
        response.setExpiredAt(application.getExpiredAt());

        return response;
    }

    @Transactional
    public LoanApplicationResponse selectOffer(Long loanRequestId, Long offerId) {

        LoanRequest loanRequest = loanRequestRepository.findById(loanRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan request not found"));

        if (loanApplicationRepository.existsByLoanRequest(loanRequest)) {
            throw new RuntimeException("Application already created for this request");
        }

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found"));

        LoanApplication application = new LoanApplication();
        application.setLoanRequest(loanRequest);
        application.setOffer(offer);
        application.setLoanAmount(loanRequest.getAmount());
        application.setTenure(loanRequest.getTenure());
        application.setInterestRate(offer.getInterestRate());
        application.setStatus(ApplicationStatus.PENDING);
        application.setCreatedAt(LocalDateTime.now());
        application.setExpiredAt(LocalDateTime.now().plusDays(7));
        Double emi = EmiCalculator.calculateEmi(
                loanRequest.getAmount(),
                offer.getInterestRate(),
                loanRequest.getTenure()
        );

        application.setEmi(emi);

        LoanApplication savedApplication =
                loanApplicationRepository.save(application);

        return mapToResponse(savedApplication);
    }


    public LoanStatusResponse updateStatus(Long applicationId) {

        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException("This loan request has already been processed.");
        }

        LoanRequest loanRequest = application.getLoanRequest();
        loanRequest.setRequestStatus(RequestStatus.INPROCESS);
        loanRequest.setErrorMessage(null);
        loanRequestRepository.save(loanRequest);

//        try {
//            User user = loanRequest.getUser();
//            Account account = accountRepository.findByUser(user);
//            if (account == null) {
//                throw new IllegalStateException("User account not found for userId: " + user.getUserId());
//            }
//
//            loanEventProducer.sendLoanApprovedEvent(applicationId);
//
//        } catch (Exception e) {
//            loanRequest.setRequestStatus(RequestStatus.REJECTED);
//            loanRequest.setErrorMessage(e.getMessage());
//            loanRequestRepository.save(loanRequest);
//            application.setStatus(ApplicationStatus.REJECTED);
//            loanApplicationRepository.save(application);
//        }
        loanEventProducer.sendLoanApprovedEvent(applicationId);
        return new LoanStatusResponse(
                applicationId,
                "Your loan application is being processed. Please check status later."
        );
    }

    /**
     * Processes an approved loan application (invoked by Kafka consumer).
     * Creates Loan and EMI entities, then updates application and request status.
     * All business logic lives here; the consumer only delegates to this method.
     */
    @Transactional
    public void processApprovedApplication(Long applicationId) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        LoanRequest loanRequest = application.getLoanRequest();

        try {
            loanService.processApprovedLoan(application);
            loanRequest.setRequestStatus(RequestStatus.DONE);
            application.setStatus(ApplicationStatus.APPROVED);
            loanRequest.setErrorMessage(null);
        } catch (Exception e) {
            loanRequest.setRequestStatus(RequestStatus.REJECTED);
            application.setStatus(ApplicationStatus.REJECTED);
            loanRequest.setErrorMessage(e.getMessage());
        } finally {
            loanRequestRepository.save(loanRequest);
            loanApplicationRepository.save(application);
        }
    }

    public List<LoanApplicationResponse> getApplication(
            ApplicationStatus status,
            String lenderName,
            String panNumber, LenderType lenderType
    ) {

        List<LoanApplication> loanApplications = loanApplicationRepository.findAll();

        if (status != null) {
            loanApplications = loanApplications.stream()
                    .filter(a -> a.getStatus() == status)
                    .toList();
        }

        if (lenderName != null) {
            loanApplications = loanApplications.stream()
                    .filter(a -> a.getOffer()
                            .getLender()
                            .getLenderName()
                            .equalsIgnoreCase(lenderName))
                    .toList();
        }

        if (panNumber != null) {
            loanApplications = loanApplications.stream()
                    .filter(a -> a.getLoanRequest()
                            .getUser()
                            .getPanNumber()
                            .equalsIgnoreCase(panNumber))
                    .toList();
        }
        if (lenderType != null) {
            loanApplications = loanApplications.stream()
                    .filter(a -> a.getOffer()
                            .getLender()
                            .getLenderType() == lenderType)
                    .toList();
        }

        if (loanApplications.isEmpty()) {
            throw new ResourceNotFoundException("No Loan Applications found");
        }

        return loanApplications.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Map<String, String> getLoanStatus(Long id){
        LoanApplication application = loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        LoanRequest request = application.getLoanRequest();
        Map<String, String> response = new HashMap<>();
        response.put("status", request.getRequestStatus().name());

        if (request.getRequestStatus() == RequestStatus.REJECTED) {
            response.put("message", request.getErrorMessage());
        } else if (request.getRequestStatus() == RequestStatus.DONE) {
            response.put("message", "Loan approved successfully");
        } else if (request.getRequestStatus() == RequestStatus.INPROCESS) {
            response.put("message", "Loan processing in progress");
        } else {
            response.put("message", "Loan request is active, not yet processed");
        }
        return response;

    }
}
