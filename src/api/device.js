import request from './request'

/** 分页查询设备列表 */
export const getDevicesAPI = (params) => request.get('/devices', { params })

/** 获取设备树形结构 */
export const getDeviceTreeAPI = () => request.get('/devices/tree')

/** 获取单个设备详情 */
export const getDeviceByIdAPI = (id) => request.get(`/devices/${id}`)

/** 创建设备 */
export const createDeviceAPI = (data) => request.post('/devices', data)

/** 更新设备 */
export const updateDeviceAPI = (id, data) => request.put(`/devices/${id}`, data)

/** 删除设备 */
export const deleteDeviceAPI = (id) => request.delete(`/devices/${id}`)

/** 导入设备（文件上传） */
export const importDevicesAPI = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/devices/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/** 导出设备 */
export const exportDevicesAPI = (params) =>
  request.get('/devices/export', {
    params,
    responseType: 'blob',
  })

/** 获取设备类型列表 */
export const getDeviceTypesAPI = () => request.get('/devices/types')
