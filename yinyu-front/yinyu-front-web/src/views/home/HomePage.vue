<template>
  <div v-loading="loading" class="container page-section home-page">
    <section class="hero">
      <div class="hero-banner hero-banner-carousel">
        <el-carousel
          v-if="homeData.banners.length"
          height="430px"
          trigger="click"
          class="hero-carousel"
        >
          <el-carousel-item
            v-for="item in homeData.banners"
            :key="`${item.targetType}-${item.targetId}-${item.id}`"
          >
            <div
              class="hero-slide is-clickable"
              :style="resolveHeroBannerStyle(item.cover)"
              @click="handleRecommendTarget(item)"
            ></div>
          </el-carousel-item>
        </el-carousel>

        <div v-else class="hero-slide" :style="resolveHeroBannerStyle('')"></div>
      </div>

      <div class="hero-side">
        <el-card shadow="never" class="card recommend-card">
          <template #header>
            <div class="card-title-row">
              <h3 class="card-title">今日推荐</h3>
            </div>
          </template>

          <div v-if="homeData.featuredSongs.length" class="recommend-song-list">
            <div
              v-for="item in homeData.featuredSongs"
              :key="`${item.targetType}-${item.targetId}-${item.id}`"
              :class="[
                'recommend-song',
                'recommend-song-hero',
                'is-clickable',
                { 'is-playing': item.targetType === 'song' && isCurrentSong(item.id) },
              ]"
              @click="handleRecommendTarget(item)"
            >
              <el-image
                v-if="resolveSongCover(item)"
                :src="resolveSongCover(item)"
                fit="cover"
                class="recommend-cover recommend-cover-image"
              />
              <div v-else class="recommend-cover recommend-cover-fallback">
                {{ resolveSongInitial(item) }}
              </div>

              <div class="recommend-info">
                <h4 :title="item.name">
                  <span>{{ item.name }}</span>
                  <span
                    v-if="item.targetType === 'song' && isCurrentSong(item.id)"
                    class="inline-playing-indicator"
                    aria-hidden="true"
                  >
                    <i></i>
                    <i></i>
                    <i></i>
                  </span>
                </h4>
                <p :title="resolveFeaturedDesc(item)">{{ resolveFeaturedDesc(item) }}</p>
                <div class="tag-row">
                  <el-tag round>{{ resolveFeaturedTypeLabel(item.targetType) }}</el-tag>
                  <el-tag round>{{ item.meta || '精选推荐' }}</el-tag>
                </div>
              </div>
            </div>
          </div>

          <el-empty v-else description="暂无今日推荐" :image-size="80" />
        </el-card>
      </div>
    </section>

    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="section-header">
          <h3 class="section-title">歌单推荐</h3>
          <el-button text @click="router.push('/playlist')">全部歌单</el-button>
        </div>
      </template>

      <div v-if="homeData.recommendedPlaylists.length" class="playlist-grid">
        <el-card
          v-for="item in homeData.recommendedPlaylists"
          :key="item.id"
          shadow="hover"
          class="playlist-card"
        >
          <div
            class="playlist-cover is-clickable"
            :style="resolvePlaylistCoverStyle(item.cover)"
            @click="router.push(`/playlist/${item.id}`)"
          >
            <span class="playlist-tag" :title="item.tag">{{ item.tag }}</span>
          </div>
          <div class="playlist-content">
            <h4 :title="item.title">{{ item.title }}</h4>
            <p :title="item.desc">{{ item.desc }}</p>
            <div class="playlist-meta" :title="item.meta">{{ item.meta }}</div>
          </div>
          <div class="card-actions playlist-card-actions">
            <button
              class="playlist-icon-btn"
              type="button"
              title="收藏歌单"
              @click="handleFavoritePlaylist(item.id)"
            >
              <i class="iconfont icon-collection"></i>
            </button>
            <el-button type="primary" round @click="router.push(`/playlist/${item.id}`)">
              查看详情
            </el-button>
          </div>
        </el-card>
      </div>
      <el-empty v-else description="暂无推荐歌单" :image-size="100" />
    </el-card>

    <section class="two-col">
      <el-card shadow="never" class="panel">
        <template #header>
          <div class="section-header">
            <h3 class="section-title">热门歌曲</h3>
            <el-button text @click="router.push('/ranking')">歌曲榜单</el-button>
          </div>
        </template>

        <div v-if="homeData.hotSongs.length" class="song-list">
          <div
            v-for="item in homeData.hotSongs"
            :key="item.id"
            :class="['song-row', 'is-clickable', { 'is-playing': isCurrentSong(item.id) }]"
            @click="playSong(item)"
          >
            <div class="song-index">{{ item.no }}</div>
            <div>
              <div class="song-name" :title="item.name">
                <span>{{ item.name }}</span>
                <span v-if="isCurrentSong(item.id)" class="inline-playing-indicator" aria-hidden="true">
                  <i></i>
                  <i></i>
                  <i></i>
                </span>
              </div>
              <div class="song-desc" :title="item.desc">{{ item.desc }}</div>
            </div>
            <div class="song-meta">{{ item.meta || '精选单曲' }}</div>
            <div class="song-action-group" @click.stop>
              <el-button type="primary" plain circle class="song-play" @click="playSong(item)">
                <i class="iconfont icon-play"></i>
              </el-button>
              <button class="song-action-btn" type="button" title="喜欢歌曲" @click="handleLikeSong(item.id)">
                <i class="iconfont icon-like"></i>
              </button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无歌曲数据" :image-size="100" />
      </el-card>

      <el-card shadow="never" class="panel">
        <template #header>
          <div class="section-header">
            <h3 class="section-title">热门歌手</h3>
            <el-button text @click="router.push('/singers')">全部歌手</el-button>
          </div>
        </template>

        <div v-if="homeData.hotSingers.length" class="singer-grid">
          <el-card
            v-for="item in homeData.hotSingers"
            :key="item.id"
            shadow="hover"
            class="singer-card is-clickable"
            @click="router.push(`/singers/${item.id}`)"
          >
            <el-image v-if="item.avatar" :src="item.avatar" fit="cover" class="avatar"></el-image>
            <div v-else class="avatar" :style="{ background: resolveCoverBackground('') }"></div>
            <h4 :title="item.name">{{ item.name }}</h4>
            <p :title="item.desc">{{ item.desc }}</p>
            <div class="listen-count">{{ item.count }}</div>
          </el-card>
        </div>
        <el-empty v-else description="暂无歌手数据" :image-size="100" />
      </el-card>
    </section>

    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="section-header">
          <h3 class="section-title">分类歌单</h3>
          <el-button text @click="router.push('/playlist')">按分类浏览</el-button>
        </div>
      </template>

      <div v-if="homeData.categories.length" class="category-grid">
        <el-card
          v-for="item in homeData.categories"
          :key="item.category"
          shadow="hover"
          class="category-card is-clickable"
          @click="router.push('/playlist')"
        >
          <h4>{{ item.title }}</h4>
          <p>{{ item.desc }}</p>
          <span>{{ item.countText }}</span>
        </el-card>
      </div>
      <el-empty v-else description="暂无分类歌单" :image-size="100" />
    </el-card>

    <footer class="footer">
      <div class="footer-line"></div>
      <p>音域 © 2026 · 首页轮播与今日推荐已切换为后台配置驱动。</p>
      <p class="footer-sub">歌曲、歌单、歌手都可以在后台统一配置并联动到首页展示。</p>
    </footer>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getHomePage } from '../../api/home'
