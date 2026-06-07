<template>
  <el-dialog
    :model-value="modelValue"
    :title="dialogTitle"
    width="1120px"
    class="song-dialog"
    destroy-on-close
    @close="handleClose"
  >
    <div class="playlist-dialog-layout" v-loading="loading">
      <div class="playlist-dialog-main">
        <div class="playlist-pane-head">
          <h3>歌单信息</h3>
          <p>设置歌单封面、分类、标签和基础展示信息。</p>
        </div>
        <el-form
          ref="formRef"
          :model="formData"
          :rules="formRules"
          label-width="92px"
          class="song-edit-form"
        >
          <div class="dialog-grid">
            <el-form-item label="歌单名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入歌单名称" />
            </el-form-item>
            <el-form-item label="副标题">
              <el-input v-model="formData.subtitle" placeholder="请输入副标题" />
            </el-form-item>
            <el-form-item label="分类" prop="category">
              <el-select v-model="formData.category" placeholder="请选择分类" filterable clearable>
                <el-option
                  v-for="item in categoryOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="String(item.id)"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="风格标签">
              <el-select
                v-model="formData.tags"
                multiple
                filterable
                clearable
                collapse-tags
                collapse-tags-tooltip
                placeholder="请选择风格"
              >
                <el-option v-for="item in styleOptions" :key="item.id" :label="item.name" :value="item.name" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="formData.status">
                <el-radio-button label="enabled">启用</el-radio-button>
                <el-radio-button label="disabled">停用</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="推荐">
              <el-switch v-model="formData.recommendFlag" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </div>

          <el-form-item label="封面">
            <div class="upload-panel">
              <div class="upload-row">
                <div class="upload-actions">
                  <el-upload
                    :show-file-list="false"
                    :before-upload="beforeCoverUpload"
                    :http-request="handleCoverUpload"
                    accept="image/*"
                  >
                    <el-button type="primary" plain :loading="coverUploadLoading">上传封面</el-button>
                  </el-upload>
                  <el-button v-if="formData.cover" link type="danger" @click="clearCover">清空封面</el-button>
                </div>
                <div class="cover-preview-card">
                  <el-image
                    v-if="formData.cover"
                    :src="formData.cover"
                    fit="cover"
                    class="cover-preview-image"
                    :preview-src-list="[formData.cover]"
                    preview-teleported
                  />
                  <div v-else class="preview-empty">暂无封面</div>
                </div>
              </div>
              <div class="upload-tip-row">
                <span class="upload-tip">支持 jpg、png、gif、webp，大小不超过 5MB</span>
              </div>
            </div>
          </el-form-item>

          <el-form-item label="简介">
            <el-input v-model="formData.intro" type="textarea" :rows="4" placeholder="请输入歌单简介" />
          </el-form-item>

          <el-form-item label="备注">
            <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
          </el-form-item>
        </el-form>
      </div>

      <div class="playlist-dialog-side">
        <div class="playlist-side-card">
          <div class="playlist-side-head">
            <h3>歌曲信息</h3>
            <p>先添加歌曲，再拖动排序调整歌单顺序。</p>
          </div>

          <el-select
            v-model="songPickerValue"
            placeholder="搜索并添加歌曲"
            filterable
            clearable
            class="playlist-song-picker"
            @change="handleSongPick"
          >
            <el-option
              v-for="item in availableSongOptions"
              :key="item.id"
              :label="`${item.name} / ${item.singerName || '未知歌手'}`"
              :value="item.id"
            />
          </el-select>

          <div class="playlist-selected-head">
            <span>已选歌曲 {{ selectedSongs.length }} 首</span>
            <el-button v-if="selectedSongs.length" link type="danger" @click="clearSongs">清空</el-button>
          </div>

          <div v-if="selectedSongs.length" class="playlist-song-list">
            <div
              v-for="(item, index) in selectedSongs"
              :key="item.id"
              class="playlist-song-item"
              draggable="true"
              @dragstart="handleDragStart(index)"
              @dragover.prevent
              @drop="handleDrop(index)"
            >
              <div class="playlist-song-main">
                <i class="iconfont icon-drag playlist-song-drag"></i>
                <div class="playlist-song-order">{{ index + 1 }}</div>
                <div class="playlist-song-content">
                  <strong>{{ item.name }}</strong>
                  <p>{{ item.singerName || '未知歌手' }}</p>
                </div>
              </div>
              <el-button link type="danger" class="playlist-song-remove" @click="removeSong(item.id)">移除</el-button>
            </div>
          </div>

          <el-empty v-else description="请先选择歌曲" :image-size="80" />
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadPlaylistCover } from '../../../api/playlist'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  mode: { type: String, default: 'create' },
  loading: { type: Boolean, default: false },
  submitting: { type: Boolean, default: false },
  initialData: { type: Object, default: () => ({}) },
  categoryOptions: { type: Array, default: () => [] },
  styleOptions: { type: Array, default: () => [] },
  songOptions: { type: Array, default: () => [] },
})

