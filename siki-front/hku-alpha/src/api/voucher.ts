import request from '@/utils/request'
import type { PageQuery, PageResult } from '@/types'

// 后端 DTO 和 Entity 的简化 TypeScript 接口
export interface VoucherAddDTO {
  title: string
  subTitle?: string
  rules?: string
  actualValue: number // 分
  payValue: number // 分
  beginTime: string // LocalDateTime 字符串
  endTime: string // LocalDateTime 字符串
  stock: number
}

export interface Voucher extends VoucherAddDTO {
  id: number
  status: number // 1-上架 2-下架 3-过期
  createTime: string // LocalDateTime 字符串
}

// 新增代金券
export const addVoucherAPI = (data: VoucherAddDTO) => {
  return request({
    url: '/voucher',
    method: 'post',
    data
  })
}

// 分页查询代金券
export const getVoucherPageListAPI = (params: PageQuery) => {
  return request<PageResult<Voucher>>({
    url: '/voucher/page',
    method: 'get',
    params
  })
}

// 根据ID查询代金券 (如果需要编辑功能)
export const getVoucherByIdAPI = (id: number) => {
  return request<Voucher>({
    url: `/voucher/${id}`,
    method: 'get'
  })
}

// 更新代金券状态 (上架/下架)
export const updateVoucherStatusAPI = (id: number, status: number) => {
  return request({
    url: `/voucher/status/${id}/${status}`,
    method: 'put'
  })
}

// 更新代金券信息 (如果需要编辑功能，且后端接口与新增不同)
// export const updateVoucherAPI = (data: Voucher) => {
//   return request({
//     url: '/admin/voucher',
//     method: 'put',
//     data
//   })
// }

export const deleteVoucherAPI = (id: number) => {
  return request({
    url: `/voucher/${id}`,
    method: 'delete'
  })
}