# Next Steps Roadmap

## Immediate Actions (This Week)

### 1. Test Current Implementation ⚡
**Priority: HIGH**

```powershell
# Run comprehensive tests
cd test-scripts
.\run-all-phases.ps1
```

**Verify**:
- [ ] All 3 users (alice, bob, charlie) can register
- [ ] Follow/unfollow works correctly
- [ ] Posts appear in followers' feeds
- [ ] Events are logged in console
- [ ] No errors in application logs

### 2. Performance Baseline 📊
**Priority: HIGH**

Create a simple performance test:

```powershell
# Create 10 users and 100 posts, measure feed load time
# Compare standard vs optimized feed

# Standard feed
Measure-Command {
    Invoke-RestMethod "http://localhost:8081/api/posts/feed" -Headers $headers
}

# Optimized feed (with cache)
Measure-Command {
    Invoke-RestMethod "http://localhost:8081/api/posts/feed/optimized" -Headers $headers
}
```

Document the results for future comparison.

### 3. Code Review & Cleanup 🔍
**Priority: MEDIUM**

- [ ] Remove the old `config/SecurityConfig.java` (already moved to `common/config/`)
- [ ] Review all TODO comments in code
- [ ] Run code formatting (IntelliJ: Ctrl+Alt+L)
- [ ] Check for unused imports
- [ ] Verify all logger statements are appropriate

---

## Phase 5: Real-Time Features (1-2 Weeks)

### Feature: WebSocket Notifications 🔔

**Goal**: Users receive instant notifications without polling

#### Implementation Steps:

1. **Add WebSocket Dependencies**

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

2. **Create Notification Module**

```
notification/
├── domain/
│   └── Notification.java
├── repository/
│   └── NotificationRepository.java
├── service/
│   └── NotificationService.java
├── websocket/
│   ├── WebSocketConfig.java
│   └── NotificationWebSocketHandler.java
└── controller/
    └── NotificationController.java
```

3. **Notification Types**:
- New follower
- Post liked
- New comment
- Mentioned in post

4. **Update Event Listeners**:

```java
@Async
@EventListener
public void handlePostLiked(PostLikedEvent event) {
    Notification notification = new Notification(
        event.postAuthorId(),
        NotificationType.POST_LIKED,
        event.likerUsername() + " liked your post"
    );

    notificationService.save(notification);
    webSocketService.sendToUser(event.postAuthorId(), notification);
}
```

**API Endpoints**:
- `GET /api/notifications` - Get user notifications
- `PUT /api/notifications/{id}/read` - Mark as read
- `DELETE /api/notifications/{id}` - Delete notification
- `ws://localhost:8081/notifications` - WebSocket connection

**Estimated Time**: 5-7 days

---

## Phase 6: Media Management (1-2 Weeks)

### Feature: Image & Video Upload 📸

**Goal**: Users can upload profile pictures and post images

#### Implementation Steps:

1. **Choose Storage Provider**:
   - **AWS S3** (recommended for production)
   - **Cloudinary** (easier setup, has free tier)
   - **Local Storage** (development only)

2. **Add Dependencies**:

```xml
<!-- For AWS S3 -->
<dependency>
    <groupId>software.amazon.awssdk</groupId>
    <artifactId>s3</artifactId>
    <version>2.20.0</version>
</dependency>

<!-- OR for Cloudinary -->
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http44</artifactId>
    <version>1.36.0</version>
</dependency>
```

3. **Create Media Module**:

```
media/
├── domain/
│   └── MediaFile.java
├── repository/
│   └── MediaRepository.java
├── service/
│   ├── MediaService.java
│   └── StorageService.java (interface)
├── infrastructure/
│   ├── S3StorageService.java
│   └── CloudinaryStorageService.java
└── controller/
    └── MediaController.java
```

4. **Features**:
- Image upload (max 5MB)
- Image compression
- Thumbnail generation
- Video upload (max 100MB)
- Progress tracking

**API Endpoints**:
- `POST /api/media/upload` - Upload file
- `GET /api/media/{id}` - Get file metadata
- `DELETE /api/media/{id}` - Delete file

**Estimated Time**: 7-10 days

---

## Phase 7: Search & Discovery (2 Weeks)

### Feature: Full-Text Search 🔍

**Goal**: Users can search posts, people, and hashtags

