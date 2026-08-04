import React, { useState, useEffect, useCallback } from 'react';
import { CalendarDays, RefreshCw, Clock, User, Stethoscope } from 'lucide-react';
import { appointmentApi } from '../../api/appointment.api';
import useAuthStore from '../../store/authStore';
import toast from 'react-hot-toast';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Pagination from '../../components/ui/Pagination';

const STATUS_VARIANT = {
  SCHEDULED: 'info',
  CONFIRMED: 'primary',
  IN_PROGRESS: 'warning',
  COMPLETED: 'success',
  CANCELLED: 'danger',
  NO_SHOW: 'neutral',
};

const AppointmentList = () => {
  const { user } = useAuthStore();
  const [data, setData] = useState(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);

  const fetchAppointments = useCallback(async () => {
    setLoading(true);
    try {
      const params = { page, size: 10, sort: 'createdAt,desc' };
      if (user?.hospitalId) params.hospitalId = user.hospitalId;
      const res = await appointmentApi.getAll(params);
      setData(res.data.data);
    } catch (err) {
      const status = err.response?.status;
      if (status === 403) {
        toast.error('You do not have permission to view appointments.');
      } else {
        toast.error('Failed to fetch appointments');
      }
    } finally {
      setLoading(false);
    }
  }, [page, user?.hospitalId]);

  useEffect(() => { fetchAppointments(); }, [fetchAppointments]);

  const formatDate = (d) => d ? new Date(d).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }) : '—';
  const formatTime = (t) => t ? t.slice(0, 5) : '—';

  return (
    <div className="animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Appointments</h1>
          <p className="page-subtitle">Manage patient appointments and schedules</p>
        </div>
        <Button variant="secondary" icon={RefreshCw} onClick={fetchAppointments} size="sm">Refresh</Button>
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
                        <span>{a.patient?.firstName} {a.patient?.lastName}</span>
                      </div>
                    </td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <Stethoscope size={14} color="var(--color-secondary)" />
                        <span>Dr. {a.doctor?.firstName} {a.doctor?.lastName}</span>
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
                      <p>Appointments will appear here once booked through the system.</p>
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
    </div>
  );
};

export default AppointmentList;
