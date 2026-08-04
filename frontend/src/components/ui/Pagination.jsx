import React from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';

const Pagination = ({ page, totalPages, totalElements, size, onPageChange }) => {
  if (totalPages <= 1) return null;

  const from = page * size + 1;
  const to = Math.min((page + 1) * size, totalElements);

  const getPageNumbers = () => {
    const pages = [];
    const delta = 1;
    const left = page - delta;
    const right = page + delta + 1;

    for (let i = 0; i < totalPages; i++) {
      if (i === 0 || i === totalPages - 1 || (i >= left && i < right)) {
        pages.push(i);
      }
    }

    const result = [];
    let prev;
    for (const p of pages) {
      if (prev !== undefined && p - prev > 1) result.push('...');
      result.push(p);
      prev = p;
    }
    return result;
  };

  return (
    <div
      style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '1rem 1rem 0.5rem',
        flexWrap: 'wrap',
        gap: '0.75rem',
      }}
    >
      <span style={{ fontSize: 'var(--font-size-sm)', color: 'var(--text-muted)' }}>
        Showing <strong style={{ color: 'var(--text-secondary)' }}>{from}–{to}</strong> of{' '}
        <strong style={{ color: 'var(--text-secondary)' }}>{totalElements}</strong> results
      </span>

      <div style={{ display: 'flex', gap: '0.375rem', alignItems: 'center' }}>
        <button
          className="btn btn-secondary btn-sm"
          onClick={() => onPageChange(page - 1)}
          disabled={page === 0}
          style={{ padding: '0.4rem 0.6rem' }}
        >
          <ChevronLeft size={14} />
        </button>

        {getPageNumbers().map((p, i) =>
          p === '...' ? (
            <span key={`dots-${i}`} style={{ color: 'var(--text-muted)', padding: '0 0.25rem' }}>
              …
            </span>
          ) : (
            <button
              key={p}
              onClick={() => onPageChange(p)}
              className={`btn btn-sm ${p === page ? 'btn-primary' : 'btn-secondary'}`}
              style={{ minWidth: '2rem', padding: '0.4rem' }}
            >
              {p + 1}
            </button>
          )
        )}

        <button
          className="btn btn-secondary btn-sm"
          onClick={() => onPageChange(page + 1)}
          disabled={page >= totalPages - 1}
          style={{ padding: '0.4rem 0.6rem' }}
        >
          <ChevronRight size={14} />
        </button>
      </div>
    </div>
  );
};

export default Pagination;
