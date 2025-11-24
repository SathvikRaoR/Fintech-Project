# 🚀 Run Fintech Project - Setup Instructions

## ⚠️ Docker Not Running

I see Docker daemon is not currently running. Here's what you need to do:

---

## Step 1: Start Docker Desktop

### On Windows:
1. Open **Start Menu**
2. Type **"Docker Desktop"**
3. Click **Docker Desktop** to launch it
4. Wait 30-60 seconds for Docker to start (watch the system tray)
5. You'll see a Docker icon in the taskbar when it's ready

### Check if Docker is Running:
Open PowerShell and run:
```powershell
docker ps
```

If you see a list (or empty table), Docker is ready! ✅

---

## Step 2: I've Already Created .env File ✅

Your `.env` file is ready with:
- ✅ JWT_SECRET (generated)
- ✅ AES_KEY (generated)
- ✅ Database credentials
- ✅ Service URLs

**Location**: `S:\Fintech-Project-main\.env`

---

## Step 3: Start Your Project

Once Docker Desktop is running, go to PowerShell:

```powershell
cd S:\Fintech-Project-main

# Start all services
docker-compose up --build
```

**Wait for these messages**:
```
✅ postgres | database system is ready to accept connections
✅ backend | Started BackendApplication in X.XXX seconds
✅ ai-service | Uvicorn running on 0.0.0.0:8000
```

**This takes 2-3 minutes the first time** (downloading images, building)

---

## Step 4: Test Your Backend (New PowerShell)

Keep docker-compose terminal running, open **new PowerShell**:

```powershell
# Test health endpoint
curl http://localhost:8080/api/health

# Expected response:
# {"status":"UP",...}
```

---

## Step 5: Login & Test (Copy the exact command)

```powershell
# Login with demo user
$body = @{
    email = "john@example.com"
    password = "Password123"
} | ConvertTo-Json

curl -X POST http://localhost:8080/api/auth/login `
  -Headers @{"Content-Type"="application/json"} `
  -Body $body

# Expected response: JWT token like:
# {"token":"eyJhbGciOiJIUzUxMiJ9...","expiresIn":3600}
```

---

## 📋 Checklist

- [ ] Docker Desktop installed on your machine
- [ ] Docker Desktop is **RUNNING** (check taskbar)
- [ ] `.env` file created (I did this already ✅)
- [ ] Run: `docker-compose up --build`
- [ ] Wait for all services healthy
- [ ] Test health endpoint
- [ ] Test login endpoint
- [ ] Celebrate! 🎉

---

## ❓ Help

**Problem**: Docker not found  
**Solution**: Install Docker Desktop from https://www.docker.com/products/docker-desktop

**Problem**: "Port 8080 already in use"  
**Solution**: Change port in docker-compose.yml from 8080 → 8081

**Problem**: Services won't start  
**Solution**: Check logs: `docker-compose logs backend`

**Problem**: Can't connect to PostgreSQL  
**Solution**: Wait longer (database takes 30+ seconds to initialize)

---

## 🎯 Summary

**What I've Done**:
- ✅ Created `.env` with secrets
- ✅ Verified Docker Compose installation
- ✅ Found Docker daemon not running (need to start it)

**What You Need to Do**:
1. Start Docker Desktop (1 minute)
2. Run `docker-compose up --build` (2-3 minutes)
3. Test endpoints (2 minutes)

**Total Time**: ~6 minutes

---

**Once Docker Desktop is running, come back and I'll start the services for you!**

Current Status: ⏳ Waiting for Docker Desktop to start
