import React, { useState, useEffect } from 'react';
import toast from 'react-hot-toast';
import { patientApi } from '../../api/patient.api';
import { doctorApi } from '../../api/doctor.api';
import { appointmentApi } from '../../api/appointment.api';
import useAuthStore from '../../store/authStore';

const unwrap = (res) => res?.data?.data ?? res?.data;
const unwrapList = (res) => {
  const data = unwrap(res);
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  return [];
};

const ACTIVE_STATUSES = new Set(['SCHEDULED', 'CONFIRMED', 'CHECKED_IN', 'IN_PROGRESS', 'WAITING']);

/** Prefer an active/upcoming appointment; otherwise the most recent. */
function pickBestAppointment(appointments) {
  if (!appointments?.length) return null;
  const active = appointments.find((a) => ACTIVE_STATUSES.has(String(a.status || '').toUpperCase()));
  return active || appointments[0];
}

async function resolveDoctorFromAppointment(appointment) {
  if (!appointment?.doctorId) return null;

  // Prefer fields already on the appointment summary (no extra round-trip).
  if (appointment.doctorEmployeeId) {
    const parts = String(appointment.doctorName || '').trim().split(/\s+/);
    return {
      id: appointment.doctorId,
      employeeId: appointment.doctorEmployeeId,
      firstName: parts[0] || '',
      lastName: parts.slice(1).join(' ') || '',
      specialization: appointment.doctorSpecialization || undefined,
    };
  }

  try {
    const res = await doctorApi.getById(appointment.doctorId);
    const d = unwrap(res);
    if (d?.id) return d;
  } catch {
    /* fall through */
  }

  const parts = String(appointment.doctorName || '').trim().split(/\s+/);
  return {
    id: appointment.doctorId,
    employeeId: '',
    firstName: parts[0] || '',
    lastName: parts.slice(1).join(' ') || '',
  };
}

/**
 * Look up a patient by hospital patient ID (e.g. P-2026-00001).
 * Optionally auto-fills doctor + appointment from the patient's bookings via onContext.
 */
export function PatientIdLookup({ onResolved, onCleared, onContext }) {
  const { user } = useAuthStore();
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [patient, setPatient] = useState(null);

  const lookup = async () => {
    const trimmed = code.trim();
    if (!trimmed) {
      toast.error('Enter a patient ID (e.g. P-2026-00001)');
      return;
    }
    if (!user?.hospitalId) {
      toast.error('Your account has no hospital context. Re-login as hospital staff.');
      return;
    }
    setLoading(true);
    try {
      const res = await patientApi.getByPatientId(trimmed, user.hospitalId);
      const p = unwrap(res);
      if (!p?.id) {
        toast.error('Patient not found');
        setPatient(null);
        onCleared?.();
        onContext?.({ appointmentId: '', doctor: null });
        return;
      }
      setPatient(p);
      onResolved?.(p);

      let appointment = null;
      let doctor = null;
      try {
        const apptRes = await appointmentApi.getByPatient(p.id, {
          page: 0, size: 50, sort: 'appointmentDate,desc',
        });
        const list = unwrapList(apptRes);
        appointment = pickBestAppointment(list);
        doctor = await resolveDoctorFromAppointment(appointment);
      } catch {
        appointment = null;
        doctor = null;
      }

      onContext?.({
        appointmentId: appointment?.id || '',
        appointment,
        doctor,
      });

      toast.success(
        doctor
          ? `Found ${p.firstName} ${p.lastName} · doctor auto-filled`
          : `Found ${p.firstName} ${p.lastName}`,
      );
    } catch (err) {
      setPatient(null);
      onCleared?.();
      onContext?.({ appointmentId: '', doctor: null });
      toast.error(err.response?.data?.detail ?? err.response?.data?.message ?? 'Patient lookup failed');
    } finally {
      setLoading(false);
    }
  };

  const clear = () => {
    setCode('');
    setPatient(null);
    onCleared?.();
    onContext?.({ appointmentId: '', doctor: null });
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
      <label className="form-label">Patient ID *</label>
      <div style={{ display: 'flex', gap: '0.5rem' }}>
        <input
          required
          value={code}
          onChange={(e) => setCode(e.target.value)}
          placeholder="e.g. P-2026-00001"
          style={{ flex: 1 }}
        />
        <button type="button" className="btn btn-secondary" onClick={lookup} disabled={loading}>
          {loading ? 'Looking up…' : 'Lookup'}
        </button>
        {patient && (
          <button type="button" className="btn btn-secondary" onClick={clear}>Clear</button>
        )}
      </div>
      {patient && (
        <div style={{ padding: '0.65rem 0.85rem', background: 'rgba(255,255,255,0.04)', borderRadius: '8px', fontSize: '0.9rem' }}>
          <strong>{patient.firstName} {patient.lastName}</strong>
          <span style={{ color: 'var(--text-muted)' }}> · {patient.patientId}</span>
          {patient.phone && <span style={{ color: 'var(--text-muted)' }}> · {patient.phone}</span>}
        </div>
      )}
    </div>
  );
}

