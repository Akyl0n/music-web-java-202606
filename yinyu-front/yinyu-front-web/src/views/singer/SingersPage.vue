<template>
  <div v-loading="loading" class="container page-section">
    <el-card shadow="never" class="page-card singer-filter-card">
      <div class="singer-filter-group">
        <div class="singer-filter-label">字母</div>
        <div class="singer-filter-options">
          <button
            v-for="item in letterOptions"
            :key="item.value"
            type="button"
            :class="['singer-filter-option', { 'is-active': filters.letter === item.value }]"
            @click="setFilter('letter', item.value)"
          >
            {{ item.label }}
          </button>
        </div>
      </div>

      <div class="singer-filter-group">
        <div class="singer-filter-label">地区</div>
        <div class="singer-filter-options">
          <button
            v-for="item in regionOptions"
            :key="item.value"
            type="button"
            :class="['singer-filter-option', { 'is-active': filters.region === item.value }]"
            @click="setFilter('region', item.value)"
          >
            {{ item.label }}
          </button>
        </div>
      </div>

      <div class="singer-filter-group">
        <div class="singer-filter-label">性别</div>
        <div class="singer-filter-options">
          <button
            v-for="item in genderOptions"
            :key="item.value"
            type="button"
            :class="['singer-filter-option', { 'is-active': activeGenderFilter === item.value }]"
            @click="setGenderFilter(item.value)"
          >
            {{ item.label }}
          </button>
        </div>
      </div>

      <div class="singer-filter-group">
        <div class="singer-filter-label">风格</div>
        <div class="singer-filter-options">
          <button
            v-for="item in styleOptions"
            :key="item.value"
            type="button"
            :class="['singer-filter-option', { 'is-active': filters.tag === item.value }]"
            @click="setFilter('tag', item.value)"
          >
            {{ item.label }}
          </button>
        </div>
      </div>
    </el-card>

    <div v-if="singerList.length" class="singer-grid singer-grid-page singer-grid-web">
      <div
        v-for="item in singerList"
        :key="item.id"
        class="singer-showcase"
        @click="openSingerDetail(item.id)"
      >
        <el-image v-if="item.avatar" :src="item.avatar" fit="cover" class="avatar singer-showcase-avatar" />
        <div v-else class="avatar singer-showcase-avatar singer-showcase-avatar-fallback">
          {{ item.avatarText }}
        </div>
        <div class="singer-showcase-name" :title="item.name">{{ item.name }}</div>
      </div>
    </div>
    <el-empty v-else description="暂无符合条件的歌手" :image-size="120" />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listWebDictItems } from '../../api/dict'
import { listWebSingers } from '../../api/singer'

const router = useRouter()
const loading = ref(false)
const singerList = ref([])
const regionOptions = ref([{ label: '全部', value: '' }])
const styleOptions = ref([{ label: '全部', value: '' }])

const filters = reactive({
  letter: '',
  region: '',
  gender: '',
  type: '',
  tag: '',
})

const letterOptions = [
  { label: '全部', value: '' },
  ...'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('').map((item) => ({ label: item, value: item })),
  { label: '#', value: '#' },
]

const genderOptions = [
  { label: '全部', value: '' },
  { label: '男', value: 'male' },
  { label: '女', value: 'female' },
  { label: '组合', value: 'group' },
]

const activeGenderFilter = computed(() => {
  if (filters.type === 'group') {
    return 'group'
  }
  return filters.gender
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

function normalizeSinger(item = {}) {
  return {
    ...item,
    avatar: normalizeFileUrl(item.avatar || ''),
    avatarText: resolveAvatarText(item.name),
  }
}

function resolveAvatarText(name) {
  return String(name || '').trim().slice(0, 1) || '#'
}

function openSingerDetail(id) {
  router.push(`/singers/${id}`)
}

function setFilter(key, value) {
  filters[key] = value
  fetchSingerList()
}

function setGenderFilter(value) {
  if (value === 'group') {
    filters.gender = ''
    filters.type = 'group'
  } else {
    filters.gender = value
    filters.type = ''
  }
  fetchSingerList()
}

async function fetchDictOptions() {
  const [countryData, styleData] = await Promise.all([
    listWebDictItems({ typeCode: 'country' }),
    listWebDictItems({ typeCode: 'style' }),
  ])
  regionOptions.value = [
    { label: '全部', value: '' },
    ...(countryData?.list || []).map((item) => ({
      label: item.name,
      value: item.name,
    })),
  ]
  styleOptions.value = [
    { label: '全部', value: '' },
    ...(styleData?.list || []).map((item) => ({
      label: item.name,
      value: item.name,
    })),
  ]
}

async function fetchSingerList() {
  loading.value = true
  try {
    const data = await listWebSingers({
      pageNo: 1,
      pageSize: 200,
      letter: filters.letter,
      region: filters.region,
      gender: filters.gender,
      type: filters.type,
      tag: filters.tag,
    })
    singerList.value = (data?.list || []).map(normalizeSinger)
  } finally {
    loading.value = false
  }
}

async function initPage() {
  loading.value = true
  try {
    await fetchDictOptions()
    await fetchSingerList()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  initPage()
})
</script>
