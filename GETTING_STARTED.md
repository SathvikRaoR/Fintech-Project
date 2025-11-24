# 🚀 Getting Started - Fintech MVP

**Status**: ✅ Ready to deploy  
**Build Status**: ✅ Compiles successfully  
**Last Update**: November 24, 2025

---

## ⚡ 5-Minute Quick Start

### 1. Clone Repository
```bash
git clone <your-repo-url>
cd Fintech-Project-main
```

### 2. Generate Secrets (One-Time Setup)
```bash
# PowerShell (Windows)
$jwtSecret = [Convert]::ToBase64String((1..32 | ForEach-Object { [byte](Get-Random -Maximum 256) }))
$aesKey = ((1..32 | ForEach-Object { '{0:X2}' -f (Get-Random -Maximum 256) }) -join '')
Write-Host "FINTECH_JWT_SECRET=$jwtSecret"
Write-Host "FINTECH_AES_KEY=$aesKey"

# Or Linux/Mac
openssl rand -base64 32    # For JWT_SECRET
openssl rand -hex 32       # For AES_KEY
```

### 3. Create .env File
```bash
cat > .env << EOF
FINTECH_JWT_SECRET=<your-generated-secret>
FINTECH_AES_KEY=<your-generated-key>
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
EOF
```

### 4. Start Services
```bash
docker-compose --env-file .env up --build
```

Wait for output:
```
✅ postgres is healthy
✅ backend is healthy
✅ ai-service is healthy
```

### 5. Test It Works
```bash
# In new terminal/PowerShell
curl http://localhost:8080/api/health

# Response:
# {"status":"UP","timestamp":"2025-11-24T09:30:00Z"}
```

**Done!** Your backend is running! 🎉

---

## 📋 Step-by-Step Setup

### Prerequisites
- ✅ Docker & Docker Compose (https://docs.docker.com/get-docker/)
- ✅ Git (https://git-scm.com/)
- ✅ PowerShell/Bash/Zsh

### Installation

#### 1. Clone Repository
```bash
git clone <your-repo-url>
cd Fintech-Project-main
```

#### 2. Generate Secrets

**Windows PowerShell**:
```powershell
# Run as regular user (no admin needed)
$jwtSecret = [Convert]::ToBase64String((1..32 | ForEach-Object { [byte](Get-Random -Maximum 256) }))
Write-Host "Copy this JWT Secret:"
Write-Host $jwtSecret

$aesKey = ((1..32 | ForEach-Object { '{0:X2}' -f (Get-Random -Maximum 256) }) -join '')
Write-Host "`nCopy this AES Key:"
Write-Host $aesKey
```

**Linux/Mac**:
```bash
# Generate JWT Secret (Base64)
openssl rand -base64 32

# Generate AES Key (Hex)
openssl rand -hex 32
```

#### 3. Create Environment File

Create file `.env` in project root:
```env
# From step 2
FINTECH_JWT_SECRET=abc123...your-secret-here...
FINTECH_AES_KEY=def456...your-aes-key-here...

# Database (can leave as-is for local dev)
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

**NEVER** commit `.env` to Git! Add to `.gitignore`:
```bash
echo ".env" >> .gitignore
```

#### 4. Start Backend Services

```bash
# Build images and start services
docker-compose --env-file .env up --build

# Output:
# Creating postgres ... done
# Creating backend ... done
# Creating ai-service ... done
# Wait for all "is healthy" messages
```

**Keep this terminal open** - it shows logs!

#### 5. Open New Terminal for Testing

```bash
# Test backend is running
curl http://localhost:8080/api/health

# Expected response:
# {"status":"UP","timestamp":"2025-11-24T09:30:00Z"}

# Test AI service is running
curl http://localhost:8000/health

# Expected response:
# {"status":"healthy","timestamp":"2025-11-24T09:30:00Z"}

# Test database is connected
curl http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"Password123"}'

# Expected response (with real JWT token):
# {"token":"eyJhbGciOiJIUzUxMiJ9...","expiresIn":3600}
```

✅ All working? Move to next section!

---

## 🔐 Security Setup

### Default Demo Users (Pre-Loaded)
```
Email: john@example.com
Password: Password123

Email: jane@example.com  
Password: Password123

Email: charlie@example.com
Password: Password123
```

### Change Database Password (Production)

```bash
# Connect to database
docker exec -it fintech-postgres psql -U postgres -d fintechdb

# Change password
ALTER USER postgres WITH PASSWORD 'NewStrongPassword123!@#';

# Then update .env:
# SPRING_DATASOURCE_PASSWORD=NewStrongPassword123!@#

# Restart backend
docker-compose restart backend
```

### Verify Encryption

Sensitive fields are automatically encrypted:
```bash
# Connect to database
docker exec -it fintech-postgres psql -U postgres -d fintechdb

# Query encrypted fields
SELECT id, email, account_number FROM accounts LIMIT 1;

# account_number will show as: "AES:abc123..." (encrypted)
# This is correct! ✅
```

---

## 🧪 Testing Endpoints

### 1. Register New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "SecurePass123",
    "name": "Test User"
  }'

# Response:
# {
#   "id": 4,
#   "email": "testuser@example.com",
#   "name": "Test User",
#   "kycStatus": "PENDING"
# }
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "SecurePass123"
  }'