#### Implementation Steps:

1. **Add Elasticsearch**:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
```

2. **Create Search Module**:

```
search/
├── document/
│   ├── PostDocument.java
│   └── ProfileDocument.java
├── repository/
│   ├── PostSearchRepository.java
│   └── ProfileSearchRepository.java
├── service/
│   └── SearchService.java
└── controller/
    └── SearchController.java
```

3. **Features**:
- Search posts by content
- Search users by username/name
- Hashtag support (#java #springboot)
- Trending topics
- Search suggestions (autocomplete)

4. **Update Post Entity**:

```java
@Entity
@Document(indexName = "posts")
public class Post {
    // ... existing fields

    @Column(name = "hashtags")
    private Set<String> hashtags = new HashSet<>();

    @PrePersist
    protected void extractHashtags() {
        // Extract hashtags from content
        Pattern pattern = Pattern.compile("#\\w+");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            hashtags.add(matcher.group().toLowerCase());
        }
    }
}
```

**API Endpoints**:
- `GET /api/search/posts?q={query}` - Search posts
- `GET /api/search/users?q={query}` - Search users
- `GET /api/search/hashtags?q={query}` - Search hashtags
- `GET /api/search/trending` - Get trending topics

**Estimated Time**: 10-14 days

---

## Phase 8: Analytics & Insights (1 Week)

### Feature: User Analytics Dashboard 📈

**Goal**: Users see their engagement metrics

#### Implementation Steps:

1. **Create Analytics Module**:

```
analytics/
├── domain/
│   ├── UserStats.java
│   └── PostStats.java
├── service/
│   └── AnalyticsService.java
└── controller/
    └── AnalyticsController.java
```

2. **Track Metrics**:
- Post views
- Profile views
- Engagement rate
- Follower growth
- Best performing posts

3. **Update Event Listeners**:

```java
@Async
@EventListener
public void handlePostViewed(PostViewedEvent event) {
    analyticsService.incrementPostViews(event.postId());
    analyticsService.trackUserActivity(event.viewerId());
}
```

4. **Features**:
- Personal dashboard
- Post performance
- Audience insights
- Export data as CSV/PDF

**API Endpoints**:
- `GET /api/analytics/me` - My stats
- `GET /api/analytics/posts/{postId}` - Post stats
- `GET /api/analytics/growth` - Follower growth over time
- `GET /api/analytics/engagement` - Engagement metrics

**Estimated Time**: 5-7 days

---

## Phase 9: Advanced Social Features (2 Weeks)

### Feature Set: Enhanced Interactions 💬

#### 1. Direct Messages (DM)

```
message/
├── domain/
│   ├── Conversation.java
│   └── Message.java
├── repository/
│   ├── ConversationRepository.java
│   └── MessageRepository.java
├── service/
│   └── MessageService.java
└── controller/
    └── MessageController.java
```

**Features**:
- One-on-one messaging
- Read receipts
- Typing indicators
- Message reactions
- Image/video messages

**API Endpoints**:
- `GET /api/messages/conversations` - List conversations
- `GET /api/messages/conversations/{id}` - Get messages
- `POST /api/messages/conversations/{id}` - Send message
- `PUT /api/messages/{id}/read` - Mark as read

#### 2. Stories (24-hour content)

```
story/
├── domain/
│   └── Story.java
├── repository/
│   └── StoryRepository.java
├── service/
│   └── StoryService.java
└── controller/
    └── StoryController.java
```

**Features**:
- Post images/videos (expires in 24h)
- Story views tracking
- Story reactions
- Auto-deletion after 24h

#### 3. Post Sharing & Reposting

```java
@Entity
public class Post {
    // ... existing fields

    @ManyToOne
    private Post originalPost; // For reposts

    private boolean isRepost;
}
```

**Estimated Time**: 10-14 days

---

## Phase 10: Production Readiness (1-2 Weeks)

### Goal: Deploy to Production 🚀

#### 1. Security Hardening

- [ ] Enable HTTPS (SSL certificates)
- [ ] Implement rate limiting
- [ ] Add CORS configuration
- [ ] Enable CSRF protection
- [ ] Implement API key authentication (for mobile apps)
- [ ] Add request signing
- [ ] Security headers (X-Frame-Options, etc.)

```java
@Configuration
public class SecurityEnhancementsConfig {

    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.create(100.0); // 100 requests per second
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .headers(headers -> headers
                .frameOptions().deny()
                .xssProtection().enable()
                .contentSecurityPolicy("default-src 'self'")
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()));

        return http.build();
    }
}
```

#### 2. Monitoring & Observability

**Add Actuator & Metrics**:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

**Configure Monitoring**:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus,info
  metrics:
    export:
      prometheus:
        enabled: true
```

