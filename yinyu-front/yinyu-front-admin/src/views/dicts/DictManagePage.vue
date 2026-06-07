<template>
  <div class="dict-manage-page">
    <el-card shadow="never" class="module-card dict-panel">
      <template #header>
        <div class="section-head">
          <div>
            <h3>字典类型</h3>
            <p>先定义字典类型，再维护该类型下的字典值。</p>
          </div>
          <el-button type="primary" plain @click="handleCreateType">新增类型</el-button>
        </div>
      </template>

      <el-form :model="typeQueryForm" class="song-filter-form dict-filter-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="16" :md="12">
            <el-form-item label="关键词" label-width="60px">
              <el-input
                v-model="typeQueryForm.keyword"
                placeholder="编码 / 名称 / 备注"
                clearable
                @keyup.enter="handleTypeSearch"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8" :md="12">
            <el-form-item class="song-filter-actions dict-filter-actions">
              <el-button type="primary" @click="handleTypeSearch">查询</el-button>
              <el-button @click="handleTypeReset">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div v-loading="typeLoading" class="dict-sortable-list">
        <div
          v-for="row in typeList"
          :key="row.id"
          class="dict-sortable-item"
          :class="{ 'is-active': row.id === activeTypeId }"
          draggable="true"
          @click="selectType(row)"
          @dragstart="onTypeDragStart(row)"
          @dragover.prevent
          @drop="onTypeDrop(row)"
        >
          <div class="dict-item-main">
            <button class="drag-handle" type="button" @click.stop>
              <i class="iconfont icon-drag"></i>
            </button>
            <div class="dict-item-content">
              <div class="dict-item-title-row">
                <strong>{{ row.name }}</strong>
                <el-tag :type="getStatusType(row.status)" round>{{ getStatusLabel(row.status) }}</el-tag>
              </div>
              <p>{{ row.code }} / 排序 {{ row.sort }}</p>
              <span>{{ row.remark || '暂无备注' }}</span>
            </div>
          </div>
          <div class="table-actions">
            <el-button link type="primary" @click.stop="handleEditType(row)">编辑</el-button>
            <el-button link type="danger" @click.stop="handleDeleteType(row)">删除</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="module-card dict-panel">
      <template #header>
        <div class="section-head">
          <div>
            <h3>字典值</h3>
            <p v-if="activeType">当前类型：{{ activeType.name }} / {{ activeType.code }}</p>
            <p v-else>请先在左侧选择一个字典类型。</p>
          </div>
          <el-button type="primary" plain @click="handleCreateItem">新增字典值</el-button>
        </div>
      </template>

      <el-form :model="itemQueryForm" class="song-filter-form dict-filter-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="16" :md="12">
            <el-form-item label="关键词" label-width="60px">
              <el-input
                v-model="itemQueryForm.keyword"
                placeholder="编码 / 名称 / 备注"
                clearable
                @keyup.enter="handleItemSearch"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="8" :md="12">
            <el-form-item class="song-filter-actions dict-filter-actions">
              <el-button type="primary" @click="handleItemSearch">查询</el-button>
              <el-button @click="handleItemReset">重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div v-loading="itemLoading" class="dict-sortable-list">
        <div
          v-for="row in itemList"
          :key="row.id"
          class="dict-sortable-item"
          draggable="true"
          @dragstart="onItemDragStart(row)"
          @dragover.prevent
          @drop="onItemDrop(row)"
        >
          <div class="dict-item-main">
            <button class="drag-handle" type="button" @click.stop>
              <i class="iconfont icon-drag"></i>
            </button>
            <div class="dict-item-content">
              <div class="dict-item-title-row">
                <strong>{{ row.name }}</strong>
                <el-tag :type="getStatusType(row.status)" round>{{ getStatusLabel(row.status) }}</el-tag>
              </div>
              <p>{{ row.code }} / 排序 {{ row.sort }}</p>
              <span>{{ row.remark || '暂无备注' }}</span>
            </div>
          </div>
          <div class="table-actions">
            <el-button link type="primary" @click.stop="handleEditItem(row)">编辑</el-button>
            <el-button link type="danger" @click.stop="handleDeleteItem(row)">删除</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <DictTypeDialog
      v-model="typeDialogVisible"
      :mode="typeDialogMode"
      :initial-data="currentType"
      :submitting="typeSubmitting"
      @submit="submitType"
    />

    <DictItemDialog
      v-model="itemDialogVisible"
      :mode="itemDialogMode"
      :initial-data="currentItem"
      :active-type="activeType"
      :submitting="itemSubmitting"
      @submit="submitItem"
    />
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createDictItem,
  createDictType,
  deleteDictItem,
  deleteDictType,
  listDictItems,
  listDictTypes,
  sortDictItems,
  sortDictTypes,
  updateDictItem,
  updateDictType,
} from '../../api/dict'
import DictItemDialog from './components/DictItemDialog.vue'
import DictTypeDialog from './components/DictTypeDialog.vue'

