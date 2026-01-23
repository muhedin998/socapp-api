package com.example.keklock.auth.port;

import com.example.keklock.auth.dto.UserRegistrationRequest;

public interface IdentityProviderPort {
    String createUser(UserRegistrationRequest request);
    void deleteUser(String identityId);
    boolean userExists(String username);
}
