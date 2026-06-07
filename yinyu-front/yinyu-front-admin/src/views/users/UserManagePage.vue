<template>
  <div class="user-manage-page">
    <el-card shadow="never" class="module-card song-filter-card">
      <el-form :model="queryForm" label-width="72px" class="song-filter-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="关键词">
              <el-input
                v-model="queryForm.keyword"
                placeholder="请输入昵称 / 邮箱"
                clearable
                @keyup.enter="handleSearch"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="性别">
              <el-select v-model="queryForm.gender" placeholder="请选择性别" clearable>
                <el-option v-for="item in genderOptions" :key="item.value" :label="item.label" :value="item.value" />
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
            <el-form-item class="song-filter-actions user-page-actions">
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
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
        <el-table-column label="用户信息" min-width="260">
          <template #default="{ row }">
            <div class="user-info-cell">
              <el-image v-if="row.avatar" :src="row.avatar" fit="cover" class="user-avatar-thumb" preview-disabled />
              <div v-else class="user-avatar-badge">{{ resolveAvatarText(row.nickname) }}</div>
              <div class="user-main-meta">
                <strong>{{ row.nickname || '-' }}</strong>
                <p>{{ row.email || '未填写邮箱' }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="性别" width="100">
          <template #default="{ row }">
            {{ getGenderLabel(row.gender) }}
          </template>
        </el-table-column>
        <el-table-column label="互动数据" min-width="220">
          <template #default="{ row }">
            <div class="user-stats-cell">
              <span>喜欢 {{ row.likeSongCount || 0 }}</span>
              <span>收藏 {{ row.favoritePlaylistCount || 0 }}</span>
              <span>播放 {{ row.playHistoryCount || 0 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" round>{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最近登录" width="180" />
        <el-table-column prop="createTime" label="注册时间" width="180" />
        <el-table-column label="备注" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.remark || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="primary" @click="handleRowStatusToggle(row)">
                {{ row.status === 'enabled' ? '停用' : '启用' }}
              </el-button>
              <el-button link type="warning" @click="handleResetPassword(row)">重置密码</el-button>
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

    <UserFormDialog
      v-model="dialogVisible"
      :loading="detailLoading"
      :submitting="submitLoading"
      :initial-data="currentUser"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getUserDetail,
  listUsers,
  resetUserPassword,
  updateUser,
  updateUserStatus,
} from '../../api/user-admin'
import UserFormDialog from './components/UserFormDialog.vue'

const genderOptions = [
  { label: '全部', value: '' },
  { label: '男', value: 'male' },
  { label: '女', value: 'female' },
  { label: '未知', value: 'unknown' },
]

const statusOptions = [
  { label: '全部', value: '' },
  { label: '启用', value: 'enabled' },
  { label: '停用', value: 'disabled' },
]

const queryForm = reactive({
  keyword: '',
  gender: '',
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
const tableData = ref([])
const selectedRows = ref([])
const dialogVisible = ref(false)
const currentUser = ref(createEmptyUser())

function createEmptyUser() {
  return {
    id: undefined,
    nickname: '',
    avatar: '',
    gender: '',
    email: '',
    signature: '',
    status: 'enabled',
    remark: '',
    likeSongCount: 0,
    favoritePlaylistCount: 0,
    playHistoryCount: 0,
    lastLoginTime: '',
    createTime: '',
    updateTime: '',
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

function normalizeUser(row = {}) {
  return {
    ...createEmptyUser(),
    ...row,
    avatar: normalizeFileUrl(row.avatar || ''),
  }
}

function resolveAvatarText(name) {
  return String(name || '').trim().slice(0, 1) || '用'
}

function getGenderLabel(gender) {
  return genderOptions.find((item) => item.value === gender)?.label || '未知'
}

function getStatusType(status) {
  return status === 'enabled' ? 'success' : 'info'
}

function getStatusLabel(status) {
  return status === 'enabled' ? '启用' : '停用'
}

async function fetchData() {
  loading.value = true
  try {
    const { list, total } = await listUsers({
      ...queryForm,
      pageNo: pagination.pageNo,
      pageSize: pagination.pageSize,
    })
    tableData.value = Array.isArray(list) ? list.map(normalizeUser) : []
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
    gender: '',
    status: '',
  })
  handleSearch()
}

async function handleEdit(row) {
  detailLoading.value = true
  try {
    const data = await getUserDetail(row.id)
    currentUser.value = normalizeUser(data || {})
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '获取用户详情失败')
  } finally {
    detailLoading.value = false
  }
}

async function handleSubmit(payload) {
  submitLoading.value = true
  try {
    await updateUser(payload)
    ElMessage.success('用户信息已更新')
    dialogVisible.value = false
    await fetchData()
  } finally {
    submitLoading.value = false
  }
}

async function handleStatusChange(status) {
  const ids = selectedRows.value.map((item) => item.id)
  if (!ids.length) {
    ElMessage.warning('请先选择用户')
    return
  }
  await updateUserStatus(ids, status)
  ElMessage.success('状态已更新')
  await fetchData()
}

async function handleRowStatusToggle(row) {
  const nextStatus = row.status === 'enabled' ? 'disabled' : 'enabled'
  await updateUserStatus([row.id], nextStatus)
  ElMessage.success('状态已更新')
  await fetchData()
}

async function handleResetPassword(row) {
  try {
    const { value } = await ElMessageBox.prompt(`请输入用户“${row.nickname}”的新密码`, '重置密码', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPattern: /^.{6,}$/,
      inputErrorMessage: '密码长度至少 6 位',
    })
    await resetUserPassword(row.id, value)
    ElMessage.success('密码已重置')
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
    ElMessage.error(error.message || '重置密码失败')
  }
}

fetchData()
</script>

<style scoped>
.user-manage-page {
  display: grid;
  gap: 24px;
}

.user-manage-page :deep(.el-table) {
  width: 100%;
}

.user-manage-page :deep(.el-select) {
  width: 100%;
}

.user-manage-page :deep(.el-form-item) {
  margin-bottom: 16px;
}

.user-page-actions {
  margin-bottom: 0;
}

.user-info-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-avatar-thumb,
.user-avatar-badge {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  flex-shrink: 0;
}

.user-avatar-thumb {
  overflow: hidden;
  background: #edf8f1;
}

.user-avatar-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #7be0b1, #31c27c);
  color: #fff;
  font-weight: 600;
}

.user-main-meta p {
  margin: 4px 0 0;
  color: #5f7f6d;
  font-size: 12px;
}

.user-stats-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: #5f7f6d;
  font-size: 12px;
}
</style>


