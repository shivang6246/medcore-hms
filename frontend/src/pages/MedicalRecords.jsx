import React, { useState, useEffect, useCallback } from 'react';
import { FileText, Plus, RefreshCw, Search, User, Stethoscope, X } from 'lucide-react';
import { medicalRecordApi } from '../api/medicalRecord.api';
import { patientApi } from '../api/patient.api';
import { appointmentApi } from '../api/appointment.api';
import toast from 'react-hot-toast';
import Modal from '../components/ui/Modal';
import useAuthStore from '../store/authStore';

const unwrap = (res) => {
  const payload = res?.data;
  return payload?.data ?? payload;
};

const unwrapList = (res) => {
  const data = unwrap(res);
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.content)) return data.content;
  return [];
};

/* ── Create Medical Record Modal ────────────────────────────────────────── */
const CreateRecordModal = ({ isOpen, onClose, onSuccess }) => {
  const { user } = useAuthStore();
  const [loading, setLoading] = useState(false);
  const [lookingUp, setLookingUp] = useState(false);
  const [patientCode, setPatientCode] = useState('');
  const [patient, setPatient] = useState(null);
  const [appointments, setAppointments] = useState([]);
  const [selectedAppointmentId, setSelectedAppointmentId] = useState('');
  const [doctorLabel, setDoctorLabel] = useState('');
  const [form, setForm] = useState({
    patientId: '',
    doctorId: '',
    appointmentId: '',
    symptoms: '',
    diagnosis: '',
    treatmentPlan: '',
    notes: '',
  });

  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  const reset = () => {
    setPatientCode('');
    setPatient(null);
    setAppointments([]);
    setSelectedAppointmentId('');
    setDoctorLabel('');
    setForm({
      patientId: '', doctorId: '', appointmentId: '',
      symptoms: '', diagnosis: '', treatmentPlan: '', notes: '',
    });
  };

  const handleLookupPatient = async () => {
    const code = patientCode.trim();
    if (!code) {
      toast.error('Enter a patient ID (e.g. P-2026-00001)');
      return;
    }
    if (!user?.hospitalId) {
      toast.error('Your account has no hospital context. Re-login as hospital staff.');
      return;
    }

    setLookingUp(true);
    setPatient(null);
    setAppointments([]);
    setSelectedAppointmentId('');
    setDoctorLabel('');
    setForm((f) => ({ ...f, patientId: '', doctorId: '', appointmentId: '' }));

    try {
      const patientRes = await patientApi.getByPatientId(code, user.hospitalId);
      const p = unwrap(patientRes);
      if (!p?.id) {
        toast.error('Patient not found for that ID');
        return;
      }
      setPatient(p);

      const apptRes = await appointmentApi.getByPatient(p.id, { page: 0, size: 50, sort: 'appointmentDate,desc' });
      const list = unwrapList(apptRes);
      setAppointments(list);
      if (!list.length) {
        toast.error('No appointments for this patient. Book an appointment first.');
      } else {
        toast.success(`Found ${p.firstName} ${p.lastName} — select an appointment`);
      }
    } catch (err) {
      toast.error(err.response?.data?.detail ?? err.response?.data?.message ?? 'Patient lookup failed');
    } finally {
      setLookingUp(false);
    }
  };

  const handleSelectAppointment = async (appointmentId) => {
    setSelectedAppointmentId(appointmentId);
    if (!appointmentId) {
      setDoctorLabel('');
      setForm((f) => ({ ...f, patientId: '', doctorId: '', appointmentId: '' }));
      return;
    }
    try {
      const res = await appointmentApi.getById(appointmentId);
      const appt = unwrap(res);
      const patientUuid = appt?.patient?.id ?? patient?.id ?? '';
      const doctorUuid = appt?.doctor?.id ?? '';
      const doctorName = appt?.doctor
        ? `Dr. ${appt.doctor.firstName ?? ''} ${appt.doctor.lastName ?? ''}`.trim()
          + (appt.doctor.specialization ? ` — ${appt.doctor.specialization}` : '')
        : '';
      setDoctorLabel(doctorName);
      setForm((f) => ({
        ...f,
        patientId: patientUuid,
        doctorId: doctorUuid,
        appointmentId,
      }));
    } catch (err) {
      toast.error(err.response?.data?.detail ?? 'Failed to load appointment details');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!form.patientId || !form.doctorId || !form.appointmentId) {
      toast.error('Look up a patient and select an appointment first');
      return;
    }

    setLoading(true);
    try {
      const payload = {
        patientId: form.patientId,
        doctorId: form.doctorId,
        appointmentId: form.appointmentId,
        symptoms: form.symptoms,
        diagnosis: form.diagnosis,
        treatmentPlan: form.treatmentPlan || undefined,
        notes: form.notes || undefined,
      };
      await medicalRecordApi.create(payload);
      toast.success('Medical record created!');
      onSuccess();
      onClose();
      reset();
    } catch (err) {
      toast.error(err.response?.data?.message ?? err.response?.data?.detail ?? 'Failed to create record.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={() => { reset(); onClose(); }}
      title="Create Medical Record"
      maxWidth="680px"
    >
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div className="form-group">
          <label className="form-label">Patient ID *</label>
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <input
              required
              value={patientCode}
              onChange={(e) => setPatientCode(e.target.value)}
              placeholder="e.g. P-2026-00001"
              style={{ flex: 1 }}
            />
            <button type="button" className="btn btn-secondary" onClick={handleLookupPatient} disabled={lookingUp}>
              {lookingUp ? 'Looking up…' : 'Lookup'}
            </button>
          </div>
        </div>

        {patient && (
          <div style={{ padding: '0.75rem 1rem', background: 'rgba(255,255,255,0.04)', borderRadius: '8px', fontSize: '0.9rem' }}>
            <strong>{patient.firstName} {patient.lastName}</strong>
            <span style={{ color: 'var(--text-muted)' }}> · {patient.patientId}</span>
            {patient.phone && <span style={{ color: 'var(--text-muted)' }}> · {patient.phone}</span>}
          </div>
        )}

        <div className="form-group">
          <label className="form-label">Appointment *</label>
          <select
            className="form-input"
            required
            value={selectedAppointmentId}
            onChange={(e) => handleSelectAppointment(e.target.value)}
            disabled={!patient || appointments.length === 0}
          >
            <option value="">
              {!patient
                ? 'Lookup up a patient first'
                : appointments.length
                  ? 'Select appointment'
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

        <div className="form-group">
          <label className="form-label">Doctor</label>
          <input
            readOnly
            value={doctorLabel || (form.doctorId ? 'Loaded' : '')}
            placeholder="Auto-filled from appointment"
          />
        </div>

        <div className="form-group">
          <label className="form-label">Diagnosis *</label>
          <input required value={form.diagnosis} onChange={(e) => set('diagnosis', e.target.value)} placeholder="Acute Bronchitis" />
        </div>
        <div className="form-group">
          <label className="form-label">Symptoms *</label>
          <textarea required rows={2} value={form.symptoms} onChange={(e) => set('symptoms', e.target.value)} placeholder="e.g. High fever, persistent cough, fatigue" />
        </div>
        <div className="form-group">
          <label className="form-label">Treatment Plan</label>
          <textarea rows={2} value={form.treatmentPlan} onChange={(e) => set('treatmentPlan', e.target.value)} placeholder="Prescribed Amoxicillin & Rest for 5 days" />
        </div>
        <div className="form-group">
          <label className="form-label">Notes</label>
          <textarea rows={2} value={form.notes} onChange={(e) => set('notes', e.target.value)} placeholder="Follow up if fever exceeds 102°F" />
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
          <button type="button" className="btn btn-secondary" onClick={() => { reset(); onClose(); }}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={loading || !form.appointmentId}>
            {loading ? 'Creating…' : 'Create Record'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

/* ── Search by Patient Modal ────────────────────────────────────────────── */
const SearchPatientModal = ({ isOpen, onClose, onResults }) => {
  const { user } = useAuthStore();
  const [patientCode, setPatientCode] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!patientCode.trim()) return;
    if (!user?.hospitalId) {
      toast.error('Hospital context missing on your account');
      return;
    }
    setLoading(true);
    try {
      let patientUuid = patientCode.trim();
      if (!/^[0-9a-f-]{36}$/i.test(patientUuid)) {
        const patientRes = await patientApi.getByPatientId(patientCode.trim(), user.hospitalId);
        const p = unwrap(patientRes);
        if (!p?.id) throw new Error('Patient not found');
        patientUuid = p.id;
      }
      const res = await medicalRecordApi.getByPatient(patientUuid, { page: 0, size: 50 });
      onResults(unwrapList(res));
      onClose();
    } catch (err) {
      toast.error(err.response?.data?.message ?? err.response?.data?.detail ?? 'Patient not found or no records.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Search by Patient ID" maxWidth="460px">
      <form onSubmit={handleSearch} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div className="form-group">
          <label className="form-label">Patient ID *</label>
          <input required value={patientCode} onChange={(e) => setPatientCode(e.target.value)} placeholder="e.g. P-2026-00001" />
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
          <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={loading}>{loading ? 'Searching…' : 'Search'}</button>
        </div>
      </form>
    </Modal>
  );
};

/* ── Main Medical Records Page ──────────────────────────────────────────── */
export default function MedicalRecords() {
  const { user, hasAnyRole } = useAuthStore();
  const isPatient = hasAnyRole(['PATIENT']);
  const canCreate = hasAnyRole(['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE']);
  const canSearch = hasAnyRole(['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE']);

  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [showSearch, setShowSearch] = useState(false);
  const [filtered, setFiltered] = useState(false);

  const fetchRecords = useCallback(async () => {
    setLoading(true);
    setFiltered(false);
    try {
      const res = isPatient && user?.patientId
        ? await medicalRecordApi.getByPatient(user.patientId, { page: 0, size: 50 })
        : await medicalRecordApi.getAll({ page: 0, size: 50 });
      setRecords(unwrapList(res));
    } catch {
      setRecords([]);
    } finally {
      setLoading(false);
    }
  }, [isPatient, user?.patientId]);

  useEffect(() => { fetchRecords(); }, [fetchRecords]);

  const handleSearchResults = (results) => {
    setRecords(results);
    setFiltered(true);
  };

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '700', color: 'var(--text-primary)' }}>Medical Records</h1>
          <p style={{ color: 'var(--text-secondary)' }}>
            {isPatient
              ? 'View your clinical notes and diagnoses'
              : 'Manage patient EHR clinical notes and diagnoses'}
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          {filtered && canSearch && (
            <button className="btn btn-secondary" onClick={fetchRecords} title="Clear filter">
              <X size={18} />
            </button>
          )}
          <button className="btn btn-secondary" onClick={fetchRecords} title="Refresh">
            <RefreshCw size={18} />
          </button>
          {canSearch && (
            <button className="btn btn-secondary" onClick={() => setShowSearch(true)}>
              <Search size={18} style={{ marginRight: '0.5rem' }} /> Search Patient
            </button>
          )}
          {canCreate && (
            <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
              <Plus size={18} style={{ marginRight: '0.5rem' }} /> Create Record
            </button>
          )}
        </div>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '4rem', color: 'var(--text-muted)' }}>
          <div className="skeleton" style={{ width: '48px', height: '48px', borderRadius: '50%', margin: '0 auto 1rem' }} />
          <p>Loading medical records…</p>
        </div>
      ) : records.length === 0 ? (
        <div className="empty-state">
          <FileText size={48} />
          <h3>No medical records found</h3>
          <p>
            {isPatient
              ? 'Records from your visits will appear here.'
              : 'Create the first EHR record for a patient.'}
          </p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '1.5rem' }}>
          {records.map((r) => (
            <div key={r.id} className="card" style={{ padding: '1.5rem', borderRadius: '12px', border: '1px solid var(--color-border)', background: 'var(--color-bg-elevated)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  <div style={{ padding: '0.5rem', borderRadius: '8px', background: 'rgba(59, 130, 246, 0.1)', color: '#3b82f6' }}>
                    <FileText size={24} />
                  </div>
                  <div>
                    <h3 style={{ fontSize: '1.05rem', fontWeight: '600' }}>{r.diagnosis || 'Clinical note'}</h3>
                    <span style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                      {r.createdAt ? new Date(r.createdAt).toLocaleDateString() : '—'}
                    </span>
                  </div>
                </div>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.9rem' }}>
                {!isPatient && (
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                    <User size={14} /> <strong>Patient:</strong> {r.patientName ?? r.patientId ?? '—'}
                  </div>
                )}
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                  <Stethoscope size={14} /> <strong>Doctor:</strong> {r.doctorName ?? r.doctorId ?? '—'}
                </div>
                {r.symptoms && <div><strong>Symptoms:</strong> {r.symptoms}</div>}
                {r.treatmentPlan && <div><strong>Plan:</strong> {r.treatmentPlan}</div>}
                {r.notes && (
                  <div style={{ marginTop: '0.5rem', background: 'rgba(255,255,255,0.03)', padding: '0.6rem 0.75rem', borderRadius: '8px' }}>
                    <strong style={{ fontSize: '0.82rem' }}>Notes:</strong>
                    <p style={{ color: 'var(--text-secondary)', marginTop: '0.15rem', fontSize: '0.85rem' }}>{r.notes}</p>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {canCreate && (
        <CreateRecordModal isOpen={showCreate} onClose={() => setShowCreate(false)} onSuccess={fetchRecords} />
      )}
      {canSearch && (
        <SearchPatientModal isOpen={showSearch} onClose={() => setShowSearch(false)} onResults={handleSearchResults} />
      )}
    </div>
  );
}
