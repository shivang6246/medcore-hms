import React, { useEffect, useState } from 'react';
import {
  Building2, Users, Stethoscope, CalendarDays,
  Activity, Clock, TrendingUp, ShieldCheck,
} from 'lucide-react';
import StatCard from '../components/ui/StatCard';
import Card from '../components/ui/Card';
import useAuthStore from '../store/authStore';
import { hospitalApi } from '../api/hospital.api';
import { doctorApi } from '../api/doctor.api';
import { patientApi } from '../api/patient.api';
import { appointmentApi } from '../api/appointment.api';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

const CHART_DATA = [
  { name: 'Mon', appointments: 24, patients: 18 },
  { name: 'Tue', appointments: 31, patients: 22 },
  { name: 'Wed', appointments: 28, patients: 25 },
  { name: 'Thu', appointments: 40, patients: 30 },
  { name: 'Fri', appointments: 35, patients: 27 },
  { name: 'Sat', appointments: 22, patients: 15 },
  { name: 'Sun', appointments: 18, patients: 12 },
];

const CustomTooltip = ({ active, payload, label }) => {
  if (!active || !payload?.length) return null;
  return (
    <div style={{ background: 'var(--color-bg-elevated)', border: '1px solid var(--color-border)', borderRadius: 'var(--radius-md)', padding: '0.625rem 0.875rem' }}>
      <p style={{ fontWeight: 600, color: 'var(--text-primary)', marginBottom: '0.25rem' }}>{label}</p>
      {payload.map((p) => (
        <p key={p.name} style={{ color: p.color, fontSize: 'var(--font-size-sm)' }}>
          {p.name}: {p.value}
        </p>
      ))}
    </div>
  );
};

