import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  RefreshCw,
  Users,
  BedDouble,
  CalendarDays,
  Stethoscope,
  CreditCard,
  ArrowUpRight,
  Activity,
} from 'lucide-react';
import toast from 'react-hot-toast';
import StatCard from '../components/ui/StatCard';
import Card from '../components/ui/Card';
import Spinner from '../components/ui/Spinner';
import { DotMatrixChart, GenderDonut } from '../components/ui/charts';
import useAuthStore from '../store/authStore';
import { dashboardApi } from '../api/dashboard.api';
import { appointmentApi } from '../api/appointment.api';
import { authApi } from '../api/auth.api';

const unwrap = (res) => res?.data?.data ?? res?.data ?? null;

const formatCount = (n) => {
  if (n == null) return '—';
  const num = Number(n);
  if (Number.isNaN(num)) return String(n);
  return num.toLocaleString();
};

const formatMoney = (n) => {
  if (n == null) return '—';
  const num = Number(n);
  if (Number.isNaN(num)) return String(n);
  return new Intl.NumberFormat(undefined, {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 0,
  }).format(num);
};

const formatTime = (t) => {
  if (!t) return '—';
  const s = String(t);
  const [hh, mm] = s.split(':');
  if (!hh || !mm) return s;
  const h = Number(hh);
  const suffix = h >= 12 ? 'PM' : 'AM';
  const h12 = h % 12 || 12;
  return `${h12}:${mm} ${suffix}`;
};

const trendToSpark = (trends = []) => trends.map((t) => Number(t.count ?? t.value ?? 0));

const todayISO = () => new Date().toISOString().slice(0, 10);

const mapAppointmentRow = (a) => {
  const name =
    a.patientName ||
    [a.patient?.firstName, a.patient?.lastName].filter(Boolean).join(' ') ||
    a.doctorName ||
    '—';
  return {
    id: a.id || a.appointmentId || `${name}-${a.startTime}`,
    time: formatTime(a.startTime),
    name,
    note: a.chiefComplaint || a.type || a.appointmentStatus || a.status || 'Appointment',
    status: a.appointmentStatus || a.status,
  };
};

