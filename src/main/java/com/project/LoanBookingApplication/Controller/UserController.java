package com.project.LoanBookingApplication.Controller;

import com.project.LoanBookingApplication.DTO.UserRequest;
import com.project.LoanBookingApplication.Entity.User;
import com.project.LoanBookingApplication.Service.UserService;
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
    public User register(@Valid @RequestBody UserRequest request) {
        return userService.registerUser(request);
    }

    @GetMapping
    public List<User> getAllUsers(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String employeeType,
            @RequestParam(required = false) Boolean kycVerified
    ) throws Exception {

        String json = userService.getAllUsersJson(userId, employeeType, kycVerified);

        return userService.parse(json);
    }

}
