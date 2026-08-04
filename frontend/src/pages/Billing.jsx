import React, { useState } from 'react';
import { CreditCard, Plus, DollarSign, CheckCircle2, AlertCircle } from 'lucide-react';
import toast from 'react-hot-toast';

export default function Billing() {
  const [invoices] = useState([
    {
      id: '1',
      invoiceNumber: 'INV-2026-0001',
      patientName: 'Bruce Wayne',
      subtotal: '$450.00',
      tax: '$45.00',
      discount: '$20.00',
      grandTotal: '$475.00',
      paidAmount: '$475.00',
      status: 'PAID',
      issueDate: '2026-08-04'
    }
  ]);

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '700', color: 'var(--text-primary)' }}>Billing & Payments</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Generate patient invoices, collect payments, manage tax & revenue reports</p>
        </div>
        <button className="btn btn-primary" onClick={() => toast.success('New Invoice Modal')}>
          <Plus size={18} style={{ marginRight: '0.5rem' }} /> Create Invoice
        </button>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(380px, 1fr))', gap: '1.5rem' }}>
        {invoices.map((inv) => (
          <div key={inv.id} className="card" style={{ padding: '1.5rem', borderRadius: '12px', border: '1px solid var(--color-border)', background: 'var(--color-bg-elevated)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <div style={{ padding: '0.5rem', borderRadius: '8px', background: 'rgba(16, 185, 129, 0.1)', color: '#10b981' }}>
                  <CreditCard size={24} />
                </div>
                <div>
                  <h3 style={{ fontSize: '1.1rem', fontWeight: '600' }}>{inv.invoiceNumber}</h3>
                  <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{inv.issueDate}</span>
                </div>
              </div>
              <span className="badge" style={{ background: 'rgba(16, 185, 129, 0.1)', color: '#10b981', padding: '0.25rem 0.6rem', borderRadius: '6px', fontSize: '0.8rem' }}>{inv.status}</span>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.9rem' }}>
              <div><strong>Patient:</strong> {inv.patientName}</div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem', marginTop: '0.5rem', background: 'var(--color-bg-subtle)', padding: '0.75rem', borderRadius: '8px' }}>
                <div>Subtotal: {inv.subtotal}</div>
                <div>Tax: {inv.tax}</div>
                <div>Discount: {inv.discount}</div>
                <div><strong>Total: {inv.grandTotal}</strong></div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
