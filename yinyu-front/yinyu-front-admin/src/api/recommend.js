import request from '../utils/request'

export function listHomeRecommends(params = {}) {
  return request({
    url: '/admin/home-recommends',
    method: 'get',
    params,
  })
}

export function getHomeRecommendDetail(id) {
  return request({
    url: `/admin/home-recommends/${id}`,
    method: 'get',
  })
}

export function createHomeRecommend(data) {
  return request({
    url: '/admin/home-recommends',
    method: 'post',
    data,
  })
}

export function updateHomeRecommend(data) {
  return request({
    url: '/admin/home-recommends',
    method: 'put',
    data,
  })
}

export function deleteHomeRecommend(id) {
  return request({
    url: `/admin/home-recommends/${id}`,
    method: 'delete',
  })
}

export function updateHomeRecommendStatus(ids, status) {
  return request({
    url: '/admin/home-recommends/status',
    method: 'put',
    data: { ids, status },
  })
}

export function sortHomeRecommends(ids) {
  return request({
    url: '/admin/home-recommends/sort',
    method: 'put',
    data: { ids },
  })
}

export function listHomeRecommendTargetOptions(targetType) {
  return request({
    url: '/admin/home-recommends/options',
    method: 'get',
    params: { targetType },
  })
}

export function uploadHomeRecommendCover(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/admin/home-recommends/cover',
    method: 'post',
    data: formData,
  })
}
