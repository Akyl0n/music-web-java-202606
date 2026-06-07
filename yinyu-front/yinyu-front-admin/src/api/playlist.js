import request from '../utils/request'

export function listPlaylists(params = {}) {
  return request({
    url: '/admin/playlists',
    method: 'get',
    params,
  })
}

export function getPlaylistDetail(id) {
  return request({
    url: `/admin/playlists/${id}`,
    method: 'get',
  })
}

export function createPlaylist(data) {
  return request({
    url: '/admin/playlists',
    method: 'post',
    data,
  })
}

export function updatePlaylist(data) {
  return request({
    url: '/admin/playlists',
    method: 'put',
    data,
  })
}

export function deletePlaylist(id) {
  return request({
    url: `/admin/playlists/${id}`,
    method: 'delete',
  })
}

export function updatePlaylistStatus(ids, status) {
  return request({
    url: '/admin/playlists/status',
    method: 'put',
    data: { ids, status },
  })
}

export function uploadPlaylistCover(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/playlists/cover',
    method: 'post',
    data: formData,
  })
}
