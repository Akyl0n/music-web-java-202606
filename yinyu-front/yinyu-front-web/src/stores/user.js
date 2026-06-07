import { defineStore } from 'pinia'
import { getCurrentUser } from '../api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    currentUser: null,
    authDialogVisible: false,
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
        this.currentUser = data || null
      } catch (error) {
        this.currentUser = null
      }
      return this.currentUser
    },
    setCurrentUser(user) {
      this.currentUser = user || null
    },
    clearCurrentUser() {
      this.currentUser = null
    },
    openAuthDialog() {
      this.authDialogVisible = true
    },
    closeAuthDialog() {
      this.authDialogVisible = false
    },
  },
})
