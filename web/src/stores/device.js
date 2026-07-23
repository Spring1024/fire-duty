import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getDevicesAPI,
  getDeviceTreeAPI,
  getDeviceByIdAPI,
  createDeviceAPI,
  updateDeviceAPI,
  deleteDeviceAPI,
  importDevicesAPI,
} from '@/api/device'

export const useDeviceStore = defineStore('device', () => {
  const deviceList = ref([])
  const loading = ref(false)
  const total = ref(0)
  const treeData = ref([])
  const currentDevice = ref(null)

  async function fetchDevices(params) {
    loading.value = true
    try {
      const res = await getDevicesAPI(params)
      deviceList.value = res.data?.list || []
      total.value = res.data?.total || 0
    } catch (err) {
      console.error('获取设备列表失败:', err)
      deviceList.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  async function fetchTree() {
    try {
      const res = await getDeviceTreeAPI()
      treeData.value = res.data || []
    } catch (err) {
      console.error('获取设备树失败:', err)
      treeData.value = []
    }
  }

  async function fetchDeviceById(id) {
    try {
      const res = await getDeviceByIdAPI(id)
      currentDevice.value = res.data || null
      return res.data
    } catch (err) {
      console.error('获取设备详情失败:', err)
      currentDevice.value = null
      return null
    }
  }

  async function createDevice(data) {
    const res = await createDeviceAPI(data)
    return res.data
  }

  async function updateDevice(id, data) {
    const res = await updateDeviceAPI(id, data)
    return res.data
  }

  async function deleteDevice(id) {
    const res = await deleteDeviceAPI(id)
    return res.data
  }

  async function importDevices(file) {
    const res = await importDevicesAPI(file)
    return res.data
  }

  return {
    deviceList,
    loading,
    total,
    treeData,
    currentDevice,
    fetchDevices,
    fetchTree,
    fetchDeviceById,
    createDevice,
    updateDevice,
    deleteDevice,
    importDevices,
  }
})
