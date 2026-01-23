package com.example.keklock.notification.repository;

import com.example.keklock.notification.domain.NotificationPreference;
import com.example.keklock.notification.domain.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {

    @Query("SELECT np FROM NotificationPreference np WHERE np.profile.id = :profileId")
    List<NotificationPreference> findByProfileId(@Param("profileId") Long profileId);

    @Query("SELECT np FROM NotificationPreference np WHERE np.profile.id = :profileId AND np.type = :type")
    Optional<NotificationPreference> findByProfileIdAndType(@Param("profileId") Long profileId, 
                                                            @Param("type") NotificationType type);

    @Query("SELECT np FROM NotificationPreference np WHERE np.profile.id = :profileId AND np.enabled = true")
    List<NotificationPreference> findEnabledByProfileId(@Param("profileId") Long profileId);

    @Query("""
        SELECT np FROM NotificationPreference np 
        WHERE np.profile.id = :profileId 
        AND np.type = :type 
        AND np.enabled = true
        """)
    Optional<NotificationPreference> findEnabledByProfileIdAndType(@Param("profileId") Long profileId, 
                                                                  @Param("type") NotificationType type);
}