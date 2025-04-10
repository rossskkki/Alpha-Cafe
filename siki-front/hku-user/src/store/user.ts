import { defineStore } from 'pinia'
import { ref } from 'vue'
import { updateUserInfoAPI } from '../api/user'

export interface UserInfo {
  id: number
  name: string
  phone: string
  token: string
  avatar?: string
  nickName?: string
}

export const useUserStore = defineStore('user', {
  state: () => {
    const userInfo = ref<UserInfo | null>(null)
    return { userInfo }
  },
  actions: {
    // 更新用户信息
    updateIcon(user: UserInfo) {
     this.userInfo.icon = user
    },

    getUserInfo() {
      return this.userInfo
    },

    // 设置用户信息
    setUserInfo(user: UserInfo) {
      this.userInfo = user
    },

    // 更新用户信息
    updateUserInfo(user: UserInfo) {
      this.userInfo.nickName = user.nickName
      this.userInfo.phone = user.phone
    },

    // 从 localStorage 恢复用户信息
    initUserInfo() {
      const userInfo = localStorage.getItem('userInfo')
      if (userInfo) {
        this.userInfo = JSON.parse(userInfo)
      }
    },
    // 清除用户信息
    clearUserInfo() {
      this.userInfo = null
    }
  },
  persist: true // 持久化存储
})