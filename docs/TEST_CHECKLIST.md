# Testing Checklist - Social Network Application

## ✅ Pre-Testing Setup

### 1. Services Running
- [ ] PostgreSQL on port 5432
- [ ] Keycloak on port 8080
- [ ] Application on port 8081 (in IntelliJ)
- [ ] Redis on port 6379 (optional, for Phase 4)

### 2. Keycloak Configuration
- [ ] Realm `my-realm` created
- [ ] Client `spring-boot-app` created
- [ ] Client secret matches application.yaml
- [ ] Admin CLI enabled

### 3. Database Setup
- [ ] Database `keycloak_db` exists
- [ ] User `keycloak` has permissions
- [ ] Application can connect

---

## 🧪 Phase 1: Registration & Profile Link

### Test 1.1: User Registration
```powershell
cd test-scripts
.\phase1-registration.ps1
```

**Expected Results**:
- [ ] Alice registered successfully
- [ ] Bob registered successfully
- [ ] Charlie registered successfully
- [ ] Duplicate username rejected (409 error)
- [ ] Users exist in Keycloak
- [ ] Profiles exist in database

**Manual Verification**:
```sql
-- Check profiles in database
SELECT id, identity_id, username, email FROM profiles;
```

### Test 1.2: JWT Token Acquisition
- [ ] Can get token for alice
- [ ] Can get token for bob
- [ ] Can get token for charlie
- [ ] Token works for authenticated endpoints

### Test 1.3: Profile Operations
- [ ] Get profile by username (public)
- [ ] Get own profile (authenticated)
- [ ] Update profile (bio, avatar)
- [ ] Changes persist in database

---

## 👥 Phase 2: Social Graph

### Test 2.1: Follow Relationships
```powershell
.\phase2-social-graph.ps1
```

**Expected Results**:
- [ ] Alice follows Bob ✓
- [ ] Alice follows Charlie ✓
- [ ] Bob follows Alice ✓
- [ ] Charlie follows Alice ✓
- [ ] Bob follows Charlie ✓

**Verify Bidirectional**:
- [ ] Alice's following: [Bob, Charlie]
- [ ] Alice's followers: [Bob, Charlie]
- [ ] Bob's following: [Alice, Charlie]
- [ ] Bob's followers: [Alice]

### Test 2.2: Follow Operations
- [ ] Cannot follow yourself (error)
- [ ] Cannot follow twice (error)
- [ ] Can unfollow
- [ ] Cannot unfollow if not following (error)

### Test 2.3: Follower Counts
- [ ] Counts update correctly
- [ ] Counts match actual relationships

**Manual Verification**:
```sql
-- Check follows table
SELECT f1.follower_id, p1.username as follower,
       f1.following_id, p2.username as following
FROM follows f1
JOIN profiles p1 ON f1.follower_id = p1.id
JOIN profiles p2 ON f1.following_id = p2.id;
```

---

## 📝 Phase 3: Posts & Content

### Test 3.1: Post Creation
```powershell
.\phase3-posts-content.ps1
```

**Expected Results**:
- [ ] Alice creates post ✓
- [ ] Bob creates 2 posts ✓
- [ ] Charlie creates post ✓
- [ ] Posts have correct author
- [ ] Timestamps are set

### Test 3.2: Likes
- [ ] Alice likes Bob's post ✓
- [ ] Charlie likes Bob's post ✓
- [ ] Bob likes Alice's post ✓
- [ ] Like count increases
- [ ] Cannot like twice (error)
- [ ] Can unlike
- [ ] Cannot unlike if not liked (error)

### Test 3.3: Comments
- [ ] Bob comments on Alice's post ✓
- [ ] Charlie comments on Alice's post ✓
- [ ] Alice comments on Bob's post ✓
- [ ] Comments appear in order
- [ ] Comment count increases

### Test 3.4: User Timeline
- [ ] Get Alice's posts (shows 1 post)
- [ ] Get Bob's posts (shows 2 posts)
- [ ] Get Charlie's posts (shows 1 post)
- [ ] Pagination works

