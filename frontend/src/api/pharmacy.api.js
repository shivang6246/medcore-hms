import axiosInstance from './axios';

export const pharmacyApi = {
  getMedicines: (params) => axiosInstance.get('/medicines', { params }),
  createMedicine: (data) => axiosInstance.post('/medicines', data),
  addStock: (data) => axiosInstance.post('/inventory/add-stock', data),
  dispense: (data) => axiosInstance.post('/dispense', data),
  getLowStock: () => axiosInstance.get('/inventory/low-stock'),
  getExpired: () => axiosInstance.get('/inventory/expired'),
};
