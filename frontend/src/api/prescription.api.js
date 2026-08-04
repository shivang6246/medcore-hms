import axiosInstance from './axios';

export const prescriptionApi = {
  create: (data) => axiosInstance.post('/prescriptions', data),
  getById: (id) => axiosInstance.get(`/prescriptions/${id}`),
  update: (id, data) => axiosInstance.put(`/prescriptions/${id}`, data),
  getByMedicalRecord: (medicalRecordId, params) => axiosInstance.get(`/medical-records/${medicalRecordId}/prescriptions`, { params }),
  deactivate: (id) => axiosInstance.delete(`/prescriptions/${id}`),
};
