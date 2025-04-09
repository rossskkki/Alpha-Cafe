import request from '@/utils/request'

/**
 * 提交订单
 * @param params 订单数据
 * @returns 
 */
export const submitOrderAPI = (params: any) => {
  return request({
    url: '/order/submit',
    method: 'post',
    data: { ...params }
  })
}

/**
 * 获取订单列表
 * @param params 分页参数
 * @returns 
 */
export const getOrderListAPI = (params: any) => {
  return request({
    url: '/order/historyOrders',
    method: 'get',
    params
  })
}

/**
 * 获取订单详情
 * @param id 订单ID
 * @returns 
 */
export const getOrderDetailAPI = (id: string) => {
  return request({
    url: `/order/userOrderDetail/${id}`,
    method: 'get'
  })
}

/**
 * 取消订单
 * @param id 订单ID
 * @returns 
 */
export const cancelOrderAPI = (id: string) => {
  return request({
    url: `/order/cancel/${id}`,
    method: 'put'
  })
}

/**
 * 再来一单
 * @param id 订单ID
 * @returns 
 */
export const repeatOrderAPI = (id: string) => {
  return request({
    url: `/order/again/${id}`,
    method: 'post'
  })
}