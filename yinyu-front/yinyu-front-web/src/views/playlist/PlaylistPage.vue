<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { listWebDictItems } from '../../api/dict'
import { listWebPlaylists } from '../../api/playlist'
import { favoritePlaylist } from '../../api/user'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const categoryOptions = ref([{ label: '全部', value: '' }])
const activeTab = ref('')
const playlistList = ref([])

const keyword = computed(() => String(route.query.q || '').trim())
const visiblePlaylists = computed(() => {
  const category = activeTab.value
  return playlistList.value.filter((item) => {
    if (category && item.category !== category) {
      return false
    }
    if (!keyword.value) {
      return true
    }
    return `${item.name}${item.subtitle || ''}${item.intro || ''}${item.tagsText || ''}`.includes(keyword.value)
  })
})

function normalizeFileUrl(url) {
  if (!url) {
    return ''
  }
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/api/')) {
    return url
  }
  return url.startsWith('/') ? `/api${url}` : `/api/${url}`
}

function formatPlaylist(item = {}) {
  return {
    ...item,
    cover: normalizeFileUrl(item.cover || ''),
    tagsText: String(item.tags || '')
      .split(',')
      .map((value) => value.trim())
      .filter(Boolean)
      .join(' / '),
    tagText: String(item.tags || '')
      .split(',')
      .map((value) => value.trim())
      .filter(Boolean)[0] || item.categoryName || item.category || '精选歌单',
    introText: item.intro || item.subtitle || '暂无简介',
    songCountText: `${item.songCount || 0} 首`,
    favoriteText: `收藏 ${item.favoriteCount || 0}`,
  }
}

async function fetchCategories() {
  const data = await listWebDictItems({ typeCode: 'category' })
  categoryOptions.value = [
    { label: '全部', value: '' },
    ...(data?.list || []).map((item) => ({
      label: item.name,
      value: String(item.id),
    })),
  ]
}

async function fetchPlaylists() {
  loading.value = true
  try {
    const data = await listWebPlaylists({
      pageNo: 1,
      pageSize: 100,
      keyword: keyword.value,
    })
    playlistList.value = (data?.list || []).map(formatPlaylist)
  } finally {
    loading.value = false
  }
}

async function handleFavorite(item) {
  if (!userStore.isLoggedIn) {
    userStore.openAuthDialog()
    return
  }
  try {
    await favoritePlaylist({ playlistId: item.id })
    ElMessage.success('已收藏歌单')
  } catch (error) {
    ElMessage.error(error.message || '收藏失败')
  }
}

function openDetail(item) {
  router.push(`/playlist/${item.id}`)
}

watch(
  () => route.query.q,
  () => {
    fetchPlaylists()
  },
)

onMounted(async () => {
  await Promise.all([fetchCategories(), fetchPlaylists()])
})
</script>

<template>
  <div class="container page-section" v-loading="loading">
    <el-card shadow="never" class="page-card playlist-library-card">
      <template #header>
        <div class="section-header">
          <h3 class="section-title">歌单分类</h3>
          <el-tabs v-model="activeTab" class="theme-tabs">
            <el-tab-pane
              v-for="item in categoryOptions"
              :key="item.value"
              :label="item.label"
              :name="item.value"
            />
          </el-tabs>
        </div>
      </template>

      <div v-if="visiblePlaylists.length" class="playlist-grid playlist-grid-page playlist-grid-five">
        <el-card v-for="item in visiblePlaylists" :key="item.id" shadow="hover" class="playlist-card">
          <div
            class="playlist-cover is-clickable"
            :style="item.cover ? {
              backgroundImage: `url('${item.cover}')`,
              backgroundSize: 'cover',
              backgroundPosition: 'center',
            } : {}"
            @click="openDetail(item)"
          >
            <span class="playlist-tag" :title="item.tagText">{{ item.tagText }}</span>
          </div>
          <div class="playlist-content">
            <h4 :title="item.name">{{ item.name }}</h4>
            <p :title="item.introText">{{ item.introText }}</p>
            <div class="playlist-meta">{{ item.songCountText }} · {{ item.favoriteText }}</div>
          </div>
          <div class="card-actions playlist-card-actions">
            <button class="playlist-icon-btn" type="button" title="收藏歌单" @click="handleFavorite(item)">
              <i class="iconfont icon-collection"></i>
            </button>
            <el-button type="primary" round @click="openDetail(item)">查看详情</el-button>
          </div>
        </el-card>
      </div>

      <el-empty v-else description="没有找到匹配的歌单" :image-size="120" />
    </el-card>
  </div>
</template>
