package com.example.keklock.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.notification.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationCleanupService {

    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupArchivedNotifications() {
        log.info("Starting scheduled cleanup of archived notifications");
        
        try {
            int deletedCount = notificationService.cleanupArchivedNotifications(30);
            log.info("Notification cleanup completed - deleted {} archived notifications older than 30 days", deletedCount);
        } catch (Exception e) {
            log.error("Failed to cleanup archived notifications", e);
        }
    }
}