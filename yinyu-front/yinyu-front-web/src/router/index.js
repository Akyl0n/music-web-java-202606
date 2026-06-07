import { createRouter, createWebHistory } from 'vue-router'
import AppShell from '../components/layout/AppShell.vue'
import HomePage from '../views/home/HomePage.vue'
import MyMusicPage from '../views/library/MyMusicPage.vue'
import PlaylistPage from '../views/playlist/PlaylistPage.vue'
import PlaylistDetailPage from '../views/playlist/PlaylistDetailPage.vue'
import RankingPage from '../views/ranking/RankingPage.vue'
import SearchPage from '../views/search/SearchPage.vue'
import SingersPage from '../views/singer/SingersPage.vue'
import SingerDetailPage from '../views/singer/SingerDetailPage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      component: AppShell,
      children: [
        {
          path: '',
          name: 'home',
          component: HomePage,
        },
        {
          path: 'my-music',
          name: 'my-music',
          component: MyMusicPage,
        },
        {
          path: 'playlist',
          name: 'playlist',
          component: PlaylistPage,
        },
        {
          path: 'playlist/:id',
          name: 'playlist-detail',
          component: PlaylistDetailPage,
        },
        {
          path: 'ranking',
          name: 'ranking',
          component: RankingPage,
        },
        {
          path: 'search',
          name: 'search',
          component: SearchPage,
        },
        {
          path: 'singers',
          name: 'singers',
          component: SingersPage,
        },
        {
          path: 'singers/:id',
          name: 'singer-detail',
          component: SingerDetailPage,
        },
      ],
    },
  ],
})

export default router
