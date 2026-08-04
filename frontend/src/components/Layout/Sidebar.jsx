import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  Building2,
  Users,
  Stethoscope,
  CalendarDays,
  FlaskConical,
  ShieldCheck,
  LogOut,
  ChevronLeft,
  Heart,
  UserCircle,
  Layers,
  Menu,
} from 'lucide-react';
import useAuthStore from '../../store/authStore';
import { authApi } from '../../api/auth.api';
import toast from 'react-hot-toast';

const NAV_ITEMS = [
  { label: 'Dashboard',    path: '/dashboard',    icon: LayoutDashboard, roles: null },
  { label: 'Hospitals',    path: '/hospitals',    icon: Building2,       roles: ['SUPER_ADMIN', 'HOSPITAL_ADMIN'] },
  { label: 'Departments',  path: '/departments',  icon: Layers,          roles: ['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DEPARTMENT_HEAD'] },
  { label: 'Doctors',      path: '/doctors',      icon: Stethoscope,     roles: ['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR'] },
  { label: 'Patients',     path: '/patients',     icon: Users,           roles: ['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE'] },
  { label: 'Appointments', path: '/appointments', icon: CalendarDays,    roles: ['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'DOCTOR', 'NURSE', 'RECEPTIONIST'] },
  { label: 'Profile',      path: '/profile',      icon: UserCircle,      roles: null },
];

const Sidebar = () => {
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);
  const { user, refreshToken, clearAuth, hasAnyRole } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      if (refreshToken) await authApi.logout(refreshToken);
    } catch {}
    clearAuth();
    navigate('/login');
    toast.success('Logged out successfully');
  };

  const visibleItems = NAV_ITEMS.filter(
    (item) => !item.roles || hasAnyRole(item.roles)
  );

  const initials = user
    ? `${user.firstName?.[0] ?? ''}${user.lastName?.[0] ?? ''}`.toUpperCase()
    : '?';

  const sidebarWidth = collapsed ? 'var(--sidebar-collapsed)' : 'var(--sidebar-width)';

  return (
    <>
      {/* Mobile hamburger */}
      <button
        onClick={() => setMobileOpen(!mobileOpen)}
        style={{
          position: 'fixed',
          top: '1rem',
          left: '1rem',
          zIndex: 200,
          display: 'none',
        }}
        className="btn btn-secondary btn-icon hide-md-show"
        id="mobile-menu-btn"
      />

      {/* Mobile overlay */}
      {mobileOpen && (
        <div
          onClick={() => setMobileOpen(false)}
          style={{
            position: 'fixed',
            inset: 0,
            background: 'rgba(0,0,0,0.6)',
            backdropFilter: 'blur(4px)',
            zIndex: 99,
            display: 'none',
          }}
        />
      )}

      {/* Sidebar */}
      <aside
        style={{
          width: sidebarWidth,
          minHeight: '100vh',
          position: 'fixed',
          top: 0,
          left: 0,
          background: 'var(--gradient-sidebar)',
          borderRight: '1px solid var(--color-border)',
          display: 'flex',
          flexDirection: 'column',
          transition: 'width var(--transition-base)',
          overflow: 'hidden',
          zIndex: 100,
        }}
      >
        {/* Logo */}
        <div
          style={{
            padding: collapsed ? '1.25rem 0' : '1.5rem 1.25rem',
            display: 'flex',
            alignItems: 'center',
            justifyContent: collapsed ? 'center' : 'space-between',
            borderBottom: '1px solid var(--color-border)',
            minHeight: 'var(--header-height)',
            gap: '0.75rem',
          }}
        >
          {!collapsed && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.625rem' }}>
              <div
                style={{
                  width: 36,
                  height: 36,
                  borderRadius: '0.625rem',
                  background: 'var(--gradient-primary)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                  boxShadow: 'var(--shadow-glow)',
                }}
              >
                <Heart size={18} color="#fff" fill="#fff" />
              </div>
              <div>
                <div style={{ fontWeight: 800, fontSize: '1rem', color: 'var(--text-primary)', lineHeight: 1.1 }}>
                  MedCore
                </div>
                <div style={{ fontSize: '0.65rem', color: 'var(--text-muted)', fontWeight: 500, letterSpacing: '0.08em', textTransform: 'uppercase' }}>
                  HMS
                </div>
              </div>
            </div>
          )}
          {collapsed && (
            <div
              style={{
                width: 36,
                height: 36,
                borderRadius: '0.625rem',
                background: 'var(--gradient-primary)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                boxShadow: 'var(--shadow-glow)',
              }}
            >
              <Heart size={18} color="#fff" fill="#fff" />
            </div>
          )}
          {!collapsed && (
            <button
              onClick={() => setCollapsed(true)}
              className="btn btn-secondary btn-icon"
              style={{ padding: '0.35rem', flexShrink: 0 }}
              title="Collapse sidebar"
            >
              <ChevronLeft size={16} />
            </button>
          )}
        </div>

        {/* Nav */}
        <nav style={{ flex: 1, padding: '1rem 0.625rem', overflowY: 'auto', overflowX: 'hidden' }}>
          {collapsed && (
            <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '0.75rem' }}>
              <button
                onClick={() => setCollapsed(false)}
                className="btn btn-secondary btn-icon"
                style={{ padding: '0.5rem' }}
                title="Expand sidebar"
              >
                <Menu size={16} />
              </button>
            </div>
          )}

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
            {visibleItems.map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                title={collapsed ? item.label : undefined}
                style={({ isActive }) => ({
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.75rem',
                  padding: collapsed ? '0.75rem' : '0.7rem 0.875rem',
                  borderRadius: 'var(--radius-md)',
                  textDecoration: 'none',
                  fontWeight: 500,
                  fontSize: 'var(--font-size-sm)',
                  transition: 'all var(--transition-fast)',
                  justifyContent: collapsed ? 'center' : 'flex-start',
                  background: isActive ? 'rgba(59,130,246,0.12)' : 'transparent',
                  color: isActive ? 'var(--color-primary-light)' : 'var(--text-secondary)',
                  borderLeft: isActive ? '2px solid var(--color-primary)' : '2px solid transparent',
                })}
                onMouseEnter={(e) => {
                  const link = e.currentTarget;
                  if (!link.getAttribute('aria-current')) {
                    link.style.background = 'rgba(255,255,255,0.04)';
                    link.style.color = 'var(--text-primary)';
                  }
                }}
                onMouseLeave={(e) => {
                  const link = e.currentTarget;
                  if (!link.getAttribute('aria-current')) {
                    link.style.background = '';
                    link.style.color = '';
                  }
                }}
              >
                <item.icon size={18} style={{ flexShrink: 0 }} />
                {!collapsed && <span>{item.label}</span>}
              </NavLink>
            ))}
          </div>
        </nav>

        {/* User section */}
        <div
          style={{
            padding: '0.875rem 0.75rem',
            borderTop: '1px solid var(--color-border)',
            display: 'flex',
            flexDirection: 'column',
            gap: '0.5rem',
          }}
        >
          {!collapsed && user && (
            <div
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '0.625rem',
                padding: '0.5rem 0.375rem',
                borderRadius: 'var(--radius-md)',
                background: 'rgba(255,255,255,0.03)',
              }}
            >
              <div className="avatar avatar-sm">{initials}</div>
              <div style={{ minWidth: 0 }}>
                <div
                  className="truncate"
                  style={{ fontSize: 'var(--font-size-sm)', fontWeight: 600, color: 'var(--text-primary)' }}
                >
                  {user.firstName} {user.lastName}
                </div>
                <div
                  className="truncate"
                  style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)' }}
                >
                  {user.roles?.[0] ?? user.email}
                </div>
              </div>
            </div>
          )}
          <button
            onClick={handleLogout}
            className="btn btn-danger btn-sm"
            style={{
              width: '100%',
              justifyContent: collapsed ? 'center' : 'flex-start',
            }}
            title="Logout"
          >
            <LogOut size={15} />
            {!collapsed && 'Logout'}
          </button>
        </div>
      </aside>
    </>
  );
};

export default Sidebar;
