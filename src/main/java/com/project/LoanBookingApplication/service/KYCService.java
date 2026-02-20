package com.project.LoanBookingApplication.service;


import com.project.LoanBookingApplication.entity.User;
import com.project.LoanBookingApplication.exception.ConflictException;
import com.project.LoanBookingApplication.exception.ResourceNotFoundException;
import com.project.LoanBookingApplication.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class KYCService {
    
    private final UserRepository userRepository;
    public KYCService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public User verifyKYC(Long userid){
        User user = userRepository.findById(userid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userid));
        if(user.getKycVerified()==true){
            throw new ConflictException("KYC already verified");
        }
        user.setKycVerified(true);
        return userRepository.save(user);
    }
}
