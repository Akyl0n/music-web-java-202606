import request from '../utils/request'

export function listUsers(params = {}) {
  return request({
    url: '/admin/users',
    method: 'get',
    params,
  })
}

export function getUserDetail(id) {
  return request({
    url: `/admin/users/${id}`,
    method: 'get',
  })
}

export function updateUser(data) {
  return request({
    url: '/admin/users',
    method: 'put',
    data,
  })
}

export function updateUserStatus(ids, status) {
  return request({
    url: '/admin/users/status',
    method: 'put',
    data: { ids, status },
  })
}

export function resetUserPassword(id, newPassword) {
  return request({
    url: '/admin/users/reset-password',
    method: 'put',
    data: { id, newPassword },
  })
}

export function uploadUserAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/users/avatar',
    method: 'post',
    data: formData,
  })
}
