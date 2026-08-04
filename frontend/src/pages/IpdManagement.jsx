import React, { useState } from 'react';
import { Bed, Plus, UserPlus, ArrowRightLeft, LogOut } from 'lucide-react';
import toast from 'react-hot-toast';

export default function IpdManagement() {
  const [admissions] = useState([
    {
      id: '1',
      patientName: 'Clark Kent',
      doctorName: 'Dr. John Watson',
      wardName: 'ICU Ward A',
      roomNumber: 'Room 102',
      bedNumber: 'Bed B-102',
      admissionDate: '2026-08-01',
      reason: 'Post-surgical monitoring & oxygen therapy',
      status: 'ADMITTED'
    }
  ]);

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '700', color: 'var(--text-primary)' }}>Inpatient Admissions (IPD)</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Manage hospital ward allocation, bed availability, patient transfers, and discharge summaries</p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button className="btn btn-secondary" onClick={() => toast.success('Transfer Bed Modal')}>
            <ArrowRightLeft size={18} style={{ marginRight: '0.5rem' }} /> Transfer Bed
          </button>
          <button className="btn btn-primary" onClick={() => toast.success('Admit Patient Modal')}>
            <UserPlus size={18} style={{ marginRight: '0.5rem' }} /> Admit Patient
          </button>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(380px, 1fr))', gap: '1.5rem' }}>
        {admissions.map((adm) => (
          <div key={adm.id} className="card" style={{ padding: '1.5rem', borderRadius: '12px', border: '1px solid var(--color-border)', background: 'var(--color-bg-elevated)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <div style={{ padding: '0.5rem', borderRadius: '8px', background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444' }}>
                  <Bed size={24} />
                </div>
                <div>
                  <h3 style={{ fontSize: '1.1rem', fontWeight: '600' }}>{adm.patientName}</h3>
                  <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Admitted: {adm.admissionDate}</span>
                </div>
              </div>
              <span className="badge" style={{ background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444', padding: '0.25rem 0.6rem', borderRadius: '6px', fontSize: '0.8rem' }}>{adm.status}</span>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.9rem' }}>
              <div><strong>Attending Doctor:</strong> {adm.doctorName}</div>
              <div style={{ marginTop: '0.5rem', background: 'var(--color-bg-subtle)', padding: '0.75rem', borderRadius: '8px' }}>
                <div><strong>Ward:</strong> {adm.wardName}</div>
                <div><strong>Room & Bed:</strong> {adm.roomNumber} ({adm.bedNumber})</div>
                <div style={{ marginTop: '0.2rem', color: 'var(--text-secondary)' }}>Reason: {adm.reason}</div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
