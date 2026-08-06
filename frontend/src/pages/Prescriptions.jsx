import React, { useState, useEffect, useCallback } from 'react';
import { Pill, Plus, RefreshCw, Search, X } from 'lucide-react';
import { prescriptionApi } from '../api/prescription.api';
import toast from 'react-hot-toast';
import Modal from '../components/ui/Modal';
import { isValidUUID, validateUUIDs } from '../utils/uuid';

/* ── New Prescription Modal ────────────────────────────────────────────── */
const NewPrescriptionModal = ({ isOpen, onClose, onSuccess }) => {
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    medicalRecordId: '', medicineName: '', dosage: '',
    frequency: '', duration: '', instructions: '', quantity: '',
  });

  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!isValidUUID(form.medicalRecordId)) {
      toast.error('Medical Record ID must be a valid UUID (e.g. 3a8f1c7d-xxxx-xxxx-xxxx-xxxxxxxxxxxx)');
      return;
    }
    setLoading(true);
    try {
      await prescriptionApi.create({
        medicalRecordId: form.medicalRecordId.trim(),
        medicineName: form.medicineName,
        dosage: form.dosage,
        frequency: form.frequency,
        duration: parseInt(form.duration, 10),
        instructions: form.instructions || undefined,
        quantity: form.quantity ? parseInt(form.quantity, 10) : undefined,
      });
      toast.success('Prescription created successfully!');
      onSuccess();
      onClose();
      setForm({ medicalRecordId: '', medicineName: '', dosage: '', frequency: '', duration: '', instructions: '', quantity: '' });
    } catch (err) {
      toast.error(err.response?.data?.message ?? err.response?.data?.detail ?? 'Failed to create prescription.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="New Prescription" maxWidth="640px">
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div className="form-group">
          <label className="form-label">Medical Record ID *</label>
          <input required value={form.medicalRecordId} onChange={(e) => set('medicalRecordId', e.target.value)} placeholder="Medical Record UUID" />
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
          <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Creating…' : 'Create Prescription'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

/* ── Search by Medical Record Modal ─────────────────────────────────────── */
const SearchModal = ({ isOpen, onClose, onResults }) => {
  const [medicalRecordId, setMedicalRecordId] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!medicalRecordId.trim()) return;
    if (!isValidUUID(medicalRecordId)) {
      toast.error('Medical Record ID must be a valid UUID (e.g. 3a8f1c7d-xxxx-xxxx-xxxx-xxxxxxxxxxxx)');
      return;
    }
    setLoading(true);
    try {
      const res = await prescriptionApi.getByMedicalRecord(medicalRecordId.trim(), { page: 0, size: 50 });
      const payload = res.data;
      const list = payload?.data?.content ?? payload?.content ?? (Array.isArray(payload?.data) ? payload.data : (Array.isArray(payload) ? payload : []));
      onResults(list);
      onClose();
    } catch (err) {
      toast.error(err.response?.data?.message ?? 'No prescriptions found for that Medical Record ID.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Search by Medical Record" maxWidth="460px">
      <form onSubmit={handleSearch} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div className="form-group">
          <label className="form-label">Medical Record UUID *</label>
          <input required value={medicalRecordId} onChange={(e) => setMedicalRecordId(e.target.value)} placeholder="e.g. 3a8f1c7d-…" />
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
          <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={loading}>{loading ? 'Searching…' : 'Search'}</button>
        </div>
      </form>
    </Modal>
  );
};

/* ── Main Prescriptions Page ───────────────────────────────────────────── */
export default function Prescriptions() {
  const [prescriptions, setPrescriptions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showCreate, setShowCreate] = useState(false);
  const [showSearch, setShowSearch] = useState(false);
  const [filtered, setFiltered] = useState(false);

  // On first render show placeholder — prescriptions require a medical record ID to fetch
  useEffect(() => { setLoading(false); }, []);

  const handleSearchResults = (results) => {
    setPrescriptions(results);
    setFiltered(true);
  };

  const clearFilter = () => {
    setPrescriptions([]);
    setFiltered(false);
  };

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '700', color: 'var(--text-primary)' }}>Prescriptions</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Manage electronic prescriptions and medicine dosages</p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          {filtered && (
            <button className="btn btn-secondary" onClick={clearFilter} title="Clear results">
              <X size={18} />
            </button>
          )}
          <button className="btn btn-secondary" onClick={() => setShowSearch(true)}>
            <Search size={18} style={{ marginRight: '0.5rem' }} /> Search by Record
          </button>
          <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
            <Plus size={18} style={{ marginRight: '0.5rem' }} /> New Prescription
          </button>
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
          <h3>{filtered ? 'No prescriptions found' : 'Search for prescriptions'}</h3>
          <p>{filtered ? 'No prescriptions linked to that medical record.' : 'Use "Search by Record" to load prescriptions for a medical record, or create a new one.'}</p>
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

      <NewPrescriptionModal isOpen={showCreate} onClose={() => setShowCreate(false)} onSuccess={() => {}} />
      <SearchModal isOpen={showSearch} onClose={() => setShowSearch(false)} onResults={handleSearchResults} />
    </div>
  );
}
