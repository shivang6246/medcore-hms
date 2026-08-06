import api from './axios';

export const ipdApi = {
  getAll: (params) => api.get('/admissions', { params }),
  getAvailableBeds: () => api.get('/beds/available'),
  admitPatient: (data) => api.post('/admissions', data),
  getAdmissionById: (id) => api.get(`/admissions/${id}`),
  transferBed: (id, data) => api.patch(`/admissions/${id}/transfer`, data),
  dischargePatient: (id, data) => api.patch(`/admissions/${id}/discharge`, data),
};
