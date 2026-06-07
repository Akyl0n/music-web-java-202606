import request from '../utils/request'

function mapItem(row) {
  return {
    ...row,
    typeId: row.parentId,
  }
}

export function listDictTypes(params = {}) {
  return request({
    url: '/admin/dicts/types',
    method: 'get',
    params,
  })
}

export function listDictItems(params) {
  return request({
    url: '/admin/dicts/items',
    method: 'get',
    params: {
      parentId: params?.typeId ?? params?.parentId,
      keyword: params?.keyword,
    },
  }).then((data) => ({
    ...data,
    list: Array.isArray(data?.list) ? data.list.map(mapItem) : [],
  }))
}

export function listDictItemsByCode(code) {
  return request({
    url: '/admin/dicts/items/options',
    method: 'get',
    params: { code },
  }).then((data) => ({
    ...data,
    list: Array.isArray(data?.list) ? data.list.map(mapItem) : [],
  }))
}

export function createDictType(data) {
  return request({
    url: '/admin/dicts/type',
    method: 'post',
    data,
  })
}

export function updateDictType(data) {
  return request({
    url: '/admin/dicts/type',
    method: 'put',
    data,
  })
}

export function deleteDictType(id) {
  return request({
    url: `/admin/dicts/type/${id}`,
    method: 'delete',
  })
}

export function createDictItem(data) {
  return request({
    url: '/admin/dicts/item',
    method: 'post',
    data: {
      ...data,
      parentId: data?.typeId ?? data?.parentId,
    },
  })
}

export function updateDictItem(data) {
  return request({
    url: '/admin/dicts/item',
    method: 'put',
    data: {
      ...data,
      parentId: data?.typeId ?? data?.parentId,
    },
  })
}

export function deleteDictItem(id) {
  return request({
    url: `/admin/dicts/item/${id}`,
    method: 'delete',
  })
}

export function sortDictTypes(ids) {
  return request({
    url: '/admin/dicts/types/sort',
    method: 'put',
    data: { ids },
  })
}

export function sortDictItems(parentId, ids) {
  return request({
    url: '/admin/dicts/items/sort',
    method: 'put',
    data: { parentId, ids },
  })
}
