<template>
  <el-dialog
    :model-value="modelValue"
    :title="dialogTitle"
    width="780px"
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
        <el-form-item label="歌手名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入歌手名称" />
        </el-form-item>
        <el-form-item label="歌手类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择歌手类型">
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="formData.gender" placeholder="请选择性别">
            <el-option v-for="item in genderOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker
            v-model="formData.birthday"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择出生日期"
          />
        </el-form-item>
        <el-form-item label="地区/国家" prop="region">
          <el-select v-model="formData.region" placeholder="请选择国家" filterable clearable>
            <el-option v-for="item in countryOptions" :key="item.id" :label="item.name" :value="item.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="检索字母">
          <el-input
            v-model="formData.letter"
            maxlength="20"
            placeholder="例如 Y"
            @input="normalizeLetterInput"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio-button label="enabled">启用</el-radio-button>
            <el-radio-button label="disabled">停用</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </div>

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

      <el-form-item label="头像">
        <div class="avatar-upload-panel">
          <div class="avatar-upload-row">
            <div class="avatar-upload-actions">
              <el-upload
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="handleAvatarUpload"
                accept="image/*"
              >
                <el-button type="primary" plain :loading="uploadLoading">上传头像</el-button>
              </el-upload>
              <el-button v-if="formData.avatar" link type="danger" @click="clearAvatar">清空头像</el-button>
            </div>
            <div class="avatar-preview-card">
              <el-image
                v-if="formData.avatar"
                :src="formData.avatar"
                fit="cover"
                class="avatar-preview-image"
                :preview-src-list="[formData.avatar]"
                preview-teleported
              />
              <div v-else class="avatar-preview-empty">暂无头像</div>
            </div>
          </div>
          <div class="avatar-upload-tip-row">
            <span class="avatar-upload-tip">支持 jpg、png、gif、webp，大小不超过 5MB</span>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="简介">
        <el-input v-model="formData.intro" type="textarea" :rows="4" placeholder="请输入歌手简介" />
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
import { uploadSingerAvatar } from '../../../api/singer-real'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  mode: {
    type: String,
    default: 'create',
  },
  loading: {
    type: Boolean,
    default: false,
  },
  submitting: {
    type: Boolean,
    default: false,
  },
  initialData: {
    type: Object,
    default: () => ({}),
  },
  countryOptions: {
    type: Array,
    default: () => [],
  },
  styleOptions: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['update:modelValue', 'submit'])

const typeOptions = [
  { label: '独立歌手', value: 'solo' },
  { label: '组合/乐队', value: 'group' },
  { label: '虚拟歌手', value: 'virtual' },
]

const genderOptions = [
  { label: '未知', value: 'unknown' },
  { label: '男', value: 'male' },
  { label: '女', value: 'female' },
  { label: '组合', value: 'group' },
]

const formRules = {
  name: [{ required: true, message: '请输入歌手名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择歌手类型', trigger: 'change' }],
  region: [{ required: true, message: '请选择地区', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const formRef = ref()
const uploadLoading = ref(false)
const formData = reactive(createDefaultForm())

const dialogTitle = computed(() => (props.mode === 'create' ? '新增歌手' : '编辑歌手'))

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

function syncFormData(data = {}) {
  const nextForm = createDefaultForm()
  nextForm.id = data.id
  nextForm.name = data.name || ''
  nextForm.gender = data.gender || 'unknown'
  nextForm.birthday = data.birthday || ''
  nextForm.region = data.region || ''
  nextForm.type = data.type || 'solo'
  nextForm.avatar = normalizeAvatarUrl(data.avatar || '')
  nextForm.intro = data.intro || ''
  nextForm.tags = mapTags(data.tags)
  nextForm.letter = data.letter || ''
  nextForm.status = data.status || 'enabled'
  nextForm.remark = data.remark || ''
  Object.assign(formData, nextForm)
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

function normalizeAvatarUrl(url) {
  if (!url) {
    return ''
  }
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/api/')) {
    return url
  }
  return url.startsWith('/') ? `/api${url}` : `/api/${url}`
}

function normalizeLetterInput(value) {
  formData.letter = (value || '').toUpperCase().replace(/\s+/g, '')
}

function clearAvatar() {
  formData.avatar = ''
}

function handleClose() {
  emit('update:modelValue', false)
}

function beforeAvatarUpload(file) {
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

async function handleAvatarUpload(option) {
  uploadLoading.value = true
  try {
    const result = await uploadSingerAvatar(option.file)
    formData.avatar = normalizeAvatarUrl(result.url)
    ElMessage.success('头像上传成功')
    option.onSuccess?.(result)
  } catch (error) {
    option.onError?.(error)
    ElMessage.error(error?.message || '头像上传失败')
  } finally {
    uploadLoading.value = false
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  emit('submit', {
    id: formData.id,
    name: formData.name,
    gender: formData.gender,
    birthday: formData.birthday || null,
    region: formData.region || null,
    type: formData.type,
    avatar: formData.avatar || null,
    intro: formData.intro || null,
    tags: formData.tags.length ? formData.tags.join(',') : null,
    letter: formData.letter || null,
    status: formData.status,
    remark: formData.remark || null,
  })
}
</script>

<style scoped>
.avatar-upload-panel {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.avatar-upload-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.avatar-upload-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.avatar-upload-tip-row {
  line-height: 1;
}

.avatar-upload-tip {
  color: #5f7f6d;
  font-size: 12px;
}

.avatar-preview-card {
  width: 120px;
  height: 120px;
  border-radius: 16px;
  overflow: hidden;
  border: 1px dashed #9fdec0;
  background: #f2fbf6;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-preview-image {
  width: 100%;
  height: 100%;
}

.avatar-preview-empty {
  color: #6e8d7c;
  font-size: 12px;
}
</style>


