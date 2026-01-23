package com.example.keklock.notification.event.listener;

import com.example.keklock.notification.service.NotificationPreferenceService;
import com.example.keklock.profile.domain.Profile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProfileEventListener {

    private final NotificationPreferenceService preferenceService;

    @Async
    @TransactionalEventListener
    public void handleProfileCreated(Profile profile) {
        log.info("Initializing notification preferences for new user: {}", profile.getId());
        
        try {
            preferenceService.initializeDefaultPreferences(profile.getId());
        } catch (Exception e) {
            log.error("Failed to initialize notification preferences for user: {}", profile.getId(), e);
        }
    }
}