package com.example.keklock.notification.dto;

import com.example.keklock.notification.domain.NotificationType;
import lombok.Builder;

import java.util.Set;

@Builder
public record NotificationPreferencesResponse(
    Set<NotificationType> enabledTypes,
    Set<NotificationType> availableTypes
) {
}