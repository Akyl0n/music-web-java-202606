<template>
  <div v-loading="loading" class="container page-section">
    <section v-if="detail.singer" class="singer-detail-hero">
      <div class="singer-detail-avatar-wrap">
        <el-image
          v-if="detail.singer.avatar"
          :src="detail.singer.avatar"
          fit="cover"
          class="singer-detail-avatar"
        />
        <div v-else class="singer-detail-avatar singer-detail-avatar-fallback">
          {{ detail.singer.avatarText }}
        </div>
      </div>

      <div class="singer-detail-main">
        <div class="singer-detail-top">
          <div>
            <h1 class="singer-detail-title">{{ detail.singer.name }}</h1>
            <p class="singer-detail-meta">
              <span v-if="detail.singer.region">地区：{{ detail.singer.region }}</span>
              <span v-if="detail.singer.genderText">性别：{{ detail.singer.genderText }}</span>
              <span v-if="detail.singer.typeText">类型：{{ detail.singer.typeText }}</span>
              <span v-if="detail.singer.tagsText">风格：{{ detail.singer.tagsText }}</span>
            </p>
            <p v-if="detail.singer.intro" class="singer-detail-intro">{{ detail.singer.intro }}</p>
          </div>
        </div>

        <div class="singer-detail-stats">
          <div class="singer-detail-stat">
            <strong>{{ detail.totalSongs }}</strong>
            <span>单曲</span>
          </div>
          <div class="singer-detail-stat">
            <strong>{{ detail.recommendCount }}</strong>
            <span>推荐</span>
          </div>
          <div class="singer-detail-stat">
            <strong>{{ detail.totalPlayText }}</strong>
            <span>热度</span>
          </div>
        </div>

        <div class="singer-detail-actions">
          <el-button type="primary" round :disabled="!detail.songs.length" @click="playAllSongs">
            播放歌手热门歌曲
          </el-button>
          <el-button round @click="router.push('/singers')">
            返回歌手列表
          </el-button>
        </div>
      </div>
    </section>

    <el-card shadow="never" class="page-card singer-song-card">
      <template #header>
        <div class="section-header">
          <h3 class="section-title">热门歌曲</h3>
          <span class="singer-song-count">共 {{ detail.totalSongs }} 首</span>
        </div>
      </template>

      <div v-if="detail.songs.length" class="singer-song-table">
        <div class="singer-song-head">
          <span>歌曲</span>
          <span>分类</span>
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
                v-if="resolveSongCover(item)"
                :src="resolveSongCover(item)"
                fit="cover"
                class="singer-song-cover"
              />
              <div v-else class="singer-song-cover singer-song-cover-fallback">
                {{ detail.singer?.avatarText || '歌' }}
              </div>
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
              <p :title="item.subtitle || item.intro || item.tagsText || ''">
                {{ item.subtitle || item.intro || item.tagsText || '暂无描述' }}
              </p>
            </div>
          </div>
          <span class="singer-song-album">{{ item.categoryText || '未分类' }}</span>
          <span class="singer-song-time">{{ formatDuration(item.durationSeconds) }}</span>
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
      <el-empty v-else description="当前歌手暂无可播放歌曲" :image-size="100" />
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { getWebSingerDetail } from '../../api/singer'
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
const detail = reactive({
  singer: null,
  songs: [],
  totalSongs: 0,
  recommendCount: 0,
  totalPlayText: '0',
})

const currentSongId = computed(() => String(playerStore.currentSong?.id ?? ''))

function normalizeFileUrl(url) {
  if (!url) {
    return ''
  }
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/api/')) {
    return url
  }
  return url.startsWith('/') ? `/api${url}` : `/api/${url}`
}

function resolveAvatarText(name) {
  return String(name || '').trim().slice(0, 1) || '#'
}

function formatDuration(seconds) {
  if (!seconds && seconds !== 0) {
    return '--:--'
  }
  const minutes = String(Math.floor(seconds / 60)).padStart(2, '0')
  const remainSeconds = String(Math.floor(seconds % 60)).padStart(2, '0')
  return `${minutes}:${remainSeconds}`
}

function formatCount(value) {
  const count = Number(value || 0)
  if (count >= 10000) {
    return `${(count / 10000).toFixed(1)}万`
  }
  return String(count)
}

function normalizeSinger(singer = {}) {
  return {
    ...singer,
    avatar: normalizeFileUrl(singer.avatar || ''),
    avatarText: resolveAvatarText(singer.name),
    genderText: singer.gender === 'male' ? '男' : singer.gender === 'female' ? '女' : '',
    typeText: singer.type === 'group' ? '组合' : singer.type === 'virtual' ? '虚拟歌手' : '歌手',
    tagsText: String(singer.tags || '')
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean)
      .join(' / '),
  }
}

function normalizeSong(song = {}, singer = null) {
  const categoryText = song.categoryName || song.category || ''
  return {
    ...song,
    cover: normalizeFileUrl(song.cover || ''),
    audioUrl: buildSongMediaUrl(song.id, song.audioUrl || ''),
    singerAvatar: singer?.avatar || '',
    categoryText,
    meta: song.meta || categoryText,
    tagsText: String(song.tags || '')
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean)
      .join(' / '),
  }
}

function resolveSongCover(song) {
  return song.cover || song.singerAvatar || ''
}

function isCurrentSong(songId) {
  return songId != null && String(songId) === currentSongId.value
}

async function playSong(song) {
  if (!song?.id) {
    return
  }
  const played = await playByUserGesture(song.id, detail.songs)
  if (!played) {
    playerStore.playSong(song.id, detail.songs)
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

async function playAllSongs() {
  if (!detail.songs.length) {
    return
  }
  await playSong(detail.songs[0])
}

async function fetchDetail() {
  loading.value = true
  try {
    const data = await getWebSingerDetail(route.params.id)
    const singer = normalizeSinger(data?.singer || {})
    const songs = (data?.songs || []).map((item) => normalizeSong(item, singer))
    detail.singer = singer
    detail.songs = songs
    detail.totalSongs = Number(data?.totalSongs || songs.length || 0)
    detail.recommendCount = songs.filter((item) => Number(item.recommendFlag) === 1).length
    detail.totalPlayText = formatCount(songs.reduce((sum, item) => sum + Number(item.playCount || 0), 0))
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchDetail()
})
</script>
