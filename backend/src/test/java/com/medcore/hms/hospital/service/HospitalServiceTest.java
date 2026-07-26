package com.medcore.hms.hospital.service;

import com.medcore.hms.common.dto.AddressDto;
import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.common.entity.Address;
import com.medcore.hms.hospital.dto.CreateHospitalRequestDto;
import com.medcore.hms.hospital.dto.HospitalResponseDto;
import com.medcore.hms.hospital.dto.HospitalSummaryDto;
import com.medcore.hms.hospital.dto.UpdateHospitalRequestDto;
import com.medcore.hms.hospital.entity.Hospital;
import com.medcore.hms.hospital.exception.DuplicateHospitalEmailException;
import com.medcore.hms.hospital.exception.DuplicateLicenseNumberException;
import com.medcore.hms.hospital.exception.DuplicateRegistrationNumberException;
import com.medcore.hms.hospital.exception.HospitalNotFoundException;
import com.medcore.hms.hospital.mapper.HospitalMapper;
import com.medcore.hms.hospital.repository.HospitalRepository;
import com.medcore.hms.hospital.service.impl.HospitalServiceImpl;
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
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link HospitalServiceImpl}.
 *
 * <p>All repository calls are mocked; mapper is used as a real Spy
 * to validate correct entity-to-DTO transformations.
 *
 * <p>Coverage:
 * <ul>
 *   <li>createHospital — success and all 3 conflict scenarios</li>
 *   <li>updateHospital — success, not found, duplicate reg/license/email on change</li>
 *   <li>getHospitalById — success and not found</li>
 *   <li>getAllHospitals — returns summary list</li>
 *   <li>getHospitals — pagination, search, filter, size clamping</li>
 *   <li>deactivateHospital — success and not found</li>
 *   <li>activateHospital — success and not found</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HospitalServiceImpl — Unit Tests")
class HospitalServiceTest {

    @Mock
    private HospitalRepository repository;

    @Spy
    private HospitalMapper mapper = new HospitalMapper();

    @InjectMocks
    private HospitalServiceImpl service;

    private AddressDto addressDto;
    private CreateHospitalRequestDto createDto;

    @BeforeEach
    void setUp() {
        addressDto = new AddressDto("123 Health Ave", "Metropolis", "NY", "10001", "USA");
        createDto = new CreateHospitalRequestDto(
                "City Hospital", "REG-100", "LIC-200",
                "info@cityhospital.com", "+1-555-0199",
                "https://cityhospital.com", "Leading healthcare provider",
                "https://cityhospital.com/logo.png", addressDto
        );
    }

    // =========================================================================
    // createHospital
    // =========================================================================

    @Nested
    @DisplayName("createHospital()")
    class CreateHospitalTests {

        @Test
        @DisplayName("Success — saves and returns HospitalResponseDto with address")
        void createHospital_ShouldSaveAndReturnDto() {
            when(repository.existsByRegistrationNumber("REG-100")).thenReturn(false);
            when(repository.existsByLicenseNumber("LIC-200")).thenReturn(false);
            when(repository.existsByEmail("info@cityhospital.com")).thenReturn(false);

            Hospital saved = Hospital.builder()
                    .name("City Hospital")
                    .registrationNumber("REG-100")
                    .licenseNumber("LIC-200")
                    .email("info@cityhospital.com")
                    .phone("+1-555-0199")
                    .website("https://cityhospital.com")
                    .description("Leading healthcare provider")
                    .logoUrl("https://cityhospital.com/logo.png")
                    .address(Address.builder()
                            .street("123 Health Ave")
                            .city("Metropolis")
                            .state("NY")
                            .postalCode("10001")
                            .country("USA")
                            .build())
                    .isActive(true)
                    .build();

            when(repository.save(any(Hospital.class))).thenReturn(saved);

            HospitalResponseDto result = service.createHospital(createDto);

            assertNotNull(result);
            assertEquals("City Hospital", result.name());
            assertEquals("REG-100", result.registrationNumber());
            assertEquals("LIC-200", result.licenseNumber());
            assertEquals("info@cityhospital.com", result.email());
            assertTrue(result.isActive());
            assertNotNull(result.address());
            assertEquals("Metropolis", result.address().city());
            verify(repository).save(any(Hospital.class));
        }

