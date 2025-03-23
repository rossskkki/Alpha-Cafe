import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCartListAPI } from '@/api/cart'

export const useCartStore = defineStore('cart', {
  state: () => {
    const cartList = ref<any[]>([])
    return { cartList }
  },
  getters: {
    // 购物车商品总数
    cartCount: (state) => computed(() => {
      return state.cartList.reduce((total, item) => total + item.number, 0)
    }),
    // 购物车商品总价
    cartTotal: (state) => computed(() => {
      return state.cartList.reduce((total, item) => {
        return total + item.number * item.amount
      }, 0).toFixed(2)
    })
  },
  actions: {
    // 获取购物车列表
    async getCartList() {
      try {
        const res = await getCartListAPI()
        if (res.data.code === 0) {
          this.cartList = res.data.data
        }
      } catch (error) {
        console.error('获取购物车列表失败', error)
      }
    },
    // 清空购物车
    clearCart() {
      this.cartList = []
    }
  },
  persist: true // 持久化存储
})