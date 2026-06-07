import request from '../utils/request'

export function listWebPlaylists(params = {}) {
  return request({
    url: '/web/playlists',
    method: 'get',
    params,
  })
}

export function getWebPlaylistDetail(id) {
  return request({
    url: `/web/playlists/${id}`,
    method: 'get',
  })
}
