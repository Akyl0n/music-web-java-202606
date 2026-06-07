import axios from 'axios'
import router from '../router'

const STORAGE_KEY = 'yinyu-admin-session'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

service.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload?.code === 200) {
      return payload.data
    }
    if (payload?.code === 401) {
      localStorage.removeItem(STORAGE_KEY)
      if (router.currentRoute.value.path !== '/login') {
        router.replace({ path: '/login', query: { redirect: router.currentRoute.value.fullPath } })
      }
    }
    return Promise.reject(new Error(payload?.info || '请求失败'))
  },
  (error) => Promise.reject(error)
)

export default service
