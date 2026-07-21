import request from './request'

/** 分页查询用户列表 */
export const getUsersAPI = (params) => request.get('/users', { params })

/** 创建用户 */
export const createUserAPI = (data) => request.post('/users', data)

/** 更新用户 */
export const updateUserAPI = (id, data) => request.put(`/users/${id}`, data)

/** 删除用户 */
export const deleteUserAPI = (id) => request.delete(`/users/${id}`)
