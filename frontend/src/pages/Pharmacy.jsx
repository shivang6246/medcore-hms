import React, { useState, useEffect, useCallback } from 'react';
import { Package, Plus, ShoppingBag, RefreshCw, AlertTriangle } from 'lucide-react';
import { pharmacyApi } from '../api/pharmacy.api';
import toast from 'react-hot-toast';
import Modal from '../components/ui/Modal';
import useAuthStore from '../store/authStore';

/* ── Add Medicine Stock Modal ──────────────────────────────────────────── */
const AddStockModal = ({ isOpen, onClose, onSuccess, medicines }) => {
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    medicineId: '', batchNumber: '', expiryDate: '',
    purchasePrice: '', sellingPrice: '', quantity: '',
  });

  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await pharmacyApi.addStock({
        ...form,
        purchasePrice: parseFloat(form.purchasePrice),
        sellingPrice: parseFloat(form.sellingPrice),
        quantity: parseInt(form.quantity, 10),
      });
      toast.success('Stock batch added successfully!');
      onSuccess();
      onClose();
      setForm({ medicineId: '', batchNumber: '', expiryDate: '', purchasePrice: '', sellingPrice: '', quantity: '' });
    } catch (err) {
      toast.error(err.response?.data?.message ?? err.response?.data?.detail ?? 'Failed to add stock.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Add Medicine Stock" maxWidth="600px">
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div className="form-grid">
          <div className="form-group">
            <label className="form-label">Medicine *</label>
            <select required value={form.medicineId} onChange={(e) => set('medicineId', e.target.value)}>
              <option value="">Select Medicine</option>
              {medicines.map((m) => (
                <option key={m.id} value={m.id}>{m.name}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Batch Number *</label>
            <input required value={form.batchNumber} onChange={(e) => set('batchNumber', e.target.value)} placeholder="BATCH-2026-08A" />
          </div>
          <div className="form-group">
            <label className="form-label">Expiry Date *</label>
            <input required type="date" value={form.expiryDate} onChange={(e) => set('expiryDate', e.target.value)} />
          </div>
          <div className="form-group">
            <label className="form-label">Purchase Price *</label>
            <input required type="number" step="0.01" min="0" value={form.purchasePrice} onChange={(e) => set('purchasePrice', e.target.value)} placeholder="3.20" />
          </div>
          <div className="form-group">
            <label className="form-label">Selling Price *</label>
            <input required type="number" step="0.01" min="0" value={form.sellingPrice} onChange={(e) => set('sellingPrice', e.target.value)} placeholder="5.50" />
          </div>
          <div className="form-group">
            <label className="form-label">Quantity *</label>
            <input required type="number" min="1" value={form.quantity} onChange={(e) => set('quantity', e.target.value)} placeholder="500" />
          </div>
        </div>
        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
          <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Adding…' : 'Add Stock'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

/* ── Dispense Medicine Modal ───────────────────────────────────────────── */
const DispenseModal = ({ isOpen, onClose, onSuccess, medicines }) => {
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({ patientId: '', remarks: '' });
  const [items, setItems] = useState([{ medicineId: '', quantity: '' }]);

  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  const setItem = (idx, k, v) => {
    setItems((prev) => prev.map((item, i) => (i === idx ? { ...item, [k]: v } : item)));
  };

  const addItem = () => setItems((prev) => [...prev, { medicineId: '', quantity: '' }]);
  const removeItem = (idx) => setItems((prev) => prev.filter((_, i) => i !== idx));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await pharmacyApi.dispense({
        patientId: form.patientId,
        remarks: form.remarks || undefined,
        items: items.map((it) => ({ medicineId: it.medicineId, quantity: parseInt(it.quantity, 10) })),
      });
      toast.success('Medicine dispensed successfully!');
      onSuccess();
      onClose();
      setForm({ patientId: '', remarks: '' });
      setItems([{ medicineId: '', quantity: '' }]);
    } catch (err) {
      toast.error(err.response?.data?.message ?? err.response?.data?.detail ?? 'Failed to dispense.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Dispense Medicine" maxWidth="650px">
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <div className="form-group">
          <label className="form-label">Patient ID *</label>
          <input required value={form.patientId} onChange={(e) => set('patientId', e.target.value)} placeholder="Patient UUID" />
        </div>

        <label className="form-label" style={{ fontWeight: 600 }}>Medicines to Dispense</label>
        {items.map((item, idx) => (
          <div key={idx} style={{ display: 'flex', gap: '0.75rem', alignItems: 'flex-end' }}>
            <div className="form-group" style={{ flex: 2 }}>
              <label className="form-label">Medicine *</label>
              <select required value={item.medicineId} onChange={(e) => setItem(idx, 'medicineId', e.target.value)}>
                <option value="">Select</option>
                {medicines.map((m) => (
                  <option key={m.id} value={m.id}>{m.name}</option>
                ))}
              </select>
            </div>
            <div className="form-group" style={{ flex: 1 }}>
              <label className="form-label">Qty *</label>
              <input required type="number" min="1" value={item.quantity} onChange={(e) => setItem(idx, 'quantity', e.target.value)} placeholder="10" />
            </div>
            {items.length > 1 && (
              <button type="button" className="btn btn-danger btn-sm" onClick={() => removeItem(idx)} style={{ marginBottom: '0.15rem' }}>✕</button>
            )}
          </div>
        ))}
        <button type="button" className="btn btn-secondary btn-sm" onClick={addItem} style={{ alignSelf: 'flex-start' }}>
          + Add Item
        </button>

        <div className="form-group">
          <label className="form-label">Remarks</label>
          <textarea rows={2} value={form.remarks} onChange={(e) => set('remarks', e.target.value)} placeholder="Optional remarks" />
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
          <button type="button" className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Dispensing…' : 'Dispense'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

/* ── Main Pharmacy Page ────────────────────────────────────────────────── */
export default function Pharmacy() {
  const [medicines, setMedicines] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAddStock, setShowAddStock] = useState(false);
  const [showDispense, setShowDispense] = useState(false);

  const fetchMedicines = useCallback(async () => {
    setLoading(true);
    try {
      const res = await pharmacyApi.getMedicines({ page: 0, size: 50 });
      const payload = res.data;
      // handle both wrapped ApiResponse { data: { content: [...] } } and raw array
      const list = payload?.data?.content ?? payload?.content ?? (Array.isArray(payload?.data) ? payload.data : (Array.isArray(payload) ? payload : []));
      setMedicines(list);
    } catch (err) {
      toast.error('Failed to load medicines.');
      setMedicines([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { fetchMedicines(); }, [fetchMedicines]);

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '700', color: 'var(--text-primary)' }}>Pharmacy & Inventory</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Manage medicine catalog, stock batches, FEFO allocation, and dispensing</p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button className="btn btn-secondary" onClick={fetchMedicines} title="Refresh">
            <RefreshCw size={18} />
          </button>
          <button className="btn btn-secondary" onClick={() => setShowDispense(true)}>
            <ShoppingBag size={18} style={{ marginRight: '0.5rem' }} /> Dispense Medicine
          </button>
          <button className="btn btn-primary" onClick={() => setShowAddStock(true)}>
            <Plus size={18} style={{ marginRight: '0.5rem' }} /> Add Medicine Stock
          </button>
        </div>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '4rem', color: 'var(--text-muted)' }}>
          <div className="skeleton" style={{ width: '48px', height: '48px', borderRadius: '50%', margin: '0 auto 1rem' }} />
          <p>Loading medicines…</p>
        </div>
      ) : medicines.length === 0 ? (
        <div className="empty-state">
          <Package size={48} />
          <h3>No medicines found</h3>
          <p>Add stock to get started.</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '1.5rem' }}>
          {medicines.map((m) => (
            <div key={m.id} className="card" style={{ padding: '1.5rem', borderRadius: '12px', border: '1px solid var(--color-border)', background: 'var(--color-bg-elevated)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                  <div style={{ padding: '0.5rem', borderRadius: '8px', background: 'rgba(245, 158, 11, 0.1)', color: '#f59e0b' }}>
                    <Package size={24} />
                  </div>
                  <div>
                    <h3 style={{ fontSize: '1.1rem', fontWeight: '600' }}>{m.name}</h3>
                    <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{m.genericName ?? '—'}</span>
                  </div>
                </div>
                <span className="badge" style={{
                  background: (m.stockQuantity ?? m.totalStock ?? 0) < 20 ? 'rgba(239, 68, 68, 0.1)' : 'rgba(16, 185, 129, 0.1)',
                  color: (m.stockQuantity ?? m.totalStock ?? 0) < 20 ? '#ef4444' : '#10b981',
                  padding: '0.25rem 0.6rem', borderRadius: '6px', fontSize: '0.8rem'
                }}>
                  {(m.stockQuantity ?? m.totalStock ?? 0) < 20 ? 'Low Stock' : 'In Stock'}
                </span>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.9rem' }}>
                <div><strong>Brand:</strong> {m.brand ?? '—'} | <strong>Category:</strong> {m.category ?? '—'}</div>
                <div><strong>Unit Price:</strong> <span style={{ color: '#10b981', fontWeight: '600' }}>${m.unitPrice ?? m.price ?? '—'}</span></div>
                <div><strong>Stock In Hand:</strong> <strong>{m.stockQuantity ?? m.totalStock ?? 0} units</strong></div>
                {m.expiryDate && <div><strong>Expiry Date:</strong> {m.expiryDate}</div>}
              </div>
            </div>
          ))}
        </div>
      )}

      <AddStockModal isOpen={showAddStock} onClose={() => setShowAddStock(false)} onSuccess={fetchMedicines} medicines={medicines} />
      <DispenseModal isOpen={showDispense} onClose={() => setShowDispense(false)} onSuccess={fetchMedicines} medicines={medicines} />
    </div>
  );
}
