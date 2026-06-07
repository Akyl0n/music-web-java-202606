import { defineStore } from 'pinia'
import { buildSongMediaUrl } from '../utils/song-media'

function formatDuration(seconds) {
  if (!seconds && seconds !== 0) {
    return '--:--'
  }
  const minutes = String(Math.floor(seconds / 60)).padStart(2, '0')
  const remainSeconds = String(Math.floor(seconds % 60)).padStart(2, '0')
  return `${minutes}:${remainSeconds}`
}

function normalizeQueue(queue = []) {
  return Array.isArray(queue)
    ? queue.map((item) => ({
        id: item.id,
        name: item.name || '未命名歌曲',
        singer: item.singer || item.singerName || '未知歌手',
        singerAvatar: item.singerAvatar || '',
        meta: item.meta || '',
        time: item.time || formatDuration(item.durationSeconds),
        durationSeconds: item.durationSeconds || 0,
        cover: item.cover || '',
        audioUrl: buildSongMediaUrl(item.id, item.audioUrl || ''),
      }))
    : []
}

function getDefaultSong() {
  return {
    id: undefined,
    name: '暂无歌曲',
    singer: '未知歌手',
    singerAvatar: '',
    meta: '等待播放',
    time: '--:--',
    durationSeconds: 0,
    cover: '',
    audioUrl: '',
  }
}

export const usePlayerStore = defineStore('player', {
  state: () => ({
    queue: [],
    currentIndex: -1,
    isPlaying: false,
    currentSeconds: 0,
    mediaDuration: 0,
    playHistoryUpdatedAt: 0,
  }),
  getters: {
    currentSong(state) {
      return state.queue[state.currentIndex] || getDefaultSong()
    },
    totalSeconds() {
      if (this.mediaDuration > 0) {
        return this.mediaDuration
      }
      if (this.currentSong.durationSeconds > 0) {
        return this.currentSong.durationSeconds
      }
      const [minutes = '0', seconds = '0'] = String(this.currentSong.time || '0:0').split(':')
      return Number(minutes) * 60 + Number(seconds)
    },
    progress() {
      return this.totalSeconds ? Math.round((this.currentSeconds / this.totalSeconds) * 100) : 0
    },
    currentTimeText(state) {
      return formatDuration(state.currentSeconds)
    },
    durationText() {
      return formatDuration(this.totalSeconds)
    },
  },
  actions: {
    setQueue(queue, currentSongId) {
      const normalizedQueue = normalizeQueue(queue)
      this.queue = normalizedQueue
      if (!normalizedQueue.length) {
        this.currentIndex = -1
        this.currentSeconds = 0
        this.mediaDuration = 0
        this.isPlaying = false
        return
      }
      const matchIndex = normalizedQueue.findIndex((item) => String(item.id) === String(currentSongId))
      this.currentIndex = matchIndex >= 0 ? matchIndex : -1
      this.currentSeconds = 0
      this.mediaDuration = 0
      this.isPlaying = matchIndex >= 0
    },
    playSong(songId, queue) {
      const normalizedQueue = queue ? normalizeQueue(queue) : this.queue
      if (!normalizedQueue.length) {
        this.queue = []
        this.currentIndex = -1
        this.currentSeconds = 0
        this.mediaDuration = 0
        this.isPlaying = false
        return
      }
      this.queue = normalizedQueue
      const index = normalizedQueue.findIndex((item) => String(item.id) === String(songId))
      if (index < 0) {
        return
      }
      this.currentIndex = index
      this.currentSeconds = 0
      this.mediaDuration = 0
      this.isPlaying = true
    },
    setSong(songId, queue) {
      if (queue) {
        this.setQueue(queue, songId)
      }
      const index = this.queue.findIndex((item) => String(item.id) === String(songId))
      if (index >= 0) {
        this.currentIndex = index
        this.currentSeconds = 0
        this.mediaDuration = 0
        this.isPlaying = true
      }
    },
    togglePlay() {
      if (this.currentIndex < 0 || !this.queue.length) {
        return
      }
      this.isPlaying = !this.isPlaying
    },
    next() {
      if (!this.queue.length) {
        return
      }
      this.currentIndex = (this.currentIndex + 1) % this.queue.length
      this.currentSeconds = 0
      this.mediaDuration = 0
      this.isPlaying = true
    },
    prev() {
      if (!this.queue.length) {
        return
      }
      this.currentIndex = (this.currentIndex - 1 + this.queue.length) % this.queue.length
      this.currentSeconds = 0
      this.mediaDuration = 0
      this.isPlaying = true
    },
    setProgress(percent) {
      this.currentSeconds = Math.floor((this.totalSeconds * percent) / 100)
    },
    setCurrentSeconds(seconds) {
      this.currentSeconds = Math.max(0, Math.floor(seconds || 0))
    },
    setDuration(seconds) {
      this.mediaDuration = Math.max(0, Math.floor(seconds || 0))
    },
    setPlaying(playing) {
      this.isPlaying = !!playing
    },
    markPlayHistoryUpdated() {
      this.playHistoryUpdatedAt = Date.now()
    },
  },
})
