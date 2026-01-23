package com.example.keklock.profile.repository;

import com.example.keklock.profile.domain.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByIdentityId(String identityId);
    Optional<Profile> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByIdentityId(String identityId);

    @Query("SELECT p.followers FROM Profile p WHERE p.id = :profileId")
    Set<Profile> findFollowersByProfileId(@Param("profileId") Long profileId);

    @Query("SELECT p.following FROM Profile p WHERE p.id = :profileId")
    Set<Profile> findFollowingByProfileId(@Param("profileId") Long profileId);
}
