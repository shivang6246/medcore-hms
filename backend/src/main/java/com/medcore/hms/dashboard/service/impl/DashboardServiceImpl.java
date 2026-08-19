package com.medcore.hms.dashboard.service.impl;

import com.medcore.hms.appointment.dto.AppointmentResponseDto;
import com.medcore.hms.appointment.entity.Appointment;
import com.medcore.hms.appointment.entity.AppointmentStatus;
import com.medcore.hms.appointment.mapper.AppointmentMapper;
import com.medcore.hms.appointment.repository.AppointmentRepository;
import com.medcore.hms.billing.entity.InvoiceStatus;
import com.medcore.hms.billing.repository.InvoiceRepository;
import com.medcore.hms.billing.repository.PaymentRepository;
import com.medcore.hms.dashboard.dto.*;
import com.medcore.hms.dashboard.service.DashboardService;
import com.medcore.hms.doctor.entity.Gender;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.ipd.dto.BedResponseDto;
import com.medcore.hms.ipd.entity.Bed;
import com.medcore.hms.ipd.entity.BedStatus;
import com.medcore.hms.ipd.mapper.IpdMapper;
import com.medcore.hms.ipd.repository.AdmissionRepository;
import com.medcore.hms.ipd.repository.BedRepository;
import com.medcore.hms.medicalrecord.dto.MedicalRecordSummaryDto;
import com.medcore.hms.medicalrecord.entity.MedicalRecord;
import com.medcore.hms.medicalrecord.mapper.MedicalRecordMapper;
import com.medcore.hms.medicalrecord.repository.MedicalRecordRepository;
import com.medcore.hms.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final BedRepository bedRepository;
    private final AdmissionRepository admissionRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    private final AppointmentMapper appointmentMapper;
    private final IpdMapper ipdMapper;
    private final MedicalRecordMapper medicalRecordMapper;

    @Override
    public AdminDashboardDto getAdminDashboard(UUID hospitalId) {
        log.info("Aggregating Admin Dashboard metrics for hospital ID: {}", hospitalId);

        long totalPatients = patientRepository.count();
        long totalDoctors = doctorRepository.count();

        LocalDate today = LocalDate.now();
        long todayAppts = appointmentRepository.countByAppointmentDate(today);

        LocalDate startOfMonth = today.withDayOfMonth(1);
        BigDecimal monthlyRevenue = paymentRepository.calculateTotalCollectedBetween(
                startOfMonth.atStartOfDay(), today.atTime(LocalTime.MAX)
        );

        BigDecimal totalOutstanding = invoiceRepository.calculateTotalOutstandingBalance();

        long totalBeds = bedRepository.count();
        long availableBeds = bedRepository.findByStatusAndIsActiveTrue(BedStatus.AVAILABLE).size();
        long occupiedBeds = totalBeds - availableBeds;
        if (occupiedBeds < 0) occupiedBeds = 0;

        double occupancyRate = totalBeds > 0 ? ((double) occupiedBeds / totalBeds) * 100.0 : 0.0;

        List<AnalyticsTrendDto> revenueTrends = getRevenueAnalytics(hospitalId, 12);

        long malePatients = patientRepository.countByGender(Gender.MALE);
        long femalePatients = patientRepository.countByGender(Gender.FEMALE);
        long otherPatients = patientRepository.countByGender(Gender.OTHER)
                + patientRepository.countByGender(Gender.PREFER_NOT_TO_SAY);

        return new AdminDashboardDto(
                totalPatients,
                totalDoctors,
                todayAppts,
                monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO,
                totalOutstanding != null ? totalOutstanding : BigDecimal.ZERO,
                Math.round(occupancyRate * 10.0) / 10.0,
                totalBeds,
                occupiedBeds,
                malePatients,
                femalePatients,
                otherPatients,
                revenueTrends,
                List.of(),
                List.of()
        );
    }

    @Override
    public DoctorDashboardDto getDoctorDashboard(UUID doctorId) {
        log.info("Aggregating Doctor Dashboard metrics for doctor ID: {}", doctorId);

        LocalDate today = LocalDate.now();
        List<Appointment> todayAppts = appointmentRepository.findByDoctor_IdAndAppointmentDateOrderByStartTime(doctorId, today);

        long totalToday = todayAppts.size();
        long pendingToday = todayAppts.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED || a.getStatus() == AppointmentStatus.CONFIRMED)
                .count();
        long completedToday = todayAppts.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED)
                .count();

        List<AppointmentResponseDto> todaySchedule = todayAppts.stream()
                .map(appointmentMapper::toResponseDto)
                .toList();

        List<MedicalRecord> records = medicalRecordRepository.findByDoctor_Id(doctorId, PageRequest.of(0, 5)).getContent();
        List<MedicalRecordSummaryDto> recentRecords = records.stream()
                .map(medicalRecordMapper::toSummaryDto)
                .toList();

        List<AnalyticsTrendDto> weeklyTrend = getAppointmentAnalytics(null, 7);

        return new DoctorDashboardDto(
                totalToday,
                pendingToday,
                completedToday,
                totalToday * 3L, // Sample metrics aggregation
                todaySchedule,
                recentRecords,
                weeklyTrend
        );
    }

    @Override
    public ReceptionDashboardDto getReceptionDashboard(UUID hospitalId) {
        log.info("Aggregating Reception Dashboard metrics for hospital ID: {}", hospitalId);

        LocalDate today = LocalDate.now();
        List<Appointment> todayAppts = appointmentRepository.findAll().stream()
                .filter(a -> a.getAppointmentDate().equals(today))
                .toList();

        long todayTotal = todayAppts.size();
        long todayCheckedIn = todayAppts.stream().filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED || a.getStatus() == AppointmentStatus.IN_PROGRESS).count();
        long todayPending = todayAppts.stream().filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED).count();

        long totalPatients = patientRepository.count();

        List<Bed> availBedsList = bedRepository.findByStatusAndIsActiveTrue(BedStatus.AVAILABLE);
        long availableBedsCount = availBedsList.size();
        long totalBeds = bedRepository.count();
        long occupiedBedsCount = totalBeds - availableBedsCount;
        if (occupiedBedsCount < 0) occupiedBedsCount = 0;

        long openInvoicesCount = invoiceRepository.findByStatus(InvoiceStatus.UNPAID, PageRequest.of(0, 100)).getTotalElements();

        List<BedResponseDto> bedDtos = availBedsList.stream()
                .limit(5)
                .map(ipdMapper::toBedResponseDto)
                .toList();

        return new ReceptionDashboardDto(
                todayTotal,
                todayCheckedIn,
                todayPending,
                totalPatients,
                availableBedsCount,
                occupiedBedsCount,
                openInvoicesCount,
                bedDtos
        );
    }

    @Override
    public List<AnalyticsTrendDto> getRevenueAnalytics(UUID hospitalId, int months) {
        log.info("Generating revenue analytics trends for past {} months", months);

        List<AnalyticsTrendDto> trends = new ArrayList<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");

        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            LocalDate start = monthDate.withDayOfMonth(1);
            LocalDate end = monthDate.withDayOfMonth(monthDate.lengthOfMonth());

            BigDecimal collected = paymentRepository.calculateTotalCollectedBetween(
                    start.atStartOfDay(), end.atTime(LocalTime.MAX)
            );

            trends.add(new AnalyticsTrendDto(
                    monthDate.format(formatter),
                    collected != null ? collected : BigDecimal.ZERO,
                    0L
            ));
        }

        return trends;
    }

    @Override
    public List<AnalyticsTrendDto> getAppointmentAnalytics(UUID hospitalId, int days) {
        log.info("Generating appointment volume analytics trends for past {} days", days);

        List<AnalyticsTrendDto> trends = new ArrayList<>();
        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE dd");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = now.minusDays(i);
            long count = appointmentRepository.countByAppointmentDate(date);

            trends.add(new AnalyticsTrendDto(
                    date.format(formatter),
                    BigDecimal.valueOf(count),
                    count
            ));
        }

        return trends;
    }
}
