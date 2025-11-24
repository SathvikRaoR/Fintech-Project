# Security & Setup Guide - Fintech MVP

## 🔒 Security Implementation Overview

This document outlines all security measures, encryption implementations, and configuration requirements for the Fintech MVP.

---

## 1. Environment Variables Setup

### Required Environment Variables

```bash
# JWT Configuration
FINTECH_JWT_SECRET=your-256-bit-secret-key-min-32-chars-for-HS512

# AES Encryption (Field-level PII encryption)
FINTECH_AES_KEY=your-256-bit-aes-key-exactly-32-bytes

# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/fintechdb
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# AI Service URL
AI_SERVICE_URL=http://localhost:8000

# Server Configuration
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
```

### How to Generate Secrets (PowerShell)

```powershell
# Generate 32-byte random string for JWT_SECRET (Base64)
$jwtSecret = [Convert]::ToBase64String((1..32 | ForEach-Object { [byte](Get-Random -Maximum 256) }))
Write-Host "FINTECH_JWT_SECRET=$jwtSecret"

# Generate 32-byte random string for AES_KEY (Hex)
$aesKey = ((1..32 | ForEach-Object { '{0:X2}' -f (Get-Random -Maximum 256) }) -join '')
Write-Host "FINTECH_AES_KEY=$aesKey"
```

### How to Generate Secrets (Linux/Mac)

```bash
# Generate JWT Secret (Base64)
openssl rand -base64 32

# Generate AES Key (Hex, must be exactly 32 bytes)
openssl rand -hex 32
```

### Windows - Set Environment Variables Permanently

```powershell
# Run as Administrator
[Environment]::SetEnvironmentVariable("FINTECH_JWT_SECRET", "your-secret-here", "User")
[Environment]::SetEnvironmentVariable("FINTECH_AES_KEY", "your-aes-key-here", "User")

# Restart PowerShell to apply changes
```

### Linux/Mac - Set Environment Variables

```bash
# Add to ~/.bashrc or ~/.zshrc
export FINTECH_JWT_SECRET="your-secret-here"
export FINTECH_AES_KEY="your-aes-key-here"

# Apply changes
source ~/.bashrc
```

### Docker - Use .env File

Create a `.env` file in the root directory:

```env
FINTECH_JWT_SECRET=your-256-bit-secret-key-min-32-chars
FINTECH_AES_KEY=your-256-bit-aes-key-exactly-32-bytes
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

Then run:
```bash
docker-compose --env-file .env up --build
```

---

## 2. Security Features Implemented

### 2.1 Authentication & Authorization

**JWT (JSON Web Tokens)**
- Algorithm: HS512 (HMAC-SHA512)
- Expiration: 3600 seconds (1 hour)
- Payload: User email
- Secret: 256-bit key from environment variable

```java
// Generated on login
POST /api/auth/login
Response: {
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "expiresIn": 3600
}

// Used on protected endpoints
Authorization: Bearer <token>
```

**Password Hashing**
- Algorithm: BCrypt
- Strength: 12 rounds
- Never stored in plaintext
- Compared during login

```java
// In AuthService.java
BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
String hashedPassword = passwordEncoder.encode(plainPassword);
```

### 2.2 Data Encryption (Field-Level)

**AES-256-GCM Encryption**

Sensitive fields are encrypted at the database level:
- Account numbers
- SSN (Social Security Number)
- Date of birth
- Any custom PII fields

```java
// In EncryptionUtil.java
public static String encrypt(String plaintext) throws Exception {
    SecretKey key = generateAESKey(); // 256-bit key from FINTECH_AES_KEY
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    cipher.init(Cipher.ENCRYPT_MODE, key);
    
    byte[] ciphertext = cipher.doFinal(plaintext.getBytes());
    // Returns: IV + ciphertext (base64 encoded)
}

public static String decrypt(String ciphertext) throws Exception {
    // Reverse process - extracts IV and decrypts
}
```

**Important**: IV (Initialization Vector) is randomly generated for each encryption, ensuring same plaintext produces different ciphertexts.

### 2.3 API Security

**CORS (Cross-Origin Resource Sharing)**
```yaml
# Allowed origins (configure in application.yaml)
spring.mvc.cors.allowed-origins: http://localhost:3000,http://localhost:5173
spring.mvc.cors.allowed-methods: GET,POST,PUT,DELETE,OPTIONS
spring.mvc.cors.allowed-headers: "*"
```

**HTTP Security Headers**
```yaml
# Configured in SecurityConfig.java
- X-Content-Type-Options: nosniff
- X-Frame-Options: DENY
- X-XSS-Protection: 1; mode=block
- Strict-Transport-Security: max-age=31536000
```

**CSRF Protection**
- Disabled for API endpoints (stateless auth)
- Enabled for form-based endpoints

**Rate Limiting** (via Resilience4j)
```java
@CircuitBreaker(name = "ai-service", fallbackMethod = "fallback")
public FraudCheckResponse checkFraud(FraudCheckRequest request) {
    // Circuit breaker prevents cascading failures
    // Fallback returns mock response if AI service down
}
```

### 2.4 Compliance & Audit

**Audit Logging**
- Every service method decorated with `@Auditable` is logged
- Records: action, username, timestamp, method name
- Stored in AuditLog table

```java
@Auditable(action = "USER_REGISTRATION")
public UserDto register(AuthRequest request) { ... }

