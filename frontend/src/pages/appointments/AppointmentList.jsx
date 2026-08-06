import React, { useState, useEffect, useCallback } from 'react';
import { CalendarDays, RefreshCw, Clock, User, Stethoscope, Plus } from 'lucide-react';
import { appointmentApi } from '../../api/appointment.api';
import { doctorApi } from '../../api/doctor.api';
import { patientApi } from '../../api/patient.api';
import useAuthStore from '../../store/authStore';
import toast from 'react-hot-toast';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Pagination from '../../components/ui/Pagination';
import Modal from '../../components/ui/Modal';

const STATUS_VARIANT = {
  SCHEDULED: 'info',
  CONFIRMED: 'primary',
  IN_PROGRESS: 'warning',
  COMPLETED: 'success',
  CANCELLED: 'danger',
  NO_SHOW: 'neutral',
  CHECKED_IN: 'warning',
};

const AppointmentList = () => {
  const { user, hasAnyRole } = useAuthStore();
  const isPatient = hasAnyRole(['PATIENT']);
  const canBook = hasAnyRole(['PATIENT', 'RECEPTIONIST', 'SUPER_ADMIN', 'HOSPITAL_ADMIN']);
  const isStaffBooker = canBook && !isPatient;

  const [data, setData] = useState(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);

  const [bookOpen, setBookOpen] = useState(false);
  const [doctors, setDoctors] = useState([]);
  const [slots, setSlots] = useState([]);
  const [booking, setBooking] = useState(false);
  const [lookingUpPatient, setLookingUpPatient] = useState(false);
  const [patientCode, setPatientCode] = useState('');
  const [lookedUpPatient, setLookedUpPatient] = useState(null);
  const [form, setForm] = useState({
    patientId: '',
    doctorId: '',
    slotDate: new Date().toISOString().slice(0, 10),
    slotId: '',
    type: 'IN_PERSON',
    chiefComplaint: '',
  });

  const fetchAppointments = useCallback(async () => {
    setLoading(true);
    try {
      const params = { page, size: 10, sort: 'appointmentDate,desc' };
      let res;

      if (isPatient) {
        if (!user?.patientId) {
          toast.error('No patient profile linked to your account. Contact hospital reception.');
          setData({ content: [], totalPages: 0, totalElements: 0 });
          return;
        }
        res = await appointmentApi.getByPatient(user.patientId, params);
      } else {
        if (user?.hospitalId) params.hospitalId = user.hospitalId;
        res = await appointmentApi.getAll(params);
      }

      setData(res.data.data);
    } catch (err) {
      const status = err.response?.status;
      if (status === 403) {
        toast.error('You do not have permission to view appointments.');
      } else {
        toast.error(err.response?.data?.detail ?? 'Failed to fetch appointments');
      }
    } finally {
      setLoading(false);
    }
  }, [page, user?.hospitalId, user?.patientId, isPatient]);

  useEffect(() => { fetchAppointments(); }, [fetchAppointments]);

  const resetBookForm = () => {
    setPatientCode('');
    setLookedUpPatient(null);
    setSlots([]);
    setForm({
      patientId: '',
      doctorId: '',
      slotDate: new Date().toISOString().slice(0, 10),
      slotId: '',
      type: 'IN_PERSON',
      chiefComplaint: '',
    });
  };

  const openBookModal = async () => {
    setBookOpen(true);
    setPatientCode('');
    setLookedUpPatient(null);
    setSlots([]);
    setForm({
      patientId: isPatient ? (user?.patientId ?? '') : '',
      doctorId: '',
      slotDate: new Date().toISOString().slice(0, 10),
      slotId: '',
      type: 'IN_PERSON',
      chiefComplaint: '',
    });
    try {
      const res = await doctorApi.getAll({ page: 0, size: 50, sort: 'createdAt,desc' });
      const list = res.data?.data?.content ?? [];
      setDoctors(list.filter((d) => d.isActive !== false));
    } catch {
      toast.error('Failed to load doctors');
    }
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
    setLookingUpPatient(true);
    setLookedUpPatient(null);
    setForm((f) => ({ ...f, patientId: '' }));
    try {
      const res = await patientApi.getByPatientId(code, user.hospitalId);
      const p = res.data?.data ?? res.data;
      if (!p?.id) {
        toast.error('Patient not found for that ID');
        return;
      }
      setLookedUpPatient(p);
      setForm((f) => ({ ...f, patientId: p.id }));
      toast.success(`Found ${p.firstName} ${p.lastName}`);
    } catch (err) {
      toast.error(err.response?.data?.detail ?? err.response?.data?.message ?? 'Patient lookup failed');
    } finally {
      setLookingUpPatient(false);
    }
  };

  useEffect(() => {
    const loadSlots = async () => {
      if (!form.doctorId || !form.slotDate) {
        setSlots([]);
        return;
      }
      try {
        const res = await appointmentApi.getSlots(form.doctorId, form.slotDate);
        const list = res.data?.data ?? [];
        setSlots(list.filter((s) => s.status === 'AVAILABLE'));
      } catch {
        setSlots([]);
      }
    };
    loadSlots();
  }, [form.doctorId, form.slotDate]);

  const submitBooking = async (e) => {
    e.preventDefault();
    const patientId = isPatient ? user?.patientId : form.patientId;
    if (!patientId) {
      toast.error(isPatient
        ? 'No patient profile linked to your account.'
        : 'Look up a patient by Patient ID first.');
      return;
    }
    if (!form.doctorId || !form.slotId) {
      toast.error('Please select a doctor and an available slot.');
      return;
    }
    setBooking(true);
    try {
      await appointmentApi.book({
        patientId,
        doctorId: form.doctorId,
        slotId: form.slotId,
        type: form.type,
        chiefComplaint: form.chiefComplaint || undefined,
      });
      toast.success('Appointment booked successfully');
      setBookOpen(false);
      resetBookForm();
      fetchAppointments();
    } catch (err) {
      toast.error(err.response?.data?.detail ?? err.response?.data?.title ?? 'Failed to book appointment');
    } finally {
      setBooking(false);
    }
  };

  const formatDate = (d) => d ? new Date(d).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }) : '—';
  const formatTime = (t) => (typeof t === 'string' ? t.slice(0, 5) : '—');

  const doctorLabel = (d) => {
    if (d.fullName) return d.fullName;
    const name = `Dr. ${d.firstName ?? ''} ${d.lastName ?? ''}`.trim();
    return name !== 'Dr.' ? name : (d.email ?? 'Doctor');
  };

  const displayPatient = (a) => {
    if (a.patientName) return a.patientName;
    const name = `${a.patient?.firstName ?? ''} ${a.patient?.lastName ?? ''}`.trim();
    return name || '—';
  };

  const displayDoctor = (a) => {
    if (a.doctorName) {
      return a.doctorName.startsWith('Dr.') ? a.doctorName : `Dr. ${a.doctorName}`;
    }
    const name = `Dr. ${a.doctor?.firstName ?? ''} ${a.doctor?.lastName ?? ''}`.trim();
    return name !== 'Dr.' ? name : '—';
  };

  return (
    <div className="animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Appointments</h1>
          <p className="page-subtitle">
            {isPatient ? 'Book and track your appointments' : 'Manage patient appointments and schedules'}
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <Button variant="secondary" icon={RefreshCw} onClick={fetchAppointments} size="sm">Refresh</Button>
          {canBook && (
            <Button icon={Plus} onClick={openBookModal} size="sm">Book Appointment</Button>
          )}
        </div>
      </div>

      <Card padding={false}>
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Appointment #</th>
                <th>Patient</th>
                <th>Doctor</th>
                <th className="hide-md">Date</th>
                <th className="hide-md">Time</th>
                <th>Type</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                Array.from({ length: 6 }).map((_, i) => (
                  <tr key={i}>
                    {Array.from({ length: 7 }).map((_, j) => (
                      <td key={j}><div className="skeleton" style={{ height: 18, width: '80%' }} /></td>
                    ))}
                  </tr>
                ))
              ) : data?.content?.length ? (
                data.content.map((a) => (
                  <tr key={a.id}>
                    <td>
                      <code style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-primary-light)', background: 'rgba(59,130,246,0.08)', padding: '0.2rem 0.5rem', borderRadius: 'var(--radius-sm)' }}>
                        {a.appointmentNumber}
                      </code>
                    </td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <User size={14} color="var(--color-accent)" />
                        <span>{displayPatient(a)}</span>
                      </div>
                    </td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <Stethoscope size={14} color="var(--color-secondary)" />
                        <span>{displayDoctor(a)}</span>
                      </div>
                    </td>
                    <td className="hide-md">{formatDate(a.appointmentDate)}</td>
                    <td className="hide-md">
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                        <Clock size={12} />
                        {formatTime(a.startTime)} – {formatTime(a.endTime)}
                      </div>
                    </td>
                    <td>
                      <span className="badge badge-neutral">{a.type?.replace(/_/g, ' ') ?? '—'}</span>
                    </td>
                    <td>
                      <span className={`badge badge-${STATUS_VARIANT[a.status] ?? 'neutral'}`}>
                        {a.status?.replace(/_/g, ' ') ?? '—'}
                      </span>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={7}>
                    <div className="empty-state">
                      <CalendarDays size={40} />
                      <h3>No appointments found</h3>
                      <p>{isPatient ? 'Book your first appointment to get started.' : 'Appointments will appear here once booked.'}</p>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        {data && (
          <Pagination page={page} totalPages={data.totalPages} totalElements={data.totalElements} size={10} onPageChange={setPage} />
        )}
      </Card>

      <Modal isOpen={bookOpen} onClose={() => { setBookOpen(false); resetBookForm(); }} title="Book Appointment" maxWidth="640px">
        <form onSubmit={submitBooking} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {isPatient && !user?.patientId && (
            <div className="alert alert-danger">
              Your login is not linked to a patient profile. Ask reception to register you with the same email, then re-login.
            </div>
          )}
          {isStaffBooker && (
            <>
              <div className="form-group">
                <label className="form-label">Patient ID *</label>
                <div style={{ display: 'flex', gap: '0.5rem' }}>
                  <input
                    className="form-input"
                    required
                    value={patientCode}
                    onChange={(e) => setPatientCode(e.target.value)}
                    placeholder="e.g. P-2026-00001"
                    style={{ flex: 1 }}
                  />
                  <Button
                    type="button"
                    variant="secondary"
                    loading={lookingUpPatient}
                    onClick={handleLookupPatient}
                  >
                    Lookup
                  </Button>
                </div>
              </div>
              {lookedUpPatient && (
                <div style={{ padding: '0.75rem 1rem', background: 'rgba(255,255,255,0.04)', borderRadius: '8px', fontSize: '0.9rem' }}>
                  <strong>{lookedUpPatient.firstName} {lookedUpPatient.lastName}</strong>
                  <span style={{ color: 'var(--text-muted)' }}> · {lookedUpPatient.patientId}</span>
                  {lookedUpPatient.phone && (
                    <span style={{ color: 'var(--text-muted)' }}> · {lookedUpPatient.phone}</span>
                  )}
                </div>
              )}
            </>
          )}
          <div className="form-group">
            <label className="form-label">Doctor</label>
            <select
              className="form-input"
              required
              value={form.doctorId}
              onChange={(e) => setForm((f) => ({ ...f, doctorId: e.target.value, slotId: '' }))}
            >
              <option value="">Select doctor</option>
              {doctors.map((d) => (
                <option key={d.id} value={d.id}>
                  {doctorLabel(d)}{d.specialization ? ` — ${d.specialization}` : ''}
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Date</label>
            <input
              type="date"
              className="form-input"
              required
              value={form.slotDate}
              min={new Date().toISOString().slice(0, 10)}
              onChange={(e) => setForm((f) => ({ ...f, slotDate: e.target.value, slotId: '' }))}
            />
          </div>
          <div className="form-group">
            <label className="form-label">Available slot</label>
            <select
              className="form-input"
              required
              value={form.slotId}
              onChange={(e) => setForm((f) => ({ ...f, slotId: e.target.value }))}
            >
              <option value="">
                {slots.length
                  ? 'Select slot'
                  : 'No available slots — open the doctor profile and use Generate slots (admin)'}
              </option>
              {slots.map((s) => (
                <option key={s.id} value={s.id}>
                  {formatTime(s.startTime)} – {formatTime(s.endTime)}
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Type</label>
            <select
              className="form-input"
              value={form.type}
              onChange={(e) => setForm((f) => ({ ...f, type: e.target.value }))}
            >
              <option value="IN_PERSON">In Person</option>
              <option value="TELECONSULTATION">Teleconsultation</option>
              <option value="FOLLOW_UP">Follow Up</option>
              <option value="EMERGENCY">Emergency</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Chief complaint</label>
            <textarea
              className="form-input"
              rows={3}
              value={form.chiefComplaint}
              onChange={(e) => setForm((f) => ({ ...f, chiefComplaint: e.target.value }))}
              placeholder="Brief reason for visit"
            />
          </div>
          <Button
            type="submit"
            loading={booking}
            disabled={
              (isPatient && !user?.patientId)
              || (isStaffBooker && !form.patientId)
              || !form.slotId
            }
          >
            Confirm Booking
          </Button>
        </form>
      </Modal>
    </div>
  );
};

export default AppointmentList;
