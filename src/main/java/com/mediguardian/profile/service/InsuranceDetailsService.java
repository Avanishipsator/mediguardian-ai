package com.mediguardian.profile.service;

import com.mediguardian.core.exception.BusinessException;
import com.mediguardian.core.common.ErrorCodes;
import com.mediguardian.profile.dto.InsuranceDetailsDto;
import com.mediguardian.profile.dto.InsuranceDetailsRequest;
import com.mediguardian.profile.entity.InsuranceDetails;
import com.mediguardian.profile.entity.Profile;
import com.mediguardian.profile.repository.InsuranceDetailsRepository;
import com.mediguardian.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InsuranceDetailsService {

    private final InsuranceDetailsRepository insuranceDetailsRepository;
    private final ProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public InsuranceDetailsDto getInsuranceDetails(UUID profileId) {
        return insuranceDetailsRepository.findByProfileId(profileId)
                .map(this::mapToDto)
                .orElse(null);
    }

    @Transactional
    public InsuranceDetailsDto saveOrUpdateInsuranceDetails(UUID profileId, InsuranceDetailsRequest request) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new BusinessException("Profile not found", ErrorCodes.NOT_FOUND));

        InsuranceDetails details = insuranceDetailsRepository.findByProfileId(profileId)
                .orElseGet(() -> InsuranceDetails.builder().profile(profile).build());

        details.setProviderName(request.getProviderName());
        details.setPolicyNumber(request.getPolicyNumber());
        details.setGroupId(request.getGroupId());
        details.setCoverageType(request.getCoverageType());
        details.setExpirationDate(request.getExpirationDate());

        InsuranceDetails saved = insuranceDetailsRepository.save(details);
        return mapToDto(saved);
    }

    private InsuranceDetailsDto mapToDto(InsuranceDetails entity) {
        return InsuranceDetailsDto.builder()
                .id(entity.getId())
                .profileId(entity.getProfile().getId())
                .providerName(entity.getProviderName())
                .policyNumber(entity.getPolicyNumber())
                .groupId(entity.getGroupId())
                .coverageType(entity.getCoverageType())
                .expirationDate(entity.getExpirationDate())
                .build();
    }
}
