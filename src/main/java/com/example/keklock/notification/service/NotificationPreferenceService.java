package com.example.keklock.notification.service;

import com.example.keklock.notification.domain.NotificationPreference;
import com.example.keklock.notification.domain.NotificationType;
import com.example.keklock.notification.repository.NotificationPreferenceRepository;
import com.example.keklock.profile.domain.Profile;
import com.example.keklock.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final ProfileRepository profileRepository;

    private static final Set<NotificationType> DEFAULT_ENABLED_TYPES = EnumSet.of(
        NotificationType.POST_LIKED,
        NotificationType.POST_COMMENTED,
        NotificationType.PROFILE_FOLLOWED
    );

    public boolean shouldReceiveNotification(Profile recipient, NotificationType type) {
        return preferenceRepository.findEnabledByProfileIdAndType(recipient.getId(), type)
            .map(NotificationPreference::isEnabled)
            .orElse(DEFAULT_ENABLED_TYPES.contains(type));
    }

    public Set<NotificationType> getEnabledNotificationTypes(Long userId) {
        List<NotificationPreference> preferences = preferenceRepository.findEnabledByProfileId(userId);
        
        if (preferences.isEmpty()) {
            return DEFAULT_ENABLED_TYPES;
        }
        
        return preferences.stream()
            .map(NotificationPreference::getType)
            .collect(Collectors.toSet());
    }

    @Transactional
    public void updateNotificationPreferences(Long userId, Set<NotificationType> enabledTypes) {
        Profile profile = profileRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        List<NotificationPreference> existingPreferences = preferenceRepository.findByProfileId(userId);
        Map<NotificationType, NotificationPreference> existingMap = existingPreferences.stream()
            .collect(Collectors.toMap(NotificationPreference::getType, Function.identity()));
        
        for (NotificationType type : NotificationType.values()) {
            boolean shouldBeEnabled = enabledTypes.contains(type);
            NotificationPreference preference = existingMap.get(type);
            
            if (preference == null) {
                preference = NotificationPreference.builder()
                    .profile(profile)
                    .type(type)
                    .enabled(shouldBeEnabled)
                    .build();
                preferenceRepository.save(preference);
            } else if (!preference.getEnabled().equals(shouldBeEnabled)) {
                preference.setEnabled(shouldBeEnabled);
                preferenceRepository.save(preference);
            }
        }
        
        log.info("Updated notification preferences for user: {} - enabled types: {}", userId, enabledTypes);
    }

    @Transactional
    public void initializeDefaultPreferences(Long userId) {
        Profile profile = profileRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        List<NotificationPreference> existingPreferences = preferenceRepository.findByProfileId(userId);
        if (!existingPreferences.isEmpty()) {
            return;
        }
        
        for (NotificationType type : NotificationType.values()) {
            NotificationPreference preference = NotificationPreference.builder()
                .profile(profile)
                .type(type)
                .enabled(DEFAULT_ENABLED_TYPES.contains(type))
                .build();
            preferenceRepository.save(preference);
        }
        
        log.info("Initialized default notification preferences for user: {}", userId);
    }
}