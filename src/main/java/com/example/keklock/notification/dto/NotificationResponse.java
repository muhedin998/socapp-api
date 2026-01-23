package com.example.keklock.notification.dto;

import com.example.keklock.notification.domain.Notification;
import com.example.keklock.notification.domain.NotificationStatus;
import com.example.keklock.notification.domain.NotificationType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificationResponse(
    UUID id,
    String actorUsername,
    String actorAvatarUrl,
    NotificationType type,
    NotificationStatus status,
    String message,
    UUID targetId,
    String targetType,
    LocalDateTime createdAt,
    LocalDateTime readAt
) {
    
    public static NotificationResponse fromEntity(Notification notification) {
        return NotificationResponse.builder()
            .id(notification.getId())
            .actorUsername(notification.getActor().getUsername())
            .actorAvatarUrl(notification.getActor().getAvatarUrl())
            .type(notification.getType())
            .status(notification.getStatus())
            .message(notification.getMessage())
            .targetId(notification.getTargetId())
            .targetType(notification.getTargetType())
            .createdAt(notification.getCreatedAt())
            .readAt(notification.getReadAt())
            .build();
    }
}