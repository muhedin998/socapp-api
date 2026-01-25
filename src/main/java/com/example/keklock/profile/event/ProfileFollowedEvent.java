package com.example.keklock.profile.event;

public record ProfileFollowedEvent(
    Long followerId,
    String followerUsername,
    Long followedId,
    String followedUsername
) {
}
