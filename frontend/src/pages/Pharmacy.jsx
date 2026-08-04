import React, { useState } from 'react';
import { Package, Plus, AlertTriangle, Calendar, ShoppingBag } from 'lucide-react';
import toast from 'react-hot-toast';

export default function Pharmacy() {
  const [medicines] = useState([
    {
      id: '1',
      name: 'Paracetamol 650mg',
      genericName: 'Acetaminophen',
      brand: 'Dolo',
      category: 'Analgesics',
      price: '$4.50',
      stockQuantity: 450,
      expiryDate: '2027-11-30'
    },
    {
      id: '2',
      name: 'Azithromycin 500mg',
      genericName: 'Azithromycin',
      brand: 'Azithral',
      category: 'Antibiotics',
      price: '$12.00',
      stockQuantity: 18,
      expiryDate: '2026-12-15'
    }
  ]);

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '1.75rem', fontWeight: '700', color: 'var(--text-primary)' }}>Pharmacy & Inventory</h1>
          <p style={{ color: 'var(--text-secondary)' }}>Manage medicine catalog, stock batches, FEFO allocation, and dispensing</p>
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <button className="btn btn-secondary" onClick={() => toast.success('Dispense Stock')}>
            <ShoppingBag size={18} style={{ marginRight: '0.5rem' }} /> Dispense Medicine
          </button>
          <button className="btn btn-primary" onClick={() => toast.success('Add Stock')}>
            <Plus size={18} style={{ marginRight: '0.5rem' }} /> Add Medicine Stock
          </button>
        </div>
      </div>

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
                  <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>{m.genericName}</span>
                </div>
              </div>
              <span className="badge" style={{ background: m.stockQuantity < 20 ? 'rgba(239, 68, 68, 0.1)' : 'rgba(16, 185, 129, 0.1)', color: m.stockQuantity < 20 ? '#ef4444' : '#10b981', padding: '0.25rem 0.6rem', borderRadius: '6px', fontSize: '0.8rem' }}>
                {m.stockQuantity < 20 ? 'Low Stock' : 'In Stock'}
              </span>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', fontSize: '0.9rem' }}>
              <div><strong>Brand:</strong> {m.brand} | <strong>Category:</strong> {m.category}</div>
              <div><strong>Unit Price:</strong> <span style={{ color: '#10b981', fontWeight: '600' }}>{m.price}</span></div>
              <div><strong>Stock In Hand:</strong> <strong>{m.stockQuantity} units</strong></div>
              <div><strong>Expiry Date:</strong> {m.expiryDate}</div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
