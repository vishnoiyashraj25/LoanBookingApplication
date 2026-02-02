package com.project.LoanBookingApplication.Service;

import com.project.LoanBookingApplication.DTO.LoanApplicationResponse;
import com.project.LoanBookingApplication.Entity.*;
import com.project.LoanBookingApplication.Exception.ResourceNotFoundException;
import com.project.LoanBookingApplication.Kafka.Producer.LoanEventProducer;
import com.project.LoanBookingApplication.Repository.LoanApplicationRepository;
import com.project.LoanBookingApplication.Repository.LoanRequestRepository;
import com.project.LoanBookingApplication.Repository.OfferRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanApplicationService {

    private final LoanRequestRepository loanRequestRepository;
    private final OfferRepository offerRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final LoanService loanService;
    private final LoanEventProducer loanEventProducer;


    public LoanApplicationService(
            LoanRequestRepository loanRequestRepository,
            OfferRepository offerRepository,
            LoanApplicationRepository loanApplicationRepository,
            LoanService loanService, LoanEventProducer loanEventProducer) {

        this.loanRequestRepository = loanRequestRepository;
        this.offerRepository = offerRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.loanService = loanService;
        this.loanEventProducer = loanEventProducer;
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
                .orElseThrow(() -> new RuntimeException("Loan request not found"));

        if (loanApplicationRepository.existsByLoanRequest(loanRequest)) {
            throw new RuntimeException("Application already created for this request");
        }

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Offer not found"));

        LoanApplication application = new LoanApplication();
        application.setLoanRequest(loanRequest);
        application.setOffer(offer);
        application.setLoanAmount(loanRequest.getAmount());
        application.setTenure(loanRequest.getTenure());
        application.setInterestRate(offer.getInterestRate());
        application.setStatus(ApplicationStatus.PENDING);
        application.setCreatedAt(LocalDateTime.now());
        application.setExpiredAt(LocalDateTime.now().plusDays(7));
        Double emi = calculateEmi(
                loanRequest.getAmount(),
                offer.getInterestRate(),
                loanRequest.getTenure()
        );

        application.setEmi(emi);

        LoanApplication savedApplication =
                loanApplicationRepository.save(application);

        return mapToResponse(savedApplication);
    }

    private Double calculateEmi(Double principal, Double annualRate, Integer tenure) {

        double monthlyRate = annualRate / 12 / 100;

        if (monthlyRate == 0) {
            return Math.round((principal / tenure) * 100.0) / 100.0;
        }

        double numerator = principal * monthlyRate * Math.pow(1 + monthlyRate, tenure);
        double denominator = Math.pow(1 + monthlyRate, tenure) - 1;

        return Math.round((numerator / denominator) * 100.0) / 100.0;
    }

    @Transactional
    public String updateStatus(Long applicationId) {

        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));


        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new RuntimeException("Already processed");
        }

        application.setStatus(ApplicationStatus.APPROVED);
        loanApplicationRepository.save(application);
        loanEventProducer.sendLoanApprovedEvent(applicationId);

        return "Loan approved. Processing started asynchronously.";

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

}
