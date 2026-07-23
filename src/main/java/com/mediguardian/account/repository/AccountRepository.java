package com.mediguardian.account.repository;

import com.mediguardian.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    
    @Query("SELECT a FROM Account a WHERE a.email = :identifier OR a.mobileNumber = :identifier")
    Optional<Account> findByEmailOrMobileNumber(@Param("identifier") String identifier);
    
    boolean existsByEmail(String email);
    
    boolean existsByMobileNumber(String mobileNumber);

    Optional<Account> findByMobileNumber(String mobileNumber);

    List<Account> findByRole(com.mediguardian.account.entity.Role role);
}
