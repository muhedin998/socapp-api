package com.example.keklock.post.service;

import com.example.keklock.common.exception.AlreadyLikedException;
import com.example.keklock.common.exception.NotLikedException;
import com.example.keklock.common.exception.ResourceNotFoundException;
import com.example.keklock.common.exception.UnauthorizedActionException;
import com.example.keklock.post.domain.Comment;
import com.example.keklock.post.domain.Post;
import com.example.keklock.post.dto.*;
import com.example.keklock.post.event.CommentAddedEvent;
import com.example.keklock.post.event.PostCreatedEvent;
import com.example.keklock.post.event.PostLikedEvent;
import com.example.keklock.post.repository.CommentRepository;
import com.example.keklock.post.repository.PostRepository;
import com.example.keklock.profile.domain.Profile;
import com.example.keklock.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
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
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ProfileRepository profileRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PostResponse createPost(String identityId, CreatePostRequest request) {
        Profile author = profileRepository.findByIdentityId(identityId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        Post post = new Post();
        post.setAuthor(author);
        post.setContent(request.content());
        post.setImageUrl(request.imageUrl());

        Post saved = postRepository.save(post);
        log.info("Post created by user: {}", identityId);

        PostCreatedEvent event = new PostCreatedEvent(
            saved.getId(),
            author.getId(),
            author.getUsername(),
            saved.getContent()
        );
        eventPublisher.publishEvent(event);
        log.debug("Published PostCreatedEvent for postId: {}", saved.getId());

        return PostResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(UUID postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        return PostResponse.from(post);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getUserPosts(String username, Pageable pageable) {
        Page<Post> posts = postRepository.findByAuthorUsername(username, pageable);
        return posts.map(PostResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getFeed(String identityId, Pageable pageable) {
        Profile user = profileRepository.findByIdentityId(identityId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        List<Profile> following = new ArrayList<>(user.getFollowing());
        following.add(user);

        Page<Post> posts = postRepository.findByAuthorsIn(following, pageable);
        return posts.map(PostResponse::from);
    }

    @Transactional
    public void deletePost(String identityId, UUID postId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (!post.getAuthor().getIdentityId().equals(identityId)) {
            throw new UnauthorizedActionException("You can only delete your own posts");
        }

        postRepository.delete(post);
        log.info("Post deleted: {}", postId);
    }

    @Transactional
    public void likePost(String identityId, UUID postId) {
        Profile user = profileRepository.findByIdentityId(identityId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (post.isLikedBy(user)) {
            throw new AlreadyLikedException("Already liked this post");
        }

        post.addLike(user);
        postRepository.save(post);
        log.info("Post {} liked by user {}", postId, identityId);

        PostLikedEvent event = new PostLikedEvent(
            postId,
            user.getId(),
            user.getUsername(),
            post.getAuthor().getId()
        );
        eventPublisher.publishEvent(event);
        log.debug("Published PostLikedEvent for postId: {}", postId);
    }

    @Transactional
    public void unlikePost(String identityId, UUID postId) {
        Profile user = profileRepository.findByIdentityId(identityId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (!post.isLikedBy(user)) {
            throw new NotLikedException("Post not liked yet");
        }

        post.removeLike(user);
        postRepository.save(post);
        log.info("Post {} unliked by user {}", postId, identityId);
    }

    @Transactional
    public CommentResponse addComment(String identityId, UUID postId, CreateCommentRequest request) {
        Profile author = profileRepository.findByIdentityId(identityId)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthor(author);
        comment.setContent(request.content());

        Comment saved = commentRepository.save(comment);
        log.info("Comment added to post {} by user {}", postId, identityId);

        CommentAddedEvent event = new CommentAddedEvent(
            saved.getId(),
            postId,
            author.getId(),
            author.getUsername(),
            saved.getContent(),
            post.getAuthor().getId()
        );
        eventPublisher.publishEvent(event);
        log.debug("Published CommentAddedEvent for commentId: {}", saved.getId());

        return CommentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getPostComments(UUID postId, Pageable pageable) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }

        Page<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);
        return comments.map(CommentResponse::from);
    }

    @Transactional
    public void deleteComment(String identityId, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        if (!comment.getAuthor().getIdentityId().equals(identityId)) {
            throw new UnauthorizedActionException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
        log.info("Comment deleted: {}", commentId);
    }
}
