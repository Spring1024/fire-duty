import request from './request'

/** 分页查询巡检任务列表 */
export const getTasksAPI = (params) => request.get('/tasks', { params })

/** 下发巡检任务 */
export const dispatchTaskAPI = (data) => request.post('/tasks', data)

/** 获取任务模板列表 */
export const getTaskTemplatesAPI = () => request.get('/tasks/templates')

/** 创建任务模板 */
export const createTaskTemplateAPI = (data) =>
  request.post('/tasks/templates', data)

/** 提交巡检结果 */
export const submitTaskResultAPI = (id, data) =>
  request.post(`/tasks/${id}/submit`, data)
