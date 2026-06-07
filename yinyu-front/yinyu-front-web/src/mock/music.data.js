export const mainMenus = [
  { label: '音乐馆', path: '/' },
  { label: '我的音乐', path: '/my-music' },
  { label: '歌单', path: '/playlist' },
  { label: '排行榜', path: '/ranking' },
  { label: '歌手', path: '/singers' },
]

export const bannerSlides = [
  {
    id: 'banner-1',
    tag: '古风新声 · 首页推荐',
    title: '更像 QQ 音乐的古风首页，清爽、规整、好浏览',
    desc: '以古风音乐为内容核心，把首页组织成更接近成熟音乐平台的信息结构，既保留古意，也更适合持续扩展。',
    actions: ['播放推荐', '查看歌单'],
  },
  {
    id: 'banner-2',
    tag: '戏腔专题 · 本周精选',
    title: '戏腔、念白与流行编曲同场出现，适合反复循环',
    desc: '把传统唱腔的辨识度保留下来，同时兼顾现代编曲和场景化聆听体验。',
    actions: ['进入专题', '试听歌单'],
  },
]

export const trendList = [
  { no: '1', name: '春庭雪', sub: '古风女声 · 意境抒情', hot: '热度 +98%' },
  { no: '2', name: '牵丝戏', sub: '戏腔经典 · 情绪浓烈', hot: '热议' },
  { no: '3', name: '不谓侠', sub: '侠气燃向 · 热门翻唱', hot: '新上榜' },
]

export const playlistTabs = ['为你推荐', '江湖武侠', '戏腔国风', '轻古风', '纯音乐']

export const playlists = [
  {
    id: 'pl-1',
    tag: '细雨江南',
    title: '烟雨江南',
    desc: '适合细雨、夜读与独处时循环播放的温柔古风歌单。',
    meta: '32 首 · 2.8 万收藏',
    cover: 'linear-gradient(135deg, #1f9a60, #75e0ae)',
    category: '为你推荐',
  },
  {
    id: 'pl-2',
    tag: '江湖热血',
    title: '剑影天涯',
    desc: '偏侠气与江湖感的热血国风，适合通勤与运动。',
    meta: '28 首 · 1.9 万收藏',
    cover: 'linear-gradient(135deg, #28a86a, #89e5b8)',
    category: '江湖武侠',
  },
  {
    id: 'pl-3',
    tag: '酒意诗心',
    title: '花间一壶酒',
    desc: '古风吟唱、笛箫与弦乐结合，安静夜晚很适合循环。',
    meta: '24 首 · 1.2 万收藏',
    cover: 'linear-gradient(135deg, #189159, #66d69d)',
    category: '戏腔国风',
  },
  {
    id: 'pl-4',
    tag: '梦境纯音',
    title: '浮生若梦',
    desc: '古典纯音乐与柔和吟唱结合，适合专注、冥想与放松。',
    meta: '40 首 · 3.6 万收藏',
    cover: 'linear-gradient(135deg, #39ba79, #9ae8c4)',
    category: '纯音乐',
  },
  {
    id: 'pl-5',
    tag: '夜色微凉',
    title: '灯火长街',
    desc: '带一点浪漫与旧城烟火气，适合散步和安静夜听。',
    meta: '30 首 · 2.1 万收藏',
    cover: 'linear-gradient(135deg, #208f59, #63d698)',
    category: '轻古风',
  },
  {
    id: 'pl-6',
    tag: '月下轻吟',
    title: '子夜清歌',
    desc: '清冷人声和埙、笛组合，适合深夜戴耳机慢慢听。',
    meta: '18 首 · 8800 收藏',
    cover: 'linear-gradient(135deg, #2a9b63, #74e0aa)',
    category: '为你推荐',
  },
]

export const chartTabs = ['实时热播', '新歌榜', '收藏榜']

