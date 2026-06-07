import axios from 'axios'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true,
})

service.interceptors.response.use(
  (response) => {
    const payload = response.data
    if (payload?.code === 200) {
      return payload.data
    }
    return Promise.reject(new Error(payload?.info || '请求失败'))
  },
  (error) => Promise.reject(error),
)

export default service
