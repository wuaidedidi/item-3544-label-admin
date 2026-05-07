import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const recentMessages = new Set()

const showError = (message) => {
  if (!message || recentMessages.has(message)) return
  recentMessages.add(message)
  setTimeout(() => recentMessages.delete(message), 2000)
  ElMessage.error({ message, grouping: true })
}

const service = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

service.interceptors.response.use(
  (response) => {
    const res = response.data

    if (res.code === undefined) {
      return res
    }

    if (res.code !== 200) {
      showError(res.message || '操作失败')
      const error = new Error(res.message || '操作失败')
      error._isBusinessError = true
      return Promise.reject(error)
    }

    return res
  },
  (error) => {
    if (error._isBusinessError) {
      return Promise.reject(error)
    }

    if (error.response) {
      const { status, data } = error.response
      let message = ''

      if (data && data.message) {
        message = data.message
      } else {
        switch (status) {
          case 401:
            message = '登录已过期，请重新登录'
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            router.push('/login')
            break
          case 403:
            message = '没有操作权限'
            break
          case 404:
            message = '请求的资源不存在'
            break
          case 500:
            message = '服务器错误，请稍后重试'
            break
          default:
            message = '请求失败'
        }
      }

      if (status === 401 && !message.includes('登录')) {
        message = '登录已过期，请重新登录'
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        router.push('/login')
      }

      showError(message)
    } else if (error.code === 'ERR_NETWORK') {
      showError('网络错误，请检查网络连接')
    }

    return Promise.reject(error)
  }
)

export default service
