import React, { useState, useRef } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { Heart, Mail, RefreshCw } from 'lucide-react';
import toast from 'react-hot-toast';
import { authApi } from '../api/auth.api';
import useAuthStore from '../store/authStore';
import Button from '../components/ui/Button';

const VerifyEmail = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { setAuth } = useAuthStore();

  const [email, setEmail] = useState(location.state?.email ?? '');
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  const inputRefs = useRef([]);

  const handleOtpChange = (index, value) => {
    if (!/^\d*$/.test(value)) return;
    const next = [...otp];
    next[index] = value.slice(-1);
    setOtp(next);
    if (value && index < 5) inputRefs.current[index + 1]?.focus();
  };

  const handleKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs.current[index - 1]?.focus();
    }
  };

  const handlePaste = (e) => {
    e.preventDefault();
    const pasted = e.clipboardData.getData('text').replace(/\D/g, '').slice(0, 6);
    const next = [...otp];
    for (let i = 0; i < pasted.length; i++) next[i] = pasted[i];
    setOtp(next);
    inputRefs.current[Math.min(pasted.length, 5)]?.focus();
  };

  const handleVerify = async () => {
    const otpStr = otp.join('');
    if (otpStr.length < 6) { toast.error('Enter the full 6-digit OTP'); return; }
    if (!email) { toast.error('Email is required'); return; }

    setLoading(true);
    try {
      const res = await authApi.verifyEmail({ email, otp: otpStr });
      const { token, refresh_token, user } = res.data;
      setAuth(token, refresh_token, user);
      try {
        const meRes = await authApi.me();
        setAuth(token, refresh_token, meRes.data);
      } catch {}
      toast.success('Email verified! Welcome to MedCore.');
      navigate('/dashboard');
    } catch (err) {
      const msg = err.response?.data?.detail ?? err.response?.data?.message ?? 'Invalid or expired OTP.';
      toast.error(msg);
      if (err.response?.status === 410) {
        toast('Click Resend OTP to get a new code.', { icon: 'ℹ️' });
      }
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (!email) { toast.error('Email is required'); return; }
    setResending(true);
    try {
      await authApi.resendOtp({ email });
      toast.success('OTP resent! Check your inbox (valid 15 minutes).');
    } catch (err) {
      const msg = err.response?.data?.detail ?? err.response?.data?.message ?? 'Failed to resend OTP. Register again if the session expired.';
      toast.error(msg);
    } finally {
      setResending(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-card">
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', marginBottom: '2rem' }}>
          <div
            style={{
              width: 56,
              height: 56,
              borderRadius: '1rem',
              background: 'var(--gradient-primary)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              marginBottom: '1rem',
              boxShadow: 'var(--shadow-glow-strong)',
            }}
          >
            <Mail size={26} color="#fff" />
          </div>
          <h1 style={{ fontSize: 'var(--font-size-2xl)', fontWeight: 800, marginBottom: '0.25rem' }}>
            Verify Email
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: 'var(--font-size-sm)', textAlign: 'center' }}>
            We sent a 6-digit OTP to your email.<br />Enter it within 15 minutes to activate your account.
          </p>
        </div>

        {/* Email input (if not prefilled from navigation state) */}
        {!location.state?.email && (
          <div className="form-group" style={{ marginBottom: '1.25rem' }}>
            <label className="form-label" htmlFor="verify-email">Email Address</label>
            <input
              id="verify-email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="you@hospital.com"
            />
          </div>
        )}

        {/* OTP Boxes */}
        <div
          style={{
            display: 'flex',
            gap: '0.625rem',
            justifyContent: 'center',
            marginBottom: '1.5rem',
          }}
          onPaste={handlePaste}
        >
          {otp.map((digit, i) => (
            <input
              key={i}
              ref={(el) => (inputRefs.current[i] = el)}
              type="text"
              inputMode="numeric"
              maxLength={1}
              value={digit}
              onChange={(e) => handleOtpChange(i, e.target.value)}
              onKeyDown={(e) => handleKeyDown(i, e)}
              style={{
                width: 52,
                height: 58,
                textAlign: 'center',
                fontSize: '1.5rem',
                fontWeight: 700,
                borderRadius: 'var(--radius-md)',
                border: digit
                  ? '2px solid var(--color-primary)'
                  : '1px solid var(--color-border)',
                background: digit ? 'rgba(59,130,246,0.08)' : 'var(--color-bg-input)',
                color: 'var(--text-primary)',
                outline: 'none',
                transition: 'all var(--transition-fast)',
                boxShadow: digit ? 'var(--shadow-glow)' : 'none',
              }}
            />
          ))}
        </div>

        <Button
          id="verify-email-btn"
          type="button"
          variant="primary"
          size="lg"
          loading={loading}
          onClick={handleVerify}
        >
          Verify & Continue
        </Button>

        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.5rem', marginTop: '1.25rem' }}>
          <span style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-muted)' }}>
            Didn't receive it?
          </span>
          <Button
            id="resend-otp-btn"
            variant="secondary"
            size="sm"
            loading={resending}
            icon={RefreshCw}
            onClick={handleResend}
          >
            Resend OTP
          </Button>
        </div>

        <div className="divider" />
        <p style={{ textAlign: 'center', fontSize: 'var(--font-size-sm)', color: 'var(--text-muted)' }}>
          <Link to="/login" style={{ color: 'var(--color-primary-light)', fontWeight: 600 }}>← Back to Login</Link>
        </p>
      </div>
    </div>
  );
};

export default VerifyEmail;
