package com.mediguardian.core.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service("securityService")
public class SecurityService {

    public boolean canAccessProfile(Authentication authentication, UUID profileId) {
        // For the hackathon MVP, authenticated users can access profile data,
        // specifically allowing doctors and patients to query details.
        return authentication != null && authentication.isAuthenticated();
    }

    public boolean canModifyProfile(Authentication authentication, UUID profileId) {
        // Basic check for modifications
        return authentication != null && authentication.isAuthenticated();
    }
}
