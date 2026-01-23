# Project Structure Overview

## Directory Tree

```
keklock/
├── src/main/java/com/example/keklock/
│   │
│   ├── auth/                           ⭐ Authentication Module
│   │   ├── config/
│   │   │   └── KeycloakConfig.java
│   │   ├── controller/
│   │   │   └── AuthController.java
│   │   ├── dto/
│   │   │   ├── UserRegistrationRequest.java
│   │   │   └── UserRegistrationResponse.java
│   │   ├── infrastructure/
│   │   │   └── KeycloakAdapter.java
│   │   ├── port/
│   │   │   └── IdentityProviderPort.java
│   │   └── service/
│   │       └── RegistrationOrchestrator.java
│   │
│   ├── profile/                        ⭐ Profile & Social Graph Module
│   │   ├── controller/
│   │   │   └── ProfileController.java
│   │   ├── domain/
│   │   │   └── Profile.java
│   │   ├── dto/
│   │   │   ├── ProfileResponse.java
│   │   │   └── UpdateProfileRequest.java
│   │   ├── repository/
│   │   │   └── ProfileRepository.java
│   │   └── service/
│   │       └── ProfileService.java
│   │
│   ├── post/                           ⭐ Content Module
│   │   ├── controller/
│   │   │   └── PostController.java
│   │   ├── cqrs/                       🚀 CQRS Pattern
│   │   │   ├── FeedEntry.java
│   │   │   ├── FeedCacheService.java
│   │   │   ├── InMemoryFeedCacheService.java
│   │   │   ├── RedisFeedCacheService.java
│   │   │   └── FeedQueryService.java
│   │   ├── domain/
│   │   │   ├── Post.java
│   │   │   └── Comment.java
│   │   ├── dto/
│   │   │   ├── CreatePostRequest.java
│   │   │   ├── CreateCommentRequest.java
│   │   │   ├── PostResponse.java
│   │   │   └── CommentResponse.java
│   │   ├── event/                      🚀 Event-Driven
│   │   │   ├── PostCreatedEvent.java
│   │   │   ├── PostLikedEvent.java
│   │   │   ├── CommentAddedEvent.java
│   │   │   └── listener/
│   │   │       ├── FeedEventListener.java
│   │   │       ├── NotificationEventListener.java
│   │   │       └── FeedCacheEventListener.java
│   │   ├── repository/
│   │   │   ├── PostRepository.java
│   │   │   └── CommentRepository.java
│   │   └── service/
│   │       └── PostService.java
│   │
│   ├── common/                         ⭐ Shared Infrastructure
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   ├── AsyncConfig.java
│   │   │   └── RedisConfig.java
│   │   ├── dto/
│   │   │   └── ApiResponse.java
│   │   ├── event/
│   │   │   └── DomainEvent.java
│   │   └── exception/
│   │       ├── GlobalExceptionHandler.java
│   │       ├── ResourceNotFoundException.java
│   │       └── DuplicateResourceException.java
│   │
│   └── KeklockApplication.java
│
├── src/main/resources/
│   └── application.yaml                📝 Configuration
│
├── test-scripts/                       🧪 Testing
│   ├── phase1-registration.ps1
│   ├── phase2-social-graph.ps1
│   ├── phase3-posts-content.ps1
│   └── run-all-phases.ps1
│
├── docs/                               📚 Documentation
│   ├── README.md
│   ├── API_DOCUMENTATION.md
│   ├── QUICK_START.md
│   ├── PHASE4_OPTIMIZATION.md
│   ├── IMPLEMENTATION_SUMMARY.md
│   └── PROJECT_STRUCTURE.md (this file)
│
└── pom.xml                             📦 Dependencies

```

## Module Dependency Graph

