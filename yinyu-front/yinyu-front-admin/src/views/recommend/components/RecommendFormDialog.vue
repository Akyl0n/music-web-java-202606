<template>
  <el-dialog
    :model-value="modelValue"
    :title="dialogTitle"
    width="760px"
    class="song-dialog"
    destroy-on-close
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      v-loading="loading"
      :model="formData"
      :rules="formRules"
      label-width="96px"
      class="song-edit-form"
    >
      <div class="dialog-grid">
        <el-form-item label="展示位置" prop="positionCode">
          <el-select v-model="formData.positionCode" placeholder="请选择展示位置">
            <el-option v-for="item in positionOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="关联类型" prop="targetType">
          <el-select v-model="formData.targetType" placeholder="请选择关联类型">
            <el-option v-for="item in targetTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="关联内容" prop="targetId">
          <el-select
            v-model="formData.targetId"
            placeholder="请选择关联内容"
            filterable
            clearable
            :loading="targetOptionsLoading"
          >
            <el-option v-for="item in targetOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio-button label="enabled">启用</el-radio-button>
            <el-radio-button label="disabled">停用</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </div>

      <el-form-item v-if="showCoverUpload" label="封面图" prop="cover">
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

      <el-alert
        v-else
        type="info"
        :closable="false"
        show-icon
        title="今日推荐不单独上传封面，将直接使用所选歌曲、歌单或歌手的封面。"
      />
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
import { uploadHomeRecommendCover } from '../../../api/recommend'

const POSITION_HOME_BANNER = 'home_banner'
const POSITION_HOME_DAILY_SONG = 'home_daily_song'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  mode: { type: String, default: 'create' },
  loading: { type: Boolean, default: false },
  submitting: { type: Boolean, default: false },
  initialData: { type: Object, default: () => ({}) },
  targetOptions: { type: Array, default: () => [] },
  targetOptionsLoading: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue', 'submit', 'target-type-change'])

const positionOptions = [
  { label: '首页轮播', value: POSITION_HOME_BANNER },
  { label: '今日推荐', value: POSITION_HOME_DAILY_SONG },
]

const targetTypeOptions = [
  { label: '歌曲', value: 'song' },
  { label: '歌单', value: 'playlist' },
  { label: '歌手', value: 'singer' },
]

const formRef = ref()
const coverUploadLoading = ref(false)
const syncingForm = ref(false)
const formData = reactive(createDefaultForm())

const dialogTitle = computed(() => (props.mode === 'create' ? '新增首页推荐' : '编辑首页推荐'))
const showCoverUpload = computed(() => formData.positionCode === POSITION_HOME_BANNER)

const formRules = {
  positionCode: [{ required: true, message: '请选择展示位置', trigger: 'change' }],
  targetType: [{ required: true, message: '请选择关联类型', trigger: 'change' }],
  targetId: [{ required: true, message: '请选择关联内容', trigger: 'change' }],
  cover: [
    {
      validator: (_rule, value, callback) => {
        if (showCoverUpload.value && !value) {
          callback(new Error('请上传封面'))
          return
        }
        callback()
      },
      trigger: 'change',
    },
  ],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

function syncFormData() {
  syncingForm.value = true
  Object.assign(formData, createDefaultForm(), normalizeFormData(props.initialData))
  if (!showCoverUpload.value) {
    formData.cover = ''
  }
  emit('target-type-change', formData.targetType)
  Promise.resolve().then(() => {
    syncingForm.value = false
  })
}

watch(
  () => props.modelValue,
  (visible) => {
    if (!visible) {
      return
    }
    syncFormData()
  },
  { immediate: true },
)

watch(
  () => props.initialData,
  () => {
    if (!props.modelValue) {
      return
    }
    syncFormData()
  },
  { deep: true },
)

watch(
  () => formData.targetType,
  (value, oldValue) => {
    if (!value) {
      return
    }
    if (!syncingForm.value && oldValue && value !== oldValue) {
      formData.targetId = undefined
    }
    emit('target-type-change', value)
  },
)

watch(
  () => formData.positionCode,
  (value) => {
    if (value !== POSITION_HOME_BANNER) {
      formData.cover = ''
      formRef.value?.clearValidate?.('cover')
    }
  },
)

function createDefaultForm() {
  return {
    id: undefined,
    positionCode: POSITION_HOME_BANNER,
    targetType: 'song',
    targetId: undefined,
    cover: '',
    status: 'enabled',
  }
}

function normalizeFormData(data = {}) {
  return {
    ...data,
    targetId: data.targetId == null ? undefined : Number(data.targetId),
    cover: normalizeFileUrl(data.cover || ''),
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
    const result = await uploadHomeRecommendCover(option.file)
    formData.cover = normalizeFileUrl(result.url)
    ElMessage.success('封面上传成功')
    option.onSuccess?.(result)
    formRef.value?.validateField('cover')
  } catch (error) {
    option.onError?.(error)
    ElMessage.error(error?.message || '封面上传失败')
  } finally {
    coverUploadLoading.value = false
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  emit('submit', {
    id: formData.id,
    positionCode: formData.positionCode,
    targetType: formData.targetType,
    targetId: formData.targetId,
    cover: showCoverUpload.value ? formData.cover || null : null,
    status: formData.status,
  })
}
</script>
