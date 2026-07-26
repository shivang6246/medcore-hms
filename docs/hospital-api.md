# MedCore HMS — Hospital Module API Reference

> **Base URL:** `http://localhost:8080/api/hospitals`  
> **Auth:** All endpoints require a valid JWT Bearer token in the `Authorization` header.  
> **Format:** JSON (Content-Type: application/json)

---

## Authentication

Include the access token from `/api/auth/login` in every request:

```
Authorization: Bearer <access_token>
```

---

## RBAC — Role Permissions

| Endpoint | SUPER_ADMIN | HOSPITAL_ADMIN | DOCTOR | NURSE | DEPARTMENT_HEAD | PATIENT |
|----------|:-----------:|:--------------:|:------:|:-----:|:---------------:|:-------:|
| GET /api/hospitals | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| GET /api/hospitals/{id} | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| POST /api/hospitals | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| PUT /api/hospitals/{id} | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| PATCH /api/hospitals/{id}/deactivate | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| PATCH /api/hospitals/{id}/activate | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| DELETE /api/hospitals/{id} | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |

---

## Endpoints

### 1. List Hospitals (Paginated)

```
GET /api/hospitals
```

Returns a paginated, filterable, and searchable list of hospital summaries.

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `search` | string | No | — | Keyword search: matches name, regNumber, licenseNumber, email, city |
| `isActive` | boolean | No | — | Filter by active status |
| `city` | string | No | — | Partial city name filter (case-insensitive) |
| `page` | int | No | 0 | Page index (0-based) |
| `size` | int | No | 10 | Page size (clamped to 1–100) |
| `sort` | string | No | `createdAt,desc` | Format: `{field},{asc|desc}` |

**Response 200 OK:**
```json
{
  "success": true,
  "message": "Hospitals fetched successfully",
  "data": {
    "content": [
      {
        "id": "uuid",
        "name": "General Hospital",
        "registrationNumber": "REG-001",
        "licenseNumber": "LIC-001",
        "email": "admin@generalhospital.com",
        "phone": "+1-555-0100",
        "logoUrl": "https://...",
        "isActive": true
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1,
    "first": true,
    "last": true
  },
  "timestamp": "2026-07-26T14:30:00Z"
}
```

**Error Responses:** `401 Unauthorized`, `403 Forbidden`

---

### 2. Get Hospital By ID

```
GET /api/hospitals/{id}
```

Returns full hospital details including address and audit timestamps.

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Hospital unique identifier |

**Response 200 OK:**
```json
{
  "success": true,
  "message": "Hospital details fetched successfully",
  "data": {
    "id": "uuid",
    "name": "General Hospital",
    "registrationNumber": "REG-001",
    "licenseNumber": "LIC-001",
    "email": "admin@generalhospital.com",
    "phone": "+1-555-0100",
    "website": "https://generalhospital.com",
    "description": "Leading healthcare provider",
    "logoUrl": "https://...",
    "isActive": true,
    "address": {
      "street": "123 Health St",
      "city": "Boston",
      "state": "MA",
      "postalCode": "02108",
      "country": "USA"
    },
    "createdAt": "2026-07-01T10:00:00",
    "updatedAt": "2026-07-26T14:00:00"
  },
  "timestamp": "2026-07-26T14:30:00Z"
}
```

**Error Responses:** `401 Unauthorized`, `403 Forbidden`, `404 Not Found`

---

### 3. Onboard a New Hospital

```
POST /api/hospitals
```

Creates a new tenant hospital. **SUPER_ADMIN only.**

**Request Body:**
```json
{
  "name": "General Hospital",
  "registrationNumber": "REG-001",
  "licenseNumber": "LIC-001",
  "email": "admin@generalhospital.com",
  "phone": "+1-555-0100",
  "website": "https://generalhospital.com",
  "description": "Leading healthcare provider",
  "logoUrl": "https://generalhospital.com/logo.png",
  "address": {
    "street": "123 Health St",
    "city": "Boston",
    "state": "MA",
    "postalCode": "02108",
    "country": "USA"
  }
}
```

**Field Constraints:**