```
                  ┌─────────────┐
                  │   Common    │
                  │  (shared)   │
                  └──────┬──────┘
                         │
           ┌─────────────┼─────────────┐
           │             │             │
           ▼             ▼             ▼
    ┌──────────┐  ┌──────────┐  ┌──────────┐
    │   Auth   │  │ Profile  │  │   Post   │
    │          │  │          │  │          │
    └────┬─────┘  └────┬─────┘  └────┬─────┘
         │             │             │
         └─────────────┼─────────────┘
                       │
                       ▼
              ┌─────────────────┐
              │    Keycloak     │
              │   PostgreSQL    │
              │   Redis (opt)   │
              └─────────────────┘
```

## Feature Map

### Phase 1: Identity & Profile Link ✅
```
User Registration
     │
     ├── Keycloak User Created
     │   └── [KeycloakAdapter]
     │
     └── Local Profile Created
         └── [ProfileRepository]
```

### Phase 2: Social Graph ✅
```
Follow/Unfollow System
     │
     ├── Bidirectional Relationships
     │   └── [Profile.following/followers]
     │
     ├── Followers Count
     │   └── [Profile.getFollowersCount()]
     │
     └── Following Count
         └── [Profile.getFollowingCount()]
```

### Phase 3: Content ✅
```
Posts & Comments
     │
     ├── Create Posts
     │   └── [PostService.createPost()]
     │
     ├── Like/Unlike
     │   ├── [PostService.likePost()]
     │   └── [PostService.unlikePost()]
     │
     ├── Comments
     │   ├── [PostService.addComment()]
     │   └── [PostService.deleteComment()]
     │
     └── Personalized Feed
         └── [PostService.getFeed()]
```

### Phase 4: Optimization ✅
```
Event-Driven + CQRS
     │
     ├── Event Publishing
     │   ├── PostCreatedEvent
     │   ├── PostLikedEvent
     │   └── CommentAddedEvent
     │
     ├── Async Processing
     │   ├── FeedEventListener
     │   ├── NotificationEventListener
     │   └── FeedCacheEventListener
     │
     └── CQRS Feed
         ├── Command: PostService (Write)
         ├── Query: FeedQueryService (Read)
         └── Cache: Redis/InMemory
```

## Data Flow Diagrams

### User Registration Flow
```
Client Request
     │
     ▼
AuthController.register()
     │
     ▼
RegistrationOrchestrator.registerNewUser()
     │
     ├──────────────────┬──────────────────┐
     │                  │                  │
     ▼                  ▼                  ▼
KeycloakAdapter   Create Profile    Rollback on Error
(creates user)    (save to DB)     (delete from KC)
     │                  │
     └────────┬─────────┘
              │
              ▼
    UserRegistrationResponse
```

### Post Creation Flow (with Events)
```
Client: POST /api/posts
     │
     ▼
PostController.createPost()
     │
     ▼
PostService.createPost()
     │
     ├─────────────────┬─────────────────┐
     │                 │                 │
     ▼                 ▼                 ▼
Save to DB      Publish Event    Return Response
     │                 │
     │                 ▼
     │         PostCreatedEvent
     │                 │
     │         ┌───────┴───────┐
     │         │               │
     │         ▼               ▼
     │   FeedEvent      FeedCacheEvent
     │   Listener        Listener
     │                        │
     │                        ▼
     │                Update Feed Cache
     │                (for followers)
     │
     └─────────────────────────────────►
                  Response to Client
```

### Feed Query Flow (CQRS)
```
Client: GET /api/posts/feed/optimized
     │
     ▼
PostController.getOptimizedFeed()
     │
     ▼
FeedQueryService.getOptimizedFeed()
     │
     ▼
FeedCacheService.getUserFeed()
     │
     ├─────────────┬─────────────┐
     │             │             │
     ▼             ▼             ▼
Cache Hit    Cache Miss    Fallback
(Redis/Mem)  (empty)      (Database)
     │             │             │
     │             ▼             │
     │      PostService.      │
     │      getFeed()         │
     │             │             │
     └─────────────┴─────────────┘
                   │
                   ▼
            Feed Response
```

