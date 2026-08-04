import api from './axios';

export const appointmentApi = {
  getAll: (params) => api.get('/appointments', { params }),
  getById: (id) => api.get(`/appointments/${id}`),
  book: (data) => api.post('/appointments', data),
  cancel: (id, data) => api.patch(`/appointments/${id}/cancel`, data),
  reschedule: (id, data) => api.patch(`/appointments/${id}/reschedule`, data),
  updateNotes: (id, data) => api.patch(`/appointments/${id}/notes`, data),
  getDailySchedule: (doctorId, date) =>
    api.get(`/appointments/daily-schedule`, { params: { doctorId, date } }),
};
