import { defineStore } from 'pinia'
import { getCurrentUser } from '../api/user'

const STORAGE_KEY = 'yinyu-web-current-user'

function readStoredUser() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null')
  } catch {
    return null
  }
}

function persistUser(user) {
  if (user?.id) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(user))
    return
  }
  localStorage.removeItem(STORAGE_KEY)
}

export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: readStoredUser(),
    authDialogVisible: false,
    currentUserLoaded: false,
  }),
  getters: {
    isLoggedIn(state) {
      return !!state.currentUser?.id
    },
  },
  actions: {
    async fetchCurrentUser() {
      try {
        const data = await getCurrentUser()
        this.setCurrentUser(data || null)
      } catch (error) {
        this.clearCurrentUser()
      }
      this.currentUserLoaded = true
      return this.currentUser
    },
    setCurrentUser(user) {
      this.currentUser = user || null
      persistUser(this.currentUser)
    },
    clearCurrentUser() {
      this.currentUser = null
      persistUser(null)
    },
    openAuthDialog() {
      this.authDialogVisible = true
    },
    closeAuthDialog() {
      this.authDialogVisible = false
    },
  },
})
