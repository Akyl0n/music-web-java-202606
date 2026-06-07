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
      label-width="92px"
      class="song-edit-form"
    >
      <div class="dialog-grid">
        <el-form-item label="用户昵称" prop="nickname">
          <el-input v-model="formData.nickname" placeholder="请输入用户昵称" />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="formData.email" placeholder="请输入邮箱" clearable />
        </el-form-item>

        <el-form-item label="性别">
          <el-select v-model="formData.gender" placeholder="请选择性别" clearable>
            <el-option v-for="item in genderOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>

        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio-button label="enabled">启用</el-radio-button>
            <el-radio-button label="disabled">停用</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </div>

      <el-form-item label="头像">
        <div class="upload-panel">
          <div class="upload-row">
            <div class="upload-actions">
              <el-upload
                :show-file-list="false"
                :before-upload="beforeAvatarUpload"
                :http-request="handleAvatarUpload"
                accept="image/*"
              >
                <el-button type="primary" plain :loading="avatarUploading">上传头像</el-button>
              </el-upload>
              <el-button v-if="formData.avatar" link type="danger" @click="formData.avatar = ''">清空头像</el-button>
            </div>

            <div class="cover-preview-card user-avatar-preview-card">
              <el-image
                v-if="formData.avatar"
                :src="formData.avatar"
                fit="cover"
                class="cover-preview-image"
                :preview-src-list="[formData.avatar]"
                preview-teleported
              />
              <div v-else class="preview-empty">{{ resolveAvatarText(formData.nickname) }}</div>
            </div>
          </div>
        </div>
      </el-form-item>

      <el-form-item label="个性签名">
        <el-input v-model="formData.signature" type="textarea" :rows="3" placeholder="请输入个性签名" />
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
import { uploadUserAvatar } from '../../../api/user-admin'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  submitting: { type: Boolean, default: false },
  initialData: { type: Object, default: () => ({}) },
})

const emit = defineEmits(['update:modelValue', 'submit'])

const genderOptions = [
  { label: '未知', value: 'unknown' },
  { label: '男', value: 'male' },
  { label: '女', value: 'female' },
]

const formRef = ref()
const avatarUploading = ref(false)
const formData = reactive(createDefaultForm())

const dialogTitle = computed(() => '编辑用户')

const formRules = {
  nickname: [{ required: true, message: '请输入用户昵称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

watch(
  () => [props.modelValue, props.initialData],
  ([visible]) => {
    if (!visible) {
      return
    }
    Object.assign(formData, createDefaultForm(), normalizeFormData(props.initialData))
  },
  { deep: true, immediate: true },
)

function createDefaultForm() {
  return {
    id: undefined,
    nickname: '',
    avatar: '',
    gender: '',
    email: '',
    signature: '',
    status: 'enabled',
    remark: '',
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

function normalizeFormData(data = {}) {
  return {
    ...data,
    avatar: normalizeFileUrl(data.avatar || ''),
  }
}

function resolveAvatarText(name) {
  return String(name || '').trim().slice(0, 1) || '用'
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
  avatarUploading.value = true
  try {
    const result = await uploadUserAvatar(option.file)
    formData.avatar = normalizeFileUrl(result.url)
    ElMessage.success('头像上传成功')
    option.onSuccess?.(result)
  } catch (error) {
    option.onError?.(error)
    ElMessage.error(error?.message || '头像上传失败')
  } finally {
    avatarUploading.value = false
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  emit('submit', {
    id: formData.id,
    nickname: formData.nickname,
    avatar: formData.avatar || '',
    gender: formData.gender || '',
    email: formData.email || '',
    signature: formData.signature || '',
    status: formData.status,
    remark: formData.remark || '',
  })
}
</script>

<style scoped>
.user-avatar-preview-card {
  width: 88px;
  height: 88px;
  border-radius: 18px;
}
</style>