const typeQueryForm = reactive({ keyword: '' })
const itemQueryForm = reactive({ keyword: '' })

const typeLoading = ref(false)
const itemLoading = ref(false)
const typeSubmitting = ref(false)
const itemSubmitting = ref(false)

const typeList = ref([])
const itemList = ref([])
const activeTypeId = ref()

const typeDialogVisible = ref(false)
const itemDialogVisible = ref(false)
const typeDialogMode = ref('create')
const itemDialogMode = ref('create')
const currentType = ref(createEmptyType())
const currentItem = ref(createEmptyItem())

const draggingTypeId = ref()
const draggingItemId = ref()

const activeType = computed(() => typeList.value.find((item) => item.id === activeTypeId.value))

function createEmptyType() {
  return {
    id: undefined,
    code: '',
    name: '',
    status: 'enabled',
    remark: '',
  }
}

function createEmptyItem() {
  return {
    id: undefined,
    typeId: undefined,
    code: '',
    name: '',
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

async function fetchTypeList() {
  typeLoading.value = true
  try {
    const { list } = await listDictTypes(typeQueryForm)
    typeList.value = Array.isArray(list) ? list : []
    if (!activeTypeId.value && typeList.value.length) {
      activeTypeId.value = typeList.value[0].id
    }
    if (activeTypeId.value && !typeList.value.find((item) => item.id === activeTypeId.value)) {
      activeTypeId.value = typeList.value[0]?.id
    }
  } finally {
    typeLoading.value = false
  }
}

async function fetchItemList() {
  if (!activeTypeId.value) {
    itemList.value = []
    return
  }
  itemLoading.value = true
  try {
    const { list } = await listDictItems({
      typeId: activeTypeId.value,
      keyword: itemQueryForm.keyword,
    })
    itemList.value = Array.isArray(list) ? list : []
  } finally {
    itemLoading.value = false
  }
}

async function initPage() {
  await fetchTypeList()
  await fetchItemList()
}

function handleTypeSearch() {
  fetchTypeList().then(fetchItemList)
}

function handleTypeReset() {
  typeQueryForm.keyword = ''
  handleTypeSearch()
}

function handleItemSearch() {
  fetchItemList()
}

function handleItemReset() {
  itemQueryForm.keyword = ''
  fetchItemList()
}

function selectType(row) {
  activeTypeId.value = row.id
  itemQueryForm.keyword = ''
  fetchItemList()
}

function onTypeDragStart(row) {
  draggingTypeId.value = row.id
}

async function onTypeDrop(targetRow) {
  if (!draggingTypeId.value || draggingTypeId.value === targetRow.id) {
    draggingTypeId.value = undefined
    return
  }
  const list = [...typeList.value]
  const fromIndex = list.findIndex((item) => item.id === draggingTypeId.value)
  const toIndex = list.findIndex((item) => item.id === targetRow.id)
  const [moved] = list.splice(fromIndex, 1)
  list.splice(toIndex, 0, moved)
  typeList.value = list
  await sortDictTypes(list.map((item) => item.id))
  draggingTypeId.value = undefined
  await fetchTypeList()
  await fetchItemList()
}

function onItemDragStart(row) {
  draggingItemId.value = row.id
}

async function onItemDrop(targetRow) {
  if (!draggingItemId.value || draggingItemId.value === targetRow.id || !activeTypeId.value) {
    draggingItemId.value = undefined
    return
  }
  const list = [...itemList.value]
  const fromIndex = list.findIndex((item) => item.id === draggingItemId.value)
  const toIndex = list.findIndex((item) => item.id === targetRow.id)
  const [moved] = list.splice(fromIndex, 1)
  list.splice(toIndex, 0, moved)
  itemList.value = list
  await sortDictItems(activeTypeId.value, list.map((item) => item.id))
  draggingItemId.value = undefined
  await fetchItemList()
}

function handleCreateType() {
  typeDialogMode.value = 'create'
  currentType.value = createEmptyType()
  typeDialogVisible.value = true
}

function handleEditType(row) {
  typeDialogMode.value = 'edit'
  currentType.value = { ...row }
  typeDialogVisible.value = true
}

async function handleDeleteType(row) {
  await ElMessageBox.confirm(`确定删除字典类型“${row.name}”吗？删除后会同时删除其下的字典值。`, '删除确认', {
    type: 'warning',
  })
  await deleteDictType(row.id)
  ElMessage.success('字典类型已删除')
  await fetchTypeList()
  await fetchItemList()
}

function handleCreateItem() {
  if (!activeTypeId.value) {
    ElMessage.warning('请先新增或选择一个字典类型')
    return
  }
  itemDialogMode.value = 'create'
  currentItem.value = createEmptyItem()
  itemDialogVisible.value = true
}

function handleEditItem(row) {
  itemDialogMode.value = 'edit'
  currentItem.value = { ...row }
  itemDialogVisible.value = true
}

async function handleDeleteItem(row) {
  await ElMessageBox.confirm(`确定删除字典值“${row.name}”吗？`, '删除确认', { type: 'warning' })
  await deleteDictItem(row.id)
  ElMessage.success('字典值已删除')
  await fetchItemList()
}

async function submitType(payload) {
  typeSubmitting.value = true
  try {
    if (typeDialogMode.value === 'create') {
      await createDictType(payload)
      ElMessage.success('字典类型新增成功')
    } else {
      await updateDictType(payload)
      ElMessage.success('字典类型编辑成功')
    }
    typeDialogVisible.value = false
    await fetchTypeList()
    await fetchItemList()
  } finally {
    typeSubmitting.value = false
  }
}

async function submitItem(payload) {
  itemSubmitting.value = true
  try {
    if (itemDialogMode.value === 'create') {
      await createDictItem(payload)
      ElMessage.success('字典值新增成功')
    } else {
      await updateDictItem(payload)
      ElMessage.success('字典值编辑成功')
    }
    itemDialogVisible.value = false
    await fetchItemList()
  } finally {
    itemSubmitting.value = false
  }
}

initPage()
</script>

<style scoped>
.dict-manage-page {
  display: grid;
  gap: 20px;
}

.dict-manage-page :deep(.el-form-item) {
  margin-bottom: 16px;
}

.dict-filter-actions {
  margin-bottom: 0;
}

.dict-panel {
  border-radius: 24px;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.section-head h3 {
  margin: 0;
  font-size: 20px;
}

.section-head p {
  margin: 6px 0 0;
  color: #5f7f6d;
  font-size: 13px;
}

.dict-sortable-list {
  display: grid;
  gap: 14px;
}

.dict-sortable-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border: 1px solid #d7f2e4;
  border-radius: 18px;
  background: #f7fdf9;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.dict-sortable-item:hover {
  border-color: #8fdcb7;
  box-shadow: 0 10px 24px rgba(49, 194, 124, 0.12);
  transform: translateY(-1px);
}

.dict-sortable-item.is-active {
  border-color: #2faf70;
  box-shadow: 0 0 0 1px rgba(49, 194, 124, 0.18);
}

.dict-item-main {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  min-width: 0;
}

.drag-handle {
  width: 34px;
  height: 34px;
  border: none;
  border-radius: 10px;
  background: #e0f7eb;
  color: #238a56;
  cursor: grab;
  flex-shrink: 0;
}

.dict-item-content {
  min-width: 0;
}

.dict-item-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.dict-item-title-row strong {
  font-size: 15px;
}

.dict-item-content p,
.dict-item-content span {
  display: block;
  color: #5f7f6d;
  font-size: 13px;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .section-head,
  .dict-sortable-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .table-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>


