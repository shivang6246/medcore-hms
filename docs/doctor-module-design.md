# MedCore HMS — Doctor Module Design

> **Status:** Design complete — implementation week follows this document.
> **Scope this week:** Entity, Hospital/Department FKs, CRUD APIs, RBAC.
> **Deferred:** Appointments, Prescriptions, Medical Records, Schedules, photo upload, ratings, signatures.

---

## 1. Entity Design

### `doctor` table

| Column                | Type          | Nullable | Default   | Notes                                          |
| --------------------- | ------------- | -------- | --------- | ---------------------------------------------- |
| `id`                  | UUID          | NO       | generated | PK (from `BaseEntity`)                         |
| `user_id`             | UUID          | NO       | —         | FK → `app_user`, unique (1:1 identity)         |
| `hospital_id`         | UUID          | NO       | —         | FK → `hospital`                                |
| `department_id`       | UUID          | NO       | —         | FK → `department`                              |
| `employee_id`         | VARCHAR(50)   | NO       | —         | Unique per hospital                            |
| `gender`              | VARCHAR(20)   | YES      | —         | `MALE`, `FEMALE`, `OTHER`, `PREFER_NOT_TO_SAY` |
| `date_of_birth`       | DATE          | YES      | —         | Must be in the past                            |
| `license_number`      | VARCHAR(100)  | NO       | —         | Globally unique                                |
| `specialization`      | VARCHAR(150)  | NO       | —         |                                                |
| `qualification`       | VARCHAR(255)  | YES      | —         | e.g. MBBS, MD                                  |
| `years_of_experience` | INTEGER       | YES      | 0         | 0–60                                           |
| `consultation_fee`    | DECIMAL(10,2) | YES      | 0.00      | Non-negative                                   |
| `profile_image_url`   | VARCHAR(500)  | YES      | —         | URL until upload module exists                 |
| `biography`           | TEXT          | YES      | —         |                                                |
| `is_active`           | BOOLEAN       | NO       | `true`    | Soft delete / employment flag                  |
| `created_at`          | TIMESTAMP     | NO       | —         | JPA auditing                                   |
| `updated_at`          | TIMESTAMP     | NO       | —         | JPA auditing                                   |

### Identity split (`User` vs `Doctor`)

Personal and login fields live on `User` (existing pattern from `Patient`):

| API / DTO field                       | Stored on | Reason                     |
| ------------------------------------- | --------- | -------------------------- |
| `firstName`, `lastName`               | `User`    | Shared identity model      |
| `email`                               | `User`    | Login + globally unique    |
| `phone`                               | `User`    | Contact on identity record |
| `password`                            | `User`    | Auth (create only)         |
| `employeeId`, `gender`, `dateOfBirth` | `Doctor`  | Employment / profile       |
| Professional fields                   | `Doctor`  | Domain-specific            |

Service layer syncs `User` fields on create/update; DTOs present a unified doctor profile to clients.

---

## 2. Relationships

### Implemented this week

```mermaid
erDiagram
    HOSPITAL ||--o{ DOCTOR : employs
    DEPARTMENT ||--o{ DOCTOR : contains
    USER ||--o| DOCTOR : "is_a"

    DOCTOR {
        UUID id PK
        UUID user_id FK UK
        UUID hospital_id FK
        UUID department_id FK
        VARCHAR employee_id
        VARCHAR license_number UK
        BOOLEAN is_active
    }
```

| Relationship        | Cardinality | FK              | Rule                                              |
| ------------------- | ----------- | --------------- | ------------------------------------------------- |
| Doctor → Hospital   | N:1         | `hospital_id`   | Required; immutable after create                  |
| Doctor → Department | N:1         | `department_id` | Required; department must belong to same hospital |
| Doctor → User       | 1:1         | `user_id`       | Required; one doctor profile per user             |
| Hospital → Doctor   | 1:N         | —               | Inverse mapping                                   |
| Department → Doctor | 1:N         | —               | Inverse mapping                                   |

### Planned (not implemented this week)

