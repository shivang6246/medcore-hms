import React, { useState, useEffect, useCallback } from 'react';
import { Stethoscope, Plus, Eye, ToggleLeft, ToggleRight, RefreshCw, DollarSign } from 'lucide-react';
import { doctorApi } from '../../api/doctor.api';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import SearchInput from '../../components/ui/SearchInput';
import Pagination from '../../components/ui/Pagination';
import { StatusBadge } from '../../components/ui/Badge';
import Modal from '../../components/ui/Modal';
import useAuthStore from '../../store/authStore';

const DoctorFormModal = ({ isOpen, onClose, onSuccess }) => {
  const [form, setForm] = useState({
    firstName: '', lastName: '', email: '', phone: '', specialization: '',
    qualification: '', licenseNumber: '', employeeId: '',
    hospitalId: '', departmentId: '', consultationFee: '',
    gender: '', experienceYears: '',
  });
  const [loading, setLoading] = useState(false);
  const { user } = useAuthStore();

  useEffect(() => {
    if (user?.hospitalId) setForm((f) => ({ ...f, hospitalId: user.hospitalId }));
  }, [user]);

  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const payload = {
        ...form,
        consultationFee: form.consultationFee ? parseFloat(form.consultationFee) : undefined,
        experienceYears: form.experienceYears ? parseInt(form.experienceYears) : undefined,
        departmentId: form.departmentId || undefined,
        hospitalId: form.hospitalId || undefined,
      };
      await doctorApi.create(payload);
      toast.success('Doctor onboarded!');
      onSuccess();
      onClose();
    } catch (err) {
      toast.error(err.response?.data?.detail ?? err.response?.data?.message ?? 'Failed to onboard doctor.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Onboard New Doctor" maxWidth="680px">
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div className="form-grid">
          <div className="form-group">
            <label className="form-label">First Name *</label>
            <input required value={form.firstName} onChange={(e) => set('firstName', e.target.value)} placeholder="Jane" />
          </div>
          <div className="form-group">
            <label className="form-label">Last Name *</label>
            <input required value={form.lastName} onChange={(e) => set('lastName', e.target.value)} placeholder="Smith" />
          </div>
          <div className="form-group">
            <label className="form-label">Email *</label>
            <input required type="email" value={form.email} onChange={(e) => set('email', e.target.value)} placeholder="doctor@hospital.com" />
          </div>
          <div className="form-group">
            <label className="form-label">Phone</label>
            <input value={form.phone} onChange={(e) => set('phone', e.target.value)} placeholder="+91 98765 43210" />
          </div>
          <div className="form-group">
            <label className="form-label">Specialization *</label>
            <input required value={form.specialization} onChange={(e) => set('specialization', e.target.value)} placeholder="Cardiology" />
          </div>
          <div className="form-group">
            <label className="form-label">Qualification *</label>
            <input required value={form.qualification} onChange={(e) => set('qualification', e.target.value)} placeholder="MBBS, MD" />
          </div>
          <div className="form-group">
            <label className="form-label">License Number *</label>
            <input required value={form.licenseNumber} onChange={(e) => set('licenseNumber', e.target.value)} placeholder="LIC-DOC-001" />
          </div>
          <div className="form-group">
            <label className="form-label">Employee ID *</label>
            <input required value={form.employeeId} onChange={(e) => set('employeeId', e.target.value)} placeholder="EMP-001" />
          </div>
          <div className="form-group">
            <label className="form-label">Hospital ID *</label>
            <input required value={form.hospitalId} onChange={(e) => set('hospitalId', e.target.value)} placeholder="UUID" />
          </div>
          <div className="form-group">
            <label className="form-label">Department ID</label>
            <input value={form.departmentId} onChange={(e) => set('departmentId', e.target.value)} placeholder="UUID (optional)" />
          </div>
          <div className="form-group">
            <label className="form-label">Consultation Fee (₹)</label>
            <input type="number" min="0" value={form.consultationFee} onChange={(e) => set('consultationFee', e.target.value)} placeholder="500" />
          </div>
          <div className="form-group">
            <label className="form-label">Experience (years)</label>
            <input type="number" min="0" value={form.experienceYears} onChange={(e) => set('experienceYears', e.target.value)} placeholder="5" />
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
        </div>
        <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end' }}>
          <Button type="button" variant="secondary" onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="primary" loading={loading}>Onboard Doctor</Button>
        </div>
      </form>
    </Modal>
  );
};

