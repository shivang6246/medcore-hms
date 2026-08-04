import api from './axios';

export const patientApi = {
  getAll: (params) => api.get('/patients', { params }),
  search: (params) => api.get('/patients/search', { params }),
  getById: (id) => api.get(`/patients/${id}`),
  getByPatientId: (patientId, hospitalId) =>
    api.get(`/patients/by-patient-id/${patientId}`, { params: { hospitalId } }),
  create: (data) => api.post('/patients', data),
  update: (id, data) => api.put(`/patients/${id}`, data),
  activate: (id) => api.patch(`/patients/${id}/activate`),
  deactivate: (id) => api.patch(`/patients/${id}/deactivate`),
};
