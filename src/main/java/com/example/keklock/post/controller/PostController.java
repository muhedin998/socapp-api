package com.example.keklock.post.controller;

import com.example.keklock.common.dto.ApiResponse;
import com.example.keklock.post.cqrs.FeedQueryService;
import com.example.keklock.post.dto.*;
import com.example.keklock.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final FeedQueryService feedQueryService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody CreatePostRequest request
    ) {
        String identityId = jwt.getSubject();
        PostResponse post = postService.createPost(identityId, request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Post created successfully", post));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(
        @PathVariable UUID postId
    ) {
        PostResponse post = postService.getPost(postId);
        return ResponseEntity.ok(ApiResponse.success(post));
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getUserPosts(
        @PathVariable String username,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostResponse> posts = postService.getUserPosts(username, pageable);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @GetMapping("/feed")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getFeed(
        @AuthenticationPrincipal Jwt jwt,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String identityId = jwt.getSubject();
        Page<PostResponse> feed = postService.getFeed(identityId, pageable);
        return ResponseEntity.ok(ApiResponse.success(feed));
    }

    @GetMapping("/feed/optimized")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getOptimizedFeed(
        @AuthenticationPrincipal Jwt jwt,
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        String identityId = jwt.getSubject();
        Page<PostResponse> feed = feedQueryService.getOptimizedFeed(identityId, pageable);
        return ResponseEntity.ok(ApiResponse.success(feed));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID postId
    ) {
        String identityId = jwt.getSubject();
        postService.deletePost(identityId, postId);
        return ResponseEntity.ok(ApiResponse.success("Post deleted successfully", null));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<Void>> likePost(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID postId
    ) {
        String identityId = jwt.getSubject();
        postService.likePost(identityId, postId);
        return ResponseEntity.ok(ApiResponse.success("Post liked successfully", null));
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<ApiResponse<Void>> unlikePost(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID postId
    ) {
        String identityId = jwt.getSubject();
        postService.unlikePost(identityId, postId);
        return ResponseEntity.ok(ApiResponse.success("Post unliked successfully", null));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID postId,
        @Valid @RequestBody CreateCommentRequest request
    ) {
        String identityId = jwt.getSubject();
        CommentResponse comment = postService.addComment(identityId, postId, request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("Comment added successfully", comment));
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getPostComments(
        @PathVariable UUID postId
    ) {
        List<CommentResponse> comments = postService.getPostComments(postId);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID commentId
    ) {
        String identityId = jwt.getSubject();
        postService.deleteComment(identityId, commentId);
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully", null));
    }
}
