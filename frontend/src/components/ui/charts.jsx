import React from 'react';

/** Compact vertical-dot sparkline used on summary cards */
export const DotSpark = ({ values = [3, 5, 4, 7, 6, 8, 5], highlightIndex = -1, onDark = false, maxDots = 8 }) => {
  const max = Math.max(...values, 1);

  return (
    <div className={`dot-spark ${onDark ? 'dot-spark--on-dark' : ''}`}>
      {values.map((v, i) => {
        const filled = Math.max(1, Math.round((v / max) * maxDots));
        const isHighlight = i === highlightIndex;
        return (
          <div
            key={i}
            className="dot-spark-col"
            style={{ animationDelay: `${i * 0.04}s` }}
          >
            {Array.from({ length: maxDots }).map((_, d) => {
              const on = d < filled;
              return (
                <span
                  key={d}
                  className={`dot ${on ? (isHighlight ? 'lime' : 'on') : ''}`}
                />
              );
            })}
          </div>
        );
      })}
    </div>
  );
};

/** Full-width monthly revenue-style dot columns */
export const DotMatrixChart = ({
  data = [],
  valueKey = 'value',
  labelKey = 'label',
  highlightIndex = null,
  highlightValue = null,
}) => {
  const max = Math.max(...data.map((d) => d[valueKey] || 0), 1);
  const rows = 12;

  return (
    <div style={{ position: 'relative', paddingTop: '0.5rem' }}>
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: `repeat(${data.length}, 1fr)`,
          gap: '0.5rem',
          alignItems: 'end',
          height: 220,
          padding: '0 0.25rem',
        }}
      >
        {data.map((item, i) => {
          const val = item[valueKey] || 0;
          const filled = Math.max(1, Math.round((val / max) * rows));
          const active = highlightIndex === i;
          return (
            <div
              key={item[labelKey] ?? i}
              style={{
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                gap: '0.55rem',
                height: '100%',
                justifyContent: 'flex-end',
                position: 'relative',
              }}
            >
              {active && highlightValue != null && (
                <div
                  style={{
                    position: 'absolute',
                    top: 0,
                    left: '50%',
                    transform: 'translateX(-50%)',
                    background: 'var(--color-primary)',
                    color: '#fff',
                    fontSize: '0.75rem',
                    fontWeight: 700,
                    padding: '0.35rem 0.65rem',
                    borderRadius: 'var(--radius-full)',
                    whiteSpace: 'nowrap',
                    boxShadow: 'var(--shadow-glow)',
                    zIndex: 2,
                  }}
                >
                  {highlightValue}
                </div>
              )}
              <div
                style={{
                  display: 'flex',
                  flexDirection: 'column-reverse',
                  gap: 4,
                  alignItems: 'center',
                  flex: 1,
                  justifyContent: 'flex-start',
                  paddingTop: active ? '2rem' : 0,
                }}
              >
                {Array.from({ length: rows }).map((_, d) => {
                  const on = d < filled;
                  return (
                    <span
                      key={d}
                      style={{
                        width: active ? 9 : 7,
                        height: active ? 9 : 7,
                        borderRadius: '50%',
                        background: on
                          ? active
                            ? 'var(--color-primary)'
                            : 'rgba(10, 77, 74, 0.22)'
                          : 'rgba(10, 77, 74, 0.07)',
                        boxShadow: active && d === filled - 1 ? '0 0 0 4px rgba(10,77,74,0.15)' : 'none',
                        transition: 'all 0.25s ease',
                      }}
                    />
                  );
                })}
              </div>
              <span
                style={{
                  fontSize: '0.72rem',
                  fontWeight: active ? 700 : 500,
                  color: active ? 'var(--color-primary)' : 'var(--text-muted)',
                }}
              >
                {item[labelKey]}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export const GenderDonut = ({ female = 0, male = 0, other = 0, size = 140 }) => {
  const total = female + male + other;
  const pct = (n) => (total > 0 ? Math.round((n / total) * 100) : 0);
  const femalePct = pct(female);
  const malePct = pct(male);
  const otherPct = pct(other);

  const r = 52;
  const c = 2 * Math.PI * r;
  const f = (femalePct / 100) * c;
  const m = (malePct / 100) * c;
  const ch = (otherPct / 100) * c;

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: '1.25rem', flexWrap: 'wrap' }}>
      <svg width={size} height={size} viewBox="0 0 140 140">
        <circle cx="70" cy="70" r={r} fill="none" stroke="#e8eef2" strokeWidth="18" />
        {total > 0 && (
          <>
            <circle
              cx="70"
              cy="70"
              r={r}
              fill="none"
              stroke="#c8ed45"
              strokeWidth="18"
              strokeDasharray={`${f} ${c - f}`}
              strokeLinecap="butt"
              transform="rotate(-90 70 70)"
            />
            <circle
              cx="70"
              cy="70"
              r={r}
              fill="none"
              stroke="#0a4d4a"
              strokeWidth="18"
              strokeDasharray={`${m} ${c - m}`}
              strokeDashoffset={-f}
              transform="rotate(-90 70 70)"
            />
            <circle
              cx="70"
              cy="70"
              r={r}
              fill="none"
              stroke="#c5d4d8"
              strokeWidth="18"
              strokeDasharray={`${ch} ${c - ch}`}
              strokeDashoffset={-(f + m)}
              transform="rotate(-90 70 70)"
            />
          </>
        )}
        <text x="70" y="66" textAnchor="middle" fontSize="11" fill="#8a9b9e" fontFamily="Plus Jakarta Sans, sans-serif">
          Total
        </text>
        <text x="70" y="86" textAnchor="middle" fontSize="18" fontWeight="800" fill="#102828" fontFamily="Sora, sans-serif">
          {total.toLocaleString()}
        </text>
      </svg>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.7rem' }}>
        {[
          { label: 'Female', value: female, pct: femalePct, color: '#c8ed45' },
          { label: 'Male', value: male, pct: malePct, color: '#0a4d4a' },
          { label: 'Other', value: other, pct: otherPct, color: '#c5d4d8' },
        ].map((item) => (
          <div key={item.label} style={{ display: 'flex', alignItems: 'center', gap: '0.55rem' }}>
            <span style={{ width: 10, height: 10, borderRadius: '50%', background: item.color }} />
            <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', minWidth: 70 }}>{item.label}</span>
            <strong style={{ fontSize: '0.85rem', color: 'var(--text-primary)' }}>
              {item.value.toLocaleString()} ({item.pct}%)
            </strong>
          </div>
        ))}
      </div>
    </div>
  );
};

export default DotSpark;
