package com.medcore.hms.doctor.service;

import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.department.entity.Department;
import com.medcore.hms.department.exception.DepartmentNotFoundException;
import com.medcore.hms.department.repository.DepartmentRepository;
import com.medcore.hms.doctor.dto.CreateDoctorRequestDto;
import com.medcore.hms.doctor.dto.DoctorResponseDto;
import com.medcore.hms.doctor.dto.DoctorSummaryDto;
import com.medcore.hms.doctor.dto.UpdateDoctorRequestDto;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.doctor.entity.Gender;
import com.medcore.hms.doctor.exception.*;
import com.medcore.hms.doctor.mapper.DoctorMapper;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.doctor.service.impl.DoctorServiceImpl;
import com.medcore.hms.hospital.entity.Hospital;
import com.medcore.hms.hospital.exception.HospitalNotFoundException;
import com.medcore.hms.hospital.repository.HospitalRepository;
import com.medcore.hms.role.entity.Role;
import com.medcore.hms.role.entity.RoleName;
import com.medcore.hms.role.repository.RoleRepository;
import com.medcore.hms.user.entity.User;
import com.medcore.hms.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DoctorServiceImpl — Unit Tests")
class DoctorServiceImplTest {

    @Mock private DoctorRepository doctorRepository;
    @Mock private HospitalRepository hospitalRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @Spy
    private DoctorMapper doctorMapper = new DoctorMapper();

    @InjectMocks
    private DoctorServiceImpl service;

    private UUID hospitalId;
    private UUID departmentId;
    private Hospital hospital;
    private Department department;
    private User user;
    private Doctor doctor;
    private CreateDoctorRequestDto createDto;

    @BeforeEach
    void setUp() {
        hospitalId   = UUID.randomUUID();
        departmentId = UUID.randomUUID();

        hospital = Hospital.builder()
                .name("Test Hospital")
                .registrationNumber("REG-001")
                .licenseNumber("LIC-001")
                .email("hospital@test.com")
                .isActive(true)
                .build();
        hospital.setId(hospitalId);

        department = Department.builder()
                .hospital(hospital)
                .name("Cardiology")
                .isActive(true)
                .build();

        user = User.builder()
                .hospital(hospital)
                .firstName("Arjun")
                .lastName("Sharma")
                .email("dr.arjun@test.com")
                .passwordHash("$2a$hash")
                .isActive(true)
                .build();

        doctor = Doctor.builder()
                .user(user)
                .hospital(hospital)
                .department(department)
                .employeeId("EMP-001")
                .email("dr.arjun@test.com")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .licenseNumber("LIC-DOC-001")
                .specialization("Cardiology")
                .qualification("MD")
                .yearsOfExperience(10)
                .consultationFee(new BigDecimal("500.00"))
                .isActive(true)
                .isAvailable(true)
                .build();

        createDto = new CreateDoctorRequestDto(
                "Arjun", "Sharma",
                "dr.arjun@test.com", "Password123!",
                "+91-9999999999",
                "EMP-001",
                Gender.MALE,
                LocalDate.of(1980, 1, 1),
                hospitalId, departmentId,
                "LIC-DOC-001", "Cardiology", "MD",
                10, new BigDecimal("500.00"),
                null, null
        );
    }

    // =========================================================================
    // createDoctor
    // =========================================================================

    @Nested
    @DisplayName("createDoctor()")
    class CreateDoctorTests {

        @Test
        @DisplayName("Success — creates User and Doctor, returns DTO")
        void createDoctor_ShouldSaveAndReturnDto() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(doctorRepository.existsByEmail("dr.arjun@test.com")).thenReturn(false);
            when(doctorRepository.existsByLicenseNumber("LIC-DOC-001")).thenReturn(false);
            when(doctorRepository.existsByEmployeeIdAndHospital_Id("EMP-001", hospitalId)).thenReturn(false);
            when(roleRepository.findByName(RoleName.DOCTOR)).thenReturn(Optional.of(Role.builder().name(RoleName.DOCTOR).build()));
            when(passwordEncoder.encode("Password123!")).thenReturn("$2a$hash");
            when(userRepository.save(any(User.class))).thenReturn(user);
            when(doctorRepository.save(any(Doctor.class))).thenReturn(doctor);

            DoctorResponseDto result = service.createDoctor(createDto);

