package com.example.keklock.post.event;

import com.example.keklock.common.event.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostLikedEvent(
    UUID postId,
    Long likerId,
    String likerUsername,
    Long postAuthorId,
    LocalDateTime occurredOn
) implements DomainEvent {
    public PostLikedEvent(UUID postId, Long likerId, String likerUsername, Long postAuthorId) {
        this(postId, likerId, likerUsername, postAuthorId, LocalDateTime.now());
    }
}
