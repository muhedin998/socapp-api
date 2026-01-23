package com.example.keklock.post.dto;

import com.example.keklock.post.domain.Comment;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentResponse(
    UUID id,
    UUID postId,
    String authorUsername,
    String authorAvatarUrl,
    String content,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
            comment.getId(),
            comment.getPost().getId(),
            comment.getAuthor().getUsername(),
            comment.getAuthor().getAvatarUrl(),
            comment.getContent(),
            comment.getCreatedAt(),
            comment.getUpdatedAt()
        );
    }
}
