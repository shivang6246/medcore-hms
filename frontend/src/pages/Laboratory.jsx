import React, { useState, useEffect, useCallback } from 'react';
import { FlaskConical, RefreshCw, Plus, Clock, CheckCircle2 } from 'lucide-react';
import { labApi } from '../api/lab.api';
import toast from 'react-hot-toast';
import Modal from '../components/ui/Modal';
import { PatientIdLookup, DoctorEmployeeLookup, AppointmentSelect, resolveDoctorFromAppointment } from '../components/lookup/EntityLookups';
import useAuthStore from '../store/authStore';

const PRIORITIES = ['NORMAL', 'URGENT', 'STAT'];

/* ── Order Lab Test Modal ──────────────────────────────────────────────── */
const OrderLabTestModal = ({ isOpen, onClose, onSuccess }) => {
  const [loading, setLoading] = useState(false);
  const [autoDoctor, setAutoDoctor] = useState(null);
  const [form, setForm] = useState({
    patientId: '', doctorId: '', appointmentId: '',
    testType: '', priority: 'NORMAL', instructions: '',
  });

  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  const reset = () => {
    setForm({
      patientId: '', doctorId: '', appointmentId: '',
      testType: '', priority: 'NORMAL', instructions: '',
    });
    setAutoDoctor(null);
  };

  const applyContext = (ctx) => {
    setForm((f) => ({
      ...f,
      appointmentId: ctx?.appointmentId || '',
      doctorId: ctx?.doctor?.id || '',
    }));
    setAutoDoctor(ctx?.doctor || null);
  };

  const handleAppointmentChange = async (id, appt) => {
    set('appointmentId', id);
    if (!appt) return;
    const doctor = await resolveDoctorFromAppointment(appt);
    if (doctor) {
      setAutoDoctor(doctor);
      set('doctorId', doctor.id);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.patientId || !form.doctorId) {
      toast.error('Look up patient and doctor first');
      return;
    }
    setLoading(true);
    try {
      await labApi.createTest({
        patientId: form.patientId,
        doctorId: form.doctorId,
        appointmentId: form.appointmentId || undefined,
        testType: form.testType,
        priority: form.priority,
        instructions: form.instructions || undefined,
      });
      toast.success('Lab test ordered successfully!');
      onSuccess();
      onClose();
      reset();
    } catch (err) {
      toast.error(err.response?.data?.message ?? err.response?.data?.detail ?? 'Failed to order lab test.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={() => { reset(); onClose(); }} title="Order Lab Test" maxWidth="620px">
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <PatientIdLookup
          onResolved={(p) => setForm((f) => ({ ...f, patientId: p.id, appointmentId: '', doctorId: '' }))}
          onCleared={() => {
            setForm((f) => ({ ...f, patientId: '', appointmentId: '', doctorId: '' }));
            setAutoDoctor(null);
          }}
          onContext={applyContext}
        />
        <DoctorEmployeeLookup
          key={form.patientId || 'no-patient'}
          autoDoctor={autoDoctor}
          onResolved={(d) => set('doctorId', d.id)}
          onCleared={() => { set('doctorId', ''); setAutoDoctor(null); }}
        />
        <AppointmentSelect
          patientUuid={form.patientId}
          value={form.appointmentId}
          onChange={handleAppointmentChange}
        />
        <div className="form-grid">
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
        </div>
        <div className="form-group">
          <label className="form-label">Instructions</label>
          <textarea rows={2} value={form.instructions} onChange={(e) => set('instructions', e.target.value)} placeholder="e.g. Fasting required for 12 hours" />
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
          <button type="button" className="btn btn-secondary" onClick={() => { reset(); onClose(); }}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={loading || !form.patientId || !form.doctorId}>
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
  const { user, hasAnyRole } = useAuthStore();
  const isPatient = hasAnyRole(['PATIENT']);
  const canOrder = hasAnyRole(['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'LAB_TECHNICIAN']);

  const [labTests, setLabTests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showOrder, setShowOrder] = useState(false);

  const fetchLabTests = useCallback(async () => {
    setLoading(true);
    try {
      const res = isPatient && user?.patientId
        ? await labApi.getByPatient(user.patientId, { page: 0, size: 50 })
        : await labApi.getAll({ page: 0, size: 50 });
      const payload = res.data;
      const list = payload?.data?.content ?? payload?.content ?? (Array.isArray(payload?.data) ? payload.data : (Array.isArray(payload) ? payload : []));
      setLabTests(list);
    } catch {
      setLabTests([]);
    } finally {
      setLoading(false);
    }
  }, [isPatient, user?.patientId]);

  useEffect(() => { fetchLabTests(); }, [fetchLabTests]);

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '700', color: 'var(--text-primary)' }}>Laboratory</h1>
          <p style={{ color: 'var(--text-secondary)' }}>
            {isPatient
              ? 'View your lab orders and reports'
              : 'Track diagnostic orders, technician assignments, and lab report publishing'}
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button className="btn btn-secondary" onClick={fetchLabTests} title="Refresh">
            <RefreshCw size={18} />
          </button>
          {canOrder && (
            <button className="btn btn-primary" onClick={() => setShowOrder(true)}>
              <FlaskConical size={18} style={{ marginRight: '0.5rem' }} /> Order Lab Test
            </button>
          )}
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
          <p>{isPatient ? 'Lab orders for you will appear here.' : 'Order a lab test to get started.'}</p>
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
                {!isPatient && <div><strong>Patient:</strong> {t.patientName ?? t.patientId ?? '—'}</div>}
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

      {canOrder && (
        <OrderLabTestModal isOpen={showOrder} onClose={() => setShowOrder(false)} onSuccess={fetchLabTests} />
      )}
    </div>
  );
}
