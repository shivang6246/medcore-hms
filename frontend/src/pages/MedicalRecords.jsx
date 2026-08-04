import React, { useState } from 'react';
import { FileText, Plus, Search, Activity, User, Stethoscope } from 'lucide-react';
import toast from 'react-hot-toast';

export default function MedicalRecords() {
  const [records] = useState([
    {
      id: '1',
      patientName: 'John Doe',
      doctorName: 'Dr. Sarah Connor',
      symptoms: 'High fever, persistent cough, fatigue',
      diagnosis: 'Acute Bronchitis',
      treatmentPlan: 'Prescribed Amoxicillin & Rest for 5 days',
      notes: 'Patient advised to follow up if fever exceeds 102F',
      createdAt: '2026-08-04'
    }
  ]);

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '700', color: 'var(--text-primary)' }}>Medical Records</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Manage patient EHR clinical notes and diagnoses</p>
        </div>
        <button className="btn btn-primary" onClick={() => toast.success('New Record Modal')}>
          <Plus size={18} style={{ marginRight: '0.5rem' }} /> Create Record
        </button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '1.5rem' }}>
        {records.map((r) => (
          <div key={r.id} className="card" style={{ padding: '1.5rem', borderRadius: '12px', border: '1px solid var(--color-border)', background: 'var(--color-bg-elevated)' }}>
            <div style={{ display: 'flex', alignItems: 'center', marginBottom: '1rem', gap: '0.75rem' }}>
              <div style={{ padding: '0.5rem', borderRadius: '8px', background: 'rgba(59, 130, 246, 0.1)', color: '#3b82f6' }}>
                <FileText size={24} />
              </div>
              <div>
                <h3 style={{ fontSize: '1.1rem', fontWeight: '600' }}>{r.diagnosis}</h3>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{r.createdAt}</span>
              </div>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.9rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <User size={16} style={{ color: 'var(--text-secondary)' }} />
                <strong>Patient:</strong> {r.patientName}
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Stethoscope size={16} style={{ color: 'var(--text-secondary)' }} />
                <strong>Doctor:</strong> {r.doctorName}
              </div>
              <div style={{ marginTop: '0.5rem' }}>
                <strong>Symptoms:</strong>
                <p style={{ color: 'var(--text-secondary)', marginTop: '0.2rem' }}>{r.symptoms}</p>
              </div>
              <div>
                <strong>Treatment Plan:</strong>
                <p style={{ color: 'var(--text-secondary)', marginTop: '0.2rem' }}>{r.treatmentPlan}</p>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
