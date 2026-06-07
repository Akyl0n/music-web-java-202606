<template>
  <div class="recommend-manage-page">
    <el-card shadow="never" class="module-card">
      <el-form :model="queryForm" label-width="72px" class="song-filter-form recommend-filter-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="展示位">
              <el-select v-model="queryForm.positionCode" placeholder="请选择展示位" clearable>
                <el-option v-for="item in positionOptions" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="类型">
              <el-select v-model="queryForm.targetType" placeholder="请选择类型" clearable>
                <el-option v-for="item in targetTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
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
            <el-form-item class="song-filter-actions">
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
              <el-button type="primary" plain @click="handleCreate">新增推荐</el-button>
              <el-button plain @click="handleStatusChange('enabled')">批量启用</el-button>
              <el-button plain @click="handleStatusChange('disabled')">批量停用</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card shadow="never" class="module-card">
      <template #header>
        <div class="section-head">
          <div>
            <h3>推荐列表</h3>
            <p>拖拽左侧手柄可调整顺序，新的顺序会立即保存。</p>
          </div>
          <el-checkbox
            :model-value="isAllSelected"
            :indeterminate="isPartiallySelected"
            @change="toggleSelectAll"
          >
            全选当前页
          </el-checkbox>
        </div>
      </template>

      <div v-loading="loading" class="recommend-sort-list">
        <div
          v-for="row in tableData"
          :key="row.id"
          class="recommend-sort-item"
          draggable="true"
          @dragstart="onDragStart(row)"
          @dragover.prevent
          @drop="onDrop(row)"
        >
          <div class="recommend-sort-main">
            <div class="recommend-control-cell">
              <el-checkbox :model-value="selectedIds.includes(row.id)" @change="toggleSelection(row.id, $event)" />
              <button class="drag-handle" type="button" @click.stop>
                <i class="iconfont icon-drag"></i>
              </button>
            </div>

            <div class="recommend-cover-cell">
              <el-image
                v-if="resolveCover(row)"
                :src="resolveCover(row)"
                fit="cover"
                :class="['recommend-cover-thumb', getCoverClass(row.positionCode)]"
              />
              <div v-else :class="['recommend-cover-badge', getCoverClass(row.positionCode)]">
                {{ resolveAvatarText(row.displayTitle) }}
              </div>
            </div>

            <div class="recommend-info-cell">
              <div class="recommend-title-row">
                <strong>{{ row.displayTitle }}</strong>
                <span class="recommend-order-badge">排序 {{ row.sortNum }}</span>
              </div>
              <div class="recommend-meta-row">
                <el-tag size="small" round>{{ row.positionLabel }}</el-tag>
                <el-tag size="small" round effect="plain">{{ row.targetTypeLabel }}</el-tag>
                <span class="recommend-target-text">关联内容：{{ row.targetName || '-' }}</span>
              </div>
            </div>
          </div>

          <div class="recommend-sort-side">
            <div class="recommend-status-row">
              <el-tag :type="row.status === 'enabled' ? 'success' : 'info'" round>
                {{ row.status === 'enabled' ? '启用' : '停用' }}
              </el-tag>
            </div>
            <div class="table-actions recommend-action-row">
              <el-button link type="primary" @click.stop="handleEdit(row)">编辑</el-button>
              <el-button link type="primary" @click.stop="handleToggle(row)">
                {{ row.status === 'enabled' ? '停用' : '启用' }}
              </el-button>
              <el-button link type="danger" @click.stop="handleDelete(row)">删除</el-button>
            </div>
          </div>
        </div>
      </div>

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

    <RecommendFormDialog
      v-model="dialogVisible"
      :mode="dialogMode"
      :loading="detailLoading"
      :submitting="submitLoading"
      :initial-data="currentRecord"
      :target-options="targetOptions"
      :target-options-loading="targetOptionsLoading"
      @submit="handleSubmit"
      @target-type-change="fetchTargetOptions"
    />
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createHomeRecommend,
  deleteHomeRecommend,
  getHomeRecommendDetail,
  listHomeRecommendTargetOptions,
  listHomeRecommends,
  sortHomeRecommends,
  updateHomeRecommend,
  updateHomeRecommendStatus,
} from '../../api/recommend'
import RecommendFormDialog from './components/RecommendFormDialog.vue'

