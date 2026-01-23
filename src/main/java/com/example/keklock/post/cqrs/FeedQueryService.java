package com.example.keklock.post.cqrs;

import com.example.keklock.common.exception.ResourceNotFoundException;
import com.example.keklock.post.dto.PostResponse;
import com.example.keklock.post.repository.PostRepository;
import com.example.keklock.post.service.PostService;
import com.example.keklock.profile.domain.Profile;
import com.example.keklock.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedQueryService {

    private final FeedCacheService feedCacheService;
    private final ProfileRepository profileRepository;
    private final PostService postService;

    @Transactional(readOnly = true)
    public Page<PostResponse> getOptimizedFeed(String identityId, Pageable pageable) {
        Profile user = profileRepository.findByIdentityId(identityId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        List<FeedEntry> cachedFeed = feedCacheService.getUserFeed(
            user.getId(),
            pageable.getPageNumber(),
            pageable.getPageSize()
        );

        if (cachedFeed.isEmpty()) {
            log.debug("Cache miss for user {}, falling back to database query", user.getId());
            return postService.getFeed(identityId, pageable);
        }

        log.debug("Cache hit for user {}, returning {} entries", user.getId(), cachedFeed.size());

        List<PostResponse> posts = cachedFeed.stream()
            .map(entry -> new PostResponse(
                entry.postId(),
                entry.authorUsername(),
                entry.authorAvatarUrl(),
                entry.content(),
                entry.imageUrl(),
                entry.likesCount(),
                entry.commentsCount(),
                entry.createdAt(),
                entry.updatedAt()
            ))
            .collect(Collectors.toList());

        return new PageImpl<>(posts, pageable, cachedFeed.size());
    }

    public void warmUpCache(Long userId) {
        log.info("Warming up cache for user: {}", userId);
        feedCacheService.invalidateUserFeed(userId);
    }
}
