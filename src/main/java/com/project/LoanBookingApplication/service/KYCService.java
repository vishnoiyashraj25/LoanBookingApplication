package com.project.LoanBookingApplication.service;


import com.project.LoanBookingApplication.entity.User;
import com.project.LoanBookingApplication.exception.ResourceNotFoundException;
import com.project.LoanBookingApplication.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class KYCService {

    private final UserRepository userRepository;
    public KYCService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @CacheEvict(value = "users_list", allEntries = true)
    public User verifyKYC(Long userid){
        User user = userRepository.findById(userid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userid));
        if(user.getKycVerified()==true){
            throw new IllegalStateException("KYC already verified");
        }
        user.setKycVerified(true);
        return userRepository.save(user);
    }

}
