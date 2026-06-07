import request from '../utils/request'

export function listWebSongs(params = {}) {
  return request({
    url: '/web/songs',
    method: 'get',
    params,
  })
}
