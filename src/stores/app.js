import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAppStore = defineStore('app', () => {
  // User info
  const user = ref({
    name: '管理员',
    username: 'admin',
    role: '超级管理员',
    avatar: '',
  })

  const isLoggedIn = computed(() => !!user.value.username)

  // Sidebar collapse
  const sidebarCollapsed = ref(false)
  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  // Login / logout
  function login(credentials) {
    user.value = {
      name: credentials.username === 'admin' ? '管理员' : credentials.username,
      username: credentials.username,
      role: '超级管理员',
    }
    sessionStorage.setItem('token', 'mock-token')
  }

  function logout() {
    user.value = { name: '', username: '', role: '', avatar: '' }
    sessionStorage.removeItem('token')
    localStorage.removeItem('token')
  }

  return {
    user,
    isLoggedIn,
    sidebarCollapsed,
    toggleSidebar,
    login,
    logout,
  }
})
