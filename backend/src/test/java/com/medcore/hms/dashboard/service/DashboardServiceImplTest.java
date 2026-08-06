package com.medcore.hms.dashboard.service;

import com.medcore.hms.appointment.mapper.AppointmentMapper;
import com.medcore.hms.appointment.repository.AppointmentRepository;
import com.medcore.hms.billing.repository.InvoiceRepository;
import com.medcore.hms.billing.repository.PaymentRepository;
import com.medcore.hms.dashboard.dto.AdminDashboardDto;
import com.medcore.hms.dashboard.dto.DoctorDashboardDto;
import com.medcore.hms.dashboard.dto.ReceptionDashboardDto;
import com.medcore.hms.dashboard.service.impl.DashboardServiceImpl;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.ipd.mapper.IpdMapper;
import com.medcore.hms.ipd.repository.AdmissionRepository;
import com.medcore.hms.ipd.repository.BedRepository;
import com.medcore.hms.medicalrecord.mapper.MedicalRecordMapper;
import com.medcore.hms.medicalrecord.repository.MedicalRecordRepository;
import com.medcore.hms.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService Unit Tests")
class DashboardServiceImplTest {

    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private BedRepository bedRepository;
    @Mock private AdmissionRepository admissionRepository;
    @Mock private MedicalRecordRepository medicalRecordRepository;

    @Mock private AppointmentMapper appointmentMapper;
    @Mock private IpdMapper ipdMapper;
    @Mock private MedicalRecordMapper medicalRecordMapper;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private UUID hospitalId;
    private UUID doctorId;

    @BeforeEach
    void setUp() {
        hospitalId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should successfully aggregate Admin Dashboard metrics")
    void getAdminDashboard_Success() {
        when(patientRepository.count()).thenReturn(150L);
        when(doctorRepository.count()).thenReturn(20L);
        when(appointmentRepository.countByAppointmentDate(any(LocalDate.class))).thenReturn(15L);
        when(paymentRepository.calculateTotalCollectedBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("25000.00"));
        when(invoiceRepository.calculateTotalOutstandingBalance()).thenReturn(new BigDecimal("5000.00"));
        when(bedRepository.count()).thenReturn(50L);

        AdminDashboardDto result = dashboardService.getAdminDashboard(hospitalId);

        assertThat(result).isNotNull();
        assertThat(result.totalPatients()).isEqualTo(150L);
        assertThat(result.totalActiveDoctors()).isEqualTo(20L);
        assertThat(result.todayAppointmentsCount()).isEqualTo(15L);
        assertThat(result.monthlyRevenue()).isEqualByComparingTo("25000.00");
    }

    @Test
    @DisplayName("Should successfully aggregate Doctor Dashboard metrics")
    void getDoctorDashboard_Success() {
        when(appointmentRepository.findByDoctor_IdAndAppointmentDateOrderByStartTime(eq(doctorId), any(LocalDate.class)))
                .thenReturn(List.of());
        when(medicalRecordRepository.findByDoctor_Id(eq(doctorId), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of()));

        DoctorDashboardDto result = dashboardService.getDoctorDashboard(doctorId);

        assertThat(result).isNotNull();
        assertThat(result.todayTotalAppointments()).isZero();
    }

    @Test
    @DisplayName("Should successfully aggregate Receptionist Dashboard metrics")
    void getReceptionDashboard_Success() {
        when(patientRepository.count()).thenReturn(150L);
        when(bedRepository.count()).thenReturn(50L);
        when(bedRepository.findByStatusAndIsActiveTrue(any())).thenReturn(List.of());
        when(appointmentRepository.findAll()).thenReturn(List.of());
        when(invoiceRepository.findByStatus(any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        ReceptionDashboardDto result = dashboardService.getReceptionDashboard(hospitalId);

        assertThat(result).isNotNull();
        assertThat(result.totalRegisteredPatients()).isEqualTo(150L);
        assertThat(result.openInvoicesCount()).isZero();
    }
}
