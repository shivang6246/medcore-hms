import React from 'react';
import { useLocation, Link } from 'react-router-dom';
import { Bell, ChevronRight } from 'lucide-react';
import useAuthStore from '../../store/authStore';

const BREADCRUMB_MAP = {
  '/dashboard':    ['Dashboard'],
  '/hospitals':    ['Hospitals'],
  '/departments':  ['Departments'],
  '/doctors':      ['Doctors'],
  '/patients':     ['Patients'],
  '/appointments': ['Appointments'],
  '/profile':      ['Profile'],
};

const Header = () => {
  const { user } = useAuthStore();
  const location = useLocation();

  const crumbs = BREADCRUMB_MAP[location.pathname] ?? [location.pathname.slice(1)];
  const initials = user
    ? `${user.firstName?.[0] ?? ''}${user.lastName?.[0] ?? ''}`.toUpperCase()
    : '?';

  return (
    <header
      style={{
        position: 'fixed',
        top: 0,
        right: 0,
        left: 'var(--sidebar-width)',
        height: 'var(--header-height)',
        background: 'rgba(8, 12, 20, 0.85)',
        backdropFilter: 'blur(16px)',
        borderBottom: '1px solid var(--color-border)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0 2rem',
        zIndex: 50,
        transition: 'left var(--transition-base)',
      }}
    >
      {/* Breadcrumb */}
      <nav style={{ display: 'flex', alignItems: 'center', gap: '0.375rem' }}>
        <Link to="/dashboard" style={{ color: 'var(--text-muted)', fontSize: 'var(--font-size-sm)', textDecoration: 'none' }}>
          Home
        </Link>
        {crumbs.map((crumb, i) => (
          <React.Fragment key={crumb}>
            <ChevronRight size={14} color="var(--text-muted)" />
            <span
              style={{
                fontSize: 'var(--font-size-sm)',
                fontWeight: i === crumbs.length - 1 ? 600 : 400,
                color: i === crumbs.length - 1 ? 'var(--text-primary)' : 'var(--text-muted)',
                textTransform: 'capitalize',
              }}
            >
              {crumb}
            </span>
          </React.Fragment>
        ))}
      </nav>

      {/* Right section */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        {/* Notification bell */}
        <button
          className="btn btn-secondary btn-icon"
          style={{ position: 'relative' }}
          title="Notifications"
        >
          <Bell size={18} />
          <span
            style={{
              position: 'absolute',
              top: 4,
              right: 4,
              width: 8,
              height: 8,
              borderRadius: '50%',
              background: 'var(--color-danger)',
              border: '1.5px solid var(--color-bg-base)',
            }}
          />
        </button>

        {/* User avatar */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
          <div className="avatar avatar-sm">{initials}</div>
          {user && (
            <div className="hide-sm">
              <div style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600, color: 'var(--text-primary)' }}>
                {user.firstName} {user.lastName}
              </div>
              <div style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}>
                {user.roles?.[0]?.replace(/_/g, ' ') ?? ''}
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default Header;
