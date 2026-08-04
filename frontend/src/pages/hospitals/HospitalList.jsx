import React, { useState, useEffect, useCallback } from 'react';
import { Building2, Plus, Eye, ToggleLeft, ToggleRight, RefreshCw, MapPin, Mail } from 'lucide-react';
import { hospitalApi } from '../../api/hospital.api';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import SearchInput from '../../components/ui/SearchInput';
import Pagination from '../../components/ui/Pagination';
import { StatusBadge } from '../../components/ui/Badge';
import Modal from '../../components/ui/Modal';
import useAuthStore from '../../store/authStore';

const HospitalFormModal = ({ isOpen, onClose, onSuccess, initial }) => {
  const [form, setForm] = useState(initial ?? {
    name: '', registrationNumber: '', licenseNumber: '', email: '',
    phone: '', website: '',
    address: { street: '', city: '', state: '', country: '', postalCode: '' },
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => { if (initial) setForm(initial); }, [initial]);

  const set = (key, value) => setForm((f) => ({ ...f, [key]: value }));
  const setAddr = (key, value) => setForm((f) => ({ ...f, address: { ...f.address, [key]: value } }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      if (initial?.id) {
        await hospitalApi.update(initial.id, form);
        toast.success('Hospital updated!');
      } else {
        await hospitalApi.create(form);
        toast.success('Hospital created!');
      }
      onSuccess();
      onClose();
    } catch (err) {
      toast.error(err.response?.data?.detail ?? err.response?.data?.message ?? 'Failed to save hospital.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={initial?.id ? 'Edit Hospital' : 'New Hospital'} maxWidth="640px">
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div className="form-grid">
          <div className="form-group">
            <label className="form-label">Hospital Name *</label>
            <input required value={form.name} onChange={(e) => set('name', e.target.value)} placeholder="City General Hospital" />
          </div>
          <div className="form-group">
            <label className="form-label">Registration Number *</label>
            <input required value={form.registrationNumber} onChange={(e) => set('registrationNumber', e.target.value)} placeholder="REG-2024-001" />
          </div>
          <div className="form-group">
            <label className="form-label">License Number *</label>
            <input required value={form.licenseNumber} onChange={(e) => set('licenseNumber', e.target.value)} placeholder="LIC-2024-001" />
          </div>
          <div className="form-group">
            <label className="form-label">Email *</label>
            <input required type="email" value={form.email} onChange={(e) => set('email', e.target.value)} placeholder="admin@hospital.com" />
          </div>
          <div className="form-group">
            <label className="form-label">Phone</label>
            <input value={form.phone} onChange={(e) => set('phone', e.target.value)} placeholder="+91 98765 43210" />
          </div>
          <div className="form-group">
            <label className="form-label">Website</label>
            <input value={form.website} onChange={(e) => set('website', e.target.value)} placeholder="https://hospital.com" />
          </div>
        </div>

        <div style={{ border: '1px solid var(--color-border)', borderRadius: 'var(--radius-md)', padding: '1rem' }}>
          <p style={{ fontWeight: 600, marginBottom: '0.75rem', fontSize: 'var(--font-size-sm)', color: 'var(--text-secondary)' }}>Address</p>
          <div className="form-grid">
            <div className="form-group">
              <label className="form-label">Street</label>
              <input value={form.address.street} onChange={(e) => setAddr('street', e.target.value)} placeholder="123 Main St" />
            </div>
            <div className="form-group">
              <label className="form-label">City</label>
              <input value={form.address.city} onChange={(e) => setAddr('city', e.target.value)} placeholder="Mumbai" />
            </div>
            <div className="form-group">
              <label className="form-label">State</label>
              <input value={form.address.state} onChange={(e) => setAddr('state', e.target.value)} placeholder="Maharashtra" />
            </div>
            <div className="form-group">
              <label className="form-label">Country</label>
              <input value={form.address.country} onChange={(e) => setAddr('country', e.target.value)} placeholder="India" />
            </div>
            <div className="form-group">
              <label className="form-label">Postal Code</label>
              <input value={form.address.postalCode} onChange={(e) => setAddr('postalCode', e.target.value)} placeholder="400001" />
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'flex-end', marginTop: '0.5rem' }}>
          <Button type="button" variant="secondary" onClick={onClose}>Cancel</Button>
          <Button type="submit" variant="primary" loading={loading}>{initial?.id ? 'Update' : 'Create'}</Button>
        </div>
      </form>
    </Modal>
  );
};

const HospitalList = () => {
  const navigate = useNavigate();
  const { hasAnyRole } = useAuthStore();
  const isSuperAdmin = hasAnyRole(['SUPER_ADMIN']);

  const [data, setData] = useState(null);
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');
  const [isActiveFilter, setIsActiveFilter] = useState('');
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [editHospital, setEditHospital] = useState(null);

  const fetchHospitals = useCallback(async () => {
    setLoading(true);
    try {
      const params = { page, size: 10, sort: 'createdAt,desc' };
      if (search) params.search = search;
      if (isActiveFilter !== '') params.isActive = isActiveFilter;
      const res = await hospitalApi.getAll(params);
      setData(res.data.data);
    } catch (err) {
      toast.error('Failed to fetch hospitals');
    } finally {
      setLoading(false);
    }
  }, [page, search, isActiveFilter]);

  useEffect(() => { fetchHospitals(); }, [fetchHospitals]);

  // Debounce search
  useEffect(() => { setPage(0); }, [search, isActiveFilter]);

  const handleToggle = async (hospital) => {
    try {
      if (hospital.isActive) {
        await hospitalApi.deactivate(hospital.id);
        toast.success('Hospital deactivated');
      } else {
        await hospitalApi.activate(hospital.id);
        toast.success('Hospital activated');
      }
      fetchHospitals();
    } catch (err) {
      toast.error('Action failed');
    }
  };

  return (
    <div className="animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Hospitals</h1>
          <p className="page-subtitle">Manage hospital tenants across the platform</p>
        </div>
        {isSuperAdmin && (
          <Button id="create-hospital-btn" variant="primary" icon={Plus} onClick={() => { setEditHospital(null); setShowModal(true); }}>
            New Hospital
          </Button>
        )}
      </div>

      {/* Filters */}
      <div className="filters-bar">
        <SearchInput id="hospital-search" value={search} onChange={setSearch} placeholder="Search hospitals…" />
        <select
          value={isActiveFilter}
          onChange={(e) => setIsActiveFilter(e.target.value)}
          style={{ width: 160, background: 'var(--color-bg-input)', border: '1px solid var(--color-border)', color: 'var(--text-secondary)', fontSize: 'var(--font-size-sm)', padding: '0.625rem 0.875rem', borderRadius: 'var(--radius-md)' }}
        >
          <option value="">All Status</option>
          <option value="true">Active</option>
          <option value="false">Inactive</option>
        </select>
        <Button variant="secondary" icon={RefreshCw} onClick={fetchHospitals} size="sm" title="Refresh" />
      </div>

      {/* Table */}
      <Card padding={false}>
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Hospital Name</th>
                <th>Registration</th>
                <th className="hide-md">City</th>
                <th className="hide-md">Email</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                Array.from({ length: 5 }).map((_, i) => (
                  <tr key={i}>
                    {Array.from({ length: 6 }).map((_, j) => (
                      <td key={j}><div className="skeleton" style={{ height: 18, width: '80%' }} /></td>
                    ))}
                  </tr>
                ))
              ) : data?.content?.length ? (
                data.content.map((h) => (
                  <tr key={h.id}>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
                        <div style={{ width: 32, height: 32, borderRadius: 'var(--radius-sm)', background: 'rgba(59,130,246,0.12)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                          <Building2 size={14} color="var(--color-primary)" />
                        </div>
                        <div>
                          <p style={{ fontWeight: 600, color: 'var(--text-primary)', fontSize: 'var(--font-size-sm)' }}>{h.name}</p>
                          <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>{h.licenseNumber}</p>
                        </div>
                      </div>
                    </td>
                    <td>{h.registrationNumber}</td>
                    <td className="hide-md">
                      <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', color: 'var(--text-secondary)' }}>
                        <MapPin size={12} /> {h.city ?? '—'}
                      </span>
                    </td>
                    <td className="hide-md">
                      <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', color: 'var(--text-secondary)' }}>
                        <Mail size={12} /> {h.email ?? '—'}
                      </span>
                    </td>
                    <td><StatusBadge isActive={h.isActive} /></td>
                    <td>
                      <div style={{ display: 'flex', gap: '0.375rem' }}>
                        <Button size="sm" variant="secondary" icon={Eye} onClick={() => navigate(`/hospitals/${h.id}`)} title="View" />
                        {isSuperAdmin && (
                          <>
                            <Button size="sm" variant="secondary" onClick={() => { setEditHospital(h); setShowModal(true); }} title="Edit">
                              Edit
                            </Button>
                            <Button
                              size="sm"
                              variant={h.isActive ? 'danger' : 'success'}
                              icon={h.isActive ? ToggleLeft : ToggleRight}
                              onClick={() => handleToggle(h)}
                              title={h.isActive ? 'Deactivate' : 'Activate'}
                            />
                          </>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan={6}>
                    <div className="empty-state">
                      <Building2 size={40} />
                      <h3>No hospitals found</h3>
                      <p>Try adjusting your search or filters</p>
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

      <HospitalFormModal
        isOpen={showModal}
        onClose={() => setShowModal(false)}
        onSuccess={fetchHospitals}
        initial={editHospital}
      />
    </div>
  );
};

export default HospitalList;
