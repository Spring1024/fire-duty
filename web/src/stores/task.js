import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getTasksAPI,
  dispatchTaskAPI,
  getTaskTemplatesAPI,
  createTaskTemplateAPI,
  submitTaskResultAPI,
} from '@/api/task'

export const useTaskStore = defineStore('task', () => {
  const taskList = ref([])
  const loading = ref(false)
  const templateList = ref([])
  const tabCounts = ref({ pending: 0, completed: 0, overdue: 0 })

  // 后端 task.status 为英文枚举，模板按中文渲染，store 层统一归一化
  const STATUS_TEXT_MAP = {
    draft: '草稿',
    pending: '待检查',
    in_progress: '检查中',
    completed: '已完成',
    overdue: '已超时',
  }

  async function fetchTasks(params) {
    loading.value = true
    try {
      const res = await getTasksAPI(params)
      const page = res.data || {}
      // 后端返回 MyBatis-Plus IPage（records/total），契约文档为 list/counts
      taskList.value = (page.records || page.list || []).map((t) => ({
        ...t,
        status: STATUS_TEXT_MAP[t.status] || t.status,
      }))
      // 后端不返回各 tab 的 counts，以 total 反映当前 tab 数量
      tabCounts.value = page.counts || {
        [params?.status || 'pending']: page.total ?? taskList.value.length,
      }
    } catch (err) {
      console.error('获取任务列表失败:', err)
      taskList.value = []
      tabCounts.value = { pending: 0, completed: 0, overdue: 0 }
    } finally {
      loading.value = false
    }
  }

  async function dispatchTask(data) {
    const res = await dispatchTaskAPI(data)
    return res.data
  }

  async function fetchTemplates() {
    try {
      const res = await getTaskTemplatesAPI()
      templateList.value = res.data || []
    } catch (err) {
      console.error('获取任务模板失败:', err)
      templateList.value = []
    }
  }

  async function createTemplate(data) {
    const res = await createTaskTemplateAPI(data)
    return res.data
  }

  async function submitTask(id, data) {
    const res = await submitTaskResultAPI(id, data)
    return res.data
  }

  return {
    taskList,
    loading,
    templateList,
    tabCounts,
    fetchTasks,
    dispatchTask,
    fetchTemplates,
    createTemplate,
    submitTask,
  }
})
