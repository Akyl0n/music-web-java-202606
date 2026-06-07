<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { listWebPlaylists } from '../../api/playlist'
import { listWebSingers } from '../../api/singer'
import { listWebSongs } from '../../api/song'
import { likeSong } from '../../api/user'
import { usePlayerStore } from '../../stores/player'
import { useUserStore } from '../../stores/user'
import { playByUserGesture } from '../../utils/player-bridge'
import { buildSongMediaUrl } from '../../utils/song-media'

const route = useRoute()
const router = useRouter()
const playerStore = usePlayerStore()
const userStore = useUserStore()

const loading = ref(false)
const activeType = ref('all')
const searchResult = ref(createDefaultResult())

const keyword = computed(() => String(route.query.q || '').trim())
const currentSongId = computed(() => String(playerStore.currentSong?.id ?? ''))
const typeSummaryOptions = computed(() => [
  { label: '全部', value: 'all', count: searchResult.value.total },
  { label: '歌曲', value: 'songs', count: searchResult.value.songs.length },
  { label: '歌单', value: 'playlists', count: searchResult.value.playlists.length },
  { label: '歌手', value: 'singers', count: searchResult.value.singers.length },
])

const hasKeyword = computed(() => Boolean(keyword.value))
const hasAnyResult = computed(() => searchResult.value.total > 0)
const visibleSongs = computed(() => (activeType.value === 'all' ? searchResult.value.songs.slice(0, 12) : searchResult.value.songs))
const visiblePlaylists = computed(() => (activeType.value === 'all' ? searchResult.value.playlists.slice(0, 10) : searchResult.value.playlists))
const visibleSingers = computed(() => (activeType.value === 'all' ? searchResult.value.singers.slice(0, 10) : searchResult.value.singers))

function createDefaultResult() {
  return {
    songs: [],
    playlists: [],
    singers: [],
    total: 0,
  }
}

function normalizeFileUrl(url) {
  if (!url) {
    return ''
  }
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/api/')) {
    return url
  }
  return url.startsWith('/') ? `/api${url}` : `/api/${url}`
}

function formatDuration(seconds) {
  if (!seconds && seconds !== 0) {
    return '--:--'
  }
  const minutes = String(Math.floor(seconds / 60)).padStart(2, '0')
  const remainSeconds = String(Math.floor(seconds % 60)).padStart(2, '0')
  return `${minutes}:${remainSeconds}`
}

function normalizeSong(item = {}, index = 0) {
  return {
    ...item,
    no: String(index + 1).padStart(2, '0'),
    singer: item.singerName || '未知歌手',
    cover: normalizeFileUrl(item.cover || ''),
    singerAvatar: normalizeFileUrl(item.singerAvatar || ''),
    meta: item.tags || item.categoryName || item.category || '精选单曲',
    time: formatDuration(item.durationSeconds),
    audioUrl: buildSongMediaUrl(item.id, item.audioUrl || ''),
  }
}

function normalizePlaylist(item = {}) {
  return {
    ...item,
    cover: normalizeFileUrl(item.cover || ''),
    tagsText: String(item.tags || '')
      .split(',')
      .map((value) => value.trim())
      .filter(Boolean)
      .join(' / '),
    introText: item.intro || item.subtitle || '暂无简介',
    tagText: String(item.tags || '')
      .split(',')
      .map((value) => value.trim())
      .filter(Boolean)[0] || item.categoryName || item.category || '精选歌单',
  }
}

function normalizeSinger(item = {}) {
  return {
    ...item,
    avatar: normalizeFileUrl(item.avatar || ''),
    avatarText: resolveAvatarText(item.name),
    tagsText: String(item.tags || '')
      .split(',')
      .map((value) => value.trim())
      .filter(Boolean)
      .join(' / '),
    introText: item.intro || item.region || '古风歌者',
  }
}

function resolveAvatarText(name) {
  return String(name || '').trim().slice(0, 1) || '歌'
}