            assertNotNull(result);
            assertEquals("dr.arjun@test.com", result.email());
            assertEquals("LIC-DOC-001", result.licenseNumber());
            assertEquals("Cardiology", result.specialization());
            assertTrue(result.isActive());
            assertTrue(result.isAvailable());
            verify(userRepository).save(any(User.class));
            verify(doctorRepository).save(any(Doctor.class));
        }

        @Test
        @DisplayName("Throws HospitalNotFoundException when hospital not found")
        void createDoctor_ShouldThrow_WhenHospitalNotFound() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.empty());
            assertThrows(HospitalNotFoundException.class, () -> service.createDoctor(createDto));
            verify(doctorRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws HospitalNotFoundException when hospital is inactive")
        void createDoctor_ShouldThrow_WhenHospitalInactive() {
            hospital.setIsActive(false);
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            assertThrows(HospitalNotFoundException.class, () -> service.createDoctor(createDto));
            verify(doctorRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws DepartmentNotFoundException when department not found")
        void createDoctor_ShouldThrow_WhenDepartmentNotFound() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());
            assertThrows(DepartmentNotFoundException.class, () -> service.createDoctor(createDto));
            verify(doctorRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws DepartmentNotFoundException when department is inactive")
        void createDoctor_ShouldThrow_WhenDepartmentInactive() {
            department.setIsActive(false);
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            assertThrows(DepartmentNotFoundException.class, () -> service.createDoctor(createDto));
            verify(doctorRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws InvalidDepartmentAssignmentException when department belongs to a different hospital")
        void createDoctor_ShouldThrow_WhenDepartmentWrongHospital() {
            Hospital otherHospital = Hospital.builder().name("Other").isActive(true).build();
            otherHospital.setId(UUID.randomUUID());
            department = Department.builder().hospital(otherHospital).name("Cardiology").isActive(true).build();

            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));

            assertThrows(InvalidDepartmentAssignmentException.class, () -> service.createDoctor(createDto));
            verify(doctorRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws DuplicateDoctorEmailException when email already exists")
        void createDoctor_ShouldThrow_WhenDuplicateEmail() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(doctorRepository.existsByEmail("dr.arjun@test.com")).thenReturn(true);

            assertThrows(DuplicateDoctorEmailException.class, () -> service.createDoctor(createDto));
            verify(doctorRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws DuplicateLicenseNumberException when license already exists")
        void createDoctor_ShouldThrow_WhenDuplicateLicense() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(doctorRepository.existsByEmail("dr.arjun@test.com")).thenReturn(false);
            when(doctorRepository.existsByLicenseNumber("LIC-DOC-001")).thenReturn(true);

            assertThrows(DuplicateLicenseNumberException.class, () -> service.createDoctor(createDto));
            verify(doctorRepository, never()).save(any());
        }

        @Test
        @DisplayName("Throws DuplicateEmployeeIdException when employeeId exists in same hospital")
        void createDoctor_ShouldThrow_WhenDuplicateEmployeeId() {
            when(hospitalRepository.findById(hospitalId)).thenReturn(Optional.of(hospital));
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(doctorRepository.existsByEmail("dr.arjun@test.com")).thenReturn(false);
            when(doctorRepository.existsByLicenseNumber("LIC-DOC-001")).thenReturn(false);
            when(doctorRepository.existsByEmployeeIdAndHospital_Id("EMP-001", hospitalId)).thenReturn(true);

            assertThrows(DuplicateEmployeeIdException.class, () -> service.createDoctor(createDto));
            verify(doctorRepository, never()).save(any());
        }
    }

    // =========================================================================
    // getDoctorById
    // =========================================================================

    @Nested
    @DisplayName("getDoctorById()")
    class GetByIdTests {

        @Test
        @DisplayName("Success — returns DoctorResponseDto when found")
        void getDoctorById_ShouldReturnDto_WhenFound() {
            UUID id = UUID.randomUUID();
            when(doctorRepository.findById(id)).thenReturn(Optional.of(doctor));

            DoctorResponseDto result = service.getDoctorById(id);

            assertNotNull(result);
            assertEquals("dr.arjun@test.com", result.email());
            assertEquals("LIC-DOC-001", result.licenseNumber());
        }

        @Test
        @DisplayName("Throws DoctorNotFoundException when not found")
        void getDoctorById_ShouldThrow_WhenNotFound() {
            UUID id = UUID.randomUUID();
            when(doctorRepository.findById(id)).thenReturn(Optional.empty());
            assertThrows(DoctorNotFoundException.class, () -> service.getDoctorById(id));
        }
    }

    // =========================================================================
    // getDoctorByUserId
    // =========================================================================

    @Nested
    @DisplayName("getDoctorByUserId()")
    class GetByUserIdTests {

        @Test
        @DisplayName("Success — returns DTO when doctor profile found for user")
        void getDoctorByUserId_ShouldReturnDto_WhenFound() {
            UUID userId = UUID.randomUUID();
            when(doctorRepository.findByUser_Id(userId)).thenReturn(Optional.of(doctor));

            DoctorResponseDto result = service.getDoctorByUserId(userId);

            assertNotNull(result);
            assertEquals("Cardiology", result.specialization());
        }

        @Test
        @DisplayName("Throws DoctorNotFoundException when no doctor profile for user")
        void getDoctorByUserId_ShouldThrow_WhenNotFound() {
            UUID userId = UUID.randomUUID();
            when(doctorRepository.findByUser_Id(userId)).thenReturn(Optional.empty());
            assertThrows(DoctorNotFoundException.class, () -> service.getDoctorByUserId(userId));
        }
    }

    // =========================================================================
    // updateDoctor
    // =========================================================================

    @Nested
    @DisplayName("updateDoctor()")
    class UpdateDoctorTests {

        @Test
        @DisplayName("Success — applies partial update and returns updated DTO")
        void updateDoctor_ShouldApplyUpdateAndReturnDto() {
            UUID id = UUID.randomUUID();
            when(doctorRepository.findById(id)).thenReturn(Optional.of(doctor));
            when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDoctorRequestDto dto = new UpdateDoctorRequestDto(
                    null, null, null, null, null,
                    "Interventional Cardiology", null, 12, null, null, null
            );

            DoctorResponseDto result = service.updateDoctor(id, dto);

            assertNotNull(result);
            assertEquals("Interventional Cardiology", result.specialization());
            assertEquals(12, result.yearsOfExperience());
        }

        @Test
        @DisplayName("Throws DoctorNotFoundException when doctor not found")
        void updateDoctor_ShouldThrow_WhenNotFound() {
            UUID id = UUID.randomUUID();
            when(doctorRepository.findById(id)).thenReturn(Optional.empty());
            assertThrows(DoctorNotFoundException.class,
                    () -> service.updateDoctor(id, new UpdateDoctorRequestDto(null, null, null, null, null, null, null, null, null, null, null)));
        }
    }

    // =========================================================================
    // activateDoctor / deactivateDoctor
    // =========================================================================

    @Nested
    @DisplayName("activateDoctor() / deactivateDoctor()")
    class ActivationTests {

        @Test
        @DisplayName("activateDoctor — sets isActive to true and saves")
        void activateDoctor_ShouldSetIsActiveTrue() {
            UUID id = UUID.randomUUID();
            doctor.setIsActive(false);
            when(doctorRepository.findById(id)).thenReturn(Optional.of(doctor));

            service.activateDoctor(id);

            assertTrue(doctor.getIsActive());
            verify(doctorRepository).save(doctor);
        }

        @Test
        @DisplayName("deactivateDoctor — sets isActive to false and saves")
        void deactivateDoctor_ShouldSetIsActiveFalse() {
            UUID id = UUID.randomUUID();
            when(doctorRepository.findById(id)).thenReturn(Optional.of(doctor));

            service.deactivateDoctor(id);

            assertFalse(doctor.getIsActive());
            verify(doctorRepository).save(doctor);
        }

        @Test
        @DisplayName("activateDoctor — throws DoctorNotFoundException when not found")
        void activateDoctor_ShouldThrow_WhenNotFound() {
            UUID id = UUID.randomUUID();
            when(doctorRepository.findById(id)).thenReturn(Optional.empty());
            assertThrows(DoctorNotFoundException.class, () -> service.activateDoctor(id));
            verify(doctorRepository, never()).save(any());
        }
    }

    // =========================================================================
    // assignDepartment
    // =========================================================================

    @Nested
    @DisplayName("assignDepartment()")
    class AssignDepartmentTests {

        @Test
        @DisplayName("Success — updates department and returns DTO")
        void assignDepartment_ShouldUpdateDepartmentAndReturnDto() {
            UUID doctorId = UUID.randomUUID();
            UUID newDeptId = UUID.randomUUID();
            Department newDept = Department.builder().hospital(hospital).name("Neurology").isActive(true).build();

            when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
            when(departmentRepository.findById(newDeptId)).thenReturn(Optional.of(newDept));
            when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));

            DoctorResponseDto result = service.assignDepartment(doctorId, newDeptId);

            assertNotNull(result);
            assertEquals("Neurology", result.department().name());
        }

        @Test
        @DisplayName("Throws InvalidDepartmentAssignmentException when department is from a different hospital")
        void assignDepartment_ShouldThrow_WhenDepartmentWrongHospital() {
            UUID doctorId = UUID.randomUUID();
            UUID newDeptId = UUID.randomUUID();
            Hospital otherHospital = Hospital.builder().name("Other Hospital").isActive(true).build();
            otherHospital.setId(UUID.randomUUID());
            Department wrongDept = Department.builder().hospital(otherHospital).name("Other Dept").isActive(true).build();

            when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
            when(departmentRepository.findById(newDeptId)).thenReturn(Optional.of(wrongDept));

            assertThrows(InvalidDepartmentAssignmentException.class,
                    () -> service.assignDepartment(doctorId, newDeptId));
            verify(doctorRepository, never()).save(any());
        }
    }

    // =========================================================================
    // updateConsultationFee
    // =========================================================================

    @Nested
    @DisplayName("updateConsultationFee()")
    class ConsultationFeeTests {

        @Test
        @DisplayName("Success — updates fee and returns DTO")
        void updateConsultationFee_ShouldUpdateFeeAndReturnDto() {
            UUID id = UUID.randomUUID();
            BigDecimal newFee = new BigDecimal("750.00");
            when(doctorRepository.findById(id)).thenReturn(Optional.of(doctor));
            when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));

            DoctorResponseDto result = service.updateConsultationFee(id, newFee);

            assertNotNull(result);
            assertEquals(0, newFee.compareTo(result.consultationFee()));
        }

        @Test
        @DisplayName("Throws DoctorNotFoundException when doctor not found")
        void updateConsultationFee_ShouldThrow_WhenNotFound() {
            UUID id = UUID.randomUUID();
            when(doctorRepository.findById(id)).thenReturn(Optional.empty());
            assertThrows(DoctorNotFoundException.class,
                    () -> service.updateConsultationFee(id, BigDecimal.TEN));
        }
    }

    // =========================================================================
    // updateAvailability
    // =========================================================================

    @Nested
    @DisplayName("updateAvailability()")
    class AvailabilityTests {

        @Test
        @DisplayName("Sets isAvailable to false and saves")
        void updateAvailability_ShouldSetFalse() {
            UUID id = UUID.randomUUID();
            when(doctorRepository.findById(id)).thenReturn(Optional.of(doctor));

            service.updateAvailability(id, false);

            assertFalse(doctor.getIsAvailable());
            verify(doctorRepository).save(doctor);
        }

        @Test
        @DisplayName("Sets isAvailable to true and saves")
        void updateAvailability_ShouldSetTrue() {
            UUID id = UUID.randomUUID();
            doctor.setIsAvailable(false);
            when(doctorRepository.findById(id)).thenReturn(Optional.of(doctor));

            service.updateAvailability(id, true);

            assertTrue(doctor.getIsAvailable());
            verify(doctorRepository).save(doctor);
        }
    }

    // =========================================================================
    // getAllDoctors
    // =========================================================================

    @Nested
    @DisplayName("getAllDoctors()")
    class GetAllDoctorsTests {

        @Test
        @DisplayName("Returns PagedResponse with summary DTOs")
        void getAllDoctors_ShouldReturnPagedResponse() {
            Page<Doctor> page = new PageImpl<>(List.of(doctor), PageRequest.of(0, 10), 1);
            when(doctorRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

            PagedResponse<DoctorSummaryDto> result = service.getAllDoctors(PageRequest.of(0, 10));

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            assertEquals("Arjun Sharma", result.content().get(0).fullName());
        }

        @Test
        @DisplayName("Returns empty page when no doctors exist")
        void getAllDoctors_ShouldReturnEmptyPage_WhenNoneExist() {
            Page<Doctor> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            when(doctorRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(emptyPage);

            PagedResponse<DoctorSummaryDto> result = service.getAllDoctors(PageRequest.of(0, 10));

            assertTrue(result.content().isEmpty());
            assertEquals(0, result.totalElements());
        }
    }
}
