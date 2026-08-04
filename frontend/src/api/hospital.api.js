import api from './axios';

export const hospitalApi = {
  getAll: (params) => api.get('/hospitals', { params }),
  getById: (id) => api.get(`/hospitals/${id}`),
  create: (data) => api.post('/hospitals', data),
  update: (id, data) => api.put(`/hospitals/${id}`, data),
  activate: (id) => api.patch(`/hospitals/${id}/activate`),
  deactivate: (id) => api.patch(`/hospitals/${id}/deactivate`),
};
