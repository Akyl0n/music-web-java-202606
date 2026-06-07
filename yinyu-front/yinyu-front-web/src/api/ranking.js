import request from '../utils/request'

export function listWebRankings() {
  return request({
    url: '/web/rankings',
    method: 'get',
  })
}

export function getWebRankingDetail(code) {
  return request({
    url: `/web/rankings/${code}`,
    method: 'get',
  })
}
