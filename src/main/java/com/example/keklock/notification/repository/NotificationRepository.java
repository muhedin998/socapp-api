package com.example.keklock.notification.repository;

import com.example.keklock.notification.domain.Notification;
import com.example.keklock.notification.domain.NotificationStatus;
import com.example.keklock.notification.domain.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID>, JpaSpecificationExecutor<Notification> {

    @Query("""
        SELECT n FROM Notification n 
        JOIN FETCH n.actor a 
        WHERE n.recipient.id = :recipientId 
        AND n.status = :status 
        ORDER BY n.createdAt DESC
        """)
    Page<Notification> findByRecipientIdAndStatus(@Param("recipientId") Long recipientId,
                                                 @Param("status") NotificationStatus status,
                                                 Pageable pageable);

    @Query("""
        SELECT n FROM Notification n 
        JOIN FETCH n.actor a 
        WHERE n.recipient.id = :recipientId 
        ORDER BY n.createdAt DESC
        """)
    Page<Notification> findByRecipientId(@Param("recipientId") Long recipientId, Pageable pageable);

    @Query("""
        SELECT COUNT(n) FROM Notification n 
        WHERE n.recipient.id = :recipientId 
        AND n.status = :status
        """)
    Long countByRecipientIdAndStatus(@Param("recipientId") Long recipientId,
                                   @Param("status") NotificationStatus status);

    @Query("""
        SELECT n FROM Notification n 
        WHERE n.recipient.id = :recipientId 
        AND n.type = :type 
        AND n.actor.id = :actorId 
        AND n.targetId = :targetId 
        AND n.createdAt > :since
        """)
    Optional<Notification> findRecentSimilarNotification(@Param("recipientId") Long recipientId,
                                                        @Param("type") NotificationType type,
                                                        @Param("actorId") Long actorId,
                                                        @Param("targetId") UUID targetId,
                                                        @Param("since") LocalDateTime since);

    @Modifying
    @Query("""
        UPDATE Notification n 
        SET n.status = :status, n.readAt = CURRENT_TIMESTAMP, n.updatedAt = CURRENT_TIMESTAMP 
        WHERE n.recipient.id = :recipientId 
        AND n.status = com.example.keklock.notification.domain.NotificationStatus.UNREAD
        """)
    int markAllAsReadForRecipient(@Param("recipientId") Long recipientId,
                                 @Param("status") NotificationStatus status);

    @Modifying
    @Query("""
        UPDATE Notification n 
        SET n.status = :newStatus, n.updatedAt = CURRENT_TIMESTAMP 
        WHERE n.id IN :notificationIds 
        AND n.recipient.id = :recipientId
        """)
    int updateStatusByIds(@Param("notificationIds") List<UUID> notificationIds,
                         @Param("recipientId") Long recipientId,
                         @Param("newStatus") NotificationStatus newStatus);

    @Query("""
        DELETE FROM Notification n 
        WHERE n.createdAt < :cutoffDate 
        AND n.status = com.example.keklock.notification.domain.NotificationStatus.ARCHIVED
        """)
    @Modifying
    int deleteArchivedNotificationsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("""
        SELECT n FROM Notification n 
        WHERE n.recipient.id = :recipientId 
        AND n.type IN :types 
        AND n.status != com.example.keklock.notification.domain.NotificationStatus.ARCHIVED
        ORDER BY n.createdAt DESC
        """)
    Page<Notification> findByRecipientIdAndTypeIn(@Param("recipientId") Long recipientId,
                                                 @Param("types") List<NotificationType> types,
                                                 Pageable pageable);
}