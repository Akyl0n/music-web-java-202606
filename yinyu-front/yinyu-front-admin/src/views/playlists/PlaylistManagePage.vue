<template>
  <div class="playlist-manage-page">
    <el-card shadow="never" class="module-card">
      <el-form :model="queryForm" label-width="72px" class="song-filter-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="关键词">
              <el-input
                v-model="queryForm.keyword"
                placeholder="请输入歌单名 / 标签"
                clearable
                @keyup.enter="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="状态">
              <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="分类">
              <el-select v-model="queryForm.category" placeholder="请选择分类" filterable clearable>
                <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="String(item.id)" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item class="song-filter-actions playlist-page-actions">
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
              <el-button type="primary" plain @click="handleCreate">新增歌单</el-button>
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
        <el-table-column label="歌单信息" min-width="320">
          <template #default="{ row }">
            <div class="playlist-info-cell">
              <el-image v-if="row.cover" :src="row.cover" fit="cover" class="playlist-cover-image" preview-disabled />
              <div v-else class="playlist-cover-badge">{{ row.name.slice(0, 1) }}</div>
              <div class="playlist-info-content">
                <strong>{{ row.name }}</strong>
                <p>{{ row.subtitle || '暂无副标题' }}</p>
                <el-button link type="primary" class="playlist-song-entry" @click="handleViewSongs(row)">
                  <i class="iconfont icon-list"></i>
                  {{ row.songCount || 0 }} 首歌曲
                </el-button>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            {{ row.categoryName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="songCount" label="歌曲数" width="90" />
        <el-table-column label="风格标签" min-width="180">
          <template #default="{ row }">
            <div class="table-tag-list">
              <el-tag v-for="item in row.tags" :key="item" size="small" round>{{ item }}</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="推荐" width="90">
          <template #default="{ row }">
            <el-tag :type="row.recommendFlag === 1 ? 'warning' : 'info'" round>
              {{ row.recommendFlag === 1 ? '推荐' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="热度" width="130">
          <template #default="{ row }">
            <div class="heat-text">
              <span>播放 {{ row.playCount || 0 }}</span>
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

    <PlaylistFormDialog
      v-model="dialogVisible"
      :mode="dialogMode"
      :loading="optionLoading || detailLoading"
      :submitting="submitLoading"
      :initial-data="currentPlaylist"
      :category-options="categoryOptions"
      :style-options="styleOptions"
      :song-options="songOptions"
      @submit="handleSubmit"
    />

    <el-dialog
      v-model="songViewVisible"
      title="歌单歌曲"
      width="1120px"
      destroy-on-close
      class="playlist-song-view-dialog"
    >
      <div v-loading="songViewLoading || songOrderSaving" class="playlist-song-view-layout">
        <div class="playlist-song-view-list">
          <div class="playlist-song-view-head">
            <div>
              <h3>{{ songViewPlaylist.name || '歌单歌曲' }}</h3>
              <p>拖动左侧列表可以调整顺序，点击歌曲即可在右侧试听。</p>
            </div>
            <div class="playlist-song-view-meta">共 {{ songViewSongs.length }} 首</div>
          </div>

          <div v-if="songViewSongs.length" class="playlist-song-view-scroll">
            <div
              v-for="(item, index) in songViewSongs"
              :key="item.id"
              class="playlist-song-view-row"
              :class="{ 'is-active': activeSong?.id === item.id }"
              draggable="true"
              @click="handleSongPlay(item)"
              @dragstart="handleSongViewDragStart(index)"
              @dragover.prevent
              @drop="handleSongViewDrop(index)"
            >
              <div class="playlist-song-view-row-left">
                <span class="playlist-song-drag-handle">
                  <i class="iconfont icon-drag"></i>
                </span>
                <div class="playlist-song-view-order">{{ index + 1 }}</div>
                <el-image
                  v-if="item.displayCover"
                  :src="item.displayCover"
                  fit="cover"
                  class="playlist-song-view-cover"
                  preview-disabled
                />
                <div v-else class="playlist-song-view-cover playlist-song-view-cover-badge">
                  {{ item.name.slice(0, 1) }}
                </div>
                <div class="playlist-song-view-content">
                  <strong class="playlist-song-view-title">{{ item.name }}</strong>
                  <p>{{ item.singerName || '未知歌手' }}</p>
                  <div class="playlist-song-view-subline">
                    <span>{{ item.categoryName || '未设置分类' }}</span>
                  </div>
                </div>
              </div>
              <div class="playlist-song-view-row-right">
                <span class="playlist-song-view-duration">{{ formatDuration(item.durationSeconds) }}</span>
                <span class="playlist-song-view-play">
                  <i v-if="item.audioUrl" class="iconfont icon-play"></i>
                  {{ item.audioUrl ? '点击播放' : '暂无音频' }}
                </span>
              </div>
            </div>
          </div>

          <el-empty v-else description="当前歌单还没有添加歌曲" :image-size="90" />
        </div>

        <div class="playlist-song-player-card">
          <div class="playlist-song-player-head">
            <h3>正在播放</h3>
            <p>确认顺序后点击保存，歌单中的歌曲顺序会同步更新。</p>
          </div>

          <div v-if="activeSong" class="playlist-song-player-body">
            <el-image
              v-if="activeSong.displayCover"
              :src="activeSong.displayCover"
              fit="cover"
              class="playlist-song-player-cover"
              :preview-src-list="[activeSong.displayCover]"
              preview-teleported
            />
            <div v-else class="playlist-song-player-cover playlist-song-view-cover-badge">
              {{ activeSong.name.slice(0, 1) }}
            </div>
            <h4>{{ activeSong.name }}</h4>
            <p>{{ activeSong.singerName || '未知歌手' }}</p>
            <p>{{ activeSong.categoryName || '未设置分类' }}</p>
            <p>{{ formatDuration(activeSong.durationSeconds) }}</p>
            <audio
              v-if="activeSong.audioUrl"
              :key="activeSong.id"
              :src="activeSong.audioUrl"
              controls
              autoplay
              class="playlist-song-player-audio"
            ></audio>
            <el-empty v-else description="当前歌曲还未上传音频" :image-size="80" />
          </div>

          <el-empty v-else description="请先从左侧选择一首歌曲" :image-size="90" />
        </div>
      </div>

      <template #footer>
        <el-button @click="songViewVisible = false">关闭</el-button>
        <el-button
          type="primary"
          :loading="songOrderSaving"
          :disabled="!songViewSongs.length"
          @click="handleSaveSongOrder"
        >
          保存顺序
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listDictItemsByCode } from '../../api/dict'
import { listSongs } from '../../api/song'
import {
  createPlaylist,
  deletePlaylist,
  getPlaylistDetail,
  listPlaylists,
  updatePlaylist,
  updatePlaylistStatus,
} from '../../api/playlist'
import PlaylistFormDialog from './components/PlaylistFormDialog.vue'

const statusOptions = [
  { label: '全部', value: '' },
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' },
]

const queryForm = reactive({
  keyword: '',
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
const detailLoading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const selectedRows = ref([])
const dialogVisible = ref(false)
const dialogMode = ref('create')
const currentPlaylist = ref(createEmptyPlaylist())
const songViewVisible = ref(false)
const songViewLoading = ref(false)
const songOrderSaving = ref(false)
const songViewDragIndex = ref(-1)
const songViewPlaylist = ref(createEmptyPlaylist())
const activeSongId = ref(undefined)
const categoryOptions = ref([])
const styleOptions = ref([])
const songOptions = ref([])

const songViewSongs = computed(() =>
  songViewPlaylist.value.songIds
    .map((id) => normalizeSongItem(songOptions.value.find((item) => item.id === id) || { id }))
    .filter((item) => item.id !== undefined),
)

const activeSong = computed(() => {
  if (!songViewSongs.value.length) {
    return null
  }
  return songViewSongs.value.find((item) => item.id === activeSongId.value) || songViewSongs.value[0]
})

function createEmptyPlaylist() {
  return {
    id: undefined,
    name: '',
    subtitle: '',
    cover: '',
    category: '',
    categoryName: '',
    tags: [],
    intro: '',
    status: 'enabled',
    recommendFlag: 0,
    songCount: 0,
    playCount: 0,
    favoriteCount: 0,
    songIds: [],
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

function resolveCategoryName(categoryId) {
  if (!categoryId && categoryId !== 0) {
    return ''
  }
  const match = categoryOptions.value.find((item) => String(item.id) === String(categoryId))
  return match?.name || String(categoryId)
}

function normalizePlaylist(row = {}) {
  return {
    ...createEmptyPlaylist(),
    ...row,
    cover: normalizeFileUrl(row.cover || ''),
    category: row.category ? String(row.category) : '',
    categoryName: resolveCategoryName(row.category),
    tags: mapTags(row.tags),
    songIds: Array.isArray(row.songIds) ? row.songIds : [],
    recommendFlag: row.recommendFlag === 1 ? 1 : 0,
  }
}

function normalizeSongItem(row = {}) {
  const cover = normalizeFileUrl(row.cover || '')
  return {
    ...row,
    id: row.id,
    name: row.name || `歌曲 ${row.id || ''}`.trim(),
    singerName: row.singerName || '',
    categoryName: resolveCategoryName(row.category),
    durationSeconds: row.durationSeconds,
    audioUrl: normalizeFileUrl(row.audioUrl || ''),
    displayCover: cover,
  }
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
    const [categoryResult, styleResult, songResult] = await Promise.all([
      listDictItemsByCode('category'),
      listDictItemsByCode('style'),
      listSongs({ pageNo: 1, pageSize: 1000 }),
    ])
    categoryOptions.value = categoryResult.list || []
    styleOptions.value = styleResult.list || []
    songOptions.value = Array.isArray(songResult.list) ? songResult.list.map(normalizeSongItem) : []
  } finally {
    optionLoading.value = false
  }
}

async function fetchData() {
  loading.value = true
  try {
    const { list, total } = await listPlaylists({
      ...queryForm,
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize,
    })
    tableData.value = Array.isArray(list) ? list.map(normalizePlaylist) : []
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
    status: '',
    category: '',
  })
  handleSearch()
}

function handleCreate() {
  dialogMode.value = 'create'
  currentPlaylist.value = createEmptyPlaylist()
  dialogVisible.value = true
}

async function handleEdit(row) {
  dialogMode.value = 'edit'
  detailLoading.value = true
  try {
    const detail = await getPlaylistDetail(row.id)
    currentPlaylist.value = normalizePlaylist(detail)
    dialogVisible.value = true
  } finally {
    detailLoading.value = false
  }
}

async function handleViewSongs(row) {
  songViewLoading.value = true
  songViewDragIndex.value = -1
  try {
    const detail = await getPlaylistDetail(row.id)
    songViewPlaylist.value = normalizePlaylist(detail)
    activeSongId.value = songViewPlaylist.value.songIds[0]
    songViewVisible.value = true
  } finally {
    songViewLoading.value = false
  }
}

function handleSongPlay(song) {
  activeSongId.value = song.id
}

function handleSongViewDragStart(index) {
  songViewDragIndex.value = index
}

function handleSongViewDrop(index) {
  if (songViewDragIndex.value < 0 || songViewDragIndex.value === index) {
    songViewDragIndex.value = -1
    return
  }
  const nextSongIds = [...songViewPlaylist.value.songIds]
  const [moved] = nextSongIds.splice(songViewDragIndex.value, 1)
  nextSongIds.splice(index, 0, moved)
  songViewPlaylist.value = {
    ...songViewPlaylist.value,
    songIds: nextSongIds,
  }
  songViewDragIndex.value = -1
}

async function handleSaveSongOrder() {
  if (!songViewPlaylist.value.id) {
    return
  }
  songOrderSaving.value = true
  try {
    await updatePlaylist({
      id: songViewPlaylist.value.id,
      name: songViewPlaylist.value.name,
      subtitle: songViewPlaylist.value.subtitle || null,
      cover: songViewPlaylist.value.cover || null,
      category: songViewPlaylist.value.category ? String(songViewPlaylist.value.category) : null,
      tags: songViewPlaylist.value.tags.length ? songViewPlaylist.value.tags.join(',') : null,
      intro: songViewPlaylist.value.intro || null,
      status: songViewPlaylist.value.status,
      recommendFlag: songViewPlaylist.value.recommendFlag === 1 ? 1 : 0,
      songIds: songViewPlaylist.value.songIds,
      remark: songViewPlaylist.value.remark || null,
    })
    ElMessage.success('歌曲顺序已保存')
    await fetchData()
  } finally {
    songOrderSaving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除歌单“${row.name}”吗？`, '删除确认', { type: 'warning' })
  await deletePlaylist(row.id)
  ElMessage.success('删除成功')
  await fetchData()
}

async function handleStatusChange(status) {
  const ids = selectedRows.value.map((item) => item.id)
  if (!ids.length) {
    ElMessage.warning('请先选择歌单')
    return
  }
  await updatePlaylistStatus(ids, status)
  ElMessage.success('状态已更新')
  await fetchData()
}

async function handleRowStatusToggle(row) {
  const nextStatus = row.status === 'enabled' ? 'disabled' : 'enabled'
  await updatePlaylistStatus([row.id], nextStatus)
  ElMessage.success('状态已更新')
  await fetchData()
}

async function handleSubmit(payload) {
  submitLoading.value = true
  try {
    if (dialogMode.value === 'create') {
      await createPlaylist(payload)
      ElMessage.success('新增成功')
    } else {
      await updatePlaylist(payload)
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
.playlist-manage-page :deep(.el-table) {
  width: 100%;
}

.playlist-manage-page :deep(.el-select) {
  width: 100%;
}

.playlist-manage-page :deep(.song-filter-form) {
  display: block;
}

.playlist-manage-page :deep(.song-filter-form .el-row) {
  width: 100%;
}

.playlist-manage-page :deep(.song-filter-form .el-form-item__content) {
  min-width: 0;
}

.playlist-manage-page :deep(.el-form-item) {
  margin-bottom: 16px;
}

.playlist-page-actions {
  margin-bottom: 0;
}

.playlist-manage-page .module-card + .module-card {
  margin-top: 20px;
}

.playlist-manage-page .module-card {
  border-radius: 24px;
}

.playlist-manage-page :deep(.el-card__body) {
  padding-top: 22px;
}

.playlist-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.playlist-info-content {
  min-width: 0;
}

.playlist-cover-image,
.playlist-cover-badge {
  width: 58px;
  height: 58px;
  border-radius: 18px;
  flex-shrink: 0;
}

.playlist-cover-image {
  overflow: hidden;
  background: linear-gradient(180deg, #f2fbf6, #dff5ea);
  box-shadow: 0 10px 24px rgba(49, 194, 124, 0.14);
}

.playlist-cover-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #79dfaf, #26a364);
  color: #fff;
  font-weight: 600;
  box-shadow: 0 10px 24px rgba(49, 194, 124, 0.16);
}

.playlist-info-cell p {
  margin: 4px 0 0;
  color: #5f7f6d;
  font-size: 12px;
}

.playlist-song-entry {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0;
  margin-top: 8px;
  color: #31c27c;
  font-size: 12px;
}

.playlist-song-entry .iconfont {
  font-size: 12px;
}

.table-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.playlist-manage-page :deep(.table-tag-list .el-tag) {
  border-color: #d1eedf;
  background: #f5fcf8;
  color: #247f50;
}

.heat-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
  color: #5f7f6d;
  font-size: 12px;
}

.playlist-song-view-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(320px, 0.85fr);
  gap: 20px;
}

.playlist-manage-page :deep(.playlist-song-view-dialog .el-dialog__body) {
  padding-top: 16px;
}

.playlist-song-view-list,
.playlist-song-player-card {
  min-width: 0;
  border: 1px solid #e3f3ea;
  border-radius: 22px;
  background: #fff;
}

.playlist-song-view-list {
  padding: 20px;
}

.playlist-song-player-card {
  padding: 20px 18px;
}

.playlist-song-view-head,
.playlist-song-player-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.playlist-song-view-head h3,
.playlist-song-player-head h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.playlist-song-view-head p,
.playlist-song-player-head p {
  margin: 8px 0 0;
  color: #909399;
  font-size: 13px;
}

.playlist-song-view-meta {
  flex-shrink: 0;
  padding: 7px 12px;
  border-radius: 999px;
  background: #f3fbf7;
  color: #606266;
  font-size: 12px;
}

.playlist-song-view-scroll {
  max-height: 560px;
  margin-top: 18px;
  overflow: auto;
  padding: 2px 4px 2px 0;
  box-sizing: border-box;
}

.playlist-song-view-scroll::-webkit-scrollbar {
  width: 6px;
}

.playlist-song-view-scroll::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: #dbeee4;
}

.playlist-song-view-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 14px 16px;
  margin-bottom: 12px;
  border: 1px solid #e8f5ee;
  border-radius: 18px;
  background: #fff;
  box-sizing: border-box;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.playlist-song-view-row:last-child {
  margin-bottom: 0;
}

.playlist-song-view-row:hover,
.playlist-song-view-row.is-active {
  border-color: #87ddb4;
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.06);
  transform: translateY(-1px);
}

.playlist-song-view-row-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  flex: 1;
}

.playlist-song-view-row-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 10px;
  margin-left: 12px;
  flex-shrink: 0;
  min-width: 0;
}

.playlist-song-drag-handle {
  width: 30px;
  height: 30px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: #f3fbf7;
  color: #909399;
  cursor: grab;
  flex-shrink: 0;
}

.playlist-song-view-order {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: #f3fbf7;
  color: #606266;
  font-size: 12px;
  flex-shrink: 0;
}

.playlist-song-view-cover,
.playlist-song-view-cover-badge {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  overflow: hidden;
  flex-shrink: 0;
}

.playlist-song-view-cover {
  background: #edf8f1;
}

.playlist-song-view-cover-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #79dfaf, #26a364);
  color: #fff;
  font-weight: 600;
}

.playlist-song-view-content {
  min-width: 0;
  flex: 1;
}

.playlist-song-view-title {
  display: block;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.playlist-song-view-content p {
  margin: 6px 0 0;
  color: #909399;
  font-size: 12px;
}

.playlist-song-view-subline {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
  flex-wrap: wrap;
}

.playlist-song-view-play {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: 999px;
  background: #f5fcf8;
  color: #31c27c;
  white-space: nowrap;
}

.playlist-song-view-duration {
  flex-shrink: 0;
  color: #606266;
  font-size: 12px;
  white-space: nowrap;
}

.playlist-song-player-body {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 18px;
  text-align: center;
}

.playlist-song-player-cover {
  width: 220px;
  height: 220px;
  border-radius: 28px;
  overflow: hidden;
  background: #edf8f1;
  box-shadow: 0 18px 36px rgba(0, 0, 0, 0.08);
}

.playlist-song-player-body h4 {
  margin: 18px 0 10px;
  font-size: 24px;
  color: #303133;
}

.playlist-song-player-body p {
  margin: 0 0 8px;
  color: #909399;
}

.playlist-song-player-audio {
  width: 100%;
  margin-top: 14px;
}

@media (max-width: 960px) {
  .playlist-song-view-layout {
    grid-template-columns: 1fr;
  }

  .playlist-song-player-cover {
    width: 180px;
    height: 180px;
  }

  .playlist-song-view-row {
    align-items: flex-start;
    flex-direction: column;
  }

  .playlist-song-view-row-left,
  .playlist-song-view-row-right {
    width: 100%;
  }

  .playlist-song-view-row-right {
    flex-direction: row;
    justify-content: space-between;
    margin-top: 12px;
    margin-left: 0;
  }
}
</style>


