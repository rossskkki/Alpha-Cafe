import request from '@/utils/request'

/**
 * 用户登录
 * @param params 登录参数
 * @returns 
 */
export const loginAPI = (params: any) => {
  return request({
    url: '/user/login',
    method: 'post',
    data: { ...params }
  })
}

/**
 * 用户注册
 * @param params 注册参数
 * @returns 
 */
export const registerAPI = (params: any) => {
  return request({
    url: '/user/register',
    method: 'post',
    data: { ...params }
  })
}

/**
 * 获取用户信息
 * @returns 
 */
export const getUserInfoAPI = () => {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

/**
 * 修改用户信息
 * @param params 用户信息
 * @returns 
 */
export const updateUserInfoAPI = (params: any) => {
  return request({
    url: '/user/update',
    method: 'put',
    data: { ...params }
  })
}

/**
 * 用户登出
 * @returns 
 */
export const logoutAPI = () => {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}