import React, { useEffect, useState } from 'react';
import { User, Mail, Phone, Building2, ShieldCheck, Calendar, Edit3, Save, X } from 'lucide-react';
import useAuthStore from '../store/authStore';
import { authApi } from '../api/auth.api';
import Card from '../components/ui/Card';
import Button from '../components/ui/Button';
import { StatusBadge } from '../components/ui/Badge';
import toast from 'react-hot-toast';

const InfoRow = ({ icon: Icon, label, value }) => (
  <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.875rem', padding: '0.875rem 0', borderBottom: '1px solid var(--color-border)' }}>
    <div style={{ width: 36, height: 36, borderRadius: 'var(--radius-sm)', background: 'rgba(59,130,246,0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-primary)', flexShrink: 0, marginTop: 2 }}>
      <Icon size={16} />
    </div>
    <div>
      <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)', marginBottom: '0.2rem', textTransform: 'uppercase', letterSpacing: '0.06em', fontWeight: 600 }}>{label}</p>
      <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-primary)', fontWeight: 500 }}>{value || '—'}</p>
    </div>
  </div>
);

const Profile = () => {
  const { user, setUser, accessToken, refreshToken } = useAuthStore();
  const [loading, setLoading] = useState(false);
  const [profileData, setProfileData] = useState(null);

  useEffect(() => {
    const fetchMe = async () => {
      setLoading(true);
      try {
        const res = await authApi.me();
        setProfileData(res.data);
        setUser(res.data);
      } catch (err) {
        toast.error('Failed to load profile');
      } finally {
        setLoading(false);
      }
    };
    fetchMe();
  }, []);

  const data = profileData ?? user;
  const initials = data ? `${data.firstName?.[0] ?? ''}${data.lastName?.[0] ?? ''}`.toUpperCase() : '?';

  return (
    <div className="animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">My Profile</h1>
          <p className="page-subtitle">View and manage your account information</p>
        </div>
      </div>

      {loading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: '4rem' }}>
          <div style={{ width: 40, height: 40, border: '3px solid var(--color-border)', borderTopColor: 'var(--color-primary)', borderRadius: '50%', animation: 'spin 0.7s linear infinite' }} />
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '1.5rem' }}>
          {/* Avatar card */}
          <Card>
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem' }}>
              <div
                style={{
                  width: 96,
                  height: 96,
                  borderRadius: '50%',
                  background: 'var(--gradient-primary)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '2rem',
                  fontWeight: 800,
                  color: '#fff',
                  boxShadow: 'var(--shadow-glow-strong)',
                  border: '3px solid var(--color-border)',
                }}
              >
                {initials}
              </div>
              <div style={{ textAlign: 'center' }}>
                <h3 style={{ fontSize: 'var(--font-size-lg)', fontWeight: 700 }}>
                  {data?.firstName} {data?.lastName}
                </h3>
                <p style={{ color: 'var(--text-muted)', fontSize: 'var(--font-size-sm)', marginTop: '0.25rem' }}>
                  {data?.email}
                </p>
                {data?.isActive !== undefined && (
                  <div style={{ marginTop: '0.75rem' }}>
                    <StatusBadge isActive={data.isActive} />
                  </div>
                )}
              </div>

              {/* Roles */}
              {data?.roles && (
                <div style={{ width: '100%' }}>
                  <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.06em', fontWeight: 600, marginBottom: '0.5rem' }}>
                    Roles
                  </p>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.375rem' }}>
                    {data.roles.map((role) => (
                      <span key={role} className="badge badge-primary">
                        {role.replace(/_/g, ' ')}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </Card>

          {/* Info card */}
          <Card>
            <h3 style={{ fontSize: 'var(--font-size-lg)', fontWeight: 700, marginBottom: '0.5rem' }}>
              Account Information
            </h3>
            <div>
              <InfoRow icon={User} label="Full Name" value={`${data?.firstName ?? ''} ${data?.lastName ?? ''}`} />
              <InfoRow icon={Mail} label="Email Address" value={data?.email} />
              <InfoRow icon={Phone} label="Phone Number" value={data?.phone} />
              <InfoRow icon={Building2} label="Hospital" value={data?.hospitalName ?? (data?.hospitalId ? data.hospitalId : 'Not assigned')} />
              <InfoRow icon={ShieldCheck} label="Account ID" value={data?.id} />
            </div>
          </Card>
        </div>
      )}
    </div>
  );
};

export default Profile;
