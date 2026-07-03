import request from './request'

export const aiApi = {
  /**
   * 发送聊天消息
   * @param {Object} data - { message: string, userId?: number }
   */
  chat(data) {
    return request({
      url: '/api/ai/chat',
      method: 'post',
      data
    })
  },

  /**
   * 获取设备推荐
   * @param {number} userId - 用户ID
   */
  getRecommendations(userId) {
    return request({
      url: '/api/ai/recommend',
      method: 'get',
      params: { userId }
    })
  },

  /**
   * 检查AI服务状态
   */
  checkStatus() {
    return request({
      url: '/api/ai/status',
      method: 'get'
    })
  }
}
