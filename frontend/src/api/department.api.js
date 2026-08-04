import api from './axios';

export const departmentApi = {
  getByHospital: (hospitalId, activeOnly = false) =>
    api.get('/departments', { params: { hospitalId, activeOnly } }),
};
