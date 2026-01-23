# Manual Actions Required

## What I've Completed ✅

### 1. Added Health Check Endpoints
- ✅ Added Spring Boot Actuator dependency to `pom.xml`
- ✅ Configured actuator endpoints in `application.yaml`
- ✅ Added application info metadata

**Available Endpoints**:
- `http://localhost:8081/actuator/health` - Health status
- `http://localhost:8081/actuator/info` - Application info
- `http://localhost:8081/actuator/metrics` - Performance metrics

### 2. Created Testing Infrastructure
- ✅ `TEST_CHECKLIST.md` - Comprehensive testing guide
- ✅ `0-verify-setup.ps1` - Pre-test verification script

### 3. Code Cleanup
- ✅ Verified no duplicate SecurityConfig files
- ✅ Project structure is clean

---

## What You Need to Do Manually 📋

### Step 1: Rebuild the Project in IntelliJ
**Why**: We added new dependencies (Actuator)

**Actions**:
1. In IntelliJ, click on Maven tool window (right side)
2. Click the "Reload All Maven Projects" button (🔄 icon)
3. Or right-click on `pom.xml` → Maven → Reload Project
4. Wait for dependencies to download

**Verify**: Check that `spring-boot-starter-actuator` appears in External Libraries

---

### Step 2: Restart the Application
**Why**: New configuration needs to be loaded

**Actions**:
1. Stop the running application in IntelliJ (Red stop button)
2. Wait 2-3 seconds
3. Run `KeklockApplication` again (Green play button)

**Verify Success**:
Look for these logs:
```
Exposing 3 endpoint(s) beneath base path '/actuator'
Started KeklockApplication in X.XXX seconds
```

---

### Step 3: Verify Setup
**Open PowerShell** and run:

```powershell
cd C:\Users\MuhedinAlic(SJJ)\Downloads\keklock\keklock\test-scripts
.\0-verify-setup.ps1
```

**Expected Output**:
```
✓ PostgreSQL is running on port 5432
✓ Keycloak is running on port 8080
✓ Application is running on port 8081
✓ Application health: UP
✓ Database connection: UP
✓ All critical services are ready!
```

**If any service shows ✗**:
- PostgreSQL: Start your PostgreSQL service
- Keycloak: Start Keycloak server
- Application: Make sure it's running in IntelliJ

---

### Step 4: Test Health Endpoints
**Open PowerShell** or browser:

```powershell
# Test health
Invoke-RestMethod http://localhost:8081/actuator/health | ConvertTo-Json

# Test info
Invoke-RestMethod http://localhost:8081/actuator/info | ConvertTo-Json
```

**Or in browser**:
- http://localhost:8081/actuator/health
- http://localhost:8081/actuator/info

**Expected Response** (health):
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

---

### Step 5: Run All Tests
**Once verification passes**, run the test phases:

```powershell
# Run all phases
.\run-all-phases.ps1

# Or run individually
.\phase1-registration.ps1
.\phase2-social-graph.ps1
.\phase3-posts-content.ps1
```

**What to Watch For**:
- ✅ All users register successfully
- ✅ Follow/unfollow works
- ✅ Posts are created
- ✅ Feeds are populated
- ✅ Events are logged in IntelliJ console

**Check IntelliJ Console** for event logs:
```
Feed Event: Post created by user alice (postId: xxx)
CQRS: Adding post xxx to followers' feeds
```

---

### Step 6: Optional - Enable Redis
**If you want to test Redis caching** (recommended for performance):

#### 6.1 Start Redis
```powershell
docker run -d --name redis -p 6379:6379 redis:latest
```

#### 6.2 Update Configuration
Edit `src/main/resources/application.yaml`:

```yaml
spring:
  data:
    redis:
      enabled: true  # Change from false to true
```

#### 6.3 Restart Application
- Stop in IntelliJ
- Run again

#### 6.4 Verify Redis Connection
Check IntelliJ console for:
```
Connected to Redis at localhost:6379
```

#### 6.5 Test Optimized Feed
```powershell
# Get token first (from test script output)
$token = "YOUR_JWT_TOKEN_HERE"

# Compare performance
Measure-Command {
    Invoke-RestMethod "http://localhost:8081/api/posts/feed" `
        -Headers @{Authorization="Bearer $token"}
}

Measure-Command {
    Invoke-RestMethod "http://localhost:8081/api/posts/feed/optimized" `
        -Headers @{Authorization="Bearer $token"}
}
```

**Expected**: Optimized feed should be 2-10x faster

---

## Troubleshooting

### Issue: Maven dependencies not downloading
**Solution**:
```
Settings → Build → Build Tools → Maven
Check "Always update snapshots"
```

### Issue: Application won't start after adding Actuator
**Check**:
1. Maven reload completed
2. No compilation errors in IntelliJ
3. Check logs for specific error

**If compile errors**:
```powershell
# In PowerShell
cd C:\Users\MuhedinAlic(SJJ)\Downloads\keklock\keklock
./mvnw clean install -DskipTests
```

### Issue: Actuator endpoints return 404
**Check**:
1. Application restarted after config change
2. URL is correct: `http://localhost:8081/actuator/health`
3. Check application.yaml for actuator config

### Issue: Tests fail
**Check TEST_CHECKLIST.md** for specific test guidance

**Common fixes**:
- Ensure all services running
- Check Keycloak realm exists
- Verify database is accessible
- Look at IntelliJ console for errors

---

## Quick Reference

### Services & Ports
| Service | Port | Check |
|---------|------|-------|
| Application | 8081 | http://localhost:8081/actuator/health |
| Keycloak | 8080 | http://localhost:8080/health |
| PostgreSQL | 5432 | `psql -U keycloak -d keycloak_db` |
| Redis (optional) | 6379 | `redis-cli ping` |

### Key Files Modified
- `pom.xml` - Added Actuator dependency
- `application.yaml` - Added actuator configuration
- `test-scripts/0-verify-setup.ps1` - New verification script
- `TEST_CHECKLIST.md` - New testing guide

### Test Scripts Order
1. `0-verify-setup.ps1` - Verify services
2. `phase1-registration.ps1` - Test registration
3. `phase2-social-graph.ps1` - Test social features
4. `phase3-posts-content.ps1` - Test content
5. `run-all-phases.ps1` - Run all tests

---

## Next Steps After Testing

Once all tests pass:

### Option A: Implement WebSocket Notifications
Real-time updates for likes, comments, follows

### Option B: Add Image Upload
Profile pictures and post images

### Option C: Production Deployment
Docker, security hardening, monitoring

**Just tell me which feature you want next!**

---

## Summary Checklist

Before proceeding to next features:

- [ ] Maven dependencies reloaded in IntelliJ
- [ ] Application restarted
- [ ] Verification script passes (`0-verify-setup.ps1`)
- [ ] Health endpoints accessible
- [ ] Phase 1 tests pass (Registration)
- [ ] Phase 2 tests pass (Social Graph)
- [ ] Phase 3 tests pass (Posts & Content)
- [ ] Event logs appearing in console
- [ ] Optional: Redis enabled and tested

**Once all checked**: Ready for Phase 5 (WebSocket) or Phase 6 (Media Upload)! 🚀
