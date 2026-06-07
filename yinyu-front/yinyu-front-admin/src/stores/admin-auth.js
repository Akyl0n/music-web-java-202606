import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { adminLogin, adminLogout, getAdminCurrent } from '../api/auth'

const STORAGE_KEY = 'yinyu-admin-session'

function readStorage() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || 'null')
  } catch {
    return null
  }
}

export const useAdminAuthStore = defineStore('admin-auth', () => {
  const session = ref(readStorage() || null)

  const isLoggedIn = computed(() => Boolean(session.value?.username))
  const username = computed(() => session.value?.username || '')
  const displayName = computed(() => session.value?.displayName || '')

  function persist(value) {
    session.value = value
    if (value) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(value))
    } else {
      localStorage.removeItem(STORAGE_KEY)
    }
  }

  async function login(payload) {
    const data = await adminLogin(payload)
    persist(data)
    return data
  }

  async function fetchCurrent() {
    const data = await getAdminCurrent()
    persist(data)
    return data
  }

  async function logout() {
    try {
      await adminLogout()
    } finally {
      persist(null)
    }
  }

  function clear() {
    persist(null)
  }

  return {
    session,
    isLoggedIn,
    username,
    displayName,
    login,
    fetchCurrent,
    logout,
    clear,
  }
})