| Relationship              | Cardinality | Notes                                                              |
| ------------------------- | ----------- | ------------------------------------------------------------------ |
| Doctor → Appointment      | 1:N         | `doctor_id` on `appointment`; inactive doctors rejected at booking |
| Doctor → Prescription     | 1:N         | Issued by doctor                                                   |
| Doctor → MedicalRecord    | 1:N         | Authored / attributed to doctor                                    |
| Doctor → Schedule         | 1:N         | Weekly slots; feeds `DoctorAvailabilityDto`                        |
| Doctor → Leave            | 1:N         | Blocks slots without deactivating doctor                           |
| Doctor → Rating           | 1:N         | Patient feedback (future)                                          |
| Doctor → DigitalSignature | 1:1         | Stored asset reference (future)                                    |

Cardinality is fixed now so FK columns and indexes can be added without schema churn.

---

## 3. Business Rules

| #     | Rule                                                                        | Enforcement                                                     |
| ----- | --------------------------------------------------------------------------- | --------------------------------------------------------------- |
| BR-01 | `license_number` is globally unique                                         | DB `UNIQUE`, service pre-check                                  |
| BR-02 | `employee_id` is unique within a hospital                                   | DB `UNIQUE (hospital_id, employee_id)`                          |
| BR-03 | A doctor belongs to exactly one department at a time                        | Single `department_id` FK; transfer = update                    |
| BR-04 | Department must belong to the doctor's hospital                             | Service validates `department.hospital_id = doctor.hospital_id` |
| BR-05 | Soft delete via `is_active = false`; no hard delete                         | PATCH deactivate only                                           |
| BR-06 | Inactive doctors cannot receive appointments                                | Appointment module checks `is_active` (future)                  |
| BR-07 | Deactivated doctor's `User.is_active` set to `false`                        | Service on deactivate                                           |
| BR-08 | On create: `User` created with `DOCTOR` role, email verified by admin flow  | `AuthService` / seeder pattern                                  |
| BR-09 | `email` globally unique on `User`                                           | Existing constraint                                             |
| BR-10 | `consultation_fee` defaults to `0.00` when omitted                          | Entity default                                                  |
| BR-11 | `years_of_experience` defaults to `0` when omitted                          | Entity default                                                  |
| BR-12 | SUPER_ADMIN must supply `hospitalId`; HOSPITAL_ADMIN scoped to JWT hospital | Controller / service                                            |
| BR-13 | Cannot deactivate doctor with active future appointments                    | Future guard when Appointment exists                            |
| BR-14 | Reactivating doctor re-enables `User.is_active`                             | Service on activate                                             |

---

## 4. Validation Rules

### Create (`CreateDoctorRequestDto`)

| Field               | Required    | Constraints                                            |
| ------------------- | ----------- | ------------------------------------------------------ |
| `firstName`         | Yes         | `@NotBlank`, max 100                                   |
| `lastName`          | Yes         | `@NotBlank`, max 100                                   |
| `email`             | Yes         | `@Email`, max 150                                      |
| `password`          | Yes         | `@NotBlank`, min 8, max 100                            |
| `phone`             | No          | Pattern `^\+?[0-9\-\s]{7,20}$`, max 20                 |
| `employeeId`        | Yes         | `@NotBlank`, max 50, pattern `^[A-Za-z0-9\-]{3,50}$`   |
| `gender`            | No          | Enum: `MALE`, `FEMALE`, `OTHER`, `PREFER_NOT_TO_SAY`   |
| `dateOfBirth`       | No          | `@Past`                                                |
| `hospitalId`        | Conditional | Required for SUPER_ADMIN; ignored for HOSPITAL_ADMIN   |
| `departmentId`      | Yes         | `@NotNull`                                             |
| `licenseNumber`     | Yes         | `@NotBlank`, max 100, pattern `^[A-Za-z0-9\-]{5,100}$` |
| `specialization`    | Yes         | `@NotBlank`, max 150                                   |
| `qualification`     | No          | max 255                                                |
| `yearsOfExperience` | No          | `@Min(0)`, `@Max(60)`, default 0                       |
| `consultationFee`   | No          | `@DecimalMin("0.00")`, `@Digits(10,2)`, default 0.00   |
| `profileImageUrl`   | No          | max 500, valid URL pattern                             |
| `biography`         | No          | max 5000 chars                                         |

### Update (`UpdateDoctorRequestDto`)

All fields optional (partial update). Same constraints when present. `hospitalId` and `departmentId` not mutable on update (transfer = dedicated future endpoint).

### Doctor self-update (DOCTOR role)