### Test 3.5: Personalized Feed
**Alice's Feed** (follows Bob & Charlie):
- [ ] Shows own posts
- [ ] Shows Bob's posts
- [ ] Shows Charlie's posts
- [ ] Sorted by created_at DESC

**Bob's Feed** (follows Alice & Charlie):
- [ ] Shows own posts
- [ ] Shows Alice's posts
- [ ] Shows Charlie's posts

### Test 3.6: Delete Operations
- [ ] Can delete own post
- [ ] Cannot delete others' posts (error)
- [ ] Can delete own comment
- [ ] Cannot delete others' comments (error)

**Manual Verification**:
```sql
-- Check posts
SELECT p.id, prof.username as author, p.content, p.created_at,
       (SELECT COUNT(*) FROM post_likes pl WHERE pl.post_id = p.id) as likes,
       (SELECT COUNT(*) FROM comments c WHERE c.post_id = p.id) as comments
FROM posts p
JOIN profiles prof ON p.author_id = prof.id
ORDER BY p.created_at DESC;
```

---

## 🚀 Phase 4: Event-Driven & CQRS

### Test 4.1: Event Publishing
**Check Application Logs**:
- [ ] PostCreatedEvent logged
- [ ] PostLikedEvent logged
- [ ] CommentAddedEvent logged

**Expected Log Output**:
```
Feed Event: Post created by user alice (postId: xxx)
Feed Event: Post xxx liked by user bob
Feed Event: Comment added to post xxx by user charlie
```

### Test 4.2: Async Processing
- [ ] Events processed in background threads
- [ ] Thread names: `async-event-1`, `async-event-2`, etc.
- [ ] Application remains responsive during event processing

### Test 4.3: CQRS Feed Cache
**Without Redis** (InMemory):
```powershell
# First request (cache miss)
Measure-Command {
    Invoke-RestMethod "http://localhost:8081/api/posts/feed/optimized" `
        -Headers @{Authorization="Bearer $token"}
}

# Second request (cache hit)
Measure-Command {
    Invoke-RestMethod "http://localhost:8081/api/posts/feed/optimized" `
        -Headers @{Authorization="Bearer $token"}
}
```

**Expected**:
- [ ] First request: Similar to standard feed
- [ ] Cache builds up as events are processed
- [ ] Feed entries stored in memory

### Test 4.4: With Redis
**Enable Redis first**:
```yaml
spring:
  data:
    redis:
      enabled: true
```

**Start Redis**:
```bash
docker run -d -p 6379:6379 redis:latest
```

**Test**:
- [ ] Application starts successfully
- [ ] Events update Redis cache
- [ ] Optimized feed reads from Redis
- [ ] Performance improvement visible

**Verify Redis**:
```bash
redis-cli
> KEYS feed:user:*
> LRANGE feed:user:1 0 10
```

---

## 🏥 Health Checks

### Test 5.1: Actuator Endpoints
```powershell
# Health check
Invoke-RestMethod http://localhost:8081/actuator/health

# Application info
Invoke-RestMethod http://localhost:8081/actuator/info

# Metrics
Invoke-RestMethod http://localhost:8081/actuator/metrics
```

**Expected**:
- [ ] Health: UP
- [ ] Database: UP
- [ ] Disk space: OK
- [ ] Info shows app details

---

## 📊 Performance Tests

### Test 6.1: Feed Load Time Comparison

```powershell
# Function to measure
function Test-FeedPerformance {
    param($endpoint, $token)

    $times = @()
    for ($i = 0; $i -lt 10; $i++) {
        $time = Measure-Command {
            Invoke-RestMethod $endpoint -Headers @{Authorization="Bearer $token"}
        }
        $times += $time.TotalMilliseconds
    }

    $avg = ($times | Measure-Object -Average).Average
    Write-Host "Average: $avg ms"
    return $avg
}

# Standard feed
$stdTime = Test-FeedPerformance "http://localhost:8081/api/posts/feed" $aliceToken

# Optimized feed
$optTime = Test-FeedPerformance "http://localhost:8081/api/posts/feed/optimized" $aliceToken

# Compare
$improvement = [math]::Round(($stdTime - $optTime) / $stdTime * 100, 2)
Write-Host "Performance improvement: $improvement%"
```

**Expected Results**:
- [ ] Standard feed: 50-200ms
- [ ] Optimized feed: 10-50ms
- [ ] Improvement: 2-10x faster

### Test 6.2: Concurrent Users
```powershell
# Simulate 10 concurrent requests
$jobs = @()
for ($i = 0; $i -lt 10; $i++) {
    $jobs += Start-Job -ScriptBlock {
        Invoke-RestMethod "http://localhost:8081/api/posts/feed/optimized" `
            -Headers @{Authorization="Bearer $using:aliceToken"}
    }
}

