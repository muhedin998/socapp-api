package com.example.keklock.profile.service;

import com.example.keklock.common.exception.AlreadyFollowingException;
import com.example.keklock.common.exception.InvalidOperationException;
import com.example.keklock.common.exception.NotFollowingException;
import com.example.keklock.common.exception.ResourceNotFoundException;
import com.example.keklock.profile.domain.Profile;
import com.example.keklock.profile.dto.ProfileResponse;
import com.example.keklock.profile.dto.UpdateProfileRequest;
import com.example.keklock.profile.event.ProfileCreatedEvent;
import com.example.keklock.profile.event.ProfileFollowedEvent;
import com.example.keklock.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public ProfileResponse getProfileByUsername(String username) {
        Profile profile = profileRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found with username: " + username));
        return ProfileResponse.from(profile);
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfileByIdentityId(String identityId) {
        Profile profile = profileRepository.findByIdentityId(identityId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found with identityId: " + identityId));
        return ProfileResponse.from(profile);
    }

    @Transactional
    public ProfileResponse updateProfile(String identityId, UpdateProfileRequest request) {
        Profile profile = profileRepository.findByIdentityId(identityId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found with identityId: " + identityId));

        if (request.bio() != null) {
            profile.setBio(request.bio());
        }
        if (request.avatarUrl() != null) {
            profile.setAvatarUrl(request.avatarUrl());
        }

        Profile updated = profileRepository.save(profile);
        log.info("Profile updated for user: {}", identityId);
        return ProfileResponse.from(updated);
    }

    @Transactional
    public void followUser(String followerIdentityId, String followingUsername) {
        Profile follower = profileRepository.findByIdentityId(followerIdentityId)
            .orElseThrow(() -> new ResourceNotFoundException("Follower profile not found"));

        Profile following = profileRepository.findByUsername(followingUsername)
            .orElseThrow(() -> new ResourceNotFoundException("User to follow not found: " + followingUsername));

        if (follower.equals(following)) {
            throw new InvalidOperationException("Cannot follow yourself");
        }

        if (follower.isFollowing(following)) {
            throw new AlreadyFollowingException("Already following this user");
        }

        follower.follow(following);
        profileRepository.save(follower);
        log.info("User {} now follows {}", followerIdentityId, followingUsername);

        ProfileFollowedEvent event = new ProfileFollowedEvent(
            follower.getId(),
            follower.getUsername(),
            following.getId(),
            following.getUsername()
        );
        eventPublisher.publishEvent(event);
        log.debug("Published ProfileFollowedEvent: {} followed {}", follower.getUsername(), following.getUsername());
    }

    @Transactional
    public void unfollowUser(String followerIdentityId, String followingUsername) {
        Profile follower = profileRepository.findByIdentityId(followerIdentityId)
            .orElseThrow(() -> new ResourceNotFoundException("Follower profile not found"));

        Profile following = profileRepository.findByUsername(followingUsername)
            .orElseThrow(() -> new ResourceNotFoundException("User to unfollow not found: " + followingUsername));

        if (!follower.isFollowing(following)) {
            throw new NotFollowingException("Not following this user");
        }

        follower.unfollow(following);
        profileRepository.save(follower);
        log.info("User {} unfollowed {}", followerIdentityId, followingUsername);
    }

    @Transactional(readOnly = true)
    public Page<ProfileResponse> getFollowers(String username, Pageable pageable) {
        Profile profile = profileRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found with username: " + username));

        Set<Profile> followers = profile.getFollowers();
        List<ProfileResponse> followersList = followers.stream()
            .map(ProfileResponse::from)
            .skip(pageable.getOffset())
            .limit(pageable.getPageSize())
            .collect(Collectors.toList());

        return new PageImpl<>(followersList, pageable, followers.size());
    }

    @Transactional(readOnly = true)
    public Page<ProfileResponse> getFollowing(String username, Pageable pageable) {
        Profile profile = profileRepository.findByUsername(username)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found with username: " + username));

        Set<Profile> following = profile.getFollowing();
        List<ProfileResponse> followingList = following.stream()
            .map(ProfileResponse::from)
            .skip(pageable.getOffset())
            .limit(pageable.getPageSize())
            .collect(Collectors.toList());

        return new PageImpl<>(followingList, pageable, following.size());
    }

    @Transactional
    public Profile createProfile(String identityId, String username, String email, String firstName, String lastName) {
        Profile profile = new Profile();
        profile.setIdentityId(identityId);
        profile.setUsername(username);
        profile.setEmail(email);
        profile.setFirstName(firstName);
        profile.setLastName(lastName);

        Profile saved = profileRepository.save(profile);
        log.info("Profile created for user: {}", username);

        ProfileCreatedEvent event = new ProfileCreatedEvent(
            saved.getId(),
            saved.getIdentityId(),
            saved.getUsername(),
            saved.getEmail()
        );
        eventPublisher.publishEvent(event);
        log.debug("Published ProfileCreatedEvent for user: {}", username);

        return saved;
    }

    @Transactional(readOnly = true)
    public Long getUserIdByIdentityId(String identityId) {
        return profileRepository.findByIdentityId(identityId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found with identityId: " + identityId))
            .getId();
    }
}
