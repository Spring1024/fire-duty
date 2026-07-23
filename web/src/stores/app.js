import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { loginAPI, getMeAPI } from '@/api/auth'

export const useAppStore = defineStore('app', {
  state: () => ({
    user: {
      name: '',
      username: '',
      role: '',
      roles: [],
      avatar: '',
    },
    sidebarCollapsed: false,
  }),
  getters: {
    isLoggedIn: (state) => !!localStorage.getItem('token'),
  },
  actions: {
    async login(credentials) {
      console.log("credentials", credentials)
      const res = await loginAPI(credentials)
      console.log("res", res)
      const { token, refreshToken, user: userInfo } = res.data

      localStorage.setItem('token', token)
      if (refreshToken) {
        localStorage.setItem('refreshToken', refreshToken)
      }

      this.user = {
        name: userInfo?.name || credentials.username,
        username: userInfo?.username || credentials.username,
        role: userInfo?.role || '',
        roles: userInfo?.roles || [],
        avatar: userInfo?.avatar || '',
      }

      return res
    },

    async fetchUserInfo() {
      try {
        const res = await getMeAPI()
        this.user = {
          name: res.data?.name || '',
          username: res.data?.username || '',
          role: res.data?.role || '',
          roles: res.data?.roles || [],
          avatar: res.data?.avatar || '',
        }
      } catch (err) {
        console.error('获取用户信息失败:', err)
      }
    },

    logout() {
      this.user = { name: '', username: '', role: '', roles: [], avatar: '' }
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
    },

    toggleSidebar() {
      this.sidebarCollapsed = !this.sidebarCollapsed
    },
  },
})
