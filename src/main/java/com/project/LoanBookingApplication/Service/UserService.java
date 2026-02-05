package com.project.LoanBookingApplication.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.LoanBookingApplication.DTO.UserRequest;
import com.project.LoanBookingApplication.Entity.User;
import com.project.LoanBookingApplication.Exception.ResourceNotFoundException;
import com.project.LoanBookingApplication.Repository.UserRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @CacheEvict(value = "users_list", allEntries = true)
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

    @Cacheable(value = "users_list",
            key = "#userId + '-' + #employeeType + '-' + #kycVerified")
    public String getAllUsersJson(
            Long userId,
            String employeeType,
            Boolean kycVerified
    ) throws Exception {

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

        return mapper.writeValueAsString(users);
    }


    public List<User> parse(String json) throws Exception {
        return mapper.readValue(json, new TypeReference<List<User>>() {});
    }
}
