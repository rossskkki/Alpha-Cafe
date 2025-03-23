import request from '@/utils/request'

/**
 * 获取用户地址列表
 * @returns 
 */
export const getAddressListAPI = () => {
  return request({
    url: '/address/list',
    method: 'get'
  })
}

/**
 * 添加用户地址
 * @param params 地址信息
 * @returns 
 */
export const addAddressAPI = (params: any) => {
  return request({
    url: '/address/add',
    method: 'post',
    data: { ...params }
  })
}

/**
 * 修改用户地址
 * @param params 地址信息
 * @returns 
 */
export const updateAddressAPI = (params: any) => {
  return request({
    url: '/address/update',
    method: 'put',
    data: { ...params }
  })
}

/**
 * 删除用户地址
 * @param id 地址ID
 * @returns 
 */
export const deleteAddressAPI = (id: number) => {
  return request({
    url: `/address/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 设置默认地址
 * @param id 地址ID
 * @returns 
 */
export const setDefaultAddressAPI = (id: number) => {
  return request({
    url: `/address/default/${id}`,
    method: 'put'
  })
}