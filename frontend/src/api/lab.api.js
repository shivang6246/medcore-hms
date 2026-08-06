import api from './axios';

export const labApi = {
  getAll: (params) => api.get('/lab-tests', { params }),
  createTest: (data) => api.post('/lab-tests', data),
  getTestById: (id) => api.get(`/lab-tests/${id}`),
  getByPatient: (patientId, params) => api.get(`/patients/${patientId}/lab-tests`, { params }),
  updateStatus: (id, data) => api.patch(`/lab-tests/${id}/status`, data),
  publishReport: (labTestId, data) => api.post(`/lab-tests/${labTestId}/report`, data),
};
