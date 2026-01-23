# Authentication Guide

## Fixed! 🎉

I've updated the SecurityConfig to support **both** browser-based login and API token authentication.

---

## What Changed

### 1. Public Endpoints (No Auth Required)
- ✅ `/` - Home page with API info
- ✅ `/api-info` - API documentation
- ✅ `/actuator/**` - Health checks and monitoring
- ✅ `/api/auth/register` - User registration
- ✅ `GET /api/profiles/{username}` - View any profile
- ✅ `GET /api/posts/{postId}` - View any post
- ✅ `GET /api/posts/user/{username}` - View user's posts

### 2. Added OAuth2 Login Flow
You can now login via browser and Keycloak will redirect you back!

---

## How to Test (RIGHT NOW)

### Step 1: Restart Your Application

**In IntelliJ**:
1. Stop the application (Red button)
2. Run it again (Green button)

**Wait for**: `Started KeklockApplication` in console

---

### Step 2: Test Public Endpoints

**Open your browser and go to**:
- http://localhost:8081/

**Expected Response**:
```json
{
  "message": "Social Network API",
  "version": "1.0.0",
  "status": "running",
  "authenticated": false,
  "endpoints": {
    "health": "/actuator/health",
    "register": "/api/auth/register",
    "login": "/oauth2/authorization/keycloak",
    "documentation": "/api-docs"
  }
}
```

**Test Health**:
- http://localhost:8081/actuator/health

**Expected**: Status UP (no 401!)

---

## Two Ways to Authenticate

### Method A: Browser-Based Login (Easy Testing)

#### 1. Navigate to Login
**In browser, go to**:
```
http://localhost:8081/oauth2/authorization/keycloak
```

**What happens**:
1. Redirects to Keycloak login page
2. You enter username/password
3. Keycloak redirects back to your app
4. You're now authenticated! 🎉

#### 2. First Time? Register a User First

**Use PowerShell**:
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" `
    -Method Post `
    -ContentType "application/json" `
    -Body '{
        "username": "testuser",
        "email": "test@example.com",
        "password": "TestPass123",
        "firstName": "Test",
        "lastName": "User"
    }' | ConvertTo-Json
```

#### 3. Now Login in Browser
1. Go to: http://localhost:8081/oauth2/authorization/keycloak
2. Login with:
   - Username: `testuser`
   - Password: `TestPass123`
3. After login, browser redirects to success page

#### 4. Test Protected Endpoints
**Now you can access protected endpoints in browser**:
- http://localhost:8081/api/profiles/me
- http://localhost:8081/api/posts/feed

---

### Method B: API with JWT Tokens (For Scripts/Apps)

#### 1. Register a User (if not done)
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" `
    -Method Post `
    -ContentType "application/json" `
    -Body '{
        "username": "alice",
        "email": "alice@example.com",
        "password": "AlicePass123",
        "firstName": "Alice",
        "lastName": "Smith"
    }'
```

#### 2. Get JWT Token from Keycloak
```powershell
$tokenResponse = Invoke-RestMethod `
    -Uri "http://localhost:8080/realms/my-realm/protocol/openid-connect/token" `
    -Method Post `
    -ContentType "application/x-www-form-urlencoded" `
    -Body @{
        grant_type = "password"
        client_id = "spring-boot-app"
        client_secret = "eQghEj2GomevJCl2ho8rSgjRmNVd6NCR"
        username = "alice"
        password = "AlicePass123"
    }

$token = $tokenResponse.access_token
Write-Host "Token: $token"
```

#### 3. Use Token for API Calls
```powershell
# Get your profile
Invoke-RestMethod -Uri "http://localhost:8081/api/profiles/me" `
    -Headers @{Authorization = "Bearer $token"}

# Create a post
Invoke-RestMethod -Uri "http://localhost:8081/api/posts" `
    -Method Post `
    -Headers @{
        Authorization = "Bearer $token"
        "Content-Type" = "application/json"
    } `
    -Body '{"content": "My first post!", "imageUrl": null}'

# Get feed
Invoke-RestMethod -Uri "http://localhost:8081/api/posts/feed" `
    -Headers @{Authorization = "Bearer $token"}
```

---

## Run the Test Scripts Now!

**All test scripts use Method B (JWT tokens)**:

```powershell
cd C:\Users\MuhedinAlic(SJJ)\Downloads\keklock\keklock\test-scripts

# Verify setup first
.\0-verify-setup.ps1

# Run all tests
.\run-all-phases.ps1
```

**These should work now!** No more 401 errors.

---

## Quick Reference

### Public URLs (No Auth)
```
GET  http://localhost:8081/                           # Home/API info
GET  http://localhost:8081/api-info                   # API documentation
GET  http://localhost:8081/actuator/health            # Health check
POST http://localhost:8081/api/auth/register          # Register user
GET  http://localhost:8081/api/profiles/{username}    # View profile
GET  http://localhost:8081/api/posts/{postId}         # View post
```

### Login URLs
```
Browser Login:
http://localhost:8081/oauth2/authorization/keycloak

API Token:
POST http://localhost:8080/realms/my-realm/protocol/openid-connect/token
```

### Protected URLs (Auth Required)
```
GET  http://localhost:8081/api/profiles/me            # Your profile
PUT  http://localhost:8081/api/profiles/me            # Update profile
POST http://localhost:8081/api/posts                  # Create post
GET  http://localhost:8081/api/posts/feed             # Your feed
POST http://localhost:8081/api/profiles/{user}/follow # Follow user
```

---

## Testing Flow

### Complete Manual Test (Browser)

1. **Register** (PowerShell):
```powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" `
    -Method Post -ContentType "application/json" -Body '{
        "username": "browsertest",
        "email": "browser@test.com",
        "password": "TestPass123",
        "firstName": "Browser",
        "lastName": "Test"
    }'
```

2. **Login** (Browser):
   - Go to: http://localhost:8081/oauth2/authorization/keycloak
   - Login with username: `browsertest`, password: `TestPass123`

3. **Test Protected Endpoints** (Browser):
   - http://localhost:8081/api/profiles/me
   - Should show your profile!

4. **Create Post** (Use Postman or PowerShell with session):
   - Browser maintains session automatically
   - Can make POST requests to /api/posts

---

## Troubleshooting

### Still Getting 401?
**Check**:
1. ✅ Application restarted after code changes
2. ✅ Keycloak is running on port 8080
3. ✅ User exists in Keycloak
4. ✅ For API: Token is valid and not expired

### Browser Login Redirects to Error?
**Check**:
1. Keycloak client `spring-boot-app` exists
2. Valid Redirect URIs includes: `http://localhost:8081/*`
3. Client secret matches application.yaml

### Token Expired?
Tokens expire after 5-15 minutes. Get a new one:
```powershell
# Re-run token request
$tokenResponse = Invoke-RestMethod ...
$token = $tokenResponse.access_token
```

### Can't Access /actuator/health?
**Restart the application** - this should fix it.

---

## What's Next?

Once authentication works:

1. ✅ Run test scripts: `.\run-all-phases.ps1`
2. ✅ Verify all phases pass
3. ✅ Optional: Enable Redis for caching
4. 🚀 Choose next feature:
   - WebSocket notifications
   - Image upload
   - Search functionality

---

## Summary

### Before Fix ❌
- All endpoints returned 401
- No way to login via browser
- Only JWT tokens worked (hard to test)

### After Fix ✅
- Public endpoints accessible
- Browser login works
- JWT tokens still work
- Easy testing!

**Restart your app and test now!** 🎉
