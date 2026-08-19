import React, { useState, useEffect, useCallback } from 'react';
import { CreditCard, Plus, RefreshCw, DollarSign, Trash2, Eye, Download, Lock, CheckCircle, Send, FileText, QrCode, Smartphone } from 'lucide-react';
import { billingApi } from '../api/billing.api';
import toast from 'react-hot-toast';
import Modal from '../components/ui/Modal';
import { PatientIdLookup } from '../components/lookup/EntityLookups';
import useAuthStore from '../store/authStore';

const ITEM_CATEGORIES = ['CONSULTATION', 'LAB_TEST', 'PHARMACY', 'ADMISSION', 'OTHER'];

/* ── Create Invoice Modal with Percentage Discount & Real-time Calculation ── */
const CreateInvoiceModal = ({ isOpen, onClose, onSuccess }) => {
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    patientId: '',
    issueDate: new Date().toISOString().slice(0, 10),
    dueDate: '',
    taxAmount: '0',
    discountPercentage: '0',
  });
  const [items, setItems] = useState([
    { description: '', category: 'CONSULTATION', unitPrice: '', quantity: '1' }
  ]);

  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));
  const setItem = (idx, k, v) => setItems((prev) => prev.map((item, i) => (i === idx ? { ...item, [k]: v } : item)));
  const addItem = () => setItems((prev) => [...prev, { description: '', category: 'CONSULTATION', unitPrice: '', quantity: '1' }]);
  const removeItem = (idx) => setItems((prev) => prev.filter((_, i) => i !== idx));

  // Live calculation preview in INR ₹
  const subtotal = items.reduce((acc, it) => {
    const p = parseFloat(it.unitPrice) || 0;
    const q = parseInt(it.quantity, 10) || 0;
    return acc + (p * q);
  }, 0);

  const discountPct = Math.max(0, Math.min(100, parseFloat(form.discountPercentage) || 0));
  const discountAmount = subtotal * (discountPct / 100);
  const tax = Math.max(0, parseFloat(form.taxAmount) || 0);
  const netSubtotal = Math.max(0, subtotal - discountAmount);
  const grandTotal = netSubtotal + tax;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.patientId) {
      toast.error('Please select a valid patient');
      return;
    }
    if (items.some((it) => !it.description || !it.unitPrice || parseFloat(it.unitPrice) < 0)) {
      toast.error('Please fill in all line item descriptions and valid prices');
      return;
    }
    setLoading(true);
    try {
      await billingApi.createInvoice({
        patientId: form.patientId,
        issueDate: form.issueDate,
        dueDate: form.dueDate || undefined,
        taxAmount: tax,
        discountPercentage: discountPct,
        discountAmount: discountAmount,
        items: items.map((it) => ({
          description: it.description,
          category: it.category,
          unitPrice: parseFloat(it.unitPrice),
          quantity: parseInt(it.quantity, 10) || 1,
        })),
      });
      toast.success('Invoice generated successfully in INR (₹)!');
      onSuccess();
      onClose();
      setForm({ patientId: '', issueDate: new Date().toISOString().slice(0, 10), dueDate: '', taxAmount: '0', discountPercentage: '0' });
      setItems([{ description: '', category: 'CONSULTATION', unitPrice: '', quantity: '1' }]);
    } catch (err) {
      toast.error(err.response?.data?.message ?? err.response?.data?.detail ?? 'Failed to create invoice.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Super Admin - Generate Invoice (INR ₹)" maxWidth="750px">
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <PatientIdLookup
          onResolved={(p) => set('patientId', p.id)}
          onCleared={() => set('patientId', '')}
        />
        <div className="form-grid" style={{ gridTemplateColumns: '1fr 1fr 1fr 1fr' }}>
          <div className="form-group">
            <label className="form-label">Issue Date *</label>
            <input required type="date" value={form.issueDate} onChange={(e) => set('issueDate', e.target.value)} />
          </div>
          <div className="form-group">
            <label className="form-label">Due Date</label>
            <input type="date" value={form.dueDate} onChange={(e) => set('dueDate', e.target.value)} />
          </div>
          <div className="form-group">
            <label className="form-label">Tax Amount (₹)</label>
            <input type="number" step="0.01" min="0" value={form.taxAmount} onChange={(e) => set('taxAmount', e.target.value)} placeholder="0.00" />
          </div>
          <div className="form-group">
            <label className="form-label">Discount (%) *</label>
            <input type="number" step="0.1" min="0" max="100" value={form.discountPercentage} onChange={(e) => set('discountPercentage', e.target.value)} placeholder="10%" />
          </div>
        </div>

        <div style={{ marginTop: '0.5rem' }}>
          <label className="form-label" style={{ fontWeight: 600, marginBottom: '0.75rem', display: 'block' }}>Itemized Line Items *</label>
          {items.map((item, idx) => {
            const lineTotal = (parseFloat(item.unitPrice) || 0) * (parseInt(item.quantity, 10) || 0);
            return (
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
                  {idx === 0 && <label className="form-label">Price (₹)</label>}
                  <input required type="number" step="0.01" min="0" value={item.unitPrice} onChange={(e) => setItem(idx, 'unitPrice', e.target.value)} placeholder="500" />
                </div>
                <div className="form-group" style={{ flex: 1 }}>
                  {idx === 0 && <label className="form-label">Qty</label>}
                  <input required type="number" min="1" value={item.quantity} onChange={(e) => setItem(idx, 'quantity', e.target.value)} />
                </div>
                <div style={{ flex: 1, paddingBottom: '0.5rem', fontSize: '0.9rem', fontWeight: 600, color: 'var(--text-primary)' }}>
                  ₹{lineTotal.toFixed(2)}
                </div>
                {items.length > 1 && (
                  <button type="button" className="btn btn-danger btn-sm" onClick={() => removeItem(idx)} style={{ marginBottom: '0.3rem' }}>
                    <Trash2 size={14} />
                  </button>
                )}
              </div>
            );
          })}
          <button type="button" className="btn btn-secondary btn-sm" onClick={addItem}>+ Add Line Item</button>
        </div>

        {/* Live Financial Calculation Box in Rupee ₹ */}
        <div style={{ background: 'var(--color-bg-subtle, rgba(255,255,255,0.04))', border: '1px solid var(--color-border)', borderRadius: '10px', padding: '1rem', marginTop: '0.5rem' }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr', gap: '0.75rem', fontSize: '0.9rem' }}>
            <div>
              <span style={{ color: 'var(--text-secondary)' }}>Subtotal:</span>
              <div style={{ fontWeight: 600, fontSize: '1.05rem' }}>₹{subtotal.toFixed(2)}</div>
            </div>
            <div>
              <span style={{ color: 'var(--text-secondary)' }}>Discount ({discountPct}%):</span>
              <div style={{ fontWeight: 600, color: '#ef4444' }}>-₹{discountAmount.toFixed(2)}</div>
            </div>
            <div>
              <span style={{ color: 'var(--text-secondary)' }}>Tax:</span>
              <div style={{ fontWeight: 600, color: '#10b981' }}>+₹{tax.toFixed(2)}</div>
            </div>
            <div>
              <span style={{ color: 'var(--text-secondary)', fontWeight: 700 }}>Grand Total:</span>
              <div style={{ fontWeight: 700, fontSize: '1.25rem', color: '#10b981' }}>₹{grandTotal.toFixed(2)}</div>
            </div>
          </div>
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
          <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Generating…' : 'Generate Invoice'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

/* ── Stripe Secure Payment Modal (Supports Card & UPI / GPay / PhonePe) ─── */
const StripePaymentModal = ({ isOpen, onClose, invoice, onSuccess }) => {
  const [payMethod, setPayMethod] = useState('CARD'); // 'CARD' or 'UPI'
  const [loading, setLoading] = useState(false);
  const [cardHolder, setCardHolder] = useState('');
  const [cardNumber, setCardNumber] = useState('4242 4242 4242 4242');
  const [expiry, setExpiry] = useState('12/28');
  const [cvc, setCvc] = useState('123');
  const [upiId, setUpiId] = useState('');

  if (!invoice) return null;

  const handlePay = async (e) => {
    e.preventDefault();
    if (payMethod === 'UPI' && !upiId) {
      toast.error('Please enter a valid UPI ID (e.g. mobile@upi or name@okaxis)');
      return;
    }
    setLoading(true);
    try {
      await billingApi.payWithStripe(invoice.id, {
        stripeToken: payMethod === 'CARD' ? 'tok_visa' : 'tok_upi',
        amount: invoice.balanceDue ?? invoice.grandTotal,
        currency: 'INR',
        paymentMethodType: payMethod,
        upiId: payMethod === 'UPI' ? upiId : undefined,
        remarks: payMethod === 'UPI' ? `Stripe UPI Payment (${upiId})` : 'Stripe Online Card Payment',
      });
      toast.success(`Payment of ₹${(invoice.balanceDue ?? invoice.grandTotal ?? 0).toFixed(2)} via Stripe ${payMethod} completed! Invoice PDF emailed to patient.`);
      onSuccess();
      onClose();
    } catch (err) {
      toast.error(err.response?.data?.message ?? err.response?.data?.detail ?? 'Stripe payment failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Stripe Online Payment (INR ₹)" maxWidth="500px">
      <form onSubmit={handlePay} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div style={{ background: 'linear-gradient(135deg, #6366f1 0%, #4f46e5 100%)', color: 'white', padding: '1.25rem', borderRadius: '12px', textAlign: 'center' }}>
          <div style={{ fontSize: '0.85rem', opacity: 0.9 }}>Amount Payable (INR)</div>
          <div style={{ fontSize: '2.2rem', fontWeight: 800 }}>₹{(invoice.balanceDue ?? invoice.grandTotal ?? 0).toFixed(2)}</div>
          <div style={{ fontSize: '0.8rem', opacity: 0.85, marginTop: '0.25rem' }}>Invoice #{invoice.invoiceNumber}</div>
        </div>

        {/* Payment Method Selector Tabs */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem', background: 'var(--color-bg-subtle, rgba(255,255,255,0.04))', padding: '0.25rem', borderRadius: '10px' }}>
          <button
            type="button"
            onClick={() => setPayMethod('CARD')}
            style={{
              padding: '0.6rem', borderRadius: '8px', border: 'none', cursor: 'pointer',
              fontWeight: 600, fontSize: '0.9rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.4rem',
              background: payMethod === 'CARD' ? '#6366f1' : 'transparent',
              color: payMethod === 'CARD' ? 'white' : 'var(--text-secondary)'
            }}
          >
            <CreditCard size={16} /> Credit / Debit Card
          </button>

          <button
            type="button"
            onClick={() => setPayMethod('UPI')}
            style={{
              padding: '0.6rem', borderRadius: '8px', border: 'none', cursor: 'pointer',
              fontWeight: 600, fontSize: '0.9rem', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '0.4rem',
              background: payMethod === 'UPI' ? '#10b981' : 'transparent',
              color: payMethod === 'UPI' ? 'white' : 'var(--text-secondary)'
            }}
          >
            <Smartphone size={16} /> Stripe UPI / QR
          </button>
        </div>

        {payMethod === 'CARD' ? (
          <>
            <div className="form-group">
              <label className="form-label">Cardholder Name</label>
              <input required type="text" placeholder="John Doe" value={cardHolder} onChange={(e) => setCardHolder(e.target.value)} />
            </div>

            <div className="form-group">
              <label className="form-label">Card Number</label>
              <div style={{ position: 'relative' }}>
                <input required type="text" value={cardNumber} onChange={(e) => setCardNumber(e.target.value)} placeholder="4242 4242 4242 4242" />
                <CreditCard size={18} style={{ position: 'absolute', right: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
              </div>
            </div>

            <div className="form-grid" style={{ gridTemplateColumns: '1fr 1fr' }}>
              <div className="form-group">
                <label className="form-label">Expiry (MM/YY)</label>
                <input required type="text" value={expiry} onChange={(e) => setExpiry(e.target.value)} placeholder="12/28" />
              </div>
              <div className="form-group">
                <label className="form-label">CVC</label>
                <input required type="password" maxLength={4} value={cvc} onChange={(e) => setCvc(e.target.value)} placeholder="123" />
              </div>
            </div>
          </>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', alignItems: 'center' }}>
            <div style={{ background: '#ffffff', padding: '1rem', borderRadius: '12px', border: '1px solid var(--color-border)', boxShadow: '0 4px 12px rgba(0,0,0,0.08)', textAlign: 'center' }}>
              <img
                src={`https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=${encodeURIComponent(`upi://pay?pa=medcorehms@upi&pn=MedCore%20Hospital&am=${(invoice.balanceDue ?? invoice.grandTotal ?? 0).toFixed(2)}&cu=INR&tn=Invoice%20${invoice.invoiceNumber}`)}`}
                alt="Scan UPI QR Code to Pay"
                style={{ width: '180px', height: '180px', borderRadius: '8px' }}
              />
              <div style={{ marginTop: '0.5rem', fontSize: '0.8rem', fontWeight: 600, color: '#111827' }}>
                Scan to pay <span style={{ color: '#10b981' }}>₹{(invoice.balanceDue ?? invoice.grandTotal ?? 0).toFixed(2)}</span>
              </div>
              <div style={{ fontSize: '0.75rem', color: '#6b7280', marginTop: '0.2rem' }}>
                Supports GPay, PhonePe, Paytm, BHIM & All UPI Apps
              </div>
            </div>

            <div className="form-group" style={{ width: '100%' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.3rem' }}>
                <label className="form-label" style={{ margin: 0 }}>Patient VPA / UPI ID *</label>
                <button
                  type="button"
                  style={{ background: 'none', border: 'none', color: '#6366f1', fontSize: '0.78rem', cursor: 'pointer', fontWeight: 600 }}
                  onClick={() => setUpiId('8726740214@axl')}
                >
                  + Use Demo UPI ID
                </button>
              </div>
              <input
                required
                type="text"
                value={upiId}
                onChange={(e) => setUpiId(e.target.value)}
                placeholder="e.g. 8726740214@axl, patient@upi, name@okaxis"
              />
            </div>

            <div style={{ width: '100%', background: 'rgba(16, 185, 129, 0.08)', border: '1px solid rgba(16, 185, 129, 0.2)', padding: '0.65rem 0.85rem', borderRadius: '8px', fontSize: '0.82rem', color: '#10b981', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <QrCode size={18} /> Scan the QR code above OR enter your UPI ID to complete instant payment.
            </div>
          </div>
        )}

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
          <Lock size={14} style={{ color: '#10b981' }} /> Encrypted & Secured by Stripe (INR ₹)
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
          <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={loading}
            style={{ background: payMethod === 'UPI' ? '#10b981' : '#6366f1', borderColor: payMethod === 'UPI' ? '#10b981' : '#6366f1' }}
          >
            {loading ? 'Processing Payment...' : `Pay ₹${(invoice.balanceDue ?? invoice.grandTotal ?? 0).toFixed(2)} via Stripe ${payMethod}`}
          </button>
        </div>
      </form>
    </Modal>
  );
};

/* ── Detailed Invoice Breakdown Modal ────────────────────────────────────── */
const InvoiceDetailsModal = ({ isOpen, onClose, invoiceId, onPayClick, onDownloadPdf }) => {
  const [invoice, setInvoice] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen && invoiceId) {
      setLoading(true);
      billingApi.getInvoiceById(invoiceId)
        .then((res) => setInvoice(res.data?.data ?? res.data))
        .catch(() => toast.error('Failed to load invoice details'))
        .finally(() => setLoading(false));
    }
  }, [isOpen, invoiceId]);

  if (!isOpen) return null;

  const discountPct = invoice?.discountPercentage ?? 0;

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={`Invoice #${invoice?.invoiceNumber ?? ''} (INR ₹)`} maxWidth="680px">
      {loading ? (
        <div style={{ textAlign: 'center', padding: '2rem' }}>Loading invoice details...</div>
      ) : invoice ? (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', background: 'var(--color-bg-subtle, rgba(255,255,255,0.03))', padding: '1rem', borderRadius: '10px' }}>
            <div>
              <h3 style={{ fontSize: '1.1rem', margin: 0 }}>{invoice.patientName ?? 'Patient Invoice'}</h3>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Issue Date: {invoice.issueDate} | Due Date: {invoice.dueDate ?? 'N/A'}</span>
            </div>
            <span className="badge" style={{ padding: '0.3rem 0.75rem', borderRadius: '6px', fontSize: '0.85rem', fontWeight: 600, background: invoice.status === 'PAID' ? 'rgba(16, 185, 129, 0.15)' : 'rgba(239, 68, 68, 0.15)', color: invoice.status === 'PAID' ? '#10b981' : '#ef4444' }}>
              {invoice.status}
            </span>
          </div>

          <div>
            <h4 style={{ fontSize: '0.95rem', fontWeight: 600, marginBottom: '0.5rem' }}>Line Items</h4>
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.9rem' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--color-border)', textAlign: 'left', color: 'var(--text-secondary)' }}>
                  <th style={{ padding: '0.5rem' }}>Description</th>
                  <th style={{ padding: '0.5rem' }}>Category</th>
                  <th style={{ padding: '0.5rem' }}>Price (₹)</th>
                  <th style={{ padding: '0.5rem' }}>Qty</th>
                  <th style={{ padding: '0.5rem', textAlign: 'right' }}>Total (₹)</th>
                </tr>
              </thead>
              <tbody>
                {invoice.items?.map((item, idx) => (
                  <tr key={idx} style={{ borderBottom: '1px solid var(--color-border)' }}>
                    <td style={{ padding: '0.6rem 0.5rem' }}>{item.description}</td>
                    <td style={{ padding: '0.6rem 0.5rem' }}>{item.category}</td>
                    <td style={{ padding: '0.6rem 0.5rem' }}>₹{(item.unitPrice ?? 0).toFixed(2)}</td>
                    <td style={{ padding: '0.6rem 0.5rem' }}>{item.quantity}</td>
                    <td style={{ padding: '0.6rem 0.5rem', textAlign: 'right', fontWeight: 600 }}>₹{(item.totalPrice ?? 0).toFixed(2)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem', background: 'var(--color-bg-subtle, rgba(255,255,255,0.03))', padding: '1rem', borderRadius: '10px', fontSize: '0.9rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span>Subtotal:</span>
              <span>₹{(invoice.subtotal ?? 0).toFixed(2)}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', color: '#ef4444' }}>
              <span>Discount ({discountPct}%):</span>
              <span>-₹{(invoice.discountAmount ?? 0).toFixed(2)}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', color: '#10b981' }}>
              <span>Tax:</span>
              <span>+₹{(invoice.taxAmount ?? 0).toFixed(2)}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 700, fontSize: '1.1rem', borderTop: '1px solid var(--color-border)', paddingTop: '0.5rem', marginTop: '0.25rem' }}>
              <span>Grand Total:</span>
              <span>₹{(invoice.grandTotal ?? 0).toFixed(2)}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', color: '#10b981' }}>
              <span>Paid Amount:</span>
              <span>₹{(invoice.paidAmount ?? 0).toFixed(2)}</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: 700, color: invoice.balanceDue > 0 ? '#ef4444' : '#10b981' }}>
              <span>Balance Due:</span>
              <span>₹{(invoice.balanceDue ?? 0).toFixed(2)}</span>
            </div>
          </div>

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '0.5rem' }}>
            <button className="btn btn-secondary" onClick={() => onDownloadPdf(invoice.id, invoice.invoiceNumber)}>
              <Download size={16} style={{ marginRight: '0.4rem' }} /> Download PDF
            </button>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              {invoice.status !== 'PAID' && invoice.balanceDue > 0 && (
                <button className="btn btn-primary" style={{ background: '#6366f1', borderColor: '#6366f1' }} onClick={() => { onClose(); onPayClick(invoice); }}>
                  <CreditCard size={16} style={{ marginRight: '0.4rem' }} /> Pay with Stripe
                </button>
              )}
              <button className="btn btn-secondary" onClick={onClose}>Close</button>
            </div>
          </div>
        </div>
      ) : null}
    </Modal>
  );
};

/* ── Status color helper ───────────────────────────────────────────────── */
const statusStyle = (status) => {
  switch (status) {
    case 'PAID': return { background: 'rgba(16, 185, 129, 0.1)', color: '#10b981' };
    case 'PARTIALLY_PAID': return { background: 'rgba(245, 158, 11, 0.1)', color: '#f59e0b' };
    case 'REFUNDED': return { background: 'rgba(20, 112, 108, 0.1)', color: '#14706c' };
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
  const [selectedInvoiceForPay, setSelectedInvoiceForPay] = useState(null);
  const [selectedInvoiceIdForDetails, setSelectedInvoiceIdForDetails] = useState(null);

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

  const handleDownloadPdf = async (invoiceId, invoiceNumber) => {
    try {
      const res = await billingApi.downloadInvoicePdf(invoiceId);
      const blob = new Blob([res.data], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `Invoice-${invoiceNumber || invoiceId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      toast.success('Invoice PDF downloaded!');
    } catch {
      toast.error('Failed to download PDF');
    }
  };

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '700', color: 'var(--text-primary)' }}>Billing & Payments (INR ₹)</h1>
          <p style={{ color: 'var(--text-secondary)' }}>
            {isPatient
              ? 'View your invoices in INR (₹), download PDFs, and pay via Stripe Card or UPI'
              : 'Generate percentage-discounted patient invoices in INR (₹), accept Stripe & UPI payments'}
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
            <div key={inv.id} className="card" style={{ padding: '1.5rem', borderRadius: '12px', border: '1px solid var(--color-border)', background: 'var(--color-bg-elevated)', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
              <div>
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
                    <div>Subtotal: ₹{(inv.subtotal ?? 0).toFixed(2)}</div>
                    <div>Tax: +₹{(inv.taxAmount ?? 0).toFixed(2)}</div>
                    <div>Discount ({inv.discountPercentage ?? 0}%): -₹{(inv.discountAmount ?? 0).toFixed(2)}</div>
                    <div><strong>Total: ₹{(inv.grandTotal ?? 0).toFixed(2)}</strong></div>
                  </div>

                  {inv.balanceDue !== undefined && (
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', marginTop: '0.4rem', color: inv.balanceDue > 0 ? '#ef4444' : '#10b981', fontWeight: 600 }}>
                      <span>Paid: ₹{(inv.paidAmount ?? 0).toFixed(2)}</span>
                      <span>Balance Due: ₹{(inv.balanceDue ?? 0).toFixed(2)}</span>
                    </div>
                  )}
                </div>
              </div>

              <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1.25rem', paddingTop: '0.75rem', borderTop: '1px solid var(--color-border)' }}>
                <button className="btn btn-secondary btn-sm" style={{ flex: 1 }} onClick={() => setSelectedInvoiceIdForDetails(inv.id)}>
                  <Eye size={14} style={{ marginRight: '0.3rem' }} /> Details
                </button>

                <button className="btn btn-secondary btn-sm" style={{ flex: 1 }} onClick={() => handleDownloadPdf(inv.id, inv.invoiceNumber)}>
                  <Download size={14} style={{ marginRight: '0.3rem' }} /> PDF
                </button>

                {inv.status !== 'PAID' && (inv.balanceDue === undefined || inv.balanceDue > 0) && (
                  <button className="btn btn-primary btn-sm" style={{ flex: 1.2, background: '#6366f1', borderColor: '#6366f1' }} onClick={() => setSelectedInvoiceForPay(inv)}>
                    <CreditCard size={14} style={{ marginRight: '0.3rem' }} /> Pay (Stripe/UPI)
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {canCreate && (
        <CreateInvoiceModal isOpen={showCreate} onClose={() => setShowCreate(false)} onSuccess={fetchInvoices} />
      )}

      <StripePaymentModal
        isOpen={Boolean(selectedInvoiceForPay)}
        onClose={() => setSelectedInvoiceForPay(null)}
        invoice={selectedInvoiceForPay}
        onSuccess={fetchInvoices}
      />

      <InvoiceDetailsModal
        isOpen={Boolean(selectedInvoiceIdForDetails)}
        onClose={() => setSelectedInvoiceIdForDetails(null)}
        invoiceId={selectedInvoiceIdForDetails}
        onPayClick={(inv) => setSelectedInvoiceForPay(inv)}
        onDownloadPdf={handleDownloadPdf}
      />
    </div>
  );
}
