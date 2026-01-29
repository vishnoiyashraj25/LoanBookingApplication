package com.project.LoanBookingApplication.Controller;

import com.project.LoanBookingApplication.DTO.UserRequest;
import com.project.LoanBookingApplication.Entity.User;
import com.project.LoanBookingApplication.Service.UserService;
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
    public User register(@RequestBody UserRequest request) {
        return userService.registerUser(request);
    }

    @GetMapping("/{userId}")
    public User getUser(@PathVariable Long userId) {
        return userService.getUser(userId);
    }

    @GetMapping
    public List<User> getAllUsers(){
        return userService.getAllUsers();
    }
}
