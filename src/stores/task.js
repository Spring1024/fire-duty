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

  async function fetchTasks(params) {
    loading.value = true
    try {
      const res = await getTasksAPI(params)
      taskList.value = res.data?.list || []
      tabCounts.value = res.data?.counts || { pending: 0, completed: 0, overdue: 0 }
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
