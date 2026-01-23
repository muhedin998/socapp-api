package com.example.keklock.post.dto;

import com.example.keklock.post.domain.Post;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostResponse(
    UUID id,
    String authorUsername,
    String authorAvatarUrl,
    String content,
    String imageUrl,
    int likesCount,
    int commentsCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
            post.getId(),
            post.getAuthor().getUsername(),
            post.getAuthor().getAvatarUrl(),
            post.getContent(),
            post.getImageUrl(),
            post.getLikesCount(),
            post.getCommentsCount(),
            post.getCreatedAt(),
            post.getUpdatedAt()
        );
    }
}
