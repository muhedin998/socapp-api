package com.example.keklock.notification.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record BulkNotificationRequest(
    @NotEmpty(message = "Notification IDs cannot be empty")
    List<UUID> notificationIds
) {
}