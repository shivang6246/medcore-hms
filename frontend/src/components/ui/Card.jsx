import React from 'react';

const Card = ({ children, className = '', padding = true, hover = false, style = {} }) => (
  <div
    className={`glass-card ${padding ? 'p-6' : ''} ${hover ? 'hover-card' : ''} ${className}`}
    style={style}
  >
    {children}
  </div>
);

export default Card;
