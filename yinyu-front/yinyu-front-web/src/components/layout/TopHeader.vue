<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import UserPasswordDialog from '../user/UserPasswordDialog.vue'
import UserProfileDialog from '../user/UserProfileDialog.vue'
import { getCaptcha, loginUser, logoutUser, registerUser } from '../../api/user'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const menus = [
  { label: '首页', path: '/' },
  { label: '我的音乐', path: '/my-music' },
  { label: '歌单', path: '/playlist' },
  { label: '排行榜', path: '/ranking' },
  { label: '歌手', path: '/singers' },
]

const keyword = ref(route.query.q || '')
const activeTab = ref('login')
const captchaImage = ref('')
const profileDialogVisible = ref(false)
const passwordDialogVisible = ref(false)

const loginForm = reactive({
  account: '',
  password: '',
  captchaCode: '',
})

const registerForm = reactive({
  nickname: '',
  email: '',
  password: '',
  confirmPassword: '',
  captchaCode: '',
})

const activeMenu = computed(() => {
  const prefixMatched = menus.find((item) => item.path !== '/' && route.path.startsWith(item.path))
  if (prefixMatched) {
    return prefixMatched.path
  }
  const matched = menus.find((item) => route.path === item.path)
  return matched?.path || '/'
})

const dialogVisible = computed({
  get: () => userStore.authDialogVisible,
  set: (value) => {
    if (value) {
      userStore.openAuthDialog()
      return
    }
    userStore.closeAuthDialog()
  },
})

const userAvatarText = computed(() => {
  const source = String(userStore.currentUser?.nickname || '').trim()
  return source.slice(0, 1) || '乐'
})

function goSearch() {
  const value = keyword.value.trim()
  if (!value) {
    ElMessage.warning('先输入想找的歌曲、歌单或歌手')
    return
  }
  router.push({ path: '/search', query: { q: value } })
}

function resetForms() {
  loginForm.account = ''
  loginForm.password = ''
  loginForm.captchaCode = ''
  registerForm.nickname = ''
  registerForm.email = ''
  registerForm.password = ''
  registerForm.confirmPassword = ''
  registerForm.captchaCode = ''
}

async function refreshCaptcha() {
  const data = await getCaptcha()
  captchaImage.value = data?.imageBase64 || ''
}

async function submitLogin() {
  try {
    const data = await loginUser({ ...loginForm })
    userStore.setCurrentUser(data)
    userStore.closeAuthDialog()
    ElMessage.success(`欢迎回来，${data?.nickname || '乐迷'}`)
    resetForms()
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
    await refreshCaptcha()
  }
}

async function submitRegister() {
  try {
    const data = await registerUser({ ...registerForm })
    userStore.setCurrentUser(data)
    userStore.closeAuthDialog()
    ElMessage.success(`注册成功，欢迎你，${data?.nickname || '乐迷'}`)
    resetForms()
  } catch (error) {
    ElMessage.error(error.message || '注册失败')
    await refreshCaptcha()
  }
}

async function handleLogout() {
  await logoutUser()
  userStore.clearCurrentUser()
  ElMessage.success('已退出登录')
  if (route.path === '/my-music') {
    router.push('/')
  }
}

function handleProfileUpdated(user) {
  userStore.setCurrentUser(user)
}

function handlePasswordUpdated() {
  userStore.clearCurrentUser()
  userStore.openAuthDialog()
}

watch(
  () => userStore.authDialogVisible,
  async (visible) => {
    if (visible) {
      await refreshCaptcha()
    }
  },
)

watch(activeTab, async () => {
  if (userStore.authDialogVisible) {
    await refreshCaptcha()
  }
})

watch(
  () => route.query.q,
  (value) => {
    keyword.value = String(value || '')
  },
)

onMounted(() => {
  userStore.fetchCurrentUser()
})
</script>

<template>
  <header class="topbar">
    <div class="container topbar-inner">
      <div class="brand is-clickable" @click="$router.push('/')">
        <div class="brand-logo">音</div>
        <div class="brand-text">
          <h1>音域</h1>
          <p>YINYU MUSIC</p>
        </div>
      </div>

      <el-menu
        mode="horizontal"
        router
        :default-active="activeMenu"
        class="top-menu"
        :ellipsis="false"
      >
        <el-menu-item v-for="item in menus" :key="item.path" :index="item.path">
          {{ item.label }}
        </el-menu-item>
      </el-menu>

      <div class="nav-right">
        <el-input
          v-model="keyword"
          class="search-input"
          placeholder="搜索歌曲、歌手、歌单"
          @keyup.enter="goSearch"
        >
          <template #append>
            <el-button @click="goSearch">搜索</el-button>
          </template>
        </el-input>

        <el-dropdown v-if="userStore.isLoggedIn" trigger="click">
          <div class="user-entry">
            <el-image v-if="userStore.currentUser?.avatar" :src="userStore.currentUser.avatar" fit="cover" class="user-avatar" />
            <div v-else class="user-avatar user-avatar-fallback">{{ userAvatarText }}</div>
            <span class="user-name">{{ userStore.currentUser?.nickname }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="$router.push('/my-music')">我的音乐</el-dropdown-item>
              <el-dropdown-item @click="profileDialogVisible = true">编辑个人信息</el-dropdown-item>
              <el-dropdown-item @click="passwordDialogVisible = true">修改密码</el-dropdown-item>
              <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <el-button v-else class="login-btn" @click="userStore.openAuthDialog()">登录 / 注册</el-button>
      </div>
    </div>
  </header>

  <el-dialog v-model="dialogVisible" title="用户中心" width="460px" class="theme-dialog">
    <el-segmented
      v-model="activeTab"
      class="auth-tabs"
      :options="[
        { label: '登录', value: 'login' },
        { label: '注册', value: 'register' },
      ]"
    />

    <el-form v-if="activeTab === 'login'" label-position="top" class="theme-form">
      <el-form-item label="账号">
        <el-input v-model="loginForm.account" placeholder="昵称 / 邮箱" @keyup.enter="submitLogin" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" @keyup.enter="submitLogin" />
      </el-form-item>
      <el-form-item label="验证码">
        <div class="captcha-row">
          <el-input v-model="loginForm.captchaCode" placeholder="请输入验证码" @keyup.enter="submitLogin" />
          <img v-if="captchaImage" :src="captchaImage" alt="验证码" class="captcha-image" @click="refreshCaptcha" />
        </div>
      </el-form-item>
    </el-form>

    <el-form v-else label-position="top" class="theme-form">
      <el-form-item label="昵称">
        <el-input v-model="registerForm.nickname" placeholder="请输入昵称" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="registerForm.password" type="password" show-password placeholder="请输入密码" />
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input v-model="registerForm.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
      </el-form-item>
      <el-form-item label="验证码">
        <div class="captcha-row">
          <el-input v-model="registerForm.captchaCode" placeholder="请输入验证码" @keyup.enter="submitRegister" />
          <img v-if="captchaImage" :src="captchaImage" alt="验证码" class="captcha-image" @click="refreshCaptcha" />
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button v-if="activeTab === 'login'" type="primary" @click="submitLogin">登录</el-button>
      <el-button v-else type="primary" @click="submitRegister">注册</el-button>
    </template>
  </el-dialog>

  <UserProfileDialog
    v-model="profileDialogVisible"
    :user="userStore.currentUser"
    @success="handleProfileUpdated"
  />
  <UserPasswordDialog
    v-model="passwordDialogVisible"
    @success="handlePasswordUpdated"
  />
</template>
