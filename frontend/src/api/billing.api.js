import api from './axios';

export const billingApi = {
  createInvoice: (data) => api.post('/invoices', data),
  getInvoiceById: (id) => api.get(`/invoices/${id}`),
  getAllInvoices: (params) => api.get('/invoices', { params }),
  getPatientInvoices: (patientId, params) => api.get(`/patients/${patientId}/invoices`, { params }),
  recordPayment: (data) => api.post('/payments', data),
  payWithStripe: (invoiceId, data) => api.post(`/invoices/${invoiceId}/stripe-pay`, data),
  downloadInvoicePdf: (invoiceId) => api.get(`/invoices/${invoiceId}/pdf`, { responseType: 'blob' }),
  getPaymentById: (id) => api.get(`/payments/${id}`),
  refund: (invoiceId, params) => api.post(`/invoices/${invoiceId}/refund`, null, { params }),
  getRevenueReport: (params) => api.get('/billing/reports/revenue', { params }),
};
