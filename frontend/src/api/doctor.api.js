import api from './axios';

export const doctorApi = {
  getAll: (params) => api.get('/doctors', { params }),
  getById: (id) => api.get(`/doctors/${id}`),
  getByEmployeeId: (employeeId, hospitalId) =>
    api.get(`/doctors/by-employee-id/${employeeId}`, { params: { hospitalId } }),
  create: (data) => api.post('/doctors', data),
  update: (id, data) => api.put(`/doctors/${id}`, data),
  activate: (id) => api.patch(`/doctors/${id}/activate`),
  deactivate: (id) => api.patch(`/doctors/${id}/deactivate`),
  assignDepartment: (id, departmentId) => api.patch(`/doctors/${id}/department`, { departmentId }),
  updateAvailability: (id, available) => api.patch(`/doctors/${id}/availability`, { available }),
  updateFee: (id, fee) => api.patch(`/doctors/${id}/consultation-fee`, { fee }),

  getSchedules: (doctorId) => api.get(`/doctors/${doctorId}/schedules`),
  createSchedule: (doctorId, data) => api.post(`/doctors/${doctorId}/schedules`, data),
  deleteSchedule: (doctorId, scheduleId) => api.delete(`/doctors/${doctorId}/schedules/${scheduleId}`),
  activateSchedule: (doctorId, scheduleId) => api.patch(`/doctors/${doctorId}/schedules/${scheduleId}/activate`),
  deactivateSchedule: (doctorId, scheduleId) => api.patch(`/doctors/${doctorId}/schedules/${scheduleId}/deactivate`),

  getSlots: (doctorId, date) => api.get(`/doctors/${doctorId}/slots`, { params: { date } }),
  generateSlots: (doctorId, fromDate, toDate) =>
    api.post(`/doctors/${doctorId}/slots/generate`, { fromDate, toDate }),
};
