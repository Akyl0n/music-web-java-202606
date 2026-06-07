<template>
  <div class="song-manage-page">
    <el-card shadow="never" class="module-card song-filter-card">
      <el-form :model="queryForm" label-width="72px" class="song-filter-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="关键词">
              <el-input
                v-model="queryForm.keyword"
                placeholder="请输入歌曲名 / 歌手 / 标签"
                clearable
                @keyup.enter="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="歌手">
              <el-select v-model="queryForm.singerId" placeholder="请选择歌手" filterable clearable>
                <el-option v-for="item in singerOptions" :key="item.id" :label="item.name" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="状态">
              <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="分类">
              <el-select v-model="queryForm.category" placeholder="请选择分类" filterable clearable>
                <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="String(item.id)" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item class="song-filter-actions song-page-actions">
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
              <el-button type="primary" plain @click="handleCreate">新增歌曲</el-button>
              <el-button plain @click="handleStatusChange('enabled')">批量启用</el-button>
              <el-button plain @click="handleStatusChange('disabled')">批量停用</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card shadow="never" class="module-card">
      <el-table v-loading="loading" :data="tableData" @selection-change="selectedRows = $event">
        <el-table-column type="selection" width="48" />
        <el-table-column label="歌曲信息" min-width="320">
          <template #default="{ row }">
            <button type="button" class="song-cell song-preview-trigger" @click="handlePreview(row)">
              <el-image
                v-if="row.displayCover"
                :src="row.displayCover"
                fit="cover"
                class="song-cover-image"
                preview-disabled
              />
              <div v-else class="song-cover-badge">{{ row.name.slice(0, 1) }}</div>
              <div class="song-meta">
                <strong>{{ row.name }}</strong>
                <p>{{ row.subtitle || row.singerName || '暂无副标题' }}</p>
                <span class="song-preview-hint">
                  <i v-if="row.audioUrl" class="iconfont icon-play song-preview-icon"></i>
                  {{ row.audioUrl ? '点击播放' : '未上传音频' }}
                </span>
              </div>
            </button>
          </template>
        </el-table-column>
        <el-table-column prop="singerName" label="歌手" width="120" />
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            {{ row.categoryName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="风格标签" min-width="180">
          <template #default="{ row }">
            <div class="table-tag-list">
              <el-tag v-for="item in row.tags" :key="item" size="small" round>{{ item }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="时长" width="100">
          <template #default="{ row }">
            {{ formatDuration(row.durationSeconds) }}
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="90">
          <template #default="{ row }">
            <el-tag :type="row.recommendFlag === 1 ? 'warning' : 'info'" round>
              {{ row.recommendFlag === 1 ? '推荐' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="热度" width="170">
          <template #default="{ row }">
            <div class="heat-text">
              <span>播放 {{ row.playCount || 0 }}</span>
              <span>点赞 {{ row.likeCount || 0 }}</span>
              <span>收藏 {{ row.favoriteCount || 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" round>{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="primary" @click="handleRowStatusToggle(row)">
                {{ row.status === 'enabled' ? '停用' : '启用' }}
              </el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <el-pagination
          v-model:current-page="pagination.pageNo"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchData"
          @size-change="handleSearch"
        />
      </div>
    </el-card>

    <SongFormDialog
      v-model="dialogVisible"
      :mode="dialogMode"
      :loading="optionLoading"
      :submitting="submitLoading"
      :initial-data="currentSong"
      :singer-options="singerOptions"
      :category-options="categoryOptions"
      :style-options="styleOptions"
      @submit="handleSubmit"
    />

    <el-dialog v-model="previewVisible" title="歌曲预览" width="520px" destroy-on-close>
      <div v-if="previewSong" class="song-preview-panel">
        <el-image
          v-if="previewSong.displayCover"
          :src="previewSong.displayCover"
          fit="cover"
          class="song-preview-cover"
          :preview-src-list="[previewSong.displayCover]"
          preview-teleported
        />
        <div v-else class="song-preview-cover song-cover-badge">{{ previewSong.name.slice(0, 1) }}</div>
        <div class="song-preview-content">
          <h3>{{ previewSong.name }}</h3>
          <p>{{ previewSong.singerName || '未知歌手' }}</p>
          <p>{{ previewSong.categoryName || '未设置分类' }}</p>
          <p>{{ formatDuration(previewSong.durationSeconds) }}</p>
          <audio
            v-if="previewSong.audioUrl"
            :src="previewSong.audioUrl"
            controls
            autoplay
            class="song-preview-audio"
          ></audio>
          <el-empty v-else description="当前歌曲还未上传音频" :image-size="80" />
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listDictItemsByCode } from '../../api/dict'
import { listSingers } from '../../api/singer-real'
import { createSong, deleteSong, listSongs, updateSong, updateSongStatus } from '../../api/song'
import SongFormDialog from './components/SongFormDialog.vue'

const statusOptions = [
  { label: '全部', value: '' },
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' },
]

const queryForm = reactive({
  keyword: '',
  singerId: undefined,
  status: '',
  category: '',
})

const pagination = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0,
})

const loading = ref(false)
const optionLoading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const selectedRows = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const currentSong = ref(createEmptySong())
const previewVisible = ref(false)
const previewSong = ref(null)
const singerOptions = ref([])
const categoryOptions = ref([])
const styleOptions = ref([])

function createEmptySong() {
  return {
    id: undefined,
    name: '',
    subtitle: '',
    singerId: undefined,
    singerName: '',
    category: '',
    categoryName: '',
    tags: [],
    cover: '',
    displayCover: '',
    audioUrl: '',
    durationSeconds: undefined,
    language: '',
    intro: '',
    releaseDate: '',
    status: 'enabled',
    recommendFlag: 0,
    playCount: 0,
    likeCount: 0,
    favoriteCount: 0,
    remark: '',
  }
}

function getStatusType(status) {
  return status === 'enabled' ? 'success' : 'info'
}

function getStatusLabel(status) {
  return status === 'enabled' ? '启用' : '停用'
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

function mapTags(tags) {
  if (!tags) {
    return []
  }
  if (Array.isArray(tags)) {
    return tags
  }
  return String(tags).split(',').map((item) => item.trim()).filter(Boolean)
}

function resolveSingerAvatar(row) {
  const singer = singerOptions.value.find((item) => item.id === row.singerId)
  return normalizeFileUrl(singer?.avatar || '')
}

function normalizeSong(row = {}) {
  const cover = normalizeFileUrl(row.cover || '')
  const singerAvatar = resolveSingerAvatar(row)
  const categoryName = resolveCategoryName(row.category)
  return {
    ...createEmptySong(),
    ...row,
    cover,
    displayCover: cover || singerAvatar,
    category: row.category ? String(row.category) : '',
    categoryName,
    audioUrl: normalizeFileUrl(row.audioUrl || ''),
    tags: mapTags(row.tags),
    recommendFlag: row.recommendFlag === 1 ? 1 : 0,
  }
}

function resolveCategoryName(categoryId) {
  if (!categoryId && categoryId !== 0) {
    return ''
  }
  const match = categoryOptions.value.find((item) => String(item.id) === String(categoryId))
  return match?.name || String(categoryId)
}

function formatDuration(durationSeconds) {
  if (!durationSeconds && durationSeconds !== 0) {
    return '-'
  }
  const minutes = Math.floor(durationSeconds / 60).toString().padStart(2, '0')
  const seconds = Math.floor(durationSeconds % 60).toString().padStart(2, '0')
  return `${minutes}:${seconds}`
}

async function fetchOptions() {
  optionLoading.value = true
  try {
    const [singerResult, categoryResult, styleResult] = await Promise.all([
      listSingers({ pageNo: 1, pageSize: 1000 }),
      listDictItemsByCode('category'),
      listDictItemsByCode('style'),
    ])
    singerOptions.value = singerResult.list || []
    categoryOptions.value = categoryResult.list || []
    styleOptions.value = styleResult.list || []
  } finally {
    optionLoading.value = false
  }
}

async function fetchData() {
  loading.value = true
  try {
    const { list, total } = await listSongs({
      ...queryForm,
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize,
    })
    tableData.value = Array.isArray(list) ? list.map(normalizeSong) : []
    pagination.total = total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNo = 1
  fetchData()
}

function handleReset() {
  Object.assign(queryForm, {
    keyword: '',
    singerId: undefined,
    status: '',
    category: '',
  })
  handleSearch()
}

function handleCreate() {
  dialogMode.value = 'create'
  currentSong.value = createEmptySong()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogMode.value = 'edit'
  currentSong.value = normalizeSong(row)
  dialogVisible.value = true
}

function handlePreview(row) {
  previewSong.value = normalizeSong(row)
  previewVisible.value = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除歌曲“${row.name}”吗？`, '删除确认', { type: 'warning' })
  await deleteSong(row.id)
  ElMessage.success('删除成功')
  await fetchData()
}

async function handleStatusChange(status) {
  const ids = selectedRows.value.map((item) => item.id)
  if (!ids.length) {
    ElMessage.warning('请先选择歌曲')
    return
  }
  await updateSongStatus(ids, status)
  ElMessage.success('状态已更新')
  await fetchData()
}

async function handleRowStatusToggle(row) {
  const nextStatus = row.status === 'enabled' ? 'disabled' : 'enabled'
  await updateSongStatus([row.id], nextStatus)
  ElMessage.success('状态已更新')
  await fetchData()
}

async function handleSubmit(payload) {
  submitLoading.value = true
  try {
    if (dialogMode.value === 'create') {
      await createSong(payload)
      ElMessage.success('新增成功')
    } else {
      await updateSong(payload)
      ElMessage.success('编辑成功')
    }
    dialogVisible.value = false
    await fetchData()
  } finally {
    submitLoading.value = false
  }
}

async function initPage() {
  await fetchOptions()
  await fetchData()
}

initPage()
</script>

<style scoped>
.song-manage-page :deep(.el-table) {
  width: 100%;
}

.song-manage-page :deep(.el-select) {
  width: 100%;
}

.song-manage-page :deep(.song-filter-form) {
  display: block;
}

.song-manage-page :deep(.song-filter-form .el-row) {
  width: 100%;
}

.song-manage-page :deep(.song-filter-form .el-form-item__content) {
  min-width: 0;
}

.song-manage-page :deep(.el-form-item) {
  margin-bottom: 16px;
}

.song-page-actions {
  margin-bottom: 0;
}

.song-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.song-preview-trigger {
  width: 100%;
  padding: 0;
  border: none;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.song-preview-trigger:hover .song-meta strong {
  color: #31c27c;
}

.song-cover-image,
.song-cover-badge {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  flex-shrink: 0;
}

.song-cover-image {
  overflow: hidden;
  background: #edf8f1;
}

.song-cover-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #7be0b1, #31c27c);
  color: #fff;
  font-weight: 600;
}

.song-meta p {
  margin: 4px 0 0;
  color: #5f7f6d;
  font-size: 12px;
}

.song-preview-hint {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 4px;
  color: #31c27c;
  font-size: 12px;
}

.song-preview-icon {
  font-size: 12px;
  line-height: 1;
}

.heat-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: #5f7f6d;
  font-size: 12px;
}

.table-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.song-preview-panel {
  display: flex;
  align-items: flex-start;
  gap: 18px;
}

.song-preview-cover {
  width: 160px;
  height: 160px;
  border-radius: 22px;
  overflow: hidden;
  background: #edf8f1;
  flex-shrink: 0;
}

.song-preview-content {
  flex: 1;
  min-width: 0;
}

.song-preview-content h3 {
  margin: 0 0 10px;
  font-size: 22px;
}

.song-preview-content p {
  margin: 0 0 8px;
  color: #5f7f6d;
}

.song-preview-audio {
  width: 100%;
  margin-top: 10px;
}

@media (max-width: 768px) {
  .song-preview-panel {
    flex-direction: column;
  }
}
</style>


