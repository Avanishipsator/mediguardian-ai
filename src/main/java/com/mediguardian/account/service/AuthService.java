package com.mediguardian.account.service;

import com.mediguardian.account.dto.AuthRequest;
import com.mediguardian.account.dto.AuthResponse;
import com.mediguardian.account.dto.RegisterRequest;
import com.mediguardian.account.entity.Account;
import com.mediguardian.account.repository.AccountRepository;
import com.mediguardian.core.common.ErrorCodes;
import com.mediguardian.core.exception.BusinessException;
import com.mediguardian.core.security.CustomUserDetails;
import com.mediguardian.core.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getEmail() != null && accountRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already in use", ErrorCodes.VALIDATION_ERROR);
        }
        if (request.getMobileNumber() != null && accountRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new BusinessException("Mobile number already in use", ErrorCodes.VALIDATION_ERROR);
        }

        var account = Account.builder()
                .email(request.getEmail())
                .mobileNumber(request.getMobileNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
                
        accountRepository.save(account);

        var userDetails = new CustomUserDetails(account);
        var jwtToken = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(jwtToken)
                .role(account.getRole().name())
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getIdentifier(),
                        request.getPassword()
                )
        );

        var account = accountRepository.findByEmailOrMobileNumber(request.getIdentifier())
                .orElseThrow(() -> new BusinessException("User not found", ErrorCodes.NOT_FOUND));
                
        var userDetails = new CustomUserDetails(account);
        var jwtToken = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(jwtToken)
                .role(account.getRole().name())
                .build();
    }

    @Transactional
    public void changePassword(com.mediguardian.account.dto.ChangePasswordRequest request) {
        java.util.UUID accountId = com.mediguardian.core.common.SecurityUtils.getCurrentAccountId()
                .orElseThrow(() -> new BusinessException("User not authenticated", ErrorCodes.UNAUTHORIZED));
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BusinessException("User not found", ErrorCodes.NOT_FOUND));

        if (!passwordEncoder.matches(request.getCurrentPassword(), account.getPassword())) {
            throw new BusinessException("Invalid current password", ErrorCodes.VALIDATION_ERROR);
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
    }
}
