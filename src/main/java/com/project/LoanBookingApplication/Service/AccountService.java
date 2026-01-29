package com.project.LoanBookingApplication.Service;

import com.project.LoanBookingApplication.DTO.AccountRequest;
//import com.project.LoanBookingApplication.DTO.LoanRequest;
import com.project.LoanBookingApplication.Entity.Account;
import com.project.LoanBookingApplication.Entity.User;
import com.project.LoanBookingApplication.Repository.AccountRepository;
import com.project.LoanBookingApplication.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
public class AccountService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    public AccountService(UserRepository userRepository, AccountRepository accountRepository){
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public Account createAccount(@RequestBody AccountRequest accountRequest){
        Account account = new Account();
        User user =  userRepository.findById(accountRequest.getUserId()).orElseThrow(() -> new RuntimeException("User not found"));
        account.setUser(user);
        account.setAccountNumber(accountRequest.getAccountNumber());
        account.setType(accountRequest.getType());
        account.setBank(accountRequest.getBank());
        account.setIfsc(accountRequest.getIfsc());
        return accountRepository.save(account);
    }

    public Account getAccount(Long account_id){
        return accountRepository.findById(account_id).orElseThrow();
    }
    public List<Account> getAllAccounts(){
        return accountRepository.findAll();
    }

//    public List<Account> getAllAccounts() {
//        return accountRepository.findAll();
//    }
}
