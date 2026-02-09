package com.project.LoanBookingApplication.service;

import com.project.LoanBookingApplication.dto.LoanRequest;
import com.project.LoanBookingApplication.dto.LoanRequestResponse;
import com.project.LoanBookingApplication.enums.RequestStatus;
import com.project.LoanBookingApplication.entity.User;
import com.project.LoanBookingApplication.exception.ResourceNotFoundException;
import com.project.LoanBookingApplication.repository.LoanRequestRepository;
import com.project.LoanBookingApplication.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanRequestService {

    private final LoanRequestRepository loanRequestRepository;
    private final UserRepository userRepository;
    public LoanRequestService(LoanRequestRepository loanRequestRepository, UserRepository userRepository){
        this.loanRequestRepository = loanRequestRepository;
        this.userRepository = userRepository;
    }
    @Transactional
    public LoanRequestResponse createLoanRequest(LoanRequest dto) {

        User user = userRepository.findById(dto.getUserid())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));


        boolean inprocess =
                loanRequestRepository.existsByUserAndRequestStatus(user, RequestStatus.INPROCESS);

        if(inprocess){
            throw new RuntimeException("This loan request is already in progress and cannot be processed again");

        }

        loanRequestRepository
                .findFirstByUserAndRequestStatus(user, RequestStatus.ACTIVE)
                .ifPresent(lr -> {
                    lr.setRequestStatus(RequestStatus.REJECTED);
                    loanRequestRepository.save(lr);
                });

        com.project.LoanBookingApplication.entity.LoanRequest req = new com.project.LoanBookingApplication.entity.LoanRequest();
        req.setUser(user);
        req.setAmount(dto.getAmount());
        req.setTenure(dto.getTenure());
        req.setLoanType(dto.getLoanType());
        req.setRequestStatus(RequestStatus.ACTIVE);

        com.project.LoanBookingApplication.entity.LoanRequest loanRequest =  loanRequestRepository.save(req);
        return mapToDto(loanRequest);
    }

    private LoanRequestResponse mapToDto(com.project.LoanBookingApplication.entity.LoanRequest req) {

        LoanRequestResponse dto = new LoanRequestResponse();

        dto.setId(req.getId());
        dto.setUserName(req.getUser().getUserName());
        dto.setPanNumber(req.getUser().getPanNumber());
        dto.setAmount(req.getAmount());
        dto.setTenure(req.getTenure());
        dto.setLoanType(req.getLoanType());
        dto.setRequestStatus(req.getRequestStatus());

        return dto;
    }


    public List<LoanRequestResponse> getLoanRequest(){
        return loanRequestRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }
}
