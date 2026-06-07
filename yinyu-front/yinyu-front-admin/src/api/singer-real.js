import request from '../utils/request'

export function listSingers(params = {}) {
  return request({
    url: '/admin/singers',
    method: 'get',
    params,
  })
}

export function createSinger(data) {
  return request({
    url: '/admin/singers',
    method: 'post',
    data,
  })
}

export function updateSinger(data) {
  return request({
    url: '/admin/singers',
    method: 'put',
    data,
  })
}

export function deleteSinger(id) {
  return request({
    url: `/admin/singers/${id}`,
    method: 'delete',
  })
}

export function updateSingerStatus(ids, status) {
  return request({
    url: '/admin/singers/status',
    method: 'put',
    data: { ids, status },
  })
}

export function uploadSingerAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/singers/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}
