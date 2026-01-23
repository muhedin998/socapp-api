package com.example.keklock.post.event.listener;

import com.example.keklock.post.event.CommentAddedEvent;
import com.example.keklock.post.event.PostCreatedEvent;
import com.example.keklock.post.event.PostLikedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedEventListener {

    @Async
    @EventListener
    public void handlePostCreated(PostCreatedEvent event) {
        log.info("Feed Event: Post created by user {} (postId: {})",
            event.authorUsername(), event.postId());

        // Future: Push to followers' feed cache (Redis)
        // Future: Send real-time notification to followers
    }

    @Async
    @EventListener
    public void handlePostLiked(PostLikedEvent event) {
        log.info("Feed Event: Post {} liked by user {}",
            event.postId(), event.likerUsername());

        // Future: Update feed cache with new like count
        // Future: Notify post author
    }

    @Async
    @EventListener
    public void handleCommentAdded(CommentAddedEvent event) {
        log.info("Feed Event: Comment added to post {} by user {}",
            event.postId(), event.commenterUsername());

        // Future: Update feed cache with new comment count
        // Future: Notify post author and mentioned users
    }
}
