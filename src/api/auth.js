import request from './request'

/** 登录 */
export const loginAPI = (data) => request.post('/auth/login', data)

/** 刷新 token */
export const refreshTokenAPI = (data) => request.post('/auth/refresh', data)

/** 获取当前用户信息 */
export const getMeAPI = () => request.get('/auth/me')

/** 修改密码 */
export const changePasswordAPI = (data) => request.put('/auth/password', data)