// Logged as:
// {
//   "action": "USER_REGISTRATION",
//   "username": "john@example.com",
//   "timestamp": "2025-11-24T10:30:00",
//   "details": "{...}"
// }
```

**AML (Anti-Money Laundering) Compliance**
```java
// Transactions > $10,000 are flagged as SUSPICIOUS
if (transaction.getAmount() > AML_THRESHOLD) {
    transaction.setRiskFlag("SUSPICIOUS");
}

// Transaction velocity: max 5 transactions per hour
if (getTransactionCount(accountId, lastHour) > 5) {
    transaction.setRiskFlag("SUSPICIOUS");
}
```

**KYC (Know Your Customer) Status**
- PENDING: User registered, needs verification
- VERIFIED: User passed KYC checks
- BLOCKED: User flagged for compliance violation

```sql
SELECT * FROM users WHERE kyc_status = 'VERIFIED';
```

---

## 3. Database Security

### 3.1 PostgreSQL Configuration

```yaml
# Connection settings in application.yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fintechdb
    username: postgres
    password: postgres  # CHANGE IN PRODUCTION
    hikaricp:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
```

### 3.2 Production Database Security

**DO NOT** use default credentials in production:

```bash
# Change PostgreSQL password
ALTER USER postgres WITH PASSWORD 'StrongPassword123!@#';

# Create dedicated database user
CREATE USER fintech_user WITH PASSWORD 'FinTechPass123!@#';
GRANT CONNECT ON DATABASE fintechdb TO fintech_user;
GRANT USAGE ON SCHEMA public TO fintech_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO fintech_user;
```

### 3.3 Connection Pooling

```java
// HikariCP in pom.xml (automatically included with Spring Boot Data JPA)
spring:
  datasource:
    hikari:
      maximum-pool-size: 20      # Max connections
      minimum-idle: 5             # Min idle connections
      connection-timeout: 30000   # 30 seconds
      idle-timeout: 600000        # 10 minutes
      max-lifetime: 1800000       # 30 minutes
```

### 3.4 SQL Injection Prevention

All queries use **JPA parameterized queries** - SQL injection is prevented:

```java
// ✅ SAFE - JPA handles parameterization
@Query("SELECT * FROM users WHERE email = ?1")
User findByEmail(String email);

// ✅ SAFE - JpaRepository method
User findByEmail(String email);
```

---

## 4. API Key & Hosting Setup

### 4.1 API Keys (Not Required for MVP)

The current implementation does **not use API keys**. Instead:
- **Authentication**: JWT tokens (stateless)
- **Authorization**: Role-based (USER role)

If you need API keys in the future:

```java
// Add to User entity
@Column(unique = true)
private String apiKey;

// Generate on first login
public String generateApiKey() {
    return UUID.randomUUID().toString();
}

// Validate on protected endpoints
Authorization: X-API-Key: <uuid>
```

### 4.2 Third-Party Service Keys

For future integrations:

```yaml
# application.yaml
integrations:
  stripe:
    api-key: ${STRIPE_API_KEY}
    secret-key: ${STRIPE_SECRET_KEY}
  
  twilio:
    account-sid: ${TWILIO_ACCOUNT_SID}
    auth-token: ${TWILIO_AUTH_TOKEN}
```

### 4.3 Hosting Configuration

#### Local Development
```bash
# Run with embedded PostgreSQL (Docker)
docker-compose up --build

# Or with system PostgreSQL
mvn spring-boot:run
```

#### Docker Hosting (Recommended for MVP)
```bash
# Build and push to Docker Hub
docker build -t your-docker-id/fintech-backend:latest .
docker push your-docker-id/fintech-backend:latest

# Pull and run
docker pull your-docker-id/fintech-backend:latest
docker run -p 8080:8080 \
  -e FINTECH_JWT_SECRET=$JWT_SECRET \
  -e FINTECH_AES_KEY=$AES_KEY \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/fintechdb \
  your-docker-id/fintech-backend:latest
