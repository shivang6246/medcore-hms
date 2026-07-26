# MedCore HMS — Architecture Decisions

> **Document Purpose:** Record key architectural decisions for the MedCore Hospital Management System.
> This is a living document — updated as decisions evolve.

---

## 1. Authentication — JWT + Refresh Tokens

### Decision
Use **stateless JWT access tokens** paired with **refresh tokens** for session management.

### Access Token
| Property | Value |
|----------|-------|
| Type | JWT (HS256) |
| Expiry | 15 minutes |
| Storage | HTTP-only Cookie or Authorization header |
| Payload | `userId`, `hospitalId`, `roles`, `iat`, `exp` |

### Refresh Token
| Property | Value |
|----------|-------|
| Type | Opaque UUID |
| Expiry | 7 days |
| Storage | Redis (key: `refresh:<token>`, value: `userId`) |
| Rotation | Yes — new refresh token issued on each use |

### Flow
```
Client → POST /auth/login
       ← Access Token (15m) + Refresh Token (cookie/body)

Client → POST /auth/refresh  [with refresh token]
       ← New Access Token + New Refresh Token (rotated)

Client → POST /auth/logout
       ← Refresh token deleted from Redis, access token blacklisted
```

---

## 2. Redis — Token Storage Strategy

### Usage
| Key Pattern | Value | TTL | Purpose |
|-------------|-------|-----|---------|
| `refresh:<uuid>` | `userId` | 7 days | Valid refresh tokens |
| `blacklist:<jti>` | `1` | Remaining JWT TTL | Revoked access tokens |
| `session:<userId>` | `Set<tokenId>` | 7 days | All active sessions per user |

### Why Redis?
- Sub-millisecond lookups for token validation on every request
- Automatic TTL expiry — no cron jobs needed
- Supports instant logout (blacklisting) and session revocation

---

## 3. Role-Based Access Control (RBAC) — 9 Roles

| # | Role | Scope | Description |
|---|------|-------|-------------|
| 1 | `SUPER_ADMIN` | Global | Full system access, manages hospitals |
| 2 | `HOSPITAL_ADMIN` | Hospital | Manages all data within their hospital |
| 3 | `DEPARTMENT_HEAD` | Department | Manages doctors within a department |
| 4 | `DOCTOR` | Hospital | Views/manages assigned patients |
| 5 | `NURSE` | Hospital | Assists doctors, limited patient access |
| 6 | `RECEPTIONIST` | Hospital | Patient registration, appointments |
| 7 | `PHARMACIST` | Hospital | Manages prescriptions and medicines |
| 8 | `LAB_TECHNICIAN` | Hospital | Manages lab reports |
| 9 | `PATIENT` | Hospital | Views own records only |

### Role Hierarchy
```
SUPER_ADMIN
  └─ HOSPITAL_ADMIN
       ├─ DEPARTMENT_HEAD
       │    └─ DOCTOR
       │         └─ NURSE
       ├─ RECEPTIONIST
       ├─ PHARMACIST
       └─ LAB_TECHNICIAN
PATIENT (isolated)
```

### Implementation
- Spring Security `@PreAuthorize("hasRole('HOSPITAL_ADMIN')")`
- Method-level security enabled via `@EnableMethodSecurity`
- Roles stored in `user_roles` join table, embedded in JWT

---

## 4. Multi-Tenancy Strategy — Hospital-Based

### Decision
**Shared Schema, Discriminator Column** approach.

All tenant-scoped entities have a `hospital_id` foreign key. This provides:
- Simple single-database deployment
- Easy cross-hospital reporting for SUPER_ADMIN
- No complex schema switching

### Enforcement
1. **At the repository layer** — every query filters by `hospitalId`
2. **Thread-local context** — `HospitalContext.getCurrentHospitalId()` set from JWT
3. **`@HospitalScoped` annotation** — custom AOP advice that auto-injects `hospitalId` into queries

### Example
```java
// Every tenant repository extends this:
public interface HospitalScopedRepository<T, ID> extends JpaRepository<T, ID> {
    List<T> findByHospitalId(UUID hospitalId);
}
```

---

## 5. Database Design Principles

- **UUID primary keys** for all entities (prevents sequential ID enumeration attacks)
- **Audit fields** on every entity: `createdAt`, `updatedAt` (via JPA Auditing, `@CreatedDate`, `@LastModifiedDate`)
- **Soft deletes**:
  - `User` / `Patient` → `deletedAt` timestamp (full audit trail for compliance)
  - `Hospital` / `Department` → `isActive` boolean flag (simpler lifecycle toggle, can be reactivated)
- **Database-level constraints** for referential integrity (unique constraints, FK constraints)
- **Performance indexes** on all FK columns and high-cardinality filter fields

---

## 6. API Design

- **RESTful** — standard HTTP verbs and status codes
- **Base Path** — all endpoints prefixed with `/api/` (e.g., `/api/hospitals`, `/api/auth/login`)
- **Versioning** — implicit v1 via the path prefix; explicit versioning (`/api/v2/`) if breaking changes needed
- **Global exception handler** — `@RestControllerAdvice` returns RFC 7807 Problem Details format:

```json
{
  "type": "https://medcore-hms.com/errors/hospital-not-found",
  "title": "Hospital Not Found",
  "status": 404,
  "detail": "Hospital not found with id: <uuid>",
  "timestamp": "2026-07-26T14:30:00Z"
}
```

- **API Response envelope** — successful responses wrapped in `ApiResponse<T>`:

```json
{
  "success": true,
  "message": "Hospital onboarded successfully",
  "data": { ... },
  "timestamp": "2026-07-26T14:30:00Z"
}
```

---

## 7. Caching Strategy — Spring Cache + Redis

| Operation | Strategy | Cache Key |
|-----------|----------|-----------|
| `GET /api/hospitals/{id}` | `@Cacheable` | `hospitals::UUID` |
| `POST /api/hospitals` | `@CacheEvict(allEntries=true)` | All hospital entries |
| `PUT /api/hospitals/{id}` | `@CacheEvict(allEntries=true)` | All hospital entries |
| `PATCH /deactivate` / `PATCH /activate` | `@CacheEvict(allEntries=true)` | All hospital entries |

> **Note:** `allEntries=true` is used for simplicity. A future optimization is to evict only the specific entry by key, reducing cache churn.

---

## 8. Completed Modules (Week 2)

| Module | Status | Notes |
|--------|--------|-------|
| Auth (JWT + Refresh) | ✅ Done | Stateless JWT, Redis token store, OTP email verification |
| RBAC (9 Roles) | ✅ Done | Method-level `@PreAuthorize`, role hierarchy |
| Hospital Module | ✅ Done | Full CRUD, soft deactivation, pagination, search, filter, sort |
| Department Module | 🔜 Next | Will link to Hospital via FK |
