package com.medcore.hms.ipd.service;

import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.ipd.dto.*;
import com.medcore.hms.ipd.entity.*;
import com.medcore.hms.ipd.exception.ActiveAdmissionExistsException;
import com.medcore.hms.ipd.exception.AdmissionNotFoundException;
import com.medcore.hms.ipd.exception.BedNotAvailableException;
import com.medcore.hms.ipd.mapper.IpdMapper;
import com.medcore.hms.ipd.repository.*;
import com.medcore.hms.ipd.service.impl.IpdServiceImpl;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IpdService Unit Tests")
class IpdServiceImplTest {

    @Mock private AdmissionRepository admissionRepository;
    @Mock private DischargeSummaryRepository dischargeSummaryRepository;
    @Mock private WardRepository wardRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private BedRepository bedRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private IpdMapper ipdMapper;

    @InjectMocks
    private IpdServiceImpl ipdService;

    private UUID admissionId;
    private UUID patientId;
    private UUID doctorId;
    private UUID wardId;
    private UUID roomId;
    private UUID bedId;

    private Patient patient;
    private Doctor doctor;
    private Ward ward;
    private Room room;
    private Bed bed;
    private Admission admission;
    private CreateAdmissionRequestDto createDto;
    private AdmissionResponseDto responseDto;

    @BeforeEach
    void setUp() {
        admissionId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        wardId = UUID.randomUUID();
        roomId = UUID.randomUUID();
        bedId = UUID.randomUUID();

        patient = Patient.builder().firstName("Bruce").lastName("Wayne").build();
        patient.setId(patientId);

        doctor = Doctor.builder().build();
        doctor.setId(doctorId);

        ward = Ward.builder().name("ICU").category("Critical").capacity(10).build();
        ward.setId(wardId);

        room = Room.builder().ward(ward).roomNumber("101").build();
        room.setId(roomId);

        bed = Bed.builder().room(room).bedNumber("B1").status(BedStatus.AVAILABLE).dailyRate(new BigDecimal("200.00")).build();
        bed.setId(bedId);

        admission = Admission.builder()
                .admissionNumber("ADM-100")
                .patient(patient)
                .doctor(doctor)
                .ward(ward)
                .room(room)
                .bed(bed)
                .admissionDate(LocalDateTime.now())
                .reason("Chest pain")
                .status(AdmissionStatus.ADMITTED)
                .build();
        admission.setId(admissionId);

        createDto = new CreateAdmissionRequestDto(
                patientId, doctorId, wardId, roomId, bedId, LocalDateTime.now(), null, "Chest pain"
        );

        responseDto = new AdmissionResponseDto(
                admissionId, "ADM-100", patientId, "Bruce Wayne", doctorId, "Dr. Smith",
                "ICU", "101", "B1", LocalDateTime.now(), null, null, "Chest pain",
                AdmissionStatus.ADMITTED, null
        );
    }

    @Nested
    @DisplayName("Admit Patient Tests")
    class AdmitTests {

        @Test
        @DisplayName("Should successfully admit patient and mark bed OCCUPIED")
        void createAdmission_Success() {
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(admissionRepository.existsActiveAdmissionForPatient(patientId)).thenReturn(false);
            when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
            when(wardRepository.findById(wardId)).thenReturn(Optional.of(ward));
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
            when(bedRepository.findById(bedId)).thenReturn(Optional.of(bed));
            when(admissionRepository.save(any(Admission.class))).thenReturn(admission);
            when(ipdMapper.toAdmissionResponseDto(admission)).thenReturn(responseDto);

            AdmissionResponseDto result = ipdService.createAdmission(createDto);

            assertThat(result).isNotNull();
            assertThat(bed.getStatus()).isEqualTo(BedStatus.OCCUPIED);
            verify(bedRepository).save(bed);
            verify(admissionRepository).save(any(Admission.class));
        }

        @Test
        @DisplayName("Should throw ActiveAdmissionExistsException when patient already admitted")
        void createAdmission_ActiveAdmissionExists() {
            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(admissionRepository.existsActiveAdmissionForPatient(patientId)).thenReturn(true);

            assertThatThrownBy(() -> ipdService.createAdmission(createDto))
                    .isInstanceOf(ActiveAdmissionExistsException.class);
        }

        @Test
        @DisplayName("Should throw BedNotAvailableException when bed is OCCUPIED")
        void createAdmission_BedNotAvailable() {
            bed.setStatus(BedStatus.OCCUPIED);

            when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
            when(admissionRepository.existsActiveAdmissionForPatient(patientId)).thenReturn(false);
            when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
            when(wardRepository.findById(wardId)).thenReturn(Optional.of(ward));
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
            when(bedRepository.findById(bedId)).thenReturn(Optional.of(bed));

            assertThatThrownBy(() -> ipdService.createAdmission(createDto))
                    .isInstanceOf(BedNotAvailableException.class);
        }
    }

    @Nested
    @DisplayName("Transfer & Discharge Tests")
    class TransferAndDischargeTests {

        @Test
        @DisplayName("Should discharge patient, release bed to AVAILABLE, and create DischargeSummary")
        void dischargePatient_Success() {
            bed.setStatus(BedStatus.OCCUPIED);
            DischargeRequestDto dischargeDto = new DischargeRequestDto(
                    LocalDateTime.now(), "Recovered", "Treated", "Good condition", "Rest", null
            );

            when(admissionRepository.findById(admissionId)).thenReturn(Optional.of(admission));
            when(dischargeSummaryRepository.save(any(DischargeSummary.class)))
                    .thenReturn(DischargeSummary.builder().build());
            when(admissionRepository.save(any(Admission.class))).thenReturn(admission);
            when(ipdMapper.toAdmissionResponseDto(any(Admission.class))).thenReturn(responseDto);

            AdmissionResponseDto result = ipdService.dischargePatient(admissionId, dischargeDto);

            assertThat(result).isNotNull();
            assertThat(bed.getStatus()).isEqualTo(BedStatus.AVAILABLE);
            assertThat(admission.getStatus()).isEqualTo(AdmissionStatus.DISCHARGED);
            verify(bedRepository).save(bed);
        }
    }
}
