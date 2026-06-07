import { createRouter, createWebHistory } from 'vue-router'
import AdminShell from '../components/layout/AdminShell.vue'
import DashboardPage from '../views/dashboard/DashboardPage.vue'
import SongManagePage from '../views/songs/SongManagePage.vue'
import PlaylistManagePage from '../views/playlists/PlaylistManagePage.vue'
import SingerManagePage from '../views/singers/SingerManageFormPage.vue'
import DictManagePage from '../views/dicts/DictManagePage.vue'
import RecommendManagePage from '../views/recommend/RecommendManagePage.vue'
import UserManagePage from '../views/users/UserManagePage.vue'
import AdminLoginPage from '../views/auth/AdminLoginPage.vue'
import { useAdminAuthStore } from '../stores/admin-auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/admin/dashboard',
    },
    {
      path: '/login',
      name: 'admin-login',
      component: AdminLoginPage,
      meta: { public: true },
    },
    {
      path: '/admin',
      component: AdminShell,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: DashboardPage,
        },
        {
          path: 'songs',
          component: SongManagePage,
        },
        {
          path: 'playlists',
          component: PlaylistManagePage,
        },
        {
          path: 'singers',
          component: SingerManagePage,
        },
        {
          path: 'recommend',
          component: RecommendManagePage,
        },
        {
          path: 'dicts',
          component: DictManagePage,
        },
        {
          path: 'users',
          component: UserManagePage,
        },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const authStore = useAdminAuthStore()
  if (to.meta.public) {
    if (to.path === '/login' && authStore.isLoggedIn) {
      return '/admin/dashboard'
    }
    return true
  }
  if (!to.meta.requiresAuth) {
    return true
  }
  if (authStore.isLoggedIn) {
    return true
  }
  return {
    path: '/login',
    query: { redirect: to.fullPath },
  }
})

export default router
