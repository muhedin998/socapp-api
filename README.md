# Social Network API

A modern social networking platform built with Spring Boot 4.0, featuring real-time notifications, event-driven architecture, and Keycloak authentication.

## 🚀 Features

### Core Functionality
- **User Authentication** - Keycloak OAuth2/OpenID Connect integration
- **User Profiles** - Profile management with avatars and bios
- **Social Graph** - Follow/unfollow users
- **Posts & Comments** - Create, like, and comment on posts
- **Personalized Feed** - View posts from followed users
- **Real-time Notifications** - WebSocket-based instant notifications
- **Notification Preferences** - Granular control over notification types

### Architecture Highlights
- **Modular Monolith** - Clean separation of domain modules
- **Hexagonal Architecture** - Ports and adapters pattern
- **Event-Driven** - Async event processing with Spring Events
- **CQRS** - Optimized read/write models for feed
- **Redis Caching** - Performance optimization for feeds
- **Domain-Driven Design** - Rich domain models with business logic

## 📋 Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 15+ (via Docker)
- Keycloak 26.0+ (via Docker)
- Redis 7+ (via Docker)

## 🏃 Quick Start

### 1. Start Infrastructure Services

```bash
# Start PostgreSQL, Keycloak, and Redis
docker compose up -d

# Verify services are running
docker ps
```

### 2. Configure Keycloak

See [`docs/AUTHENTICATION_GUIDE.md`](docs/AUTHENTICATION_GUIDE.md) for detailed setup instructions.

Quick setup:
1. Access Keycloak: http://localhost:8080
2. Login with admin/admin
3. Create realm: `my-realm`
4. Create client: `spring-boot-app`
5. Configure redirect URIs: `http://localhost:8081/*`

### 3. Run the Application

```bash
mvn spring-boot:run
```

The API will be available at: http://localhost:8081

## 🧪 Testing

```bash
# Run all tests
mvn test

# Use the provided test scripts
cd test-scripts
./phase1-registration.sh        # Test user registration
./phase2-social-graph.sh         # Test follow/unfollow
./phase3-posts-content.sh        # Test posts and comments
```

## 📚 Documentation

Comprehensive documentation is available in the [`docs/`](docs/) folder:

- **[Quick Start Guide](docs/QUICK_START.md)** - Get up and running quickly
- **[API Documentation](docs/API_DOCUMENTATION.md)** - Complete API reference
- **[Authentication Guide](docs/AUTHENTICATION_GUIDE.md)** - Keycloak setup and OAuth2 configuration
- **[Docker Compose Guide](docs/DOCKER_COMPOSE_GUIDE.md)** - Infrastructure setup
- **[Project Structure](docs/PROJECT_STRUCTURE.md)** - Architecture and code organization
- **[Implementation Summary](docs/IMPLEMENTATION_SUMMARY.md)** - Technical implementation details
- **[Migration Guide](docs/MIGRATION_GUIDE.md)** - Deployment and migration strategies

## 🏗️ Project Structure

```
keklock/
├── src/main/java/com/example/keklock/
│   ├── auth/              # Authentication & Keycloak integration
│   ├── profile/           # User profiles & social graph
│   ├── post/              # Posts, comments, feed (CQRS)
│   ├── notification/      # Real-time notifications system
│   └── common/            # Shared components & configuration
├── docs/                  # Comprehensive documentation
├── test-scripts/          # API test scripts
└── keycloak-backup/       # Keycloak configuration backup
```

## 🔧 Technology Stack

| Category | Technology |
|----------|-----------|
| **Framework** | Spring Boot 4.0.1 |
| **Language** | Java 21 |
| **Build Tool** | Maven |
| **Database** | PostgreSQL 15 |
| **Cache** | Redis 7 |
| **Authentication** | Keycloak 26.0 (OAuth2/OIDC) |
| **Real-time** | WebSocket (STOMP) |
| **ORM** | Hibernate/JPA |
| **Security** | Spring Security |

## 🔐 Security

- OAuth2/OpenID Connect via Keycloak
- JWT-based authentication
- Role-based access control
- WebSocket security
- CSRF protection disabled (API-only)

## 📊 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user

### Profiles
- `GET /api/profiles/{username}` - Get user profile
- `PUT /api/profiles/me` - Update own profile
- `POST /api/profiles/{username}/follow` - Follow user
- `DELETE /api/profiles/{username}/follow` - Unfollow user

### Posts
- `POST /api/posts` - Create post
- `GET /api/posts/{postId}` - Get post
- `GET /api/posts/feed` - Get personalized feed
- `POST /api/posts/{postId}/like` - Like post
- `POST /api/posts/{postId}/comments` - Add comment

### Notifications
- `GET /api/notifications` - Get notifications
- `GET /api/notifications/unread/count` - Get unread count
- `PUT /api/notifications/{id}/read` - Mark as read
- `PUT /api/notifications/read-all` - Mark all as read
- `GET /api/notifications/preferences` - Get preferences

For complete API documentation, see [`docs/API_DOCUMENTATION.md`](docs/API_DOCUMENTATION.md).

## 🌐 WebSocket Endpoints

- **Connection**: `ws://localhost:8081/ws`
- **User Notifications**: `/user/{userId}/queue/notifications`

## ⚙️ Configuration

Key configuration in `application.yaml`:

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/keycloak_db
    username: keycloak
    password: password123

  data:
    redis:
      host: localhost
      port: 6379

keycloak:
  auth-server-url: http://localhost:8080
  realm: my-realm
```

## 🔄 Backup & Restore

### Backup Keycloak Configuration

```bash
cd keycloak-backup
./backup.sh
```

This creates a portable backup of your Keycloak realm, clients, users, and roles.

### Restore on Another Machine

```bash
cd keycloak-backup
./restore.sh
```

See [`keycloak-backup/README.md`](keycloak-backup/README.md) for details.

## 🐛 Troubleshooting

### Application won't start
1. Ensure PostgreSQL is running: `docker ps | grep postgres`
2. Check database name matches: `keycloak_db`
3. Verify Keycloak is accessible: http://localhost:8080

### Authentication errors
1. Verify realm exists: `my-realm`
2. Check client configuration in Keycloak
3. Ensure JWT issuer URI is correct

### WebSocket connection issues
1. Check CORS settings
2. Verify WebSocket endpoint: `/ws`
3. Ensure proper authentication

## 📈 Performance

- **Redis Caching**: Feed entries cached for 24 hours
- **Connection Pooling**: HikariCP with optimized settings
- **Async Processing**: Event handlers run asynchronously
- **Scheduled Cleanup**: Old notifications cleaned daily

## 🤝 Contributing

1. Follow the modular architecture
2. Keep domain logic pure
3. Use DTOs for API contracts
4. Write tests for business logic
5. Follow Spring Boot best practices

## 📝 License

This project is for educational purposes.

## 👨‍💻 Development

### Build the project
```bash
mvn clean install
```

### Run tests
```bash
mvn test
```

### Create a production build
```bash
mvn clean package -DskipTests
```

### Run the JAR
```bash
java -jar target/keklock-0.0.1-SNAPSHOT.jar
```

## 🔗 Useful Links

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Redis Documentation](https://redis.io/docs/)

---

**Built with ❤️ using Spring Boot and modern Java**
