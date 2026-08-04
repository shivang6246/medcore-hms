import React from 'react';

const StatCard = ({ title, value, icon: Icon, color = '#3b82f6', change, subtitle }) => {
  const bgGlow = `${color}18`;
  const borderGlow = `${color}30`;

  return (
    <div
      className="glass-card"
      style={{
        padding: '1.5rem',
        background: `linear-gradient(135deg, ${bgGlow} 0%, rgba(13,18,32,0.6) 100%)`,
        border: `1px solid ${borderGlow}`,
        transition: 'all 0.25s ease',
        cursor: 'default',
      }}
      onMouseEnter={(e) => {
        e.currentTarget.style.transform = 'translateY(-2px)';
        e.currentTarget.style.boxShadow = `0 8px 32px ${color}20`;
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.transform = 'translateY(0)';
        e.currentTarget.style.boxShadow = '';
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <p style={{ fontSize: 'var(--font-size-xs)', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.06em', marginBottom: '0.5rem' }}>
            {title}
          </p>
          <h3 style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--text-primary)', lineHeight: 1 }}>
            {value ?? '—'}
          </h3>
          {subtitle && (
            <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--text-muted)', marginTop: '0.4rem' }}>
              {subtitle}
            </p>
          )}
          {change !== undefined && (
            <p style={{ fontSize: 'var(--font-size-xs)', marginTop: '0.4rem', color: change >= 0 ? 'var(--color-success)' : 'var(--color-danger)' }}>
              {change >= 0 ? '▲' : '▼'} {Math.abs(change)}% from last month
            </p>
          )}
        </div>
        {Icon && (
          <div
            style={{
              width: 48,
              height: 48,
              borderRadius: '0.875rem',
              background: `${color}20`,
              border: `1px solid ${color}30`,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color,
              flexShrink: 0,
            }}
          >
            <Icon size={22} />
          </div>
        )}
      </div>
    </div>
  );
};

export default StatCard;
