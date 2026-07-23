import request from './request'

/** 离线同步 */
export const syncMobileAPI = (since) =>
  request.get('/mobile/sync', { params: { since } })

/** 扫码检查提交 */
export const scanCheckAPI = (data) => request.post('/mobile/scan-check', data)

/**
 * 移动端 — 上传水印照片
 * POST /api/v1/mobile/photo
 */
export const uploadPhotoAPI = (deviceCode, file) => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('deviceCode', deviceCode)
  return request.post('/mobile/photo', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}
