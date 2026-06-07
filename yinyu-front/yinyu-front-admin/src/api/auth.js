import request from '../utils/request'

export function getAdminCaptcha() {
  return request({
    url: '/admin/auth/captcha',
    method: 'get',
  })
}

export function adminLogin(data) {
  return request({
    url: '/admin/auth/login',
    method: 'post',
    data,
  })
}

export function getAdminCurrent() {
  return request({
    url: '/admin/auth/current',
    method: 'get',
  })
}

export function adminLogout() {
  return request({
    url: '/admin/auth/logout',
    method: 'post',
  })
}
