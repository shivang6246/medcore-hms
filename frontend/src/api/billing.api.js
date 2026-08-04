import axiosInstance from './axios';

export const billingApi = {
  createInvoice: (data) => axiosInstance.post('/invoices', data),
  getInvoiceById: (id) => axiosInstance.get(`/invoices/${id}`),
  recordPayment: (data) => axiosInstance.post('/payments', data),
  refund: (invoiceId, params) => axiosInstance.post(`/invoices/${invoiceId}/refund`, null, { params }),
  getRevenueReport: (params) => axiosInstance.get('/billing/reports/revenue', { params }),
};
