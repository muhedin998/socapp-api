# Docker Compose Guide

## 🚀 Quick Start

Start **all services** with a single command!

```bash
docker-compose up -d
```

That's it! This starts:
- ✅ PostgreSQL (port 5432)
- ✅ Keycloak (port 8080)
- ✅ Redis (port 6379)

---

## 📋 Prerequisites

### 1. Install Docker Desktop
**Windows**: Download from https://www.docker.com/products/docker-desktop

**After installation**:
- Start Docker Desktop
- Wait for "Docker is running" status

### 2. Verify Docker is Running
```powershell
docker --version
docker-compose --version
```

**Expected**:
```
Docker version 24.x.x
Docker Compose version v2.x.x
```

---

## 🎯 Commands

### Start All Services
```bash
# Start in background (detached mode)
docker-compose up -d

# Start with logs visible
docker-compose up
```

**First time**: Downloads images (takes 2-5 minutes)
**After that**: Starts in ~30 seconds

### Check Status
```bash
docker-compose ps
```

**Expected Output**:
```
NAME                          STATUS    PORTS
social-network-postgres       Up        0.0.0.0:5432->5432/tcp
social-network-keycloak       Up        0.0.0.0:8080->8080/tcp
social-network-redis          Up        0.0.0.0:6379->6379/tcp
```

### View Logs
```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f postgres
docker-compose logs -f keycloak
docker-compose logs -f redis
```

### Stop All Services
```bash
# Stop (keeps data)
docker-compose stop

# Stop and remove containers (keeps data in volumes)
docker-compose down

# Stop and DELETE all data (fresh start)
docker-compose down -v
```

### Restart a Service
```bash
docker-compose restart postgres
docker-compose restart keycloak
docker-compose restart redis
```

---

## 🔍 Verify Services

### 1. PostgreSQL
```powershell
# Test connection
docker exec -it social-network-postgres psql -U keycloak -d keycloak_db -c "SELECT 1;"
```

**Expected**: Returns `1`

### 2. Keycloak
**Browser**: http://localhost:8080

**Login**:
- Username: `admin`
- Password: `admin`

**Or check health**:
```powershell
Invoke-RestMethod http://localhost:8080/health
```

### 3. Redis
```bash
# Test Redis
docker exec -it social-network-redis redis-cli ping
```

**Expected**: `PONG`

**Check keys**:
```bash
docker exec -it social-network-redis redis-cli KEYS *
```

---

## ⚙️ Update Application Configuration

### Enable Redis in Application

Edit `src/main/resources/application.yaml`:

```yaml
spring:
  data:
    redis:
      enabled: true  # Change from false to true
      host: localhost
      port: 6379
```

**Then restart your application in IntelliJ**

---

## 🎨 Optional: Redis Web UI

Want to see what's in Redis? Start the Redis Commander:

```bash
docker-compose --profile tools up -d redis-commander
```

**Access**: http://localhost:8081

**Features**:
- Browse Redis keys
- View cache data
- Delete keys
- Monitor memory usage

---

## 📊 Health Checks

All services have built-in health checks!

### Check Service Health
```bash
# PostgreSQL
docker-compose exec postgres pg_isready -U keycloak

# Redis
docker-compose exec redis redis-cli ping

# Keycloak (wait ~60 seconds after start)
curl http://localhost:8080/health/ready
```

**Docker Compose** automatically:
- Waits for PostgreSQL before starting Keycloak
- Monitors service health
- Restarts unhealthy services

---

## 🗄️ Data Persistence

Data is stored in Docker volumes and **survives restarts**:

### View Volumes
```bash
docker volume ls
```

**You'll see**:
- `keklock_postgres_data` - Database data
- `keklock_redis_data` - Redis cache

### Backup Database
```bash
docker exec social-network-postgres pg_dump -U keycloak keycloak_db > backup.sql
```

### Restore Database
```bash
cat backup.sql | docker exec -i social-network-postgres psql -U keycloak -d keycloak_db
```

### Clear All Data (Fresh Start)
```bash
docker-compose down -v
docker-compose up -d
```

**Note**: This deletes:
- All users
- All posts
- All data in database
- All cache in Redis

---

## 🔧 Troubleshooting

### Port Already in Use
**Error**: `Bind for 0.0.0.0:5432 failed: port is already allocated`

**Solution**: Stop the local service
```powershell
# Stop PostgreSQL service
net stop postgresql-x64-14

# Or change port in docker-compose.yml
ports:
  - "5433:5432"  # Use 5433 instead
```

### Keycloak Not Starting
**Check logs**:
```bash
docker-compose logs keycloak
```

**Common issues**:
- PostgreSQL not ready yet (wait 60 seconds)
- Port 8080 in use (stop local Keycloak)

