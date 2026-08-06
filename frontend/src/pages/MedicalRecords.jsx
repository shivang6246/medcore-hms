import React, { useState, useEffect, useCallback } from 'react';
import { FileText, Plus, RefreshCw, Search, User, Stethoscope, Edit, X } from 'lucide-react';
import { medicalRecordApi } from '../api/medicalRecord.api';
import toast from 'react-hot-toast';
import Modal from '../components/ui/Modal';
import useAuthStore from '../store/authStore';
import { validateUUIDs } from '../utils/uuid';

/* ── Create Medical Record Modal ────────────────────────────────────────── */
const CreateRecordModal = ({ isOpen, onClose, onSuccess }) => {
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    patientId: '', doctorId: '', appointmentId: '',
    symptoms: '', diagnosis: '', treatmentPlan: '', notes: '',
  });

  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    const uuidErr = validateUUIDs({
      'Patient ID': form.patientId,
      'Doctor ID': form.doctorId,
      ...(form.appointmentId ? { 'Appointment ID': form.appointmentId } : {}),
    });
    if (uuidErr) { toast.error(uuidErr); return; }
    setLoading(true);
    try {
      await medicalRecordApi.create({
        patientId: form.patientId.trim(),
        doctorId: form.doctorId.trim(),
        appointmentId: form.appointmentId?.trim() || undefined,
        symptoms: form.symptoms,
        diagnosis: form.diagnosis,
        treatmentPlan: form.treatmentPlan || undefined,
        notes: form.notes || undefined,
      });
      toast.success('Medical record created!');
      onSuccess();
      onClose();
      setForm({ patientId: '', doctorId: '', appointmentId: '', symptoms: '', diagnosis: '', treatmentPlan: '', notes: '' });
    } catch (err) {
      toast.error(err.response?.data?.message ?? err.response?.data?.detail ?? 'Failed to create record.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Create Medical Record" maxWidth="680px">
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div className="form-grid">
          <div className="form-group">
            <label className="form-label">Patient ID *</label>
            <input required value={form.patientId} onChange={(e) => set('patientId', e.target.value)} placeholder="Patient UUID" />
          </div>
          <div className="form-group">
            <label className="form-label">Doctor ID *</label>
            <input required value={form.doctorId} onChange={(e) => set('doctorId', e.target.value)} placeholder="Doctor UUID" />
          </div>
          <div className="form-group">
            <label className="form-label">Appointment ID</label>
            <input value={form.appointmentId} onChange={(e) => set('appointmentId', e.target.value)} placeholder="Optional UUID" />
          </div>
          <div className="form-group">
            <label className="form-label">Diagnosis *</label>
            <input required value={form.diagnosis} onChange={(e) => set('diagnosis', e.target.value)} placeholder="Acute Bronchitis" />
          </div>
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
          <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Creating…' : 'Create Record'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

/* ── Search by Patient Modal ────────────────────────────────────────────── */
const SearchPatientModal = ({ isOpen, onClose, onResults }) => {
  const [patientId, setPatientId] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!patientId.trim()) return;
    setLoading(true);
    try {
      const res = await medicalRecordApi.getByPatient(patientId.trim(), { page: 0, size: 50 });
      const payload = res.data;
      const list = payload?.data?.content ?? payload?.content ?? (Array.isArray(payload?.data) ? payload.data : (Array.isArray(payload) ? payload : []));
      onResults(list);
      onClose();
    } catch (err) {
      toast.error(err.response?.data?.message ?? 'Patient not found or no records.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Search by Patient ID" maxWidth="460px">
      <form onSubmit={handleSearch} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div className="form-group">
          <label className="form-label">Patient UUID *</label>
          <input required value={patientId} onChange={(e) => setPatientId(e.target.value)} placeholder="e.g. 3a8f1c7d-…" />
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
  const { user } = useAuthStore();
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [showSearch, setShowSearch] = useState(false);
  const [filtered, setFiltered] = useState(false);

  const fetchRecords = useCallback(async () => {
    setLoading(true);
    setFiltered(false);
    try {
      const res = await medicalRecordApi.getAll({ page: 0, size: 50 });
      const payload = res.data;
      const list = payload?.data?.content ?? payload?.content ?? (Array.isArray(payload?.data) ? payload.data : (Array.isArray(payload) ? payload : []));
      setRecords(list);
    } catch {
      setRecords([]);
    } finally {
      setLoading(false);
    }
  }, []);

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
          <p style={{ color: 'var(--text-secondary)' }}>Manage patient EHR clinical notes and diagnoses</p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          {filtered && (
            <button className="btn btn-secondary" onClick={fetchRecords} title="Clear filter">
              <X size={18} />
            </button>
          )}
          <button className="btn btn-secondary" onClick={fetchRecords} title="Refresh">
            <RefreshCw size={18} />
          </button>
          <button className="btn btn-secondary" onClick={() => setShowSearch(true)}>
            <Search size={18} style={{ marginRight: '0.5rem' }} /> Search Patient
          </button>
          <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
            <Plus size={18} style={{ marginRight: '0.5rem' }} /> Create Record
          </button>
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
          <p>Create the first EHR record for a patient.</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(380px, 1fr))', gap: '1.5rem' }}>
          {records.map((r) => (
            <div key={r.id} className="card" style={{ padding: '1.5rem', borderRadius: '12px', border: '1px solid var(--color-border)', background: 'var(--color-bg-elevated)' }}>
              <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem', gap: '0.75rem' }}>
                <div style={{ padding: '0.5rem', borderRadius: '8px', background: 'rgba(59, 130, 246, 0.1)', color: '#3b82f6', flexShrink: 0 }}>
                  <FileText size={24} />
                </div>
                <div style={{ flex: 1, minWidth: 0 }}>
                  <h3 style={{ fontSize: '1.05rem', fontWeight: '600', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {r.diagnosis ?? '—'}
                  </h3>
                  <span style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                    {r.createdAt ? new Date(r.createdAt).toLocaleDateString() : '—'}
                  </span>
                </div>
                <span className="badge" style={{
                  background: r.active !== false ? 'rgba(16, 185, 129, 0.1)' : 'rgba(239,68,68,0.1)',
                  color: r.active !== false ? '#10b981' : '#ef4444',
                  padding: '0.25rem 0.6rem', borderRadius: '6px', fontSize: '0.78rem', flexShrink: 0,
                }}>
                  {r.active !== false ? 'Active' : 'Inactive'}
                </span>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.9rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <User size={15} style={{ color: 'var(--text-secondary)', flexShrink: 0 }} />
                  <strong>Patient:</strong>&nbsp;{r.patientName ?? r.patientId ?? '—'}
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                  <Stethoscope size={15} style={{ color: 'var(--text-secondary)', flexShrink: 0 }} />
                  <strong>Doctor:</strong>&nbsp;{r.doctorName ?? r.doctorId ?? '—'}
                </div>
                {r.symptoms && (
                  <div style={{ marginTop: '0.4rem', background: 'rgba(255,255,255,0.03)', padding: '0.6rem 0.75rem', borderRadius: '8px' }}>
                    <strong style={{ fontSize: '0.82rem' }}>Symptoms:</strong>
                    <p style={{ color: 'var(--text-secondary)', marginTop: '0.15rem', fontSize: '0.85rem' }}>{r.symptoms}</p>
                  </div>
                )}
                {r.treatmentPlan && (
                  <div>
                    <strong>Tx Plan:</strong>&nbsp;
                    <span style={{ color: 'var(--text-secondary)' }}>{r.treatmentPlan}</span>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      <CreateRecordModal isOpen={showCreate} onClose={() => setShowCreate(false)} onSuccess={fetchRecords} />
      <SearchPatientModal isOpen={showSearch} onClose={() => setShowSearch(false)} onResults={handleSearchResults} />
    </div>
  );
}
