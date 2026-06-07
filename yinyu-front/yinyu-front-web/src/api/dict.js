import request from '../utils/request'

export function listWebDictTypes(params = {}) {
  return request({
    url: '/web/dicts/types',
    method: 'get',
    params,
  })
}

export function listWebDictItems(params = {}) {
  return request({
    url: '/web/dicts/items',
    method: 'get',
    params,
  })
}

export function listWebDictTree(params = {}) {
  return request({
    url: '/web/dicts/tree',
    method: 'get',
    params,
  })
}