/**
 * Look up a doctor by hospital employee ID (e.g. EMP-DOC-001).
 * Pass autoDoctor to fill from patient appointment context.
 */
export function DoctorEmployeeLookup({ onResolved, onCleared, autoDoctor }) {
  const { user } = useAuthStore();
  const [code, setCode] = useState('');
  const [loading, setLoading] = useState(false);
  const [doctor, setDoctor] = useState(null);

  useEffect(() => {
    if (!autoDoctor?.id) {
      return;
    }
    setCode(autoDoctor.employeeId || '');
    setDoctor(autoDoctor);
    onResolved?.(autoDoctor);
  }, [autoDoctor?.id, autoDoctor?.employeeId]);

  const lookup = async () => {
    const trimmed = code.trim();
    if (!trimmed) {
      toast.error('Enter an employee ID (e.g. EMP-DOC-001)');
      return;
    }
    if (!user?.hospitalId) {
      toast.error('Your account has no hospital context. Re-login as hospital staff.');
      return;
    }
    setLoading(true);
    try {
      const res = await doctorApi.getByEmployeeId(trimmed, user.hospitalId);
      const d = unwrap(res);
      if (!d?.id) {
        toast.error('Doctor not found');
        setDoctor(null);
        onCleared?.();
        return;
      }
      setDoctor(d);
      onResolved?.(d);
      const name = `Dr. ${d.firstName ?? ''} ${d.lastName ?? ''}`.trim();
      toast.success(`Found ${name}`);
    } catch (err) {
      setDoctor(null);
      onCleared?.();
      toast.error(err.response?.data?.detail ?? err.response?.data?.message ?? 'Doctor lookup failed');
    } finally {
      setLoading(false);
    }
  };

  const clear = () => {
    setCode('');
    setDoctor(null);
    onCleared?.();
  };

  const label = doctor
    ? `Dr. ${doctor.firstName ?? ''} ${doctor.lastName ?? ''}`.trim()
      + (doctor.specialization ? ` — ${doctor.specialization}` : '')
      + (doctor.employeeId ? ` (${doctor.employeeId})` : '')
    : '';

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
      <label className="form-label">Doctor Employee ID *</label>
      <div style={{ display: 'flex', gap: '0.5rem' }}>
        <input
          required
          value={code}
          onChange={(e) => setCode(e.target.value)}
          placeholder="e.g. EMP-DOC-001"
          style={{ flex: 1 }}
        />
        <button type="button" className="btn btn-secondary" onClick={lookup} disabled={loading}>
          {loading ? 'Looking up…' : 'Lookup'}
        </button>
        {doctor && (
          <button type="button" className="btn btn-secondary" onClick={clear}>Clear</button>
        )}
      </div>
      {doctor && (
        <div style={{ padding: '0.65rem 0.85rem', background: 'rgba(255,255,255,0.04)', borderRadius: '8px', fontSize: '0.9rem' }}>
          <strong>{label}</strong>
        </div>
      )}
    </div>
  );
}

/**
 * After patient is resolved, load appointments and let user pick one (optional or required).
 * onChange(id, appointment) — appointment is null when cleared.
 */
export function AppointmentSelect({ patientUuid, value, onChange, required = false }) {
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!patientUuid) {
      setAppointments([]);
      onChange?.('', null);
      return;
    }
    let cancelled = false;
    (async () => {
      setLoading(true);
      try {
        const res = await appointmentApi.getByPatient(patientUuid, {
          page: 0, size: 50, sort: 'appointmentDate,desc',
        });
        if (!cancelled) setAppointments(unwrapList(res));
      } catch {
        if (!cancelled) setAppointments([]);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => { cancelled = true; };
  }, [patientUuid]);

  return (
    <div className="form-group">
      <label className="form-label">Appointment{required ? ' *' : ''}</label>
      <select
        className="form-input"
        required={required}
        value={value || ''}
        onChange={(e) => {
          const id = e.target.value;
          const appt = appointments.find((a) => a.id === id) || null;
          onChange?.(id, appt);
        }}
        disabled={!patientUuid || loading}
      >
        <option value="">
          {!patientUuid
            ? 'Look up a patient first'
            : loading
              ? 'Loading…'
              : appointments.length
                ? (required ? 'Select appointment' : 'Optional — select appointment')
                : 'No appointments found'}
        </option>
        {appointments.map((a) => (
          <option key={a.id} value={a.id}>
            {a.appointmentDate} {String(a.startTime || '').slice(0, 5)}
            {a.doctorName ? ` · ${a.doctorName}` : ''}
            {a.status ? ` · ${a.status}` : ''}
          </option>
        ))}
      </select>
    </div>
  );
}

export { unwrap, unwrapList, pickBestAppointment, resolveDoctorFromAppointment };
