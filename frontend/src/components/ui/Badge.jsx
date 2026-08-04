import React from 'react';

const Badge = ({ children, variant = 'primary', dot = false }) => {
  return (
    <span className={`badge badge-${variant}`}>
      {dot && (
        <span
          style={{
            width: 6,
            height: 6,
            borderRadius: '50%',
            background: 'currentColor',
            flexShrink: 0,
            animation: 'pulse-dot 2s infinite',
          }}
        />
      )}
      {children}
    </span>
  );
};

export const StatusBadge = ({ isActive }) => (
  <Badge variant={isActive ? 'success' : 'danger'} dot>
    {isActive ? 'Active' : 'Inactive'}
  </Badge>
);

export default Badge;
