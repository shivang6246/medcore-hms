import React from 'react';

const Spinner = ({ size = 20, color = 'var(--color-primary)' }) => (
  <div
    style={{
      width: size,
      height: size,
      border: `2px solid rgba(255,255,255,0.15)`,
      borderTopColor: color,
      borderRadius: '50%',
      animation: 'spin 0.7s linear infinite',
      flexShrink: 0,
    }}
  />
);

export default Spinner;
