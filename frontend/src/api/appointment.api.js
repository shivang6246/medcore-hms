import api from './axios';

export const appointmentApi = {
  getAll: (params) => api.get('/appointments', { params }),
  search: (params) => api.get('/appointments/search', { params }),
  getByPatient: (patientId, params) => api.get(`/appointments/patient/${patientId}`, { params }),
  getByDoctor: (doctorId, params) => api.get(`/appointments/doctor/${doctorId}`, { params }),
  getById: (id) => api.get(`/appointments/${id}`),
  book: (data) => api.post('/appointments', data),
  cancel: (id, data) => api.patch(`/appointments/${id}/cancel`, data),
  reschedule: (id, data) => api.patch(`/appointments/${id}/reschedule`, data),
  updateNotes: (id, data) => api.patch(`/appointments/${id}/notes`, data),
  getDailySchedule: (doctorId, date) =>
    api.get(`/appointments/doctor/${doctorId}/schedule`, { params: { date } }),
  getSlots: (doctorId, date) =>
    api.get(`/doctors/${doctorId}/slots`, { params: { date } }),
};
