# Social Network API Documentation

## Overview
This is a social network application built using Spring Boot with Hexagonal Architecture and Modular Monolith design patterns. It integrates with Keycloak for authentication and authorization.

## Architecture

### Modular Structure
```
com.example.keklock
├── auth          - Authentication and registration (Keycloak integration)
├── profile       - User profiles and social graph (followers/following)
├── post          - Posts, comments, and likes
└── common        - Shared components (exceptions, DTOs, security)
```

### Design Patterns
- **Hexagonal Architecture**: Clean separation between domain logic and infrastructure
- **Ports and Adapters**: KeycloakAdapter implements IdentityProviderPort
- **Dual-Write Pattern**: Ensures consistency between Keycloak and local database
- **Repository Pattern**: Data access abstraction

## API Endpoints

### Authentication

#### Register New User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "identityId": "uuid-from-keycloak",
    "username": "johndoe",
    "email": "john@example.com",
    "message": "User registered successfully"
  },
  "timestamp": "2024-01-22T10:30:00"
}
```

### Profile Management

#### Get Current User Profile
```http
GET /api/profiles/me
Authorization: Bearer {jwt-token}
```

#### Get Profile by Username
```http
GET /api/profiles/{username}
```

#### Update Profile
```http
PUT /api/profiles/me
Authorization: Bearer {jwt-token}
Content-Type: application/json

{
  "bio": "Software developer and tech enthusiast",
  "avatarUrl": "https://example.com/avatar.jpg"
}
```

#### Follow User
```http
POST /api/profiles/{username}/follow
Authorization: Bearer {jwt-token}
```

#### Unfollow User
```http
DELETE /api/profiles/{username}/follow
Authorization: Bearer {jwt-token}
```

#### Get Followers
```http
GET /api/profiles/{username}/followers
```

#### Get Following
```http
GET /api/profiles/{username}/following
```

### Posts

#### Create Post
```http
POST /api/posts
Authorization: Bearer {jwt-token}
Content-Type: application/json

{
  "content": "This is my first post!",
  "imageUrl": "https://example.com/image.jpg"
}
```

#### Get Post by ID
```http
GET /api/posts/{postId}
```

#### Get User Posts
```http
GET /api/posts/user/{username}?page=0&size=20
```

#### Get Feed (Posts from Following)
```http
GET /api/posts/feed?page=0&size=20
Authorization: Bearer {jwt-token}
```

#### Delete Post
```http
DELETE /api/posts/{postId}
Authorization: Bearer {jwt-token}
```

#### Like Post
```http
POST /api/posts/{postId}/like
Authorization: Bearer {jwt-token}
```

#### Unlike Post
```http
DELETE /api/posts/{postId}/like
Authorization: Bearer {jwt-token}
```

### Comments

#### Add Comment
```http
POST /api/posts/{postId}/comments
Authorization: Bearer {jwt-token}
Content-Type: application/json

{
  "content": "Great post!"
}
```

#### Get Post Comments
```http
GET /api/posts/{postId}/comments
```

#### Delete Comment
```http
DELETE /api/posts/comments/{commentId}
Authorization: Bearer {jwt-token}
```

## Authentication Flow

### Getting a JWT Token

1. **Register a user** (as shown above)

2. **Get token from Keycloak**:
```http
POST http://localhost:8080/realms/my-realm/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password
&client_id=spring-boot-app
&client_secret=eQghEj2GomevJCl2ho8rSgjRmNVd6NCR
&username=johndoe
&password=SecurePass123
```

3. **Use the access_token** from the response in subsequent requests:
```http
Authorization: Bearer {access_token}
```

## Error Responses

### Validation Error (400)
```json
{
  "status": 400,
  "errors": {
    "username": "Username is required",
    "email": "Email should be valid"
  },
  "timestamp": "2024-01-22T10:30:00"
}
```

### Not Found (404)
```json
{
  "status": 404,
  "message": "Profile not found with username: johndoe",
  "timestamp": "2024-01-22T10:30:00"
}
```

### Conflict (409)
```json
{
  "status": 409,
  "message": "Username already exists: johndoe",
  "timestamp": "2024-01-22T10:30:00"
}
```

### Unauthorized (401)
```json
{
  "status": 401,
  "message": "Full authentication is required to access this resource",
  "timestamp": "2024-01-22T10:30:00"
}
```

## Database Schema

### Tables
- **profiles**: User profiles linked to Keycloak identities
- **posts**: User-generated content
- **comments**: Comments on posts
- **follows**: Many-to-many relationship for followers/following
- **post_likes**: Many-to-many relationship for post likes

## Configuration

### Required Environment Variables
Update `application.yaml` with your configuration:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/your-db
    username: your-username
    password: your-password

keycloak:
  auth-server-url: http://localhost:8080
  realm: my-realm
  admin:
    username: admin
    password: admin
```

## Running the Application

1. Start PostgreSQL database
2. Start Keycloak server
3. Run the application:
```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8081`
