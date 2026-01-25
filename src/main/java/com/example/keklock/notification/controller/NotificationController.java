package com.example.keklock.notification.controller;

import com.example.keklock.common.dto.ApiResponse;
import com.example.keklock.notification.domain.NotificationStatus;
import com.example.keklock.notification.domain.NotificationType;
import com.example.keklock.notification.dto.*;
import com.example.keklock.notification.service.NotificationService;
import com.example.keklock.notification.service.NotificationPreferenceService;
import com.example.keklock.profile.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationPreferenceService preferenceService;
    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(required = false) NotificationStatus status,
        @RequestParam(required = false) List<NotificationType> types,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String identityId = jwt.getSubject();
        Long userId = getUserId(identityId);
        
        Page<NotificationResponse> notifications;
        if (status != null) {
            notifications = notificationService.getUserNotificationsByStatus(userId, status, pageable)
                .map(NotificationResponse::fromEntity);
        } else if (types != null && !types.isEmpty()) {
            notifications = notificationService.getUserNotificationsByTypes(userId, types, pageable)
                .map(NotificationResponse::fromEntity);
        } else {
            notifications = notificationService.getUserNotifications(userId, pageable)
                .map(NotificationResponse::fromEntity);
        }
        
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@AuthenticationPrincipal Jwt jwt) {
        String identityId = jwt.getSubject();
        Long userId = getUserId(identityId);
        
        Long unreadCount = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(unreadCount));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID notificationId
    ) {
        String identityId = jwt.getSubject();
        Long userId = getUserId(identityId);
        
        NotificationResponse notification = NotificationResponse.fromEntity(
            notificationService.markAsRead(userId, notificationId)
        );
        
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
    }

    @PutMapping("/{notificationId}/unread")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsUnread(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID notificationId
    ) {
        String identityId = jwt.getSubject();
        Long userId = getUserId(identityId);
        
        NotificationResponse notification = NotificationResponse.fromEntity(
            notificationService.markAsUnread(userId, notificationId)
        );
        
        return ResponseEntity.ok(ApiResponse.success("Notification marked as unread", notification));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead(@AuthenticationPrincipal Jwt jwt) {
        String identityId = jwt.getSubject();
        Long userId = getUserId(identityId);
        
        int updatedCount = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(
            String.format("Marked %d notifications as read", updatedCount), 
            updatedCount
        ));
    }

    @PutMapping("/{notificationId}/archive")
    public ResponseEntity<ApiResponse<Void>> archiveNotification(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID notificationId
    ) {
        String identityId = jwt.getSubject();
        Long userId = getUserId(identityId);
        
        notificationService.archiveNotification(userId, notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification archived successfully", null));
    }

    @PutMapping("/archive")
    public ResponseEntity<ApiResponse<Void>> archiveNotifications(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody BulkNotificationRequest request
    ) {
        String identityId = jwt.getSubject();
        Long userId = getUserId(identityId);
        
        notificationService.archiveNotifications(userId, request.notificationIds());
        return ResponseEntity.ok(ApiResponse.success(
            String.format("Archived %d notifications", request.notificationIds().size()), 
            null
        ));
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID notificationId
    ) {
        String identityId = jwt.getSubject();
        Long userId = getUserId(identityId);
        
        notificationService.deleteNotification(userId, notificationId);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", null));
    }

    @GetMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreferencesResponse>> getPreferences(
        @AuthenticationPrincipal Jwt jwt
    ) {
        String identityId = jwt.getSubject();
        Long userId = getUserId(identityId);
        
        NotificationPreferencesResponse preferences = NotificationPreferencesResponse.builder()
            .enabledTypes(preferenceService.getEnabledNotificationTypes(userId))
            .availableTypes(EnumSet.allOf(NotificationType.class))
            .build();
            
        return ResponseEntity.ok(ApiResponse.success(preferences));
    }

    @PutMapping("/preferences")
    public ResponseEntity<ApiResponse<NotificationPreferencesResponse>> updatePreferences(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody NotificationPreferencesRequest request
    ) {
        String identityId = jwt.getSubject();
        Long userId = getUserId(identityId);
        
        preferenceService.updateNotificationPreferences(userId, request.enabledTypes());
        
        NotificationPreferencesResponse preferences = NotificationPreferencesResponse.builder()
            .enabledTypes(request.enabledTypes())
            .availableTypes(EnumSet.allOf(NotificationType.class))
            .build();
            
        return ResponseEntity.ok(ApiResponse.success(
            "Notification preferences updated successfully", 
            preferences
        ));
    }

    private Long getUserId(String identityId) {
        return profileService.getUserIdByIdentityId(identityId);
    }
}