import request from './request'

/** 获取合规率趋势 */
export const getComplianceTrendAPI = (params) =>
  request.get('/statistics/compliance', { params })

/** 获取隐患分布 */
export const getHazardDistributionAPI = () =>
  request.get('/statistics/hazard-distribution')

/** 获取统计摘要 */
export const getStatisticsSummaryAPI = () =>
  request.get('/statistics/summary')

/** 导出统计报表 */
export const exportStatisticsAPI = (params) =>
  request.get('/statistics/export', {
    params,
    responseType: 'blob',
  })