$jobs | Wait-Job | Receive-Job
```

**Expected**:
- [ ] All requests succeed
- [ ] No timeout errors
- [ ] Consistent response times

---

## 🔒 Security Tests

### Test 7.1: Authentication
- [ ] Cannot access protected endpoints without token
- [ ] Expired token rejected (401)
- [ ] Invalid token rejected (401)
- [ ] Valid token accepted

### Test 7.2: Authorization
- [ ] Cannot delete others' posts
- [ ] Cannot delete others' comments
- [ ] Cannot update others' profiles
- [ ] Can only access own data

### Test 7.3: Input Validation
```powershell
# Invalid registration
Invoke-RestMethod http://localhost:8081/api/auth/register -Method Post -ContentType "application/json" -Body '{
    "username": "ab",  # Too short
    "email": "invalid",  # Invalid email
    "password": "123",  # Too short
    "firstName": "",  # Empty
    "lastName": ""  # Empty
}'
```

**Expected**:
- [ ] Returns 400 Bad Request
- [ ] Shows validation errors
- [ ] User not created

---

## 📝 Manual Verification Queries

### Database State
```sql
-- Count records
SELECT 'profiles' as table_name, COUNT(*) as count FROM profiles
UNION ALL
SELECT 'posts', COUNT(*) FROM posts
UNION ALL
SELECT 'comments', COUNT(*) FROM comments
UNION ALL
SELECT 'follows', COUNT(*) FROM follows
UNION ALL
SELECT 'post_likes', COUNT(*) FROM post_likes;

-- Full social graph
SELECT
    p.username,
    (SELECT COUNT(*) FROM follows WHERE follower_id = p.id) as following_count,
    (SELECT COUNT(*) FROM follows WHERE following_id = p.id) as followers_count,
    (SELECT COUNT(*) FROM posts WHERE author_id = p.id) as posts_count
FROM profiles p;
```

---

## ✅ Success Criteria

### All Tests Pass
- [ ] Phase 1: 100% (Registration & Profile)
- [ ] Phase 2: 100% (Social Graph)
- [ ] Phase 3: 100% (Posts & Content)
- [ ] Phase 4: 100% (Events & CQRS)
- [ ] Health: All checks passing
- [ ] Performance: Improvement visible

### Application Logs
- [ ] No ERROR logs
- [ ] Events are logged correctly
- [ ] Async threads working
- [ ] Database queries optimized

### Database Integrity
- [ ] All foreign keys valid
- [ ] No orphaned records
- [ ] Timestamps correct
- [ ] Counts match actual data

---

## 🐛 Common Issues & Solutions

### Issue: Application won't start
**Check**:
- PostgreSQL running
- Keycloak running
- Correct database credentials
- Port 8081 available

### Issue: Tests fail to get token
**Check**:
- Keycloak realm exists
- Client configured correctly
- Client secret matches
- User exists in Keycloak

### Issue: Events not firing
**Check**:
- Async enabled in application
- Event listeners registered
- Check logs for exceptions

### Issue: Feed cache not working
**Check**:
- Cache service bean created
- Events reaching listeners
- Redis connection (if enabled)

---

## 📞 Next Steps After Testing

Once all tests pass:

1. **Document Results**: Note any performance numbers
2. **Start Redis**: For production-like performance
3. **Choose Next Feature**: WebSocket notifications or Media upload
4. **Prepare for Deployment**: Docker setup

---

## 🎯 Testing Completed

Date: __________

Results:
- [ ] All tests passed ✅
- [ ] Some tests failed ⚠️ (see notes below)

Notes:
```
[Add any observations, issues, or performance metrics here]
```

Signed: __________