# Response:
# {
#   "token": "eyJhbGciOiJIUzUxMiJ9...",
#   "expiresIn": 3600
# }

# SAVE THE TOKEN! You'll use it next
TOKEN="eyJhbGciOiJIUzUxMiJ9..."
```

### 3. Create Account

```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "accountType": "SAVINGS",
    "initialDeposit": 1000.00
  }'

# Response:
# {
#   "id": 5,
#   "accountType": "SAVINGS",
#   "balance": 1000.00,
#   "status": "ACTIVE"
# }

# SAVE THE ACCOUNT ID!
ACCOUNT_ID=5
```

### 4. Deposit Money

```bash
curl -X POST http://localhost:8080/api/transactions/deposit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": '$ACCOUNT_ID',
    "amount": 500.00
  }'

# Response:
# {
#   "transactionId": 51,
#   "newBalance": 1500.00,
#   "riskFlag": "NORMAL"
# }
```

### 5. Check Fraud Detection

```bash
# Try to deposit large amount (triggers fraud check)
curl -X POST http://localhost:8080/api/transactions/deposit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": '$ACCOUNT_ID',
    "amount": 50000.00
  }'

# Response (if amount > $10K):
# {
#   "transactionId": 52,
#   "newBalance": 51500.00,
#   "riskFlag": "SUSPICIOUS"  # ✅ Fraud detection worked!
# }
```

### 6. Get Account List

```bash
curl http://localhost:8080/api/accounts \
  -H "Authorization: Bearer $TOKEN"

# Response:
# [
#   {
#     "id": 1,
#     "accountType": "CHECKING",
#     "balance": 5000.00,
#     "status": "ACTIVE"
#   },
#   ...
# ]
```

---

## 📊 View Demo Data

### Check Users
```bash
docker exec -it fintech-postgres psql -U postgres -d fintechdb \
  -c "SELECT id, email, name, kyc_status FROM users LIMIT 5;"
```

### Check Accounts
```bash
docker exec -it fintech-postgres psql -U postgres -d fintechdb \
  -c "SELECT id, account_type, balance FROM accounts LIMIT 10;"
```

### Check Transactions
```bash
docker exec -it fintech-postgres psql -U postgres -d fintechdb \
  -c "SELECT id, amount, risk_flag FROM transactions LIMIT 20;"
```

### Check Audit Logs
```bash
docker exec -it fintech-postgres psql -U postgres -d fintechdb \
  -c "SELECT action, username, timestamp FROM audit_logs LIMIT 10;"
```

---

## 🐳 Docker Commands

### View Running Services
```bash
docker-compose ps

# Output shows:
# postgres: healthy
# backend: healthy
# ai-service: healthy
```

### View Logs
```bash
# All services
docker-compose logs

# Specific service
docker-compose logs backend
docker-compose logs postgres
docker-compose logs ai-service

# Real-time logs
docker-compose logs -f backend

# Last 50 lines
docker-compose logs --tail=50 backend
```

### Restart Services
```bash
# Restart one service
docker-compose restart backend

# Restart all services
docker-compose restart

# Rebuild and restart
docker-compose up --build backend
```

### Stop Services
```bash
# Stop all services (keeps data)
docker-compose stop

# Stop and remove containers
docker-compose down

# Remove everything including volumes
docker-compose down -v  # ⚠️ Deletes database!
```

---

## 🚀 Deploy to Cloud

See `DEPLOYMENT_GUIDE.md` for:
- Railway.app (Recommended - 15 min) ⭐
- AWS Elastic Beanstalk (30 min)
- Google Cloud Run (20 min)
- Azure App Service (25 min)
- DigitalOcean (20 min)

---

## 🧪 Run Tests

### Compile Project
```bash
mvn clean compile

