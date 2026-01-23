# Migration Guide: Old Containers → Docker Compose

## Current Situation

**Running Containers:**
- `keycloak-postgres` (postgres) - port 5432
- `practical_colden` (keycloak) - port 8080
- `social-network-redis` (redis) - port 6379 ✅ (from docker-compose, working!)

**Volumes:**
- `keklock_postgres_data` - Docker Compose PostgreSQL volume (empty, not used yet)
- `keklock_redis_data` - Docker Compose Redis volume (in use ✅)
- Anonymous volume for old containers

## The Problem

Your old containers are blocking ports 5432 and 8080, preventing docker-compose from starting its PostgreSQL and Keycloak containers.

---

## 🎯 Solution: Migrate to Docker Compose

### Option A: Fresh Start (Recommended if OK to lose test data)

**Pros:**
- ✅ Clean setup
- ✅ Docker-compose managed
- ✅ Easy to reset

**Steps:**

1. **Stop old containers:**
```bash
docker stop keycloak-postgres practical_colden
```

2. **Remove old containers:**
```bash
docker rm keycloak-postgres practical_colden
```

3. **Start docker-compose:**
```bash
docker-compose up -d
```

4. **Reconfigure Keycloak** (5 minutes):
   - Go to http://localhost:8080
   - Login: admin / admin
   - Create realm `my-realm`
   - Create client `spring-boot-app`
   - Configure client secret

5. **Re-run tests:**
```bash
cd test-scripts
.\run-all-phases.ps1
```

**Data will be recreated, stored in docker-compose volumes, and preserved across restarts!**

---

### Option B: Backup First (If you want to keep existing data)

**If your old containers have important Keycloak realm configuration or database data:**

1. **Backup PostgreSQL data:**
```bash
docker exec keycloak-postgres pg_dumpall -U postgres > backup_$(date +%Y%m%d).sql
```

2. **Export Keycloak realm** (if configured):
   - Login to http://localhost:8080
   - Go to your realm
   - Export realm configuration
   - Save the JSON file

3. **Then do Option A** (stop, remove, start fresh)

4. **Restore after docker-compose starts:**
```bash
# Restore database
cat backup_20240122.sql | docker exec -i social-network-postgres psql -U keycloak

# Import Keycloak realm
# Use Keycloak admin console to import the JSON
```

---

## 🚀 Recommended: Option A (Fresh Start)

**Why?**
- Your test data can be recreated in 2 minutes
- Cleaner setup
- Docker-compose will manage everything
- Easy to reset anytime with `docker-compose down -v`

---

## Step-by-Step (Do This Now)

### 1. Stop Old Containers
```bash
docker stop keycloak-postgres practical_colden
```

### 2. Remove Old Containers
```bash
docker rm keycloak-postgres practical_colden
```

### 3. Verify They're Gone
```bash
docker ps -a | findstr keycloak
```

**Expected:** No output (containers removed)

### 4. Start Docker Compose
```bash
docker-compose up -d
```

**Expected:**
```
Creating social-network-postgres ... done
Creating social-network-keycloak ... done
social-network-redis is up-to-date
```

### 5. Check Status
```bash
docker-compose ps
```

**Expected:**
```
NAME                          STATUS
social-network-postgres       Up (healthy)
social-network-keycloak       Up (healthy)
social-network-redis          Up (healthy)
```

### 6. Wait for Keycloak (60 seconds)
```bash
docker-compose logs -f keycloak
# Wait until you see "Keycloak ... started"
# Press Ctrl+C to exit logs
```

### 7. Configure Keycloak
**Browser:** http://localhost:8080

**Login:**
- Username: `admin`
- Password: `admin`

**Create Realm:**
- Click dropdown → "Create Realm"
- Name: `my-realm`
- Click "Create"

**Create Client:**
- Clients → "Create client"
- Client ID: `spring-boot-app`
- Next → Client authentication: ON
- Next → Valid redirect URIs: `http://localhost:8081/*`
- Save

**Get Secret:**
- Credentials tab
- Copy client secret

**Update application.yaml** (if secret changed):
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          keycloak:
            client-secret: YOUR_SECRET_HERE
```

### 8. Enable Redis
**Edit `application.yaml`:**
```yaml
spring:
  data:
    redis:
      enabled: true  # Change to true
```

### 9. Restart App in IntelliJ

### 10. Run Tests
```bash
cd test-scripts
.\run-all-phases.ps1
```

**Expected:** All phases pass! ✅

---

## 🧹 Optional: Cleanup Old Volumes

After everything works, clean up unused volumes:

```bash
# List all volumes
docker volume ls

# Remove unused volumes (be careful!)
docker volume prune

# Or remove specific old volumes if you know them
docker volume rm <old-volume-name>
```

---

## ✅ Final State

After migration:

**Containers (all managed by docker-compose):**
```
social-network-postgres  (PostgreSQL)
social-network-keycloak  (Keycloak)
social-network-redis     (Redis)
```

**Volumes (persisted data):**
```
keklock_postgres_data  (Database data)
keklock_redis_data     (Cache data)
```

**Management:**
```bash
docker-compose up -d     # Start all
docker-compose down      # Stop all
docker-compose restart   # Restart all
docker-compose logs -f   # View logs
```

---

## 🎯 Benefits After Migration

- ✅ One command starts everything
- ✅ One command stops everything
- ✅ Easy to reset: `docker-compose down -v && docker-compose up -d`
- ✅ All services health-checked
- ✅ Data persisted in volumes
- ✅ Redis included and enabled
- ✅ Portable to any machine

---

## 🔍 Troubleshooting

### Still see port conflicts?
```bash
# Check what's using the ports
docker ps

# If old containers still running:
docker stop keycloak-postgres practical_colden
docker rm keycloak-postgres practical_colden
```

### Keycloak won't start?
```bash
# Check logs
docker-compose logs keycloak

# Common: Wait 60 seconds for PostgreSQL to be ready
# Restart Keycloak:
docker-compose restart keycloak
```

### Lost data after migration?
**Don't worry!** Your old data might still be in volumes:
```bash
docker volume ls
```

If you see anonymous volumes, you can mount them back or restore from backup.

---

## Ready to Migrate?

**Run these commands:**

```bash
# 1. Stop old containers
docker stop keycloak-postgres practical_colden

# 2. Remove old containers
docker rm keycloak-postgres practical_colden

# 3. Start docker-compose
docker-compose up -d

# 4. Check status
docker-compose ps

# 5. Wait and check logs
docker-compose logs -f
```

**Then configure Keycloak and run tests!**

Let me know when you're ready or if you need help! 🚀
