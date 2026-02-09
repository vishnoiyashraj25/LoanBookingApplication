package com.project.LoanBookingApplication.controller;

import com.project.LoanBookingApplication.dto.AccountRequest;
import com.project.LoanBookingApplication.dto.AccountResponse;
import com.project.LoanBookingApplication.enums.AccountType;
import com.project.LoanBookingApplication.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;
    public AccountController(AccountService accountService){
        this.accountService = accountService;
    }

    @PostMapping
    public AccountResponse createAccount(@Valid  @RequestBody AccountRequest accountRequest){
        return accountService.createAccount(accountRequest);
    }

    @GetMapping
    public List<AccountResponse> getAllAccounts(@Valid @RequestParam(required = false) Long accountId, @Valid @RequestParam(required = false)AccountType accountType){
        return accountService.getAllAccounts(accountId,accountType);
    }


}
