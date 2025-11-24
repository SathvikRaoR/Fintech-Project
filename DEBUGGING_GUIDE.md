# Debugging & Troubleshooting Guide

Complete guide to debug and fix issues in the Fintech MVP.

---

## ⚙️ Backend Troubleshooting

### 1. Maven Compilation Errors

#### Error: "cannot find symbol: method parserBuilder()"

```
Error: /src/main/java/com/fintech/backend/util/JwtUtil.java:[33,20] 
cannot find symbol symbol: method parserBuilder()
```

**Cause**: JJWT 0.12.3 uses new API  
**Fix**: Use `Jwts.parser()` instead of `Jwts.parserBuilder()`

```java
// ❌ OLD (JJWT 0.11.x)
Jwts.parserBuilder().setSigningKey(...).build().parseClaimsJws(token)

// ✅ NEW (JJWT 0.12.3)
Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
```

#### Error: "cannot find symbol: class UserRepository"

```
Error: /src/main/java/com/fintech/backend/service/AuthService.java:[25,20]
cannot find symbol symbol: class UserRepository
```

**Cause**: Repository interface not created  
**Fix**: Create `src/main/java/com/fintech/backend/repository/UserRepository.java`

```java
package com.fintech.backend.repository;

import com.fintech.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
```

#### Error: "Cannot resolve class User"

**Cause**: Entity class not found  
**Fix**: Verify entity exists in `src/main/java/com/fintech/backend/model/User.java`

```java
// Minimal User entity
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
}
```

### 2. Runtime Errors

#### Error: "Column 'kyc_status' not found"

```
ERROR: column "kyc_status" of relation "users" does not exist
```

**Cause**: Database table hasn't been created or updated  
**Fix**: 
1. Delete database: `DROP DATABASE fintechdb;`
2. Restart backend: Hibernate will recreate tables with `ddl-auto: update`

```bash
# Or manually
psql -U postgres -c "DROP DATABASE fintechdb;"
psql -U postgres -c "CREATE DATABASE fintechdb;"
```

#### Error: "Connection refused - PostgreSQL"

```
FATAL: Connection refused
Location: 127.0.0.1:5432
```

**Cause**: PostgreSQL not running  
**Fix**:

```bash
# Check if PostgreSQL is running
docker-compose ps

# Start PostgreSQL
docker-compose up postgres -d

# Or start all services
docker-compose up --build
```

#### Error: "java.lang.IllegalArgumentException: JWT claims string is empty"

**Cause**: JWT token is missing or empty  
**Fix**:

```bash
# 1. Verify token is being sent
curl -v http://localhost:8080/api/accounts \
  -H "Authorization: Bearer YOUR-TOKEN-HERE"

# 2. Get new token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"Password123"}'

# 3. Use token from response
```

#### Error: "Cannot decrypt string - AES key not set"

```
javax.crypto.BadPaddingException: Given final block not properly padded
```

**Cause**: FINTECH_AES_KEY not set or incorrect  
**Fix**:

```bash
# Verify environment variable is set
echo $FINTECH_AES_KEY

# Set it (PowerShell)
$env:FINTECH_AES_KEY = "your-256-bit-hex-key"

# Or set it (Linux/Mac)
export FINTECH_AES_KEY="your-256-bit-hex-key"

# Verify (should print 64 hex characters = 32 bytes)
echo $FINTECH_AES_KEY | wc -c
# Should output: 65 (64 chars + newline)
```

### 3. Database Issues

#### No Demo Data Loaded

**Cause**: DemoDataLoader not executed or database error  
**Fix**:

```bash
# 1. Check if CommandLineRunner ran
docker-compose logs backend | grep "Loading demo data"

# 2. Manually load data
psql -U postgres -d fintechdb << EOF
INSERT INTO users (email, password, name, kyc_status) 
VALUES ('john@example.com', 'hash...', 'John', 'VERIFIED');
EOF

# 3. Verify data exists
psql -U postgres -d fintechdb -c "SELECT COUNT(*) FROM users;"
```

#### Database Connection Pool Exhausted

```
Cannot get a connection, pool error Timeout waiting for idle object
```

**Cause**: Too many connections, not released  
**Fix**:

```yaml
# application.yaml - increase pool size
spring:
  datasource:
    hikari:
      maximum-pool-size: 30  # Increase from 10
      minimum-idle: 5
```

Then restart backend.

### 4. Service Integration Issues

#### AI Service Endpoint Not Responding

```
Error: Connection refused to http://localhost:8000
```

**Cause**: AI service not running  
**Fix**:

```bash
# Start AI service
docker-compose up ai-service -d

# Verify it's running
curl http://localhost:8000/health

# Check logs
docker-compose logs ai-service
```

#### Fraud Check Always Returns Mock Response

```
Response: {
  "score": 0.5,
  "decision": "NORMAL",
  "explanation": "Circuit breaker fallback - AI service unavailable"
}
```

**Cause**: AI service unreachable, circuit breaker activated  
**Fix**:

```bash
# 1. Verify AI service is up
curl http://localhost:8000/health

# 2. Check network connectivity
docker exec backend ping ai-service

# 3. Check Docker network
docker network ls
docker network inspect fintech-net

# 4. Restart both services
docker-compose restart backend ai-service
```

