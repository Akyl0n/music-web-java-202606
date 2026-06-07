<template>
  <div class="admin-login-page">
    <div class="admin-login-panel">
      <div class="admin-login-brand">
        <div class="admin-login-logo">音</div>
        <div>
          <h1>音屿管理后台</h1>
        </div>
      </div>

      <el-form ref="formRef" :model="formData" :rules="formRules" class="admin-login-form" @keyup.enter="handleSubmit">
        <el-form-item prop="username">
          <el-input v-model="formData.username" placeholder="请输入账号" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="formData.password" type="password" show-password placeholder="请输入密码" size="large" />
        </el-form-item>
        <el-form-item prop="captchaCode">
          <div class="admin-captcha-row">
            <el-input v-model="formData.captchaCode" placeholder="请输入验证码" size="large" />
            <img v-if="captchaImage" :src="captchaImage" alt="验证码" class="admin-captcha-image" @click="fetchCaptcha" />
            <div v-else class="admin-captcha-placeholder">{{ captchaLoading ? '加载中' : '暂无验证码' }}</div>
          </div>
        </el-form-item>
        <el-form-item class="admin-login-action">
          <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">登录后台</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAdminCaptcha } from '../../api/auth'
import { useAdminAuthStore } from '../../stores/admin-auth'

const route = useRoute()
const router = useRouter()
const adminAuthStore = useAdminAuthStore()

const formRef = ref()
const submitting = ref(false)
const captchaLoading = ref(false)
const captchaImage = ref('')
const formData = reactive({
  username: '',
  password: '',
  captchaCode: '',
})

const formRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}

async function fetchCaptcha() {
  captchaLoading.value = true
  try {
    const data = await getAdminCaptcha()
    captchaImage.value = data?.imageBase64 || ''
    formData.captchaCode = ''
  } catch (error) {
    captchaImage.value = ''
    ElMessage.error(error.message || '验证码获取失败')
  } finally {
    captchaLoading.value = false
  }
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await adminAuthStore.login({ ...formData })
    ElMessage.success('登录成功')
    router.replace(route.query.redirect || '/admin/dashboard')
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
    await fetchCaptcha()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchCaptcha()
})
</script>
