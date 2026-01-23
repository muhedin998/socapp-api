package com.example.keklock.notification.service;

import com.example.keklock.common.exception.ResourceNotFoundException;
import com.example.keklock.notification.domain.Notification;
import com.example.keklock.notification.domain.NotificationStatus;
import com.example.keklock.notification.domain.NotificationType;
import com.example.keklock.notification.port.NotificationDeliveryPort;
import com.example.keklock.notification.repository.NotificationRepository;
import com.example.keklock.profile.domain.Profile;
import com.example.keklock.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ProfileRepository profileRepository;
    private final NotificationDeliveryPort deliveryPort;
    private final NotificationPreferenceService preferenceService;

    @Transactional
    public Notification createNotification(Long recipientId, Long actorId, NotificationType type,
                                         UUID targetId, String targetType, String customMessage) {
        
        if (recipientId.equals(actorId)) {
            log.debug("Skipping self-notification for user: {}", recipientId);
            return null;
        }

        Profile recipient = profileRepository.findById(recipientId)
            .orElseThrow(() -> new ResourceNotFoundException("Recipient not found with id: " + recipientId));
        
        Profile actor = profileRepository.findById(actorId)
            .orElseThrow(() -> new ResourceNotFoundException("Actor not found with id: " + actorId));

        if (!preferenceService.shouldReceiveNotification(recipient, type)) {
            log.debug("User {} has disabled notifications for type: {}", recipientId, type);
            return null;
        }

        Optional<Notification> recentSimilar = notificationRepository.findRecentSimilarNotification(
            recipientId, type, actorId, targetId, LocalDateTime.now().minusHours(1)
        );
        
        if (recentSimilar.isPresent()) {
            log.debug("Skipping duplicate notification for recipient: {}, type: {}", recipientId, type);
            return recentSimilar.get();
        }

        String message = customMessage != null ? customMessage : 
            String.format("%s %s", actor.getUsername(), type.getMessage());

        Notification notification = Notification.builder()
            .recipient(recipient)
            .actor(actor)
            .type(type)
            .targetId(targetId)
            .targetType(targetType)
            .message(message)
            .status(NotificationStatus.UNREAD)
            .build();

        notification = notificationRepository.save(notification);
        log.info("Created notification: {} for recipient: {}", notification.getId(), recipientId);

        try {
            deliveryPort.sendRealTimeNotification(notification);
        } catch (Exception e) {
            log.warn("Failed to send real-time notification: {}", e.getMessage());
        }

        return notification;
    }

    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientId(userId, pageable);
    }

    public Page<Notification> getUserNotificationsByStatus(Long userId, NotificationStatus status, Pageable pageable) {
        return notificationRepository.findByRecipientIdAndStatus(userId, status, pageable);
    }

    public Page<Notification> getUserNotificationsByTypes(Long userId, List<NotificationType> types, Pageable pageable) {
        return notificationRepository.findByRecipientIdAndTypeIn(userId, types, pageable);
    }

    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndStatus(userId, NotificationStatus.UNREAD);
    }

    @Transactional
    public Notification markAsRead(Long userId, UUID notificationId) {
        Notification notification = getNotificationForUser(userId, notificationId);
        
        if (notification.isUnread()) {
            notification.markAsRead();
            notification = notificationRepository.save(notification);
            log.debug("Marked notification {} as read for user: {}", notificationId, userId);
        }
        
        return notification;
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        int updatedCount = notificationRepository.markAllAsReadForRecipient(userId, NotificationStatus.READ);
        log.info("Marked {} notifications as read for user: {}", updatedCount, userId);
        return updatedCount;
    }

    @Transactional
    public Notification markAsUnread(Long userId, UUID notificationId) {
        Notification notification = getNotificationForUser(userId, notificationId);
        
        if (notification.isRead()) {
            notification.markAsUnread();
            notification = notificationRepository.save(notification);
            log.debug("Marked notification {} as unread for user: {}", notificationId, userId);
        }
        
        return notification;
    }

    @Transactional
    public void archiveNotification(Long userId, UUID notificationId) {
        Notification notification = getNotificationForUser(userId, notificationId);
        notification.archive();
        notificationRepository.save(notification);
        log.debug("Archived notification {} for user: {}", notificationId, userId);
    }

    @Transactional
    public void archiveNotifications(Long userId, List<UUID> notificationIds) {
        int updatedCount = notificationRepository.updateStatusByIds(
            notificationIds, userId, NotificationStatus.ARCHIVED
        );
        log.info("Archived {} notifications for user: {}", updatedCount, userId);
    }

    @Transactional
    public void deleteNotification(Long userId, UUID notificationId) {
        Notification notification = getNotificationForUser(userId, notificationId);
        notificationRepository.delete(notification);
        log.debug("Deleted notification {} for user: {}", notificationId, userId);
    }

    @Transactional
    public int cleanupArchivedNotifications(int daysToKeep) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        int deletedCount = notificationRepository.deleteArchivedNotificationsOlderThan(cutoffDate);
        log.info("Cleaned up {} archived notifications older than {} days", deletedCount, daysToKeep);
        return deletedCount;
    }

    private Notification getNotificationForUser(Long userId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        
        if (!notification.getRecipient().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found for user: " + userId);
        }
        
        return notification;
    }
}