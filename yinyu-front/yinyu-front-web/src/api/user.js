import request from '../utils/request'

export function getCaptcha() {
  return request({
    url: '/web/users/captcha',
    method: 'get',
  })
}

export function registerUser(data) {
  return request({
    url: '/web/users/register',
    method: 'post',
    data,
  })
}

export function loginUser(data) {
  return request({
    url: '/web/users/login',
    method: 'post',
    data,
  })
}

export function logoutUser() {
  return request({
    url: '/web/users/logout',
    method: 'post',
  })
}

export function getCurrentUser() {
  return request({
    url: '/web/users/current',
    method: 'get',
  })
}

export function getUserLibrary() {
  return request({
    url: '/web/users/library',
    method: 'get',
  })
}

export function updateUserProfile(data) {
  return request({
    url: '/web/users/profile',
    method: 'put',
    data,
  })
}

export function updateUserPassword(data) {
  return request({
    url: '/web/users/password',
    method: 'put',
    data,
  })
}

export function uploadUserAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/web/users/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export function likeSong(data) {
  return request({
    url: '/web/users/actions/songs/like',
    method: 'post',
    data,
  })
}

export function favoritePlaylist(data) {
  return request({
    url: '/web/users/actions/playlists/favorite',
    method: 'post',
    data,
  })
}

export function reportPlaySong(data) {
  return request({
    url: '/web/users/actions/songs/play',
    method: 'post',
    data,
  })
}
