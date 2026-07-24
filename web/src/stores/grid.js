import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getGridsAPI,
  createGridAPI,
  updateGridAPI,
  deleteGridAPI,
} from '@/api/grid'

export const useGridStore = defineStore('grid', () => {
  const gridList = ref([])
  const loading = ref(false)

  async function fetchGrids() {
    loading.value = true
    try {
      const res = await getGridsAPI()
      // 后端 GET /grids 直接返回网格数组（Result<List<Grid>>），非分页对象
      gridList.value = Array.isArray(res.data) ? res.data : res.data?.list || []
    } catch (err) {
      console.error('获取网格列表失败:', err)
      gridList.value = []
    } finally {
      loading.value = false
    }
  }

  async function createGrid(data) {
    const res = await createGridAPI(data)
    return res.data
  }

  async function updateGrid(id, data) {
    const res = await updateGridAPI(id, data)
    return res.data
  }

  async function deleteGrid(id) {
    const res = await deleteGridAPI(id)
    return res.data
  }

  return {
    gridList,
    loading,
    fetchGrids,
    createGrid,
    updateGrid,
    deleteGrid,
  }
})