const positionOptions = [
  { label: '全部', value: '' },
  { label: '首页轮播', value: 'home_banner' },
  { label: '今日推荐', value: 'home_daily_song' },
]

const targetTypeOptions = [
  { label: '全部', value: '' },
  { label: '歌曲', value: 'song' },
  { label: '歌单', value: 'playlist' },
  { label: '歌手', value: 'singer' },
]

const statusOptions = [
  { label: '全部', value: '' },
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' },
]

const queryForm = reactive({
  positionCode: '',
  targetType: '',
  status: '',
})

const pagination = reactive({
  pageNo: 1,
  pageSize: 10,
  total: 0,
})

const loading = ref(false)
const detailLoading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogMode = ref('create')
const tableData = ref([])
const selectedIds = ref([])
const currentRecord = ref(createEmptyRecord())
const targetOptions = ref([])
const targetOptionsLoading = ref(false)
const draggingId = ref()

const isAllSelected = computed(() => tableData.value.length > 0 && tableData.value.every((item) => selectedIds.value.includes(item.id)))
const isPartiallySelected = computed(() => selectedIds.value.length > 0 && !isAllSelected.value)

function createEmptyRecord() {
  return {
    id: undefined,
    positionCode: 'home_banner',
    targetType: 'song',
    targetId: undefined,
    cover: '',
    status: 'enabled',
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

function normalizeRecord(item = {}) {
  return {
    ...item,
    cover: normalizeFileUrl(item.cover || ''),
    targetCover: normalizeFileUrl(item.targetCover || ''),
    displayTitle: item.targetName || '未命名推荐',
  }
}

function resolveCover(row) {
  return row.cover || row.targetCover || ''
}

function resolveAvatarText(name) {
  return String(name || '').trim().slice(0, 1) || '推'
}

function getCoverClass(positionCode) {
  return positionCode === 'home_banner' ? 'is-banner' : 'is-daily-song'
}

function toggleSelection(id, checked) {
  if (checked) {
    if (!selectedIds.value.includes(id)) {
      selectedIds.value = [...selectedIds.value, id]
    }
    return
  }
  selectedIds.value = selectedIds.value.filter((item) => item !== id)
}

function toggleSelectAll(checked) {
  selectedIds.value = checked ? tableData.value.map((item) => item.id) : []
}

async function fetchData() {
  loading.value = true
  try {
    const data = await listHomeRecommends({
      ...queryForm,
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize,
    })
    tableData.value = (data?.list || []).map(normalizeRecord)
    pagination.total = Number(data?.total || 0)
    selectedIds.value = selectedIds.value.filter((id) => tableData.value.some((item) => item.id === id))
  } catch (error) {
    ElMessage.error(error.message || '获取首页推荐失败')
  } finally {
    loading.value = false
  }
}

async function fetchTargetOptions(targetType) {
  if (!targetType) {
    targetOptions.value = []
    return
  }
  targetOptionsLoading.value = true
  try {
    const data = await listHomeRecommendTargetOptions(targetType)
    targetOptions.value = Array.isArray(data)
      ? data.map((item) => ({
          ...item,
          cover: normalizeFileUrl(item.cover || ''),
        }))
      : []
  } catch (error) {
    ElMessage.error(error.message || '获取关联内容失败')
  } finally {
    targetOptionsLoading.value = false
  }
}

function handleSearch() {
  pagination.pageNo = 1
  fetchData()
}

function handleReset() {
  Object.assign(queryForm, {
    positionCode: '',
    targetType: '',
    status: '',
  })
  handleSearch()
}

async function handleCreate() {
  dialogMode.value = 'create'
  currentRecord.value = createEmptyRecord()
  targetOptions.value = []
  await fetchTargetOptions(currentRecord.value.targetType)
  dialogVisible.value = true
}

async function handleEdit(row) {
  dialogMode.value = 'edit'
  detailLoading.value = true
  try {
    const data = await getHomeRecommendDetail(row.id)
    currentRecord.value = normalizeRecord(data || {})
    await fetchTargetOptions(currentRecord.value.targetType)
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '获取推荐详情失败')
  } finally {
    detailLoading.value = false
  }
}

async function handleSubmit(payload) {
  submitLoading.value = true
  try {
    if (dialogMode.value === 'create') {
      await createHomeRecommend(payload)
      ElMessage.success('新增成功')
    } else {
      await updateHomeRecommend(payload)
      ElMessage.success('更新成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除推荐“${row.displayTitle}”吗？`, '删除提示', {
    type: 'warning',
  })
  try {
    await deleteHomeRecommend(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (error) {
    ElMessage.error(error.message || '删除失败')
  }
}

async function handleToggle(row) {
  try {
    await updateHomeRecommendStatus([row.id], row.status === 'enabled' ? 'disabled' : 'enabled')
    ElMessage.success('状态更新成功')
    fetchData()
  } catch (error) {
    ElMessage.error(error.message || '状态更新失败')
  }
}

async function handleStatusChange(status) {
  if (!selectedIds.value.length) {
    ElMessage.warning('请先选择数据')
    return
  }
  try {
    await updateHomeRecommendStatus(selectedIds.value, status)
    ElMessage.success('批量操作成功')
    fetchData()
  } catch (error) {
    ElMessage.error(error.message || '批量操作失败')
  }
}

function onDragStart(row) {
  draggingId.value = row.id
}

async function onDrop(targetRow) {
  if (!draggingId.value || draggingId.value === targetRow.id) {
    draggingId.value = undefined
    return
  }
  const list = [...tableData.value]
  const fromIndex = list.findIndex((item) => item.id === draggingId.value)
  const toIndex = list.findIndex((item) => item.id === targetRow.id)
  const [moved] = list.splice(fromIndex, 1)
  list.splice(toIndex, 0, moved)
  tableData.value = list
  try {
    await sortHomeRecommends(list.map((item) => item.id))
    ElMessage.success('排序已更新')
    await fetchData()
  } catch (error) {
    ElMessage.error(error.message || '排序更新失败')
  } finally {
    draggingId.value = undefined
  }
}

fetchData()
</script>

<style scoped>
.recommend-manage-page {
  display: grid;
  gap: 24px;
}

.recommend-filter-form {
  margin-bottom: 8px;
}

.recommend-sort-list {
  display: grid;
  gap: 14px;
}

.recommend-sort-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 18px 20px;
  border: 1px solid rgba(49, 194, 124, 0.12);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.9);
}

.recommend-sort-main {
  min-width: 0;
  display: flex;
  align-items: flex-start;
  gap: 18px;
  flex: 1;
}

.recommend-control-cell {
  width: 56px;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
  padding-top: 18px;
}

.recommend-cover-cell {
  width: 156px;
  display: flex;
  justify-content: flex-start;
  flex-shrink: 0;
}

.recommend-info-cell {
  flex: 1;
  min-width: 0;
  padding-top: 2px;
}

.recommend-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 6px;
}

.recommend-order-badge {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(49, 194, 124, 0.12);
  color: #2f8e61;
  font-size: 12px;
  white-space: nowrap;
}

.recommend-sort-side {
  width: 186px;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 14px;
  flex-shrink: 0;
  padding-top: 2px;
}

.recommend-status-row {
  width: 100%;
  display: flex;
  justify-content: flex-end;
}

.recommend-action-row {
  justify-content: flex-end;
  flex-wrap: wrap;
  row-gap: 4px;
}
</style>


