package com.mediguardian.admin.service;

import com.mediguardian.account.entity.Account;
import com.mediguardian.account.entity.Role;
import com.mediguardian.account.repository.AccountRepository;
import com.mediguardian.admin.dto.AccountUpdateDto;
import com.mediguardian.core.common.ErrorCodes;
import com.mediguardian.core.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AccountRepository accountRepository;

    public List<Account> getAccountsByRole(Role role) {
        if (role != null) {
            return accountRepository.findByRole(role);
        }
        return accountRepository.findAll();
    }

    @Transactional
    public void freezeAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("Account not found", ErrorCodes.NOT_FOUND));
        account.setFrozen(true);
        accountRepository.save(account);
    }

    @Transactional
    public void resumeAccount(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("Account not found", ErrorCodes.NOT_FOUND));
        account.setFrozen(false);
        accountRepository.save(account);
    }

    @Transactional
    public Account updateAccount(UUID accountId, AccountUpdateDto request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("Account not found", ErrorCodes.NOT_FOUND));

        if (request.getEmail() != null) {
            account.setEmail(request.getEmail());
        }
        if (request.getMobileNumber() != null) {
            account.setMobileNumber(request.getMobileNumber());
        }

        return accountRepository.save(account);
    }
}
