<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getWebRankingDetail, listWebRankings } from '../../api/ranking'
import { likeSong } from '../../api/user'
import { usePlayerStore } from '../../stores/player'
import { useUserStore } from '../../stores/user'
import { playByUserGesture } from '../../utils/player-bridge'
import { buildSongMediaUrl } from '../../utils/song-media'

const loading = ref(false)
const boards = ref([])
const activeTab = ref('hot')
const rankingDetail = ref(createDefaultDetail())
const playerStore = usePlayerStore()
const userStore = useUserStore()

const rankingSongs = computed(() => rankingDetail.value.songs)
const topThree = computed(() => rankingSongs.value.slice(0, 3))
const restSongs = computed(() => rankingSongs.value.slice(3))
const currentSongId = computed(() => String(playerStore.currentSong?.id ?? ''))

function createDefaultDetail() {
  return {
    currentBoard: null,
    boards: [],
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

function formatCount(count) {
  const value = Number(count || 0)
  if (value >= 10000) {
    return `${(value / 10000).toFixed(1)}万`
  }
  return `${value}`
}

function normalizeSong(item = {}, index = 0) {
  return {
    ...item,
    no: String(index + 1).padStart(2, '0'),
    singer: item.singerName || '未知歌手',
    desc: item.subtitle || item.intro || item.categoryName || item.category || '精选单曲',
    meta: item.categoryName || item.category || '未分类',
    time: formatDuration(item.durationSeconds),
    cover: normalizeFileUrl(item.cover || ''),
    singerAvatar: normalizeFileUrl(item.singerAvatar || ''),
    audioUrl: buildSongMediaUrl(item.id, item.audioUrl || ''),
    hotText: `播放 ${formatCount(item.playCount)}`,
    favoriteText: `收藏 ${formatCount(item.favoriteCount)}`,
  }
}

function isCurrentSong(songId) {
  return songId != null && String(songId) === currentSongId.value
}

function resolveSongCover(song) {
  return song?.cover || song?.singerAvatar || ''
}

async function fetchBoards() {
  const data = await listWebRankings()
  boards.value = Array.isArray(data) ? data : []
  if (!boards.value.length) {
    activeTab.value = ''
    return
  }
  if (!boards.value.some((item) => item.code === activeTab.value)) {
    activeTab.value = boards.value[0].code
  }
}

async function fetchRankingDetail(code) {
  if (!code) {
    rankingDetail.value = createDefaultDetail()
    return
  }
  loading.value = true
  try {
    const data = await getWebRankingDetail(code)
    rankingDetail.value = {
      currentBoard: data?.currentBoard || null,
      boards: data?.boards || boards.value,
      songs: (data?.songs || []).map((item, index) => normalizeSong(item, index)),
    }
  } finally {
    loading.value = false
  }
}

async function playSong(item) {
  if (!item?.id) {
    return
  }
  const queue = rankingDetail.value.songs
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

watch(
  () => activeTab.value,
  (value) => {
    if (!value) {
      return
    }
    fetchRankingDetail(value)
  },
  { immediate: true },
)

onMounted(async () => {
  try {
    await fetchBoards()
  } catch (error) {
    ElMessage.error(error.message || '获取排行榜失败')
  }
})
</script>

<template>
  <div class="container page-section" v-loading="loading">
    <section class="ranking-hero">
      <div class="ranking-banner">
        <p class="page-kicker">排行榜</p>
        <h2>{{ rankingDetail.currentBoard?.name || '古风榜单' }}</h2>
        <p class="page-desc">{{ rankingDetail.currentBoard?.note || '按现有歌曲热度与内容数据整理出的正式榜单。' }}</p>
        <div class="ranking-banner-meta">
          <span>当前榜单：{{ rankingDetail.currentBoard?.name || '未选择' }}</span>
          <span>上榜歌曲：{{ rankingSongs.length }} 首</span>
          <span>{{ rankingDetail.currentBoard?.updateText || '最近更新' }}</span>
        </div>
      </div>

      <el-card shadow="never" class="ranking-side-card">
        <template #header>
          <div class="section-header">
            <h3 class="card-title">榜单切换</h3>
          </div>
        </template>
        <el-tabs v-model="activeTab" class="theme-tabs theme-tabs-vertical">
          <el-tab-pane v-for="item in boards" :key="item.code" :label="item.name" :name="item.code" />
        </el-tabs>
        <div class="ranking-tip">
          <strong>听榜建议</strong>
          <p>先播放前三首，再顺着完整榜单继续听，体验会更接近正式音乐平台。</p>
        </div>
      </el-card>
    </section>

    <section v-if="topThree.length" class="ranking-podium">
      <el-card
        v-for="(item, index) in topThree"
        :key="item.id"
        shadow="hover"
        class="ranking-podium-card is-clickable"
        :class="`is-rank-${index + 1}`"
        @click="playSong(item)"
      >
        <div class="podium-top">
          <span class="podium-badge">TOP {{ index + 1 }}</span>
          <span class="podium-time">{{ item.time }}</span>
        </div>
        <h3>{{ item.name }}</h3>
        <p>{{ item.desc }}</p>
        <div class="podium-meta">{{ item.meta }}</div>
        <div class="podium-footer">
          <span>{{ item.singer }}</span>
          <div class="singer-song-action-group">
            <button class="playlist-icon-btn" type="button" title="播放歌曲" @click.stop="playSong(item)">
              <i class="iconfont icon-play"></i>
            </button>
            <button class="playlist-icon-btn" type="button" title="喜欢歌曲" @click.stop="handleLikeSong(item.id)">
              <i class="iconfont icon-like"></i>
            </button>
          </div>
        </div>
      </el-card>
    </section>

    <el-card shadow="never" class="page-card">
      <template #header>
        <div class="section-header">
          <h3 class="section-title">完整榜单</h3>
          <el-tag round effect="plain">{{ rankingDetail.currentBoard?.name || '榜单' }}</el-tag>
        </div>
      </template>

      <div v-if="restSongs.length" class="singer-song-table">
        <div class="singer-song-head">
          <span>歌曲</span>
          <span>分类</span>
          <span>时长</span>
          <span>操作</span>
        </div>

        <button
          v-for="item in restSongs"
          :key="item.id"
          type="button"
          :class="['singer-song-row', { 'is-playing': isCurrentSong(item.id) }]"
          @click="playSong(item)"
        >
          <div class="singer-song-main">
            <span class="singer-song-order">{{ item.no }}</span>
            <div class="singer-song-cover-wrap">
              <el-image
                v-if="resolveSongCover(item)"
                :src="resolveSongCover(item)"
                fit="cover"
                class="singer-song-cover"
              />
              <span v-else class="singer-song-cover-fallback">{{ item.name?.slice(0, 1) || '歌' }}</span>
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
              <p :title="`${item.singer} · ${item.hotText}`">{{ item.singer }} · {{ item.hotText }}</p>
            </div>
          </div>
          <span class="singer-song-album">{{ item.meta }}</span>
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
      <el-empty v-else description="当前榜单暂无歌曲" :image-size="120" />
    </el-card>
  </div>
</template>
