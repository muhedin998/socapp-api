package com.example.keklock.post.cqrs;

import java.time.LocalDateTime;
import java.util.UUID;

public record FeedEntry(
    UUID postId,
    String authorUsername,
    String authorAvatarUrl,
    String content,
    String imageUrl,
    int likesCount,
    int commentsCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) implements Comparable<FeedEntry> {

    @Override
    public int compareTo(FeedEntry other) {
        return other.createdAt.compareTo(this.createdAt);
    }
}
