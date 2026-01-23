package com.example.keklock.notification.infrastructure;

import com.example.keklock.notification.domain.Notification;
import com.example.keklock.notification.dto.NotificationResponse;
import com.example.keklock.notification.port.NotificationDeliveryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketNotificationAdapter implements NotificationDeliveryPort {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendRealTimeNotification(Notification notification) {
        try {
            NotificationResponse response = NotificationResponse.fromEntity(notification);
            String destination = "/user/" + notification.getRecipient().getId() + "/queue/notifications";
            
            messagingTemplate.convertAndSend(destination, response);
            log.debug("Sent real-time notification to user: {} via WebSocket", notification.getRecipient().getId());
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification to user: {}", 
                notification.getRecipient().getId(), e);
        }
    }

    @Override
    public void sendPushNotification(Notification notification) {
        log.debug("Push notification not implemented for user: {}", notification.getRecipient().getId());
    }

    @Override
    public void sendEmailNotification(Notification notification) {
        log.debug("Email notification not implemented for user: {}", notification.getRecipient().getId());
    }
}