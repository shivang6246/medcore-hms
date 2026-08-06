import React, { useState, useEffect, useCallback } from 'react';
import { FlaskConical, RefreshCw, Plus, Clock, CheckCircle2 } from 'lucide-react';
import { labApi } from '../api/lab.api';
import toast from 'react-hot-toast';
import Modal from '../components/ui/Modal';
import { validateUUIDs } from '../utils/uuid';

const PRIORITIES = ['NORMAL', 'URGENT', 'STAT'];

/* ── Order Lab Test Modal ──────────────────────────────────────────────── */
const OrderLabTestModal = ({ isOpen, onClose, onSuccess }) => {
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    patientId: '', doctorId: '', appointmentId: '',
    testType: '', priority: 'NORMAL', instructions: '',
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
      await labApi.createTest({
        patientId: form.patientId.trim(),
        doctorId: form.doctorId.trim(),
        appointmentId: form.appointmentId?.trim() || undefined,
        testType: form.testType,
        priority: form.priority,
        instructions: form.instructions || undefined,
      });
      toast.success('Lab test ordered successfully!');
      onSuccess();
      onClose();
      setForm({ patientId: '', doctorId: '', appointmentId: '', testType: '', priority: 'NORMAL', instructions: '' });
    } catch (err) {
      toast.error(err.response?.data?.message ?? err.response?.data?.detail ?? 'Failed to order lab test.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Order Lab Test" maxWidth="620px">
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
            <label className="form-label">Test Type *</label>
            <input required value={form.testType} onChange={(e) => set('testType', e.target.value)} placeholder="Complete Blood Count (CBC)" />
          </div>
          <div className="form-group">
            <label className="form-label">Priority *</label>
            <select required value={form.priority} onChange={(e) => set('priority', e.target.value)}>
              {PRIORITIES.map((p) => <option key={p} value={p}>{p}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Appointment ID</label>
            <input value={form.appointmentId} onChange={(e) => set('appointmentId', e.target.value)} placeholder="Optional" />
          </div>
        </div>
        <div className="form-group">
          <label className="form-label">Instructions</label>
          <textarea rows={2} value={form.instructions} onChange={(e) => set('instructions', e.target.value)} placeholder="e.g. Fasting required for 12 hours" />
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
          <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Ordering…' : 'Order Lab Test'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

/* ── Status helpers ────────────────────────────────────────────────────── */
const statusStyle = (status) => {
  switch (status) {
    case 'PUBLISHED': return { background: 'rgba(16, 185, 129, 0.1)', color: '#10b981' };
    case 'SAMPLE_COLLECTED': return { background: 'rgba(59, 130, 246, 0.1)', color: '#3b82f6' };
    case 'IN_PROGRESS': return { background: 'rgba(245, 158, 11, 0.1)', color: '#f59e0b' };
    default: return { background: 'rgba(139, 92, 246, 0.1)', color: '#8b5cf6' };
  }
};

const priorityStyle = (priority) => {
  switch (priority) {
    case 'STAT': return { color: '#ef4444', fontWeight: '700' };
    case 'URGENT': return { color: '#ef4444', fontWeight: '600' };
    default: return { color: 'var(--text-secondary)' };
  }
};

/* ── Main Laboratory Page ──────────────────────────────────────────────── */
export default function Laboratory() {
  const [labTests, setLabTests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showOrder, setShowOrder] = useState(false);

  const fetchLabTests = useCallback(async () => {
    setLoading(true);
    try {
      // Try the generic list endpoint — backend has GET /api/lab-tests (via search or list)
      const res = await labApi.getAll
        ? labApi.getAll({ page: 0, size: 50 })
        : { data: { data: { content: [] } } };
      const payload = res.data;
      const list = payload?.data?.content ?? payload?.content ?? (Array.isArray(payload?.data) ? payload.data : (Array.isArray(payload) ? payload : []));
      setLabTests(list);
    } catch {
      setLabTests([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchLabTests(); }, [fetchLabTests]);

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '700', color: 'var(--text-primary)' }}>Laboratory Management</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Track diagnostic orders, technician assignments, and lab report publishing</p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button className="btn btn-secondary" onClick={fetchLabTests} title="Refresh">
            <RefreshCw size={18} />
          </button>
          <button className="btn btn-primary" onClick={() => setShowOrder(true)}>
            <FlaskConical size={18} style={{ marginRight: '0.5rem' }} /> Order Lab Test
          </button>
        </div>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '4rem', color: 'var(--text-muted)' }}>
          <div className="skeleton" style={{ width: '48px', height: '48px', borderRadius: '50%', margin: '0 auto 1rem' }} />
          <p>Loading lab tests…</p>
        </div>
      ) : labTests.length === 0 ? (
        <div className="empty-state">
          <FlaskConical size={48} />
          <h3>No lab tests found</h3>
          <p>Order a lab test to get started.</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(380px, 1fr))', gap: '1.5rem' }}>
          {labTests.map((t) => (
            <div key={t.id} className="card" style={{ padding: '1.5rem', borderRadius: '12px', border: '1px solid var(--color-border)', background: 'var(--color-bg-elevated)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  <div style={{ padding: '0.5rem', borderRadius: '8px', background: 'rgba(139, 92, 246, 0.1)', color: '#8b5cf6' }}>
                    <FlaskConical size={24} />
                  </div>
                  <div>
                    <h3 style={{ fontSize: '1.1rem', fontWeight: '600' }}>{t.testType}</h3>
                    <span style={{ fontSize: '0.85rem', ...priorityStyle(t.priority) }}>{t.priority}</span>
                  </div>
                </div>
                <span className="badge" style={{ ...statusStyle(t.status), padding: '0.25rem 0.6rem', borderRadius: '6px', fontSize: '0.8rem' }}>
                  {t.status}
                </span>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.9rem' }}>
                <div><strong>Patient:</strong> {t.patientName ?? t.patientId ?? '—'}</div>
                <div><strong>Ordering Doctor:</strong> {t.doctorName ?? t.doctorId ?? '—'}</div>
                {t.result && (
                  <div style={{ marginTop: '0.5rem', background: 'var(--color-bg-subtle, rgba(255,255,255,0.03))', padding: '0.75rem', borderRadius: '8px' }}>
                    <strong>Lab Results:</strong>
                    <p style={{ color: 'var(--text-primary)', marginTop: '0.2rem', fontFamily: 'monospace' }}>{t.result}</p>
                    {t.remarks && <small style={{ color: 'var(--text-secondary)' }}>Remarks: {t.remarks}</small>}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      <OrderLabTestModal isOpen={showOrder} onClose={() => setShowOrder(false)} onSuccess={fetchLabTests} />
    </div>
  );
}
