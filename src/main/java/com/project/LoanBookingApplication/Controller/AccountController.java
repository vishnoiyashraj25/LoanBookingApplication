package com.project.LoanBookingApplication.Controller;

import com.project.LoanBookingApplication.DTO.AccountRequest;
import com.project.LoanBookingApplication.DTO.AccountResponse;
import com.project.LoanBookingApplication.Entity.Account;
import com.project.LoanBookingApplication.Entity.AccountType;
import com.project.LoanBookingApplication.Service.AccountService;
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

//    @GetMapping("/{account_id}")
//    public Account getAccount(@PathVariable Long account_id){
//        return accountService.getAccount(account_id);
//    }



    @GetMapping
    public List<AccountResponse> getAllAccounts(@Valid @RequestParam(required = false) Long account_id, @Valid @RequestParam(required = false)AccountType accountType){
        return accountService.getAllAccounts(account_id,accountType);
    }


}
