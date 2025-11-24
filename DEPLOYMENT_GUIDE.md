# Deployment & Hosting Guide - Fintech MVP

Complete guide for deploying the Fintech MVP to various platforms with minimal effort.

---

## 🚀 Quick Start Deployments

### 1. Local Docker (Easiest for Development)

```bash
# 1. Clone repository
git clone <your-repo-url>
cd Fintech-Project-main

# 2. Create .env file with secrets
cat > .env << EOF
FINTECH_JWT_SECRET=$(openssl rand -base64 32)
FINTECH_AES_KEY=$(openssl rand -hex 32)
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
EOF

# 3. Start all services
docker-compose --env-file .env up --build

# 4. Test
curl http://localhost:8080/api/health

# 5. Stop
docker-compose down
```

**Time to Deploy**: 5 minutes  
**Cost**: Free (local machine)

---

### 2. Railway.app (Recommended for MVP)

**Pros**: 
- ✅ Free tier: $5/month credits
- ✅ Auto-deploy on git push
- ✅ Built-in PostgreSQL
- ✅ Custom domain support
- ✅ Environment variables GUI

**Steps**:

```bash
# 1. Create GitHub repo and push code
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/YOUR-USERNAME/fintech-backend.git
git push -u origin main

# 2. Go to railway.app and sign up with GitHub

# 3. Create new project

# 4. Add PostgreSQL from Templates
- Click "Add Service" > "Database" > "PostgreSQL"
- Note the connection string

# 5. Add Backend Service
- Click "Add Service" > "GitHub Repo"
- Select your fintech-backend repo
- Build command: mvn clean install
- Start command: java -jar target/backend-0.0.1-SNAPSHOT.jar

# 6. Add Environment Variables in Railway Dashboard
FINTECH_JWT_SECRET=<generate-secret>
FINTECH_AES_KEY=<generate-key>
SPRING_DATASOURCE_URL=<from-postgres-connection>
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=<from-postgres>

# 7. Deploy
Railway auto-deploys on every git push!

# 8. Get your public URL
https://<your-project>-<random>.railway.app
```

**Time to Deploy**: 15 minutes  
**Cost**: Free first month, then $5/month (~$1-2 for this app)  
**Uptime**: 99.9%

---

### 3. Heroku (Needs Credit Card but Very Easy)

**Deprecated**: Heroku removed free tier in Nov 2022. Use Railway.app instead.

---

### 4. AWS Elastic Beanstalk

**Pros**:
- ✅ Professional hosting
- ✅ Auto-scaling
- ✅ Load balancing
- ✅ RDS database included
- ✅ AWS free tier available

**Steps**:

```bash
# 1. Install AWS CLI
# Windows: https://aws.amazon.com/cli/
# Mac: brew install awscli
# Linux: sudo apt-get install awscli

# 2. Configure AWS credentials
aws configure
# Enter: Access Key ID, Secret Access Key, Region (us-east-1)

# 3. Install Elastic Beanstalk CLI
pip install awsebcli

# 4. Initialize EB project
eb init -p java-17 fintech-backend --region us-east-1

# 5. Create environment
eb create fintech-prod --instance-type t3.micro

# 6. Set environment variables
eb setenv \
  FINTECH_JWT_SECRET=<your-secret> \
  FINTECH_AES_KEY=<your-key> \
  SPRING_DATASOURCE_URL=<RDS-URL> \
  SPRING_DATASOURCE_USERNAME=postgres \
  SPRING_DATASOURCE_PASSWORD=<db-password>

# 7. Deploy
eb deploy

# 8. Open in browser
eb open

# 9. View logs
eb logs

# 10. Monitor
eb status
```

**Time to Deploy**: 30 minutes  
**Cost**: ~$10-20/month (t3.micro eligible for free tier)  
**Uptime**: 99.95%

---

### 5. Google Cloud Run (Simplest Cloud Option)

**Pros**:
- ✅ Pay-per-use (very cheap!)
- ✅ Auto-scaling
- ✅ No server management
- ✅ Free tier: 2 million requests/month

**Steps**:

```bash
# 1. Install Google Cloud SDK
# Windows: https://cloud.google.com/sdk/docs/install-sdk#windows
# Mac: brew install --cask google-cloud-sdk
# Linux: curl https://sdk.cloud.google.com | bash

# 2. Authenticate
gcloud auth login

# 3. Create project
gcloud projects create fintech-backend --name "Fintech Backend"
gcloud config set project fintech-backend

# 4. Create Cloud SQL PostgreSQL instance
gcloud sql instances create fintech-db \
  --database-version=POSTGRES_15 \
  --tier=db-f1-micro \
  --region=us-central1

# 5. Create database
gcloud sql databases create fintechdb --instance=fintech-db

# 6. Build and push Docker image
gcloud builds submit --tag gcr.io/fintech-backend/app

# 7. Deploy to Cloud Run
gcloud run deploy fintech-backend \
  --image gcr.io/fintech-backend/app \
  --platform managed \
  --region us-central1 \
  --set-env-vars \
    FINTECH_JWT_SECRET=<secret>,\
    FINTECH_AES_KEY=<key>,\
    SPRING_DATASOURCE_URL=jdbc:postgresql://<CLOUD_SQL_IP>/fintechdb

# 8. Get public URL
# Check console output for service URL
https://<random-hash>-uc.a.run.app

# 9. Monitor
gcloud run services describe fintech-backend --region us-central1
```

