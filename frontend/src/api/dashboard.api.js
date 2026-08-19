import axiosInstance from './axios';

export const dashboardApi = {
  getAdminMetrics: (hospitalId) =>
    axiosInstance.get('/dashboard/admin', { params: hospitalId ? { hospitalId } : {} }),

  getDoctorMetrics: (doctorId) =>
    axiosInstance.get('/dashboard/doctor', { params: { doctorId } }),

  getReceptionMetrics: (hospitalId) =>
    axiosInstance.get('/dashboard/reception', { params: hospitalId ? { hospitalId } : {} }),

  getRevenueAnalytics: (months = 12, hospitalId) =>
    axiosInstance.get('/dashboard/analytics/revenue', {
      params: { months, ...(hospitalId ? { hospitalId } : {}) },
    }),

  getAppointmentAnalytics: (days = 7, hospitalId) =>
    axiosInstance.get('/dashboard/analytics/appointments', {
      params: { days, ...(hospitalId ? { hospitalId } : {}) },
    }),
};
