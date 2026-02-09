package com.project.LoanBookingApplication.service;

import com.project.LoanBookingApplication.dto.AccountRequest;
import com.project.LoanBookingApplication.dto.AccountResponse;
import com.project.LoanBookingApplication.entity.Account;
import com.project.LoanBookingApplication.enums.AccountType;
import com.project.LoanBookingApplication.entity.User;
import com.project.LoanBookingApplication.exception.ResourceNotFoundException;
import com.project.LoanBookingApplication.repository.AccountRepository;
import com.project.LoanBookingApplication.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AccountService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    public AccountService(UserRepository userRepository, AccountRepository accountRepository){
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AccountResponse createAccount(AccountRequest accountRequest) {

        User user = userRepository.findById(accountRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber(accountRequest.getAccountNumber());
        account.setType(accountRequest.getType());
        account.setBank(accountRequest.getBank());
        account.setIfsc(accountRequest.getIfsc());

        Account savedAccount = accountRepository.save(account);
        return mapToResponse(savedAccount);
    }


    public List<AccountResponse> getAllAccounts(Long accountId, AccountType accountType) {

        List<Account> accounts = accountRepository.findAll();

        if (accountId != null) {
            accounts = accounts.stream()
                    .filter(a -> a.getId().equals(accountId))
                    .toList();
        }

        if (accountType != null) {
            accounts = accounts.stream()
                    .filter(a -> a.getType() == accountType)
                    .toList();
        }

        if (accounts.isEmpty()) {
            throw new ResourceNotFoundException("Account not found");
        }

        return accounts.stream()
                .map(this::mapToResponse)
                .toList();
    }




    private AccountResponse mapToResponse(Account account) {
        AccountResponse response = new AccountResponse();

        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setBank(account.getBank());
        response.setIfsc(account.getIfsc());
        response.setAccountType(account.getType());
        response.setUserName(account.getUser().getUserName());
        response.setPanNumber(account.getUser().getPanNumber());

        return response;
    }

}
