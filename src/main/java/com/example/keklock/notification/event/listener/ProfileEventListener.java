package com.example.keklock.notification.event.listener;

import com.example.keklock.notification.domain.NotificationType;
import com.example.keklock.notification.service.NotificationPreferenceService;
import com.example.keklock.notification.service.NotificationService;
import com.example.keklock.profile.event.ProfileCreatedEvent;
import com.example.keklock.profile.event.ProfileFollowedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileEventListener {

    private final NotificationPreferenceService preferenceService;
    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleProfileCreated(ProfileCreatedEvent event) {
        log.info("Initializing notification preferences for new user: {}", event.username());

        try {
            preferenceService.initializeDefaultPreferences(event.profileId());
        } catch (Exception e) {
            log.error("Failed to initialize notification preferences for user: {}", event.profileId(), e);
        }
    }

    @Async
    @EventListener
    public void handleProfileFollowed(ProfileFollowedEvent event) {
        log.info("Notification: {} followed {}", event.followerUsername(), event.followedUsername());

        try {
            notificationService.createNotification(
                event.followedId(),
                event.followerId(),
                NotificationType.PROFILE_FOLLOWED,
                null,
                "PROFILE",
                null
            );
        } catch (Exception e) {
            log.error("Failed to create follow notification: {} -> {}",
                event.followerUsername(), event.followedUsername(), e);
        }
    }
}