import request from '../utils/request'

export function getRoleList(params) {
  return request.get('/roles', { params })
}

export function getAllRoles() {
  return request.get('/roles/all')
}

export function getRoleById(id) {
  return request.get(`/roles/${id}`)
}

export function createRole(data) {
  return request.post('/roles', data)
}

export function updateRole(data) {
  return request.put('/roles', data)
}

export function deleteRole(id) {
  return request.delete(`/roles/${id}`)
}

export function getAllPermissions() {
  return request.get('/roles/permissions')
}

export function getRolePermissions(id) {
  return request.get(`/roles/${id}/permissions`)
}

export function exportRoles(params) {
  return request.get('/roles/export', {
    params,
    responseType: 'blob'
  })
}
