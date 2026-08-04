import axiosInstance from './axios';

export const labApi = {
  createTest: (data) => axiosInstance.post('/lab-tests', data),
  getTestById: (id) => axiosInstance.get(`/lab-tests/${id}`),
  getByPatient: (patientId, params) => axiosInstance.get(`/patients/${patientId}/lab-tests`, { params }),
  updateStatus: (id, status) => axiosInstance.patch(`/lab-tests/${id}/status`, null, { params: { status } }),
  addReport: (id, data) => axiosInstance.post(`/lab-tests/${id}/reports`, data),
  publishReport: (reportId) => axiosInstance.patch(`/lab-reports/${reportId}/publish`),
};
