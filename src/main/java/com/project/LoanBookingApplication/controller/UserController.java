package com.project.LoanBookingApplication.controller;
import com.project.LoanBookingApplication.dto.UserRequest;
import com.project.LoanBookingApplication.dto.UserResponse;
import com.project.LoanBookingApplication.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserResponse register(@Valid @RequestBody UserRequest request) {
        return userService.registerUser(request);
    }

    @GetMapping
    public List<UserResponse> getAllUsers(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String employeeType,
            @RequestParam(required = false) Boolean kycVerified
    ) throws Exception{
        return userService.getAllUsersJson(userId, employeeType, kycVerified);
    }
}