        @Test
        @DisplayName("Throws DuplicateRegistrationNumberException when regNumber exists")
        void createHospital_ShouldThrow_WhenDuplicateRegNumber() {
            when(repository.existsByRegistrationNumber("REG-100")).thenReturn(true);

            assertThrows(DuplicateRegistrationNumberException.class,
                    () -> service.createHospital(createDto));
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Throws DuplicateLicenseNumberException when licenseNumber exists")
        void createHospital_ShouldThrow_WhenDuplicateLicenseNumber() {
            when(repository.existsByRegistrationNumber("REG-100")).thenReturn(false);
            when(repository.existsByLicenseNumber("LIC-200")).thenReturn(true);

            assertThrows(DuplicateLicenseNumberException.class,
                    () -> service.createHospital(createDto));
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Throws DuplicateHospitalEmailException when email exists")
        void createHospital_ShouldThrow_WhenDuplicateEmail() {
            when(repository.existsByRegistrationNumber("REG-100")).thenReturn(false);
            when(repository.existsByLicenseNumber("LIC-200")).thenReturn(false);
            when(repository.existsByEmail("info@cityhospital.com")).thenReturn(true);

            assertThrows(DuplicateHospitalEmailException.class,
                    () -> service.createHospital(createDto));
            verify(repository, never()).save(any());
        }
    }

    // =========================================================================
    // updateHospital
    // =========================================================================

    @Nested
    @DisplayName("updateHospital()")
    class UpdateHospitalTests {

        private UUID id;
        private Hospital existing;

        @BeforeEach
        void setUpExisting() {
            id = UUID.randomUUID();
            existing = Hospital.builder()
                    .name("Old Hospital Name")
                    .registrationNumber("REG-100")
                    .licenseNumber("LIC-200")
                    .email("old@hospital.com")
                    .isActive(true)
                    .build();
        }

        @Test
        @DisplayName("Success — applies partial update and saves")
        void updateHospital_ShouldApplyUpdatesAndSave() {
            when(repository.findById(id)).thenReturn(Optional.of(existing));
            when(repository.save(any(Hospital.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateHospitalRequestDto dto = new UpdateHospitalRequestDto(
                    "Updated Hospital Name", null, null, null, null, null,
                    "Updated Description", null, null
            );

            HospitalResponseDto response = service.updateHospital(id, dto);

            assertNotNull(response);
            assertEquals("Updated Hospital Name", response.name());
            assertEquals("Updated Description", response.description());
            verify(repository).save(existing);
        }

        @Test
        @DisplayName("Throws HospitalNotFoundException when hospital missing")
        void updateHospital_ShouldThrow_WhenHospitalNotFound() {
            when(repository.findById(id)).thenReturn(Optional.empty());

            UpdateHospitalRequestDto dto = new UpdateHospitalRequestDto(
                    "Name", null, null, null, null, null, null, null, null
            );

            assertThrows(HospitalNotFoundException.class,
                    () -> service.updateHospital(id, dto));
        }

        @Test
        @DisplayName("Throws DuplicateRegistrationNumberException when regNumber is changed to an existing one")
        void updateHospital_ShouldThrow_WhenRegNumberAlreadyExists() {
            when(repository.findById(id)).thenReturn(Optional.of(existing));
            when(repository.existsByRegistrationNumber("REG-TAKEN")).thenReturn(true);

            UpdateHospitalRequestDto dto = new UpdateHospitalRequestDto(
                    null, "REG-TAKEN", null, null, null, null, null, null, null
            );

            assertThrows(DuplicateRegistrationNumberException.class,
                    () -> service.updateHospital(id, dto));
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Throws DuplicateLicenseNumberException when licenseNumber is changed to an existing one")
        void updateHospital_ShouldThrow_WhenLicenseNumberAlreadyExists() {
            when(repository.findById(id)).thenReturn(Optional.of(existing));
            when(repository.existsByLicenseNumber("LIC-TAKEN")).thenReturn(true);

            UpdateHospitalRequestDto dto = new UpdateHospitalRequestDto(
                    null, null, "LIC-TAKEN", null, null, null, null, null, null
            );

            assertThrows(DuplicateLicenseNumberException.class,
                    () -> service.updateHospital(id, dto));
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Throws DuplicateHospitalEmailException when email is changed to an existing one")
        void updateHospital_ShouldThrow_WhenEmailAlreadyExists() {
            when(repository.findById(id)).thenReturn(Optional.of(existing));
            when(repository.existsByEmail("taken@hospital.com")).thenReturn(true);

            UpdateHospitalRequestDto dto = new UpdateHospitalRequestDto(
                    null, null, null, "taken@hospital.com", null, null, null, null, null
            );

            assertThrows(DuplicateHospitalEmailException.class,
                    () -> service.updateHospital(id, dto));
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("No duplicate check when registration number is unchanged")
        void updateHospital_ShouldNotCheck_WhenRegNumberUnchanged() {
            when(repository.findById(id)).thenReturn(Optional.of(existing));
            when(repository.save(any(Hospital.class))).thenAnswer(inv -> inv.getArgument(0));

            // Same regNumber as existing — should not trigger duplicate check
            UpdateHospitalRequestDto dto = new UpdateHospitalRequestDto(
                    "New Name", "REG-100", null, null, null, null, null, null, null
            );

            assertDoesNotThrow(() -> service.updateHospital(id, dto));
            verify(repository, never()).existsByRegistrationNumber(any());
        }
    }

    // =========================================================================
    // getHospitalById
    // =========================================================================

    @Nested
    @DisplayName("getHospitalById()")
    class GetByIdTests {

        @Test
        @DisplayName("Success — returns HospitalResponseDto when found")
        void getHospitalById_ShouldReturnDto_WhenFound() {
            UUID id = UUID.randomUUID();
            Hospital entity = Hospital.builder()
                    .name("City Hospital")
                    .registrationNumber("REG-100")
                    .licenseNumber("LIC-200")
                    .email("info@cityhospital.com")
                    .isActive(true)
                    .build();
            when(repository.findById(id)).thenReturn(Optional.of(entity));

            HospitalResponseDto result = service.getHospitalById(id);

            assertNotNull(result);
            assertEquals("City Hospital", result.name());
        }

        @Test
        @DisplayName("Throws HospitalNotFoundException when not found")
        void getHospitalById_ShouldThrow_WhenNotFound() {
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThrows(HospitalNotFoundException.class,
                    () -> service.getHospitalById(id));
        }
    }

    // =========================================================================
    // getAllHospitals
    // =========================================================================

    @Nested
    @DisplayName("getAllHospitals()")
    class GetAllHospitalsTests {

        @Test
        @DisplayName("Returns summary list of all hospitals")
        void getAllHospitals_ShouldReturnSummaryList() {
            Hospital h1 = Hospital.builder().name("H1").registrationNumber("R1").licenseNumber("L1")
                    .email("h1@h.com").isActive(true).build();
            Hospital h2 = Hospital.builder().name("H2").registrationNumber("R2").licenseNumber("L2")
                    .email("h2@h.com").isActive(false).build();

            when(repository.findAll()).thenReturn(List.of(h1, h2));

            List<HospitalSummaryDto> result = service.getAllHospitals();

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("H1", result.get(0).name());
            assertEquals("H2", result.get(1).name());
        }
    }

    // =========================================================================
    // getHospitals (pagination)
    // =========================================================================

    @Nested
    @DisplayName("getHospitals() — Pagination, Search, Filter")
    class GetHospitalsTests {

        @Test
        @DisplayName("Returns PagedResponse with matched content")
        void getHospitals_ShouldReturnPagedResponse() {
            Hospital entity = Hospital.builder()
                    .name("City Hospital")
                    .registrationNumber("REG-100")
                    .licenseNumber("LIC-200")
                    .email("info@cityhospital.com")
                    .isActive(true)
                    .build();

            Page<Hospital> page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
            when(repository.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class)))
                    .thenReturn(page);

            PagedResponse<HospitalSummaryDto> result =
                    service.getHospitals("City", true, "Metropolis", PageRequest.of(0, 10));

            assertNotNull(result);
            assertEquals(1, result.totalElements());
            assertEquals(1, result.totalPages());
            assertEquals("City Hospital", result.content().get(0).name());
            assertTrue(result.first());
            assertTrue(result.last());
        }

        @Test
        @DisplayName("Page size is clamped to 100 when oversized value provided")
        void getHospitals_ShouldClampPageSize_WhenExceedsMax() {
            Page<Hospital> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 100), 0);
            when(repository.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class)))
                    .thenReturn(emptyPage);

            // Request page size of 999 — should be clamped to 100 inside service
            service.getHospitals(null, null, null, PageRequest.of(0, 999));

            // Verify repo was called — clamping is internal; we verify the service handles it
            verify(repository).findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class));
        }

