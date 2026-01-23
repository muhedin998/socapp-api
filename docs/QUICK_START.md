# Quick Start Guide

Get your social network up and running in 5 minutes!

## Prerequisites Checklist

- [ ] Java 21 installed
- [ ] PostgreSQL running on port 5432
- [ ] Keycloak running on port 8080
- [ ] IntelliJ IDEA (or your preferred IDE)

## Step 1: Database Setup (2 minutes)

### Create Database

```sql
CREATE DATABASE keycloak_db;
CREATE USER keycloak WITH PASSWORD 'password123';
GRANT ALL PRIVILEGES ON DATABASE keycloak_db TO keycloak;
```

### Verify Connection

```bash
psql -U keycloak -d keycloak_db -h localhost
# Enter password: password123
# Should connect successfully
```

## Step 2: Keycloak Setup (3 minutes)

### 1. Access Keycloak Admin Console
- URL: http://localhost:8080/admin
- Username: `admin`
- Password: `admin`

### 2. Create Realm
1. Click dropdown next to "Keycloak" (top left)
2. Click "Create Realm"
3. Name: `my-realm`
4. Click "Create"

### 3. Create Client
1. In `my-realm`, go to "Clients"
2. Click "Create client"
3. **General Settings**:
   - Client ID: `spring-boot-app`
   - Client authentication: `ON`
   - Click "Next"

4. **Capability config**:
   - Standard flow: `ON` ✓
   - Direct access grants: `ON` ✓
   - Click "Next"

5. **Login settings**:
   - Valid redirect URIs: `http://localhost:8081/*`
   - Click "Save"

6. **Get Client Secret**:
   - Go to "Credentials" tab
   - Copy the "Client secret"
   - Update `application.yaml` if different from default

### 4. Verify Admin CLI
1. Go to "Clients"
2. Find `admin-cli`
3. Ensure it exists (used for user registration)

## Step 3: Run the Application

### Option A: IntelliJ IDEA

1. Open project in IntelliJ
2. Wait for Maven dependencies to download
3. Find `KeklockApplication.java`
4. Right-click → Run
5. Wait for "Started KeklockApplication" in console
6. Application runs on http://localhost:8081

### Option B: Command Line

```bash
mvnw clean install
mvnw spring-boot:run
```

## Step 4: Test the Application (5 minutes)

### Using PowerShell Scripts

```powershell
cd test-scripts

# Test all phases
.\run-all-phases.ps1

# Or test individually
.\phase1-registration.ps1
.\phase2-social-graph.ps1
.\phase3-posts-content.ps1
```

### Using cURL (Manual)

#### 1. Register a User

```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "TestPass123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

#### 2. Get JWT Token

```bash
curl -X POST http://localhost:8080/realms/my-realm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=spring-boot-app" \
  -d "client_secret=eQghEj2GomevJCl2ho8rSgjRmNVd6NCR" \
  -d "username=testuser" \
  -d "password=TestPass123"
```

Copy the `access_token` from the response.

#### 3. Get Your Profile

```bash
curl http://localhost:8081/api/profiles/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

#### 4. Create a Post

```bash
curl -X POST http://localhost:8081/api/posts \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "My first post!",
    "imageUrl": null
  }'
```

## Step 5: Enable Redis (Optional)

### 1. Start Redis

```bash
docker run -d -p 6379:6379 redis:latest
```

### 2. Update Configuration

Edit `src/main/resources/application.yaml`:

```yaml
spring:
  data:
    redis:
      enabled: true  # Change from false to true
```

### 3. Restart Application

Redis-based feed caching is now active!

### 4. Test Optimized Feed

```bash
curl http://localhost:8081/api/posts/feed/optimized \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## Common Issues & Solutions

### Issue: "Port 8081 already in use"

**Solution**: Change the port in `application.yaml`:
```yaml
server:
  port: 8082
```

### Issue: "Connection refused" to PostgreSQL

**Solution**: Check PostgreSQL is running:
```bash
# Windows
net start postgresql-x64-14

