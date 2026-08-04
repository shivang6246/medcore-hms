package com.medcore.hms.ipd.repository;

import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.hospital.entity.Hospital;
import com.medcore.hms.hospital.repository.HospitalRepository;
import com.medcore.hms.ipd.entity.*;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@DisplayName("IPD Repositories — Integration Tests")
class IpdRepositoryTest {

    @Autowired private WardRepository wardRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private BedRepository bedRepository;
    @Autowired private AdmissionRepository admissionRepository;
    @Autowired private DischargeSummaryRepository dischargeSummaryRepository;
    @Autowired private HospitalRepository hospitalRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private DoctorRepository doctorRepository;

    private Patient patient;
    private Doctor doctor;
    private Ward ward;
    private Room room;
    private Bed bed;

    @BeforeEach
    void setUp() {
        Hospital hospital = hospitalRepository.save(Hospital.builder()
                .name("IPD Hospital " + UUID.randomUUID().toString().substring(0, 5))
                .registrationNumber("REG-" + UUID.randomUUID().toString().substring(0, 8))
                .licenseNumber("LIC-" + UUID.randomUUID().toString().substring(0, 8))
                .email("ipd_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .phone("1234567890")
                .isActive(true)
                .build());

        patient = patientRepository.save(Patient.builder()
                .hospital(hospital)
                .patientId("PID-" + UUID.randomUUID().toString().substring(0, 8))
                .firstName("Clark")
                .lastName("Kent")
                .dateOfBirth(LocalDate.of(1985, 6, 18))
                .phone("9876543" + (int)(Math.random()*100))
                .email("clark_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com")
                .emergencyContactName("Lois Lane")
                .emergencyContactPhone("9876543210")
                .isActive(true)
                .build());

        doctor = doctorRepository.save(Doctor.builder()
                .hospital(hospital)
                .licenseNumber("DOC-" + UUID.randomUUID().toString().substring(0, 8))
                .specialization("General Medicine")
                .consultationFee(new BigDecimal("100.00"))
                .yearsOfExperience(10)
                .isActive(true)
                .build());

        ward = wardRepository.save(Ward.builder()
                .name("General Ward " + UUID.randomUUID().toString().substring(0, 5))
                .category("General")
                .capacity(20)
                .isActive(true)
                .build());

        room = roomRepository.save(Room.builder()
                .ward(ward)
                .roomNumber("ROOM-101")
                .roomType("Single")
                .isActive(true)
                .build());

        bed = bedRepository.save(Bed.builder()
                .room(room)
                .bedNumber("BED-01")
                .status(BedStatus.AVAILABLE)
                .dailyRate(new BigDecimal("150.00"))
                .isActive(true)
                .build());
    }

    @Test
    @DisplayName("Should query available beds by ward")
    void findAvailableBedsByWard_Success() {
        List<Bed> availableBeds = bedRepository.findAvailableBedsByWard(ward.getId());

        assertThat(availableBeds).hasSize(1);
        assertThat(availableBeds.get(0).getBedNumber()).isEqualTo("BED-01");
    }

    @Test
    @DisplayName("Should detect active admission for patient")
    void existsActiveAdmissionForPatient_Success() {
        admissionRepository.save(Admission.builder()
                .admissionNumber("ADM-TEST-99")
                .patient(patient)
                .doctor(doctor)
                .ward(ward)
                .room(room)
                .bed(bed)
                .admissionDate(LocalDateTime.now())
                .status(AdmissionStatus.ADMITTED)
                .isActive(true)
                .build());

        boolean hasActive = admissionRepository.existsActiveAdmissionForPatient(patient.getId());

        assertThat(hasActive).isTrue();
    }
}
