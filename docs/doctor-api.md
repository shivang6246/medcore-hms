# MedCore HMS — Doctor Module API Reference

> **Base URL:** `http://localhost:8080/api/doctors`
> **Auth:** JWT Bearer token required on all endpoints.
> **Format:** JSON (`Content-Type: application/json`)

---

## RBAC Summary

| Endpoint | SUPER_ADMIN | HOSPITAL_ADMIN | DOCTOR | PATIENT |
|----------|:-----------:|:--------------:|:------:|:-------:|
| `POST /api/doctors` | ✅ | ✅ (own hospital) | ❌ | ❌ |
| `GET /api/doctors` | ✅ | ✅ (own hospital) | ✅ (read) | ❌ |
| `GET /api/doctors/search` | ✅ | ✅ | ✅ | ✅ (read) |
| `GET /api/doctors/{id}` | ✅ | ✅ | ✅ (self / same hospital) | ❌ |
| `PUT /api/doctors/{id}` | ✅ | ✅ | ✅ (self, limited) | ❌ |
| `PATCH /api/doctors/{id}/activate` | ✅ | ✅ | ❌ | ❌ |
| `PATCH /api/doctors/{id}/deactivate` | ✅ | ✅ | ❌ | ❌ |

---

## Endpoints

### 1. Onboard Doctor

```
POST /api/doctors
```

Creates `User` (with `DOCTOR` role) and `Doctor` profile.

**Request:**
```json
{
  "firstName": "Anita",
  "lastName": "Sharma",
  "email": "anita.sharma@cityhospital.com",
  "password": "SecurePass123!",
  "phone": "+91-9876543210",
  "employeeId": "EMP-DR-001",
  "gender": "FEMALE",
  "dateOfBirth": "1985-03-15",
  "hospitalId": "uuid",
  "departmentId": "uuid",
  "licenseNumber": "MCI-12345",
  "specialization": "Cardiology",
  "qualification": "MBBS, MD",
  "yearsOfExperience": 12,
  "consultationFee": 1500.00,
  "profileImageUrl": "https://cdn.example.com/doctors/anita.jpg",
  "biography": "Senior cardiologist with 12 years of experience."
}
```

**Response `201 Created`:**
```json
{
  "success": true,
  "message": "Doctor onboarded successfully",
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "employeeId": "EMP-DR-001",
    "firstName": "Anita",
    "lastName": "Sharma",
    "email": "anita.sharma@cityhospital.com",
    "phone": "+91-9876543210",
    "gender": "FEMALE",
    "dateOfBirth": "1985-03-15",
    "licenseNumber": "MCI-12345",
    "specialization": "Cardiology",
    "qualification": "MBBS, MD",
    "yearsOfExperience": 12,
    "consultationFee": 1500.00,
    "profileImageUrl": "https://cdn.example.com/doctors/anita.jpg",
    "biography": "Senior cardiologist with 12 years of experience.",
    "isActive": true,
    "hospital": { "id": "uuid", "name": "City Hospital" },
    "department": { "id": "uuid", "name": "Cardiology" },
    "createdAt": "2026-07-28T10:00:00",
    "updatedAt": "2026-07-28T10:00:00"
  },
  "timestamp": "2026-07-28T10:00:00Z"
}
```

**Errors:** `400` validation, `401`, `403`, `409` duplicate license/employeeId/email, `404` hospital/department not found.

---

### 2. List Doctors (Paginated)

```
GET /api/doctors
```

**Query parameters:**

| Param | Type | Default | Description |
|-------|------|---------|-------------|
| `search` | string | — | name, employeeId, licenseNumber, email, specialization |
| `hospitalId` | UUID | — | SUPER_ADMIN only |
| `departmentId` | UUID | — | Filter by department |
| `specialization` | string | — | Partial match |
| `isActive` | boolean | — | Active filter |
| `page` | int | 0 | Page index |
| `size` | int | 10 | Page size (1–100) |
| `sort` | string | `createdAt,desc` | `lastName,asc`, etc. |

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Doctors fetched successfully",
  "data": {
    "content": [
      {
        "id": "uuid",
        "employeeId": "EMP-DR-001",
        "fullName": "Anita Sharma",
        "email": "anita.sharma@cityhospital.com",
        "specialization": "Cardiology",
        "departmentName": "Cardiology",
        "hospitalName": "City Hospital",
        "consultationFee": 1500.00,
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
  "timestamp": "2026-07-28T10:00:00Z"
}
```

---

### 3. Search Doctors (Lightweight)

```
GET /api/doctors/search
```

For autocomplete and appointment booking. Returns active doctors only.

**Query parameters:**

| Param | Type | Required | Description |
|-------|------|----------|-------------|
| `q` | string | Yes | Min 2 characters |
| `hospitalId` | UUID | Conditional | SUPER_ADMIN |
| `departmentId` | UUID | No | Narrow results |
| `limit` | int | No | Default 20, max 50 |

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Search completed",
  "data": [
    {
      "id": "uuid",
      "employeeId": "EMP-DR-001",
      "fullName": "Anita Sharma",
      "email": "anita.sharma@cityhospital.com",
      "specialization": "Cardiology",
      "departmentName": "Cardiology",
      "hospitalName": "City Hospital",
      "consultationFee": 1500.00,
      "isActive": true
    }
  ],
  "timestamp": "2026-07-28T10:00:00Z"
}
```

---

### 4. Get Doctor By ID

```
GET /api/doctors/{id}
```

**Response `200 OK`:** Same structure as create response (`DoctorResponseDto`).

**Errors:** `401`, `403`, `404`.

---

### 5. Update Doctor

```
PUT /api/doctors/{id}
```

Partial update — null fields ignored.

**Request (all optional):**
```json
{
  "firstName": "Anita",
  "lastName": "Sharma",
  "phone": "+91-9876543210",
  "gender": "FEMALE",
  "dateOfBirth": "1985-03-15",
  "specialization": "Interventional Cardiology",
  "qualification": "MBBS, MD, DM",
  "yearsOfExperience": 13,
  "consultationFee": 2000.00,
  "profileImageUrl": "https://cdn.example.com/doctors/anita-v2.jpg",
  "biography": "Updated biography."
}
```

**Response `200 OK`:** Full `DoctorResponseDto`.

**Errors:** `400`, `401`, `403`, `404`, `409`.

---

### 6. Deactivate Doctor

```
PATCH /api/doctors/{id}/deactivate
```

Sets `isActive = false` and `User.isActive = false`.

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Doctor deactivated successfully",
  "data": null,
  "timestamp": "2026-07-28T10:00:00Z"
}
```

---

### 7. Activate Doctor

```
PATCH /api/doctors/{id}/activate
```

Sets `isActive = true` and `User.isActive = true`.

**Response `200 OK`:**
```json
{
  "success": true,
  "message": "Doctor activated successfully",
  "data": null,
  "timestamp": "2026-07-28T10:00:00Z"
}
```

---

## Error Responses (RFC 7807)

```json
{
  "type": "https://medcore-hms.com/errors/doctor-not-found",
  "title": "Doctor Not Found",
  "status": 404,
  "detail": "Doctor not found with id: uuid",
  "timestamp": "2026-07-28T10:00:00Z"
}
```

| Slug | Status | Trigger |
|------|--------|---------|
| `duplicate-license-number` | 409 | License already exists |
| `duplicate-employee-id` | 409 | Employee ID exists in hospital |
| `department-hospital-mismatch` | 400 | Department not in hospital |
| `validation-error` | 400 | Bean validation failure |

---

## Swagger

- UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`
