import request from '../utils/request'

export function getFileList(params) {
  return request.get('/files', { params })
}

export function uploadFile(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function uploadFiles(files) {
  const formData = new FormData()
  files.forEach(f => formData.append('files', f))
  return request.post('/files/upload/batch', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function downloadFile(id) {
  return request.get(`/files/download/${id}`, { responseType: 'blob' })
}

export function batchDownloadFiles(ids) {
  return request.post('/files/download/batch', ids, { responseType: 'blob' })
}

export function deleteFile(id) {
  return request.delete(`/files/${id}`)
}

export function exportFiles(params) {
  return request.get('/files/export', {
    params,
    responseType: 'blob'
  })
}
