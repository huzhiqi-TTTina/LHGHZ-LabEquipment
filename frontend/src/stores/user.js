import { defineStore } from 'pinia'
import { authApi } from '@/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null')
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.userInfo?.role === 'ADMIN',
    userId: (state) => state.userInfo?.userId || null,
    username: (state) => state.userInfo?.username || '',
    realName: (state) => state.userInfo?.realName || '',
    role: (state) => state.userInfo?.role || '',
    avatar: (state) => state.userInfo?.avatar || ''
  },

  actions: {
    // 设置token
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },

    // 设置用户信息
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },

    // 登录
    async login(loginData) {
      const res = await authApi.login(loginData)
      this.setToken(res.data.token)
      this.setUserInfo(res.data)
      return res
    },

    // 登出
    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },

    // 获取用户信息
    async getUserInfo() {
      const res = await authApi.getCurrentUser()
      this.setUserInfo(res.data)
      return res
    },

    // 修改密码
    async changePassword(data) {
      return authApi.changePassword(data)
    }
  }
})
