package com.example.keklock.notification.domain;

import com.example.keklock.profile.domain.Profile;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"profile_id", "notification_type"}),
       indexes = @Index(name = "idx_notification_pref_profile", columnList = "profile_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"profile", "type"})
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType type;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "real_time_enabled", nullable = false)
    @Builder.Default
    private Boolean realTimeEnabled = true;

    @Column(name = "email_enabled", nullable = false)
    @Builder.Default
    private Boolean emailEnabled = false;

    @Column(name = "push_enabled", nullable = false)
    @Builder.Default
    private Boolean pushEnabled = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }

    public boolean isRealTimeEnabled() {
        return isEnabled() && Boolean.TRUE.equals(realTimeEnabled);
    }

    public boolean isEmailEnabled() {
        return isEnabled() && Boolean.TRUE.equals(emailEnabled);
    }

    public boolean isPushEnabled() {
        return isEnabled() && Boolean.TRUE.equals(pushEnabled);
    }
}