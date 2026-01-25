package com.example.keklock.post.event.listener;

import com.example.keklock.notification.domain.NotificationType;
import com.example.keklock.notification.service.NotificationService;
import com.example.keklock.post.domain.Post;
import com.example.keklock.post.event.CommentAddedEvent;
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
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final PostRepository postRepository;

    @Async
    @EventListener
    public void handlePostLiked(PostLikedEvent event) {
        log.info("Notification: {} liked post (postId: {})", event.likerUsername(), event.postId());
        
        postRepository.findById(event.postId()).ifPresentOrElse(
            post -> {
                try {
                    notificationService.createNotification(
                        post.getAuthor().getId(),
                        event.likerId(),
                        NotificationType.POST_LIKED,
                        event.postId(),
                        "POST",
                        null
                    );
                } catch (Exception e) {
                    log.error("Failed to create like notification for post: {}", event.postId(), e);
                }
            },
            () -> log.warn("Post not found for notification: {}", event.postId())
        );
    }

    @Async
    @EventListener
    public void handleCommentAdded(CommentAddedEvent event) {
        log.info("Notification: {} commented on post (postId: {})", event.commenterUsername(), event.postId());
        
        postRepository.findById(event.postId()).ifPresentOrElse(
            post -> {
                try {
                    notificationService.createNotification(
                        post.getAuthor().getId(),
                        event.commenterId(),
                        NotificationType.POST_COMMENTED,
                        event.postId(),
                        "POST",
                        null
                    );
                } catch (Exception e) {
                    log.error("Failed to create comment notification for post: {}", event.postId(), e);
                }
            },
            () -> log.warn("Post not found for notification: {}", event.postId())
        );
    }
}
