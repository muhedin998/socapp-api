package com.example.keklock.notification.dto;

import com.example.keklock.notification.domain.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.Set;

@Builder
public record NotificationPreferencesRequest(
    @NotNull(message = "Enabled notification types are required")
    Set<NotificationType> enabledTypes
) {
}