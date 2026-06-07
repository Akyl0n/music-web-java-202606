export const topMenus = [
  { label: '首页', path: '/admin/dashboard' },
  { label: '内容管理', path: '/admin/songs' },
  { label: '运营中心', path: '/admin/recommend' },
  { label: '系统管理', path: '/admin/dicts' },
]

export const sideGroups = [
  {
    title: '控制台',
    items: [{ label: '数据看板', path: '/admin/dashboard' }],
  },
  {
    title: '内容管理',
    items: [
      { label: '歌曲管理', path: '/admin/songs' },
      { label: '歌单管理', path: '/admin/playlists' },
      { label: '歌手管理', path: '/admin/singers' },
    ],
  },
  {
    title: '运营管理',
    items: [{ label: '首页推荐', path: '/admin/recommend' }],
  },
  {
    title: '系统',
    items: [
      { label: '字典管理', path: '/admin/dicts' },
      { label: '用户管理', path: '/admin/users' },
    ],
  },
]

export const placeholderMap = {
  songs: {
    title: '歌曲管理',
    desc: '管理歌曲信息、上下架状态、音频资源与审核流程。',
  },
  playlists: {
    title: '歌单管理',
    desc: '维护歌单封面、简介、排序和关联歌曲。',
  },
  singers: {
    title: '歌手管理',
    desc: '管理歌手资料、头像、简介和代表作品。',
  },
  recommend: {
    title: '首页推荐',
    desc: '配置首页轮播图和今日推荐，统一管理首页展示内容。',
  },
  dicts: {
    title: '字典管理',
    desc: '统一维护国家、风格、分类等基础字典数据。',
  },
  users: {
    title: '用户管理',
    desc: '查看用户资料、状态和互动行为数据。',
  },
}
