import React, { useState, useEffect } from 'react';
import { Pill, Plus, Search, X } from 'lucide-react';
import { prescriptionApi } from '../api/prescription.api';
import { patientApi } from '../api/patient.api';
import { medicalRecordApi } from '../api/medicalRecord.api';
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

/* ── New Prescription Modal ────────────────────────────────────────────── */
const NewPrescriptionModal = ({ isOpen, onClose, onSuccess }) => {
  const { user } = useAuthStore();
  const [loading, setLoading] = useState(false);
  const [lookingUp, setLookingUp] = useState(false);
  const [patientCode, setPatientCode] = useState('');
  const [patient, setPatient] = useState(null);
  const [records, setRecords] = useState([]);
  const [form, setForm] = useState({
    medicalRecordId: '', medicineName: '', dosage: '',
    frequency: '', duration: '', instructions: '', quantity: '',
  });

  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  const reset = () => {
    setPatientCode('');
    setPatient(null);
    setRecords([]);
    setForm({
      medicalRecordId: '', medicineName: '', dosage: '',
      frequency: '', duration: '', instructions: '', quantity: '',
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
    setRecords([]);
    setForm((f) => ({ ...f, medicalRecordId: '' }));

    try {
      const patientRes = await patientApi.getByPatientId(code, user.hospitalId);
      const p = unwrap(patientRes);
      if (!p?.id) {
        toast.error('Patient not found for that ID');
        return;
      }
      setPatient(p);

      const recordRes = await medicalRecordApi.getByPatient(p.id, { page: 0, size: 50 });
      const list = unwrapList(recordRes);
      setRecords(list);
      if (!list.length) {
        toast.error('No medical records for this patient. Create one first.');
      } else {
        toast.success(`Found ${p.firstName} ${p.lastName} — select a medical record`);
      }
    } catch (err) {
      toast.error(err.response?.data?.detail ?? err.response?.data?.message ?? 'Patient lookup failed');
    } finally {
      setLookingUp(false);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.medicalRecordId) {
      toast.error('Look up a patient and select a medical record first');
      return;
    }
    setLoading(true);
    try {
      await prescriptionApi.create({
        medicalRecordId: form.medicalRecordId,
        medicineName: form.medicineName,
        dosage: form.dosage,
        frequency: form.frequency,
        duration: parseInt(form.duration, 10),
        instructions: form.instructions || undefined,
        quantity: form.quantity ? parseInt(form.quantity, 10) : undefined,
      });
      toast.success('Prescription created successfully!');
      onSuccess(form.medicalRecordId);
      onClose();
      reset();
    } catch (err) {
      toast.error(err.response?.data?.message ?? err.response?.data?.detail ?? 'Failed to create prescription.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={() => { reset(); onClose(); }} title="New Prescription" maxWidth="640px">
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
          <label className="form-label">Medical Record *</label>
          <select
            className="form-input"
            required
            value={form.medicalRecordId}
            onChange={(e) => set('medicalRecordId', e.target.value)}
            disabled={!patient || records.length === 0}
          >
            <option value="">
              {!patient
                ? 'Look up a patient first'
                : records.length
                  ? 'Select medical record'
                  : 'No medical records found'}
            </option>
            {records.map((r) => (
              <option key={r.id} value={r.id}>
                {(r.diagnosis || 'Record')} · {r.createdAt ? new Date(r.createdAt).toLocaleDateString() : '—'}
                {r.doctorName ? ` · ${r.doctorName}` : ''}
              </option>
            ))}
          </select>
        </div>

        <div className="form-grid">
          <div className="form-group">
            <label className="form-label">Medicine Name *</label>
            <input required value={form.medicineName} onChange={(e) => set('medicineName', e.target.value)} placeholder="Amoxicillin 500mg" />
          </div>
          <div className="form-group">
            <label className="form-label">Dosage *</label>
            <input required value={form.dosage} onChange={(e) => set('dosage', e.target.value)} placeholder="1 capsule" />
          </div>
          <div className="form-group">
            <label className="form-label">Frequency *</label>
            <input required value={form.frequency} onChange={(e) => set('frequency', e.target.value)} placeholder="3 times daily after meals" />
          </div>
          <div className="form-group">
            <label className="form-label">Duration (days) *</label>
            <input required type="number" min="1" value={form.duration} onChange={(e) => set('duration', e.target.value)} placeholder="7" />
          </div>
          <div className="form-group">
            <label className="form-label">Total Quantity</label>
            <input type="number" min="1" value={form.quantity} onChange={(e) => set('quantity', e.target.value)} placeholder="21" />
          </div>
        </div>
        <div className="form-group">
          <label className="form-label">Instructions</label>
          <textarea rows={2} value={form.instructions} onChange={(e) => set('instructions', e.target.value)} placeholder="Complete full course, drink plenty of water" />
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
          <button type="button" className="btn btn-secondary" onClick={() => { reset(); onClose(); }}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={loading || !form.medicalRecordId}>
            {loading ? 'Creating…' : 'Create Prescription'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

/* ── Search by Patient Modal ────────────────────────────────────────────── */
const SearchModal = ({ isOpen, onClose, onResults }) => {
  const { user } = useAuthStore();
  const [patientCode, setPatientCode] = useState('');
  const [lookingUp, setLookingUp] = useState(false);
  const [patient, setPatient] = useState(null);
  const [records, setRecords] = useState([]);
  const [medicalRecordId, setMedicalRecordId] = useState('');
  const [loading, setLoading] = useState(false);

  const reset = () => {
    setPatientCode('');
    setPatient(null);
    setRecords([]);
    setMedicalRecordId('');
  };

  const handleLookupPatient = async () => {
    const code = patientCode.trim();
    if (!code) {
      toast.error('Enter a patient ID (e.g. P-2026-00001)');
      return;
    }
    if (!user?.hospitalId) {
      toast.error('Hospital context missing on your account');
      return;
    }
    setLookingUp(true);
    setPatient(null);
    setRecords([]);
    setMedicalRecordId('');
    try {
      const patientRes = await patientApi.getByPatientId(code, user.hospitalId);
      const p = unwrap(patientRes);
      if (!p?.id) {
        toast.error('Patient not found');
        return;
      }
      setPatient(p);
      const recordRes = await medicalRecordApi.getByPatient(p.id, { page: 0, size: 50 });
      const list = unwrapList(recordRes);
      setRecords(list);
      if (!list.length) toast.error('No medical records for this patient');
    } catch (err) {
      toast.error(err.response?.data?.detail ?? err.response?.data?.message ?? 'Patient lookup failed');
    } finally {
      setLookingUp(false);
    }
  };

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!medicalRecordId) {
      toast.error('Select a medical record');
      return;
    }
    setLoading(true);
    try {
      const res = await prescriptionApi.getByMedicalRecord(medicalRecordId, { page: 0, size: 50 });
      onResults(unwrapList(res));
      onClose();
      reset();
    } catch (err) {
      toast.error(err.response?.data?.message ?? 'No prescriptions found for that medical record.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={() => { reset(); onClose(); }} title="Search Prescriptions" maxWidth="520px">
      <form onSubmit={handleSearch} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
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
          </div>
        )}

        <div className="form-group">
          <label className="form-label">Medical Record *</label>
          <select
            className="form-input"
            required
            value={medicalRecordId}
            onChange={(e) => setMedicalRecordId(e.target.value)}
            disabled={!patient || records.length === 0}
          >
            <option value="">
              {!patient ? 'Look up a patient first' : records.length ? 'Select medical record' : 'No records found'}
            </option>
            {records.map((r) => (
              <option key={r.id} value={r.id}>
                {(r.diagnosis || 'Record')} · {r.createdAt ? new Date(r.createdAt).toLocaleDateString() : '—'}
              </option>
            ))}
          </select>
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
          <button type="button" className="btn btn-secondary" onClick={() => { reset(); onClose(); }}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={loading || !medicalRecordId}>
            {loading ? 'Searching…' : 'Search'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

/* ── Main Prescriptions Page ───────────────────────────────────────────── */
export default function Prescriptions() {
  const { user, hasAnyRole } = useAuthStore();
  const isPatient = hasAnyRole(['PATIENT']);
  const canCreate = hasAnyRole(['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR']);
  const canSearch = hasAnyRole(['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'PHARMACIST', 'NURSE']);

  const [prescriptions, setPrescriptions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showCreate, setShowCreate] = useState(false);
  const [showSearch, setShowSearch] = useState(false);
  const [filtered, setFiltered] = useState(false);

  useEffect(() => {
    if (!isPatient || !user?.patientId) {
      setLoading(false);
      return;
    }
    let cancelled = false;
    (async () => {
      setLoading(true);
      try {
        const recordsRes = await medicalRecordApi.getByPatient(user.patientId, { page: 0, size: 50 });
        const records = unwrapList(recordsRes);
        const lists = await Promise.all(
          records.map((r) =>
            prescriptionApi.getByMedicalRecord(r.id, { page: 0, size: 50 })
              .then(unwrapList)
              .catch(() => []),
          ),
        );
        if (!cancelled) {
          setPrescriptions(lists.flat());
          setFiltered(true);
        }
      } catch {
        if (!cancelled) setPrescriptions([]);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => { cancelled = true; };
  }, [isPatient, user?.patientId]);

  const handleSearchResults = (results) => {
    setPrescriptions(results);
    setFiltered(true);
  };

  const clearFilter = () => {
    setPrescriptions([]);
    setFiltered(false);
  };

  const reloadForRecord = async (medicalRecordId) => {
    if (!medicalRecordId) return;
    try {
      const res = await prescriptionApi.getByMedicalRecord(medicalRecordId, { page: 0, size: 50 });
      setPrescriptions(unwrapList(res));
      setFiltered(true);
    } catch {
      /* keep existing list */
    }
  };

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '700', color: 'var(--text-primary)' }}>Prescriptions</h1>
          <p style={{ color: 'var(--text-secondary)' }}>
            {isPatient
              ? 'View medicines prescribed for you'
              : 'Manage electronic prescriptions and medicine dosages'}
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          {filtered && !isPatient && (
            <button className="btn btn-secondary" onClick={clearFilter} title="Clear results">
              <X size={18} />
            </button>
          )}
          {canSearch && (
            <button className="btn btn-secondary" onClick={() => setShowSearch(true)}>
              <Search size={18} style={{ marginRight: '0.5rem' }} /> Search by Patient
            </button>
          )}
          {canCreate && (
            <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
              <Plus size={18} style={{ marginRight: '0.5rem' }} /> New Prescription
            </button>
          )}
        </div>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '4rem', color: 'var(--text-muted)' }}>
          <div className="skeleton" style={{ width: '48px', height: '48px', borderRadius: '50%', margin: '0 auto 1rem' }} />
          <p>Loading prescriptions…</p>
        </div>
      ) : prescriptions.length === 0 ? (
        <div className="empty-state">
          <Pill size={48} />
          <h3>{filtered || isPatient ? 'No prescriptions found' : 'Search for prescriptions'}</h3>
          <p>
            {isPatient
              ? 'Prescriptions from your visits will appear here.'
              : filtered
                ? 'No prescriptions linked to that medical record.'
                : 'Look up a patient ID to load prescriptions, or create a new one.'}
          </p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '1.5rem' }}>
          {prescriptions.map((p) => (
            <div key={p.id} className="card" style={{ padding: '1.5rem', borderRadius: '12px', border: '1px solid var(--color-border)', background: 'var(--color-bg-elevated)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  <div style={{ padding: '0.5rem', borderRadius: '8px', background: 'rgba(16, 185, 129, 0.1)', color: '#10b981' }}>
                    <Pill size={24} />
                  </div>
                  <div>
                    <h3 style={{ fontSize: '1.05rem', fontWeight: '600' }}>{p.medicineName}</h3>
                    <span style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>Qty: {p.quantity ?? '—'}</span>
                  </div>
                </div>
                <span className="badge badge-success" style={{
                  background: p.active !== false ? 'rgba(16, 185, 129, 0.1)' : 'rgba(239, 68, 68, 0.1)',
                  color: p.active !== false ? '#10b981' : '#ef4444',
                  padding: '0.25rem 0.6rem', borderRadius: '6px', fontSize: '0.78rem'
                }}>
                  {p.active !== false ? 'Active' : 'Inactive'}
                </span>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.9rem' }}>
                <div><strong>Dosage:</strong> {p.dosage}</div>
                <div><strong>Frequency:</strong> {p.frequency}</div>
                <div><strong>Duration:</strong> {p.duration} {typeof p.duration === 'number' ? 'days' : ''}</div>
                {p.instructions && (
                  <div style={{ marginTop: '0.5rem', background: 'rgba(255,255,255,0.03)', padding: '0.6rem 0.75rem', borderRadius: '8px' }}>
                    <strong style={{ fontSize: '0.82rem' }}>Instructions:</strong>
                    <p style={{ color: 'var(--text-secondary)', marginTop: '0.15rem', fontSize: '0.85rem' }}>{p.instructions}</p>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {canCreate && (
        <NewPrescriptionModal
          isOpen={showCreate}
          onClose={() => setShowCreate(false)}
          onSuccess={reloadForRecord}
        />
      )}
      {canSearch && (
        <SearchModal isOpen={showSearch} onClose={() => setShowSearch(false)} onResults={handleSearchResults} />
      )}
    </div>
  );
}
