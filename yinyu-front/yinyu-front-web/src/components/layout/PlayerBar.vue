<template>
  <div class="player-bar-wrap">
    <div class="player-bar">
      <div class="player-left">
        <el-image v-if="displayCover" :src="displayCover" fit="cover" class="player-cover" />
        <div v-else class="player-cover player-cover-fallback">{{ coverFallbackText }}</div>
        <div class="player-info">
          <h4 :title="currentSong.name">
            <span>{{ currentSong.name }}</span>
            <span v-if="isPlaying && currentSong.audioUrl" class="player-playing-indicator" aria-hidden="true">
              <i></i>
              <i></i>
              <i></i>
            </span>
          </h4>
          <p :title="currentSong.singer">{{ currentSong.singer }} · {{ currentSong.meta || '等待播放' }}</p>
        </div>
      </div>

      <div class="player-center">
        <el-button circle class="player-control is-plain" @click="handlePrev">
          <i class="iconfont icon-pre"></i>
        </el-button>
        <el-button circle class="player-control is-main" @click="handleTogglePlay">
          <i :class="['iconfont', isPlaying ? 'icon-pause' : 'icon-play']"></i>
        </el-button>
        <el-button circle class="player-control is-plain" @click="handleNext">
          <i class="iconfont icon-next"></i>
        </el-button>
        <div class="progress-wrap">
          <span class="player-time">{{ currentTimeText }}</span>
          <el-slider
            :model-value="progress"
            :show-tooltip="false"
            class="theme-slider"
            :disabled="!currentSong.audioUrl"
            @update:model-value="handleSliderChange"
          />
          <span class="player-time">{{ durationText }}</span>
        </div>
      </div>

      <div class="player-right">
        <el-popover placement="top" :width="260" trigger="click" popper-class="player-volume-popover">
          <template #reference>
            <button class="player-mini-action" type="button">
              <i class="iconfont icon-audio"></i>
            </button>
          </template>
          <div class="player-volume-panel">
            <button class="player-volume-btn" type="button" @click="changeVolume(-10)">-</button>
            <el-slider
              :model-value="volumePercent"
              :show-tooltip="false"
              class="theme-slider player-volume-slider"
              @update:model-value="handleVolumeChange"
            />
            <button class="player-volume-btn" type="button" @click="changeVolume(10)">+</button>
            <span class="player-volume-text">{{ volumePercent }}%</span>
          </div>
        </el-popover>
      </div>
    </div>

    <audio
      ref="audioRef"
      preload="metadata"
      @loadedmetadata="handleLoadedMetadata"
      @durationchange="handleLoadedMetadata"
      @timeupdate="handleTimeUpdate"
      @ended="handleEnded"
    ></audio>
  </div>
</template>

<script setup>
import { storeToRefs } from 'pinia'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { usePlayerStore } from '../../stores/player'
import { registerPlayerHandler, unregisterPlayerHandler } from '../../utils/player-bridge'

const playerStore = usePlayerStore()
const { currentSong, isPlaying, progress, currentTimeText, durationText } = storeToRefs(playerStore)
const audioRef = ref(null)
const volumePercent = ref(70)
let suppressSyncWatch = false

const displayCover = computed(() => currentSong.value.cover || currentSong.value.singerAvatar || '')
const coverFallbackText = computed(() => String(currentSong.value.name || '').trim().slice(0, 1) || '歌')

function handleSliderChange(percent) {
  const audio = audioRef.value
  if (!audio || !audio.duration) {
    return
  }
  const nextTime = (audio.duration * percent) / 100
  audio.currentTime = nextTime
  playerStore.setCurrentSeconds(nextTime)
  if (!isPlaying.value && currentSong.value.audioUrl) {
    playerStore.setPlaying(true)
    audio.play().catch(() => {
      playerStore.setPlaying(false)
    })
  }
}

function handleTimeUpdate(event) {
  playerStore.setCurrentSeconds(event.target.currentTime)
}

function handleLoadedMetadata(event) {
  const duration = event.target?.duration
  if (Number.isFinite(duration) && duration > 0) {
    playerStore.setDuration(duration)
  }
}

function applyVolume(percent) {
  const nextVolume = Math.max(0, Math.min(100, Math.round(percent || 0)))
  volumePercent.value = nextVolume
  if (audioRef.value) {
    audioRef.value.volume = nextVolume / 100
  }
}

function handleVolumeChange(percent) {
  applyVolume(percent)
}

function changeVolume(delta) {
  applyVolume(volumePercent.value + delta)
}

function handleEnded() {
  playerStore.setCurrentSeconds(0)
  handleNext()
}

function pauseAudio() {
  const audio = audioRef.value
  if (!audio) {
    return
  }
  audio.pause()
}

async function syncAudioSourceWithSong(song, autoPlay = false) {
  const audio = audioRef.value
  if (!audio) {
    return false
  }
  const nextSource = song?.audioUrl || ''
  if (audio.src !== nextSource) {
    audio.src = nextSource
  }
  playerStore.setCurrentSeconds(0)
  playerStore.setDuration(0)
  if (!nextSource) {
    pauseAudio()
    playerStore.setPlaying(false)
    return false
  }
  audio.load()
  applyVolume(volumePercent.value)
  if (!autoPlay) {
    return true
  }
  try {
    await audio.play()
    playerStore.setPlaying(true)
    return true
  } catch (error) {
    playerStore.setPlaying(false)
    return false
  }
}

function syncAudioSource(autoPlay = false) {
  return syncAudioSourceWithSong(currentSong.value, autoPlay)
}

async function handleDirectPlay(songId, nextQueue) {
  suppressSyncWatch = true
  playerStore.playSong(songId, nextQueue)
  const targetSong = playerStore.currentSong
  const result = await syncAudioSourceWithSong(targetSong, true)
  await nextTick()
  suppressSyncWatch = false
  return result
}

function handleTogglePlay() {
  const audio = audioRef.value
  if (!audio || !currentSong.value.audioUrl) {
    return
  }
  if (isPlaying.value) {
    pauseAudio()
    playerStore.setPlaying(false)
    return
  }
  playerStore.setPlaying(true)
  audio.play().catch(() => {
    playerStore.setPlaying(false)
  })
}

function handlePrev() {
  playerStore.prev()
}

function handleNext() {
  playerStore.next()
}

onMounted(() => {
  applyVolume(volumePercent.value)
  registerPlayerHandler(handleDirectPlay)
})

onBeforeUnmount(() => {
  unregisterPlayerHandler(handleDirectPlay)
})

watch(
  () => [currentSong.value.id, currentSong.value.audioUrl],
  async ([, audioUrl], oldValue) => {
    if (suppressSyncWatch) {
      return
    }
    const oldAudioUrl = Array.isArray(oldValue) ? oldValue[1] : undefined
    if (!audioUrl && !oldAudioUrl) {
      return
    }
    syncAudioSource(isPlaying.value)
  },
  { immediate: true },
)

watch(
  () => isPlaying.value,
  (playing) => {
    const audio = audioRef.value
    if (!audio || !currentSong.value.audioUrl) {
      return
    }
    if (playing) {
      playerStore.markPlayHistoryUpdated()
      audio.play().catch(() => {
        playerStore.setPlaying(false)
      })
      return
    }
    pauseAudio()
  },
)
</script>
