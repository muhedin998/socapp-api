package com.example.keklock.post.event;

import com.example.keklock.common.event.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostCreatedEvent(
    UUID postId,
    Long authorId,
    String authorUsername,
    String content,
    LocalDateTime occurredOn
) implements DomainEvent {
    public PostCreatedEvent(UUID postId, Long authorId, String authorUsername, String content) {
        this(postId, authorId, authorUsername, content, LocalDateTime.now());
    }
}
