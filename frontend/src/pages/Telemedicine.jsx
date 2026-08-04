import React, { useState } from 'react';
import { Video, User, Clock, CheckCircle, ExternalLink, Play } from 'lucide-react';
import toast from 'react-hot-toast';

export default function Telemedicine() {
  const [sessions] = useState([
    {
      id: '1',
      roomCode: 'ROOM-TELE-9901',
      patientName: 'Peter Parker',
      doctorName: 'Dr. Stephen Strange',
      scheduledStartTime: '2026-08-04 15:00',
      status: 'WAITING_ROOM',
      meetingUrl: 'https://telehealth.medcore.hms/meet/ROOM-TELE-9901'
    }
  ]);

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '700', color: 'var(--text-primary)' }}>Telemedicine & Virtual Rooms</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Virtual video consultation rooms, waiting queues, and meeting tokens</p>
        </div>
        <button className="btn btn-primary" onClick={() => toast.success('Video Consultation Room Link Created')}>
          <Video size={18} style={{ marginRight: '0.5rem' }} /> Create Consultation Room
        </button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(380px, 1fr))', gap: '1.5rem' }}>
        {sessions.map((s) => (
          <div key={s.id} className="card" style={{ padding: '1.5rem', borderRadius: '12px', border: '1px solid var(--color-border)', background: 'var(--color-bg-elevated)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <div style={{ padding: '0.5rem', borderRadius: '8px', background: 'rgba(59, 130, 246, 0.1)', color: '#3b82f6' }}>
                  <Video size={24} />
                </div>
                <div>
                  <h3 style={{ fontSize: '1.1rem', fontWeight: '600' }}>{s.roomCode}</h3>
                  <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{s.scheduledStartTime}</span>
                </div>
              </div>
              <span className="badge" style={{ background: 'rgba(245, 158, 11, 0.1)', color: '#f59e0b', padding: '0.25rem 0.6rem', borderRadius: '6px', fontSize: '0.8rem' }}>Waiting Room</span>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.9rem' }}>
              <div><strong>Patient:</strong> {s.patientName}</div>
              <div><strong>Doctor:</strong> {s.doctorName}</div>
              <div style={{ marginTop: '1rem', display: 'flex', gap: '0.5rem' }}>
                <button className="btn btn-primary" style={{ flex: 1 }} onClick={() => window.open(s.meetingUrl, '_blank')}>
                  <Play size={16} style={{ marginRight: '0.4rem' }} /> Join Meeting Room
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
