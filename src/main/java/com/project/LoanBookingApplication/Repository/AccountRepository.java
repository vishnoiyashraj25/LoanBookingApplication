package com.project.LoanBookingApplication.Repository;

import com.project.LoanBookingApplication.Entity.Account;
import com.project.LoanBookingApplication.Entity.AccountType;
import com.project.LoanBookingApplication.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByUser(User user);
//    List<Account>findByAccountId(Long account_id);
    List<Account>findByType(AccountType accountType);
    List<Account>findByIdAndType(Long account_id, AccountType accountType);
}
