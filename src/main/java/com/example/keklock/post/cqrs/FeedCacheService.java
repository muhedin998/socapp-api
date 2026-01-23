package com.example.keklock.post.cqrs;

import java.util.List;
import java.util.UUID;

public interface FeedCacheService {
    void addToFollowerFeeds(Long authorId, FeedEntry feedEntry);
    void updateFeedEntry(UUID postId, FeedEntry feedEntry);
    void removeFeedEntry(UUID postId, Long userId);
    List<FeedEntry> getUserFeed(Long userId, int page, int size);
    void invalidateUserFeed(Long userId);
}
