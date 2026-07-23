import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getComplianceTrendAPI,
  getHazardDistributionAPI,
  getStatisticsSummaryAPI,
} from '@/api/statistics'

export const useStatisticsStore = defineStore('statistics', () => {
  const compliance = ref([])
  const hazardDistribution = ref([])
  const summary = ref({
    maxHazardType: '',
    maxHazardPercentage: 0,
    overallComplianceRate: 0,
  })
  const loading = ref(false)

  async function fetchCompliance(params) {
    try {
      console.log("params", params)
      const res = await getComplianceTrendAPI(params)
      compliance.value = res.data || []
    } catch (err) {
      console.error('获取合规率趋势失败:', err)
      compliance.value = []
    }
  }

  async function fetchHazardDistribution() {
    try {
      const res = await getHazardDistributionAPI()
      hazardDistribution.value = res.data || []
    } catch (err) {
      console.error('获取隐患分布失败:', err)
      hazardDistribution.value = []
    }
  }

  async function fetchSummary() {
    loading.value = true
    try {
      const res = await getStatisticsSummaryAPI()
      summary.value = {
        maxHazardType: res.data?.maxHazardType ?? '',
        maxHazardPercentage: res.data?.maxHazardPercentage ?? 0,
        overallComplianceRate: res.data?.overallComplianceRate ?? 0,
      }
    } catch (err) {
      console.error('获取统计摘要失败:', err)
      summary.value = { maxHazardType: '', maxHazardPercentage: 0, overallComplianceRate: 0 }
    } finally {
      loading.value = false
    }
  }

  return {
    compliance,
    hazardDistribution,
    summary,
    loading,
    fetchCompliance,
    fetchHazardDistribution,
    fetchSummary,
  }
})
