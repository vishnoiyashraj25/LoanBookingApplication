package com.project.LoanBookingApplication.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.LoanBookingApplication.dto.UserRequest;
import com.project.LoanBookingApplication.dto.UserResponse;
import com.project.LoanBookingApplication.entity.User;
import com.project.LoanBookingApplication.exception.ResourceNotFoundException;
import com.project.LoanBookingApplication.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private final ObjectMapper mapper = new ObjectMapper();

    public UserResponse registerUser(UserRequest request) {

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

        User saved = userRepository.save(user);

        return mapToDto(saved);
    }

    public List<UserResponse> getAllUsersJson(
            Long userId,
            String employeeType,
            Boolean kycVerified
    ) throws JsonProcessingException {

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
                    .filter(u -> u.getKycVerified().equals(kycVerified))
                    .toList();
        }

        if (users.isEmpty()) {
            throw new ResourceNotFoundException("No users found");
        }

        List<UserResponse> userRespons =  users.stream()
                .map(this::mapToDto)
                .toList();

        String json = mapper.writeValueAsString(userRespons);
        return parse(json);
    }

    public List<UserResponse> parse(String json) throws JsonProcessingException {
        return mapper.readValue(json, new TypeReference<List<UserResponse>>() {});
    }
    private UserResponse mapToDto(User u) {
        return new UserResponse(
                u.getUserId(),
                u.getUserName(),
                u.getEmail(),
                u.getPhoneNumber(),
                u.getKycVerified(),
                u.getAge(),
                u.getEmployeeType(),
                u.getMonthlyIncome(),
                u.getCreditScore()
        );
    }
}
