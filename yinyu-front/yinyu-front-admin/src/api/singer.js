let singerSeed = [
  {
    id: 1,
    name: '晚舟',
    style: '古风女声',
    representative: '春庭雪',
    worksCount: 28,
    monthlyListeners: 770000,
    status: 'active',
    sort: 10,
    avatarUrl: 'https://example.com/avatar/wanzhou.jpg',
    intro: '清冷女声，适合夜听和抒情古风作品。',
    updatedAt: '2026-04-17 10:20:00',
  },
  {
    id: 2,
    name: '清辞',
    style: '戏腔国风',
    representative: '牵丝戏',
    worksCount: 34,
    monthlyListeners: 820000,
    status: 'active',
    sort: 20,
    avatarUrl: 'https://example.com/avatar/qingci.jpg',
    intro: '擅长唯美古风与轻戏腔，声线辨识度高。',
    updatedAt: '2026-04-17 09:40:00',
  },
  {
    id: 3,
    name: '墨尘',
    style: '江湖武侠',
    representative: '不谓侠',
    worksCount: 21,
    monthlyListeners: 690000,
    status: 'active',
    sort: 30,
    avatarUrl: 'https://example.com/avatar/mochen.jpg',
    intro: '偏侠气男声与剧情歌，适合燃向场景。',
    updatedAt: '2026-04-16 18:00:00',
  },
  {
    id: 4,
    name: '云笙',
    style: '纯音乐',
    representative: '松烟入墨',
    worksCount: 15,
    monthlyListeners: 510000,
    status: 'inactive',
    sort: 40,
    avatarUrl: 'https://example.com/avatar/yunsheng.jpg',
    intro: '擅长古典纯音与器乐配乐。',
    updatedAt: '2026-04-15 16:15:00',
  },
]

function clone(value) {
  return JSON.parse(JSON.stringify(value))
}

export function listSingers(params) {
  const {
    keyword = '',
    style = '',
    status = '',
    pageNo = 1,
    pageSize = 10,
  } = params

  const filtered = singerSeed.filter((item) => {
    const matchKeyword = keyword ? `${item.name}${item.representative}${item.intro}`.includes(keyword) : true
    const matchStyle = style ? item.style === style : true
    const matchStatus = status ? item.status === status : true
    return matchKeyword && matchStyle && matchStatus
  })

  const start = (pageNo - 1) * pageSize
  return Promise.resolve({
    list: clone(filtered.slice(start, start + pageSize)),
    total: filtered.length,
  })
}

export function createSinger(payload) {
  const nextId = singerSeed.length ? Math.max(...singerSeed.map((item) => item.id)) + 1 : 1
  singerSeed = [
    {
      ...payload,
      id: nextId,
      worksCount: payload.worksCount ?? 0,
      monthlyListeners: payload.monthlyListeners ?? 0,
      updatedAt: new Date().toLocaleString('sv-SE').replace('T', ' '),
    },
    ...singerSeed,
  ]
  return Promise.resolve()
}

export function updateSinger(payload) {
  singerSeed = singerSeed.map((item) =>
    item.id === payload.id
      ? { ...item, ...payload, updatedAt: new Date().toLocaleString('sv-SE').replace('T', ' ') }
      : item
  )
  return Promise.resolve()
}

export function deleteSinger(id) {
  singerSeed = singerSeed.filter((item) => item.id !== id)
  return Promise.resolve()
}

export function updateSingerStatus(ids, status) {
  singerSeed = singerSeed.map((item) =>
    ids.includes(item.id)
      ? { ...item, status, updatedAt: new Date().toLocaleString('sv-SE').replace('T', ' ') }
      : item
  )
  return Promise.resolve()
}
