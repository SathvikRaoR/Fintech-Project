# Quick Reference Guide - Fintech MVP

## 🚀 Start Services

```bash
# Full stack with Docker Compose
docker-compose up --build

# Individual services
docker run -d --name postgres -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 -v postgres_data:/var/lib/postgresql/data \
  postgres:15

cd ai-service
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000

mvn spring-boot:run
```

## 🔑 Authentication

### Register New User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "SecurePass123",
    "name": "New User"
  }'
```

### Login & Get Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "Password123"
  }'

# Save token to variable
TOKEN="your-jwt-token-here"
```

## 💳 Account Operations

### Get All Accounts
```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/accounts
```

### Create New Account
```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"accountType":"SAVINGS","initialDeposit":5000}' \
  http://localhost:8080/api/accounts
```

### Get Specific Account
```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/accounts/1
```

## 💰 Transactions

### Deposit Money
```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"accountId":1,"amount":500}' \
  http://localhost:8080/api/transactions/deposit
```

### Withdraw Money
```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"accountId":1,"amount":100}' \
  http://localhost:8080/api/transactions/withdraw
```

### Transfer Between Accounts
```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"accountId":1,"toAccountId":2,"amount":200}' \
  http://localhost:8080/api/transactions/transfer
```

### Get Transaction History
```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/transactions/history/1
```

## 🤖 AI Services

### Fraud Detection
```bash
# Normal transaction
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId":1,
    "amount":1000,
    "timestamp":"2025-11-24T10:00:00"
  }' \
  http://localhost:8080/api/ai/fraud-check

# Suspicious transaction (>50K)
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId":1,
    "amount":75000,
    "timestamp":"2025-11-24T10:00:00"
  }' \
  http://localhost:8080/api/ai/fraud-check
```

### Credit Score
```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"features":{}}' \
  http://localhost:8080/api/ai/credit-score
```

### FinBot Chat
```bash
# Saving advice
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId":1,
    "message":"How can I save 20% of my income?"
  }' \
  http://localhost:8080/api/ai/chat

# Investment advice
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId":1,
    "message":"What are good investment options?"
  }' \
  http://localhost:8080/api/ai/chat

# Credit advice
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId":1,
    "message":"How do I improve my credit score?"
  }' \
  http://localhost:8080/api/ai/chat
```

## 🏥 Health Checks

```bash
# Backend health
curl http://localhost:8080/api/health

# Spring Boot actuator (detailed)
curl http://localhost:8080/actuator/health

# AI Service health
curl http://localhost:8000/health

# Database connection test
psql -h localhost -U postgres -d fintechdb -c "SELECT version();"
```

## 🧪 Testing

### Unit Tests
```bash
# All tests
mvn test

# Specific test class
mvn -Dtest=AccountServiceTest test

# With coverage
mvn clean test jacoco:report

# View coverage report
# Open: target/site/jacoco/index.html
```

### Postman Collection
```bash
# Install Newman
npm install -g newman

# Run collection
newman run tests/postman/fintech.postman_collection.json \
  --environment tests/postman/fintech.postman_env.json \
  --reporters cli,json,html

# Results in: newman/ directory
```

### Build & Package
```bash
# Clean build
mvn clean package

# Build without tests
mvn clean package -DskipTests

# Build with logging
mvn clean package -X

# Check jar
ls -lh target/*.jar
```

## 🐳 Docker Commands

### Compose Operations
```bash
# Start all services
docker-compose up --build

# Start in background
docker-compose up -d --build

# View logs
docker-compose logs -f

# Logs specific service
docker-compose logs -f backend
docker-compose logs -f ai-service
docker-compose logs -f postgres

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Rebuild specific service
docker-compose up --build backend

# Execute command in container
docker-compose exec backend mvn test
```

### Individual Docker Commands
```bash
# List running containers
docker ps

# List all containers
docker ps -a

# View container logs
docker logs -f container-name

# Stop container
docker stop container-name

# Remove container
docker rm container-name

# Build image
docker build -t fintech-backend:latest .

# Run container
docker run -d --name backend \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/fintechdb \
  -p 8080:8080 \
  fintech-backend:latest

# Remove image
docker rmi fintech-backend:latest

# Clean up (dangling images/containers)
docker system prune -a
```