```

#### AWS Hosting (EC2 + RDS)
```yaml
# In docker-compose for AWS
environment:
  SPRING_DATASOURCE_URL: jdbc:postgresql://your-rds-endpoint:5432/fintechdb
  SPRING_DATASOURCE_USERNAME: fintech_user
  SPRING_DATASOURCE_PASSWORD: ${RDS_PASSWORD}
  FINTECH_JWT_SECRET: ${JWT_SECRET}  # Use AWS Secrets Manager
  FINTECH_AES_KEY: ${AES_KEY}        # Use AWS Secrets Manager
```

#### Railway.app (Easiest for MVP)
```bash
# 1. Push code to GitHub
git push origin main

# 2. Create Railway account (railway.app)

# 3. Connect GitHub repo

# 4. Set environment variables in Railway dashboard
FINTECH_JWT_SECRET=...
FINTECH_AES_KEY=...

# 5. Railway auto-deploys on push
```

#### Render.com (Free Alternative)
```bash
# 1. Push code to GitHub

# 2. Create Render account (render.com)

# 3. Create New > Web Service > Connect GitHub

# 4. Set build command: mvn clean install
# 5. Set start command: java -jar target/backend-0.0.1-SNAPSHOT.jar

# 6. Add environment variables in Render dashboard
```

#### Azure Container Instances
```bash
# 1. Build Docker image
docker build -t fintech-backend:latest .

# 2. Push to Azure Container Registry
az acr build --registry MyRegistry --image fintech-backend:latest .

# 3. Deploy
az container create \
  --resource-group MyResourceGroup \
  --name fintech-backend \
  --image MyRegistry.azurecr.io/fintech-backend:latest \
  --environment-variables \
    FINTECH_JWT_SECRET=$JWT_SECRET \
    FINTECH_AES_KEY=$AES_KEY
```

---

## 5. SSL/TLS Certificate Setup

### For Production Hosting

#### Let's Encrypt (Free)
```bash
# Using Certbot
sudo apt-get install certbot
sudo certbot certonly --standalone -d your-domain.com

# Certificates stored in /etc/letsencrypt/live/your-domain.com/
```

#### Configure Spring Boot for HTTPS
```yaml
# application.yaml
server:
  ssl:
    key-store: /etc/letsencrypt/live/your-domain.com/keystore.jks
    key-store-password: ${SSL_PASSWORD}
    key-store-type: JKS
  port: 8443
```

#### Docker with HTTPS
```dockerfile
# Copy certificates into image
COPY /etc/letsencrypt/live/your-domain.com/ /app/certs/

ENV JAVA_OPTS="-Dserver.ssl.key-store=/app/certs/keystore.jks"
```

---

## 6. Secrets Management

### Local Development (Use .env file)

```env
# .env (never commit to git)
FINTECH_JWT_SECRET=abc123...
FINTECH_AES_KEY=def456...
SPRING_DATASOURCE_PASSWORD=postgres
```

### Production Secrets Management

#### Option 1: AWS Secrets Manager (Recommended)
```java
// In application.yaml
spring:
  datasource:
    password: ${db-password}  # Fetched from AWS Secrets Manager
```

#### Option 2: HashiCorp Vault
```bash
# Store secrets in Vault
vault kv put secret/fintech \
  jwt_secret=abc123 \
  aes_key=def456

# Application fetches on startup
```

#### Option 3: Kubernetes Secrets (For K8s deployment)
```bash
kubectl create secret generic fintech-secrets \
  --from-literal=JWT_SECRET=abc123 \
  --from-literal=AES_KEY=def456
```

#### Option 4: CI/CD Pipeline Secrets (GitHub Actions)
```yaml
# .github/workflows/deploy.yml
env:
  FINTECH_JWT_SECRET: ${{ secrets.FINTECH_JWT_SECRET }}
  FINTECH_AES_KEY: ${{ secrets.FINTECH_AES_KEY }}
```

---

## 7. Security Checklist

### Before Going to Production ✅

- [ ] Change all default passwords
- [ ] Generate strong JWT and AES keys
- [ ] Enable HTTPS/SSL certificates
- [ ] Configure CORS for your frontend domain only
- [ ] Set up secrets management (AWS/Vault/K8s)
- [ ] Enable database backups
- [ ] Configure database user with limited privileges
- [ ] Set up audit logging and monitoring
- [ ] Enable WAF (Web Application Firewall) on API Gateway
- [ ] Run security scan: `mvn dependency-check:check`
- [ ] Configure firewall rules (allow only ports 443, 80)
- [ ] Set up DDoS protection
- [ ] Enable request rate limiting
- [ ] Configure HSTS header (enforce HTTPS)
- [ ] Set up security monitoring and alerts
- [ ] Regular security updates for dependencies

### Dependency Vulnerability Scanning

```bash
# Check for known vulnerabilities
mvn dependency-check:check