**Setup**:
- Prometheus for metrics
- Grafana for dashboards
- ELK Stack for logging (Elasticsearch, Logstash, Kibana)
- Sentry for error tracking

#### 3. Database Optimization

```sql
-- Add indexes for performance
CREATE INDEX idx_profile_identity_id ON profiles(identity_id);
CREATE INDEX idx_profile_username ON profiles(username);
CREATE INDEX idx_post_author_created ON posts(author_id, created_at DESC);
CREATE INDEX idx_post_created ON posts(created_at DESC);
CREATE INDEX idx_comment_post ON comments(post_id);
CREATE INDEX idx_follows_follower ON follows(follower_id);
CREATE INDEX idx_follows_following ON follows(following_id);
```

**Update application.yaml**:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # Change from 'update' to 'validate'
    properties:
      hibernate:
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
```

#### 4. Docker & Kubernetes

**Create Dockerfile**:

```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

**Create docker-compose.yml**:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: keycloak_db
      POSTGRES_USER: keycloak
      POSTGRES_PASSWORD: password123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  keycloak:
    image: quay.io/keycloak/keycloak:26.0
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
    ports:
      - "8080:8080"
    command: start-dev

  app:
    build: .
    ports:
      - "8081:8081"
    depends_on:
      - postgres
      - redis
      - keycloak
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/keycloak_db
      SPRING_DATA_REDIS_HOST: redis
      KEYCLOAK_AUTH_SERVER_URL: http://keycloak:8080

volumes:
  postgres_data:
```

**Kubernetes Deployment** (k8s/deployment.yaml):

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: social-network
spec:
  replicas: 3
  selector:
    matchLabels:
      app: social-network
  template:
    metadata:
      labels:
        app: social-network
    spec:
      containers:
      - name: social-network
        image: your-registry/social-network:latest
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: production
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 20
          periodSeconds: 5
```

#### 5. CI/CD Pipeline

**GitHub Actions** (.github/workflows/deploy.yml):

```yaml
name: Build and Deploy

on:
  push:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3

    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'

    - name: Build with Maven
      run: mvn clean package -DskipTests

    - name: Run tests
      run: mvn test

    - name: Build Docker image
      run: docker build -t social-network:${{ github.sha }} .

    - name: Push to registry
      run: |
        echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
        docker push social-network:${{ github.sha }}

    - name: Deploy to Kubernetes
      run: |
        kubectl set image deployment/social-network social-network=social-network:${{ github.sha }}
```

**Estimated Time**: 10-14 days

---

## Phase 11: Mobile App Support (3-4 Weeks)

### Goal: Create Mobile API Layer

#### 1. Mobile-Specific Endpoints

```
mobile/
├── controller/
│   ├── MobileAuthController.java
│   └── MobileFeedController.java
└── dto/
    ├── MobileFeedResponse.java
    └── CompactProfileResponse.java
```

**Optimizations**:
- Reduced payload sizes
- Image thumbnails
- Paginated responses optimized for infinite scroll
- Push notification tokens

#### 2. Push Notifications

```xml
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.2.0</version>
</dependency>
```

```java
@Service
public class PushNotificationService {

    public void sendNotification(String deviceToken, String title, String body) {
        Message message = Message.builder()
            .setToken(deviceToken)
            .setNotification(Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build())
            .build();

        FirebaseMessaging.getInstance().send(message);
    }
}
```

**Estimated Time**: 20-25 days

---

## Phase 12: Microservices Architecture (4-6 Weeks)

### Goal: Split into Independent Services

#### Services to Create:

1. **Auth Service** (Port 8081)
   - User registration
   - Authentication
   - Keycloak integration

2. **Profile Service** (Port 8082)
   - Profile management
   - Follow/unfollow
   - Social graph

3. **Post Service** (Port 8083)
   - Post CRUD
   - Comments
   - Likes

