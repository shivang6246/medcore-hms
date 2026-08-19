import React from 'react';
import { DotSpark } from './charts';

const StatCard = ({
  title,
  value,
  change,
  subtitle,
  variant = 'light',
  sparkValues,
  sparkHighlight = -1,
  bedStats,
  icon: Icon,
  color,
}) => {
  const isDark = variant === 'dark';

  return (
    <div
      className="glass-card animate-rise"
      style={{
        padding: '1.35rem 1.4rem',
        background: isDark ? 'var(--gradient-hero-dark)' : '#fff',
        border: isDark ? 'none' : '1px solid var(--color-border)',
        color: isDark ? '#fff' : 'var(--text-primary)',
        minHeight: 170,
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'space-between',
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: '0.75rem' }}>
        <div>
          <p
            style={{
              fontSize: '0.8rem',
              fontWeight: 600,
              color: isDark ? 'rgba(255,255,255,0.7)' : 'var(--text-muted)',
              marginBottom: '0.45rem',
            }}
          >
            {title}
          </p>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: '0.55rem', flexWrap: 'wrap' }}>
            <h3
              style={{
                fontFamily: 'var(--font-display)',
                fontSize: '2rem',
                fontWeight: 800,
                color: isDark ? '#fff' : 'var(--text-primary)',
                lineHeight: 1,
                letterSpacing: '-0.03em',
              }}
            >
              {value ?? '—'}
            </h3>
            {change != null && (
              <span
                style={{
                  fontSize: '0.78rem',
                  fontWeight: 700,
                  color: isDark ? 'var(--color-secondary)' : 'var(--color-success)',
                }}
              >
                {change >= 0 ? '+' : ''}
                {change}%
              </span>
            )}
          </div>
          {subtitle && (
            <p
              style={{
                fontSize: '0.75rem',
                marginTop: '0.4rem',
                color: isDark ? 'rgba(255,255,255,0.55)' : 'var(--text-muted)',
              }}
            >
              {subtitle}
            </p>
          )}
        </div>
        {Icon && (
          <div
            style={{
              width: 40,
              height: 40,
              borderRadius: '12px',
              background: isDark ? 'rgba(255,255,255,0.1)' : `${color || 'var(--color-primary)'}18`,
              display: 'grid',
              placeItems: 'center',
              color: isDark ? 'var(--color-secondary)' : color || 'var(--color-primary)',
            }}
          >
            <Icon size={18} />
          </div>
        )}
      </div>

      {sparkValues && (
        <DotSpark values={sparkValues} highlightIndex={sparkHighlight} onDark={isDark} />
      )}

      {bedStats && (
        <div>
          <div className="bed-track">
            <div className="bed-track-booked" style={{ width: `${bedStats.bookedPct}%` }} />
            <div className="bed-track-free" style={{ width: `${bedStats.availablePct}%` }} />
          </div>
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              marginTop: '0.55rem',
              fontSize: '0.72rem',
              fontWeight: 600,
              color: isDark ? 'rgba(255,255,255,0.7)' : 'var(--text-muted)',
            }}
          >
            <span>Booked {bedStats.bookedPct}%</span>
            <span>Available {bedStats.availablePct}%</span>
          </div>
        </div>
      )}
    </div>
  );
};

export default StatCard;
