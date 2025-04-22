import request from '@/utils/request'

// 用户端代金券接口定义
export interface UserVoucher {
  id: number
  title: string
  subTitle?: string
  actualValue: number // 分
  payValue: number // 分
  beginTime: string // LocalDateTime 字符串
  endTime: string // LocalDateTime 字符串
  stock: number
  // 可以根据需要添加其他用户端需要的字段，例如图片URL
  imageUrl?: string // 假设后端会提供图片URL
}

/**
 * 获取可供秒杀的代金券列表
 * @returns Promise<UserVoucher[]>
 */
export const getSeckillVouchersAPI = () => {
  // 注意：这里的 URL '/voucher/seckill/list' 是假设的，需要后端实现此接口
  return request<UserVoucher[]>({
    url: '/voucher/seckill/list',
    method: 'get'
  })
}

/**
 * 根据ID获取代金券详情 (用户端)
 * @param id 代金券ID
 * @returns Promise<UserVoucher>
 */
export const getVoucherDetailByIdAPI = (id: number) => {
  // 注意：这里的 URL `/voucher/detail/${id}` 是假设的，需要后端实现此接口
  return request<UserVoucher>({
    url: `/voucher/detail/${id}`,
    method: 'get'
  })
}