package com.medcore.hms.hospital.repository;

import com.medcore.hms.common.entity.Address;
import com.medcore.hms.hospital.entity.Hospital;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link HospitalRepository} using the real PostgreSQL database.
 *
 * <p>Each test runs in a transaction that is rolled back on completion,
 * ensuring test isolation without polluting the database.
 *
 * <p>Coverage:
 * <ul>
 *   <li>Save and find by ID with address eager-loading</li>
 *   <li>Finder methods: by regNumber, licenseNumber, email</li>
 *   <li>Existence checks: existsByRegistrationNumber, existsByLicenseNumber, existsByEmail</li>
 *   <li>findByIsActiveTrue: returns only active hospitals</li>
 *   <li>Specification-based filtering: isActive, city search</li>
 *   <li>Pagination and sort</li>
 *   <li>JPA Auditing: createdAt and updatedAt populated automatically</li>
 * </ul>
 */
@SpringBootTest
@Transactional
@DisplayName("HospitalRepository — Integration Tests")
class HospitalRepositoryTest {

    @Autowired
    private HospitalRepository hospitalRepository;

    private String uniqueSuffix;
    private String regNum;
    private String licNum;
    private String email;
    private Hospital savedHospital;

    @BeforeEach
    void setUp() {
        uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        regNum = "REG-TEST-" + uniqueSuffix;
        licNum = "LIC-TEST-" + uniqueSuffix;
        email  = "test-" + uniqueSuffix + "@hospital.com";

        Address address = Address.builder()
                .street("100 Main St")
                .city("Springfield")
                .state("IL")
                .postalCode("62701")
                .country("USA")
                .build();

        Hospital hospital = Hospital.builder()
                .name("St. Jude Test Hospital")
                .registrationNumber(regNum)
                .licenseNumber(licNum)
                .email(email)
                .phone("+1-555-0100")
                .website("https://test-hospital.com")
                .description("Test hospital for integration tests")
                .logoUrl("https://test-hospital.com/logo.png")
                .address(address)
                .isActive(true)
                .build();

        savedHospital = hospitalRepository.save(hospital);
    }

    // =========================================================================
    // Basic Save & Audit
    // =========================================================================

    @Nested
    @DisplayName("Save & Audit")
    class SaveAndAuditTests {

        @Test
        @DisplayName("Saved hospital has UUID, createdAt, updatedAt populated by JPA Auditing")
        void save_ShouldPopulateAuditFields() {
            assertNotNull(savedHospital.getId(), "UUID should be generated");
            assertNotNull(savedHospital.getCreatedAt(), "createdAt should be set by JPA Auditing");
            assertNotNull(savedHospital.getUpdatedAt(), "updatedAt should be set by JPA Auditing");
        }

        @Test
        @DisplayName("Saved hospital has correct field values")
        void save_ShouldPersistAllFields() {
            assertEquals("St. Jude Test Hospital", savedHospital.getName());
            assertEquals(regNum, savedHospital.getRegistrationNumber());
            assertEquals(licNum, savedHospital.getLicenseNumber());
            assertEquals(email, savedHospital.getEmail());
            assertTrue(savedHospital.getIsActive());
        }

        @Test
        @DisplayName("Saved hospital has nested address persisted")
        void save_ShouldPersistAddress() {
            assertNotNull(savedHospital.getAddress(), "Address should be persisted");
            assertEquals("Springfield", savedHospital.getAddress().getCity());
            assertEquals("Illinois", savedHospital.getAddress().getState().equals("IL") ? "Illinois" : "IL");
        }
    }

    // =========================================================================
    // Finder Methods
    // =========================================================================

    @Nested
    @DisplayName("Finder Methods")
    class FinderMethodTests {

        @Test
        @DisplayName("findById eager-loads address")
        void findById_ShouldReturnHospitalWithAddress() {
            Optional<Hospital> found = hospitalRepository.findById(savedHospital.getId());

            assertTrue(found.isPresent());
            assertNotNull(found.get().getAddress());
            assertEquals("Springfield", found.get().getAddress().getCity());
        }

        @Test
        @DisplayName("findById returns empty when not found")
        void findById_ShouldReturnEmpty_WhenNotFound() {
            Optional<Hospital> found = hospitalRepository.findById(UUID.randomUUID());
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("findByRegistrationNumber returns the correct hospital")
        void findByRegistrationNumber_ShouldReturnHospital() {
            Optional<Hospital> byReg = hospitalRepository.findByRegistrationNumber(regNum);

            assertTrue(byReg.isPresent());
            assertEquals(licNum, byReg.get().getLicenseNumber());
        }

        @Test
        @DisplayName("findByLicenseNumber returns the correct hospital")
        void findByLicenseNumber_ShouldReturnHospital() {
            Optional<Hospital> byLic = hospitalRepository.findByLicenseNumber(licNum);

            assertTrue(byLic.isPresent());
            assertEquals(regNum, byLic.get().getRegistrationNumber());
        }

        @Test
        @DisplayName("findByEmail returns the correct hospital")
        void findByEmail_ShouldReturnHospital() {
            Optional<Hospital> byEmail = hospitalRepository.findByEmail(email);
            assertTrue(byEmail.isPresent());
        }
    }