**Time to Deploy**: 20 minutes  
**Cost**: ~$5-10/month (mostly database)  
**Uptime**: 99.95%

---

### 6. Azure App Service

**Pros**:
- ✅ Enterprise support
- ✅ Integration with Microsoft services
- ✅ Free tier available
- ✅ Managed databases

**Steps**:

```bash
# 1. Install Azure CLI
# Windows: https://aka.ms/azcli
# Mac: brew install azure-cli
# Linux: curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash

# 2. Login to Azure
az login

# 3. Create resource group
az group create --name fintech-rg --location eastus

# 4. Create App Service plan
az appservice plan create \
  --name fintech-plan \
  --resource-group fintech-rg \
  --sku B1 \
  --is-linux

# 5. Create PostgreSQL database
az postgres flexible-server create \
  --resource-group fintech-rg \
  --name fintech-db \
  --admin-user postgres \
  --admin-password <strong-password> \
  --sku-name Standard_B1ms \
  --tier Burstable

# 6. Create web app
az webapp create \
  --resource-group fintech-rg \
  --plan fintech-plan \
  --name fintech-backend \
  --deployment-container-image-name \
    <docker-image>

# 7. Configure app settings
az webapp config appsettings set \
  --resource-group fintech-rg \
  --name fintech-backend \
  --settings \
    FINTECH_JWT_SECRET=<secret> \
    FINTECH_AES_KEY=<key> \
    SPRING_DATASOURCE_URL=jdbc:postgresql://<postgres-url>/fintechdb

# 8. Deploy
az webapp deployment source config-zip \
  --resource-group fintech-rg \
  --name fintech-backend \
  --src target/backend-0.0.1-SNAPSHOT.jar

# 9. View logs
az webapp log tail --resource-group fintech-rg --name fintech-backend
```

**Time to Deploy**: 25 minutes  
**Cost**: ~$15-30/month  
**Uptime**: 99.95%

---

### 7. DigitalOcean App Platform

**Pros**:
- ✅ Simple pricing
- ✅ Great documentation
- ✅ Managed databases
- ✅ Reasonable cost

**Steps**:

```bash
# 1. Go to DigitalOcean.com (create account)

# 2. Create App
- Click "Create" > "App"
- Connect GitHub repo
- Select your fintech-backend repo
- Choose branch: main

# 3. Configure Build
- Build command: mvn clean install
- Output directory: target/backend-0.0.1-SNAPSHOT.jar

# 4. Configure Run
- Run command: java -jar target/backend-0.0.1-SNAPSHOT.jar
- HTTP Port: 8080

# 5. Add PostgreSQL database
- Click "Create Component" > "Database" > "PostgreSQL"
- Choose Starter: $15/month

# 6. Set Environment Variables
FINTECH_JWT_SECRET=<secret>
FINTECH_AES_KEY=<key>
SPRING_DATASOURCE_URL=${DB_CONNECTION}
SPRING_DATASOURCE_USERNAME=${DB_USER}
SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}

# 7. Deploy
DigitalOcean auto-deploys on GitHub push!
```

**Time to Deploy**: 20 minutes  
**Cost**: $12/month (3 containers)  
**Uptime**: 99.99%

---

## 📊 Deployment Comparison

| Platform | Difficulty | Cost/Month | Uptime | Setup Time | Auto-Deploy |
|----------|-----------|-----------|--------|-----------|------------|
| **Local Docker** | ⭐ | Free | 100% | 5 min | ❌ |
| **Railway.app** | ⭐⭐ | $5-10 | 99.9% | 15 min | ✅ |
| **AWS Beanstalk** | ⭐⭐⭐ | $10-20 | 99.95% | 30 min | ✅ |
| **Google Cloud Run** | ⭐⭐⭐ | $5-10 | 99.95% | 20 min | ✅ |
| **Azure App Service** | ⭐⭐⭐ | $15-30 | 99.95% | 25 min | ✅ |
| **DigitalOcean** | ⭐⭐⭐ | $12+ | 99.99% | 20 min | ✅ |

**Recommendation for MVP**: **Railway.app** ✅
- Easiest setup
- Cheapest cost
- No credit card needed initially
- Perfect for testing

---

## 🔐 Production Setup Checklist

For ANY deployment platform:

```bash
# Before deploying to production:

# 1. Generate strong secrets
FINTECH_JWT_SECRET=$(openssl rand -base64 32)
FINTECH_AES_KEY=$(openssl rand -hex 32)
echo "JWT_SECRET: $FINTECH_JWT_SECRET"
echo "AES_KEY: $FINTECH_AES_KEY"

# 2. Change database password
ALTER USER postgres WITH PASSWORD '<new-strong-password>';

# 3. Enable HTTPS/SSL
# Set certificate in application.yaml

# 4. Update CORS configuration
spring.mvc.cors.allowed-origins: https://your-frontend.com

# 5. Set up monitoring
# CloudWatch, DataDog, or Sentry

# 6. Configure database backups
# Enable automated daily backups

# 7. Set up alerts
# Alert on: CPU > 80%, Memory > 85%, Error rate > 1%

# 8. Configure firewall rules
# Allow: 443 (HTTPS), 80 (HTTP)
# Deny: Everything else

# 9. Enable WAF (Web Application Firewall)
# Protect against: SQL injection, XSS, DDoS

# 10. Run security scan
mvn dependency-check:check

# 11. Document incident response
# Create runbook for outages

# 12. Test disaster recovery
# Verify backups work
# Test database restore
```

---

## 🆘 Troubleshooting Deployments

### Issue: Service won't start after deployment

```bash
# Check logs
docker logs <container-id>

# Check environment variables
echo $FINTECH_JWT_SECRET
echo $FINTECH_AES_KEY

# Check database connection
psql -h <db-host> -U postgres -d fintechdb -c "SELECT 1;"

# Verify application.yaml configuration
cat src/main/resources/application.yaml
```

### Issue: 502 Bad Gateway

```
Cause: Backend service crashed or unreachable

Solution:
1. Restart service: eb restart (AWS) or docker restart
2. Check database connection
3. Check environment variables are set
4. View application logs
```

### Issue: Database connection timeout

```
Cause: Network connectivity or database unreachable

Solution:
1. Check database is running
2. Verify connection string in SPRING_DATASOURCE_URL
3. Check security group allows traffic
4. Verify credentials are correct
```

### Issue: JWT token not working

```
Error: 401 Unauthorized

Solution:
1. Verify FINTECH_JWT_SECRET is set
2. Verify token was generated from login endpoint
3. Verify Authorization header format: "Bearer <token>"
4. Check token expiration (1 hour default)
```

---

## 📈 Scaling Guide

### When to Scale

```
- If you have > 1000 daily users: Upgrade to larger instance
- If CPU usage > 80%: Add more instances or upgrade
- If database connections > 80% of max: Increase pool size or upgrade database
- If response time > 2 seconds: Optimize queries or add caching
```

### Scaling Steps (Railway.app Example)

```bash
# 1. Go to Railway dashboard
# 2. Select service
# 3. Click "Settings"
# 4. Change "Instance Size"
# 5. Confirm deploy
```

---

## 🔄 CI/CD Pipeline

All deployments should use GitHub Actions:

```yaml
# .github/workflows/deploy.yml
name: Deploy

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      
      - name: Build
        run: mvn clean package -DskipTests
      
      - name: Deploy to Railway
        env:
          RAILWAY_TOKEN: ${{ secrets.RAILWAY_TOKEN }}
        run: |
          npm install -g @railway/cli
          railway deploy --service fintech-backend
      
      - name: Notify Slack
        uses: slackapi/slack-github-action@v1
        with:
          payload: |
            {
              "text": "Fintech MVP deployed successfully!"
            }
```

---

## 📱 Custom Domain Setup

### For Railway.app

```bash
# 1. Go to Railway dashboard
# 2. Select service
# 3. Click "Settings" > "Domains"
# 4. Add custom domain: api.myfintech.com
# 5. Update DNS settings:
#    - Type: CNAME
#    - Name: api
#    - Value: <railway-domain>
# 6. Wait 24 hours for DNS propagation
```

### For AWS Beanstalk

```bash
# 1. Get CNAME from AWS Beanstalk dashboard
# 2. Add DNS record:
#    - Type: CNAME
#    - Name: api
#    - Value: <beanstalk-cname>
```

---

## 🎉 Post-Deployment Checklist

- [ ] Verify health check endpoint works
- [ ] Test login/register endpoints
- [ ] Verify database has demo data
- [ ] Check logs for errors
- [ ] Monitor CPU and memory usage
- [ ] Test from different regions
- [ ] Set up error tracking (Sentry)
- [ ] Configure backups
- [ ] Document deployment steps
- [ ] Train team on deployment process

---

## Quick Reference

```bash
# Test deployed endpoint
curl https://your-deployed-app/api/health

# View real-time logs
railway logs  # Railway
eb logs       # AWS Beanstalk
gcloud run logs read fintech-backend  # Google Cloud Run

# Rollback to previous version
railway rollback  # Railway
eb appversion use <version>  # AWS
gcloud run deploy --revision-suffix=v2  # Google Cloud Run

# Scale service
railway scale --instances 3  # Railway
eb scale 3  # AWS (more instances in load balancer)
```

---

**Happy Deploying! 🚀**

For support: Check logs first, then see SECURITY_AND_SETUP_GUIDE.md

Version: 1.0.0 | Updated: November 24, 2025