export const songs = [
  { id: 'song-1', no: '01', name: '春庭雪', desc: '细腻女声，带有雪夜独白的画面感', meta: '古风女声 · 意境抒情', time: '03:48', singer: '晚舟', playlistId: 'pl-1', chart: '实时热播' },
  { id: 'song-2', no: '02', name: '半城烟沙', desc: '国风经典，江湖叙事感很强', meta: '国风经典 · 史诗叙事', time: '04:12', singer: '墨尘', playlistId: 'pl-2', chart: '实时热播' },
  { id: 'song-3', no: '03', name: '不谓侠', desc: '侠气十足，鼓点与弦乐张力鲜明', meta: '侠气燃向 · 热门翻唱', time: '03:31', singer: '墨尘', playlistId: 'pl-2', chart: '实时热播' },
  { id: 'song-4', no: '04', name: '牵丝戏', desc: '戏腔经典，旋律辨识度极高', meta: '戏腔经典 · 情绪浓烈', time: '04:25', singer: '清辞', playlistId: 'pl-3', chart: '收藏榜' },
  { id: 'song-5', no: '05', name: '谓风', desc: '仙侠气质明显，适合剧情向听感', meta: '仙侠国风 · 剧情向', time: '04:06', singer: '云笙', playlistId: 'pl-5', chart: '新歌榜' },
  { id: 'song-6', no: '06', name: '山月不知心底事', desc: '古筝铺底，适合夜晚单曲循环', meta: '古风精选 · 夜读向', time: '03:48', singer: '晚舟', playlistId: 'pl-6', chart: '收藏榜' },
  { id: 'song-7', no: '07', name: '松烟入墨', desc: '器乐与环境采样结合，更适合专注场景', meta: '纯音乐 · 氛围向', time: '05:01', singer: '云笙', playlistId: 'pl-4', chart: '新歌榜' },
]

export const singers = [
  { id: 'singer-1', name: '清辞', desc: '擅长唯美古风与轻戏腔', count: '月听众 82 万', avatar: 'linear-gradient(135deg, #1f9a60, #75e0ae)', style: '戏腔国风' },
  { id: 'singer-2', name: '墨尘', desc: '偏侠气男声与剧情歌', count: '月听众 69 万', avatar: 'linear-gradient(135deg, #28a86a, #89e5b8)', style: '江湖武侠' },
  { id: 'singer-3', name: '云笙', desc: '擅长古典纯音与配乐', count: '月听众 51 万', avatar: 'linear-gradient(135deg, #189159, #66d69d)', style: '纯音乐' },
  { id: 'singer-4', name: '晚舟', desc: '清冷女声，适合夜听', count: '月听众 77 万', avatar: 'linear-gradient(135deg, #37b877, #98e8c3)', style: '轻古风' },
]

export const categoryTabs = ['全部', '学习专注', '深夜循环', '旅行通勤']

export const categories = [
  { title: '夜读书房', desc: '低干扰古风纯音与轻吟唱，适合学习、写字、阅读时播放。', count: '16 张精选歌单', scene: '学习专注' },
  { title: '江湖出行', desc: '更偏节奏感和侠气氛围，适合通勤、运动或外出时打开。', count: '9 张精选歌单', scene: '旅行通勤' },
  { title: '花灯长街', desc: '温柔浪漫的国风旋律，适合夜晚散步与放松聆听。', count: '13 张精选歌单', scene: '深夜循环' },
  { title: '山水入眠', desc: '静谧治愈向歌单，强调环境感、空灵感和更柔和的听感。', count: '21 张精选歌单', scene: '深夜循环' },
]

export const libraryCards = [
  { title: '我喜欢', value: '126 首', desc: '最近新增 8 首', tone: 'warm' },
  { title: '最近播放', value: '42 首', desc: '过去 7 天更新', tone: 'dark' },
  { title: '收藏歌单', value: '18 个', desc: '按情绪和场景整理', tone: 'light' },
]

export const statsCards = [
  { label: '本周播放', value: '286 次' },
  { label: '收藏歌手', value: '14 位' },
  { label: '云端歌单', value: '36 张' },
]

export function getPlaylistById(id) {
  return playlists.find((item) => item.id === id)
}

export function getSongsByPlaylist(playlistId) {
  return songs.filter((item) => item.playlistId === playlistId)
}

export function searchAll(keyword) {
  const text = keyword.trim()
  if (!text) {
    return { songs: [], playlists: [], singers: [] }
  }

  return {
    songs: songs.filter((item) => `${item.name}${item.desc}${item.meta}${item.singer}`.includes(text)),
    playlists: playlists.filter((item) => `${item.title}${item.desc}${item.tag}`.includes(text)),
    singers: singers.filter((item) => `${item.name}${item.desc}${item.style}`.includes(text)),
  }
}
