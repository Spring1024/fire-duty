import request from './request'

/** 获取网格列表 */
export const getGridsAPI = () => request.get('/grids')

/** 创建网格 */
export const createGridAPI = (data) => request.post('/grids', data)

/** 更新网格 */
export const updateGridAPI = (id, data) => request.put(`/grids/${id}`, data)

/** 删除网格 */
export const deleteGridAPI = (id) => request.delete(`/grids/${id}`)