# Linux/Mac
sudo service postgresql start
```

### Issue: "Keycloak admin client failed"

**Solution**: Verify Keycloak admin credentials in `application.yaml`:
```yaml
keycloak:
  admin:
    username: admin
    password: admin
```

### Issue: "JWT token expired"

**Solution**: Get a new token (tokens expire after 5-15 minutes):
```bash
# Use the token request from Step 4.2
```

### Issue: "User already exists"

**Solution**: Either:
1. Use a different username
2. Delete user from Keycloak admin console
3. Reset the database:
```sql
DROP DATABASE keycloak_db;
CREATE DATABASE keycloak_db;
```

## Verify Everything Works

### Health Check Endpoints

```bash
# Application health
curl http://localhost:8081/actuator/health

# Keycloak health
curl http://localhost:8080/health

# Database connection
psql -U keycloak -d keycloak_db -c "SELECT 1;"
```

### Expected Log Output

When everything is working, you should see:

```
Started KeklockApplication in X.XXX seconds
Keycloak admin client initialized successfully
Connected to PostgreSQL database
Hibernate: create table profiles ...
```

## Next Steps

Now that your application is running:

1. **Read Documentation**:
   - `API_DOCUMENTATION.md` - All API endpoints
   - `PHASE4_OPTIMIZATION.md` - Advanced features
   - `IMPLEMENTATION_SUMMARY.md` - Architecture overview

2. **Test All Features**:
   ```powershell
   .\test-scripts\run-all-phases.ps1
   ```

3. **Explore the Code**:
   - `auth/` - User registration
   - `profile/` - Social graph
   - `post/` - Content and feeds
   - `common/` - Shared utilities

4. **Try the Optimized Feed**:
   - Enable Redis
   - Compare performance between `/feed` and `/feed/optimized`

5. **Extend the Application**:
   - Add new features
   - Implement Phase 5-9 from roadmap
   - Customize for your needs

## Performance Testing

### Compare Feed Performance

```powershell
# Standard feed
Measure-Command {
  Invoke-RestMethod "http://localhost:8081/api/posts/feed" `
    -Headers @{Authorization="Bearer $token"}
}

# Optimized feed (with Redis)
Measure-Command {
  Invoke-RestMethod "http://localhost:8081/api/posts/feed/optimized" `
    -Headers @{Authorization="Bearer $token"}
}
```

Expected results:
- Standard: 100-300ms
- Optimized: 10-50ms

## Development Tips

### Hot Reload in IntelliJ
1. File → Settings → Build → Compiler
2. Enable "Build project automatically"
3. Changes reload automatically

### Debug Mode
1. Set breakpoints in your code
2. Right-click → Debug instead of Run
3. Use IntelliJ debugger

### View Database
1. IntelliJ → Database
2. Add PostgreSQL datasource
3. View tables and data in real-time

## Production Checklist

Before deploying to production:

- [ ] Change default passwords in `application.yaml`
- [ ] Update Keycloak client secret
- [ ] Set `ddl-auto: validate` (not `update`)
- [ ] Configure proper logging levels
- [ ] Enable HTTPS
- [ ] Set up monitoring (Actuator + Prometheus)
- [ ] Configure connection pooling
- [ ] Enable Redis for production
- [ ] Set up database backups
- [ ] Configure CORS properly

## Getting Help

- Check `README.md` for detailed setup
- Review `API_DOCUMENTATION.md` for API usage
- See `IMPLEMENTATION_SUMMARY.md` for architecture details
- Open an issue if you encounter problems

## Success Indicators

You'll know everything is working when:

✅ Application starts without errors
✅ You can register a new user
✅ You can get a JWT token
✅ You can create and view posts
✅ You can follow/unfollow users
✅ Your feed shows posts from followed users
✅ Events are logged in the console
✅ Cache updates happen asynchronously

**Congratulations! Your social network is ready!** 🎉