const emit = defineEmits(['update:modelValue', 'submit'])

const formRef = ref()
const coverUploadLoading = ref(false)
const dragIndex = ref(-1)
const songPickerValue = ref(undefined)
const formData = reactive(createDefaultForm())

const dialogTitle = computed(() => (props.mode === 'create' ? '新增歌单' : '编辑歌单'))

const formRules = {
  name: [{ required: true, message: '请输入歌单名称', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const selectedSongs = computed(() =>
  formData.songIds
    .map((id) => props.songOptions.find((item) => item.id === id))
    .filter(Boolean),
)

const availableSongOptions = computed(() =>
  props.songOptions.filter((item) => !formData.songIds.includes(item.id)),
)

watch(
  () => [props.modelValue, props.initialData],
  ([visible]) => {
    if (!visible) {
      return
    }
    syncFormData(props.initialData)
  },
  { deep: true, immediate: true },
)

function createDefaultForm() {
  return {
    id: undefined,
    name: '',
    subtitle: '',
    cover: '',
    category: '',
    tags: [],
    intro: '',
    status: 'enabled',
    recommendFlag: 0,
    songIds: [],
    remark: '',
  }
}

function syncFormData(data = {}) {
  dragIndex.value = -1
  songPickerValue.value = undefined
  Object.assign(formData, createDefaultForm(), {
    ...data,
    cover: normalizeFileUrl(data.cover || ''),
    category: data.category ? String(data.category) : '',
    tags: mapTags(data.tags),
    songIds: Array.isArray(data.songIds) ? [...data.songIds] : [],
    recommendFlag: data.recommendFlag === 1 ? 1 : 0,
  })
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

function normalizeFileUrl(url) {
  if (!url) {
    return ''
  }
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/api/')) {
    return url
  }
  return url.startsWith('/') ? `/api${url}` : `/api/${url}`
}

function handleClose() {
  emit('update:modelValue', false)
}

function clearCover() {
  formData.cover = ''
}

function beforeCoverUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
  }
  return isImage && isLt5M
}

async function handleCoverUpload(option) {
  coverUploadLoading.value = true
  try {
    const result = await uploadPlaylistCover(option.file)
    formData.cover = normalizeFileUrl(result.url)
    ElMessage.success('封面上传成功')
    option.onSuccess?.(result)
  } catch (error) {
    option.onError?.(error)
    ElMessage.error(error?.message || '封面上传失败')
  } finally {
    coverUploadLoading.value = false
  }
}

function handleSongPick(songId) {
  if (!songId) {
    return
  }
  if (!formData.songIds.includes(songId)) {
    formData.songIds = [...formData.songIds, songId]
  }
  songPickerValue.value = undefined
}

function removeSong(songId) {
  formData.songIds = formData.songIds.filter((item) => item !== songId)
}

function clearSongs() {
  formData.songIds = []
}

function handleDragStart(index) {
  dragIndex.value = index
}

function handleDrop(index) {
  if (dragIndex.value < 0 || dragIndex.value === index) {
    dragIndex.value = -1
    return
  }
  const next = [...formData.songIds]
  const [moved] = next.splice(dragIndex.value, 1)
  next.splice(index, 0, moved)
  formData.songIds = next
  dragIndex.value = -1
}

async function handleSubmit() {
  await formRef.value.validate()
  emit('submit', {
    id: formData.id,
    name: formData.name,
    subtitle: formData.subtitle || null,
    cover: formData.cover || null,
    category: formData.category ? String(formData.category) : null,
    tags: formData.tags.length ? formData.tags.join(',') : null,
    intro: formData.intro || null,
    status: formData.status,
    recommendFlag: formData.recommendFlag === 1 ? 1 : 0,
    songIds: formData.songIds,
    remark: formData.remark || null,
  })
}
</script>

<style scoped>
.playlist-dialog-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(320px, 0.8fr);
  gap: 20px;
}

