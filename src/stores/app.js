import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginAPI, getMeAPI } from '@/api/auth'

export const useAppStore = defineStore('app', () => {
  // User info
  const user = ref({
    name: '',
    username: '',
    role: '',
    avatar: '',
  })

  const isLoggedIn = computed(() => !!localStorage.getItem('token'))

  // Sidebar collapse
  const sidebarCollapsed = ref(false)
  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  // Login / logout
  async function login(credentials) {
    const res = await loginAPI(credentials)
    const { token, refreshToken, user: userInfo } = res.data

    localStorage.setItem('token', token)
    if (refreshToken) {
      localStorage.setItem('refreshToken', refreshToken)
    }

    user.value = {
      name: userInfo?.name || credentials.username,
      username: userInfo?.username || credentials.username,
      role: userInfo?.role || '',
      avatar: userInfo?.avatar || '',
    }

    return res
  }

  async function fetchUserInfo() {
    try {
      const res = await getMeAPI()
      user.value = {
        name: res.data?.name || '',
        username: res.data?.username || '',
        role: res.data?.role || '',
        avatar: res.data?.avatar || '',
      }
    } catch (err) {
      console.error('获取用户信息失败:', err)
    }
  }

  function logout() {
    user.value = { name: '', username: '', role: '', avatar: '' }
    localStorage.removeItem('token')
    localStorage.removeItem('refreshToken')
  }

  return {
    user,
    isLoggedIn,
    sidebarCollapsed,
    toggleSidebar,
    login,
    fetchUserInfo,
    logout,
  }
})