**Solution**:
```bash
docker-compose restart keycloak
```

### Redis Connection Refused
**Check Redis is running**:
```bash
docker-compose ps redis
```

**Restart if needed**:
```bash
docker-compose restart redis
```

### Application Can't Connect
**If running app in IntelliJ** (not Docker):
- Services are on `localhost`
- Ports: PostgreSQL=5432, Keycloak=8080, Redis=6379

**If running app in Docker** (future):
- Services are on `postgres`, `keycloak`, `redis` hostnames
- Need to update application.yaml

---

## 🌐 Network Information

All services run in `social-network` network.

### View Network
```bash
docker network inspect keklock_social-network
```

### Container Hostnames
**From your machine** (IntelliJ):
- PostgreSQL: `localhost:5432`
- Keycloak: `localhost:8080`
- Redis: `localhost:6379`

**From containers** (future app container):
- PostgreSQL: `postgres:5432`
- Keycloak: `keycloak:8080`
- Redis: `redis:6379`

---

## 📝 Configuration Files

### docker-compose.yml
Main configuration file - defines all services

### .env (Optional)
Create from `.env.example`:
```bash
cp .env.example .env
```

Edit `.env` to change passwords, ports, etc.

**Example**:
```env
POSTGRES_PASSWORD=mySecurePassword123
KEYCLOAK_ADMIN_PASSWORD=myAdminPassword456
```

Then:
```bash
docker-compose up -d
```

---

## 🚀 Complete Workflow

### First Time Setup
```bash
# 1. Start all services
docker-compose up -d

# 2. Wait for services to be ready (60 seconds)
docker-compose logs -f keycloak

# 3. Create Keycloak realm
# Go to http://localhost:8080 and create 'my-realm'

# 4. Update application.yaml (enable Redis)
# Edit: spring.data.redis.enabled = true

# 5. Start your application in IntelliJ

# 6. Run tests
cd test-scripts
.\run-all-phases.ps1
```

### Daily Development
```bash
# Start services
docker-compose up -d

# Run your app in IntelliJ

# Work on features...

# Stop when done
docker-compose stop
```

### Reset Everything
```bash
# Nuclear option - fresh start
docker-compose down -v
docker-compose up -d

# Recreate Keycloak realm
# Re-run test scripts
```

---

## 💡 Pro Tips

### 1. View All Logs in Real-Time
```bash
docker-compose logs -f --tail=100
```

### 2. Execute Commands in Containers
```bash
# PostgreSQL shell
docker exec -it social-network-postgres psql -U keycloak -d keycloak_db

# Redis shell
docker exec -it social-network-redis redis-cli

# Shell access
docker exec -it social-network-postgres sh
```

### 3. Resource Usage
```bash
# See resource usage
docker stats
```

### 4. Quick Health Check
```bash
# Create alias in PowerShell profile
function Check-SocialNetwork {
    docker-compose ps
    Write-Host "`nPostgreSQL:" -ForegroundColor Cyan
    docker exec social-network-postgres pg_isready -U keycloak
    Write-Host "`nRedis:" -ForegroundColor Cyan
    docker exec social-network-redis redis-cli ping
}
```

---

## 🎯 What's Next

### After Services Are Running:

1. **Enable Redis**:
   - Update `application.yaml`
   - Restart app in IntelliJ
   - Test optimized feed

2. **Run Tests**:
   ```powershell
   cd test-scripts
   .\run-all-phases.ps1
   ```

3. **Choose Next Feature**:
   - WebSocket notifications
   - Image upload
   - Search functionality

---

## 📞 Quick Reference

| Service | Port | URL/Command | Credentials |
|---------|------|-------------|-------------|
| PostgreSQL | 5432 | `psql -U keycloak -d keycloak_db -h localhost` | User: keycloak, Pass: password123 |
| Keycloak | 8080 | http://localhost:8080 | Admin: admin, Pass: admin |
| Redis | 6379 | `redis-cli -h localhost` | No password |
| Redis UI | 8081 | http://localhost:8081 | (with --profile tools) |
| App Health | 8082 | http://localhost:8082/actuator/health | (when running) |

---

## ✅ Success Checklist

Before running your application:

- [ ] Docker Desktop is running
- [ ] `docker-compose up -d` executed
- [ ] All services show "Up" in `docker-compose ps`
- [ ] Keycloak accessible at http://localhost:8080
- [ ] Realm `my-realm` created in Keycloak
- [ ] Client `spring-boot-app` configured
- [ ] Redis enabled in application.yaml
- [ ] Application started in IntelliJ
- [ ] Tests passing

**All checked?** You're ready to build features! 🚀
