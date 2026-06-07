import request from '../utils/request'

export function getHomePage() {
  return request({
    url: '/web/home',
    method: 'get',
  })
}
