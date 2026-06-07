<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { sideGroups, topMenus } from '../../mock/admin.data'
import { useAdminAuthStore } from '../../stores/admin-auth'

const route = useRoute()
const router = useRouter()
const adminAuthStore = useAdminAuthStore()

const activePath = computed(() => route.path)

const topMenuMatcher = [
  { topPath: '/admin/dashboard', matchPaths: ['/admin/dashboard'] },
  { topPath: '/admin/songs', matchPaths: ['/admin/songs', '/admin/playlists', '/admin/singers'] },
  { topPath: '/admin/recommend', matchPaths: ['/admin/recommend'] },
  { topPath: '/admin/dicts', matchPaths: ['/admin/dicts', '/admin/users'] },
]

const activeTopPath = computed(() => {
  const matched = topMenuMatcher.find((item) => item.matchPaths.includes(route.path))
  return matched?.topPath || '/admin/dashboard'
})

const adminDisplayName = computed(() => adminAuthStore.displayName || '管理员')
const adminInitial = computed(() => adminDisplayName.value.slice(0, 1) || '管')

function go(path) {
  router.push(path)
}

async function handleLogout() {
  await ElMessageBox.confirm('确认退出当前后台登录吗？', '退出提示', { type: 'warning' })
  await adminAuthStore.logout()
  router.replace('/login')
}
</script>

<template>
  <div class="admin-layout">
    <header class="admin-topbar">
      <div class="admin-brand is-clickable" @click="go('/admin/dashboard')">
        <div class="admin-brand-logo">音</div>
        <div class="admin-brand-text">
          <h1>音域后台</h1>
          <p>YINYU ADMIN CONSOLE</p>
        </div>
      </div>

      <nav class="admin-topnav">
        <a
          v-for="item in topMenus"
          :key="item.path"
          class="is-clickable"
          :class="{ active: activeTopPath === item.path }"
          @click="go(item.path)"
        >
          {{ item.label }}
        </a>
      </nav>

      <div class="admin-topbar-right">
        <div class="admin-user-meta">
          <span class="admin-user-name">{{ adminDisplayName }}</span>
          <button class="admin-text-btn" type="button" @click="handleLogout">退出登录</button>
        </div>
        <div class="admin-user-pill">{{ adminInitial }}</div>
      </div>
    </header>

    <div class="admin-body">
      <aside class="admin-sidebar">
        <div v-for="group in sideGroups" :key="group.title" class="sidebar-group">
          <p class="sidebar-title">{{ group.title }}</p>
          <el-menu :default-active="activePath" class="sidebar-menu" @select="go">
            <el-menu-item v-for="item in group.items" :key="item.path" :index="item.path">
              {{ item.label }}
            </el-menu-item>
          </el-menu>
        </div>
      </aside>

      <section class="admin-content">
        <router-view />
      </section>
    </div>
  </div>
</template>
