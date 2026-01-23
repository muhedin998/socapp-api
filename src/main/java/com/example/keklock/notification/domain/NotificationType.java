package com.example.keklock.notification.domain;

public enum NotificationType {
    POST_LIKED("liked your post"),
    POST_COMMENTED("commented on your post"),
    PROFILE_FOLLOWED("started following you"),
    POST_MENTIONED("mentioned you in a post"),
    COMMENT_MENTIONED("mentioned you in a comment");

    private final String message;

    NotificationType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}