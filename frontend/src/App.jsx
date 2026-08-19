import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';

// Auth pages
import Login from './pages/Login';
import Register from './pages/Register';
import VerifyEmail from './pages/VerifyEmail';

// Protected pages
import Dashboard from './pages/Dashboard';
import Profile from './pages/Profile';
import HospitalList from './pages/hospitals/HospitalList';
import HospitalDetail from './pages/hospitals/HospitalDetail';
import DoctorList from './pages/doctors/DoctorList';
import DoctorDetail from './pages/doctors/DoctorDetail';
import DepartmentList from './pages/departments/DepartmentList';
import AppointmentList from './pages/appointments/AppointmentList';
import PatientList from './pages/patients/PatientList';

import MedicalRecords from './pages/MedicalRecords';
import Prescriptions from './pages/Prescriptions';
import Laboratory from './pages/Laboratory';
import Pharmacy from './pages/Pharmacy';
import Billing from './pages/Billing';
import IpdManagement from './pages/IpdManagement';
import Telemedicine from './pages/Telemedicine';

// Layout
import ProtectedRoute, { AppShell } from './components/Layout/ProtectedRoute';

function App() {
  return (
    <BrowserRouter>
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 4000,
          style: {
            background: '#ffffff',
            color: 'var(--text-primary)',
            border: '1px solid var(--color-border)',
            borderRadius: '1rem',
            fontSize: 'var(--font-size-sm)',
            fontFamily: 'var(--font-family)',
            boxShadow: 'var(--shadow-md)',
          },
          success: {
            iconTheme: { primary: '#0a4d4a', secondary: '#c8ed45' },
          },
          error: {
            iconTheme: { primary: '#e11d48', secondary: '#ffffff' },
          },
        }}
      />

      <Routes>
        {/* Public */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/verify-email" element={<VerifyEmail />} />

        {/* Protected shell */}
        <Route element={<AppShell />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/appointments" element={<AppointmentList />} />
          <Route path="/medical-records" element={<MedicalRecords />} />
          <Route path="/prescriptions" element={<Prescriptions />} />
          <Route path="/laboratory" element={<Laboratory />} />
          <Route path="/pharmacy" element={<Pharmacy />} />
          <Route path="/billing" element={<Billing />} />
          <Route path="/ipd" element={<IpdManagement />} />
          <Route path="/telemedicine" element={<Telemedicine />} />
          <Route path="/departments" element={<DepartmentList />} />

          <Route element={<ProtectedRoute roles={['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST']} />}>
            <Route path="/patients" element={<PatientList />} />
          </Route>

          <Route element={<ProtectedRoute roles={['SUPER_ADMIN', 'HOSPITAL_ADMIN']} />}>
            <Route path="/hospitals" element={<HospitalList />} />
            <Route path="/hospitals/:id" element={<HospitalDetail />} />
          </Route>

          <Route element={<ProtectedRoute roles={['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'RECEPTIONIST', 'PATIENT']} />}>
            <Route path="/doctors" element={<DoctorList />} />
            <Route path="/doctors/:id" element={<DoctorDetail />} />
          </Route>
        </Route>

        {/* Catch-all */}
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