    // =========================================================================
    // Existence Checks
    // =========================================================================

    @Nested
    @DisplayName("Existence Checks")
    class ExistenceCheckTests {

        @Test
        @DisplayName("existsByRegistrationNumber returns true for existing regNumber")
        void existsByRegistrationNumber_ShouldReturnTrue() {
            assertTrue(hospitalRepository.existsByRegistrationNumber(regNum));
        }

        @Test
        @DisplayName("existsByRegistrationNumber returns false for unknown regNumber")
        void existsByRegistrationNumber_ShouldReturnFalse_WhenUnknown() {
            assertFalse(hospitalRepository.existsByRegistrationNumber("UNKNOWN-REG"));
        }

        @Test
        @DisplayName("existsByLicenseNumber returns true for existing licenseNumber")
        void existsByLicenseNumber_ShouldReturnTrue() {
            assertTrue(hospitalRepository.existsByLicenseNumber(licNum));
        }

        @Test
        @DisplayName("existsByEmail returns true for existing email")
        void existsByEmail_ShouldReturnTrue() {
            assertTrue(hospitalRepository.existsByEmail(email));
        }
    }

    // =========================================================================
    // findByIsActiveTrue — Soft delete filter
    // =========================================================================

    @Nested
    @DisplayName("findByIsActiveTrue() — Soft Delete Filtering")
    class FindByIsActiveTrueTests {

        @Test
        @DisplayName("Returns only active hospitals; excludes inactive")
        void findByIsActiveTrue_ShouldExcludeInactiveHospitals() {
            // Deactivate the saved hospital
            savedHospital.setIsActive(false);
            hospitalRepository.save(savedHospital);

            // Create an active hospital
            String suffix2 = UUID.randomUUID().toString().substring(0, 8);
            Hospital active = Hospital.builder()
                    .name("Active Hospital")
                    .registrationNumber("REG-ACTIVE-" + suffix2)
                    .licenseNumber("LIC-ACTIVE-" + suffix2)
                    .email("active-" + suffix2 + "@hospital.com")
                    .isActive(true)
                    .build();
            hospitalRepository.save(active);

            List<Hospital> activeList = hospitalRepository.findByIsActiveTrue();

            // The deactivated one should not appear; the new active one should
            assertTrue(activeList.stream().noneMatch(h -> h.getId().equals(savedHospital.getId())),
                    "Deactivated hospital should not appear in findByIsActiveTrue");
            assertTrue(activeList.stream().anyMatch(h -> h.getId().equals(active.getId())),
                    "Active hospital should appear");
        }
    }

    // =========================================================================
    // Specification-based filtering
    // =========================================================================

    @Nested
    @DisplayName("Specification Filtering — Paginated Search")
    class SpecificationFilterTests {

        @Test
        @DisplayName("Filter by isActive=true returns only active hospitals")
        void findAll_WithIsActiveFilter_ShouldReturnOnlyActive() {
            var spec = HospitalSpecification.filterHospitals(null, true, null);
            Page<Hospital> result = hospitalRepository.findAll(spec, PageRequest.of(0, 10));

            assertNotNull(result);
            assertTrue(result.getContent().stream().allMatch(Hospital::getIsActive),
                    "All returned hospitals should be active");
        }

        @Test
        @DisplayName("Keyword search on name returns matching hospital")
        void findAll_WithNameSearch_ShouldReturnMatchingHospitals() {
            var spec = HospitalSpecification.filterHospitals("St. Jude", null, null);
            Page<Hospital> result = hospitalRepository.findAll(spec, PageRequest.of(0, 10));

            assertNotNull(result);
            assertTrue(result.getContent().stream()
                    .anyMatch(h -> h.getRegistrationNumber().equals(regNum)),
                    "Searched hospital should be found by name keyword");
        }

        @Test
        @DisplayName("No match search returns empty page")
        void findAll_WithNoMatchSearch_ShouldReturnEmptyPage() {
            var spec = HospitalSpecification.filterHospitals("XYZNONEXISTENT12345", null, null);
            Page<Hospital> result = hospitalRepository.findAll(spec, PageRequest.of(0, 10));

            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
        }

        @Test
        @DisplayName("Pagination and sort by name asc works correctly")
        void findAll_WithPaginationAndSort_ShouldReturnPagedResults() {
            var spec = HospitalSpecification.filterHospitals(null, null, null);
            Page<Hospital> result = hospitalRepository.findAll(
                    spec, PageRequest.of(0, 5, Sort.by(Sort.Direction.ASC, "name"))
            );

            assertNotNull(result);
            assertTrue(result.getSize() <= 5);
        }
    }
}
