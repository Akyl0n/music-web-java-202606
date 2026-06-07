<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUserLibrary, likeSong } from '../../api/user'
import { usePlayerStore } from '../../stores/player'
import { useUserStore } from '../../stores/user'
import { playByUserGesture } from '../../utils/player-bridge'
import { buildSongMediaUrl } from '../../utils/song-media'

const router = useRouter()
const playerStore = usePlayerStore()
const userStore = useUserStore()

const tabs = ['喜欢歌曲', '最近播放', '收藏歌单']
const activeTab = ref('喜欢歌曲')
const loading = ref(false)
const library = ref(createDefaultLibrary())

const statsCards = computed(() => [
  { label: '喜欢歌曲', value: library.value.likeSongCount },
  { label: '收藏歌单', value: library.value.favoritePlaylistCount },
  { label: '最近播放', value: library.value.playHistoryCount },
])

const currentSongId = computed(() => String(playerStore.currentSong?.id ?? ''))

const songList = computed(() => {
  if (activeTab.value === '最近播放') {
    return library.value.recentSongs
  }
  return library.value.likedSongs
})

const playlistList = computed(() => library.value.favoritePlaylists)

function createDefaultLibrary() {
  return {
    user: null,
    likeSongCount: 0,
    favoritePlaylistCount: 0,
    playHistoryCount: 0,
    likedSongs: [],
    recentSongs: [],
    favoritePlaylists: [],
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

function normalizeSong(item = {}) {
  return {
    ...item,
    singer: item.singerName || '未知歌手',
    meta: item.tags || item.categoryName || item.category || '精选单曲',
    cover: normalizeFileUrl(item.cover || item.singerAvatar || ''),
    audioUrl: buildSongMediaUrl(item.id, item.audioUrl || ''),
    time: formatDuration(item.durationSeconds),
  }
}

function normalizePlaylist(item = {}) {
  return {
    ...item,
    cover: normalizeFileUrl(item.cover || ''),
    title: item.name,
    desc: item.intro || item.subtitle || '暂无简介',
    meta: `${item.songCount || 0} 首 · 收藏 ${item.favoriteCount || 0}`,
    tag: item.tags || item.categoryName || item.category || '精选歌单',
  }
}

function isCurrentSong(songId) {
  return songId != null && String(songId) === currentSongId.value
}

function resolveAvatarText(name) {
  return String(name || '').trim().slice(0, 1) || '歌'
}

function formatDuration(seconds) {
  if (!seconds && seconds !== 0) {
    return '--:--'
  }
  const minutes = String(Math.floor(seconds / 60)).padStart(2, '0')
  const remainSeconds = String(Math.floor(seconds % 60)).padStart(2, '0')
  return `${minutes}:${remainSeconds}`
}

async function fetchLibrary() {
  if (!userStore.isLoggedIn) {
    library.value = createDefaultLibrary()
    return
  }
  loading.value = true
  try {
    const data = await getUserLibrary()
    library.value = {
      ...createDefaultLibrary(),
      ...data,
      likedSongs: (data?.likedSongs || []).map(normalizeSong),
      recentSongs: (data?.recentSongs || []).map(normalizeSong),
      favoritePlaylists: (data?.favoritePlaylists || []).map(normalizePlaylist),
    }
  } catch (error) {
    ElMessage.error(error.message || '获取我的音乐失败')
  } finally {
    loading.value = false
  }
}

async function playSong(item) {
  const queue = songList.value
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
    fetchLibrary()
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

function openPlaylist(item) {
  router.push({ path: '/playlist', query: { q: item.title } })
}

watch(
  () => userStore.currentUser?.id,
  () => {
    fetchLibrary()
  },
)

watch(
  () => playerStore.playHistoryUpdatedAt,
  (value) => {
    if (!value || !userStore.isLoggedIn) {
      return
    }
    fetchLibrary()
  },
)

watch(
  () => activeTab.value,
  (value) => {
    if (value === tabs[1] && userStore.isLoggedIn) {
      fetchLibrary()
    }
  },
)

onMounted(() => {
  fetchLibrary()
})
</script>

<template>
  <div class="container page-section" v-loading="loading">
    <section class="page-hero page-hero-library">
      <div>
        <p class="page-kicker">我的音乐</p>
        <h2 v-if="userStore.isLoggedIn">把喜欢、收藏和最近播放都放在这里，继续听歌会更顺手。</h2>
        <h2 v-else>登录后可以同步你的喜欢歌曲、收藏歌单和最近播放记录。</h2>
        <p class="page-desc">
          {{ userStore.isLoggedIn
            ? `当前账号：${library.user?.nickname || '乐迷'}`
            : '先登录，再把你喜欢的歌曲和歌单慢慢收进自己的音乐仓库。' }}
        </p>
      </div>
      <div class="stats-inline">
        <el-card v-for="item in statsCards" :key="item.label" shadow="never" class="stats-card">
          <strong>{{ item.value }}</strong>
          <span>{{ item.label }}</span>
        </el-card>
      </div>
    </section>

    <el-card v-if="!userStore.isLoggedIn" shadow="never" class="page-card">
      <el-empty description="登录后查看你的音乐内容" :image-size="120">
        <el-button type="primary" @click="userStore.openAuthDialog()">去登录</el-button>
      </el-empty>
    </el-card>

    <template v-else>
      <el-card shadow="never" class="page-card">
        <template #header>
          <div class="section-header">
            <h3 class="section-title">音乐仓库</h3>
            <el-segmented v-model="activeTab" :options="tabs" />
          </div>
        </template>

        <div v-if="activeTab !== '收藏歌单'" class="song-stack">
          <div v-if="songList.length" class="singer-song-table">
            <div class="singer-song-head">
              <span>歌曲</span>
              <span>歌手</span>
              <span>时长</span>
              <span>操作</span>
            </div>

            <button
              v-for="(item, index) in songList"
              :key="item.id"
              type="button"
              :class="['singer-song-row', { 'is-playing': isCurrentSong(item.id) }]"
              @click="playSong(item)"
            >
              <div class="singer-song-main">
                <span class="singer-song-order">{{ index + 1 }}</span>
                <div class="singer-song-cover-wrap">
                  <el-image
                    v-if="item.cover"
                    :src="item.cover"
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
          <el-empty v-else :description="activeTab === '喜欢歌曲' ? '还没有喜欢的歌曲' : '还没有播放记录'" :image-size="100" />
        </div>

        <div v-else class="playlist-grid playlist-grid-page playlist-grid-five">
          <template v-if="playlistList.length">
            <el-card
              v-for="item in playlistList"
              :key="item.id"
              shadow="hover"
              class="playlist-card is-clickable"
              @click="openPlaylist(item)"
            >
              <div
                class="playlist-cover"
                :style="item.cover ? {
                  backgroundImage: `url('${item.cover}')`,
                  backgroundSize: 'cover',
                  backgroundPosition: 'center',
                } : {}"
              >
                <span class="playlist-tag">{{ item.tag }}</span>
              </div>
              <div class="playlist-content">
                <h4>{{ item.title }}</h4>
                <p>{{ item.desc }}</p>
                <div class="playlist-meta">{{ item.meta }}</div>
              </div>
            </el-card>
          </template>
          <el-empty v-else description="还没有收藏的歌单" :image-size="100" />
        </div>
      </el-card>
    </template>
  </div>
</template>
