import api from './axios';

export const pharmacyApi = {
  getMedicines: (params) => api.get('/medicines', { params }),
  getMedicineById: (id) => api.get(`/medicines/${id}`),
  createMedicine: (data) => api.post('/medicines', data),
  updateMedicine: (id, data) => api.put(`/medicines/${id}`, data),
  addStock: (data) => api.post('/inventory/add-stock', data),
  adjustStock: (data) => api.post('/inventory/adjust', data),
  dispense: (data) => api.post('/dispense', data),
  getLowStock: () => api.get('/inventory/low-stock'),
  getExpired: () => api.get('/inventory/expired'),
  getTransactions: (params) => api.get('/inventory/transactions', { params }),
  createSupplier: (data) => api.post('/suppliers', data),
  getSuppliers: (params) => api.get('/suppliers', { params }),
};