import { favoritePlaylist, likeSong } from '../../api/user'
import { usePlayerStore } from '../../stores/player'
import { useUserStore } from '../../stores/user'
import { playByUserGesture } from '../../utils/player-bridge'
import { buildSongMediaUrl } from '../../utils/song-media'

const router = useRouter()
const playerStore = usePlayerStore()
const userStore = useUserStore()

const loading = ref(false)
const homeData = ref(createDefaultHomeData())
const currentSongId = computed(() => String(playerStore.currentSong?.id ?? ''))

function createDefaultHomeData() {
  return {
    banners: [],
    featuredSongs: [],
    trends: [],
    recommendedPlaylists: [],
    hotSongs: [],
    hotSingers: [],
    categories: [],
    playQueue: [],
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

function normalizeSong(song = {}, index = 0) {
  return {
    ...song,
    id: song.id,
    targetType: song.targetType || 'song',
    targetId: song.targetId ?? song.id,
    no: song.no || String(index + 1).padStart(2, '0'),
    cover: normalizeFileUrl(song.cover || ''),
    singerAvatar: normalizeFileUrl(song.singerAvatar || ''),
    audioUrl: buildSongMediaUrl(song.id, song.audioUrl || ''),
  }
}

function normalizePlaylist(playlist = {}) {
  return {
    ...playlist,
    id: playlist.id,
    targetType: playlist.targetType || 'playlist',
    targetId: playlist.targetId ?? playlist.id,
    cover: normalizeFileUrl(playlist.cover || ''),
    songIds: Array.isArray(playlist.songIds) ? playlist.songIds : [],
  }
}

function normalizeSinger(singer = {}) {
  return {
    ...singer,
    avatar: normalizeFileUrl(singer.avatar || ''),
  }
}

function normalizeHomeData(data = {}) {
  return {
    banners: Array.isArray(data.banners) ? data.banners.map(normalizePlaylist) : [],
    featuredSongs: Array.isArray(data.featuredSongs)
      ? data.featuredSongs.map((item, index) => normalizeSong(item, index))
      : [],
    trends: Array.isArray(data.trends) ? data.trends : [],
    recommendedPlaylists: Array.isArray(data.recommendedPlaylists)
      ? data.recommendedPlaylists.map(normalizePlaylist)
      : [],
    hotSongs: Array.isArray(data.hotSongs) ? data.hotSongs.map((item, index) => normalizeSong(item, index)) : [],
    hotSingers: Array.isArray(data.hotSingers) ? data.hotSingers.map(normalizeSinger) : [],
    categories: Array.isArray(data.categories) ? data.categories : [],
    playQueue: Array.isArray(data.playQueue) ? data.playQueue.map((item, index) => normalizeSong(item, index)) : [],
  }
}

function resolveCoverBackground(cover) {
  if (cover) {
    return `url("${cover}")`
  }
  return 'linear-gradient(135deg, #1f9a60, #75e0ae)'
}

function resolvePlaylistCoverStyle(cover) {
  if (cover) {
    return {
      backgroundImage: resolveCoverBackground(cover),
      backgroundPosition: 'center',
      backgroundSize: 'cover',
      backgroundRepeat: 'no-repeat',
    }
  }
  return {
    background: resolveCoverBackground(''),
  }
}

function resolveHeroBannerStyle(cover) {
  if (cover) {
    return {
      backgroundImage: `url("${cover}")`,
      backgroundPosition: 'center',
      backgroundSize: 'cover',
      backgroundRepeat: 'no-repeat',
    }
  }
  return {
    background: 'linear-gradient(120deg, rgba(12, 48, 33, 0.96), rgba(24, 112, 71, 0.88), rgba(103, 216, 162, 0.7))',
  }
}

function resolveSongCover(song) {
  return normalizeFileUrl(song?.cover || song?.singerAvatar || '')
}

function resolveSongInitial(song) {
  const name = song?.name?.trim?.()
  return name ? name.charAt(0).toUpperCase() : '音'
}

function resolveFeaturedTypeLabel(targetType) {
  if (targetType === 'playlist') {
    return '歌单推荐'
  }
  if (targetType === 'singer') {
    return '歌手推荐'
  }
  return '歌曲推荐'
}

function resolveFeaturedDesc(item) {
  if (!item) {
    return ''
  }
  if (item.targetType === 'song') {
    return [item.singer, item.desc].filter(Boolean).join(' · ')
  }
  return item.desc || item.singer || '今日推荐内容'
}

function isCurrentSong(songId) {
  return songId != null && String(songId) === currentSongId.value
}

async function playSong(song) {
  if (!song?.id) {
    return
  }
  const played = await playByUserGesture(song.id, homeData.value.playQueue)
  if (!played) {
    playerStore.playSong(song.id, homeData.value.playQueue)
  }
}

async function handleRecommendTarget(item) {
  if (!item) {
    return
  }
  if (item.targetType === 'song') {
    const songId = item.targetId || item.firstSongId || item.id
    if (!songId) {
      return
    }
    const played = await playByUserGesture(songId, homeData.value.playQueue)
    if (!played) {
      playerStore.playSong(songId, homeData.value.playQueue)
    }
    return
  }
  if (item.targetType === 'playlist' && item.targetId) {
    router.push(`/playlist/${item.targetId}`)
    return
  }
  if (item.targetType === 'singer' && item.targetId) {
    router.push(`/singers/${item.targetId}`)
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

async function handleFavoritePlaylist(playlistId) {
  if (!userStore.isLoggedIn) {
    userStore.openAuthDialog()
    return
  }
  try {
    await favoritePlaylist({ playlistId })
    ElMessage.success('已收藏歌单')
  } catch (error) {
    ElMessage.error(error.message || '操作失败')
  }
}

async function fetchHomeData() {
  loading.value = true
  try {
    const data = await getHomePage()
    homeData.value = normalizeHomeData(data)
    if (!playerStore.queue.length && homeData.value.playQueue.length) {
      playerStore.setQueue(homeData.value.playQueue)
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchHomeData()
})
</script>
