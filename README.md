# MedCore HMS - Hospital Management System

> A production-grade, multi-tenant Hospital Management System built with Spring Boot 3, PostgreSQL, and Redis.

## 🏗️ Tech Stack

| Layer               | Technology                             |
| ------------------- | -------------------------------------- |
| Backend             | Java 17 + Spring Boot 3.3.5            |
| Security            | Spring Security + JWT + Refresh Tokens |
| Database            | PostgreSQL 16                          |
| Cache / Token Store | Redis 7                                |
| ORM                 | Spring Data JPA + Hibernate            |
| Build               | Maven                                  |
| Containerization    | Docker + Docker Compose                |

---

## 📁 Project Structure

```
medcore-hms/
├── backend/          ← Spring Boot application
├── frontend/         ← Vite + React + TypeScript UI
├── docs/
│   ├── er-diagram.md         ← Database ER diagram
│   └── architecture.md       ← Architecture decisions
├── docker-compose.yml
└── README.md
```

---

## 🗺️ Week 1 Roadmap

### Day 1 — Foundation ✅

- [x] Project Setup & Repository Structure
- [x] Spring Boot Project Initialization
- [x] Docker Compose (PostgreSQL + Redis)
- [x] Package Structure (13 feature packages)
- [x] Database ER Diagram
- [x] Architecture Decisions Document
- [x] README Roadmap

### Day 2 — JPA Entities & Database Foundation ✅

- [x] Finalize ER Diagram (Hospital, User, Role, Department, Doctor, Patient, Address)
- [x] BaseEntity with UUID PK + JPA Auditing (createdAt, updatedAt)
- [x] Core JPA Entities with full Bean Validation
- [x] Entity Relationships (One-to-Many, Many-to-Many, One-to-One)
- [x] JpaRepository interfaces for all 7 entities
- [x] PostgreSQL + JPA/Hibernate configuration (HikariCP, UTC timezone)
- [x] DataSeeder — 9 roles, 1 sample hospital, 10 departments

### Day 2-3 — Authentication

- [ ] JWT Token Generation & Validation
- [ ] Refresh Token Mechanism
- [ ] Redis Token Storage & Blacklisting
- [ ] Login / Logout endpoints

### Day 4-5 — RBAC

- [ ] 9 Role definitions
- [ ] Role-based method security
- [ ] Permission guards

### Day 6 — Hospital Module

- [ ] Hospital entity & CRUD
- [ ] Multi-tenancy filter
- [ ] Hospital registration flow

### Day 7 — Department Module

- [ ] Department entity & CRUD
- [ ] Department ↔ Hospital relationship
- [ ] Department listing by hospital

---

## 🐳 Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker Desktop

### Run Infrastructure

```bash
docker compose up -d
```

### Run Backend

```bash
cd backend
mvn spring-boot:run
```

### Run Frontend

```bash
cd frontend
npm install
npm run dev
```

- UI: http://localhost:5173
- API proxy: requests to `/api` forward to `http://localhost:8080`

### Demo logins

| Role | Email | Password |
|------|-------|----------|
| Super Admin | `shivangv493@gmail.com` | `Password123!` |
| Doctor | `dr.arjun.sharma@medcore-hospital.com` | `Doctor@123!` |

### Verify

- App: http://localhost:8080
- PostgreSQL: localhost:5432
- Redis: `docker exec medcore-redis redis-cli ping` → PONG

---

## 🏥 Modules Planned

| Module       | Status    |
| ------------ | --------- |
| Hospital     | 🔜 Week 1 |
| Department   | 🔜 Week 1 |
| Doctor       | 🔜 Week 1 |
| Patient      | 🔜 Week 2 |
| Appointments | 🔜 Week 2 |
| Billing      | 🔜 Week 3 |
| Reports      | 🔜 Week 3 |
