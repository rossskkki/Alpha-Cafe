import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface UserInfo {
  id: number
  name: string
  phone: string
  token: string
}

export const useUserStore = defineStore('user', {
  state: () => {
    const userInfo = ref<UserInfo | null>(null)
    return { userInfo }
  },
  actions: {
    // 设置用户信息
    setUserInfo(user: UserInfo) {
      this.userInfo = user
    },
    // 清除用户信息
    clearUserInfo() {
      this.userInfo = null
    }
  },
  persist: true // 持久化存储
})