.playlist-dialog-main,
.playlist-dialog-side {
  min-width: 0;
}

.playlist-dialog-main {
  padding: 20px 22px;
  border: 1px solid #d7f2e4;
  border-radius: 24px;
  background:
    radial-gradient(circle at top left, rgba(49, 194, 124, 0.16), transparent 32%),
    linear-gradient(180deg, #fbfefc, #f3fcf7);
}

.playlist-pane-head {
  margin-bottom: 18px;
}

.playlist-pane-head h3 {
  margin: 0;
  font-size: 20px;
}

.playlist-pane-head p {
  margin: 8px 0 0;
  color: #5f7f6d;
  font-size: 13px;
}

.playlist-side-card {
  height: 100%;
  padding: 18px;
  border: 1px solid #d7f2e4;
  border-radius: 24px;
  background:
    radial-gradient(circle at top right, rgba(49, 194, 124, 0.14), transparent 30%),
    linear-gradient(180deg, #f7fdf9, #f1fbf5);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.6);
}

.playlist-side-head h3 {
  margin: 0;
  font-size: 18px;
}

.playlist-side-head p {
  margin: 8px 0 0;
  color: #5f7f6d;
  font-size: 13px;
}

.playlist-song-picker {
  width: 100%;
  margin-top: 16px;
}

.playlist-song-picker :deep(.el-input__wrapper) {
  border-radius: 14px;
}

.playlist-selected-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 18px 0 12px;
  color: #2d7d51;
  font-size: 13px;
}

.playlist-song-list {
  display: grid;
  gap: 10px;
  max-height: 520px;
  overflow: auto;
}

.playlist-song-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border: 1px solid #d7f2e4;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 8px 18px rgba(49, 194, 124, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.playlist-song-item:hover {
  transform: translateY(-1px);
  border-color: #8cddb6;
  box-shadow: 0 12px 24px rgba(49, 194, 124, 0.12);
}

.playlist-song-main {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.playlist-song-drag {
  color: #31c27c;
  cursor: grab;
  font-size: 16px;
}

.playlist-song-order {
  width: 30px;
  height: 30px;
  border-radius: 11px;
  background: linear-gradient(135deg, #e0f7eb, #c5efd9);
  color: #238a56;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  flex-shrink: 0;
}

.playlist-song-content {
  min-width: 0;
}

.playlist-song-content strong {
  display: block;
}

.playlist-song-content p {
  margin: 4px 0 0;
  color: #5f7f6d;
  font-size: 12px;
}

.playlist-song-remove {
  opacity: 0.8;
}

.upload-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.upload-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.upload-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.upload-tip-row {
  line-height: 1;
}

.upload-tip {
  color: #5f7f6d;
  font-size: 12px;
}

.cover-preview-card {
  width: 180px;
  min-height: 120px;
  border-radius: 18px;
  overflow: hidden;
  border: 1px dashed #9fdec0;
  background: #f2fbf6;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
}

.cover-preview-image {
  width: 100%;
  height: 120px;
}

.preview-empty {
  color: #6e8d7c;
  font-size: 12px;
}

@media (max-width: 960px) {
  .playlist-dialog-layout {
    grid-template-columns: 1fr;
  }
}
</style>