function isCurrentSong(songId) {
  return songId != null && String(songId) === currentSongId.value
}

function resolveSongCover(item) {
  return item.cover || item.singerAvatar || ''
}

async function fetchSearchResult() {
  if (!keyword.value) {
    searchResult.value = createDefaultResult()
    return
  }
  loading.value = true
  try {
    const [songData, playlistData, singerData] = await Promise.all([
      listWebSongs({ pageNo: 1, pageSize: 30, keyword: keyword.value }),
      listWebPlaylists({ pageNo: 1, pageSize: 20, keyword: keyword.value }),
      listWebSingers({ pageNo: 1, pageSize: 20, keyword: keyword.value }),
    ])

    const songs = (songData?.list || []).map((item, index) => normalizeSong(item, index))
    const playlists = (playlistData?.list || []).map(normalizePlaylist)
    const singers = (singerData?.list || []).map(normalizeSinger)
    searchResult.value = {
      songs,
      playlists,
      singers,
      total: songs.length + playlists.length + singers.length,
    }
  } finally {
    loading.value = false
  }
}

async function playSong(item) {
  const queue = searchResult.value.songs
  const played = await playByUserGesture(item.id, queue)
  if (!played) {
    playerStore.playSong(item.id, queue)
  }
}

async function handleLikeSong(songId) {
  if (!userStore.isLoggedIn) {
    userStore.openAuthDialog()
    return
  }
  try {
    await likeSong({ songId })
    ElMessage.success('已加入喜欢歌曲')
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

function openPlaylist(id) {
  router.push(`/playlist/${id}`)
}

function openSinger(id) {
  router.push(`/singers/${id}`)
}

watch(
  () => route.query.q,
  () => {
    fetchSearchResult()
  },
)

onMounted(() => {
  fetchSearchResult()
})
</script>

<template>
  <div class="container page-section" v-loading="loading">
    <el-card shadow="never" class="page-card search-switch-card">
      <div class="section-header">
        <h3 class="section-title">
          {{ hasKeyword ? `“${keyword}” 的搜索结果` : '搜索结果' }}
        </h3>
        <div class="search-type-switch" role="tablist" aria-label="搜索结果类型">
          <button
            v-for="item in typeSummaryOptions"
            :key="item.value"
            type="button"
            :class="['search-type-pill', { 'is-active': activeType === item.value }]"
            @click="activeType = item.value"
          >
            <span>{{ item.label }}</span>
            <strong>{{ item.count }}</strong>
          </button>
        </div>
      </div>
    </el-card>

    <el-card v-if="!hasKeyword" shadow="never" class="page-card search-empty-card">
      <el-empty description="先输入关键词，再开始搜索" :image-size="120" />
    </el-card>

    <el-card v-else-if="!hasAnyResult" shadow="never" class="page-card search-empty-card">
      <el-empty :description="`没有找到与“${keyword}”相关的内容`" :image-size="120" />
    </el-card>

    <template v-else>
      <el-card v-if="activeType === 'all' || activeType === 'songs'" shadow="never" class="page-card search-section">
        <template #header>
          <div class="section-header">
            <h3 class="section-title">歌曲</h3>
            <span class="singer-song-count">共 {{ searchResult.songs.length }} 首</span>
          </div>
        </template>

        <div v-if="visibleSongs.length" class="singer-song-table">
          <div class="singer-song-head">
            <span>歌曲</span>
            <span>歌手</span>
            <span>时长</span>
            <span>操作</span>
          </div>

          <button
            v-for="(item, index) in visibleSongs"
            :key="item.id"
            type="button"
            :class="['singer-song-row', { 'is-playing': isCurrentSong(item.id) }]"
            @click="playSong(item)"
          >
            <div class="singer-song-main">
              <span class="singer-song-order">{{ index + 1 }}</span>
              <div class="singer-song-cover-wrap">
                <el-image
                  v-if="resolveSongCover(item)"
                  :src="resolveSongCover(item)"
                  fit="cover"
                  class="singer-song-cover"
                />
                <span v-else class="singer-song-cover-fallback">{{ resolveAvatarText(item.name) }}</span>
              </div>
              <div class="singer-song-info">
                <strong :title="item.name">
                  <span>{{ item.name }}</span>
                  <span v-if="isCurrentSong(item.id)" class="inline-playing-indicator" aria-hidden="true">
                    <i></i>
                    <i></i>
                    <i></i>
                  </span>
                </strong>
                <p :title="item.meta">{{ item.meta }}</p>
              </div>
            </div>
            <span class="singer-song-album">{{ item.singer }}</span>
            <span class="singer-song-time">{{ item.time }}</span>
            <div class="singer-song-action-group">
              <button class="playlist-icon-btn" type="button" title="播放歌曲" @click.stop="playSong(item)">
                <i class="iconfont icon-play"></i>
              </button>
              <button class="playlist-icon-btn" type="button" title="喜欢歌曲" @click.stop="handleLikeSong(item.id)">
                <i class="iconfont icon-like"></i>
              </button>
            </div>
          </button>
        </div>
        <el-empty v-else description="没有匹配到歌曲结果" :image-size="100" />
      </el-card>

      <el-card v-if="activeType === 'all' || activeType === 'playlists'" shadow="never" class="page-card search-section">
        <template #header>
          <div class="section-header">
            <h3 class="section-title">歌单</h3>
            <span class="singer-song-count">共 {{ searchResult.playlists.length }} 张</span>
          </div>
        </template>

        <div v-if="visiblePlaylists.length" class="playlist-grid playlist-grid-page playlist-grid-five">
          <el-card
            v-for="item in visiblePlaylists"
            :key="item.id"
            shadow="hover"
            class="playlist-card"
          >
            <div
              class="playlist-cover is-clickable"
              :style="item.cover ? {
                backgroundImage: `url('${item.cover}')`,
                backgroundSize: 'cover',
                backgroundPosition: 'center',
              } : {}"
              @click="openPlaylist(item.id)"
            >
              <span class="playlist-tag" :title="item.tagText">{{ item.tagText }}</span>
            </div>
            <div class="playlist-content">
              <h4 :title="item.name">{{ item.name }}</h4>
              <p :title="item.introText">{{ item.introText }}</p>
              <div class="playlist-meta">{{ item.songCount || 0 }} 首 · 收藏 {{ item.favoriteCount || 0 }}</div>
            </div>
            <div class="card-actions playlist-card-actions">
              <el-button type="primary" round @click="openPlaylist(item.id)">查看详情</el-button>
            </div>
          </el-card>
        </div>
        <el-empty v-else description="没有匹配到歌单结果" :image-size="100" />
      </el-card>

      <el-card v-if="activeType === 'all' || activeType === 'singers'" shadow="never" class="page-card search-section">
        <template #header>
          <div class="section-header">
            <h3 class="section-title">歌手</h3>
            <span class="singer-song-count">共 {{ searchResult.singers.length }} 位</span>
          </div>
        </template>

        <div v-if="visibleSingers.length" class="singer-grid singer-grid-page singer-grid-web search-singer-grid">
          <div
            v-for="item in visibleSingers"
            :key="item.id"
            class="singer-showcase"
            @click="openSinger(item.id)"
          >
            <el-image v-if="item.avatar" :src="item.avatar" fit="cover" class="avatar singer-showcase-avatar" />
            <div v-else class="avatar singer-showcase-avatar singer-showcase-avatar-fallback">
              {{ item.avatarText }}
            </div>
            <div class="singer-showcase-name" :title="item.name">{{ item.name }}</div>
            <p class="search-singer-meta" :title="item.tagsText || item.introText">{{ item.tagsText || item.introText }}</p>
          </div>
        </div>
        <el-empty v-else description="没有匹配到歌手结果" :image-size="100" />
      </el-card>
    </template>
  </div>
</template>
