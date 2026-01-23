package com.example.keklock.profile.controller;

import com.example.keklock.common.dto.ApiResponse;
import com.example.keklock.profile.dto.ProfileResponse;
import com.example.keklock.profile.dto.UpdateProfileRequest;
import com.example.keklock.profile.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> getCurrentProfile(
        @AuthenticationPrincipal Jwt jwt
    ) {
        String identityId = jwt.getSubject();
        ProfileResponse profile = profileService.getProfileByIdentityId(identityId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(
        @PathVariable String username
    ) {
        ProfileResponse profile = profileService.getProfileByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody UpdateProfileRequest request
    ) {
        String identityId = jwt.getSubject();
        ProfileResponse updated = profileService.updateProfile(identityId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
    }

    @PostMapping("/{username}/follow")
    public ResponseEntity<ApiResponse<Void>> followUser(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String username
    ) {
        String identityId = jwt.getSubject();
        profileService.followUser(identityId, username);
        return ResponseEntity.ok(ApiResponse.success("Successfully followed user", null));
    }

    @DeleteMapping("/{username}/follow")
    public ResponseEntity<ApiResponse<Void>> unfollowUser(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable String username
    ) {
        String identityId = jwt.getSubject();
        profileService.unfollowUser(identityId, username);
        return ResponseEntity.ok(ApiResponse.success("Successfully unfollowed user", null));
    }

    @GetMapping("/{username}/followers")
    public ResponseEntity<ApiResponse<Set<ProfileResponse>>> getFollowers(
        @PathVariable String username
    ) {
        Set<ProfileResponse> followers = profileService.getFollowers(username);
        return ResponseEntity.ok(ApiResponse.success(followers));
    }

    @GetMapping("/{username}/following")
    public ResponseEntity<ApiResponse<Set<ProfileResponse>>> getFollowing(
        @PathVariable String username
    ) {
        Set<ProfileResponse> following = profileService.getFollowing(username);
        return ResponseEntity.ok(ApiResponse.success(following));
    }
}
