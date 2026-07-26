# Sprint Retrospective — Hospital Module Sprint

> **Module:** Hospital Management Module  
> **Sprint:** Week 2 / Hospital Module Sprint  
> **Date:** July 26, 2026  
> **Status:** ✅ Completed & Production-Ready  
> **Tag:** `v0.2.0-hospital-module`

---

## 🎯 Sprint Goals vs. Outcomes

| Deliverable | Status | Notes |
|-------------|--------|-------|
| Code Review — package structure, naming, SOLID | ✅ Done | Removed duplicate interface methods, renamed fields, added Javadoc, section comments |
| Security Review — RBAC, auth, sensitive data | ✅ Done | All 7 endpoints verified; no sensitive data in DTOs |
| Database Review — schema, indexes, timestamps, soft-delete | ✅ Done | Unique constraints, 4 indexes, JPA Auditing, isActive soft-delete verified |
| Testing — full regression suite | ✅ Done | 30+ controller tests, 20+ service unit tests, 15+ repository integration tests |
| Documentation — README, API docs, architecture, Swagger | ✅ Done | hospital-api.md created, README updated, architecture.md corrected |
| Git — clean commits, sprint tag | ✅ Done | Tagged `v0.2.0-hospital-module` |
| Sprint Retrospective | ✅ This document |

---

## ✅ What Went Well

### 1. Architecture & Package Structure
- Clean vertical slice architecture: `controller → service → repository → entity` with dedicated `dto`, `mapper`, `exception` packages
- `HospitalMapper` provides a single, testable place for all entity ↔ DTO transformations (SRP honoured)
- `HospitalSpecification` factory cleanly separates query logic from service logic (OCP honoured)

### 2. Security Implementation
- All 7 endpoints covered by `@PreAuthorize` with correct role sets
- `HospitalResponseDto` and `HospitalSummaryDto` contain **zero** sensitive fields (no passwords, tokens, or internal IDs leaked)
- GlobalExceptionHandler provides consistent RFC 7807 Problem Details for all error types
- JWT filter chain correctly returns 401 for unauthenticated requests before reaching controller

### 3. Database Schema Quality
- 3 unique constraints (regNumber, licenseNumber, email) enforced at both DB and application level
- 4 performance indexes on high-cardinality filter columns
- JPA Auditing (`@EnableJpaAuditing`) auto-populates `createdAt`/`updatedAt` — no manual timestamp management
- Soft deactivation via `isActive` boolean — hospital data preserved for audit trail

### 4. Test Quality
- All three test layers covered (controller, service, repository)
- Tests organized in `@Nested` classes by operation — easy to find failures
- Controller tests cover: success paths, validation (400), conflict (409), authentication (401), authorization (403)
- Service tests verify all business rule enforcement without touching the database
- Repository tests verify real JPA behavior against actual PostgreSQL schema

---

## ⚠️ Issues Found & Fixed During Sprint

| Issue | Root Cause | Fix Applied |
|-------|-----------|-------------|
| `DuplicateLicenseNumberException` not handled by GlobalExceptionHandler | Missing `@ExceptionHandler` mapping | Added handler → returns `409 Conflict` |
| `DELETE /api/hospitals/{id}` lacked OpenAPI documentation | Endpoint added without annotations | Full `@Operation` + `@ApiResponses` added |
| `HospitalSpecification` used INNER JOIN for address | City filter dropped hospitals without address | Changed to `LEFT JOIN` via `JoinType.LEFT` |
| Duplicate default methods in `HospitalService` interface | Leftover aliases from early iteration | Removed 5 redundant `default` methods |
| `HospitalController` used ambiguous `service` field name | Auto-injected by Lombok `@RequiredArgsConstructor` | Renamed to `hospitalService` |
| Architecture docs had wrong API prefix (`/api/v1/`) | Copy-paste error from template | Corrected to `/api/` with versioning note |

---

## 🛠️ Technical Debt — Tracked for Next Sprint

| Item | Impact | Priority | Target Sprint |
|------|--------|----------|---------------|
| Cache eviction uses `allEntries=true` | Unnecessarily evicts all hospitals on every write | Low | Week 3 |
| No rate limiting on Hospital endpoints | Security — production hardening needed | Medium | Week 3 |
| `getAllHospitals()` (no pagination) unused in controller | Dead method — may cause OOM on large datasets | Low | Week 3 refactor |
| Hospital soft-delete does not cascade to users/doctors/departments | Deactivating hospital leaves sub-entities active | High | Department Module |

---

## 🚀 Department Module Readiness — Handover Checklist

- [x] **Hospital entity PK:** `UUID id` — ready for FK reference in `department.hospital_id`
- [x] **Hospital isActive flag:** Departments should validate `hospital.isActive` before creation
- [x] **Sample data:** DataSeeder creates 1 default hospital + 10 departments — integration tests can use this
- [x] **HospitalRepository:** `findById(UUID)` eager-loads address — use in Department service when resolving parent
- [x] **HospitalResponseDto:** Does not include department list — Department module handles its own listing
- [x] **RBAC:** `HOSPITAL_ADMIN` has update access to hospital — will need department-scoped access too

---

## 📚 Lessons Learned

1. **Always LEFT JOIN embeddable relationships in Specifications** — INNER JOIN silently drops rows where the optional relationship is null.

2. **Exception handler completeness matters** — all custom exceptions must be mapped in `GlobalExceptionHandler`. A missing handler falls through to the generic 500 handler, masking useful 4xx responses.

3. **Interface default methods as aliases add zero value** — they increase surface area without providing polymorphism or testability benefits. Removed immediately when found.

4. **Test naming convention matters** — `@Nested` + `@DisplayName` patterns dramatically improve test failure readability in CI logs.

---

## 📊 Code Metrics (End of Sprint)

| Metric | Value |
|--------|-------|
| Production Classes | 12 |
| Test Classes | 3 |
| Test Cases | 50+ |
| API Endpoints | 7 |
| Lines of Production Code | ~600 |
| Lines of Test Code | ~750 |
