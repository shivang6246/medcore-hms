import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Building2, Mail, Phone, Globe, MapPin, Hash, FileText } from 'lucide-react';
import { hospitalApi } from '../../api/hospital.api';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import { StatusBadge } from '../../components/ui/Badge';
import toast from 'react-hot-toast';

const DetailRow = ({ icon: Icon, label, value }) => (
  <div style={{ display: 'flex', gap: '0.875rem', padding: '0.875rem 0', borderBottom: '1px solid var(--color-border)' }}>
    <div style={{ width: 32, height: 32, borderRadius: 'var(--radius-sm)', background: 'rgba(59,130,246,0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--color-primary)', flexShrink: 0, marginTop: 2 }}>
      <Icon size={14} />
    </div>
    <div>
      <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.06em', fontWeight: 600, marginBottom: '0.15rem' }}>{label}</p>
      <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-primary)', fontWeight: 500 }}>{value || '—'}</p>
    </div>
  </div>
);

const HospitalDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [hospital, setHospital] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      setLoading(true);
      try {
        const res = await hospitalApi.getById(id);
        setHospital(res.data.data);
      } catch (err) {
        toast.error('Hospital not found');
        navigate('/hospitals');
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, [id]);

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
        <div style={{ width: 40, height: 40, border: '3px solid var(--color-border)', borderTopColor: 'var(--color-primary)', borderRadius: '50%', animation: 'spin 0.7s linear infinite' }} />
      </div>
    );
  }

  if (!hospital) return null;

  const addr = hospital.address;
  const fullAddress = addr
    ? [addr.street, addr.city, addr.state, addr.postalCode, addr.country].filter(Boolean).join(', ')
    : '—';

  return (
    <div className="animate-fade-in">
      <div className="page-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
          <Button variant="secondary" icon={ArrowLeft} size="sm" onClick={() => navigate('/hospitals')}>
            Back
          </Button>
          <div>
            <h1 className="page-title">{hospital.name}</h1>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginTop: '0.25rem' }}>
              <StatusBadge isActive={hospital.isActive} />
              <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                Created {new Date(hospital.createdAt).toLocaleDateString()}
              </span>
            </div>
          </div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem' }}>
        {/* Basic Info */}
        <Card>
          <h3 style={{ fontWeight: 700, marginBottom: '0.5rem', fontSize: 'var(--font-size-lg)' }}>Basic Information</h3>
          <DetailRow icon={Building2} label="Hospital Name" value={hospital.name} />
          <DetailRow icon={Hash} label="Registration Number" value={hospital.registrationNumber} />
          <DetailRow icon={FileText} label="License Number" value={hospital.licenseNumber} />
          <DetailRow icon={Mail} label="Email" value={hospital.email} />
          <DetailRow icon={Phone} label="Phone" value={hospital.phone} />
          <DetailRow icon={Globe} label="Website" value={hospital.website} />
        </Card>

        {/* Address & Meta */}
        <Card>
          <h3 style={{ fontWeight: 700, marginBottom: '0.5rem', fontSize: 'var(--font-size-lg)' }}>Address & Metadata</h3>
          <DetailRow icon={MapPin} label="Full Address" value={fullAddress} />
          <DetailRow icon={MapPin} label="City" value={addr?.city} />
          <DetailRow icon={MapPin} label="State" value={addr?.state} />
          <DetailRow icon={MapPin} label="Country" value={addr?.country} />
          <DetailRow icon={MapPin} label="Postal Code" value={addr?.postalCode} />

          <div style={{ marginTop: '1rem', padding: '0.875rem', background: 'rgba(59,130,246,0.05)', borderRadius: 'var(--radius-md)', border: '1px solid var(--color-border)' }}>
            <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)', marginBottom: '0.25rem' }}>Hospital ID</p>
            <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-primary)', fontFamily: 'monospace', wordBreak: 'break-all' }}>{hospital.id}</p>
          </div>
        </Card>
      </div>
    </div>
  );
};

export default HospitalDetail;
