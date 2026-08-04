import axiosInstance from './axios';

export const ipdApi = {
  getAvailableBeds: () => axiosInstance.get('/beds/available'),
  admitPatient: (data) => axiosInstance.post('/admissions', data),
  getAdmissionById: (id) => axiosInstance.get(`/admissions/${id}`),
  transferBed: (id, bedId) => axiosInstance.patch(`/admissions/${id}/transfer`, null, { params: { newBedId: bedId } }),
  dischargePatient: (id, data) => axiosInstance.patch(`/admissions/${id}/discharge`, data),
};
