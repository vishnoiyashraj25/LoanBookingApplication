package com.project.LoanBookingApplication.Controller;

import com.project.LoanBookingApplication.DTO.AccountRequest;
import com.project.LoanBookingApplication.Entity.Account;
import com.project.LoanBookingApplication.Service.AccountService;
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
    public Account createAccount(@RequestBody AccountRequest accountRequest){
        return accountService.createAccount(accountRequest);
    }

    @GetMapping("/{account_id}")
    public Account getAccount(@PathVariable Long account_id){
        return accountService.getAccount(account_id);
    }

    @GetMapping
    public List<Account> getAllAccounts(){
        return accountService.getAllAccounts();
    }


}
