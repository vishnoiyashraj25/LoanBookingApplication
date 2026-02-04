package com.project.LoanBookingApplication.Service;


import com.project.LoanBookingApplication.Entity.User;
import com.project.LoanBookingApplication.Repository.UserRepository;
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
                .orElseThrow(() -> new RuntimeException("User not found with id " + userid));
        if(user.getKycVerified()==true){
            throw new IllegalStateException("KYC already verified");
        }
        user.setKycVerified(true);
        return userRepository.save(user);
    }

}
