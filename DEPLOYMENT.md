# MedCore HMS — Production Deployment & DevOps Guide

## Architecture Overview
MedCore HMS is an enterprise-grade Hospital Management System containerized with Docker, orchestrated via Docker Compose, reverse proxied by Nginx, and monitored using Spring Boot Actuator.

```
[ Client / Browser / Mobile App ]
              │
              ▼ (HTTP/80)
      [ Nginx Reverse Proxy ]
              │
              ▼
   [ Spring Boot Backend (App / Actuator) ]
        │                  │
        ▼                  ▼
 [ PostgreSQL DB ]    [ Redis Cache ]
```

---

## 1. Quick Start with Docker Compose

To launch the full enterprise stack locally or on a production VM:

```bash
docker-compose up -d --build
```

### Services Included:
- **PostgreSQL 16**: Port `5432`
- **Redis 7 Cache**: Port `6379`
- **Spring Boot Backend**: Port `8080` (`/actuator/health` enabled)
- **Nginx Reverse Proxy**: Port `80`

---

## 2. Environment Variables & Profile

The production configuration is governed by `application-prod.yml`:
- `SPRING_DATASOURCE_URL`: PostgreSQL connection string
- `SPRING_REDIS_HOST`: Redis host
- `JWT_SECRET`: 256-bit secret key for HMAC SHA signing
- `PORT`: Server port (default 8080)

---

## 3. Health Checks & Monitoring

Spring Boot Actuator endpoints are exposed:
- **Health Check**: `GET /actuator/health`
- **Metrics**: `GET /actuator/metrics`
- **Prometheus Metrics**: `GET /actuator/prometheus`

---

## 4. Automated CI/CD Pipeline

The GitHub Actions workflow at `.github/workflows/ci-cd.yml`:
1. Triggers on every push / pull request to `main`.
2. Runs unit & integration test suites (`mvn clean test`).
3. Builds the production Docker container image.
