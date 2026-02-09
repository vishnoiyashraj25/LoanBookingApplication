package com.project.LoanBookingApplication.repository;

import com.project.LoanBookingApplication.entity.Account;
import com.project.LoanBookingApplication.enums.AccountType;
import com.project.LoanBookingApplication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByUser(User user);
    List<Account>findByType(AccountType accountType);
    List<Account>findByIdAndType(Long accountId, AccountType accountType);
}
