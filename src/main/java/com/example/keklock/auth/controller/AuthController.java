package com.example.keklock.auth.controller;

import com.example.keklock.auth.dto.UserRegistrationRequest;
import com.example.keklock.auth.dto.UserRegistrationResponse;
import com.example.keklock.auth.service.RegistrationOrchestrator;
import com.example.keklock.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegistrationOrchestrator registrationOrchestrator;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserRegistrationResponse>> register(
        @Valid @RequestBody UserRegistrationRequest request
    ) {
        UserRegistrationResponse response = registrationOrchestrator.registerNewUser(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("User registered successfully", response));
    }
}