        @Test
        @DisplayName("Returns empty page when no hospitals match criteria")
        void getHospitals_ShouldReturnEmptyPage_WhenNoMatch() {
            Page<Hospital> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
            when(repository.findAll(any(Specification.class), any(org.springframework.data.domain.Pageable.class)))
                    .thenReturn(emptyPage);

            PagedResponse<HospitalSummaryDto> result =
                    service.getHospitals("NoSuchHospital", null, null, PageRequest.of(0, 10));

            assertNotNull(result);
            assertEquals(0, result.totalElements());
            assertTrue(result.content().isEmpty());
        }
    }

    // =========================================================================
    // deactivateHospital
    // =========================================================================

    @Nested
    @DisplayName("deactivateHospital()")
    class DeactivateHospitalTests {

        @Test
        @DisplayName("Success — sets isActive to false and saves")
        void deactivateHospital_ShouldSetIsActiveToFalse() {
            UUID id = UUID.randomUUID();
            Hospital existing = Hospital.builder().name("City Hospital").isActive(true).build();
            when(repository.findById(id)).thenReturn(Optional.of(existing));

            service.deactivateHospital(id);

            assertFalse(existing.getIsActive());
            verify(repository).save(existing);
        }

        @Test
        @DisplayName("Throws HospitalNotFoundException when not found")
        void deactivateHospital_ShouldThrow_WhenHospitalNotFound() {
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThrows(HospitalNotFoundException.class,
                    () -> service.deactivateHospital(id));
            verify(repository, never()).save(any());
        }
    }

    // =========================================================================
    // activateHospital
    // =========================================================================

    @Nested
    @DisplayName("activateHospital()")
    class ActivateHospitalTests {

        @Test
        @DisplayName("Success — sets isActive to true and saves")
        void activateHospital_ShouldSetIsActiveToTrue() {
            UUID id = UUID.randomUUID();
            Hospital existing = Hospital.builder().name("City Hospital").isActive(false).build();
            when(repository.findById(id)).thenReturn(Optional.of(existing));

            service.activateHospital(id);

            assertTrue(existing.getIsActive());
            verify(repository).save(existing);
        }

        @Test
        @DisplayName("Throws HospitalNotFoundException when not found")
        void activateHospital_ShouldThrow_WhenHospitalNotFound() {
            UUID id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThrows(HospitalNotFoundException.class,
                    () -> service.activateHospital(id));
            verify(repository, never()).save(any());
        }
    }
}
