import request from '../utils/request'

export function getUserList(params) {
  return request.get('/users', { params })
}

export function getUserById(id) {
  return request.get(`/users/${id}`)
}

export function createUser(data) {
  return request.post('/users', data)
}

export function updateUser(data) {
  return request.put('/users', data)
}

export function deleteUser(id) {
  return request.delete(`/users/${id}`)
}

export function exportUsers(params) {
  return request.get('/users/export', {
    params,
    responseType: 'blob'
  })
}

export function importUsers(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/users/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
