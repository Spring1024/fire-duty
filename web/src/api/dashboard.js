import request from './request'

/** 获取仪表盘统计数据 */
export const getDashboardStatsAPI = () => request.get('/statistics/dashboard-stats')

/** 获取仪表盘告警列表 */
export const getDashboardAlertsAPI = () => request.get('/statistics/dashboard-alerts')