---

## 🔑 Security & Authentication Issues

### JWT Token Problems

#### "Invalid signature" Error

```
JwtException: Invalid signature for compact JWT [...]
```

**Cause**: JWT secret doesn't match  
**Fix**:

```bash
# 1. Check JWT secret is same everywhere
echo $FINTECH_JWT_SECRET | wc -c
# Should be same in:
# - Backend environment variable
# - Docker compose
# - GitHub Secrets (if deployed)

# 2. Generate new secret if uncertain
openssl rand -base64 32

# 3. Update everywhere and restart
export FINTECH_JWT_SECRET="<new-secret>"
docker-compose restart backend
```

#### Token Works in Postman but Not in Frontend

**Cause**: CORS issue or token format  
**Fix**:

```javascript
// ❌ WRONG - missing Bearer prefix
headers: {
  "Authorization": "token-here"
}

// ✅ CORRECT
headers: {
  "Authorization": "Bearer token-here"
}

// Also check CORS
fetch('http://localhost:8080/api/accounts', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'application/json'
  },
  credentials: 'include'
})
```

### Password & Encryption Issues

#### "Bad credentials" on Login

```
Error: 401 Unauthorized - Bad credentials
```

**Cause**: Password doesn't match (or user doesn't exist)  
**Fix**:

```bash
# 1. Check user exists
psql -U postgres -d fintechdb -c "SELECT email FROM users;"

# 2. Verify password (remember demo users)
# Default users:
# Email: john@example.com, Password: Password123
# Email: jane@example.com, Password: Password123

# 3. Reset user password (in database)
# First, get BCrypt hash of new password
# Then update: UPDATE users SET password = '<new-hash>' WHERE email = 'john@example.com';

# 4. Or register new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "newuser@example.com",
    "password": "Password123",
    "name": "New User"
  }'
```

#### Encrypted Fields Unreadable in Database

```
SELECT account_number FROM accounts;
# Shows: "AES:abc123def456..."
```

This is **expected behavior**! Encryption is working correctly. To view decrypted data:

```java
// Only in application code (has AES key)
String decrypted = EncryptionUtil.decrypt(encryptedValue);
System.out.println(decrypted); // Now readable
```

---

## 🌐 API & REST Issues

### 404 Not Found Errors

```
GET /api/accounts → 404 Not Found
```

**Cause**: Endpoint path is wrong  
**Fix**:

```bash
# Check actual endpoints (from controller)
# Correct:
GET /api/accounts        # ✅
GET /api/accounts/{id}   # ✅
POST /api/accounts       # ✅

# Wrong:
GET /accounts            # ❌ Missing /api prefix
GET /api/account         # ❌ Wrong path
GET /api/accounts/123    # ❌ Use path variable, not query param
```

### 400 Bad Request

```json
{
  "error": "Invalid request",
  "message": "email cannot be blank"
}
```

**Cause**: Validation error (empty required field)  
**Fix**:

```bash
# ❌ WRONG
curl -X POST http://localhost:8080/api/auth/register \
  -d '{"password":"test","name":"John"}'

# ✅ CORRECT
curl -X POST http://localhost:8080/api/auth/register \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123",
    "name": "John Doe"
  }'
```

### 409 Conflict (Duplicate Email)

```
POST /api/auth/register → 409 Conflict
Error: Email already registered
```

**Cause**: Email already exists  
**Fix**:

```bash
# Use different email
curl -X POST http://localhost:8080/api/auth/register \
  -d '{"email":"newemail@example.com","password":"Pass123","name":"User"}'

# Or delete existing user
psql -U postgres -d fintechdb \
  -c "DELETE FROM users WHERE email='olduser@example.com';"
```

### 500 Internal Server Error

```
GET /api/accounts → 500 Internal Server Error
```

**Cause**: Unhandled exception  
**Fix**:

```bash
# 1. Check backend logs
docker-compose logs backend | tail -50

# 2. Common causes:
# - Database query failed
# - NullPointerException in service
# - Wrong entity mapping

# 3. View full stack trace
docker-compose logs backend | grep -A 20 "Exception"

# 4. Fix the issue in code
# Then rebuild and restart
docker-compose up --build backend
```

---

## 🧪 Testing Issues

### Unit Tests Failing

#### "No tests found in [TestClass]"

```
No tests found in com.fintech.backend.service.AccountServiceTest
```

**Cause**: Test methods not annotated with `@Test`  
**Fix**:

```java
// ❌ WRONG - missing @Test
public void testAccountCreation() {
    // test code
}

// ✅ CORRECT
@Test
public void testAccountCreation() {
    // test code
}
```

#### "MockitoException: Cannot instantiate class"

```
Could not create MockitoAnnotations for @Mock fields
```

**Cause**: Missing `@ExtendWith(MockitoExtension.class)` or wrong JUnit version  
**Fix**:

```java
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)  // ✅ Add this
class AccountServiceTest {
    @Mock
    private AccountRepository repository;
}
```

### Integration Tests Timeout

