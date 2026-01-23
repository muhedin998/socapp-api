package com.example.keklock.auth.service;

import com.example.keklock.auth.dto.UserRegistrationRequest;
import com.example.keklock.auth.dto.UserRegistrationResponse;
import com.example.keklock.auth.port.IdentityProviderPort;
import com.example.keklock.common.exception.DuplicateResourceException;
import com.example.keklock.profile.domain.Profile;
import com.example.keklock.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegistrationOrchestrator {

    private final IdentityProviderPort identityProvider;
    private final ProfileRepository profileRepository;

    @Transactional
    public UserRegistrationResponse registerNewUser(UserRegistrationRequest request) {
        log.info("Starting registration for user: {}", request.username());

        if (profileRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already exists: " + request.username());
        }

        String keycloakUuid = null;

        try {
            keycloakUuid = identityProvider.createUser(request);
            log.info("User created in Keycloak with ID: {}", keycloakUuid);

            Profile profile = new Profile();
            profile.setIdentityId(keycloakUuid);
            profile.setUsername(request.username());
            profile.setEmail(request.email());
            profile.setFirstName(request.firstName());
            profile.setLastName(request.lastName());

            profileRepository.save(profile);
            log.info("Profile created in database for user: {}", request.username());

            return new UserRegistrationResponse(
                keycloakUuid,
                request.username(),
                request.email(),
                "User registered successfully"
            );

        } catch (Exception e) {
            log.error("Registration failed for user: {}", request.username(), e);

            if (keycloakUuid != null) {
                try {
                    identityProvider.deleteUser(keycloakUuid);
                    log.info("Rolled back Keycloak user creation for: {}", keycloakUuid);
                } catch (Exception rollbackException) {
                    log.error("Failed to rollback Keycloak user creation", rollbackException);
                }
            }

            throw new RuntimeException("User registration failed: " + e.getMessage(), e);
        }
    }
}
