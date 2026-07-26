package com.medcore.hms.role.entity;

/**
 * Enumeration of all 9 system roles in MedCore HMS.
 * Roles are global (not hospital-scoped) and pre-seeded at startup.
 */
public enum RoleName {

    /** Full system access — manages hospitals, users, and system config. */
    SUPER_ADMIN,

    /** Full access within their own hospital. */
    HOSPITAL_ADMIN,

    /** Manages doctors within a single department. */
    DEPARTMENT_HEAD,

    /** Views and manages assigned patient records. */
    DOCTOR,

    /** Assists doctors; limited patient access. */
    NURSE,

    /** Patient registration and appointment scheduling. */
    RECEPTIONIST,

    /** Manages prescriptions and medicines. */
    PHARMACIST,

    /** Manages lab reports and test results. */
    LAB_TECHNICIAN,

    /** Can view only their own records. */
    PATIENT
}
