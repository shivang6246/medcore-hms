import axiosInstance from './axios';

export const medicalRecordApi = {
  create: (data) => axiosInstance.post('/medical-records', data),
  getById: (id) => axiosInstance.get(`/medical-records/${id}`),
  update: (id, data) => axiosInstance.put(`/medical-records/${id}`, data),
  getByPatient: (patientId, params) => axiosInstance.get(`/patients/${patientId}/medical-records`, { params }),
  getByDoctor: (doctorId, params) => axiosInstance.get(`/doctors/${doctorId}/medical-records`, { params }),
};