## API Endpoint Organization

### Public Endpoints (No Auth Required)
```
POST   /api/auth/register
GET    /api/profiles/{username}
GET    /api/posts/{postId}
GET    /api/posts/user/{username}
```

### Protected Endpoints (JWT Required)
```
Profile Management:
GET    /api/profiles/me
PUT    /api/profiles/me
POST   /api/profiles/{username}/follow
DELETE /api/profiles/{username}/follow
GET    /api/profiles/{username}/followers
GET    /api/profiles/{username}/following

Post Management:
POST   /api/posts
GET    /api/posts/feed
GET    /api/posts/feed/optimized
DELETE /api/posts/{postId}
POST   /api/posts/{postId}/like
DELETE /api/posts/{postId}/like
POST   /api/posts/{postId}/comments
GET    /api/posts/{postId}/comments
DELETE /api/posts/comments/{commentId}
```

## Configuration Files

### application.yaml
```yaml
Key Configurations:
├── Server (port 8081)
├── Database (PostgreSQL)
├── JPA/Hibernate
├── OAuth2 Resource Server
├── Keycloak Admin Client
├── Redis (optional)
└── Logging
```

### pom.xml
```xml
Dependencies:
├── Spring Boot Starters
│   ├── data-jpa
│   ├── security
│   ├── webmvc
│   ├── oauth2-resource-server
│   ├── oauth2-client
│   ├── validation
│   ├── data-redis
│   └── cache
├── Database
│   └── postgresql
├── Identity
│   └── keycloak-admin-client
├── Utilities
│   └── lombok
└── Testing
    ├── data-jpa-test
    ├── security-test
    └── webmvc-test
```

## Design Patterns Map

```
Hexagonal Architecture
├── auth/port/ (interfaces)
└── auth/infrastructure/ (implementations)

Repository Pattern
├── *Repository interfaces
└── Spring Data JPA implementations

CQRS
├── Command: PostService (writes)
└── Query: FeedQueryService (reads)

Event-Driven
├── Events: post/event/*Event.java
└── Listeners: post/event/listener/*Listener.java

Observer Pattern
└── Spring's @EventListener

Dual-Write Pattern
└── auth/service/RegistrationOrchestrator

DTO Pattern
└── */dto/*.java

Builder Pattern (via Lombok)
└── @Builder annotations
```

## Key Files Explained

| File | Purpose | Pattern |
|------|---------|---------|
| `SecurityConfig` | OAuth2 & JWT setup | Configuration |
| `KeycloakAdapter` | Keycloak integration | Adapter |
| `RegistrationOrchestrator` | Dual-write logic | Orchestrator |
| `PostService` | Post operations | Service |
| `FeedQueryService` | Optimized reads | CQRS Query |
| `FeedCacheService` | Cache abstraction | Strategy |
| `PostCreatedEvent` | Domain event | Event |
| `FeedEventListener` | Async processing | Observer |
| `Profile` | Domain entity | Entity |
| `PostRepository` | Data access | Repository |

## Testing Strategy

```
Phase 1: Foundation
└── Registration + Profile CRUD

Phase 2: Social Features
└── Follow/Unfollow + Relationships

Phase 3: Content
└── Posts + Comments + Likes + Feed

Integration Testing
└── Multi-user workflows
```

## Summary Statistics

- **Total Java Files**: 43
- **Total Lines of Code**: ~3,500+
- **Modules**: 4 (auth, profile, post, common)
- **API Endpoints**: 19
- **Database Tables**: 5
- **Design Patterns**: 8
- **Test Scripts**: 4
- **Documentation Files**: 6

---

**This structure enables**:
- ✅ Easy navigation
- ✅ Clear separation of concerns
- ✅ Independent module development
- ✅ Microservices migration path
- ✅ Team collaboration
- ✅ Feature extensibility