const Dashboard = () => {
  const { user, hasAnyRole } = useAuthStore();
  const [stats, setStats] = useState({ hospitals: null, doctors: null, patients: null, appointments: null });
  const [loading, setLoading] = useState(true);

  const isSuperAdmin = hasAnyRole(['SUPER_ADMIN']);
  const isAdmin = hasAnyRole(['SUPER_ADMIN', 'HOSPITAL_ADMIN']);

  useEffect(() => {
    const fetchStats = async () => {
      setLoading(true);
      const results = {};

      if (isSuperAdmin) {
        try {
          const r = await hospitalApi.getAll({ page: 0, size: 1 });
          results.hospitals = r.data.data?.totalElements ?? r.data.data?.content?.length ?? '—';
        } catch { results.hospitals = '—'; }
      }

      if (isAdmin) {
        try {
          const r = await doctorApi.getAll({ page: 0, size: 1 });
          results.doctors = r.data.data?.totalElements ?? '—';
        } catch { results.doctors = '—'; }
      }

      if (hasAnyRole(['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE'])) {
        if (user?.hospitalId) {
          try {
            const r = await patientApi.getAll({ hospitalId: user.hospitalId, page: 0, size: 1 });
            results.patients = r.data.data?.totalElements ?? '—';
          } catch { results.patients = '—'; }
        } else {
          results.patients = '—';
        }
      }

      setStats(results);
      setLoading(false);
    };

    fetchStats();
  }, []);

  const greeting = () => {
    const h = new Date().getHours();
    if (h < 12) return 'Good Morning';
    if (h < 18) return 'Good Afternoon';
    return 'Good Evening';
  };

  const STAT_CARDS = [
    isSuperAdmin && {
      title: 'Total Hospitals',
      value: loading ? '...' : stats.hospitals,
      icon: Building2,
      color: '#3b82f6',
      subtitle: 'Registered tenants',
    },
    isAdmin && {
      title: 'Total Doctors',
      value: loading ? '...' : stats.doctors,
      icon: Stethoscope,
      color: '#8b5cf6',
      subtitle: 'Active practitioners',
    },
    hasAnyRole(['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE']) && {
      title: 'Total Patients',
      value: loading ? '...' : stats.patients,
      icon: Users,
      color: '#06b6d4',
      subtitle: 'Registered this hospital',
    },
    {
      title: "Today's Appointments",
      value: '—',
      icon: CalendarDays,
      color: '#10b981',
      subtitle: 'Scheduled for today',
    },
  ].filter(Boolean);

  return (
    <div className="animate-fade-in">
      {/* Greeting */}
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: 'var(--font-size-3xl)', fontWeight: 800, marginBottom: '0.25rem' }}>
          {greeting()},{' '}
          <span className="gradient-text">{user?.firstName ?? 'Doctor'}</span> 👋
        </h1>
        <p style={{ color: 'var(--text-muted)' }}>
          Here's what's happening in MedCore HMS today
        </p>
      </div>

      {/* Stat Cards */}
      <div className="stats-grid">
        {STAT_CARDS.map((s) => (
          <StatCard key={s.title} {...s} />
        ))}
      </div>

      {/* Charts row */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '1.5rem', marginBottom: '1.5rem' }}>
        {/* Area Chart */}
        <Card>
          <div style={{ marginBottom: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <h3 style={{ fontSize: 'var(--font-size-lg)', fontWeight: 700 }}>Weekly Overview</h3>
              <p style={{ color: 'var(--text-muted)', fontSize: 'var(--font-size-sm)' }}>Appointments &amp; New Patients</p>
            </div>
            <div style={{ display: 'flex', gap: '1rem', fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.375rem' }}>
                <span style={{ width: 10, height: 10, borderRadius: '50%', background: '#3b82f6', display: 'inline-block' }} />
                Appointments
              </span>
              <span style={{ display: 'flex', alignItems: 'center', gap: '0.375rem' }}>
                <span style={{ width: 10, height: 10, borderRadius: '50%', background: '#8b5cf6', display: 'inline-block' }} />
                Patients
              </span>
            </div>
          </div>
          <ResponsiveContainer width="100%" height={220}>
            <AreaChart data={CHART_DATA} margin={{ top: 5, right: 5, bottom: 0, left: -20 }}>
              <defs>
                <linearGradient id="colorAppts" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.3} />
                  <stop offset="95%" stopColor="#3b82f6" stopOpacity={0} />
                </linearGradient>
                <linearGradient id="colorPts" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#8b5cf6" stopOpacity={0.3} />
                  <stop offset="95%" stopColor="#8b5cf6" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.04)" />
              <XAxis dataKey="name" tick={{ fill: 'var(--text-muted)', fontSize: 12 }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fill: 'var(--text-muted)', fontSize: 12 }} axisLine={false} tickLine={false} />
              <Tooltip content={<CustomTooltip />} />
              <Area type="monotone" dataKey="appointments" name="Appointments" stroke="#3b82f6" strokeWidth={2} fill="url(#colorAppts)" />
              <Area type="monotone" dataKey="patients" name="Patients" stroke="#8b5cf6" strokeWidth={2} fill="url(#colorPts)" />
            </AreaChart>
          </ResponsiveContainer>
        </Card>

        {/* Quick info card */}
        <Card>
          <h3 style={{ fontSize: 'var(--font-size-lg)', fontWeight: 700, marginBottom: '1.25rem' }}>Quick Info</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {[
              { icon: ShieldCheck, label: 'Your Role', value: user?.roles?.[0]?.replace(/_/g, ' ') ?? '—', color: '#3b82f6' },
              { icon: Building2, label: 'Hospital', value: user?.hospitalName ?? (isSuperAdmin ? 'All Hospitals' : '—'), color: '#8b5cf6' },
              { icon: Activity, label: 'Status', value: 'Active', color: '#10b981' },
              { icon: Clock, label: 'Session', value: '15 min JWT', color: '#f59e0b' },
            ].map((item) => (
              <div key={item.label} style={{ display: 'flex', alignItems: 'center', gap: '0.875rem', padding: '0.625rem', borderRadius: 'var(--radius-md)', background: 'rgba(255,255,255,0.025)' }}>
                <div style={{ width: 36, height: 36, borderRadius: 'var(--radius-sm)', background: `${item.color}18`, display: 'flex', alignItems: 'center', justifyContent: 'center', color: item.color, flexShrink: 0 }}>
                  <item.icon size={16} />
                </div>
                <div>
                  <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)', marginBottom: '0.1rem' }}>{item.label}</p>
                  <p style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600, color: 'var(--text-primary)' }}>{item.value}</p>
                </div>
              </div>
            ))}
          </div>
        </Card>
      </div>

      {/* Recent activity placeholder */}
      <Card>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
          <h3 style={{ fontSize: 'var(--font-size-lg)', fontWeight: 700 }}>Recent Activity</h3>
          <span className="badge badge-info">Live</span>
        </div>
        <div className="empty-state" style={{ padding: '2rem' }}>
          <TrendingUp size={40} />
          <h3>Activity feed coming soon</h3>
          <p style={{ fontSize: 'var(--font-size-sm)' }}>Real-time activity will appear here as your team uses the system.</p>
        </div>
      </Card>
    </div>
  );
};

export default Dashboard;
