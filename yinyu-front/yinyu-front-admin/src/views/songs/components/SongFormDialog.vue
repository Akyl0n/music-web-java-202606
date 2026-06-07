<template>
  <el-dialog
    :model-value="modelValue"
    :title="dialogTitle"
    width="860px"
    class="song-dialog"
    destroy-on-close
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      v-loading="loading"
      :model="formData"
      :rules="formRules"
      label-width="92px"
      class="song-edit-form"
    >
      <div class="dialog-grid">
        <el-form-item label="歌曲名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入歌曲名称" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="formData.subtitle" placeholder="请输入副标题" />
        </el-form-item>
        <el-form-item label="歌手" prop="singerId">
          <el-select v-model="formData.singerId" placeholder="请选择歌手" filterable clearable>
            <el-option v-for="item in singerOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="formData.category" placeholder="请选择分类" filterable clearable>
            <el-option v-for="item in categoryOptions" :key="item.id" :label="item.name" :value="String(item.id)" />
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
        <el-form-item label="时长(秒)">
          <el-input-number v-model="formData.durationSeconds" :min="0" :max="36000" controls-position="right" />
        </el-form-item>
        <el-form-item label="语言">
          <el-input v-model="formData.language" placeholder="请输入语言" />
        </el-form-item>
        <el-form-item label="发行日期">
          <el-date-picker
            v-model="formData.releaseDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择发行日期"
          />
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

      <el-form-item label="音频文件">
        <div class="upload-panel">
          <div class="upload-row">
            <div class="upload-actions">
              <el-upload
                :show-file-list="false"
                :before-upload="beforeAudioUpload"
                :http-request="handleAudioUpload"
                accept=".mp3,.wav,.flac,.m4a,.aac,.ogg,audio/*"
              >
                <el-button type="primary" plain :loading="audioUploadLoading">上传音频</el-button>
              </el-upload>
              <el-button v-if="formData.audioUrl" link type="danger" @click="clearAudio">清空音频</el-button>
            </div>
            <div class="audio-preview-card">
              <audio v-if="formData.audioUrl" :src="formData.audioUrl" controls class="audio-player"></audio>
              <div v-else class="preview-empty">暂无音频</div>
            </div>
          </div>
          <div class="upload-tip-row">
            <span class="upload-tip">支持 mp3、wav、flac、m4a、aac、ogg，大小不超过 20MB</span>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="简介">
        <el-input v-model="formData.intro" type="textarea" :rows="4" placeholder="请输入歌曲简介" />
      </el-form-item>

      <el-form-item label="备注">
        <el-input v-model="formData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadSongAudio, uploadSongCover } from '../../../api/song'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  mode: { type: String, default: 'create' },
  loading: { type: Boolean, default: false },
  submitting: { type: Boolean, default: false },
  initialData: { type: Object, default: () => ({}) },
  singerOptions: { type: Array, default: () => [] },
  categoryOptions: { type: Array, default: () => [] },
  styleOptions: { type: Array, default: () => [] },
})

const emit = defineEmits(['update:modelValue', 'submit'])

const formRef = ref()
const coverUploadLoading = ref(false)
const audioUploadLoading = ref(false)
const formData = reactive(createDefaultForm())

const dialogTitle = computed(() => (props.mode === 'create' ? '新增歌曲' : '编辑歌曲'))

const formRules = {
  name: [{ required: true, message: '请输入歌曲名称', trigger: 'blur' }],
  singerId: [{ required: true, message: '请选择歌手', trigger: 'change' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

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
    singerId: undefined,
    category: '',
    tags: [],
    cover: '',
    audioUrl: '',
    durationSeconds: undefined,
    language: '',
    intro: '',
    releaseDate: '',
    status: 'enabled',
    recommendFlag: 0,
    remark: '',
  }
}

function syncFormData(data = {}) {
  Object.assign(formData, createDefaultForm(), {
    ...data,
    cover: normalizeFileUrl(data.cover || ''),
    audioUrl: normalizeFileUrl(data.audioUrl || ''),
    tags: mapTags(data.tags),
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

function clearAudio() {
  formData.audioUrl = ''
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

function beforeAudioUpload(file) {
  const allowedExtensions = ['.mp3', '.wav', '.flac', '.m4a', '.aac', '.ogg']
  const fileName = (file.name || '').toLowerCase()
  const matched = allowedExtensions.some((item) => fileName.endsWith(item))
  const isLt20M = file.size / 1024 / 1024 < 20
  if (!matched) {
    ElMessage.error('只能上传音频文件')
  }
  if (!isLt20M) {
    ElMessage.error('音频大小不能超过 20MB')
  }
  return matched && isLt20M
}

async function handleCoverUpload(option) {
  coverUploadLoading.value = true
  try {
    const result = await uploadSongCover(option.file)
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

async function handleAudioUpload(option) {
  audioUploadLoading.value = true
  try {
    const result = await uploadSongAudio(option.file)
    formData.audioUrl = normalizeFileUrl(result.url)
    ElMessage.success('音频上传成功')
    option.onSuccess?.(result)
  } catch (error) {
    option.onError?.(error)
    ElMessage.error(error?.message || '音频上传失败')
  } finally {
    audioUploadLoading.value = false
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  emit('submit', {
    id: formData.id,
    name: formData.name,
    subtitle: formData.subtitle || null,
    singerId: formData.singerId,
    category: formData.category ? String(formData.category) : null,
    tags: formData.tags.length ? formData.tags.join(',') : null,
    cover: formData.cover || null,
    audioUrl: formData.audioUrl || null,
    durationSeconds: formData.durationSeconds ?? null,
    language: formData.language || null,
    intro: formData.intro || null,
    releaseDate: formData.releaseDate || null,
    status: formData.status,
    recommendFlag: formData.recommendFlag === 1 ? 1 : 0,
    remark: formData.remark || null,
  })
}
</script>

<style scoped>
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

.cover-preview-card,
.audio-preview-card {
  width: 180px;
  min-height: 120px;
  border-radius: 16px;
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

.audio-player {
  width: 100%;
}

.preview-empty {
  color: #6e8d7c;
  font-size: 12px;
}
</style>


