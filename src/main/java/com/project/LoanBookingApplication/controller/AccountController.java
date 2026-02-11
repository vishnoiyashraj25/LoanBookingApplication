package com.project.LoanBookingApplication.controller;

import com.project.LoanBookingApplication.dto.AccountRequest;
import com.project.LoanBookingApplication.dto.AccountResponse;
import com.project.LoanBookingApplication.enums.AccountType;
import com.project.LoanBookingApplication.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;
    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    @PostMapping
    public AccountResponse createAccount(@Valid @RequestBody AccountRequest accountRequest){
        return accountService.createAccount(accountRequest);
    }

    @GetMapping
    public List<AccountResponse> getAllAccounts(
            @RequestParam(required = false) @Positive(message = "Account ID must be positive when provided") Long accountId,
            @RequestParam(required = false) AccountType accountType){
        return accountService.getAllAccounts(accountId, accountType);
    }


}