# Or using OWASP tool
mvn org.owasp:dependency-check-maven:check
```

### Regular Security Updates

```bash
# Check for dependency updates
mvn versions:display-dependency-updates

# Update to latest patch versions
mvn versions:update-properties
```

---

## 8. Testing Security Features

### Test JWT Token

```bash
# 1. Register user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "SecurePass123!",
    "name": "Test User"
  }'

# 2. Login and get token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "SecurePass123!"
  }'

# Response includes JWT token
# Copy the token from response

# 3. Use token to access protected endpoint
curl http://localhost:8080/api/accounts \
  -H "Authorization: Bearer <your-jwt-token>"
```

### Test Encryption

```bash
# Verify encrypted fields in database
psql -U postgres -d fintechdb -c "SELECT id, email, account_number FROM accounts LIMIT 1;"

# account_number should show encrypted value (e.g., "AES:abc123...")
```

### Test AML Compliance

```bash
# Try to deposit amount > $10,000
curl -X POST http://localhost:8080/api/transactions/deposit \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": 1,
    "amount": 15000.00
  }'

# Response should include riskFlag: "SUSPICIOUS"
```

---

## 9. Monitoring & Logging

### Audit Log Query

```sql
-- View all user registrations
SELECT * FROM audit_logs WHERE action = 'USER_REGISTRATION' ORDER BY timestamp DESC;

-- View all transactions by user
SELECT * FROM audit_logs WHERE action = 'TRANSACTION_DEPOSIT' AND username = 'john@example.com';

-- View suspicious activities
SELECT * FROM transactions WHERE risk_flag = 'SUSPICIOUS' ORDER BY timestamp DESC;
```

### Application Logging

```yaml
# application.yaml
logging:
  level:
    root: INFO
    com.fintech.backend: DEBUG
    org.springframework.security: DEBUG
  file:
    name: logs/fintech.log
    max-size: 10MB
    max-history: 30
```

### Metrics & Health

```bash
# Health check
curl http://localhost:8080/api/health

# JVM metrics (if actuator enabled)
curl http://localhost:8080/actuator/metrics
```

---

## 10. Troubleshooting Security Issues

### JWT Token Expired

```
Error: 401 Unauthorized - Token expired

Solution: Login again to get a new token
curl -X POST http://localhost:8080/api/auth/login ...
```

### AES Encryption Key Mismatch

```
Error: javax.crypto.BadPaddingException

Solution: Verify FINTECH_AES_KEY environment variable is set correctly
echo $FINTECH_AES_KEY
# Should output 64 hex characters (32 bytes)
```

### Database Connection Refused

```
Error: Connection refused to PostgreSQL

Solution 1: Start PostgreSQL
docker-compose up postgres

Solution 2: Check credentials in application.yaml
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/fintechdb
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

### CORS Error in Frontend

```
Error: Access-Control-Allow-Origin missing

Solution: Update application.yaml CORS settings
spring.mvc.cors.allowed-origins: http://localhost:3000

Then restart backend
```

---

## 11. Quick Setup Checklist

```bash
# 1. Generate secrets
openssl rand -base64 32  # For JWT_SECRET
openssl rand -hex 32     # For AES_KEY

# 2. Create .env file
cat > .env << EOF
FINTECH_JWT_SECRET=<generated-secret>
FINTECH_AES_KEY=<generated-key>
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
EOF

# 3. Start services
docker-compose --env-file .env up --build

# 4. Test endpoints
curl http://localhost:8080/api/health

# 5. View logs
docker-compose logs -f backend
```

---

## 12. Production Checklist Summary

| Item | Status | Notes |
|------|--------|-------|
| JWT Secret Generated | 🔲 | Min 32 chars, use openssl rand |
| AES Key Generated | 🔲 | Exactly 32 bytes hex |
| Database Credentials Changed | 🔲 | Use strong passwords |
| CORS Domain Configured | 🔲 | Set to your frontend domain |
| SSL Certificate | 🔲 | Use Let's Encrypt for free |
| Secrets Management Setup | 🔲 | AWS Secrets / Vault / K8s |
| Backup Strategy | 🔲 | Daily automated backups |
| Monitoring Setup | 🔲 | Error tracking, metrics |
| Security Scan | 🔲 | mvn dependency-check:check |
| Load Testing | 🔲 | JMeter or k6 |
| Incident Response | 🔲 | Escalation procedures |
| Documentation | 🔲 | API docs, security policies |

---

## Questions? 

For more information, see:
- `/README.md` - API documentation
- `/IMPLEMENTATION_SUMMARY.md` - Component overview
- `/QUICK_REFERENCE.md` - Common commands
- `src/main/resources/application.yaml` - Configuration

---

**Version**: 1.0.0  
**Date**: November 24, 2025  
**Maintained by**: Development Team
