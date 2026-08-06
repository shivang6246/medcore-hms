import api from './axios';

export const prescriptionApi = {
  create: (data) => api.post('/prescriptions', data),
  getById: (id) => api.get(`/prescriptions/${id}`),
  update: (id, data) => api.put(`/prescriptions/${id}`, data),
  deactivate: (id) => api.patch(`/prescriptions/${id}/deactivate`),
  getByMedicalRecord: (medicalRecordId, params) =>
    api.get(`/medical-records/${medicalRecordId}/prescriptions`, { params }),
  getAllByMedicalRecord: (medicalRecordId, params) =>
    api.get(`/medical-records/${medicalRecordId}/prescriptions/all`, { params }),
};