const DoctorList = () => {
  const navigate = useNavigate();
  const { hasAnyRole } = useAuthStore();
  const isAdmin = hasAnyRole(['SUPER_ADMIN', 'HOSPITAL_ADMIN']);

  const [data, setData] = useState(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);

  const fetchDoctors = useCallback(async () => {
    setLoading(true);
    try {
      const res = await doctorApi.getAll({ page, size: 10, sort: 'createdAt,desc' });
      setData(res.data.data);
    } catch {
      toast.error('Failed to fetch doctors');
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => { fetchDoctors(); }, [fetchDoctors]);

  const handleToggle = async (doctor) => {
    try {
      if (doctor.isActive) {
        await doctorApi.deactivate(doctor.id);
        toast.success('Doctor deactivated');
      } else {
        await doctorApi.activate(doctor.id);
        toast.success('Doctor activated');
      }
      fetchDoctors();
    } catch { toast.error('Action failed'); }
  };

  return (
    <div className="animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Doctors</h1>
          <p className="page-subtitle">Manage doctor profiles and availability</p>
        </div>
        {isAdmin && (
          <Button id="create-doctor-btn" variant="primary" icon={Plus} onClick={() => setShowModal(true)}>
            Onboard Doctor
          </Button>
        )}
      </div>

      <div className="filters-bar">
        <Button variant="secondary" icon={RefreshCw} onClick={fetchDoctors} size="sm" />
      </div>

      <Card padding={false}>
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Doctor</th>
                <th>Specialization</th>
                <th className="hide-md">License</th>
                <th className="hide-md">Fee</th>
                <th>Available</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                Array.from({ length: 5 }).map((_, i) => (
                  <tr key={i}>
                    {Array.from({ length: 7 }).map((_, j) => (
                      <td key={j}><div className="skeleton" style={{ height: 18, width: '80%' }} /></td>
                    ))}
                  </tr>
                ))
              ) : data?.content?.length ? (
                data.content.map((d) => (
                  <tr key={d.id}>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
                        <div className="avatar avatar-sm" style={{ background: 'linear-gradient(135deg, #8b5cf6, #3b82f6)' }}>
                          {`${d.firstName?.[0] ?? ''}${d.lastName?.[0] ?? ''}`}
                        </div>
                        <div>
                          <p style={{ fontWeight: 600, color: 'var(--text-primary)', fontSize: 'var(--font-size-sm)' }}>
                            Dr. {d.firstName} {d.lastName}
                          </p>
                          <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>{d.employeeId}</p>
                        </div>
                      </div>
                    </td>
                    <td>{d.specialization}</td>
                    <td className="hide-md">{d.licenseNumber}</td>
                    <td className="hide-md">
                      {d.consultationFee != null ? (
                        <span style={{ display: 'flex', alignItems: 'center', gap: '0.2rem', color: 'var(--color-success)' }}>
                          <DollarSign size={12} />₹{d.consultationFee}
                        </span>
                      ) : '—'}
                    </td>
                    <td>
                      <span className={`badge ${d.available ? 'badge-success' : 'badge-neutral'}`}>
                        {d.available ? 'Available' : 'Unavailable'}
                      </span>
                    </td>
                    <td><StatusBadge isActive={d.isActive} /></td>
                    <td>
                      <div style={{ display: 'flex', gap: '0.375rem' }}>
                        <Button size="sm" variant="secondary" icon={Eye} onClick={() => navigate(`/doctors/${d.id}`)} />
                        {isAdmin && (
                          <Button
                            size="sm"
                            variant={d.isActive ? 'danger' : 'success'}
                            icon={d.isActive ? ToggleLeft : ToggleRight}
                            onClick={() => handleToggle(d)}
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
                      <Stethoscope size={40} />
                      <h3>No doctors found</h3>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        {data && (
          <Pagination
            page={page}
            totalPages={data.totalPages}
            totalElements={data.totalElements}
            size={10}
            onPageChange={setPage}
          />
        )}
      </Card>

      <DoctorFormModal isOpen={showModal} onClose={() => setShowModal(false)} onSuccess={fetchDoctors} />
    </div>
  );
};

export default DoctorList;
