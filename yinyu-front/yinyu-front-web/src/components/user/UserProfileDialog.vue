<template>
  <el-dialog v-model="dialogVisible" title="编辑个人信息" width="520px" class="theme-dialog">
    <el-form label-position="top" class="theme-form profile-form">
      <el-form-item label="头像">
        <div class="profile-avatar-row">
          <el-image v-if="previewAvatar" :src="previewAvatar" fit="cover" class="profile-avatar-preview" />
          <div v-else class="profile-avatar-preview profile-avatar-fallback">{{ avatarText }}</div>
          <div class="profile-avatar-actions">
            <el-upload
              :show-file-list="false"
              :auto-upload="false"
              accept="image/*"
              :on-change="handleAvatarChange"
            >
              <el-button plain>上传头像</el-button>
            </el-upload>
            <span class="profile-upload-tip">支持常见图片格式，上传后自动保存地址</span>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="昵称">
        <el-input v-model="form.nickname" maxlength="32" placeholder="请输入昵称" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" maxlength="64" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="性别">
        <el-select v-model="form.gender" placeholder="请选择性别" clearable>
          <el-option label="男" value="male" />
          <el-option label="女" value="female" />
          <el-option label="保密" value="secret" />
        </el-select>
      </el-form-item>
      <el-form-item label="签名">
        <el-input v-model="form.signature" type="textarea" :rows="3" maxlength="255" placeholder="写一句签名吧" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { updateUserProfile, uploadUserAvatar } from '../../api/user'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  user: {
    type: Object,
    default: () => null,
  },
})

const emit = defineEmits(['update:modelValue', 'success'])

const form = reactive({
  nickname: '',
  email: '',
  gender: '',
  avatar: '',
  signature: '',
})

const saving = ref(false)

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

const previewAvatar = computed(() => normalizeFileUrl(form.avatar))
const avatarText = computed(() => String(form.nickname || props.user?.nickname || '').trim().slice(0, 1) || '乐')

watch(
  () => props.modelValue,
  (visible) => {
    if (!visible) {
      return
    }
    form.nickname = props.user?.nickname || ''
    form.email = props.user?.email || ''
    form.gender = props.user?.gender || ''
    form.avatar = props.user?.avatar || ''
    form.signature = props.user?.signature || ''
  },
  { immediate: true },
)

function normalizeFileUrl(url) {
  if (!url) {
    return ''
  }
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('/api/')) {
    return url
  }
  return url.startsWith('/') ? `/api${url}` : `/api/${url}`
}

async function handleAvatarChange(uploadFile) {
  const rawFile = uploadFile?.raw
  if (!rawFile) {
    return
  }
  try {
    const data = await uploadUserAvatar(rawFile)
    form.avatar = data?.url || ''
    ElMessage.success('头像上传成功')
  } catch (error) {
    ElMessage.error(error.message || '头像上传失败')
  }
}

async function submit() {
  const nickname = form.nickname.trim()
  if (!nickname) {
    ElMessage.warning('请输入昵称')
    return
  }
  try {
    saving.value = true
    const data = await updateUserProfile({
      nickname,
      email: form.email.trim(),
      gender: form.gender,
      avatar: form.avatar,
      signature: form.signature.trim(),
    })
    ElMessage.success('个人信息已更新')
    emit('success', data)
    dialogVisible.value = false
  } catch (error) {
    ElMessage.error(error.message || '个人信息更新失败')
  } finally {
    saving.value = false
  }
}
</script>
