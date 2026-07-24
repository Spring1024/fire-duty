import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getDashboardStatsAPI, getDashboardAlertsAPI } from '@/api/dashboard'

export const useDashboardStore = defineStore('dashboard', () => {
  const stats = ref({
    totalDevices: 0,
    onlineRate: 0,
    todayInspections: 0,
    pendingRectifications: 0,
    completionRate: 0,
    plannedTasks: 0,
    completedTasks: 0,
    overdueTasks: 0,
  })
  const alerts = ref([])
  const loading = ref(false)

  async function fetchStats() {
    try {
      const res = await getDashboardStatsAPI()
      stats.value = {
        totalDevices: res.data?.totalDevices ?? 0,
        onlineRate: res.data?.onlineRate ?? 0,
        todayInspections: res.data?.todayInspections ?? 0,
        pendingRectifications: res.data?.pendingRectifications ?? 0,
        completionRate: res.data?.completionRate ?? 0,
        plannedTasks: res.data?.plannedTasks ?? 0,
        completedTasks: res.data?.completedTasks ?? 0,
        overdueTasks: res.data?.overdueTasks ?? 0,
      }
    } catch (err) {
      console.error('获取仪表盘统计数据失败:', err)
    }
  }

  async function fetchAlerts() {
    loading.value = true
    try {
      const res = await getDashboardAlertsAPI()
      alerts.value = res.data || []
    } catch (err) {
      console.error('获取告警列表失败:', err)
      alerts.value = []
    } finally {
      loading.value = false
    }
  }

  return {
    stats,
    alerts,
    loading,
    fetchStats,
    fetchAlerts,
  }
})
