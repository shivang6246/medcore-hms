import React, { useEffect, useRef, useState } from 'react';
import { NavLink, useNavigate, useLocation, Link } from 'react-router-dom';
import {
  LayoutDashboard,
  CalendarDays,
  Users,
  Stethoscope,
  Building2,
  Layers,
  FileText,
  Pill,
  FlaskConical,
  Package,
  CreditCard,
  Bed,
  Video,
  UserCircle,
  Search,
  Bell,
  Settings,
  LogOut,
  Menu,
  X,
  ChevronDown,
  Heart,
  Activity,
} from 'lucide-react';
import useAuthStore from '../../store/authStore';
import { authApi } from '../../api/auth.api';
import toast from 'react-hot-toast';

const PRIMARY_NAV = [
  { label: 'Dashboard', path: '/dashboard', icon: LayoutDashboard, roles: null },
  { label: 'Schedule', path: '/appointments', icon: CalendarDays, roles: ['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST', 'PATIENT'] },
  { label: 'Patients', path: '/patients', icon: Users, roles: ['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST'] },
  { label: 'Doctors', path: '/doctors', icon: Stethoscope, roles: ['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'RECEPTIONIST', 'PATIENT'] },
];

const MORE_NAV = [
  { label: 'Hospitals', path: '/hospitals', icon: Building2, roles: ['SUPER_ADMIN', 'HOSPITAL_ADMIN'] },
  { label: 'Departments', path: '/departments', icon: Layers, roles: ['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DEPARTMENT_HEAD'] },
  { label: 'Medical Records', path: '/medical-records', icon: FileText, roles: ['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE', 'PATIENT'] },
  { label: 'Prescriptions', path: '/prescriptions', icon: Pill, roles: ['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'PHARMACIST', 'PATIENT'] },
  { label: 'Laboratory', path: '/laboratory', icon: FlaskConical, roles: ['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'LAB_TECHNICIAN', 'PATIENT'] },
  { label: 'Pharmacy', path: '/pharmacy', icon: Package, roles: ['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'PHARMACIST'] },
  { label: 'Billing', path: '/billing', icon: CreditCard, roles: ['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'ACCOUNTANT', 'RECEPTIONIST', 'PATIENT'] },
  { label: 'Inpatient', path: '/ipd', icon: Bed, roles: ['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST'] },
  { label: 'Telemedicine', path: '/telemedicine', icon: Video, roles: ['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'PATIENT', 'RECEPTIONIST'] },
  { label: 'Activity', path: '/profile', icon: Activity, roles: null },
];

const Header = () => {
  const { user, refreshToken, clearAuth, hasAnyRole } = useAuthStore();
  const navigate = useNavigate();
  const location = useLocation();
  const [moreOpen, setMoreOpen] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const moreRef = useRef(null);

  const visiblePrimary = PRIMARY_NAV.filter((item) => !item.roles || hasAnyRole(item.roles));
  const visibleMore = MORE_NAV.filter((item) => !item.roles || hasAnyRole(item.roles));
  const moreActive = visibleMore.some((item) => location.pathname.startsWith(item.path));

  const initials = user
    ? `${user.firstName?.[0] ?? ''}${user.lastName?.[0] ?? ''}`.toUpperCase()
    : '?';

  const roleLabel = user?.roles?.[0]?.replace(/_/g, ' ') ?? 'User';

  useEffect(() => {
    const onDoc = (e) => {
      if (moreRef.current && !moreRef.current.contains(e.target)) setMoreOpen(false);
    };
    document.addEventListener('mousedown', onDoc);
    return () => document.removeEventListener('mousedown', onDoc);
  }, []);

  useEffect(() => {
    setMobileOpen(false);
    setMoreOpen(false);
  }, [location.pathname]);

  const handleLogout = async () => {
    try {
      if (refreshToken) await authApi.logout(refreshToken);
    } catch { /* ignore */ }
    clearAuth();
    navigate('/login');
    toast.success('Logged out successfully');
  };

  const navLinkStyle = ({ isActive }) => ({
    display: 'inline-flex',
    alignItems: 'center',
    gap: '0.45rem',
    padding: '0.55rem 1rem',
    borderRadius: 'var(--radius-full)',
    textDecoration: 'none',
    fontSize: '0.875rem',
    fontWeight: 600,
    transition: 'all var(--transition-fast)',
    background: isActive ? 'var(--color-primary)' : 'transparent',
    color: isActive ? '#fff' : 'var(--text-secondary)',
  });

  return (
    <header
      style={{
        position: 'sticky',
        top: 0,
        zIndex: 80,
        padding: '1rem 1.5rem 0.75rem',
        background: 'linear-gradient(180deg, rgba(238,243,246,0.96) 0%, rgba(238,243,246,0.88) 100%)',
        backdropFilter: 'blur(14px)',
      }}
    >
      <div
        style={{
          maxWidth: 'var(--shell-max)',
          margin: '0 auto',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          gap: '1rem',
        }}
      >
        {/* Brand */}
        <Link to="/dashboard" style={{ display: 'flex', alignItems: 'center', gap: '0.65rem', textDecoration: 'none', flexShrink: 0 }}>
          <div
            style={{
              width: 40,
              height: 40,
              borderRadius: '50%',
              background: 'var(--gradient-primary)',
              display: 'grid',
              placeItems: 'center',
              boxShadow: 'var(--shadow-glow)',
            }}
          >
            <Heart size={18} color="#c8ed45" fill="#c8ed45" />
          </div>
          <span
            style={{
              fontFamily: 'var(--font-display)',
              fontWeight: 800,
              fontSize: '1.15rem',
              color: 'var(--text-primary)',
              letterSpacing: '-0.03em',
            }}
          >
            MedCore
          </span>
        </Link>

        {/* Center pill nav */}
        <nav
          className="hide-md"
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '0.2rem',
            background: '#fff',
            borderRadius: 'var(--radius-full)',
            padding: '0.3rem',
            boxShadow: 'var(--shadow-sm)',
            border: '1px solid var(--color-border)',
          }}
        >
          {visiblePrimary.map((item) => (
            <NavLink key={item.path} to={item.path} style={navLinkStyle}>
              <item.icon size={16} />
              {item.label}
            </NavLink>
          ))}

          {visibleMore.length > 0 && (
            <div ref={moreRef} style={{ position: 'relative' }}>
              <button
                type="button"
                onClick={() => setMoreOpen((v) => !v)}
                style={{
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '0.35rem',
                  padding: '0.55rem 1rem',
                  borderRadius: 'var(--radius-full)',
                  border: 'none',
                  cursor: 'pointer',
                  fontSize: '0.875rem',
                  fontWeight: 600,
                  fontFamily: 'var(--font-family)',
                  background: moreActive || moreOpen ? 'var(--color-primary)' : 'transparent',
                  color: moreActive || moreOpen ? '#fff' : 'var(--text-secondary)',
                }}
              >
                <Layers size={16} />
                More
                <ChevronDown size={14} style={{ transform: moreOpen ? 'rotate(180deg)' : 'none', transition: 'transform 0.2s' }} />
              </button>

              {moreOpen && (
                <div
                  style={{
                    position: 'absolute',
                    top: 'calc(100% + 0.6rem)',
                    left: '50%',
                    transform: 'translateX(-50%)',
                    width: 260,
                    background: '#fff',
                    borderRadius: 'var(--radius-2xl)',
                    border: '1px solid var(--color-border)',
                    boxShadow: 'var(--shadow-lg)',
                    padding: '0.5rem',
                    animation: 'riseIn 0.2s ease',
                    zIndex: 90,
                  }}
                >
                  {visibleMore.map((item) => (
                    <NavLink
                      key={item.path}
                      to={item.path}
                      onClick={() => setMoreOpen(false)}
                      style={({ isActive }) => ({
                        display: 'flex',
                        alignItems: 'center',
                        gap: '0.7rem',
                        padding: '0.7rem 0.85rem',
                        borderRadius: 'var(--radius-lg)',
                        textDecoration: 'none',
                        fontSize: '0.875rem',
                        fontWeight: 600,
                        color: isActive ? 'var(--color-primary)' : 'var(--text-secondary)',
                        background: isActive ? 'rgba(10,77,74,0.08)' : 'transparent',
                      })}
                    >
                      <item.icon size={16} />
                      {item.label}
                    </NavLink>
                  ))}
                </div>
              )}
            </div>
          )}
        </nav>

        {/* Right utilities */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.55rem', flexShrink: 0 }}>
          <button type="button" className="btn btn-secondary btn-icon hide-sm" title="Search" aria-label="Search">
            <Search size={17} />
          </button>
          <button type="button" className="btn btn-secondary btn-icon hide-sm" title="Notifications" aria-label="Notifications">
            <Bell size={17} />
          </button>
          <Link to="/profile" className="btn btn-secondary btn-icon hide-sm" title="Settings" aria-label="Settings">
            <Settings size={17} />
          </Link>

          <div
            className="hide-sm"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.65rem',
              marginLeft: '0.35rem',
              paddingLeft: '0.65rem',
              borderLeft: '1px solid var(--color-border)',
            }}
          >
            <div className="avatar avatar-sm">{initials}</div>
            <div>
              <div style={{ fontSize: '0.85rem', fontWeight: 700, color: 'var(--text-primary)', lineHeight: 1.2 }}>
                {user?.firstName} {user?.lastName}
              </div>
              <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', textTransform: 'capitalize' }}>
                {roleLabel.toLowerCase()}
              </div>
            </div>
            <button type="button" className="btn btn-secondary btn-icon" onClick={handleLogout} title="Logout" aria-label="Logout">
              <LogOut size={15} />
            </button>
          </div>

          <button
            type="button"
            className="btn btn-secondary btn-icon"
            onClick={() => setMobileOpen((v) => !v)}
            style={{ display: 'none' }}
            id="mobile-nav-toggle"
            aria-label="Menu"
          >
            {mobileOpen ? <X size={18} /> : <Menu size={18} />}
          </button>
        </div>
      </div>

      {/* Mobile drawer */}
      {mobileOpen && (
        <div
          style={{
            marginTop: '0.75rem',
            background: '#fff',
            borderRadius: 'var(--radius-2xl)',
            border: '1px solid var(--color-border)',
            boxShadow: 'var(--shadow-md)',
            padding: '0.75rem',
            animation: 'riseIn 0.25s ease',
            maxWidth: 'var(--shell-max)',
            marginLeft: 'auto',
            marginRight: 'auto',
          }}
          className="mobile-nav-panel"
        >
          {[...visiblePrimary, ...visibleMore, { label: 'Profile', path: '/profile', icon: UserCircle }].map((item) => (
            <NavLink
              key={item.path}
              to={item.path}
              style={({ isActive }) => ({
                display: 'flex',
                alignItems: 'center',
                gap: '0.75rem',
                padding: '0.75rem 0.85rem',
                borderRadius: 'var(--radius-lg)',
                textDecoration: 'none',
                fontWeight: 600,
                fontSize: '0.9rem',
                color: isActive ? '#fff' : 'var(--text-secondary)',
                background: isActive ? 'var(--color-primary)' : 'transparent',
                marginBottom: '0.2rem',
              })}
            >
              <item.icon size={17} />
              {item.label}
            </NavLink>
          ))}
          <button
            type="button"
            onClick={handleLogout}
            className="btn btn-danger"
            style={{ width: '100%', marginTop: '0.5rem', justifyContent: 'flex-start' }}
          >
            <LogOut size={16} /> Logout
          </button>
        </div>
      )}

      <style>{`
        @media (max-width: 1024px) {
          #mobile-nav-toggle { display: inline-flex !important; }
        }
      `}</style>
    </header>
  );
};

export default Header;
