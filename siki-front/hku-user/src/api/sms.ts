import request from '@/utils/request'

/**
 * 发送短信验证码
 * @param params 包含手机号的参数对象
 * @returns 
 */
export const sendSmsCodeAPI = (params: { phone: string }) => {
  return request({
    url: '/user/sms',
    method: 'post',
    data: params
  })
}

/**
 * 使用短信验证码登录
 * @param params 登录参数，包含手机号和验证码
 * @returns 
 */
export const loginBySmsAPI = (loginForm: { phone: string, code: string }) => {
  return request({
    url: '/user/login',
    method: 'post',
    data: loginForm
  })
}