Allowed fields: `phone`, `qualification`, `biography`, `profileImageUrl`, `consultationFee`.
Not allowed: `licenseNumber`, `employeeId`, `departmentId`, `hospitalId`, `email`, `isActive`.

---

## 5. REST API Contract

Base path: `/api/doctors`. All endpoints require JWT unless noted.

| Method  | Path                           | Description                              |
| ------- | ------------------------------ | ---------------------------------------- |
| `POST`  | `/api/doctors`                 | Onboard doctor (User + Doctor)           |
| `GET`   | `/api/doctors`                 | Paginated list with search, filter, sort |
| `GET`   | `/api/doctors/search`          | Lightweight search for booking UI        |
| `GET`   | `/api/doctors/{id}`            | Full doctor profile                      |
| `PUT`   | `/api/doctors/{id}`            | Admin partial update                     |
| `PATCH` | `/api/doctors/{id}/activate`   | Reactivate                               |
| `PATCH` | `/api/doctors/{id}/deactivate` | Soft deactivate                          |

Full request/response examples: [`doctor-api.md`](./doctor-api.md).

### `GET /api/doctors` query parameters

| Param            | Type    | Default          | Description                                            |
| ---------------- | ------- | ---------------- | ------------------------------------------------------ |
| `search`         | string  | —                | Name, employeeId, licenseNumber, email, specialization |
| `hospitalId`     | UUID    | —                | SUPER_ADMIN filter                                     |
| `departmentId`   | UUID    | —                | Filter by department                                   |
| `specialization` | string  | —                | Exact or partial match                                 |
| `isActive`       | boolean | —                | Active filter                                          |
| `page`           | int     | 0                | Page index                                             |
| `size`           | int     | 10               | Page size (1–100)                                      |
| `sort`           | string  | `createdAt,desc` | e.g. `lastName,asc`                                    |

### `GET /api/doctors/search` query parameters

| Param          | Type   | Required    | Description                         |
| -------------- | ------ | ----------- | ----------------------------------- |
| `q`            | string | Yes         | Min 2 chars; name or specialization |
| `hospitalId`   | UUID   | Conditional | SUPER_ADMIN                         |
| `departmentId` | UUID   | No          | Narrow to department                |
| `limit`        | int    | No          | Default 20, max 50                  |

Returns `List<DoctorSummaryDto>` (not paginated wrapper) for autocomplete.

---

## 6. DTO Plan

| DTO                      | Purpose          | Exposes                                                                                                                 |
| ------------------------ | ---------------- | ----------------------------------------------------------------------------------------------------------------------- |
| `CreateDoctorRequestDto` | Onboard          | Input only                                                                                                              |
| `UpdateDoctorRequestDto` | Admin update     | Input only                                                                                                              |
| `DoctorResponseDto`      | Detail view      | id, userId, employeeId, name, email, phone, gender, dob, hospital/department refs, professional fields, isActive, audit |
| `DoctorSummaryDto`       | List / search    | id, employeeId, fullName, email, specialization, departmentName, hospitalName, consultationFee, isActive                |
| `DoctorAvailabilityDto`  | Future schedules | dayOfWeek, slotStart, slotEnd, isBookable — stub this week                                                              |

**Not exposed:** `passwordHash`, internal FK objects, Hibernate proxies, `User.deletedAt`.

Nested refs in `DoctorResponseDto`:

```json
"hospital": { "id": "...", "name": "..." },
"department": { "id": "...", "name": "..." }
```

---

## 7. RBAC Matrix

| Endpoint                  |   SUPER_ADMIN   | HOSPITAL_ADMIN  |          DOCTOR          |    PATIENT     |      Others       |
| ------------------------- | :-------------: | :-------------: | :----------------------: | :------------: | :---------------: |
| `POST /api/doctors`       | ✅ any hospital | ✅ own hospital |            ❌            |       ❌       |        ❌         |
| `GET /api/doctors`        |       ✅        | ✅ own hospital |  ✅ own hospital (read)  |       ❌       |        ❌         |
| `GET /api/doctors/search` |       ✅        | ✅ own hospital |     ✅ own hospital      | ✅ read only\* | RECEPTIONIST ✅\* |
| `GET /api/doctors/{id}`   |       ✅        | ✅ own hospital | ✅ self or same hospital |       ❌       |        ❌         |
| `PUT /api/doctors/{id}`   |       ✅        | ✅ own hospital |    ✅ self (limited)     |       ❌       |        ❌         |
| `PATCH .../activate`      |       ✅        | ✅ own hospital |            ❌            |       ❌       |        ❌         |
| `PATCH .../deactivate`    |       ✅        | ✅ own hospital |            ❌            |       ❌       |        ❌         |