4. **Feed Service** (Port 8084)
   - Feed generation
   - CQRS read model
   - Redis caching

5. **Notification Service** (Port 8085)
   - Notifications
   - WebSocket handling
   - Push notifications

6. **Media Service** (Port 8086)
   - File upload
   - Image processing
   - CDN integration

7. **Search Service** (Port 8087)
   - Elasticsearch
   - Full-text search

8. **Analytics Service** (Port 8088)
   - Metrics collection
   - Reporting

#### Infrastructure:

```
infrastructure/
├── api-gateway/          # Spring Cloud Gateway (Port 8080)
├── service-registry/     # Eureka Server (Port 8761)
├── config-server/        # Spring Cloud Config (Port 8888)
└── message-broker/       # RabbitMQ or Kafka
```

**Communication**:
- Synchronous: REST APIs via API Gateway
- Asynchronous: Event-driven with RabbitMQ/Kafka

**Estimated Time**: 30-40 days

---

## Priority Matrix

### Must Have (Next 1 Month)
1. ✅ Test current implementation thoroughly
2. ✅ Add WebSocket notifications
3. ✅ Implement media upload
4. ✅ Production hardening

### Should Have (2-3 Months)
1. Search & discovery
2. Analytics dashboard
3. Direct messages
4. Docker deployment

### Nice to Have (3-6 Months)
1. Stories feature
2. Mobile app support
3. Advanced analytics
4. Microservices migration

---

## Learning Resources

### Books
- "Spring Boot in Action" by Craig Walls
- "Microservices Patterns" by Chris Richardson
- "Clean Architecture" by Robert C. Martin

### Online Courses
- Spring Framework Guru
- Baeldung Spring tutorials
- Udemy: Spring Boot Microservices

### Tools to Learn
- Docker & Kubernetes
- Elasticsearch
- Redis advanced features
- AWS/Azure cloud services
- Terraform (Infrastructure as Code)

---

## Maintenance Tasks (Ongoing)

### Weekly
- [ ] Review application logs
- [ ] Check error rates
- [ ] Monitor database performance
- [ ] Update dependencies

### Monthly
- [ ] Security audit
- [ ] Performance testing
- [ ] Database cleanup
- [ ] Backup verification

### Quarterly
- [ ] Major dependency updates
- [ ] Architecture review
- [ ] Scalability testing
- [ ] Disaster recovery drill

---

## Decision Points

### Question 1: What's Your Primary Goal?

**A) Launch MVP Quickly** (2-4 weeks)
→ Focus on: Testing + Notifications + Media Upload + Production Deploy

**B) Build Comprehensive Platform** (3-6 months)
→ Follow phases 5-11 sequentially

**C) Learn & Experiment** (Flexible timeline)
→ Pick features that interest you most

### Question 2: Expected Scale?

**A) < 1,000 users**
→ Current architecture is sufficient, add features as needed

**B) 1,000 - 100,000 users**
→ Optimize: Redis, database indexing, CDN for media

**C) > 100,000 users**
→ Plan for: Microservices, Kubernetes, distributed caching

### Question 3: Team Size?

**A) Solo Developer**
→ Focus on features, use managed services (AWS RDS, Cloudinary, etc.)

**B) Small Team (2-5)**
→ Split by modules (one person per module)

**C) Large Team (5+)**
→ Consider microservices sooner

---

## My Recommendation

### Start Here (Next 2 Weeks):

1. **Day 1-2**: Run all tests, fix any issues
2. **Day 3-5**: Implement WebSocket notifications
3. **Day 6-8**: Add media upload (use Cloudinary for simplicity)
4. **Day 9-11**: Production hardening (security, monitoring)
5. **Day 12-14**: Deploy to staging environment (Docker)

### Then (Week 3-4):

6. Add search functionality
7. Implement direct messages
8. Create admin dashboard

### After That:

Choose based on your goals:
- **MVP Launch**: Deploy to production, gather user feedback
- **Feature Development**: Continue with Phase 7-9
- **Scaling**: Focus on performance optimization

---

## Need Help?

As you implement these phases, you can:

1. Ask me to implement specific features
2. Get code reviews
3. Discuss architecture decisions
4. Debug issues
5. Optimize performance

**Just let me know which phase you want to tackle next!** 🚀
