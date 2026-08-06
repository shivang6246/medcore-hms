import React, { useState, useEffect, useCallback } from 'react';
import { Video, Plus, RefreshCw, Play, CheckCircle, Clock, Users, PhoneOff } from 'lucide-react';
import { telemedicineApi } from '../api/telemedicine.api';
import useAuthStore from '../store/authStore';
import toast from 'react-hot-toast';
import Modal from '../components/ui/Modal';
import { validateUUIDs } from '../utils/uuid';

/* ── Create Session Modal ───────────────────────────────────────────────── */
const CreateSessionModal = ({ isOpen, onClose, onSuccess }) => {
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    patientId: '', doctorId: '', appointmentId: '',
    scheduledStartTime: '', notes: '',
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
      await telemedicineApi.createSession({
        patientId: form.patientId.trim(),
        doctorId: form.doctorId.trim(),
        appointmentId: form.appointmentId?.trim() || undefined,
        scheduledStartTime: form.scheduledStartTime ? new Date(form.scheduledStartTime).toISOString() : undefined,
        notes: form.notes || undefined,
      });
      toast.success('Consultation room created!');
      onSuccess();
      onClose();
      setForm({ patientId: '', doctorId: '', appointmentId: '', scheduledStartTime: '', notes: '' });
    } catch (err) {
      toast.error(err.response?.data?.message ?? err.response?.data?.detail ?? 'Failed to create session.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Create Consultation Room" maxWidth="620px">
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
            <label className="form-label">Scheduled Time</label>
            <input type="datetime-local" value={form.scheduledStartTime} onChange={(e) => set('scheduledStartTime', e.target.value)} />
          </div>
        </div>
        <div className="form-group">
          <label className="form-label">Notes</label>
          <textarea rows={2} value={form.notes} onChange={(e) => set('notes', e.target.value)} placeholder="Routine follow-up consultation" />
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
          <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Creating…' : 'Create Room'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

/* ── Status badge helper ────────────────────────────────────────────────── */
const statusBadge = (status) => {
  const map = {
    WAITING_ROOM: { bg: 'rgba(245,158,11,0.1)', color: '#f59e0b', label: 'Waiting Room', icon: Clock },
    IN_PROGRESS: { bg: 'rgba(59,130,246,0.1)', color: '#3b82f6', label: 'In Progress', icon: Play },
    COMPLETED: { bg: 'rgba(16,185,129,0.1)', color: '#10b981', label: 'Completed', icon: CheckCircle },
    SCHEDULED: { bg: 'rgba(139,92,246,0.1)', color: '#8b5cf6', label: 'Scheduled', icon: Clock },
    CANCELLED: { bg: 'rgba(239,68,68,0.1)', color: '#ef4444', label: 'Cancelled', icon: PhoneOff },
  };
  return map[status] ?? { bg: 'rgba(100,100,100,0.1)', color: 'var(--text-secondary)', label: status, icon: Video };
};

/* ── Session Card ───────────────────────────────────────────────────────── */
const SessionCard = ({ session, onAction }) => {
  const badge = statusBadge(session.status);
  const BadgeIcon = badge.icon;

  return (
    <div className="card" style={{ padding: '1.5rem', borderRadius: '12px', border: '1px solid var(--color-border)', background: 'var(--color-bg-elevated)' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div style={{ padding: '0.5rem', borderRadius: '8px', background: 'rgba(59,130,246,0.1)', color: '#3b82f6' }}>
            <Video size={24} />
          </div>
          <div>
            <h3 style={{ fontSize: '1.05rem', fontWeight: '600' }}>{session.roomCode ?? `ROOM-${session.id?.slice(0,8)}`}</h3>
            <span style={{ fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
              {session.scheduledStartTime
                ? new Date(session.scheduledStartTime).toLocaleString()
                : 'No scheduled time'}
            </span>
          </div>
        </div>
        <span style={{ background: badge.bg, color: badge.color, padding: '0.25rem 0.6rem', borderRadius: '6px', fontSize: '0.78rem', display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
          <BadgeIcon size={12} /> {badge.label}
        </span>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem', fontSize: '0.9rem', marginBottom: '1rem' }}>
        <div><strong>Patient:</strong> {session.patientName ?? session.patientId ?? '—'}</div>
        <div><strong>Doctor:</strong> {session.doctorName ?? session.doctorId ?? '—'}</div>
        {session.meetingToken && (
          <div style={{ marginTop: '0.3rem', background: 'rgba(255,255,255,0.03)', padding: '0.5rem 0.75rem', borderRadius: '8px', fontFamily: 'monospace', fontSize: '0.82rem', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            Token: {session.meetingToken}
          </div>
        )}
      </div>

      <div style={{ display: 'flex', gap: '0.5rem' }}>
        {session.meetingUrl && (
          <button
            className="btn btn-primary btn-sm"
            style={{ flex: 1 }}
            onClick={() => window.open(session.meetingUrl, '_blank')}
          >
            <Play size={14} style={{ marginRight: '0.3rem' }} /> Join Room
          </button>
        )}
        {session.status === 'WAITING_ROOM' && (
          <button
            className="btn btn-secondary btn-sm"
            style={{ flex: 1 }}
            onClick={() => onAction('start', session.id)}
          >
            Start Session
          </button>
        )}
        {session.status === 'IN_PROGRESS' && (
          <button
            className="btn btn-secondary btn-sm"
            style={{ flex: 1, background: 'rgba(16,185,129,0.1)', color: '#10b981', border: '1px solid rgba(16,185,129,0.3)' }}
            onClick={() => onAction('complete', session.id)}
          >
            <CheckCircle size={14} style={{ marginRight: '0.3rem' }} /> Complete
          </button>
        )}
      </div>
    </div>
  );
};

/* ── Main Telemedicine Page ─────────────────────────────────────────────── */
export default function Telemedicine() {
  const { user } = useAuthStore();
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);

  const fetchSessions = useCallback(async () => {
    setLoading(true);
    try {
      // Try to load doctor's waiting room if user is a doctor
      const isDoctor = user?.roles?.some((r) => r === 'DOCTOR');
      let list = [];

      if (isDoctor && user?.doctorId) {
        const res = await telemedicineApi.getDoctorWaitingRoom(user.doctorId);
        const payload = res.data;
        list = payload?.data?.content ?? payload?.content ?? (Array.isArray(payload?.data) ? payload.data : (Array.isArray(payload) ? payload : []));
      } else if (user?.patientId) {
        const res = await telemedicineApi.getPatientHistory(user.patientId, { page: 0, size: 50 });
        const payload = res.data;
        list = payload?.data?.content ?? payload?.content ?? (Array.isArray(payload?.data) ? payload.data : (Array.isArray(payload) ? payload : []));
      }

      setSessions(list);
    } catch {
      setSessions([]);
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => { fetchSessions(); }, [fetchSessions]);

  const handleAction = async (action, sessionId) => {
    try {
      if (action === 'start') {
        await telemedicineApi.startSession(sessionId);
        toast.success('Session started!');
      } else if (action === 'complete') {
        await telemedicineApi.completeSession(sessionId, '');
        toast.success('Session completed!');
      }
      fetchSessions();
    } catch (err) {
      toast.error(err.response?.data?.message ?? 'Action failed.');
    }
  };

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '700', color: 'var(--text-primary)' }}>Telemedicine & Virtual Rooms</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Virtual video consultation rooms, waiting queues, and meeting tokens</p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button className="btn btn-secondary" onClick={fetchSessions} title="Refresh">
            <RefreshCw size={18} />
          </button>
          <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
            <Video size={18} style={{ marginRight: '0.5rem' }} /> Create Consultation Room
          </button>
        </div>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '4rem', color: 'var(--text-muted)' }}>
          <div className="skeleton" style={{ width: '48px', height: '48px', borderRadius: '50%', margin: '0 auto 1rem' }} />
          <p>Loading sessions…</p>
        </div>
      ) : sessions.length === 0 ? (
        <div className="empty-state">
          <Video size={48} />
          <h3>No active sessions</h3>
          <p>Create a consultation room to start a video visit.</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(380px, 1fr))', gap: '1.5rem' }}>
          {sessions.map((s) => (
            <SessionCard key={s.id} session={s} onAction={handleAction} />
          ))}
        </div>
      )}

      <CreateSessionModal isOpen={showCreate} onClose={() => setShowCreate(false)} onSuccess={fetchSessions} />
    </div>
  );
}
