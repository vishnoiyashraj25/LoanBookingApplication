package com.project.LoanBookingApplication.Service;

import com.project.LoanBookingApplication.DTO.UserRequest;
import com.project.LoanBookingApplication.Entity.User;
import com.project.LoanBookingApplication.Exception.ResourceNotFoundException;
import com.project.LoanBookingApplication.Repository.UserRepository;
import jakarta.validation.constraints.Null;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(UserRequest request) {
        User user = new User();
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAadharNumber(request.getAadharNumber());
        user.setPanNumber(request.getPanNumber());
        user.setAge(request.getAge());
        user.setEmployeeType(request.getEmployeeType());
        user.setMonthlyIncome(request.getMonthlyIncome());
        user.setKycVerified(false);
        user.setCreditScore(request.getCreditScore());
        return userRepository.save(user);
    }

//    public User getUser(Long userId) {
//        return userRepository.findById(userId).orElseThrow();
//    }
public List<User> getAllUsers(
        Long userId,
        String employeeType,
        Boolean kycVerified
) {

    List<User> users = userRepository.findAll();

    if (userId != null) {
        users = users.stream()
                .filter(u -> u.getUserId().equals(userId))
                .toList();
    }

    if (employeeType != null) {
        users = users.stream()
                .filter(u -> employeeType.equalsIgnoreCase(u.getEmployeeType()))
                .toList();
    }

    if (kycVerified != null) {
        users = users.stream()
                .filter(u -> Boolean.TRUE.equals(u.getKycVerified()) == kycVerified)
                .toList();
    }

    if (users.isEmpty()) {
        throw new ResourceNotFoundException("No users found");
    }

    return users;
}

}
