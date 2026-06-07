<template>
  <div class="song-manage-page singer-manage-page">
    <el-card shadow="never" class="module-card song-filter-card">
      <el-form :model="queryForm" label-width="72px" class="song-filter-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="关键词">
              <el-input v-model="queryForm.keyword" placeholder="请输入歌手名 / 风格 / 地区" clearable
                @keyup.enter="handleSearch" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="类型">
              <el-select v-model="queryForm.type" placeholder="请选择歌手类型" clearable>
                <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="状态">
              <el-select v-model="queryForm.status" placeholder="请选择状态" clearable>
                <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item class="song-filter-actions singer-filter-actions">
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
              <el-button type="primary" plain @click="handleCreate">新增歌手</el-button>
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
        <el-table-column label="歌手信息" min-width="240">
          <template #default="{ row }">
            <div class="singer-info-cell">
              <el-image v-if="row.avatar" :src="row.avatar" fit="cover" class="singer-avatar-thumb" preview-disabled />
              <div v-else class="artist-avatar-badge">{{ row.name.slice(0, 1) }}</div>
              <div>
                <strong>{{ row.name }}</strong>
                <p>{{ row.region || '未填写地区' }} / {{ row.letter || '#' }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="类型/性别" width="150">
          <template #default="{ row }">
            <div>{{ getTypeLabel(row.type) }}</div>
            <span class="table-sub-text">{{ getGenderLabel(row.gender) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="tags" label="风格标签" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" round>{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180" />
        <el-table-column label="简介" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="artist-intro-text">{{ row.intro || '暂无简介' }}</span>
          </template>
        </el-table-column>
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
        <el-pagination v-model:current-page="pagination.pageNo" v-model:page-size="pagination.pageSize"
          :total="pagination.total" layout="total, sizes, prev, pager, next, jumper" @current-change="fetchData"
          @size-change="handleSearch" />
      </div>
    </el-card>

    <SingerFormDialog v-model="dialogVisible" :mode="dialogMode" :loading="optionLoading" :submitting="submitLoading"
      :initial-data="currentSinger" :country-options="countryOptions" :style-options="styleOptions"
      @submit="handleSubmit" />
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listDictItemsByCode } from '../../api/dict'
import { createSinger, deleteSinger, listSingers, updateSinger, updateSingerStatus } from '../../api/singer-real'
import SingerFormDialog from './components/SingerFormDialog.vue'

const typeOptions = [
  { label: '独立歌手', value: 'solo' },
  { label: '组合/乐队', value: 'group' },
  { label: '虚拟歌手', value: 'virtual' },
]

const statusOptions = [
  { label: '全部', value: '' },
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' },
]

const genderOptions = [
  { label: '未知', value: 'unknown' },
  { label: '男', value: 'male' },
  { label: '女', value: 'female' },
  { label: '组合', value: 'group' },
]

const queryForm = reactive({
  keyword: '',
  type: '',
  status: '',
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
const currentSinger = ref(createEmptySinger())
const countryOptions = ref([])
const styleOptions = ref([])

function createEmptySinger() {
  return {
    id: undefined,
    name: '',
    gender: 'unknown',
    birthday: '',
    region: '',
    type: 'solo',
    avatar: '',
    intro: '',
    tags: [],
    letter: '',
    status: 'enabled',
    remark: '',
  }
}

function getStatusType(status) {
  return status === 'enabled' ? 'success' : 'info'
}

function getStatusLabel(status) {
  return status === 'enabled' ? '启用' : '停用'
}

function getTypeLabel(type) {
  return typeOptions.find((item) => item.value === type)?.label || type || '-'
}

function getGenderLabel(gender) {
  return genderOptions.find((item) => item.value === gender)?.label || '-'
}

function normalizeAvatarUrl(url) {
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
  return String(tags)
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
}

function normalizeSinger(row = {}) {
  return {
    ...createEmptySinger(),
    ...row,
    avatar: normalizeAvatarUrl(row.avatar || ''),
    tags: mapTags(row.tags),
  }
}

async function fetchDictOptions() {
  optionLoading.value = true
  try {
    const [countryResult, styleResult] = await Promise.all([
      listDictItemsByCode('country'),
      listDictItemsByCode('style'),
    ])
    countryOptions.value = countryResult.list || []
    styleOptions.value = styleResult.list || []
  } finally {
    optionLoading.value = false
  }
}

async function fetchData() {
  loading.value = true
  try {
    const { list, total } = await listSingers({
      ...queryForm,
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize,
    })
    tableData.value = Array.isArray(list) ? list.map(normalizeSinger) : []
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
    type: '',
    status: '',
  })
  handleSearch()
}

function handleCreate() {
  dialogMode.value = 'create'
  currentSinger.value = createEmptySinger()
  dialogVisible.value = true
}

function handleEdit(row) {
  dialogMode.value = 'edit'
  currentSinger.value = normalizeSinger(row)
  dialogVisible.value = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除歌手“${row.name}”吗？`, '删除确认', { type: 'warning' })
  await deleteSinger(row.id)
  ElMessage.success('删除成功')
  await fetchData()
}

async function handleStatusChange(status) {
  const ids = selectedRows.value.map((item) => item.id)
  if (!ids.length) {
    ElMessage.warning('请先选择歌手')
    return
  }
  await updateSingerStatus(ids, status)
  ElMessage.success('状态已更新')
  await fetchData()
}

async function handleRowStatusToggle(row) {
  const nextStatus = row.status === 'enabled' ? 'disabled' : 'enabled'
  await updateSingerStatus([row.id], nextStatus)
  ElMessage.success('状态已更新')
  await fetchData()
}

async function handleSubmit(payload) {
  submitLoading.value = true
  try {
    if (dialogMode.value === 'create') {
      await createSinger(payload)
      ElMessage.success('新增成功')
    } else {
      await updateSinger(payload)
      ElMessage.success('编辑成功')
    }
    dialogVisible.value = false
    await fetchData()
  } finally {
    submitLoading.value = false
  }
}

async function initPage() {
  await fetchDictOptions()
  await fetchData()
}

initPage()
</script>

<style scoped>
.singer-manage-page :deep(.el-table) {
  width: 100%;
}

.singer-manage-page :deep(.el-select) {
  width: 100%;
}

.singer-manage-page :deep(.el-form-item) {
  margin-bottom: 16px;
}

.singer-filter-actions {
  margin-bottom: 0;
}

.singer-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.singer-avatar-thumb {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  flex-shrink: 0;
  overflow: hidden;
  background: #edf8f1;
}

.table-sub-text {
  color: #5f7f6d;
  font-size: 12px;
}
</style>


