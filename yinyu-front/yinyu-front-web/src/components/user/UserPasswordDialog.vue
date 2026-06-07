<template>
  <el-dialog v-model="dialogVisible" title="修改密码" width="460px" class="theme-dialog">
    <el-form label-position="top" class="theme-form">
      <el-form-item label="原密码">
        <el-input v-model="form.oldPassword" type="password" show-password placeholder="请输入原密码" />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input v-model="form.newPassword" type="password" show-password placeholder="请输入新密码" />
      </el-form-item>
      <el-form-item label="确认新密码">
        <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入新密码" @keyup.enter="submit" />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submit">确认修改</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { logoutUser, updateUserPassword } from '../../api/user'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'success'])

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value),
})

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      form.oldPassword = ''
      form.newPassword = ''
      form.confirmPassword = ''
    }
  },
)

async function submit() {
  if (!form.oldPassword.trim() || !form.newPassword.trim() || !form.confirmPassword.trim()) {
    ElMessage.warning('请完整填写密码信息')
    return
  }
  try {
    await updateUserPassword({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
      confirmPassword: form.confirmPassword,
    })
    await logoutUser()
    ElMessage.success('密码修改成功，请重新登录')
    emit('success')
    dialogVisible.value = false
  } catch (error) {
    ElMessage.error(error.message || '密码修改失败')
  }
}
</script>
