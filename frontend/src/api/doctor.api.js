import api from './axios';

export const doctorApi = {
  getAll: (params) => api.get('/doctors', { params }),
  getById: (id) => api.get(`/doctors/${id}`),
  create: (data) => api.post('/doctors', data),
  update: (id, data) => api.put(`/doctors/${id}`, data),
  activate: (id) => api.patch(`/doctors/${id}/activate`),
  deactivate: (id) => api.patch(`/doctors/${id}/deactivate`),
  assignDepartment: (id, departmentId) => api.patch(`/doctors/${id}/department`, { departmentId }),
  updateAvailability: (id, available) => api.patch(`/doctors/${id}/availability`, { available }),
  updateFee: (id, fee) => api.patch(`/doctors/${id}/consultation-fee`, { fee }),
};