| Field | Required | Max Length | Notes |
|-------|----------|------------|-------|
| `name` | ✅ | 200 | — |
| `registrationNumber` | ✅ | 100 | Must be globally unique |
| `licenseNumber` | ✅ | 100 | Must be globally unique |
| `email` | ✅ | 150 | Valid email, globally unique |
| `phone` | No | 20 | Pattern: `+?[0-9\-\s]{7,20}` |
| `website` | No | 255 | — |
| `description` | No | TEXT | — |
| `logoUrl` | No | 255 | — |
| `address` | No | — | All address fields required if provided |

**Response 201 Created:** Same as Get Hospital By ID response.

**Error Responses:**

| Status | Trigger |
|--------|---------|
| `400 Bad Request` | Validation failure (blank required field, invalid email/phone) |
| `401 Unauthorized` | Missing/invalid JWT |
| `403 Forbidden` | Non-SUPER_ADMIN role |
| `409 Conflict` | Duplicate registrationNumber, licenseNumber, or email |

---

### 4. Update Hospital

```
PUT /api/hospitals/{id}
```

Partial update — any null field in the request body is ignored (existing value preserved).

**Request Body:** Same structure as Create, but all fields optional.

**Response 200 OK:** Full updated hospital DTO.

**Error Responses:** `400`, `401`, `403`, `404`, `409`

---

### 5. Deactivate Hospital

```
PATCH /api/hospitals/{id}/deactivate
```

Soft-deactivates a hospital (sets `isActive = false`). **SUPER_ADMIN only.**

**Response 200 OK:**
```json
{
  "success": true,
  "message": "Hospital deactivated successfully",
  "data": null,
  "timestamp": "2026-07-26T14:30:00Z"
}
```

---

### 6. Activate Hospital

```
PATCH /api/hospitals/{id}/activate
```

Re-activates a hospital (sets `isActive = true`). **SUPER_ADMIN only.**

**Response 200 OK:**
```json
{
  "success": true,
  "message": "Hospital activated successfully",
  "data": null,
  "timestamp": "2026-07-26T14:30:00Z"
}
```

---

### 7. Delete Hospital (Soft-Delete Alias)

```
DELETE /api/hospitals/{id}
```

Functionally identical to `PATCH /{id}/deactivate`. Provided for REST convention compatibility.

---

## Standard Error Response (RFC 7807 Problem Details)

All errors return RFC 7807 Problem Details format:

```json
{
  "type": "https://medcore-hms.com/errors/hospital-not-found",
  "title": "Hospital Not Found",
  "status": 404,
  "detail": "Hospital not found with id: uuid",
  "timestamp": "2026-07-26T14:30:00Z"
}
```

### Validation Error (400):
```json
{
  "type": "https://medcore-hms.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Request validation failed. Check 'errors' for details.",
  "errors": {
    "name": "Hospital name is required",
    "email": "Invalid email format"
  },
  "timestamp": "2026-07-26T14:30:00Z"
}
```

---

## Database Schema

```sql
CREATE TABLE hospital (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(200) NOT NULL,
    registration_number VARCHAR(100) NOT NULL,
    license_number      VARCHAR(100) NOT NULL,
    email               VARCHAR(150) NOT NULL,
    phone               VARCHAR(20),
    website             VARCHAR(255),
    description         TEXT,
    logo_url            VARCHAR(255),
    is_active           BOOLEAN NOT NULL DEFAULT true,
    address_id          UUID REFERENCES address(id),
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL,

    CONSTRAINT uk_hospital_reg_num     UNIQUE (registration_number),
    CONSTRAINT uk_hospital_license_num UNIQUE (license_number),
    CONSTRAINT uk_hospital_email       UNIQUE (email)
);

-- Performance Indexes
CREATE INDEX idx_hospital_registration ON hospital(registration_number);
CREATE INDEX idx_hospital_license      ON hospital(license_number);
CREATE INDEX idx_hospital_email        ON hospital(email);
CREATE INDEX idx_hospital_active       ON hospital(is_active);
```

---

## Swagger / OpenAPI

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs
