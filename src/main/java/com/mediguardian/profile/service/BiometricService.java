package com.mediguardian.profile.service;

import com.machinezoo.sourceafis.FingerprintImage;
import com.machinezoo.sourceafis.FingerprintImageOptions;
import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;
import com.mediguardian.core.common.ErrorCodes;
import com.mediguardian.core.exception.BusinessException;
import com.mediguardian.profile.entity.Profile;
import com.mediguardian.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BiometricService {

    private final ProfileRepository profileRepository;

    // The recommended matching threshold in SourceAFIS is 40
    private static final double MATCH_THRESHOLD = 40.0;

    /**
     * Converts an uploaded fingerprint image into a serialized template
     */
    public byte[] createTemplate(MultipartFile imageFile) {
        try {
            FingerprintImage image = new FingerprintImage()
                    .decode(imageFile.getBytes());
            FingerprintTemplate template = new FingerprintTemplate(image);
            return template.toByteArray();
        } catch (IOException e) {
            throw new BusinessException("Failed to read fingerprint image", ErrorCodes.VALIDATION_ERROR);
        } catch (Exception e) {
            log.error("Error generating fingerprint template", e);
            throw new BusinessException("Invalid fingerprint image format. Please ensure clear ridges.", ErrorCodes.VALIDATION_ERROR);
        }
    }

    /**
     * Searches for a matching profile by comparing the scanned fingerprint
     * against all templates in the database.
     */
    public UUID searchEmergencyProfileByFingerprint(MultipartFile scannedImage) {
        // 1. Convert the scanned image to a template
        byte[] scannedTemplateBytes = createTemplate(scannedImage);
        FingerprintTemplate probeTemplate = new FingerprintTemplate(scannedTemplateBytes);
        FingerprintMatcher matcher = new FingerprintMatcher(probeTemplate);

        // 2. Fetch all profiles that have a fingerprint template
        List<Profile> profilesWithTemplates = profileRepository.findByFingerprintTemplateIsNotNull();

        Profile bestMatch = null;
        double highestScore = 0.0;

        // 3. Match against all candidates
        for (Profile candidate : profilesWithTemplates) {
            FingerprintTemplate candidateTemplate = new FingerprintTemplate(candidate.getFingerprintTemplate());
            double score = matcher.match(candidateTemplate);
            
            log.debug("Profile ID {} scored {}", candidate.getId(), score);

            if (score > highestScore && score >= MATCH_THRESHOLD) {
                highestScore = score;
                bestMatch = candidate;
            }
        }

        if (bestMatch == null) {
            throw new BusinessException("No matching patient found for this fingerprint", ErrorCodes.NOT_FOUND);
        }

        log.info("Found match for Profile {} with score {}", bestMatch.getId(), highestScore);

        return bestMatch.getEmergencyId();
    }
}
