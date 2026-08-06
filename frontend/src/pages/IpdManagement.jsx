import React, { useState, useEffect, useCallback } from 'react';
import { Bed, Plus, UserPlus, ArrowRightLeft, LogOut, RefreshCw, CheckCircle } from 'lucide-react';
import { ipdApi } from '../api/ipd.api';
import toast from 'react-hot-toast';
import Modal from '../components/ui/Modal';

/* ── Admit Patient Modal ────────────────────────────────────────────────── */
const AdmitPatientModal = ({ isOpen, onClose, onSuccess, beds }) => {
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    patientId: '', doctorId: '', bedId: '',
    reason: '', expectedDischargeDate: '',
  });

  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await ipdApi.admitPatient({
        patientId: form.patientId,
        doctorId: form.doctorId,
        bedId: form.bedId,
        reason: form.reason,
        expectedDischargeDate: form.expectedDischargeDate || undefined,
      });
      toast.success('Patient admitted successfully!');
      onSuccess();
      onClose();
      setForm({ patientId: '', doctorId: '', bedId: '', reason: '', expectedDischargeDate: '' });
    } catch (err) {
      toast.error(err.response?.data?.message ?? err.response?.data?.detail ?? 'Failed to admit patient.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Admit Patient" maxWidth="640px">
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
            <label className="form-label">Bed *</label>
            <select required value={form.bedId} onChange={(e) => set('bedId', e.target.value)}>
              <option value="">Select Available Bed</option>
              {beds.map((b) => (
                <option key={b.id} value={b.id}>
                  {b.bedNumber ?? b.id} — {b.wardName ?? b.ward?.name ?? 'Ward'}
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Expected Discharge</label>
            <input type="date" value={form.expectedDischargeDate} onChange={(e) => set('expectedDischargeDate', e.target.value)} />
          </div>
        </div>
        <div className="form-group">
          <label className="form-label">Reason for Admission *</label>
          <textarea required rows={2} value={form.reason} onChange={(e) => set('reason', e.target.value)} placeholder="Post-surgical monitoring & oxygen therapy" />
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
          <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Admitting…' : 'Admit Patient'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

/* ── Transfer Bed Modal ─────────────────────────────────────────────────── */
const TransferModal = ({ isOpen, onClose, onSuccess, admission, beds }) => {
  const [loading, setLoading] = useState(false);
  const [bedId, setBedId] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!admission) return;
    setLoading(true);
    try {
      await ipdApi.transferBed(admission.id, { newBedId: bedId });
      toast.success('Bed transfer successful!');
      onSuccess();
      onClose();
    } catch (err) {
      toast.error(err.response?.data?.message ?? 'Transfer failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Transfer to New Bed" maxWidth="460px">
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
          Transferring: <strong>{admission?.patientName ?? admission?.id}</strong>
        </p>
        <div className="form-group">
          <label className="form-label">New Bed *</label>
          <select required value={bedId} onChange={(e) => setBedId(e.target.value)}>
            <option value="">Select New Bed</option>
            {beds.map((b) => (
              <option key={b.id} value={b.id}>
                {b.bedNumber ?? b.id} — {b.wardName ?? b.ward?.name ?? 'Ward'}
              </option>
            ))}
          </select>
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem' }}>
          <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={loading || !bedId}>
            {loading ? 'Transferring…' : 'Confirm Transfer'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

/* ── Discharge Modal ────────────────────────────────────────────────────── */
const DischargeModal = ({ isOpen, onClose, onSuccess, admission }) => {
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({ finalDiagnosis: '', treatmentSummary: '', dischargeNotes: '', followUpInstructions: '' });
  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!admission) return;
    setLoading(true);
    try {
      await ipdApi.dischargePatient(admission.id, {
        finalDiagnosis: form.finalDiagnosis,
        treatmentSummary: form.treatmentSummary || undefined,
        dischargeNotes: form.dischargeNotes || undefined,
        followUpInstructions: form.followUpInstructions || undefined,
      });
      toast.success('Patient discharged successfully!');
      onSuccess();
      onClose();
      setForm({ finalDiagnosis: '', treatmentSummary: '', dischargeNotes: '', followUpInstructions: '' });
    } catch (err) {
      toast.error(err.response?.data?.message ?? 'Discharge failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Discharge Patient" maxWidth="600px">
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
          Discharging: <strong>{admission?.patientName ?? admission?.id}</strong>
        </p>
        <div className="form-group">
          <label className="form-label">Final Diagnosis *</label>
          <input required value={form.finalDiagnosis} onChange={(e) => set('finalDiagnosis', e.target.value)} placeholder="Post-op recovery – stable" />
        </div>
        <div className="form-group">
          <label className="form-label">Treatment Summary</label>
          <textarea rows={2} value={form.treatmentSummary} onChange={(e) => set('treatmentSummary', e.target.value)} placeholder="IV antibiotics for 5 days, wound dressing" />
        </div>
        <div className="form-group">
          <label className="form-label">Discharge Notes</label>
          <textarea rows={2} value={form.dischargeNotes} onChange={(e) => set('dischargeNotes', e.target.value)} placeholder="Patient advised bed rest for 1 week" />
        </div>
        <div className="form-group">
          <label className="form-label">Follow-up Instructions</label>
          <textarea rows={2} value={form.followUpInstructions} onChange={(e) => set('followUpInstructions', e.target.value)} placeholder="Return in 2 weeks for stitches removal" />
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
          <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Discharging…' : 'Confirm Discharge'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

/* ── Status color helper ────────────────────────────────────────────────── */
const statusStyle = (status) => {
  switch (status) {
    case 'ADMITTED': return { background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444' };
    case 'DISCHARGED': return { background: 'rgba(16, 185, 129, 0.1)', color: '#10b981' };
    case 'TRANSFERRED': return { background: 'rgba(245, 158, 11, 0.1)', color: '#f59e0b' };
    default: return { background: 'rgba(139, 92, 246, 0.1)', color: '#8b5cf6' };
  }
};

/* ── Main IPD Page ──────────────────────────────────────────────────────── */
export default function IpdManagement() {
  const [admissions, setAdmissions] = useState([]);
  const [beds, setBeds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAdmit, setShowAdmit] = useState(false);
  const [transferTarget, setTransferTarget] = useState(null);
  const [dischargeTarget, setDischargeTarget] = useState(null);

  const fetchAll = useCallback(async () => {
    setLoading(true);
    try {
      const [admRes, bedRes] = await Promise.allSettled([
        ipdApi.getAll({ page: 0, size: 50 }),
        ipdApi.getAvailableBeds(),
      ]);

      if (admRes.status === 'fulfilled') {
        const payload = admRes.value.data;
        const list = payload?.data?.content ?? payload?.content ?? (Array.isArray(payload?.data) ? payload.data : (Array.isArray(payload) ? payload : []));
        setAdmissions(list);
      } else {
        setAdmissions([]);
      }

      if (bedRes.status === 'fulfilled') {
        const payload = bedRes.value.data;
        const list = payload?.data?.content ?? payload?.content ?? (Array.isArray(payload?.data) ? payload.data : (Array.isArray(payload) ? payload : []));
        setBeds(list);
      } else {
        setBeds([]);
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchAll(); }, [fetchAll]);

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '700', color: 'var(--text-primary)' }}>Inpatient Admissions (IPD)</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Manage ward allocation, bed availability, transfers, and discharge summaries</p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button className="btn btn-secondary" onClick={fetchAll} title="Refresh">
            <RefreshCw size={18} />
          </button>
          <button className="btn btn-primary" onClick={() => setShowAdmit(true)}>
            <UserPlus size={18} style={{ marginRight: '0.5rem' }} /> Admit Patient
          </button>
        </div>
      </div>

      {/* Available beds count */}
      <div style={{ display: 'flex', gap: '1rem', marginBottom: '1.5rem' }}>
        <div style={{
          padding: '0.75rem 1.25rem', borderRadius: '10px',
          background: 'rgba(16, 185, 129, 0.08)', border: '1px solid rgba(16,185,129,0.2)',
          display: 'flex', alignItems: 'center', gap: '0.5rem',
        }}>
          <Bed size={18} style={{ color: '#10b981' }} />
          <span style={{ fontWeight: 600, color: '#10b981' }}>{beds.length}</span>
          <span style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>beds available</span>
        </div>
        <div style={{
          padding: '0.75rem 1.25rem', borderRadius: '10px',
          background: 'rgba(239, 68, 68, 0.08)', border: '1px solid rgba(239,68,68,0.2)',
          display: 'flex', alignItems: 'center', gap: '0.5rem',
        }}>
          <UserPlus size={18} style={{ color: '#ef4444' }} />
          <span style={{ fontWeight: 600, color: '#ef4444' }}>{admissions.filter((a) => a.status === 'ADMITTED').length}</span>
          <span style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>currently admitted</span>
        </div>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '4rem', color: 'var(--text-muted)' }}>
          <div className="skeleton" style={{ width: '48px', height: '48px', borderRadius: '50%', margin: '0 auto 1rem' }} />
          <p>Loading admissions…</p>
        </div>
      ) : admissions.length === 0 ? (
        <div className="empty-state">
          <Bed size={48} />
          <h3>No active admissions</h3>
          <p>Admit a patient to get started.</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(380px, 1fr))', gap: '1.5rem' }}>
          {admissions.map((adm) => (
            <div key={adm.id} className="card" style={{ padding: '1.5rem', borderRadius: '12px', border: '1px solid var(--color-border)', background: 'var(--color-bg-elevated)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  <div style={{ padding: '0.5rem', borderRadius: '8px', background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444' }}>
                    <Bed size={24} />
                  </div>
                  <div>
                    <h3 style={{ fontSize: '1.05rem', fontWeight: '600' }}>{adm.patientName ?? adm.patientId ?? '—'}</h3>
                    <span style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
                      Admitted: {adm.admissionDate ? new Date(adm.admissionDate).toLocaleDateString() : '—'}
                    </span>
                  </div>
                </div>
                <span className="badge" style={{ ...statusStyle(adm.status), padding: '0.25rem 0.6rem', borderRadius: '6px', fontSize: '0.78rem' }}>
                  {adm.status}
                </span>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.9rem' }}>
                <div><strong>Doctor:</strong> {adm.doctorName ?? adm.doctorId ?? '—'}</div>
                <div style={{ marginTop: '0.5rem', background: 'rgba(255,255,255,0.03)', padding: '0.75rem', borderRadius: '8px' }}>
                  <div><strong>Ward:</strong> {adm.wardName ?? adm.ward?.name ?? '—'}</div>
                  <div><strong>Bed:</strong> {adm.bedNumber ?? adm.bed?.bedNumber ?? '—'}</div>
                  {adm.reason && <div style={{ marginTop: '0.2rem', color: 'var(--text-secondary)' }}>Reason: {adm.reason}</div>}
                </div>
              </div>

              {adm.status === 'ADMITTED' && (
                <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1rem' }}>
                  <button
                    className="btn btn-secondary btn-sm"
                    style={{ flex: 1 }}
                    onClick={() => setTransferTarget(adm)}
                  >
                    <ArrowRightLeft size={14} style={{ marginRight: '0.3rem' }} /> Transfer
                  </button>
                  <button
                    className="btn btn-primary btn-sm"
                    style={{ flex: 1, background: '#10b981' }}
                    onClick={() => setDischargeTarget(adm)}
                  >
                    <LogOut size={14} style={{ marginRight: '0.3rem' }} /> Discharge
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      <AdmitPatientModal isOpen={showAdmit} onClose={() => setShowAdmit(false)} onSuccess={fetchAll} beds={beds} />
      <TransferModal
        isOpen={!!transferTarget}
        onClose={() => setTransferTarget(null)}
        onSuccess={fetchAll}
        admission={transferTarget}
        beds={beds}
      />
      <DischargeModal
        isOpen={!!dischargeTarget}
        onClose={() => setDischargeTarget(null)}
        onSuccess={fetchAll}
        admission={dischargeTarget}
      />
    </div>
  );
}