## 📊 Database

### Connect to PostgreSQL
```bash
# Via psql
psql -h localhost -U postgres -d fintechdb

# List tables
\dt

# Describe table
\d users

# Query
SELECT * FROM users;
SELECT * FROM audit_logs;
```

### Database Operations
```bash
# Backup
pg_dump -h localhost -U postgres fintechdb > backup.sql

# Restore
psql -h localhost -U postgres fintechdb < backup.sql

# Drop and recreate
DROP DATABASE fintechdb;
CREATE DATABASE fintechdb;
```

## 🔍 Debugging

### Logs Location
```bash
# Backend logs (from Docker)
docker-compose logs -f backend

# AI service logs
docker-compose logs -f ai-service

# PostgreSQL logs
docker-compose logs -f postgres
```

### Common Issues

#### Port Already in Use
```bash
# Find process using port
lsof -i :8080
lsof -i :8000
lsof -i :5432

# Kill process
kill -9 <PID>
```

#### Database Connection Error
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Test connection
nc -zv localhost 5432

# Check environment variables
echo $FINTECH_JWT_SECRET
echo $FINTECH_AES_KEY
```

#### JWT Token Issues
```bash
# Token expired? Generate new one
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"Password123"}'

# Decode JWT (using jwt.io or similar)
# Copy token and paste in payload section
```

## 📈 Performance

### JMeter Load Test
```bash
# Run test plan
jmeter -n -t tests/jmeter/fintech.jmx \
  -l results.jtl \
  -j jmeter.log

# Generate report
jmeter -g results.jtl -o html-report
```

### Database Queries
```bash
# List slow queries
SELECT query, mean_exec_time FROM pg_stat_statements 
  ORDER BY mean_exec_time DESC LIMIT 10;

# Check table sizes
SELECT schemaname, tablename, 
  pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
FROM pg_tables 
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```

## 🔄 Git Workflow

### Feature Branch
```bash
# Create feature branch
git checkout -b feature/accounts

# Make changes
git add .

# Commit
git commit -m "feat(accounts): implement account management"

# Push
git push -u origin feature/accounts

# Create Pull Request on GitHub
# Merge to dev after review
```

### Merge Strategy
```bash
# Update local branches
git fetch origin

# Switch to dev
git checkout dev

# Merge feature
git merge feature/accounts

# Push to remote
git push origin dev

# Delete feature branch
git branch -d feature/accounts
git push origin --delete feature/accounts
```

## 🌐 URLs

| Service | URL | Purpose |
|---------|-----|---------|
| Backend | http://localhost:8080 | REST API |
| API Health | http://localhost:8080/api/health | Health check |
| Actuator | http://localhost:8080/actuator | Spring metrics |
| AI Service | http://localhost:8000 | FastAPI app |
| AI Health | http://localhost:8000/health | AI health check |
| PostgreSQL | localhost:5432 | Database |
| Swagger UI | http://localhost:8080/swagger-ui.html | API docs (if enabled) |

## 🎓 Demo Credentials

```
Email: john@example.com
Password: Password123

Email: jane@example.com
Password: SecurePass456

Email: bob@example.com
Password: MyPassword789
```

## ⚙️ Environment Variables

```bash
# Required
FINTECH_JWT_SECRET=your-secret-key-min-32-chars
FINTECH_AES_KEY=your-aes-key-16-chars

# Optional (defaults provided)
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/fintechdb
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
AI_SERVICE_URL=http://localhost:8000
```

## 📚 Documentation Files

- `README.md` - Comprehensive setup and usage guide
- `IMPLEMENTATION_SUMMARY.md` - What was implemented
- `.github/workflows/ci.yml` - GitHub Actions pipeline
- `tests/postman/fintech.postman_collection.json` - API tests
- `tests/e2e/features/*.feature` - Cucumber scenarios

---

**Last Updated**: November 24, 2025
**Version**: 1.0.0
