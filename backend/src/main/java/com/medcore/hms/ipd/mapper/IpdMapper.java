package com.medcore.hms.ipd.mapper;

import com.medcore.hms.ipd.dto.*;
import com.medcore.hms.ipd.entity.*;
import org.springframework.stereotype.Component;

@Component
public class IpdMapper {

    public AdmissionResponseDto toAdmissionResponseDto(Admission a) {
        if (a == null) return null;

        String patientName = a.getPatient() != null
                ? a.getPatient().getFirstName() + " " + a.getPatient().getLastName()
                : "Unknown Patient";

        String doctorName = (a.getDoctor() != null && a.getDoctor().getUser() != null)
                ? a.getDoctor().getUser().getFirstName() + " " + a.getDoctor().getUser().getLastName()
                : "Unknown Doctor";

        String wardName = a.getWard() != null ? a.getWard().getName() : null;
        String roomNumber = a.getRoom() != null ? a.getRoom().getRoomNumber() : null;
        String bedNumber = a.getBed() != null ? a.getBed().getBedNumber() : null;

        DischargeSummaryResponseDto dsDto = toDischargeSummaryResponseDto(a.getDischargeSummary());

        return new AdmissionResponseDto(
                a.getId(),
                a.getAdmissionNumber(),
                a.getPatient() != null ? a.getPatient().getId() : null,
                patientName,
                a.getDoctor() != null ? a.getDoctor().getId() : null,
                doctorName,
                wardName,
                roomNumber,
                bedNumber,
                a.getAdmissionDate(),
                a.getExpectedDischargeDate(),
                a.getDischargeDate(),
                a.getReason(),
                a.getStatus(),
                dsDto
        );
    }

    public AdmissionSummaryDto toAdmissionSummaryDto(Admission a) {
        if (a == null) return null;

        String patientName = a.getPatient() != null
                ? a.getPatient().getFirstName() + " " + a.getPatient().getLastName()
                : "Unknown Patient";

        String doctorName = (a.getDoctor() != null && a.getDoctor().getUser() != null)
                ? a.getDoctor().getUser().getFirstName() + " " + a.getDoctor().getUser().getLastName()
                : "Unknown Doctor";

        String bedLoc = String.format("%s / Room %s / Bed %s",
                a.getWard() != null ? a.getWard().getName() : "N/A",
                a.getRoom() != null ? a.getRoom().getRoomNumber() : "N/A",
                a.getBed() != null ? a.getBed().getBedNumber() : "N/A");

        return new AdmissionSummaryDto(
                a.getId(),
                a.getAdmissionNumber(),
                patientName,
                doctorName,
                bedLoc,
                a.getAdmissionDate(),
                a.getStatus()
        );
    }

    public BedResponseDto toBedResponseDto(Bed b) {
        if (b == null) return null;
        String roomNum = b.getRoom() != null ? b.getRoom().getRoomNumber() : null;
        String wardName = (b.getRoom() != null && b.getRoom().getWard() != null) ? b.getRoom().getWard().getName() : null;
        return new BedResponseDto(
                b.getId(),
                b.getRoom() != null ? b.getRoom().getId() : null,
                roomNum,
                wardName,
                b.getBedNumber(),
                b.getStatus(),
                b.getDailyRate(),
                b.isActive()
        );
    }

    public WardResponseDto toWardResponseDto(Ward w) {
        if (w == null) return null;
        return new WardResponseDto(
                w.getId(),
                w.getName(),
                w.getCategory(),
                w.getCapacity(),
                w.getDescription(),
                w.isActive()
        );
    }

    public DischargeSummaryResponseDto toDischargeSummaryResponseDto(DischargeSummary ds) {
        if (ds == null) return null;
        String docName = (ds.getAttendingDoctor() != null && ds.getAttendingDoctor().getUser() != null)
                ? ds.getAttendingDoctor().getUser().getFirstName() + " " + ds.getAttendingDoctor().getUser().getLastName()
                : null;
        return new DischargeSummaryResponseDto(
                ds.getId(),
                ds.getAdmission() != null ? ds.getAdmission().getId() : null,
                docName,
                ds.getDischargeDate(),
                ds.getFinalDiagnosis(),
                ds.getTreatmentSummary(),
                ds.getDischargeNotes(),
                ds.getFollowUpInstructions(),
                ds.getFinalInvoice() != null ? ds.getFinalInvoice().getId() : null
        );
    }
}
