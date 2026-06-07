import request from '../utils/request'

export function listSongs(params = {}) {
  return request({
    url: '/admin/songs',
    method: 'get',
    params,
  })
}

export function createSong(data) {
  return request({
    url: '/admin/songs',
    method: 'post',
    data,
  })
}

export function updateSong(data) {
  return request({
    url: '/admin/songs',
    method: 'put',
    data,
  })
}

export function deleteSong(id) {
  return request({
    url: `/admin/songs/${id}`,
    method: 'delete',
  })
}

export function updateSongStatus(ids, status) {
  return request({
    url: '/admin/songs/status',
    method: 'put',
    data: { ids, status },
  })
}

export function uploadSongCover(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/songs/cover',
    method: 'post',
    data: formData,
  })
}

export function uploadSongAudio(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/songs/audio',
    method: 'post',
    data: formData,
  })
}
