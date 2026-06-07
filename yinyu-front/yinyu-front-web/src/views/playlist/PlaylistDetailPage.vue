<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getWebPlaylistDetail } from '../../api/playlist'
import { favoritePlaylist, likeSong } from '../../api/user'
import { usePlayerStore } from '../../stores/player'
import { useUserStore } from '../../stores/user'
import { playByUserGesture } from '../../utils/player-bridge'
import { buildSongMediaUrl } from '../../utils/song-media'

const route = useRoute()
const router = useRouter()
const playerStore = usePlayerStore()
const userStore = useUserStore()

const loading = ref(false)
const detail = ref(createDefaultDetail())

const currentSongId = computed(() => String(playerStore.currentSong?.id ?? ''))

function createDefaultDetail() {
  return {
    playlist: null,
    songs: [],
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

function normalizeSong(item = {}, fallbackCover = '') {
  return {
    ...item,
    cover: normalizeFileUrl(item.cover || fallbackCover || ''),
    singerAvatar: normalizeFileUrl(item.singerAvatar || ''),
    singer: item.singerName || '未知歌手',
    meta: item.tags || item.categoryName || item.category || '精选单曲',
    audioUrl: buildSongMediaUrl(item.id, item.audioUrl || ''),
    durationText: formatDuration(item.durationSeconds),
  }
}

function normalizePlaylist(item = {}) {
  return {
    ...item,
    cover: normalizeFileUrl(item.cover || ''),
    introText: item.intro || item.subtitle || '暂无简介',
    tagsText: String(item.tags || '')
      .split(',')
      .map((value) => value.trim())
      .filter(Boolean)
      .join(' / '),
  }
}

function isCurrentSong(songId) {
  return songId != null && String(songId) === currentSongId.value
}

async function fetchDetail() {
  loading.value = true
  try {
    const data = await getWebPlaylistDetail(route.params.id)
    const playlist = normalizePlaylist(data?.playlist || {})
    detail.value = {
      playlist,
      songs: (data?.songs || []).map((item) => normalizeSong(item, playlist.cover)),
    }
  } finally {
    loading.value = false
  }
}

async function playSong(item) {
  const queue = detail.value.songs
  const played = await playByUserGesture(item.id, queue)
  if (!played) {
    playerStore.playSong(item.id, queue)
  }
}

async function playAllSongs() {
  if (!detail.value.songs.length) {
    return
  }
  await playSong(detail.value.songs[0])
}

async function handleFavoritePlaylist() {
  if (!userStore.isLoggedIn) {
    userStore.openAuthDialog()
    return
  }
  try {
    await favoritePlaylist({ playlistId: detail.value.playlist.id })
    ElMessage.success('已收藏歌单')
  } catch (error) {
    ElMessage.error(error.message || '收藏失败')
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

onMounted(() => {
  fetchDetail()
})
</script>

<template>
  <div class="container page-section" v-loading="loading">
    <section v-if="detail.playlist" class="singer-detail-hero">
      <div class="singer-detail-avatar-wrap">
        <el-image
          v-if="detail.playlist.cover"
          :src="detail.playlist.cover"
          fit="cover"
          class="singer-detail-avatar"
        />
        <div v-else class="singer-detail-avatar singer-detail-avatar-fallback">单</div>
      </div>

      <div class="singer-detail-main">
        <div class="singer-detail-top">
          <div>
            <h1 class="singer-detail-title">{{ detail.playlist.name }}</h1>
            <p class="singer-detail-meta">
              <span v-if="detail.playlist.tagsText">风格：{{ detail.playlist.tagsText }}</span>
              <span>歌曲：{{ detail.playlist.songCount || 0 }} 首</span>
              <span>收藏：{{ detail.playlist.favoriteCount || 0 }}</span>
            </p>
            <p class="singer-detail-intro">{{ detail.playlist.introText }}</p>
          </div>
        </div>

        <div class="singer-detail-actions">
          <el-button type="primary" round :disabled="!detail.songs.length" @click="playAllSongs">
            播放歌单
          </el-button>
          <button class="playlist-icon-btn playlist-icon-btn-large" type="button" title="收藏歌单" @click="handleFavoritePlaylist">
            <i class="iconfont icon-collection"></i>
          </button>
          <el-button plain round @click="router.push('/playlist')">返回歌单广场</el-button>
        </div>
      </div>
    </section>

    <el-card shadow="never" class="page-card singer-song-card">
      <template #header>
        <div class="section-header">
          <h3 class="section-title">歌单歌曲</h3>
          <span class="singer-song-count">共 {{ detail.songs.length }} 首</span>
        </div>
      </template>

      <div v-if="detail.songs.length" class="singer-song-table">
        <div class="singer-song-head">
          <span>歌曲</span>
          <span>歌手</span>
          <span>时长</span>
          <span>操作</span>
        </div>

        <button
          v-for="(item, index) in detail.songs"
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
              <div v-else class="singer-song-cover singer-song-cover-fallback">歌</div>
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
          <span class="singer-song-time">{{ item.durationText }}</span>
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
      <el-empty v-else description="当前歌单暂无歌曲" :image-size="100" />
    </el-card>
  </div>
</template>
