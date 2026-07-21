import request from './request'

/** 分页查询隐患整改列表 */
export const getRectificationsAPI = (params) =>
  request.get('/rectifications', { params })

/** 获取隐患整改详情（含时间线 + 照片） */
export const getRectificationByIdAPI = (id) =>
  request.get(`/rectifications/${id}`)

/** 派发整改任务 */
export const dispatchRectificationAPI = (id, data) =>
  request.put(`/rectifications/${id}/dispatch`, data)

/** 提交整改结果 */
export const submitRectificationFixAPI = (id, data) =>
  request.put(`/rectifications/${id}/submit-fix`, data)

/** 复核整改结果 */
export const reviewRectificationAPI = (id, data) =>
  request.put(`/rectifications/${id}/review`, data)

/** 上传整改照片 */
export const uploadRectificationPhotoAPI = (id, file, type) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/rectifications/${id}/photos`, formData, {
    params: { type },
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
