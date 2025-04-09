import request from '@/utils/request'

/**
 * 添加购物车
 * @param params 购物车数据
 * @returns 
 */
export const addToCartAPI = (params: any) => {
  return request({
    url: '/shoppingCart/add',
    method: 'post',
    data: { ...params }
  })
}

/**
 * 查看购物车
 * @returns 
 */
export const getCartListAPI = () => {
  return request({
    url: '/shoppingCart/list',
    method: 'get'
  })
}

/**
 * 清空购物车
 * @returns 
 */
export const clearCartAPI = () => {
  return request({
    url: '/shoppingCart/clean',
    method: 'delete'
  })
}

/**
 * 删除购物车中的商品
 * @param id 购物车ID
 * @returns 
 */
export const deleteCartItemAPI = (id: number) => {
  return request({
    url: '/shoppingCart/delete',
    method: 'delete',
    params: { id }
  })
}

/**
 * 修改购物车商品数量
 * @param params 购物车数据
 * @returns 
 */
export const updateCartItemAPI = (params: any) => {
  return request({
    url: '/shoppingCart/update',
    method: 'put',
    data: { ...params }
  })
}
  
export const subCartItemAPI = (params: any) => {
  return request({
    url: '/shoppingCart/sub',
    method: 'post',
    data: { ...params }
  })
}

export const addCartItemAPI = (params: any) => {
  return request({
    url: '/shoppingCart/add',
    method: 'post',
    data: { ...params }
  })
}