```
Tests timeout after 300 seconds
```

**Cause**: Testcontainers waiting for database  
**Fix**:

```bash
# 1. Ensure Docker is running
docker ps

# 2. Check system resources (RAM, disk space)
df -h
docker stats

# 3. Increase timeout
@Test
@Timeout(value = 2, unit = TimeUnit.MINUTES)
public void testWithDatabase() {
    // test code
}
```

### Postman Tests Failing

#### "Cannot read properties of undefined"

```javascript
// ❌ WRONG - variable not set
pm.variables.get("token")  // undefined

// ✅ CORRECT
// First request should save token from login response
pm.environment.set("token", pm.response.json().token);

// Then use in subsequent requests
pm.request.headers.add({
  "key": "Authorization",
  "value": "Bearer {{token}}"
});
```

---

## 🐳 Docker Issues

### "Container exited with code 1"

```
backend exited with code 1
```

**Cause**: Application failed to start  
**Fix**:

```bash
# 1. Check logs
docker-compose logs backend

# 2. Common causes:
# - Environment variables not set
# - Port already in use
# - Database not running

# 3. Check port
netstat -ano | findstr :8080  # Windows
lsof -i :8080                 # Mac/Linux

# 4. Kill process using port
netstat -ano | findstr :8080 | findstr LISTENING | awk '{print $5}' | xargs kill  # Windows
lsof -ti:8080 | xargs kill -9  # Mac/Linux

# 5. Restart
docker-compose up --build backend
```

### "Network fintech-net not found"

```
ERROR: Network fintech-net not found
```

**Cause**: Docker network not created  
**Fix**:

```bash
# Create network
docker network create fintech-net

# Or let docker-compose recreate it
docker-compose down
docker-compose up --build
```

### Persistent Data Lost After Restart

**Cause**: No volume mounted for PostgreSQL  
**Fix**:

```bash
# Check volume exists in docker-compose.yml
services:
  postgres:
    volumes:
      - postgres_data:/var/lib/postgresql/data  # ✅ Add this

volumes:
  postgres_data:  # ✅ Define volume
```

Then restart:
```bash
docker-compose down
docker-compose up --build
```

---

## 📊 Performance Issues

### Slow Database Queries

```
Transaction took 5000ms to complete (too slow!)
```

**Cause**: Missing index or inefficient query  
**Fix**:

```sql
-- Add index to frequently queried columns
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);

-- Check query execution
EXPLAIN ANALYZE SELECT * FROM transactions WHERE account_id = 1;
```

### High Memory Usage

```
Backend using 80%+ of available memory
```

**Cause**: Connection pool too large or memory leak  
**Fix**:

```yaml
# Reduce connection pool in application.yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10    # Reduce from 20
      minimum-idle: 3          # Reduce from 5

# Or increase Docker memory
services:
  backend:
    mem_limit: 2g  # Increase from 1g
```

### API Response Time > 2 seconds

**Causes & Fixes**:

```bash
# 1. Database queries slow
# → Add indexes (see above)

# 2. Network latency
# → Use local environment or CDN

# 3. Service overload
# → Scale horizontally (more instances)

# 4. Transaction processing slow
# → Check AI service isn't blocking

# 5. Missing caching
# → Add Redis cache for frequently accessed data
```

---

## 📋 Debugging Checklist

```bash
# When something goes wrong, follow this:

# 1. Check if services are running
docker-compose ps

# 2. Check logs
docker-compose logs backend
docker-compose logs postgres
docker-compose logs ai-service

# 3. Test connectivity
curl http://localhost:8080/api/health
curl http://localhost:8000/health
curl http://localhost:5432  # Database

# 4. Test authentication
curl -X POST http://localhost:8080/api/auth/login \
  -d '{"email":"john@example.com","password":"Password123"}'

# 5. Verify environment variables
echo $FINTECH_JWT_SECRET
echo $FINTECH_AES_KEY

# 6. Check database
psql -U postgres -d fintechdb -c "SELECT COUNT(*) FROM users;"

# 7. Search error messages
grep -r "ERROR" docker logs

# 8. Check application logs
cat target/fintech.log | tail -100

# 9. Restart everything if stuck
docker-compose down -v
docker-compose up --build
```

---

## 🔗 Quick Reference

| Issue | Command | Solution |
|-------|---------|----------|
| Services won't start | `docker-compose logs` | Fix environment variables |
| Database won't connect | `psql -U postgres` | Start PostgreSQL |
| JWT not working | `echo $FINTECH_JWT_SECRET` | Set environment variable |
| Port in use | `lsof -i :8080` | Kill process or change port |
| Memory/CPU high | `docker stats` | Restart or increase resources |
| Tests timing out | `docker ps` | Ensure Docker running |

---

## 🆘 Still Stuck?

1. **Check logs**: `docker-compose logs -f`
2. **Google the error**: Most errors have solutions online
3. **Check GitHub issues**: Similar problems might be documented
4. **Ask in forums**: Stack Overflow, Reddit r/java, Spring forums
5. **Review code**: Read the relevant service/controller class

---

**Last Updated**: November 24, 2025
**Version**: 1.0.0
