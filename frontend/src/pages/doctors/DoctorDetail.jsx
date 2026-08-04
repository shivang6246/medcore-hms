import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Stethoscope, Mail, Phone, Award, DollarSign, Clock, Building2 } from 'lucide-react';
import { doctorApi } from '../../api/doctor.api';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import { StatusBadge } from '../../components/ui/Badge';
import toast from 'react-hot-toast';

const DoctorDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [doctor, setDoctor] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      setLoading(true);
      try {
        const res = await doctorApi.getById(id);
        setDoctor(res.data.data);
      } catch {
        toast.error('Doctor not found');
        navigate('/doctors');
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, [id]);

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
        {/* Profile card */}
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
                { icon: DollarSign, val: doctor.consultationFee != null ? `₹${doctor.consultationFee}` : null, label: 'Consultation Fee' },
                { icon: Clock, val: doctor.experienceYears != null ? `${doctor.experienceYears} years exp.` : null },
              ].map(({ icon: Icon, val, label }, i) => val && (
                <div key={i} style={{ display: 'flex', alignItems: 'center', gap: '0.625rem', padding: '0.5rem 0.625rem', background: 'rgba(255,255,255,0.025)', borderRadius: 'var(--radius-sm)' }}>
                  <Icon size={14} color="var(--color-primary)" />
                  <span style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-secondary)' }}>{val}</span>
                </div>
              ))}
            </div>
          </div>
        </Card>

        {/* Details */}
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
    </div>
  );
};

export default DoctorDetail;
