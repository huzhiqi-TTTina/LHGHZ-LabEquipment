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
  }
}