const Dashboard = () => {
  const { user, setUser, hasAnyRole } = useAuthStore();
  const [loading, setLoading] = useState(true);
  const [admin, setAdmin] = useState(null);
  const [doctorDash, setDoctorDash] = useState(null);
  const [reception, setReception] = useState(null);
  const [apptTrends, setApptTrends] = useState([]);
  const [todayAppointments, setTodayAppointments] = useState([]);

  const isAdmin = hasAnyRole(['SUPER_ADMIN', 'HOSPITAL_ADMIN']);
  const isDoctor = hasAnyRole(['DOCTOR']);
  const isReception = hasAnyRole(['RECEPTIONIST']);
  const isPatient = hasAnyRole(['PATIENT']);
  const canSeeRevenue = hasAnyRole(['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'ACCOUNTANT']);
  const canSeeApptAnalytics = hasAnyRole(['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR']);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      let profile = useAuthStore.getState().user;
      try {
        const meRes = await authApi.me();
        profile = meRes.data?.data ?? meRes.data;
        if (profile) setUser(profile);
      } catch {
        /* keep cached user */
      }

      const hospitalId = profile?.hospitalId;
      const doctorId = profile?.doctorId;
      const patientId = profile?.patientId;
      const today = todayISO();

      const roles = profile?.roles ?? [];
      const roleHas = (list) => list.some((r) => roles.includes(r));

      const adminRole = roleHas(['SUPER_ADMIN', 'HOSPITAL_ADMIN']);
      const doctorRole = roleHas(['DOCTOR']);
      const receptionRole = roleHas(['RECEPTIONIST']);
      const patientRole = roleHas(['PATIENT']);
      const apptAnalyticsRole = roleHas(['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'DOCTOR']);
      const searchRole = roleHas(['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'RECEPTIONIST', 'NURSE']);

      const tasks = [];

      if (adminRole) {
        tasks.push(
          dashboardApi.getAdminMetrics(hospitalId).then((r) => setAdmin(unwrap(r))).catch(() => setAdmin(null))
        );
      } else setAdmin(null);

      if (doctorRole && doctorId) {
        tasks.push(
          dashboardApi.getDoctorMetrics(doctorId).then((r) => setDoctorDash(unwrap(r))).catch(() => setDoctorDash(null))
        );
      } else setDoctorDash(null);

      if (receptionRole || adminRole) {
        tasks.push(
          dashboardApi.getReceptionMetrics(hospitalId).then((r) => setReception(unwrap(r))).catch(() => setReception(null))
        );
      } else setReception(null);

      if (apptAnalyticsRole) {
        tasks.push(
          dashboardApi
            .getAppointmentAnalytics(7, hospitalId)
            .then((r) => setApptTrends(unwrap(r) || []))
            .catch(() => setApptTrends([]))
        );
      } else setApptTrends([]);

      if (patientRole && patientId) {
        tasks.push(
          appointmentApi
            .getByPatient(patientId, { page: 0, size: 50, sort: 'startTime,asc' })
            .then((r) => {
              const page = unwrap(r);
              const list = page?.content ?? [];
              setTodayAppointments(list.filter((a) => a.appointmentDate === today).slice(0, 8));
            })
            .catch(() => setTodayAppointments([]))
        );
      } else if (doctorRole && doctorId) {
        tasks.push(
          appointmentApi
            .getDailySchedule(doctorId, today)
            .then((r) => {
              const schedule = unwrap(r);
              const items = (schedule?.schedule ?? [])
                .filter((s) => s.appointmentId || s.patientName)
                .slice(0, 8);
              setTodayAppointments(items);
            })
            .catch(() => setTodayAppointments([]))
        );
      } else if (searchRole) {
        tasks.push(
          appointmentApi
            .search({
              ...(hospitalId ? { hospitalId } : {}),
              fromDate: today,
              toDate: today,
              page: 0,
              size: 8,
              sort: 'startTime,asc',
            })
            .then((r) => setTodayAppointments(unwrap(r)?.content ?? []))
            .catch(() => setTodayAppointments([]))
        );
      } else {
        setTodayAppointments([]);
      }

      await Promise.all(tasks);
    } catch (err) {
      toast.error(err.response?.data?.message ?? 'Failed to load dashboard');
    } finally {
      setLoading(false);
    }
  }, [setUser]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    if (doctorDash?.todaySchedule?.length) {
      setTodayAppointments(doctorDash.todaySchedule.slice(0, 8));
    }
  }, [doctorDash]);

  const revenueData = useMemo(() => {
    const trends = admin?.revenueTrends;
    if (!Array.isArray(trends) || !trends.length) return [];
    return trends.map((t) => ({
      label: String(t.label || '').split(' ')[0] || t.label,
      value: Number(t.value ?? 0),
    }));
  }, [admin]);

  const revenueHighlight = useMemo(() => {
    if (!revenueData.length) return { index: null, value: null };
    let maxIdx = 0;
    revenueData.forEach((d, i) => {
      if (d.value >= revenueData[maxIdx].value) maxIdx = i;
    });
    return { index: maxIdx, value: formatMoney(revenueData[maxIdx].value) };
  }, [revenueData]);

  const sparkValues = useMemo(() => {
    const fromAppt = trendToSpark(apptTrends);
    if (fromAppt.length) return fromAppt;
    if (doctorDash?.weeklyAppointmentTrend?.length) {
      return trendToSpark(doctorDash.weeklyAppointmentTrend);
    }
    return [];
  }, [apptTrends, doctorDash]);

  const volumeChartData = useMemo(() => {
    const source = apptTrends.length ? apptTrends : doctorDash?.weeklyAppointmentTrend || [];
    return source.map((t) => ({
      label: String(t.label || '').split(' ')[0] || t.label,
      value: Number(t.count ?? t.value ?? 0),
    }));
  }, [apptTrends, doctorDash]);

  const bedStats = useMemo(() => {
    const total = Number(admin?.totalBeds ?? 0);
    const occupied = Number(admin?.occupiedBeds ?? reception?.occupiedBedsCount ?? 0);
    const available =
      reception?.availableBedsCount != null
        ? Number(reception.availableBedsCount)
        : Math.max(total - occupied, 0);
    const denom = total > 0 ? total : occupied + available;
    if (!denom) return null;
    const bookedPct = Math.round((occupied / denom) * 100);
    return {
      total: total || denom,
      occupied,
      available,
      bookedPct,
      availablePct: Math.max(0, 100 - bookedPct),
    };
  }, [admin, reception]);

  const gender = useMemo(
    () => ({
      female: Number(admin?.femalePatients ?? 0),
      male: Number(admin?.malePatients ?? 0),
      other: Number(admin?.otherPatients ?? 0),
    }),
    [admin]
  );

  const appointmentRows = todayAppointments.map(mapAppointmentRow);
  const firstName = user?.firstName ?? 'there';

  const showAdminStats = isAdmin || isReception;
  const showDoctorStats = isDoctor && !isAdmin;
  const showPatientStats = isPatient && !isAdmin && !isDoctor && !isReception;

  if (loading && !admin && !doctorDash && !reception && !appointmentRows.length) {
    return (
      <div style={{ display: 'grid', placeItems: 'center', minHeight: 320 }}>
        <Spinner size={28} />
      </div>
    );
  }

  return (
    <div className="animate-fade-in">
      <div className="page-header" style={{ marginBottom: '1.35rem', alignItems: 'flex-start' }}>
        <div>
          <h1
            style={{
              fontFamily: 'var(--font-display)',
              fontSize: 'clamp(1.6rem, 2.5vw, 2.15rem)',
              fontWeight: 800,
              letterSpacing: '-0.035em',
              marginBottom: '0.35rem',
            }}
          >
            Welcome back, {firstName}
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.92rem', maxWidth: 560 }}>
            Live operational view{user?.hospitalName ? ` for ${user.hospitalName}` : ''} — data from your hospital APIs.
          </p>
        </div>
        <button type="button" className="btn btn-secondary" onClick={load} disabled={loading}>
          <RefreshCw size={15} style={loading ? { animation: 'spin 0.8s linear infinite' } : undefined} />
          Refresh
        </button>
      </div>

      <div
        className="dash-grid"
        style={{
          display: 'grid',
          gridTemplateColumns: 'minmax(0, 1fr) 340px',
          gap: '1.25rem',
          alignItems: 'start',
        }}
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem', minWidth: 0 }}>
          <div
            className="dash-stats"
            style={{ display: 'grid', gridTemplateColumns: 'repeat(3, minmax(0, 1fr))', gap: '1rem' }}
          >
            {showAdminStats && (
              <StatCard
                title="Today's Appointments"
                value={formatCount(
                  admin?.todayAppointmentsCount ?? reception?.todayAppointmentsCount ?? appointmentRows.length
                )}
                subtitle={
                  reception
                    ? `${formatCount(reception.todayPendingCount)} pending · ${formatCount(reception.todayCheckedInCount)} checked in`
                    : undefined
                }
                sparkValues={sparkValues.length ? sparkValues : undefined}
                icon={CalendarDays}
                color="#0a4d4a"
              />
            )}
            {showDoctorStats && (
              <StatCard
                title="Today's Appointments"
                value={formatCount(doctorDash?.todayTotalAppointments ?? appointmentRows.length)}
                subtitle={
                  doctorDash
                    ? `${formatCount(doctorDash.todayPendingAppointments)} pending · ${formatCount(doctorDash.todayCompletedAppointments)} completed`
                    : undefined
                }
                sparkValues={sparkValues.length ? sparkValues : undefined}
                icon={CalendarDays}
                color="#0a4d4a"
              />
            )}
            {showPatientStats && (
              <StatCard
                title="My Appointments Today"
                value={formatCount(appointmentRows.length)}
                icon={CalendarDays}
                color="#0a4d4a"
              />
            )}

            {showAdminStats && (
              <StatCard
                title="Patients"
                value={formatCount(admin?.totalPatients ?? reception?.totalRegisteredPatients)}
                variant="dark"
                sparkValues={sparkValues.length ? sparkValues : undefined}
                sparkHighlight={sparkValues.length ? sparkValues.length - 1 : -1}
                icon={Users}
              />
            )}
            {showDoctorStats && (
              <StatCard
                title="My Patients"
                value={formatCount(doctorDash?.totalUniquePatients)}
                variant="dark"
                icon={Users}
              />
            )}
            {showPatientStats && (
              <StatCard title="Hospital" value={user?.hospitalName || '—'} variant="dark" />
            )}

            {showAdminStats && bedStats && (
              <StatCard
                title="Total Beds"
                value={formatCount(bedStats.total)}
                bedStats={{ bookedPct: bedStats.bookedPct, availablePct: bedStats.availablePct }}
                icon={BedDouble}
                color="#0a4d4a"
              />
            )}
            {showAdminStats && !bedStats && (
              <StatCard
                title="Active Doctors"
                value={formatCount(admin?.totalActiveDoctors)}
                icon={Stethoscope}
                color="#0a4d4a"
              />
            )}
            {showDoctorStats && (
              <StatCard
                title="Completed Today"
                value={formatCount(doctorDash?.todayCompletedAppointments)}
                subtitle="Consultations finished"
                icon={Activity}
                color="#0a4d4a"
              />
            )}
            {showPatientStats && (
              <StatCard
                title="Profile"
                value={user?.roles?.[0]?.replace(/_/g, ' ') || 'Patient'}
                icon={Users}
                color="#0a4d4a"
              />
            )}
          </div>

          {canSeeRevenue && revenueData.length > 0 && (
            <Card className="animate-rise">
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'flex-start',
                  marginBottom: '0.5rem',
                  gap: '1rem',
                  flexWrap: 'wrap',
                }}
              >
                <div>
                  <h3 style={{ fontFamily: 'var(--font-display)', fontSize: '1.15rem', fontWeight: 700 }}>
                    Total Revenue
                  </h3>
                  <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
                    Monthly collections · this month {formatMoney(admin?.monthlyRevenue)}
                  </p>
                </div>
                <span className="badge badge-primary">Live</span>
              </div>
              <DotMatrixChart
                data={revenueData}
                highlightIndex={revenueHighlight.index}
                highlightValue={revenueHighlight.value}
              />
            </Card>
          )}

          {(!canSeeRevenue || !revenueData.length) && volumeChartData.length > 0 && canSeeApptAnalytics && (
            <Card className="animate-rise">
              <div style={{ marginBottom: '0.75rem' }}>
                <h3 style={{ fontFamily: 'var(--font-display)', fontSize: '1.15rem', fontWeight: 700 }}>
                  Appointment Volume
                </h3>
                <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)', marginTop: '0.2rem' }}>
                  Last 7 days
                </p>
              </div>
              <DotMatrixChart data={volumeChartData} />
            </Card>
          )}
        </div>

        <div className="dash-rail" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <Card className="animate-rise">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <h3 style={{ fontFamily: 'var(--font-display)', fontSize: '1rem', fontWeight: 700 }}>
                Appointments Today
              </h3>
              <Link to="/appointments" style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--color-primary)' }}>
                See All
              </Link>
            </div>

            {appointmentRows.length === 0 ? (
              <div className="empty-state" style={{ padding: '1.5rem 0.5rem' }}>
                <CalendarDays size={28} />
                <p style={{ fontSize: '0.85rem' }}>No appointments scheduled for today.</p>
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem' }}>
                {appointmentRows.map((appt) => (
                  <div
                    key={appt.id}
                    style={{
                      display: 'grid',
                      gridTemplateColumns: '72px 1fr',
                      gap: '0.7rem',
                      alignItems: 'center',
                    }}
                  >
                    <span style={{ fontSize: '0.78rem', fontWeight: 700, color: 'var(--text-muted)' }}>
                      {appt.time}
                    </span>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', minWidth: 0 }}>
                      <div className="avatar avatar-sm" style={{ fontSize: '0.7rem' }}>
                        {String(appt.name)
                          .split(' ')
                          .map((p) => p[0])
                          .filter(Boolean)
                          .slice(0, 2)
                          .join('')
                          .toUpperCase() || '?'}
                      </div>
                      <div style={{ minWidth: 0 }}>
                        <div className="truncate" style={{ fontSize: '0.88rem', fontWeight: 700, color: 'var(--text-primary)' }}>
                          {appt.name}
                        </div>
                        <div className="truncate" style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                          {appt.note}
                          {appt.status ? ` · ${String(appt.status).replace(/_/g, ' ')}` : ''}
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </Card>

          {isAdmin && (
            <Card className="animate-rise">
              <h3 style={{ fontFamily: 'var(--font-display)', fontSize: '1rem', fontWeight: 700, marginBottom: '1rem' }}>
                Patients by Gender
              </h3>
              {gender.female + gender.male + gender.other === 0 ? (
                <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>No patient gender data yet.</p>
              ) : (
                <GenderDonut female={gender.female} male={gender.male} other={gender.other} />
              )}
            </Card>
          )}

          {(isAdmin || isReception) && (
            <div
              className="animate-rise"
              style={{
                borderRadius: 'var(--radius-2xl)',
                padding: '1.4rem',
                background: 'var(--gradient-hero-dark)',
                color: '#fff',
                boxShadow: 'var(--shadow-glow)',
              }}
            >
              <span className="badge badge-lime" style={{ marginBottom: '0.75rem', display: 'inline-flex' }}>
                Billing
              </span>
              <div style={{ fontSize: '0.8rem', opacity: 0.75, marginBottom: '0.35rem' }}>Outstanding balance</div>
              <div style={{ fontFamily: 'var(--font-display)', fontSize: '1.5rem', fontWeight: 800, marginBottom: '0.35rem' }}>
                {formatMoney(admin?.totalOutstandingBalance)}
              </div>
              <div style={{ fontSize: '0.8rem', opacity: 0.75, marginBottom: '1rem' }}>
                Open invoices: {formatCount(reception?.openInvoicesCount)}
              </div>
              <Link
                to="/billing"
                style={{
                  width: 42,
                  height: 42,
                  borderRadius: '50%',
                  background: 'var(--color-secondary)',
                  color: 'var(--color-primary)',
                  display: 'grid',
                  placeItems: 'center',
                }}
                aria-label="Open billing"
              >
                <ArrowUpRight size={18} strokeWidth={2.5} />
              </Link>
            </div>
          )}

          {isDoctor && doctorDash?.recentMedicalRecords?.length > 0 && (
            <Card>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.85rem' }}>
                <h3 style={{ fontFamily: 'var(--font-display)', fontSize: '1rem', fontWeight: 700 }}>
                  Recent Records
                </h3>
                <Link to="/medical-records" style={{ fontSize: '0.8rem', fontWeight: 600, color: 'var(--color-primary)' }}>
                  View
                </Link>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem' }}>
                {doctorDash.recentMedicalRecords.slice(0, 5).map((r) => (
                  <div key={r.id} style={{ display: 'flex', gap: '0.65rem', alignItems: 'flex-start' }}>
                    <Activity size={14} style={{ marginTop: 3, color: 'var(--color-primary)' }} />
                    <div style={{ minWidth: 0 }}>
                      <div className="truncate" style={{ fontSize: '0.85rem', fontWeight: 700 }}>
                        {r.patientName || r.diagnosis || 'Medical record'}
                      </div>
                      <div className="truncate" style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                        {r.diagnosis || r.createdAt || ''}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </Card>
          )}

          {isAdmin && (
            <Card style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
              <div
                style={{
                  width: 40,
                  height: 40,
                  borderRadius: 12,
                  background: 'rgba(10,77,74,0.08)',
                  display: 'grid',
                  placeItems: 'center',
                  color: 'var(--color-primary)',
                }}
              >
                <Stethoscope size={18} />
              </div>
              <div>
                <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', fontWeight: 600 }}>Active doctors</div>
                <div style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: '1.15rem' }}>
                  {formatCount(admin?.totalActiveDoctors)}
                </div>
              </div>
            </Card>
          )}

          {(isReception || isAdmin) && reception && (
            <Card style={{ display: 'flex', alignItems: 'center', gap: '0.85rem' }}>
              <div
                style={{
                  width: 40,
                  height: 40,
                  borderRadius: 12,
                  background: 'rgba(10,77,74,0.08)',
                  display: 'grid',
                  placeItems: 'center',
                  color: 'var(--color-primary)',
                }}
              >
                <CreditCard size={18} />
              </div>
              <div>
                <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', fontWeight: 600 }}>Open invoices</div>
                <div style={{ fontFamily: 'var(--font-display)', fontWeight: 800, fontSize: '1.15rem' }}>
                  {formatCount(reception.openInvoicesCount)}
                </div>
              </div>
            </Card>
          )}
        </div>
      </div>

      <style>{`
        @media (max-width: 1100px) {
          .dash-grid { grid-template-columns: 1fr !important; }
        }
        @media (max-width: 800px) {
          .dash-stats { grid-template-columns: 1fr !important; }
        }
      `}</style>
    </div>
  );
};

export default Dashboard;
