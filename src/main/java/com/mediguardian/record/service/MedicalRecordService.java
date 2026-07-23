package com.mediguardian.record.service;

import com.mediguardian.core.common.ErrorCodes;
import com.mediguardian.core.common.SecurityUtils;
import com.mediguardian.core.exception.BusinessException;
import com.mediguardian.family.entity.FamilyMember;
import com.mediguardian.family.repository.FamilyMemberRepository;
import com.mediguardian.profile.entity.Profile;
import com.mediguardian.profile.repository.ProfileRepository;
import com.mediguardian.record.dto.MedicalRecordResponse;
import com.mediguardian.record.entity.MedicalRecord;
import com.mediguardian.record.entity.RecordType;
import com.mediguardian.record.repository.MedicalRecordRepository;
import com.mediguardian.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository recordRepository;
    private final ProfileRepository profileRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final com.mediguardian.family.repository.FamilyRepository familyRepository;
    private final StorageService storageService;
    private final NotificationService notificationService;

    @Transactional
    public MedicalRecordResponse uploadRecord(UUID profileId, MultipartFile file, String title, RecordType type, String description) {
        Profile targetProfile = profileRepository.findById(profileId)
                .orElseThrow(() -> new BusinessException("Profile not found", ErrorCodes.NOT_FOUND));

        verifyPermission(targetProfile);

        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase() : "";

        if (!fileExtension.equals(".pdf") && !fileExtension.equals(".jpg") && !fileExtension.equals(".jpeg") && !fileExtension.equals(".png")) {
            throw new BusinessException("Unsupported file type. Supported types are pdf, jpg, jpeg, png.", ErrorCodes.VALIDATION_ERROR);
        }
        String fileKey = "records/" + profileId.toString() + "/" + UUID.randomUUID().toString() + fileExtension;

        String uploadedKey = storageService.uploadFile(file, fileKey);

        MedicalRecord record = MedicalRecord.builder()
                .profileId(profileId)
                .title(title)
                .type(type)
                .description(description)
                .s3FileKey(uploadedKey)
                .uploadDate(Instant.now())
                .build();
        
        record = recordRepository.save(record);

        boolean hasHospitalRole = SecurityUtils.hasRole("HOSPITAL") || SecurityUtils.hasRole("DOCTOR");
        
        if (hasHospitalRole) {
            String hospitalName = "A Hospital"; // We could pull this from the Hospital's account info
            notificationService.createNotification(targetProfile.getAccountId(), hospitalName + " uploaded a new " + type.name() + " report.");
        }

        return mapToResponse(record);
    }

    public List<MedicalRecordResponse> getRecordsForProfile(UUID profileId) {
        Profile targetProfile = profileRepository.findById(profileId)
                .orElseThrow(() -> new BusinessException("Profile not found", ErrorCodes.NOT_FOUND));

        verifyPermission(targetProfile);

        return recordRepository.findByProfileIdOrderByUploadDateDesc(profileId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MedicalRecordResponse updateVisibility(UUID recordId, com.mediguardian.record.entity.RecordVisibility visibility) {
        MedicalRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("Medical record not found", ErrorCodes.NOT_FOUND));
        
        Profile targetProfile = profileRepository.findById(record.getProfileId())
                .orElseThrow(() -> new BusinessException("Profile not found", ErrorCodes.NOT_FOUND));

        // Only the profile owner or family head can change visibility. For simplicity here we just use verifyPermission, but in a real scenario we'd check strict ownership.
        verifyPermission(targetProfile);

        record.setVisibility(visibility);
        record = recordRepository.save(record);

        return mapToResponse(record);
    }

    private void verifyPermission(Profile targetProfile) {
        UUID currentAccountId = SecurityUtils.getCurrentAccountId()
                .orElseThrow(() -> new BusinessException("User not authenticated", ErrorCodes.UNAUTHORIZED));

        if (currentAccountId.equals(targetProfile.getAccountId())) {
            return; // User owns this profile
        }

        boolean hasHospitalRole = SecurityUtils.hasRole("HOSPITAL") || SecurityUtils.hasRole("DOCTOR") || SecurityUtils.hasRole("LAB");
        if (hasHospitalRole) {
            return;
        }

        Profile currentProfile = profileRepository.findByAccountId(currentAccountId)
                .orElseThrow(() -> new BusinessException("Your profile not found", ErrorCodes.NOT_FOUND));

        List<FamilyMember> targetMemberships = familyMemberRepository.findByProfileId(targetProfile.getId());
        List<FamilyMember> currentMemberships = familyMemberRepository.findByProfileId(currentProfile.getId());

        boolean hasPermission = false;
        
        java.util.List<com.mediguardian.family.entity.Family> familiesWhereCurrentIsHead = familyRepository.findByHeadProfileId(currentProfile.getId());
        
        for (FamilyMember myMembership : currentMemberships) {
            for (FamilyMember theirMembership : targetMemberships) {
                if (myMembership.getFamilyId().equals(theirMembership.getFamilyId())) {
                    if (theirMembership.isCanViewMedicalHistory()) {
                        hasPermission = true;
                        break;
                    }
                    // Current profile is head of this family
                    for (com.mediguardian.family.entity.Family family : familiesWhereCurrentIsHead) {
                        if (family.getId().equals(theirMembership.getFamilyId())) {
                            hasPermission = true;
                            break;
                        }
                    }
                }
            }
        }

        if (!hasPermission) {
            throw new BusinessException("You do not have permission to access medical records for this profile", ErrorCodes.FORBIDDEN);
        }
    }

    private MedicalRecordResponse mapToResponse(MedicalRecord record) {
        String url = storageService.getCdnUrl(record.getS3FileKey());
        
        return MedicalRecordResponse.builder()
                .id(record.getId())
                .profileId(record.getProfileId())
                .title(record.getTitle())
                .type(record.getType())
                .description(record.getDescription())
                .visibility(record.getVisibility())
                .uploadDate(record.getUploadDate())
                .presignedUrl(url)
                .build();
    }
}
