import React, { useState, useEffect, useCallback } from 'react';
import { CreditCard, Plus, RefreshCw, DollarSign, Trash2 } from 'lucide-react';
import { billingApi } from '../api/billing.api';
import toast from 'react-hot-toast';
import Modal from '../components/ui/Modal';
import { PatientIdLookup } from '../components/lookup/EntityLookups';
import useAuthStore from '../store/authStore';

const ITEM_CATEGORIES = ['CONSULTATION', 'LAB_TEST', 'PHARMACY', 'ADMISSION', 'OTHER'];

/* ── Create Invoice Modal ──────────────────────────────────────────────── */
const CreateInvoiceModal = ({ isOpen, onClose, onSuccess }) => {
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    patientId: '', issueDate: new Date().toISOString().slice(0, 10),
    dueDate: '', taxAmount: '0', discountAmount: '0',
  });
  const [items, setItems] = useState([{ description: '', category: 'CONSULTATION', unitPrice: '', quantity: '1' }]);

  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));
  const setItem = (idx, k, v) => setItems((prev) => prev.map((item, i) => (i === idx ? { ...item, [k]: v } : item)));
  const addItem = () => setItems((prev) => [...prev, { description: '', category: 'CONSULTATION', unitPrice: '', quantity: '1' }]);
  const removeItem = (idx) => setItems((prev) => prev.filter((_, i) => i !== idx));

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.patientId) {
      toast.error('Look up a patient first');
      return;
    }
    setLoading(true);
    try {
      await billingApi.createInvoice({
        patientId: form.patientId,
        issueDate: form.issueDate,
        dueDate: form.dueDate || undefined,
        taxAmount: parseFloat(form.taxAmount) || 0,
        discountAmount: parseFloat(form.discountAmount) || 0,
        items: items.map((it) => ({
          description: it.description,
          category: it.category,
          unitPrice: parseFloat(it.unitPrice),
          quantity: parseInt(it.quantity, 10),
        })),
      });
      toast.success('Invoice created successfully!');
      onSuccess();
      onClose();
      setForm({ patientId: '', issueDate: new Date().toISOString().slice(0, 10), dueDate: '', taxAmount: '0', discountAmount: '0' });
      setItems([{ description: '', category: 'CONSULTATION', unitPrice: '', quantity: '1' }]);
    } catch (err) {
      toast.error(err.response?.data?.message ?? err.response?.data?.detail ?? 'Failed to create invoice.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Create Invoice" maxWidth="720px">
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <PatientIdLookup
          onResolved={(p) => set('patientId', p.id)}
          onCleared={() => set('patientId', '')}
        />
        <div className="form-grid">
          <div className="form-group">
            <label className="form-label">Issue Date *</label>
            <input required type="date" value={form.issueDate} onChange={(e) => set('issueDate', e.target.value)} />
          </div>
          <div className="form-group">
            <label className="form-label">Due Date</label>
            <input type="date" value={form.dueDate} onChange={(e) => set('dueDate', e.target.value)} />
          </div>
          <div className="form-group">
            <label className="form-label">Tax Amount</label>
            <input type="number" step="0.01" min="0" value={form.taxAmount} onChange={(e) => set('taxAmount', e.target.value)} />
          </div>
          <div className="form-group">
            <label className="form-label">Discount Amount</label>
            <input type="number" step="0.01" min="0" value={form.discountAmount} onChange={(e) => set('discountAmount', e.target.value)} />
          </div>
        </div>

        <div style={{ marginTop: '0.5rem' }}>
          <label className="form-label" style={{ fontWeight: 600, marginBottom: '0.75rem', display: 'block' }}>Line Items *</label>
          {items.map((item, idx) => (
            <div key={idx} style={{ display: 'flex', gap: '0.5rem', alignItems: 'flex-end', marginBottom: '0.75rem' }}>
              <div className="form-group" style={{ flex: 3 }}>
                {idx === 0 && <label className="form-label">Description</label>}
                <input required value={item.description} onChange={(e) => setItem(idx, 'description', e.target.value)} placeholder="Consultation Fee" />
              </div>
              <div className="form-group" style={{ flex: 2 }}>
                {idx === 0 && <label className="form-label">Category</label>}
                <select value={item.category} onChange={(e) => setItem(idx, 'category', e.target.value)}>
                  {ITEM_CATEGORIES.map((c) => <option key={c} value={c}>{c.replace('_', ' ')}</option>)}
                </select>
              </div>
              <div className="form-group" style={{ flex: 1 }}>
                {idx === 0 && <label className="form-label">Price</label>}
                <input required type="number" step="0.01" min="0" value={item.unitPrice} onChange={(e) => setItem(idx, 'unitPrice', e.target.value)} placeholder="150" />
              </div>
              <div className="form-group" style={{ flex: 1 }}>
                {idx === 0 && <label className="form-label">Qty</label>}
                <input required type="number" min="1" value={item.quantity} onChange={(e) => setItem(idx, 'quantity', e.target.value)} />
              </div>
              {items.length > 1 && (
                <button type="button" className="btn btn-danger btn-sm" onClick={() => removeItem(idx)} style={{ marginBottom: '0.15rem' }}>
                  <Trash2 size={14} />
                </button>
              )}
            </div>
          ))}
          <button type="button" className="btn btn-secondary btn-sm" onClick={addItem}>+ Add Line Item</button>
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
          <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Creating…' : 'Create Invoice'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

/* ── Status color helper ───────────────────────────────────────────────── */
const statusStyle = (status) => {
  switch (status) {
    case 'PAID': return { background: 'rgba(16, 185, 129, 0.1)', color: '#10b981' };
    case 'PARTIALLY_PAID': return { background: 'rgba(245, 158, 11, 0.1)', color: '#f59e0b' };
    case 'REFUNDED': return { background: 'rgba(139, 92, 246, 0.1)', color: '#8b5cf6' };
    default: return { background: 'rgba(239, 68, 68, 0.1)', color: '#ef4444' };
  }
};

/* ── Main Billing Page ─────────────────────────────────────────────────── */
export default function Billing() {
  const { user, hasAnyRole } = useAuthStore();
  const isPatient = hasAnyRole(['PATIENT']);
  const canCreate = hasAnyRole(['SUPER_ADMIN', 'HOSPITAL_ADMIN', 'ACCOUNTANT', 'RECEPTIONIST']);

  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);

  const fetchInvoices = useCallback(async () => {
    setLoading(true);
    try {
      const res = isPatient && user?.patientId
        ? await billingApi.getPatientInvoices(user.patientId, { page: 0, size: 50 })
        : await billingApi.getAllInvoices({ page: 0, size: 50 });
      const payload = res.data;
      const list = payload?.data?.content ?? payload?.content ?? (Array.isArray(payload?.data) ? payload.data : (Array.isArray(payload) ? payload : []));
      setInvoices(list);
    } catch {
      setInvoices([]);
    } finally {
      setLoading(false);
    }
  }, [isPatient, user?.patientId]);

  useEffect(() => { fetchInvoices(); }, [fetchInvoices]);

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '700', color: 'var(--text-primary)' }}>Billing & Payments</h1>
          <p style={{ color: 'var(--text-secondary)' }}>
            {isPatient
              ? 'View your invoices and payment status'
              : 'Generate patient invoices, collect payments, manage tax & revenue reports'}
          </p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button className="btn btn-secondary" onClick={fetchInvoices} title="Refresh">
            <RefreshCw size={18} />
          </button>
          {canCreate && (
            <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
              <Plus size={18} style={{ marginRight: '0.5rem' }} /> Create Invoice
            </button>
          )}
        </div>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '4rem', color: 'var(--text-muted)' }}>
          <div className="skeleton" style={{ width: '48px', height: '48px', borderRadius: '50%', margin: '0 auto 1rem' }} />
          <p>Loading invoices…</p>
        </div>
      ) : invoices.length === 0 ? (
        <div className="empty-state">
          <CreditCard size={48} />
          <h3>No invoices found</h3>
          <p>{isPatient ? 'Invoices for your visits will appear here.' : 'Create your first invoice to get started.'}</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(380px, 1fr))', gap: '1.5rem' }}>
          {invoices.map((inv) => (
            <div key={inv.id} className="card" style={{ padding: '1.5rem', borderRadius: '12px', border: '1px solid var(--color-border)', background: 'var(--color-bg-elevated)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  <div style={{ padding: '0.5rem', borderRadius: '8px', background: 'rgba(16, 185, 129, 0.1)', color: '#10b981' }}>
                    <CreditCard size={24} />
                  </div>
                  <div>
                    <h3 style={{ fontSize: '1.1rem', fontWeight: '600' }}>{inv.invoiceNumber ?? `INV-${inv.id?.slice(0, 8)}`}</h3>
                    <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{inv.issueDate}</span>
                  </div>
                </div>
                <span className="badge" style={{ ...statusStyle(inv.status), padding: '0.25rem 0.6rem', borderRadius: '6px', fontSize: '0.8rem' }}>
                  {inv.status}
                </span>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.9rem' }}>
                {!isPatient && <div><strong>Patient:</strong> {inv.patientName ?? inv.patientId ?? '—'}</div>}
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem', marginTop: '0.5rem', background: 'var(--color-bg-subtle, rgba(255,255,255,0.03))', padding: '0.75rem', borderRadius: '8px' }}>
                  <div>Subtotal: ${inv.subtotal ?? '—'}</div>
                  <div>Tax: ${inv.taxAmount ?? inv.tax ?? '—'}</div>
                  <div>Discount: ${inv.discountAmount ?? inv.discount ?? '—'}</div>
                  <div><strong>Total: ${inv.grandTotal ?? inv.totalAmount ?? '—'}</strong></div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {canCreate && (
        <CreateInvoiceModal isOpen={showCreate} onClose={() => setShowCreate(false)} onSuccess={fetchInvoices} />
      )}
    </div>
  );
}
