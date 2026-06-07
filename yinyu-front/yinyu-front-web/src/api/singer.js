import request from '../utils/request'

export function listWebSingers(params = {}) {
  return request({
    url: '/web/singers',
    method: 'get',
    params,
  })
}

export function getWebSingerDetail(id) {
  return request({
    url: `/web/singers/${id}`,
    method: 'get',
  })
}
