package com.example.keklock.post.event;

import com.example.keklock.common.event.DomainEvent;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentAddedEvent(
    UUID commentId,
    UUID postId,
    Long commenterId,
    String commenterUsername,
    String content,
    Long postAuthorId,
    LocalDateTime occurredOn
) implements DomainEvent {
    public CommentAddedEvent(UUID commentId, UUID postId, Long commenterId, String commenterUsername, String content, Long postAuthorId) {
        this(commentId, postId, commenterId, commenterUsername, content, postAuthorId, LocalDateTime.now());
    }
}
