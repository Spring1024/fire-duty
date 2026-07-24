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
      const res = await getComplianceTrendAPI(params)
      // 后端返回 ComplianceData {months: string[], rates: number[]}，归一化为模板使用的 {month, rate}[]
      const raw = res.data
      if (Array.isArray(raw)) {
        compliance.value = raw
      } else if (raw && Array.isArray(raw.months)) {
        compliance.value = raw.months.map((month, i) => ({
          month,
          rate: raw.rates?.[i] ?? 0,
        }))
      } else {
        compliance.value = []
      }
    } catch (err) {
      console.error('获取合规率趋势失败:', err)
      compliance.value = []
    }
  }

  async function fetchHazardDistribution() {
    try {
      const res = await getHazardDistributionAPI()
      // 后端返回 HazardItem {type, rate, color}，模板使用 {name, percentage, color}
      hazardDistribution.value = (res.data || []).map((item) => ({
        name: item.name ?? item.type ?? '',
        percentage: item.percentage ?? item.rate ?? 0,
        color: item.color || '',
      }))
    } catch (err) {
      console.error('获取隐患分布失败:', err)
      hazardDistribution.value = []
    }
  }

  async function fetchSummary() {
    loading.value = true
    try {
      const res = await getStatisticsSummaryAPI()
      // 后端返回 SummaryData {overallComplianceRate, topHazardType, topHazardRate}
      summary.value = {
        maxHazardType: res.data?.maxHazardType ?? res.data?.topHazardType ?? '',
        maxHazardPercentage: res.data?.maxHazardPercentage ?? res.data?.topHazardRate ?? 0,
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