\* PATIENT and RECEPTIONIST read-only on search — active doctors only; documented for appointment module prep.

### `@PreAuthorize` sketch (implementation week)

```java
@PreAuthorize("hasRole('SUPER_ADMIN')")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN')")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR')")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'PATIENT', 'RECEPTIONIST')") // search only
```

Hospital scoping enforced in service via JWT `hospitalId` + role checks.

---

## 8. Database Constraints

```sql
CREATE TABLE doctor (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID NOT NULL REFERENCES app_user(id),
    hospital_id         UUID NOT NULL REFERENCES hospital(id),
    department_id       UUID NOT NULL REFERENCES department(id),
    employee_id         VARCHAR(50) NOT NULL,
    gender              VARCHAR(20),
    date_of_birth       DATE,
    license_number      VARCHAR(100) NOT NULL,
    specialization      VARCHAR(150) NOT NULL,
    qualification       VARCHAR(255),
    years_of_experience INTEGER DEFAULT 0,
    consultation_fee    DECIMAL(10,2) DEFAULT 0.00,
    profile_image_url   VARCHAR(500),
    biography           TEXT,
    is_active           BOOLEAN NOT NULL DEFAULT true,
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL,

    CONSTRAINT uk_doctor_user           UNIQUE (user_id),
    CONSTRAINT uk_doctor_license        UNIQUE (license_number),
    CONSTRAINT uk_doctor_hospital_emp   UNIQUE (hospital_id, employee_id)
);

CREATE INDEX idx_doctor_hospital_id     ON doctor(hospital_id);
CREATE INDEX idx_doctor_department_id   ON doctor(department_id);
CREATE INDEX idx_doctor_license         ON doctor(license_number);
CREATE INDEX idx_doctor_employee        ON doctor(hospital_id, employee_id);
CREATE INDEX idx_doctor_specialization  ON doctor(specialization);
CREATE INDEX idx_doctor_active          ON doctor(is_active);
CREATE INDEX idx_doctor_active_hospital ON doctor(hospital_id, is_active);
```

---

## 9. Future Extensions

| Feature              | Design hook                                                                     |
| -------------------- | ------------------------------------------------------------------------------- |
| Weekly schedules     | `schedule` table with `doctor_id`; `DoctorAvailabilityDto` populated from slots |
| Leave management     | `doctor_leave` table; blocks slots without `is_active = false`                  |
| Consultation slots   | Derived from schedule minus bookings + leave                                    |
| Profile photo upload | Replace `profile_image_url` string with `file_asset_id` FK                      |
| Doctor ratings       | `doctor_rating` table; average exposed on `DoctorSummaryDto` later              |
| Digital signatures   | `doctor_signature` 1:1; URL or blob reference for prescriptions                 |
| Department transfer  | `PATCH /api/doctors/{id}/transfer` with audit log                               |
| Appointment guard    | BR-06 enforced in `AppointmentService`                                          |

`DoctorRepository` will extend `JpaSpecificationExecutor` for list/search filters (same pattern as Hospital).

---

## 10. Module Structure (implementation week)

```
doctor/
├── controller/DoctorController.java
├── dto/
│   ├── CreateDoctorRequestDto.java
│   ├── UpdateDoctorRequestDto.java
│   ├── DoctorResponseDto.java
│   ├── DoctorSummaryDto.java
│   ├── DoctorAvailabilityDto.java
│   └── DepartmentRefDto.java / HospitalRefDto.java (or common)
├── entity/Doctor.java
├── exception/
│   ├── DoctorNotFoundException.java
│   ├── DuplicateLicenseNumberException.java
│   ├── DuplicateEmployeeIdException.java
│   └── DepartmentHospitalMismatchException.java
├── mapper/DoctorMapper.java
├── repository/
│   ├── DoctorRepository.java
│   └── DoctorSpecification.java
└── service/
    ├── DoctorService.java
    └── impl/DoctorServiceImpl.java
```
