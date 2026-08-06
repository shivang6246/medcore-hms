import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Eye, EyeOff, Heart, User, Mail, Lock, Phone } from 'lucide-react';
import toast from 'react-hot-toast';
import { authApi } from '../api/auth.api';
import Button from '../components/ui/Button';

const schema = z.object({
  firstName: z.string().min(1, 'First name is required').max(100),
  lastName: z.string().min(1, 'Last name is required').max(100),
  email: z.string().email('Invalid email').max(150),
  phone: z.string().max(20).optional(),
  password: z.string().min(8, 'Password must be at least 8 characters').max(100),
  confirmPassword: z.string(),
  roleName: z.enum(['PATIENT', 'DOCTOR']),
}).refine((d) => d.password === d.confirmPassword, {
  message: "Passwords don't match",
  path: ['confirmPassword'],
});

const ROLES = [
  { value: 'PATIENT', label: 'Patient' },
  { value: 'DOCTOR', label: 'Doctor' },
];

const Register = () => {
  const navigate = useNavigate();
  const [showPw, setShowPw] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
    resolver: zodResolver(schema),
    defaultValues: { roleName: 'PATIENT' },
  });

  const onSubmit = async (data) => {
    try {
      const payload = {
        firstName: data.firstName,
        lastName: data.lastName,
        email: data.email,
        password: data.password,
        phone: data.phone || undefined,
        roleName: data.roleName || 'PATIENT',
      };
      await authApi.register(payload);
      toast.success('Registered! Check your email for the OTP.');
      navigate('/verify-email', { state: { email: data.email } });
    } catch (err) {
      const msg = err.response?.data?.detail ?? err.response?.data?.message ?? 'Registration failed.';
      toast.error(msg);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card" style={{ maxWidth: 520 }}>
        {/* Logo */}
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginBottom: '2rem' }}>
          <div
            style={{
              width: 52,
              height: 52,
              borderRadius: '1rem',
              background: 'var(--gradient-primary)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              marginBottom: '0.875rem',
              boxShadow: 'var(--shadow-glow-strong)',
            }}
          >
            <Heart size={24} color="#fff" fill="#fff" />
          </div>
          <h1 style={{ fontSize: 'var(--font-size-2xl)', fontWeight: 800, marginBottom: '0.25rem' }}>
            Create Account
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: 'var(--font-size-sm)' }}>
            Join MedCore HMS
          </p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          <div className="form-grid">
            {/* First Name */}
            <div className="form-group">
              <label className="form-label" htmlFor="reg-firstname">First Name</label>
              <div style={{ position: 'relative' }}>
                <User size={15} style={{ position: 'absolute', left: '0.875rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)', pointerEvents: 'none' }} />
                <input id="reg-firstname" placeholder="John" style={{ paddingLeft: '2.5rem' }} {...register('firstName')} />
              </div>
              {errors.firstName && <span className="form-error">{errors.firstName.message}</span>}
            </div>

            {/* Last Name */}
            <div className="form-group">
              <label className="form-label" htmlFor="reg-lastname">Last Name</label>
              <div style={{ position: 'relative' }}>
                <User size={15} style={{ position: 'absolute', left: '0.875rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)', pointerEvents: 'none' }} />
                <input id="reg-lastname" placeholder="Doe" style={{ paddingLeft: '2.5rem' }} {...register('lastName')} />
              </div>
              {errors.lastName && <span className="form-error">{errors.lastName.message}</span>}
            </div>
          </div>

          {/* Email */}
          <div className="form-group">
            <label className="form-label" htmlFor="reg-email">Email Address</label>
            <div style={{ position: 'relative' }}>
              <Mail size={15} style={{ position: 'absolute', left: '0.875rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)', pointerEvents: 'none' }} />
              <input id="reg-email" type="email" placeholder="you@hospital.com" style={{ paddingLeft: '2.5rem' }} {...register('email')} />
            </div>
            {errors.email && <span className="form-error">{errors.email.message}</span>}
          </div>

          <div className="form-grid">
            {/* Phone */}
            <div className="form-group">
              <label className="form-label" htmlFor="reg-phone">Phone (optional)</label>
              <div style={{ position: 'relative' }}>
                <Phone size={15} style={{ position: 'absolute', left: '0.875rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)', pointerEvents: 'none' }} />
                <input id="reg-phone" placeholder="+91 98765 43210" style={{ paddingLeft: '2.5rem' }} {...register('phone')} />
              </div>
              {errors.phone && <span className="form-error">{errors.phone.message}</span>}
            </div>

            {/* Role */}
            <div className="form-group">
              <label className="form-label" htmlFor="reg-role">I am registering as</label>
              <select id="reg-role" {...register('roleName')}>
                {ROLES.map((r) => (
                  <option key={r.value} value={r.value}>{r.label}</option>
                ))}
              </select>
              <span style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)', marginTop: '0.35rem', display: 'block' }}>
                Creates a login plus a matching patient or doctor profile. Staff roles are assigned by admins.
              </span>
            </div>
          </div>

          {/* Password */}
          <div className="form-group">
            <label className="form-label" htmlFor="reg-password">Password</label>
            <div style={{ position: 'relative' }}>
              <Lock size={15} style={{ position: 'absolute', left: '0.875rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)', pointerEvents: 'none' }} />
              <input id="reg-password" type={showPw ? 'text' : 'password'} placeholder="Min 8 characters" style={{ paddingLeft: '2.5rem', paddingRight: '2.75rem' }} {...register('password')} />
              <button type="button" onClick={() => setShowPw(!showPw)} style={{ position: 'absolute', right: '0.875rem', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)', padding: 0, display: 'flex' }}>
                {showPw ? <EyeOff size={15} /> : <Eye size={15} />}
              </button>
            </div>
            {errors.password && <span className="form-error">{errors.password.message}</span>}
          </div>

          {/* Confirm Password */}
          <div className="form-group">
            <label className="form-label" htmlFor="reg-confirm">Confirm Password</label>
            <div style={{ position: 'relative' }}>
              <Lock size={15} style={{ position: 'absolute', left: '0.875rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)', pointerEvents: 'none' }} />
              <input id="reg-confirm" type={showConfirm ? 'text' : 'password'} placeholder="Re-enter password" style={{ paddingLeft: '2.5rem', paddingRight: '2.75rem' }} {...register('confirmPassword')} />
              <button type="button" onClick={() => setShowConfirm(!showConfirm)} style={{ position: 'absolute', right: '0.875rem', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)', padding: 0, display: 'flex' }}>
                {showConfirm ? <EyeOff size={15} /> : <Eye size={15} />}
              </button>
            </div>
            {errors.confirmPassword && <span className="form-error">{errors.confirmPassword.message}</span>}
          </div>

          <Button id="register-submit-btn" type="submit" variant="primary" size="lg" loading={isSubmitting}>
            Create Account
          </Button>
        </form>

        <div className="divider" />
        <p style={{ textAlign: 'center', fontSize: 'var(--font-size-sm)', color: 'var(--text-muted)' }}>
          Already have an account?{' '}
          <Link to="/login" style={{ color: 'var(--color-primary-light)', fontWeight: 600 }}>Sign In</Link>
        </p>
      </div>
    </div>
  );
};

export default Register;
