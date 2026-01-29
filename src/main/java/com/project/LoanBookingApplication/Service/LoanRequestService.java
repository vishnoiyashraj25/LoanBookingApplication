package com.project.LoanBookingApplication.Service;

import com.project.LoanBookingApplication.DTO.LoanRequestDTO;
import com.project.LoanBookingApplication.Entity.LoanRequest;
import com.project.LoanBookingApplication.Entity.User;
import com.project.LoanBookingApplication.Repository.LoanRequestRepository;
import com.project.LoanBookingApplication.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class LoanRequestService {

    private final LoanRequestRepository loanRequestRepository;
    private final UserRepository userRepository;
    public LoanRequestService(LoanRequestRepository loanRequestRepository, UserRepository userRepository){
        this.loanRequestRepository = loanRequestRepository;
        this.userRepository = userRepository;
    }
    public LoanRequest requestLoan(@RequestBody LoanRequestDTO loanRequestDTO)
    {
        LoanRequest loanRequest = new LoanRequest();
        loanRequest.setLoanType(loanRequestDTO.getLoanType());
        loanRequest.setAmount(loanRequestDTO.getAmount());
        loanRequest.setTenure(loanRequestDTO.getTenure());
        User user = userRepository.findById(loanRequestDTO.getUserid()).orElseThrow();
        loanRequest.setUser(user);
        return loanRequestRepository.save(loanRequest);
    }

}
