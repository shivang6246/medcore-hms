import React, { useState, useEffect } from 'react';
import { Layers, RefreshCw } from 'lucide-react';
import { departmentApi } from '../../api/department.api';
import useAuthStore from '../../store/authStore';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import { StatusBadge } from '../../components/ui/Badge';
import toast from 'react-hot-toast';

const DepartmentList = () => {
  const { user } = useAuthStore();
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchDepts = async () => {
    if (!user?.hospitalId) {
      toast.error('No hospital associated with your account.');
      return;
    }
    setLoading(true);
    try {
      const res = await departmentApi.getByHospital(user.hospitalId);
      setDepartments(res.data.data ?? []);
    } catch {
      toast.error('Failed to fetch departments');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchDepts(); }, [user?.hospitalId]);

  return (
    <div className="animate-fade-in">
      <div className="page-header">
        <div>
          <h1 className="page-title">Departments</h1>
          <p className="page-subtitle">Hospital departments for {user?.hospitalName ?? 'your hospital'}</p>
        </div>
        <Button variant="secondary" icon={RefreshCw} onClick={fetchDepts} size="sm">Refresh</Button>
      </div>

      {loading ? (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: '1rem' }}>
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="glass-card p-6">
              <div className="skeleton" style={{ height: 20, width: '70%', marginBottom: '0.75rem' }} />
              <div className="skeleton" style={{ height: 14, width: '90%' }} />
            </div>
          ))}
        </div>
      ) : departments.length ? (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: '1rem' }}>
          {departments.map((dept) => (
            <Card key={dept.id} hover>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.625rem' }}>
                <div
                  style={{
                    width: 40,
                    height: 40,
                    borderRadius: 'var(--radius-md)',
                    background: 'rgba(10, 77, 74,0.12)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: 'var(--color-primary)',
                  }}
                >
                  <Layers size={18} />
                </div>
                <StatusBadge isActive={dept.isActive} />
              </div>
              <h4 style={{ fontWeight: 700, fontSize: 'var(--font-size-base)', marginBottom: '0.375rem' }}>{dept.name}</h4>
              {dept.description && (
                <p style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-muted)', lineHeight: 1.6 }}>{dept.description}</p>
              )}
            </Card>
          ))}
        </div>
      ) : (
        <Card>
          <div className="empty-state">
            <Layers size={48} />
            <h3>No departments found</h3>
            <p>No departments are configured for this hospital yet.</p>
          </div>
        </Card>
      )}
    </div>
  );
};

export default DepartmentList;
