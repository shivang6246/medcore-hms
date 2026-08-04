import React, { useState } from 'react';
import { FlaskConical, CheckCircle2, Clock, Upload, Send } from 'lucide-react';
import toast from 'react-hot-toast';

export default function Laboratory() {
  const [labTests] = useState([
    {
      id: '1',
      testType: 'Complete Blood Count (CBC)',
      priority: 'URGENT',
      status: 'PUBLISHED',
      patientName: 'Jane Smith',
      doctorName: 'Dr. Gregory House',
      result: 'Hemoglobin: 14.2 g/dL, WBC: 6,500 /mcL, Platelets: 250,000 /mcL',
      remarks: 'All hematology parameters within normal physiological limits.'
    }
  ]);

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '700', color: 'var(--text-primary)' }}>Laboratory Management</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Track diagnostic orders, technician assignments, and lab report publishing</p>
        </div>
        <button className="btn btn-primary" onClick={() => toast.success('New Lab Order')}>
          <FlaskConical size={18} style={{ marginRight: '0.5rem' }} /> Order Lab Test
        </button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(380px, 1fr))', gap: '1.5rem' }}>
        {labTests.map((t) => (
          <div key={t.id} className="card" style={{ padding: '1.5rem', borderRadius: '12px', border: '1px solid var(--color-border)', background: 'var(--color-bg-elevated)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <div style={{ padding: '0.5rem', borderRadius: '8px', background: 'rgba(139, 92, 246, 0.1)', color: '#8b5cf6' }}>
                  <FlaskConical size={24} />
                </div>
                <div>
                  <h3 style={{ fontSize: '1.1rem', fontWeight: '600' }}>{t.testType}</h3>
                  <span style={{ fontSize: '0.85rem', color: '#ef4444', fontWeight: '600' }}>{t.priority}</span>
                </div>
              </div>
              <span className="badge" style={{ background: 'rgba(16, 185, 129, 0.1)', color: '#10b981', padding: '0.25rem 0.6rem', borderRadius: '6px', fontSize: '0.8rem' }}>{t.status}</span>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.9rem' }}>
              <div><strong>Patient:</strong> {t.patientName}</div>
              <div><strong>Ordering Doctor:</strong> {t.doctorName}</div>
              <div style={{ marginTop: '0.5rem', background: 'var(--color-bg-subtle)', padding: '0.75rem', borderRadius: '8px' }}>
                <strong>Lab Results:</strong>
                <p style={{ color: 'var(--text-primary)', marginTop: '0.2rem', fontFamily: 'monospace' }}>{t.result}</p>
                <small style={{ color: 'var(--text-secondary)' }}>Remarks: {t.remarks}</small>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
