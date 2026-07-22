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
import com.mediguardian.profile.service.ProfileService;
import com.mediguardian.family.service.FamilyService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ProfileService profileService;
    private final FamilyService familyService;
    private final com.mediguardian.core.security.RevokedTokenRepository revokedTokenRepository;

    public AuthService(AccountRepository accountRepository, 
                       PasswordEncoder passwordEncoder, 
                       JwtService jwtService, 
                       AuthenticationManager authenticationManager, 
                       @Lazy ProfileService profileService, 
                       @Lazy FamilyService familyService, 
                       com.mediguardian.core.security.RevokedTokenRepository revokedTokenRepository) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.profileService = profileService;
        this.familyService = familyService;
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Transactional
    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (token != null && revokedTokenRepository.findByToken(token).isEmpty()) {
            revokedTokenRepository.save(com.mediguardian.core.security.RevokedToken.builder().token(token).build());
        }
    }

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
        
        // If it's a PATIENT (USER), create Profile and Family group
        if (request.getRole() == com.mediguardian.account.entity.Role.USER) {
            if (request.getProfile() == null) {
                throw new BusinessException("Profile details are required for patient registration", ErrorCodes.VALIDATION_ERROR);
            }
            com.mediguardian.profile.dto.ProfileResponse profile = profileService.createProfileForAccount(request.getProfile(), account.getId());
            String familyName = (profile.getLastName() != null ? profile.getLastName() : profile.getFirstName()) + " Family";
            familyService.createFamilyGroupForAccount(familyName, profile.getId());
        }

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
