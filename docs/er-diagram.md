# MedCore HMS — Database ER Diagram

> **Scope (Day 1):** Hospital, User, Role, Department, Doctor, Patient, Address
> Multi-tenancy: all tenant-scoped entities carry a `hospital_id` FK.

---

## Entity Relationship Diagram

```mermaid
erDiagram
    HOSPITAL {
        UUID id PK
        VARCHAR name
        VARCHAR registration_number UK
        VARCHAR phone
        VARCHAR email
        VARCHAR website
        BOOLEAN is_active
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    ADDRESS {
        UUID id PK
        VARCHAR street
        VARCHAR city
        VARCHAR state
        VARCHAR postal_code
        VARCHAR country
    }

    ROLE {
        UUID id PK
        VARCHAR name UK
        VARCHAR description
    }

    USER {
        UUID id PK
        UUID hospital_id FK
        VARCHAR first_name
        VARCHAR last_name
        VARCHAR email UK
        VARCHAR password_hash
        VARCHAR phone
        BOOLEAN is_active
        BOOLEAN is_email_verified
        TIMESTAMP last_login_at
        TIMESTAMP created_at
        TIMESTAMP updated_at
        TIMESTAMP deleted_at
    }

    USER_ROLES {
        UUID user_id FK
        UUID role_id FK
    }

    DEPARTMENT {
        UUID id PK
        UUID hospital_id FK
        VARCHAR name
        VARCHAR description
        BOOLEAN is_active
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    DOCTOR {
        UUID id PK
        UUID user_id FK
        UUID hospital_id FK
        UUID department_id FK
        VARCHAR license_number UK
        VARCHAR specialization
        INTEGER experience_years
        VARCHAR qualification
        DECIMAL consultation_fee
        BOOLEAN is_available
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }

    PATIENT {
        UUID id PK
        UUID user_id FK
        UUID hospital_id FK
        UUID address_id FK
        DATE date_of_birth
        VARCHAR gender
        VARCHAR blood_group
        VARCHAR emergency_contact_name
        VARCHAR emergency_contact_phone
        TEXT medical_history
        TIMESTAMP created_at
        TIMESTAMP updated_at
        TIMESTAMP deleted_at
    }

    HOSPITAL ||--o{ USER : "employs"
    HOSPITAL ||--o{ DEPARTMENT : "has"
    HOSPITAL ||--o{ DOCTOR : "employs"
    HOSPITAL ||--o{ PATIENT : "registers"

    USER ||--o{ USER_ROLES : "has"
    ROLE ||--o{ USER_ROLES : "assigned_to"

    USER ||--o| DOCTOR : "is_a"
    USER ||--o| PATIENT : "is_a"

    DEPARTMENT ||--o{ DOCTOR : "contains"

    PATIENT ||--o| ADDRESS : "lives_at"
```

---

## Table Descriptions

### `hospital`
The root tenant entity. Every hospital in the system is isolated by its `id`. SUPER_ADMIN can see all hospitals; all other roles are scoped to one hospital.

### `address`
Standalone address entity — reusable. Currently linked to `patient` (1:1) but designed to be attached to `hospital` and `doctor` in future iterations.

### `role`
Pre-seeded with 9 roles (see architecture.md). No hospital scoping — roles are global definitions.

### `user`
Core identity entity. All humans in the system are users first. `hospital_id` enforces tenant isolation. Soft-deleted via `deleted_at`.

### `user_roles` (join table)
Many-to-many between `user` and `role`. A user can have multiple roles (e.g., a DOCTOR who is also a DEPARTMENT_HEAD).

### `department`
Hospital-scoped grouping of doctors (e.g., Cardiology, Orthopedics). Belongs to one hospital.

### `doctor`
Extends `user` via 1:1 `user_id` FK. Stores professional info (license, specialization, fee). Belongs to a department.

### `patient`
Extends `user` via 1:1 `user_id` FK. Stores medical info. Has one address. Soft-deletable.

---

## Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| UUID PKs everywhere | Prevents sequential ID enumeration attacks |
| `hospital_id` on all tenant tables | Single-schema multi-tenancy — simple joins, easy SUPER_ADMIN reporting |
| Soft delete (`deleted_at`) | Medical records must be retained for compliance |
| `user` → `doctor`/`patient` via 1:1 | Single login for any role; user switches between doctor/patient profiles |
| `user_roles` join table | Users can hold multiple roles simultaneously |

---

## Indexes (to be added in migration)

```sql
-- Performance indexes
CREATE INDEX idx_user_hospital_id ON "user"(hospital_id);
CREATE INDEX idx_user_email ON "user"(email);
CREATE INDEX idx_doctor_hospital_id ON doctor(hospital_id);
CREATE INDEX idx_doctor_department_id ON doctor(department_id);
CREATE INDEX idx_patient_hospital_id ON patient(hospital_id);
CREATE INDEX idx_department_hospital_id ON department(hospital_id);

-- Partial index for soft deletes
CREATE INDEX idx_user_active ON "user"(hospital_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_patient_active ON patient(hospital_id) WHERE deleted_at IS NULL;
```
