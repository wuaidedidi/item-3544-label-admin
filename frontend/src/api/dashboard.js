import request from '../utils/request'

export function getStats() {
  return request.get('/dashboard/stats')
}

export function getAdminStats() {
  return request.get('/dashboard/admin-stats')
}
