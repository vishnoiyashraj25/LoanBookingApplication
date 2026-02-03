package com.project.LoanBookingApplication.Service;

import com.project.LoanBookingApplication.DTO.LoanRequestDTO;
import com.project.LoanBookingApplication.Entity.LoanRequest;
import com.project.LoanBookingApplication.Entity.RequestStatus;
import com.project.LoanBookingApplication.Entity.User;
import com.project.LoanBookingApplication.Exception.ResourceNotFoundException;
import com.project.LoanBookingApplication.Repository.LoanRequestRepository;
import com.project.LoanBookingApplication.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

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

        if (exists)
        {
            LoanRequest loanRequest = loanRequestRepository.findByUser(user);
            loanRequest.setRequestStatus(RequestStatus.CLOSED);
        }

        LoanRequest req = new LoanRequest();
        req.setUser(user);
        req.setAmount(dto.getAmount());
        req.setTenure(dto.getTenure());
        req.setLoanType(dto.getLoanType());
        req.setRequestStatus(RequestStatus.ACTIVE);

        return loanRequestRepository.save(req);
    }

    public List<LoanRequest> getLoanRequest(){
        return loanRequestRepository.findAll();
    }

}
