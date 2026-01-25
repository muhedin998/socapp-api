package com.example.keklock.post.cqrs;

import com.example.keklock.profile.domain.Profile;
import com.example.keklock.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.data.redis.enabled", havingValue = "false", matchIfMissing = true)
public class InMemoryFeedCacheService implements FeedCacheService {

    private final Map<Long, List<FeedEntry>> feedCache = new ConcurrentHashMap<>();
    private final ProfileRepository profileRepository;

    @Override
    public void addToFollowerFeeds(Long authorId, FeedEntry feedEntry) {
        profileRepository.findById(authorId).ifPresentOrElse(
            author -> {
                author.getFollowers().forEach(follower -> {
                    feedCache.computeIfAbsent(follower.getId(), k -> new ArrayList<>()).add(feedEntry);
                    log.debug("Added post {} to user {}'s feed", feedEntry.postId(), follower.getId());
                });

                feedCache.computeIfAbsent(authorId, k -> new ArrayList<>()).add(feedEntry);
                log.debug("Added post {} to author {}'s own feed", feedEntry.postId(), authorId);
            },
            () -> log.warn("Author not found with id: {}", authorId)
        );
    }

    @Override
    public void updateFeedEntry(UUID postId, FeedEntry updatedEntry) {
        feedCache.forEach((userId, entries) -> {
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).postId().equals(postId)) {
                    entries.set(i, updatedEntry);
                    log.debug("Updated post {} in user {}'s feed", postId, userId);
                    break;
                }
            }
        });
    }

    @Override
    public void removeFeedEntry(UUID postId, Long userId) {
        List<FeedEntry> userFeed = feedCache.get(userId);
        if (userFeed != null) {
            userFeed.removeIf(entry -> entry.postId().equals(postId));
            log.debug("Removed post {} from user {}'s feed", postId, userId);
        }
    }

    @Override
    public List<FeedEntry> getUserFeed(Long userId, int page, int size) {
        List<FeedEntry> userFeed = feedCache.getOrDefault(userId, new ArrayList<>());

        Collections.sort(userFeed);

        int start = page * size;
        int end = Math.min(start + size, userFeed.size());

        if (start >= userFeed.size()) {
            return Collections.emptyList();
        }

        return new ArrayList<>(userFeed.subList(start, end));
    }

    @Override
    public void invalidateUserFeed(Long userId) {
        feedCache.remove(userId);
        log.debug("Invalidated feed cache for user: {}", userId);
    }
}
