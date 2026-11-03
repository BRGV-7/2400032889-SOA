import client from '../api/client'

export const authApi = {
  login: (credentials) => client.post('/auth/login', credentials),
  register: (credentials) => client.post('/auth/register', credentials),
  profile: () => client.get('/auth/me'),
}
export const electionApi = {
  list: () => client.get('/elections'),
  get: (id) => client.get(`/elections/${id}`),
  create: (data) => client.post('/elections', data),
  update: (id, data) => client.put(`/elections/${id}`, data),
  addCandidate: (id, data) => client.post(`/elections/${id}/candidates`, data),
  removeCandidate: (electionId, candidateId) => client.delete(`/elections/${electionId}/candidates/${candidateId}`),
}
export const votingApi = {
  cast: (electionId, candidateId) => client.post('/votes', { electionId, candidateId }),
  status: (electionId) => client.get(`/votes/status/${electionId}`),
}
export const resultApi = {
  get: (id) => client.get(`/results/${id}`),
  publish: (id) => client.post(`/results/${id}/publish`),
}
