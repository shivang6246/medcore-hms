import React, { useState, useEffect, useCallback } from 'react';
import { Users, Plus, RefreshCw, Eye, ToggleLeft, ToggleRight, Search } from 'lucide-react';
import { patientApi } from '../../api/patient.api';
import toast from 'react-hot-toast';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import SearchInput from '../../components/ui/SearchInput';
import Pagination from '../../components/ui/Pagination';
import { StatusBadge } from '../../components/ui/Badge';
import Modal from '../../components/ui/Modal';
import useAuthStore from '../../store/authStore';

const PatientFormModal = ({ isOpen, onClose, onSuccess }) => {
  const { user } = useAuthStore();
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    firstName: '', lastName: '', dateOfBirth: '', gender: '', bloodGroup: '',
    phone: '', email: '',
    emergencyContactName: '', emergencyContactPhone: '', emergencyContactRelationship: '',
    hospitalId: user?.hospitalId ?? '',
    insuranceProvider: '', insurancePolicyNumber: '', allergies: '', medicalHistory: '',
  });

  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await patientApi.create(form);
      toast.success('Patient registered!');
      onSuccess();
      onClose();
    } catch (err) {
      toast.error(err.response?.data?.detail ?? err.response?.data?.message ?? 'Failed to register patient.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Register New Patient" maxWidth="700px">
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div className="form-grid">
          <div className="form-group">
            <label className="form-label">First Name *</label>
            <input required value={form.firstName} onChange={(e) => set('firstName', e.target.value)} placeholder="John" />
          </div>
          <div className="form-group">
            <label className="form-label">Last Name *</label>
            <input required value={form.lastName} onChange={(e) => set('lastName', e.target.value)} placeholder="Doe" />
          </div>
          <div className="form-group">
            <label className="form-label">Date of Birth *</label>
            <input required type="date" value={form.dateOfBirth} onChange={(e) => set('dateOfBirth', e.target.value)} />
          </div>
          <div className="form-group">
            <label className="form-label">Gender</label>
            <select value={form.gender} onChange={(e) => set('gender', e.target.value)}>
              <option value="">Select</option>
              <option value="MALE">Male</option>
              <option value="FEMALE">Female</option>
              <option value="OTHER">Other</option>
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Blood Group</label>
            <select value={form.bloodGroup} onChange={(e) => set('bloodGroup', e.target.value)}>
              <option value="">Select</option>
              {['A_POSITIVE','A_NEGATIVE','B_POSITIVE','B_NEGATIVE','AB_POSITIVE','AB_NEGATIVE','O_POSITIVE','O_NEGATIVE'].map(bg => (
                <option key={bg} value={bg}>{bg.replace('_', ' ')}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Phone *</label>
            <input required value={form.phone} onChange={(e) => set('phone', e.target.value)} placeholder="+91 98765 43210" />
          </div>
          <div className="form-group">
            <label className="form-label">Email</label>
            <input type="email" value={form.email} onChange={(e) => set('email', e.target.value)} placeholder="patient@email.com" />
          </div>
          <div className="form-group">
            <label className="form-label">Hospital ID *</label>
            <input required value={form.hospitalId} onChange={(e) => set('hospitalId', e.target.value)} placeholder="UUID" />
          </div>
        </div>

        <div style={{ border: '1px solid var(--color-border)', borderRadius: 'var(--radius-md)', padding: '1rem' }}>
          <p style={{ fontWeight: 600, marginBottom: '0.75rem', fontSize: 'var(--font-size-sm)', color: 'var(--text-secondary)' }}>Emergency Contact</p>
          <div className="form-grid">
            <div className="form-group">
              <label className="form-label">Name *</label>
              <input required value={form.emergencyContactName} onChange={(e) => set('emergencyContactName', e.target.value)} placeholder="Jane Doe" />
            </div>
            <div className="form-group">
              <label className="form-label">Phone *</label>
              <input required value={form.emergencyContactPhone} onChange={(e) => set('emergencyContactPhone', e.target.value)} placeholder="+91 98765 43210" />
            </div>
            <div className="form-group">
              <label className="form-label">Relationship</label>
              <input value={form.emergencyContactRelationship} onChange={(e) => set('emergencyContactRelationship', e.target.value)} placeholder="Spouse" />
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end' }}>
          <Button type="button" variant="secondary" onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="primary" loading={loading}>Register Patient</Button>
        </div>
      </form>
    </Modal>
  );
};

const PatientList = () => {
  const { user, hasAnyRole } = useAuthStore();
  const isAdmin = hasAnyRole(['SUPER_ADMIN', 'HOSPITAL_ADMIN']);

  const [data, setData] = useState(null);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);

  const fetchPatients = useCallback(async () => {
    if (!user?.hospitalId) return;
    setLoading(true);
    try {
      if (search.trim()) {
        const res = await patientApi.search({ hospitalId: user.hospitalId, name: search, page, size: 10 });
        setData(res.data.data);
      } else {
        const res = await patientApi.getAll({ hospitalId: user.hospitalId, page, size: 10 });
        setData(res.data.data);
      }
    } catch {
      toast.error('Failed to fetch patients');
    } finally {
      setLoading(false);
    }
  }, [page, search, user?.hospitalId]);

  useEffect(() => { fetchPatients(); }, [fetchPatients]);
  useEffect(() => { setPage(0); }, [search]);

  const handleToggle = async (patient) => {
    try {
      if (patient.isActive) {
        await patientApi.deactivate(patient.id);
        toast.success('Patient deactivated');
      } else {
        await patientApi.activate(patient.id);
        toast.success('Patient activated');
      }
      fetchPatients();
    } catch { toast.error('Action failed'); }
  };

  const calcAge = (dob) => {
    if (!dob) return '—';
    const diff = Date.now() - new Date(dob).getTime();
    return Math.floor(diff / (1000 * 60 * 60 * 24 * 365.25)) + ' yrs';
  };

  return (
    <div className="animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Patients</h1>
          <p className="page-subtitle">Manage patient records for {user?.hospitalName ?? 'your hospital'}</p>
        </div>
        {isAdmin && (
          <Button id="register-patient-btn" variant="primary" icon={Plus} onClick={() => setShowModal(true)}>
            Register Patient
          </Button>
        )}
      </div>

      <div className="filters-bar">
        <SearchInput id="patient-search" value={search} onChange={setSearch} placeholder="Search by name, phone…" />
        <Button variant="secondary" icon={RefreshCw} onClick={fetchPatients} size="sm" />
      </div>

      <Card padding={false}>
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Patient</th>
                <th>Patient ID</th>
                <th className="hide-md">Age</th>
                <th className="hide-md">Blood Group</th>
                <th>Phone</th>
                <th>Status</th>
                <th>Actions</th>
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
                data.content.map((p) => (
                  <tr key={p.id}>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
                        <div className="avatar avatar-sm" style={{ background: 'linear-gradient(135deg, #06b6d4, #3b82f6)' }}>
                          {`${p.firstName?.[0] ?? ''}${p.lastName?.[0] ?? ''}`}
                        </div>
                        <div>
                          <p style={{ fontWeight: 600, color: 'var(--text-primary)', fontSize: 'var(--font-size-sm)' }}>
                            {p.firstName} {p.lastName}
                          </p>
                          <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                            {p.gender ?? 'Unknown'}
                          </p>
                        </div>
                      </div>
                    </td>
                    <td><code style={{ fontSize: 'var(--font-size-xs)', color: 'var(--color-primary-light)' }}>{p.patientId}</code></td>
                    <td className="hide-md">{calcAge(p.dateOfBirth)}</td>
                    <td className="hide-md">
                      {p.bloodGroup ? (
                        <span className="badge badge-info">{p.bloodGroup.replace('_', ' ')}</span>
                      ) : '—'}
                    </td>
                    <td>{p.phone}</td>
                    <td><StatusBadge isActive={p.isActive} /></td>
                    <td>
                      <div style={{ display: 'flex', gap: '0.375rem' }}>
                        {isAdmin && (
                          <Button
                            size="sm"
                            variant={p.isActive ? 'danger' : 'success'}
                            icon={p.isActive ? ToggleLeft : ToggleRight}
                            onClick={() => handleToggle(p)}
                          />
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={7}>
                    <div className="empty-state">
                      <Users size={40} />
                      <h3>No patients found</h3>
                      {!user?.hospitalId && <p>Your account is not associated with a hospital.</p>}
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

      <PatientFormModal isOpen={showModal} onClose={() => setShowModal(false)} onSuccess={fetchPatients} />
    </div>
  );
};

export default PatientList;
