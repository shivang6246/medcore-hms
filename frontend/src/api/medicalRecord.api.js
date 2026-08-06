import api from './axios';

export const medicalRecordApi = {
  getAll: (params) => api.get('/medical-records', { params }),
  create: (data) => api.post('/medical-records', data),
  getById: (id) => api.get(`/medical-records/${id}`),
  update: (id, data) => api.put(`/medical-records/${id}`, data),
  deactivate: (id) => api.patch(`/medical-records/${id}/deactivate`),
  getByPatient: (patientId, params) => api.get(`/patients/${patientId}/medical-records`, { params }),
  getByDoctor: (doctorId, params) => api.get(`/doctors/${doctorId}/medical-records`, { params }),
};
