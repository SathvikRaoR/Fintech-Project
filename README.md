# Fintech <img width="1536" height="1024" alt="ChatGPT Image Nov 24, 2025, 11_32_49 AM" src="https://github.com/user-attachments/assets/60de4a8a-ac51-4882-80c8-ece88d2137d4" />
MVP: AI-Powered Digital Banking & Investment Hub

A production-ready, open-source fintech platform demonstrating advanced features like AI-driven fraud detection, credit scoring, and intelligent financial advice through FinBot.

## 🎯 Overview

This project showcases a complete microservices architecture for digital banking with:
- **Smart Fraud Detection**: Real-time anomaly detection using Isolation Forest
- **AI Credit Scoring**: Predictive credit risk assessment  
- **FinBot**: Conversational AI for personalized financial advice
- **AML Compliance**: Automated Anti-Money Laundering rules
- **Audit Logging**: Complete transaction and action tracking
- **JWT Security**: Token-based authentication with BCrypt password hashing

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+
- Python 3.11+
- Docker & Docker Compose

### Environment Setup

```bash
# Clone and setup
git clone https://github.com/SathvikRaoR/Fintech-Project.git
cd Fintech-Project

# Set environment variables
export FINTECH_JWT_SECRET="your-secret-key-min-32-chars"
export FINTECH_AES_KEY="your-aes-key-16-chars"
```

### Start All Services

```bash
# Build and start with Docker Compose
docker-compose up --build

# Verify services
curl http://localhost:8080/api/health  # Backend
curl http://localhost:8000/health      # AI Service
```

### Demo Data

Automatically loaded at startup:
- **3 Users**: john@example.com, jane@example.com, bob@example.com
- **4 Accounts**: Mix of account types
- **50 Transactions**: With some flagged as suspicious

## 📋 API Endpoints

### Authentication
```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"Password123"}'
  
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"SecurePass123","name":"User"}'
```

### Accounts
```bash
# Get accounts
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/accounts

# Create account
curl -X POST -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"accountType":"SAVINGS","initialDeposit":5000}' \
  http://localhost:8080/api/accounts
```

### Transactions
```bash
# Deposit
curl -X POST -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"accountId":1,"amount":500}' \
  http://localhost:8080/api/transactions/deposit

# Fraud check
curl -X POST -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"accountId":1,"amount":75000,"timestamp":"2025-11-24T10:00:00"}' \
  http://localhost:8080/api/ai/fraud-check

# FinBot chat
curl -X POST -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"message":"How can I save 20% of my income?"}' \
  http://localhost:8080/api/ai/chat
```

## 🧪 Testing

```bash
# Unit tests
mvn test

# Coverage report
mvn jacoco:report

# API tests with Newman
npm install -g newman
newman run tests/postman/fintech.postman_collection.json
```

## 🔐 Security Features

- **JWT Authentication**: 1-hour token expiration
- **BCrypt Passwords**: Secure hashing with salts
- **AES Encryption**: Field-level encryption for sensitive data
- **AML Rules**: Auto-flag transactions > $50,000
- **Audit Logging**: AspectJ-based method interception
- **KYC Tracking**: PENDING/VERIFIED status management

## 📊 Architecture

- **Backend**: Spring Boot 3.x + PostgreSQL (Port 8080)
- **AI Service**: FastAPI + scikit-learn (Port 8000)
- **Database**: PostgreSQL 15 (Port 5432)
- **Frontend**: Next.js/React (Port 3000) - To be implemented

## 📚 Documentation

Full documentation available in:
- `docs/architecture.md` - System design
- `docs/api-spec.md` - API specification
- `docs/security.md` - Security implementation
- `tests/postman/fintech.postman_collection.json` - Postman API collection

## 🧩 Project Structure

```
fintech-project/
├── src/main/java/com/fintech/backend/
│   ├── controller/        # REST APIs
│   ├── service/           # Business logic
│   ├── model/             # JPA entities
│   ├── repository/        # Data access
│   ├── config/            # Security & config
│   ├── audit/             # Audit logging
│   └── util/              # JWT, encryption
├── ai-service/
│   ├── app/main.py        # FastAPI app
│   ├── scripts/           # Model training
│   └── Dockerfile
├── tests/
│   ├── postman/           # API tests
│   └── e2e/features/      # Cucumber specs
├── .github/workflows/ci.yml  # GitHub Actions
├── docker-compose.yml
├── Dockerfile
└── pom.xml
```

## 🔄 CI/CD Pipeline

GitHub Actions workflow includes:
1. Unit tests with Jacoco coverage
2. Docker image builds
3. Security scanning with Trivy
4. Postman API tests
5. Slack notifications

## 🎓 Key Features Implemented

✅ REST API with Spring Boot
✅ JWT authentication & authorization
✅ PostgreSQL database with JPA
✅ Audit logging with AOP
✅ AI fraud detection (Isolation Forest)
✅ Credit scoring (RandomForest)
✅ FinBot conversational AI
✅ Unit & integration tests
✅ Docker containerization
✅ GitHub Actions CI/CD
✅ Demo data loader
✅ AML/KYC compliance

## 📝 Default Credentials

| Email | Password |
|-------|----------|
| john@example.com | Password123 |
| jane@example.com | SecurePass456 |
| bob@example.com | MyPassword789 |

## 🚀 Next Steps

1. Start all services: `docker-compose up --build`
2. Test login endpoint with demo credentials
3. Create accounts and perform transactions
4. Try fraud detection and FinBot chat endpoints
5. Review audit logs in database

## 📞 Support

For issues:
1. Check logs: `docker-compose logs backend`
2. Verify ports are available
3. Ensure environment variables are set
4. Check database connectivity

---

**Version**: 1.0.0 (MVP)
**Last Updated**: November 24, 2025
**Status**: 🟢 Production Ready
