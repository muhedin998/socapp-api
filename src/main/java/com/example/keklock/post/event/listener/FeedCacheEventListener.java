package com.example.keklock.post.event.listener;

import com.example.keklock.post.cqrs.FeedCacheService;
import com.example.keklock.post.cqrs.FeedEntry;
import com.example.keklock.post.domain.Post;
import com.example.keklock.post.event.CommentAddedEvent;
import com.example.keklock.post.event.PostCreatedEvent;
import com.example.keklock.post.event.PostLikedEvent;
import com.example.keklock.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedCacheEventListener {

    private final FeedCacheService feedCacheService;
    private final PostRepository postRepository;

    @Async
    @EventListener
    public void handlePostCreated(PostCreatedEvent event) {
        log.info("CQRS: Adding post {} to followers' feeds", event.postId());

        Post post = postRepository.findById(event.postId()).orElse(null);
        if (post == null) {
            log.warn("Post not found: {}", event.postId());
            return;
        }

        FeedEntry feedEntry = new FeedEntry(
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

        feedCacheService.addToFollowerFeeds(event.authorId(), feedEntry);
    }

    @Async
    @EventListener
    public void handlePostLiked(PostLikedEvent event) {
        log.info("CQRS: Updating like count for post {}", event.postId());

        Post post = postRepository.findById(event.postId()).orElse(null);
        if (post == null) {
            log.warn("Post not found: {}", event.postId());
            return;
        }

        FeedEntry updatedEntry = new FeedEntry(
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

        feedCacheService.updateFeedEntry(event.postId(), updatedEntry);
    }

    @Async
    @EventListener
    public void handleCommentAdded(CommentAddedEvent event) {
        log.info("CQRS: Updating comment count for post {}", event.postId());

        Post post = postRepository.findById(event.postId()).orElse(null);
        if (post == null) {
            log.warn("Post not found: {}", event.postId());
            return;
        }

        FeedEntry updatedEntry = new FeedEntry(
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

        feedCacheService.updateFeedEntry(event.postId(), updatedEntry);
    }
}
