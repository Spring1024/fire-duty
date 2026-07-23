import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getRectificationsAPI,
  getRectificationByIdAPI,
  dispatchRectificationAPI,
  submitRectificationFixAPI,
  reviewRectificationAPI,
  uploadRectificationPhotoAPI,
} from '@/api/rectification'

export const useRectificationStore = defineStore('rectification', () => {
  const rectList = ref([])
  const loading = ref(false)
  const total = ref(0)
  const tabCounts = ref({ pending: 0, ongoing: 0, review: 0, closed: 0 })
  const currentRect = ref(null)
  const timeline = ref([])
  const photos = ref({ before: null, after: null })

  async function fetchRectifications(params) {
    loading.value = true
    try {
      const res = await getRectificationsAPI(params)
      rectList.value = res.data?.list || []
      tabCounts.value = res.data?.counts || { pending: 0, ongoing: 0, review: 0, closed: 0 }
      total.value = res.data?.total || 0
    } catch (err) {
      console.error('获取隐患整改列表失败:', err)
      rectList.value = []
      tabCounts.value = { pending: 0, ongoing: 0, review: 0, closed: 0 }
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  async function fetchRectById(id) {
    try {
      const res = await getRectificationByIdAPI(id)
      currentRect.value = res.data?.rect || null
      timeline.value = res.data?.timeline || []
      photos.value = res.data?.photos || { before: null, after: null }
      return res.data
    } catch (err) {
      console.error('获取隐患详情失败:', err)
      currentRect.value = null
      timeline.value = []
      photos.value = { before: null, after: null }
      return null
    }
  }

  async function dispatchRect(id, data) {
    const res = await dispatchRectificationAPI(id, data)
    return res.data
  }

  async function submitFix(id, data) {
    const res = await submitRectificationFixAPI(id, data)
    return res.data
  }

  async function reviewRect(id, data) {
    const res = await reviewRectificationAPI(id, data)
    return res.data
  }

  async function uploadPhoto(id, file, type) {
    const res = await uploadRectificationPhotoAPI(id, file, type)
    return res.data
  }

  return {
    rectList,
    loading,
    total,
    tabCounts,
    currentRect,
    timeline,
    photos,
    fetchRectifications,
    fetchRectById,
    dispatchRect,
    submitFix,
    reviewRect,
    uploadPhoto,
  }
})
