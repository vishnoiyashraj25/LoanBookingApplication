package com.project.LoanBookingApplication.Service;

import com.project.LoanBookingApplication.DTO.LoanRequestDTO;
import com.project.LoanBookingApplication.DTO.LoanRequestResponse;
import com.project.LoanBookingApplication.Entity.LoanRequest;
import com.project.LoanBookingApplication.Entity.RequestStatus;
import com.project.LoanBookingApplication.Entity.User;
import com.project.LoanBookingApplication.Exception.ResourceNotFoundException;
import com.project.LoanBookingApplication.Repository.LoanRequestRepository;
import com.project.LoanBookingApplication.Repository.UserRepository;
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
    public LoanRequest createLoanRequest(LoanRequestDTO dto) {

        User user = userRepository.findById(dto.getUserid())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean exists =
                loanRequestRepository.existsByUserAndRequestStatus(user, RequestStatus.ACTIVE);

        boolean inprocess =
                loanRequestRepository.existsByUserAndRequestStatus(user, RequestStatus.INPROCESS);

        if(inprocess){
            throw new RuntimeException("This loan request is already in progress and cannot be processed again");

        }

        if (exists)
        {
            LoanRequest loanRequest = loanRequestRepository.findByUser(user);
            loanRequest.setRequestStatus(RequestStatus.REJECTED);
        }

        LoanRequest req = new LoanRequest();
        req.setUser(user);
        req.setAmount(dto.getAmount());
        req.setTenure(dto.getTenure());
        req.setLoanType(dto.getLoanType());
        req.setRequestStatus(RequestStatus.ACTIVE);

        return loanRequestRepository.save(req);
    }

    private LoanRequestResponse mapToDto(LoanRequest req) {

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