# Output: BUILD SUCCESS
```

### Run Unit Tests
```bash
mvn test

# Runs:
# - AccountServiceTest
# - TransactionServiceTest
# - ComplianceServiceTest
```

### Run All Tests
```bash
mvn verify

# Runs unit + integration tests
```

### View Coverage Report
```bash
mvn clean verify
open target/site/jacoco/index.html

# Shows code coverage percentage
```

---

## 📚 Documentation

### Quick Reference (Copy/Paste Commands)
See: `QUICK_REFERENCE.md`

### API Documentation
See: `README.md`

### Security & Setup
See: `SECURITY_AND_SETUP_GUIDE.md`

### Deployment & Hosting
See: `DEPLOYMENT_GUIDE.md`

### Debugging & Issues
See: `DEBUGGING_GUIDE.md`

### Complete Implementation Details
See: `IMPLEMENTATION_SUMMARY.md`

---

## 🆘 Troubleshooting

### Issue: "Port 8080 already in use"
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <pid> /F

# Mac/Linux
lsof -i :8080
kill -9 <pid>
```

### Issue: "Cannot connect to PostgreSQL"
```bash
# Check if PostgreSQL is running
docker-compose ps postgres

# Restart it
docker-compose up postgres -d

# Check logs
docker-compose logs postgres
```

### Issue: "JWT Token Invalid"
```bash
# Get new token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"Password123"}'

# Check it's in Authorization header
# Format: Authorization: Bearer <token>
```

### Issue: "Build fails"
```bash
# Clean and rebuild
mvn clean
mvn compile

# Check Java version
java -version
# Should be Java 17 or higher

# Check Maven version
mvn -version
# Should be Maven 3.9+
```

**More issues?** See: `DEBUGGING_GUIDE.md`

---

## ✅ Verification Checklist

After setup, verify everything works:

```bash
# 1. Services running
docker-compose ps
# All should show "Up" status

# 2. Health check
curl http://localhost:8080/api/health
# Should return {"status":"UP",...}

# 3. Demo users exist
curl -X POST http://localhost:8080/api/auth/login \
  -d '{"email":"john@example.com","password":"Password123"}'
# Should return JWT token

# 4. Database connected
docker exec -it fintech-postgres psql -U postgres -d fintechdb \
  -c "SELECT COUNT(*) FROM users;"
# Should show: count = 3

# 5. AI service responding
curl http://localhost:8000/health
# Should return healthy status

# 6. Fraud detection works
curl -X POST http://localhost:8080/api/transactions/deposit \
  -H "Authorization: Bearer <token>" \
  -d '{"accountId":1,"amount":50000}'
# Should have riskFlag: "SUSPICIOUS" for amounts > $10K
```

If all pass: ✅ **System is ready!**

---

## 🎯 Next Steps

### 1. Local Testing (Now)
- ✅ Start backend locally
- ✅ Test endpoints with curl/Postman
- ✅ Verify database is working
- ✅ Check demo data loaded

### 2. Frontend Development (Next)
- Build Next.js frontend
- Connect to these backend APIs
- Deploy both together

### 3. Deploy to Cloud (After Testing)
- Choose hosting: Railway.app recommended
- Follow DEPLOYMENT_GUIDE.md
- Configure domain
- Enable HTTPS

### 4. Production Checklist
- [ ] Change all default passwords
- [ ] Generate strong secrets
- [ ] Enable SSL/HTTPS
- [ ] Configure monitoring
- [ ] Set up backups
- [ ] Run security scan

---

## 📞 Quick Reference

```bash
# Start backend
docker-compose up --build

# Stop backend
docker-compose down

# View logs
docker-compose logs -f backend

# Run tests
mvn test

# Build for production
mvn clean package

# Login (get token)
curl -X POST http://localhost:8080/api/auth/login ...

# Use token (replace TOKEN)
curl -H "Authorization: Bearer TOKEN" http://localhost:8080/api/accounts
```

---

## 🎉 You're Ready!

Your Fintech MVP backend is fully functional and ready to:
- ✅ Test locally
- ✅ Deploy to production
- ✅ Connect to frontend
- ✅ Scale and optimize

**Questions?** Check the documentation:
- Setup issues: `SECURITY_AND_SETUP_GUIDE.md`
- API usage: `README.md` or `QUICK_REFERENCE.md`
- Problems: `DEBUGGING_GUIDE.md`
- Deployment: `DEPLOYMENT_GUIDE.md`

---

**Happy building! 🚀**

Version: 1.0.0 | Date: November 24, 2025
