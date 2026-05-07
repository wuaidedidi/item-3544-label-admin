import request from '../utils/request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function getSM2PublicKey() {
  return request.get('/auth/sm2/public-key')
}
