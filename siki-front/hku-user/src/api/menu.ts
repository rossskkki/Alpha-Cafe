import request from '@/utils/request'

/**
 * 获取菜品分类列表
 * @returns 
 */
export const getCategoryListAPI = () => {
  return request({
    url: '/category/list',
    method: 'get'
  })
}

/**
 * 获取热点菜品列表
 * @returns 
 */
export const getHotDishListAPI = () => {
  return request({
    url: '/dish/hot',
    method: 'get'
  })
}

/**
 * 获取菜品列表
 * @param categoryId 分类ID
 * @returns 
 */
export const getDishListAPI = (categoryId: number) => {
  return request({
    url: '/dish/list',
    method: 'get',
    params: { categoryId }
  })
}

/**
 * 获取套餐列表
 * @param categoryId 分类ID
 * @returns 
 */
export const getSetmealListAPI = (categoryId: number) => {
  return request({
    url: '/setmeal/list',
    method: 'get',
    params: { categoryId }
  })
}

/**
 * 获取菜品详情
 * @param id 菜品ID
 * @returns 
 */
export const getDishDetailAPI = (id: number) => {
  return request({
    url: `/dish/${id}`,
    method: 'get'
  })
}

/**
 * 获取热点菜品详情
 * @param id 套餐ID
 * @returns
 */
export const getHotDishDetailAPI = (id: number) => {
  return request({
    url: `/dish/hot/${id}`,
    method: 'get' 
  }) 
}

/**
 * 获取套餐详情
 * @param id 套餐ID
 * @returns 
 */
export const getSetmealDetailAPI = (id: number) => {
  return request({
    url: `/setmeal/${id}`,
    method: 'get'
  })
}