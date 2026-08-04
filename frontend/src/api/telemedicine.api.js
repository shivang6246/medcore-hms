import axiosInstance from './axios';

export const telemedicineApi = {
  createSession: (data) => axiosInstance.post('/telemedicine/sessions', data),
  getSessionById: (id) => axiosInstance.get(`/telemedicine/sessions/${id}`),
  joinSession: (id, role) => axiosInstance.post(`/telemedicine/sessions/${id}/join`, null, { params: { role } }),
  startSession: (id) => axiosInstance.post(`/telemedicine/sessions/${id}/start`),
  completeSession: (id, notes) => axiosInstance.post(`/telemedicine/sessions/${id}/complete`, null, { params: { notes } }),
  getDoctorWaitingRoom: (doctorId) => axiosInstance.get(`/telemedicine/doctor/${doctorId}/waiting-room`),
  getPatientHistory: (patientId, params) => axiosInstance.get(`/telemedicine/patient/${patientId}/history`, { params }),
};
