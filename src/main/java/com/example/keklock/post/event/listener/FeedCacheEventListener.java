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

        postRepository.findById(event.postId()).ifPresentOrElse(
            post -> {
                FeedEntry feedEntry = createFeedEntryFromPost(post);
                feedCacheService.addToFollowerFeeds(event.authorId(), feedEntry);
            },
            () -> log.warn("Post not found: {}", event.postId())
        );
    }

    @Async
    @EventListener
    public void handlePostLiked(PostLikedEvent event) {
        log.info("CQRS: Updating like count for post {}", event.postId());

        postRepository.findById(event.postId()).ifPresentOrElse(
            post -> {
                FeedEntry updatedEntry = createFeedEntryFromPost(post);
                feedCacheService.updateFeedEntry(event.postId(), updatedEntry);
            },
            () -> log.warn("Post not found: {}", event.postId())
        );
    }

    @Async
    @EventListener
    public void handleCommentAdded(CommentAddedEvent event) {
        log.info("CQRS: Updating comment count for post {}", event.postId());

        postRepository.findById(event.postId()).ifPresentOrElse(
            post -> {
                FeedEntry updatedEntry = createFeedEntryFromPost(post);
                feedCacheService.updateFeedEntry(event.postId(), updatedEntry);
            },
            () -> log.warn("Post not found: {}", event.postId())
        );
    }

    private FeedEntry createFeedEntryFromPost(Post post) {
        return new FeedEntry(
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
