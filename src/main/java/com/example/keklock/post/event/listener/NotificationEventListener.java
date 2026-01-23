package com.example.keklock.post.event.listener;

import com.example.keklock.post.event.CommentAddedEvent;
import com.example.keklock.post.event.PostLikedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationEventListener {

    @Async
    @EventListener
    public void handlePostLiked(PostLikedEvent event) {
        log.info("Notification: {} liked your post (postId: {})",
            event.likerUsername(), event.postId());

        // Future: Send notification to post author
        // Future: Save notification to database
        // Future: Send push notification or email
    }

    @Async
    @EventListener
    public void handleCommentAdded(CommentAddedEvent event) {
        log.info("Notification: {} commented on your post (postId: {})",
            event.commenterUsername(), event.postId());

        // Future: Send notification to post author
        // Future: Save notification to database
        // Future: Send push notification or email
    }
}
