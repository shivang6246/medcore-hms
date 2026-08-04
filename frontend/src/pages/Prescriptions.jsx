import React, { useState } from 'react';
import { Pill, Plus, CheckCircle, Clock } from 'lucide-react';
import toast from 'react-hot-toast';

export default function Prescriptions() {
  const [prescriptions] = useState([
    {
      id: '1',
      medicineName: 'Amoxicillin 500mg',
      dosage: '1 Capsule',
      frequency: 'Thrice daily (Every 8 hours)',
      duration: '7 Days',
      instructions: 'Take after meals with plenty of water',
      quantity: 21,
      isActive: true
    }
  ]);

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '700', color: 'var(--text-primary)' }}>Prescriptions</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Manage electronic prescriptions and medicine dosages</p>
        </div>
        <button className="btn btn-primary" onClick={() => toast.success('Add Prescription')}>
          <Plus size={18} style={{ marginRight: '0.5rem' }} /> New Prescription
        </button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '1.5rem' }}>
        {prescriptions.map((p) => (
          <div key={p.id} className="card" style={{ padding: '1.5rem', borderRadius: '12px', border: '1px solid var(--color-border)', background: 'var(--color-bg-elevated)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <div style={{ padding: '0.5rem', borderRadius: '8px', background: 'rgba(16, 185, 129, 0.1)', color: '#10b981' }}>
                  <Pill size={24} />
                </div>
                <div>
                  <h3 style={{ fontSize: '1.1rem', fontWeight: '600' }}>{p.medicineName}</h3>
                  <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Qty: {p.quantity}</span>
                </div>
              </div>
              <span className="badge badge-success" style={{ background: 'rgba(16, 185, 129, 0.1)', color: '#10b981', padding: '0.25rem 0.6rem', borderRadius: '6px', fontSize: '0.8rem' }}>Active</span>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.9rem' }}>
              <div><strong>Dosage:</strong> {p.dosage}</div>
              <div><strong>Frequency:</strong> {p.frequency}</div>
              <div><strong>Duration:</strong> {p.duration}</div>
              <div style={{ marginTop: '0.5rem', background: 'var(--color-bg-subtle)', padding: '0.75rem', borderRadius: '8px' }}>
                <strong style={{ fontSize: '0.85rem' }}>Instructions:</strong>
                <p style={{ color: 'var(--text-secondary)', marginTop: '0.2rem' }}>{p.instructions}</p>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
