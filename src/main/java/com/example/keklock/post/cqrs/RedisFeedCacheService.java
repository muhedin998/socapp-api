package com.example.keklock.post.cqrs;

import com.example.keklock.profile.domain.Profile;
import com.example.keklock.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.data.redis.enabled", havingValue = "true")
public class RedisFeedCacheService implements FeedCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProfileRepository profileRepository;

    private static final String FEED_KEY_PREFIX = "feed:user:";
    private static final long FEED_TTL_HOURS = 24;

    private String getUserFeedKey(Long userId) {
        return FEED_KEY_PREFIX + userId;
    }

    @Override
    public void addToFollowerFeeds(Long authorId, FeedEntry feedEntry) {
        profileRepository.findById(authorId).ifPresentOrElse(
            author -> {
                author.getFollowers().forEach(follower -> {
                    String key = getUserFeedKey(follower.getId());
                    redisTemplate.opsForList().leftPush(key, feedEntry);
                    redisTemplate.expire(key, FEED_TTL_HOURS, TimeUnit.HOURS);
                    log.debug("Redis: Added post {} to user {}'s feed", feedEntry.postId(), follower.getId());
                });

                String authorKey = getUserFeedKey(authorId);
                redisTemplate.opsForList().leftPush(authorKey, feedEntry);
                redisTemplate.expire(authorKey, FEED_TTL_HOURS, TimeUnit.HOURS);
                log.debug("Redis: Added post {} to author {}'s own feed", feedEntry.postId(), authorId);
            },
            () -> log.warn("Author not found with id: {}", authorId)
        );
    }

    @Override
    public void updateFeedEntry(UUID postId, FeedEntry updatedEntry) {
        Set<String> keys = redisTemplate.keys(FEED_KEY_PREFIX + "*");
        if (keys == null) return;

        keys.forEach(key -> {
            List<Object> feed = redisTemplate.opsForList().range(key, 0, -1);
            if (feed == null) return;

            for (int i = 0; i < feed.size(); i++) {
                if (feed.get(i) instanceof FeedEntry entry && entry.postId().equals(postId)) {
                    redisTemplate.opsForList().set(key, i, updatedEntry);
                    log.debug("Redis: Updated post {} in key {}", postId, key);
                    break;
                }
            }
        });
    }

    @Override
    public void removeFeedEntry(UUID postId, Long userId) {
        String key = getUserFeedKey(userId);
        List<Object> feed = redisTemplate.opsForList().range(key, 0, -1);
        if (feed == null) return;

        feed.stream()
            .filter(obj -> obj instanceof FeedEntry)
            .map(obj -> (FeedEntry) obj)
            .filter(entry -> entry.postId().equals(postId))
            .findFirst()
            .ifPresent(entry -> {
                redisTemplate.opsForList().remove(key, 1, entry);
                log.debug("Redis: Removed post {} from user {}'s feed", postId, userId);
            });
    }

    @Override
    public List<FeedEntry> getUserFeed(Long userId, int page, int size) {
        String key = getUserFeedKey(userId);
        long start = (long) page * size;
        long end = start + size - 1;

        List<Object> feed = redisTemplate.opsForList().range(key, start, end);
        if (feed == null || feed.isEmpty()) {
            return Collections.emptyList();
        }

        return feed.stream()
            .filter(obj -> obj instanceof FeedEntry)
            .map(obj -> (FeedEntry) obj)
            .collect(Collectors.toList());
    }

    @Override
    public void invalidateUserFeed(Long userId) {
        String key = getUserFeedKey(userId);
        redisTemplate.delete(key);
        log.debug("Redis: Invalidated feed cache for user: {}", userId);
    }
}
