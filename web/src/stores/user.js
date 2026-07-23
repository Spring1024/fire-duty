import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getUsersAPI,
  createUserAPI,
  updateUserAPI,
  deleteUserAPI,
} from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const userList = ref([])
  const loading = ref(false)
  const total = ref(0)

  async function fetchUsers(params) {
    loading.value = true
    try {
      const res = await getUsersAPI(params)
      userList.value = res.data?.records || []
      total.value = res.data?.total || 0
    } catch (err) {
      console.error('获取用户列表失败:', err)
      userList.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  async function createUser(data) {
    const res = await createUserAPI(data)
    return res.data
  }

  async function updateUser(id, data) {
    const res = await updateUserAPI(id, data)
    return res.data
  }

  async function deleteUser(id) {
    const res = await deleteUserAPI(id)
    return res.data
  }

  return {
    userList,
    loading,
    total,
    fetchUsers,
    createUser,
    updateUser,
    deleteUser,
  }
})
