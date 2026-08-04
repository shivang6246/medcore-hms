package com.medcore.hms.ipd.service.impl;

import com.medcore.hms.common.dto.PagedResponse;
import com.medcore.hms.doctor.entity.Doctor;
import com.medcore.hms.doctor.exception.DoctorNotFoundException;
import com.medcore.hms.doctor.repository.DoctorRepository;
import com.medcore.hms.ipd.dto.*;
import com.medcore.hms.ipd.entity.*;
import com.medcore.hms.ipd.exception.*;
import com.medcore.hms.ipd.mapper.IpdMapper;
import com.medcore.hms.ipd.repository.*;
import com.medcore.hms.ipd.service.IpdService;
import com.medcore.hms.patient.entity.Patient;
import com.medcore.hms.patient.exception.PatientNotFoundException;
import com.medcore.hms.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IpdServiceImpl implements IpdService {

    private final AdmissionRepository admissionRepository;
    private final DischargeSummaryRepository dischargeSummaryRepository;
    private final WardRepository wardRepository;
    private final RoomRepository roomRepository;
    private final BedRepository bedRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final IpdMapper ipdMapper;

    @Override
    @Transactional
    public AdmissionResponseDto createAdmission(CreateAdmissionRequestDto dto) {
        log.info("Admitting patient ID: {} to bed ID: {}", dto.patientId(), dto.bedId());

        Patient patient = patientRepository.findById(dto.patientId())
                .orElseThrow(() -> new PatientNotFoundException(dto.patientId()));

        if (admissionRepository.existsActiveAdmissionForPatient(dto.patientId())) {
            throw new ActiveAdmissionExistsException(dto.patientId());
        }

        Doctor doctor = doctorRepository.findById(dto.doctorId())
                .orElseThrow(() -> new DoctorNotFoundException(dto.doctorId()));

        Ward ward = wardRepository.findById(dto.wardId())
                .orElseThrow(() -> new WardNotFoundException(dto.wardId()));

        Room room = roomRepository.findById(dto.roomId())
                .orElseThrow(() -> new RoomNotFoundException(dto.roomId()));

        Bed bed = bedRepository.findById(dto.bedId())
                .orElseThrow(() -> new BedNotFoundException(dto.bedId()));

        if (bed.getStatus() != BedStatus.AVAILABLE) {
            throw new BedNotAvailableException(dto.bedId());
        }

        // Occupy bed
        bed.setStatus(BedStatus.OCCUPIED);
        bedRepository.save(bed);

        String admNum = "ADM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Admission admission = Admission.builder()
                .admissionNumber(admNum)
                .patient(patient)
                .doctor(doctor)
                .ward(ward)
                .room(room)
                .bed(bed)
                .admissionDate(dto.admissionDate() != null ? dto.admissionDate() : LocalDateTime.now())
                .expectedDischargeDate(dto.expectedDischargeDate())
                .reason(dto.reason())
                .status(AdmissionStatus.ADMITTED)
                .isActive(true)
                .build();

        Admission savedAdmission = admissionRepository.save(admission);
        log.info("Patient admitted successfully with reference number: {}", admNum);
        return ipdMapper.toAdmissionResponseDto(savedAdmission);
    }

    @Override
    public AdmissionResponseDto getAdmissionById(UUID id) {
        log.info("Fetching admission by ID: {}", id);

        Admission admission = admissionRepository.findById(id)
                .orElseThrow(() -> new AdmissionNotFoundException(id));

        return ipdMapper.toAdmissionResponseDto(admission);
    }

    @Override
    @Transactional
    public AdmissionResponseDto transferBed(UUID admissionId, TransferBedRequestDto dto) {
        log.info("Transferring admission ID: {} to new bed ID: {}", admissionId, dto.newBedId());

        Admission admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new AdmissionNotFoundException(admissionId));

        if (admission.getStatus() == AdmissionStatus.DISCHARGED || admission.getStatus() == AdmissionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot transfer patient with status: " + admission.getStatus());
        }

        Ward newWard = wardRepository.findById(dto.newWardId())
                .orElseThrow(() -> new WardNotFoundException(dto.newWardId()));

        Room newRoom = roomRepository.findById(dto.newRoomId())
                .orElseThrow(() -> new RoomNotFoundException(dto.newRoomId()));

        Bed newBed = bedRepository.findById(dto.newBedId())
                .orElseThrow(() -> new BedNotFoundException(dto.newBedId()));

        if (newBed.getStatus() != BedStatus.AVAILABLE) {
            throw new BedNotAvailableException(dto.newBedId());
        }

        // Release old bed
        Bed oldBed = admission.getBed();
        if (oldBed != null) {
            oldBed.setStatus(BedStatus.AVAILABLE);
            bedRepository.save(oldBed);
        }

        // Occupy new bed
        newBed.setStatus(BedStatus.OCCUPIED);
        bedRepository.save(newBed);

        admission.setWard(newWard);
        admission.setRoom(newRoom);
        admission.setBed(newBed);
        admission.setStatus(AdmissionStatus.TRANSFERRED);
        if (dto.reason() != null && !dto.reason().isBlank()) {
            admission.setReason(admission.getReason() + " | Transfer: " + dto.reason());
        }

        Admission updatedAdmission = admissionRepository.save(admission);
        log.info("Patient transfer completed for admission ID: {}", admissionId);
        return ipdMapper.toAdmissionResponseDto(updatedAdmission);
    }

    @Override
    @Transactional
    public AdmissionResponseDto dischargePatient(UUID admissionId, DischargeRequestDto dto) {
        log.info("Discharging patient for admission ID: {}", admissionId);

        Admission admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new AdmissionNotFoundException(admissionId));

        if (admission.getStatus() == AdmissionStatus.DISCHARGED) {
            throw new IllegalStateException("Patient is already DISCHARGED.");
        }

        Doctor attendingDoctor = dto.attendingDoctorId() != null
                ? doctorRepository.findById(dto.attendingDoctorId()).orElse(admission.getDoctor())
                : admission.getDoctor();

        LocalDateTime dischargeTime = dto.dischargeDate() != null ? dto.dischargeDate() : LocalDateTime.now();

        // Release bed
        Bed bed = admission.getBed();
        if (bed != null) {
            bed.setStatus(BedStatus.AVAILABLE);
            bedRepository.save(bed);
        }

        admission.setDischargeDate(dischargeTime);
        admission.setStatus(AdmissionStatus.DISCHARGED);

        DischargeSummary summary = DischargeSummary.builder()
                .admission(admission)
                .attendingDoctor(attendingDoctor)
                .dischargeDate(dischargeTime)
                .finalDiagnosis(dto.finalDiagnosis())
                .treatmentSummary(dto.treatmentSummary())
                .dischargeNotes(dto.dischargeNotes())
                .followUpInstructions(dto.followUpInstructions())
                .build();

        DischargeSummary savedSummary = dischargeSummaryRepository.save(summary);
        admission.setDischargeSummary(savedSummary);

        Admission finalAdmission = admissionRepository.save(admission);
        log.info("Patient discharged successfully for admission ID: {}", admissionId);
        return ipdMapper.toAdmissionResponseDto(finalAdmission);
    }

    @Override
    public PagedResponse<BedResponseDto> getAvailableBeds(UUID wardId, Pageable pageable) {
        log.info("Fetching available beds (ward filter: {})", wardId);

        if (wardId != null) {
            List<Bed> beds = bedRepository.findAvailableBedsByWard(wardId);
            Page<Bed> page = new PageImpl<>(beds, pageable, beds.size());
            return PagedResponse.from(page.map(ipdMapper::toBedResponseDto));
        } else {
            Page<Bed> page = bedRepository.findByStatusAndIsActiveTrue(BedStatus.AVAILABLE, pageable);
            return PagedResponse.from(page.map(ipdMapper::toBedResponseDto));
        }
    }

    @Override
    public PagedResponse<AdmissionSummaryDto> getAllAdmissions(AdmissionStatus status, Pageable pageable) {
        log.info("Fetching admissions paginated (status filter: {})", status);

        Page<Admission> page = status != null
                ? admissionRepository.findByStatus(status, pageable)
                : admissionRepository.findAll(pageable);

        return PagedResponse.from(page.map(ipdMapper::toAdmissionSummaryDto));
    }
}
