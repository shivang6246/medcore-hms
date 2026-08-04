import axiosInstance from './axios';

export const dashboardApi = {
  getAdminMetrics: () => axiosInstance.get('/dashboard/admin'),
  getDoctorMetrics: (doctorId) => axiosInstance.get('/dashboard/doctor', { params: { doctorId } }),
  getReceptionMetrics: () => axiosInstance.get('/dashboard/reception'),
};
