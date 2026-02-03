package com.project.LoanBookingApplication.Service;


import com.project.LoanBookingApplication.Entity.User;
import com.project.LoanBookingApplication.Repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class KYCService {

    private final UserRepository userRepository;
    public KYCService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

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
