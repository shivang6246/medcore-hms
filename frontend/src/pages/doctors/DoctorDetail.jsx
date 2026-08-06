import React, { useCallback, useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  ArrowLeft, Mail, Phone, Award, DollarSign, Clock, CalendarPlus, Trash2, CalendarRange,
} from 'lucide-react';
import { doctorApi } from '../../api/doctor.api';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import { StatusBadge } from '../../components/ui/Badge';
import useAuthStore from '../../store/authStore';
import toast from 'react-hot-toast';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];

const emptySchedule = {
  dayOfWeek: 'MONDAY',
  startTime: '09:00',
  endTime: '17:00',
  lunchBreakStart: '13:00',
  lunchBreakEnd: '14:00',
  slotDurationMinutes: 30,
};

const DoctorDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { hasAnyRole } = useAuthStore();
  const canManageSlots = hasAnyRole(['SUPER_ADMIN', 'HOSPITAL_ADMIN']);

  const [doctor, setDoctor] = useState(null);
  const [schedules, setSchedules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [scheduleForm, setScheduleForm] = useState(emptySchedule);
  const [savingSchedule, setSavingSchedule] = useState(false);
  const [slotRange, setSlotRange] = useState(() => {
    const from = new Date();
    const to = new Date();
    to.setDate(to.getDate() + 13);
    return {
      fromDate: from.toISOString().slice(0, 10),
      toDate: to.toISOString().slice(0, 10),
    };
  });
  const [generating, setGenerating] = useState(false);

  const loadSchedules = useCallback(async () => {
    if (!canManageSlots) return;
    try {
      const res = await doctorApi.getSchedules(id);
      setSchedules(res.data.data ?? []);
    } catch {
      setSchedules([]);
    }
  }, [id, canManageSlots]);

  useEffect(() => {
    const fetch = async () => {
      setLoading(true);
      try {
        const res = await doctorApi.getById(id);
        setDoctor(res.data.data);
        await loadSchedules();
      } catch {
        toast.error('Doctor not found');
        navigate('/doctors');
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, [id, navigate, loadSchedules]);

  const setSch = (k, v) => setScheduleForm((f) => ({ ...f, [k]: v }));

  const handleAddSchedule = async (e) => {
    e.preventDefault();
    setSavingSchedule(true);
    try {
      const payload = {
        ...scheduleForm,
        lunchBreakStart: scheduleForm.lunchBreakStart || null,
        lunchBreakEnd: scheduleForm.lunchBreakEnd || null,
        slotDurationMinutes: Number(scheduleForm.slotDurationMinutes) || 30,
      };
      await doctorApi.createSchedule(id, payload);
      toast.success('Weekly schedule added');
      setScheduleForm(emptySchedule);
      await loadSchedules();
    } catch (err) {
      toast.error(err.response?.data?.detail ?? err.response?.data?.message ?? 'Failed to add schedule');
    } finally {
      setSavingSchedule(false);
    }
  };

  const handleDeleteSchedule = async (scheduleId) => {
    if (!window.confirm('Delete this weekly schedule?')) return;
    try {
      await doctorApi.deleteSchedule(id, scheduleId);
      toast.success('Schedule deleted');
      await loadSchedules();
    } catch (err) {
      toast.error(err.response?.data?.detail ?? err.response?.data?.message ?? 'Failed to delete schedule');
    }
  };

  const handleGenerateSlots = async (e) => {
    e.preventDefault();
    if (!schedules.some((s) => s.isActive)) {
      toast.error('Add at least one active weekly schedule before generating slots');
      return;
    }
    setGenerating(true);
    try {
      const res = await doctorApi.generateSlots(id, slotRange.fromDate, slotRange.toDate);
      const count = res.data?.data?.length ?? 0;
      toast.success(res.data?.message ?? `${count} slots generated`);
    } catch (err) {
      toast.error(err.response?.data?.detail ?? err.response?.data?.message ?? 'Failed to generate slots');
    } finally {
      setGenerating(false);
    }
  };

  if (loading) return (
    <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
      <div style={{ width: 40, height: 40, border: '3px solid var(--color-border)', borderTopColor: 'var(--color-primary)', borderRadius: '50%', animation: 'spin 0.7s linear infinite' }} />
    </div>
  );
  if (!doctor) return null;

  const initials = `${doctor.firstName?.[0] ?? ''}${doctor.lastName?.[0] ?? ''}`.toUpperCase();

  return (
    <div className="animate-fade-in">
      <div className="page-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <Button variant="secondary" icon={ArrowLeft} size="sm" onClick={() => navigate('/doctors')}>Back</Button>
          <div>
            <h1 className="page-title">Dr. {doctor.firstName} {doctor.lastName}</h1>
            <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.25rem' }}>
              <StatusBadge isActive={doctor.isActive} />
              <span className={`badge ${doctor.available ? 'badge-info' : 'badge-neutral'}`}>
                {doctor.available ? 'Available' : 'Unavailable'}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '1.5rem' }}>
        <Card>
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem' }}>
            <div
              style={{
                width: 96,
                height: 96,
                borderRadius: '50%',
                background: 'linear-gradient(135deg, #8b5cf6, #3b82f6)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '2rem',
                fontWeight: 800,
                color: '#fff',
                boxShadow: '0 0 32px rgba(139,92,246,0.3)',
              }}
            >
              {initials}
            </div>
            <div style={{ textAlign: 'center' }}>
              <h3 style={{ fontWeight: 700 }}>Dr. {doctor.firstName} {doctor.lastName}</h3>
              <p style={{ color: 'var(--color-secondary)', fontWeight: 600, fontSize: 'var(--font-size-sm)', marginTop: '0.25rem' }}>{doctor.specialization}</p>
            </div>
            <div style={{ width: '100%', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
              {[
                { icon: Mail, val: doctor.email },
                { icon: Phone, val: doctor.phone },
                { icon: Award, val: doctor.qualification },
                { icon: DollarSign, val: doctor.consultationFee != null ? `₹${doctor.consultationFee}` : null },
                { icon: Clock, val: doctor.experienceYears != null ? `${doctor.experienceYears} years exp.` : null },
              ].map(({ icon: Icon, val }, i) => val && (
                <div key={i} style={{ display: 'flex', alignItems: 'center', gap: '0.625rem', padding: '0.5rem 0.625rem', background: 'rgba(255,255,255,0.025)', borderRadius: 'var(--radius-sm)' }}>
                  <Icon size={14} color="var(--color-primary)" />
                  <span style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-secondary)' }}>{val}</span>
                </div>
              ))}
            </div>
          </div>
        </Card>

        <Card>
          <h3 style={{ fontWeight: 700, marginBottom: '1rem', fontSize: 'var(--font-size-lg)' }}>Professional Details</h3>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            {[
              { label: 'License Number', value: doctor.licenseNumber },
              { label: 'Employee ID', value: doctor.employeeId },
              { label: 'Gender', value: doctor.gender },
              { label: 'Hospital', value: doctor.hospital?.name },
              { label: 'Department', value: doctor.department?.name ?? '—' },
              { label: 'Created', value: doctor.createdAt ? new Date(doctor.createdAt).toLocaleDateString() : '—' },
            ].map(({ label, value }) => (
              <div key={label} style={{ padding: '0.75rem', background: 'rgba(255,255,255,0.025)', borderRadius: 'var(--radius-md)' }}>
                <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.06em', fontWeight: 600, marginBottom: '0.25rem' }}>{label}</p>
                <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-primary)', fontWeight: 500 }}>{value || '—'}</p>
              </div>
            ))}
          </div>

          {doctor.bio && (
            <div style={{ marginTop: '1.25rem' }}>
              <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.06em', fontWeight: 600, marginBottom: '0.5rem' }}>Bio</p>
              <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-secondary)', lineHeight: 1.7, padding: '0.875rem', background: 'rgba(255,255,255,0.025)', borderRadius: 'var(--radius-md)' }}>{doctor.bio}</p>
            </div>
          )}
        </Card>
      </div>

      {canManageSlots && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem', marginTop: '1.5rem' }}>
          <Card>
            <h3 style={{ fontWeight: 700, marginBottom: '0.35rem', fontSize: 'var(--font-size-lg)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <CalendarRange size={18} /> Weekly schedules
            </h3>
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-muted)', marginBottom: '1rem' }}>
              Define working days first. Slots are generated from these hours.
            </p>

            {schedules.length === 0 ? (
              <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-secondary)', marginBottom: '1rem' }}>
                No schedules yet. Add Mon–Fri hours to start.
              </p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', marginBottom: '1rem' }}>
                {schedules.map((s) => (
                  <div
                    key={s.id}
                    style={{
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      gap: '0.75rem',
                      padding: '0.65rem 0.75rem',
                      background: 'rgba(255,255,255,0.025)',
                      borderRadius: 'var(--radius-md)',
                    }}
                  >
                    <div>
                      <p style={{ fontWeight: 600, fontSize: 'var(--font-size-sm)' }}>{s.dayOfWeek}</p>
                      <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                        {String(s.startTime).slice(0, 5)} – {String(s.endTime).slice(0, 5)} · {s.slotDurationMinutes} min
                        {!s.isActive ? ' · inactive' : ''}
                      </p>
                    </div>
                    <Button variant="secondary" size="sm" icon={Trash2} onClick={() => handleDeleteSchedule(s.id)} />
                  </div>
                ))}
              </div>
            )}

            <form onSubmit={handleAddSchedule} style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <div className="form-grid" style={{ gridTemplateColumns: '1fr 1fr' }}>
                <div className="form-group">
                  <label className="form-label">Day</label>
                  <select className="form-input" value={scheduleForm.dayOfWeek} onChange={(e) => setSch('dayOfWeek', e.target.value)}>
                    {DAYS.map((d) => <option key={d} value={d}>{d}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label className="form-label">Slot mins</label>
                  <input className="form-input" type="number" min={5} max={120} value={scheduleForm.slotDurationMinutes} onChange={(e) => setSch('slotDurationMinutes', e.target.value)} />
                </div>
                <div className="form-group">
                  <label className="form-label">Start</label>
                  <input className="form-input" type="time" required value={scheduleForm.startTime} onChange={(e) => setSch('startTime', e.target.value)} />
                </div>
                <div className="form-group">
                  <label className="form-label">End</label>
                  <input className="form-input" type="time" required value={scheduleForm.endTime} onChange={(e) => setSch('endTime', e.target.value)} />
                </div>
                <div className="form-group">
                  <label className="form-label">Lunch start</label>
                  <input className="form-input" type="time" value={scheduleForm.lunchBreakStart} onChange={(e) => setSch('lunchBreakStart', e.target.value)} />
                </div>
                <div className="form-group">
                  <label className="form-label">Lunch end</label>
                  <input className="form-input" type="time" value={scheduleForm.lunchBreakEnd} onChange={(e) => setSch('lunchBreakEnd', e.target.value)} />
                </div>
              </div>
              <Button type="submit" icon={CalendarPlus} loading={savingSchedule}>Add schedule</Button>
            </form>
          </Card>

          <Card>
            <h3 style={{ fontWeight: 700, marginBottom: '0.35rem', fontSize: 'var(--font-size-lg)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <CalendarPlus size={18} /> Generate appointment slots
            </h3>
            <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-muted)', marginBottom: '1rem' }}>
              Creates bookable slots for the date range from active weekly schedules (max 90 days).
            </p>

            <form onSubmit={handleGenerateSlots} style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <div className="form-grid" style={{ gridTemplateColumns: '1fr 1fr' }}>
                <div className="form-group">
                  <label className="form-label">From date</label>
                  <input
                    className="form-input"
                    type="date"
                    required
                    value={slotRange.fromDate}
                    onChange={(e) => setSlotRange((r) => ({ ...r, fromDate: e.target.value }))}
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">To date</label>
                  <input
                    className="form-input"
                    type="date"
                    required
                    value={slotRange.toDate}
                    min={slotRange.fromDate}
                    onChange={(e) => setSlotRange((r) => ({ ...r, toDate: e.target.value }))}
                  />
                </div>
              </div>
              <Button type="submit" icon={CalendarPlus} loading={generating} disabled={schedules.length === 0}>
                Generate slots
              </Button>
              {schedules.length === 0 && (
                <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                  Add a weekly schedule on the left before generating.
                </p>
              )}
            </form>
          </Card>
        </div>
      )}
    </div>
  );
};

export default DoctorDetail;
