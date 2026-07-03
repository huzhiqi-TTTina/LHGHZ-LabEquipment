<template>
  <div class="ai-chat-container">
    <!-- 头部 -->
    <div class="chat-header">
      <el-icon><ChatDotRound /></el-icon>
      <span>AI智能助手</span>
      <el-tag type="success" size="small" class="ai-tag">Qwen</el-tag>
    </div>

    <!-- 聊天消息区 -->
    <div class="chat-messages" ref="messagesContainer">
      <!-- 欢迎消息 -->
      <div class="message ai-message">
        <el-avatar :size="40" class="message-avatar ai-avatar">AI</el-avatar>
        <div class="message-content">
          <div class="message-text">
            <p>您好！👋 我是实验室设备管理的AI助手。</p>
            <p style="margin-top: 10px;">我可以帮您：</p>
            <ul>
              <li>了解设备的用途和操作方法</li>
              <li>查询设备状态和规格参数</li>
              <li>推荐适合您的设备</li>
              <li>解答预约和借用相关问题</li>
            </ul>
            <p style="margin-top: 10px;">请问有什么可以帮您？</p>
          </div>
        </div>
      </div>

      <!-- 对话消息列表 -->
      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['message', msg.role === 'user' ? 'user-message' : 'ai-message']"
      >
        <el-avatar :size="40" class="message-avatar" :class="msg.role === 'user' ? 'user-avatar' : 'ai-avatar'">
          {{ msg.role === 'user' ? userStore.realName?.charAt(0) || '我' : 'AI' }}
        </el-avatar>
        <div class="message-content">
          <div class="message-text" v-html="formatMessage(msg.content)"></div>

          <!-- 设备推荐列表 -->
          <div v-if="msg.recommendations && msg.recommendations.length > 0" class="recommendations">
            <div class="recommendations-title">
              <el-icon><Monitor /></el-icon>
              <span>为您推荐以下设备：</span>
            </div>
            <div class="equipment-list">
              <div
                v-for="eq in msg.recommendations"
                :key="eq.id"
                class="equipment-card"
                @click="viewEquipment(eq)"
              >
                <div class="equipment-name">{{ eq.equipmentName }}</div>
                <div class="equipment-info">
                  {{ eq.brand }} {{ eq.model }}
                </div>
                <el-tag :type="getStatusType(eq.status)" size="small">
                  {{ getStatusText(eq.status) }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="message ai-message">
        <el-avatar :size="40" class="message-avatar ai-avatar">AI</el-avatar>
        <div class="message-content">
          <div class="loading-indicator">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>正在思考...</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区 -->
    <div class="chat-input">
      <el-input
        v-model="userInput"
        placeholder="输入您的问题，例如：推荐一些电子测量设备"
        size="large"
        @keyup.enter="sendMessage"
        :disabled="loading"
      >
        <template #append>
          <el-button
            type="primary"
            @click="sendMessage"
            :loading="loading"
            :disabled="!userInput.trim()"
          >
            发送
          </el-button>
        </template>
      </el-input>
    </div>

    <!-- 快捷提问 -->
    <div class="quick-questions">
      <span class="quick-label">快捷问题：</span>
      <el-button
        v-for="q in quickQuestions"
        :key="q.text"
        size="small"
        @click="quickAsk(q.text)"
        :disabled="loading"
      >
        {{ q.text }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { aiApi } from '@/api/ai'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const userInput = ref('')
const loading = ref(false)
const messages = ref([])
const messagesContainer = ref(null)

const quickQuestions = [
  { text: '推荐常用设备' },
  { text: '如何预约设备？' },
  { text: '显微镜的使用方法' },
  { text: '有哪些电子设备？' },
  { text: '设备借出流程' }
]

// 发送消息
const sendMessage = async () => {
  if (!userInput.value.trim() || loading.value) return

  const userMessage = userInput.value
  messages.value.push({ role: 'user', content: userMessage })
  userInput.value = ''
  loading.value = true

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  try {
    const res = await aiApi.chat({
      message: userMessage,
      userId: userStore.userId
    })

    if (res.code === 200) {
      messages.value.push({
        role: 'assistant',
        content: res.data.reply,
        recommendations: res.data.recommendations || []
      })
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (error) {
    console.error('AI聊天失败:', error)
    ElMessage.error('AI服务暂时不可用，请稍后重试')
    // 添加错误提示消息
    messages.value.push({
      role: 'assistant',
      content: '抱歉，AI服务暂时不可用。请检查后端服务是否启动，或联系管理员配置讯飞AI API Key。'
    })
  } finally {
    loading.value = false
    await nextTick()
    scrollToBottom()
  }
}

// 快捷提问
const quickAsk = (question) => {
  userInput.value = question
  sendMessage()
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 格式化消息（支持简单的Markdown）
const formatMessage = (content) => {
  if (!content) return ''
  // 简单的Markdown渲染
  return content
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')  // 粗体
    .replace(/\*(.*?)\*/g, '<em>$1</em>')              // 斜体
    .replace(/`(.*?)`/g, '<code>$1</code>')              // 行内代码
    .replace(/\n/g, '<br>')                            // 换行
}

// 获取状态类型
const getStatusType = (status) => {
  const typeMap = {
    'NORMAL': 'success',
    'BORROWED': 'warning',
    'MAINTENANCE': 'danger',
    'SCRAPPED': 'info'
  }
  return typeMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    'NORMAL': '正常',
    'BORROWED': '借出中',
    'MAINTENANCE': '维修中',
    'SCRAPPED': '报废'
  }
  return textMap[status] || status
}

// 查看设备详情
const viewEquipment = (equipment) => {
  router.push({ name: 'Equipment' })
}
</script>

<style lang="scss" scoped>
.ai-chat-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 120px);
  background: #f0f2f5;
}

.chat-header {
  padding: 16px 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 18px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .ai-tag {
    margin-left: auto;
    background: rgba(255, 255, 255, 0.2);
    border: none;
    color: white;
  }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  scroll-behavior: smooth;

  .message {
    display: flex;
    gap: 12px;
    margin-bottom: 20px;
    animation: fadeIn 0.3s ease;

    &.user-message {
      flex-direction: row-reverse;

      .message-content {
        background: #1890ff;
        color: white;
        border-radius: 16px 16px 4px 16px;

        .recommendations-title {
          color: white;
        }
      }
    }

    &.ai-message .message-content {
      background: white;
      color: #333;
      border-radius: 16px 16px 16px 4px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    }

    .message-avatar {
      flex-shrink: 0;

      &.user-avatar {
        background: #1890ff;
        color: white;
      }

      &.ai-avatar {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
      }
    }

    .message-content {
      max-width: 75%;
      padding: 14px 18px;

      .message-text {
        line-height: 1.7;
        font-size: 14px;

        ul {
          margin: 8px 0;
          padding-left: 20px;
        }

        li {
          margin: 4px 0;
        }

        code {
          background: #f5f5f5;
          padding: 2px 6px;
          border-radius: 4px;
          font-family: Consolas, monospace;
        }
      }

      .loading-indicator {
        display: flex;
        align-items: center;
        gap: 8px;
        color: #999;
      }

      .recommendations {
        margin-top: 16px;
        padding-top: 12px;
        border-top: 1px solid #eee;

        .recommendations-title {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 13px;
          font-weight: 500;
          color: #666;
          margin-bottom: 10px;
        }

        .equipment-list {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
          gap: 10px;
        }

        .equipment-card {
          background: #f8f9fa;
          padding: 12px;
          border-radius: 8px;
          cursor: pointer;
          transition: all 0.2s;
          border: 1px solid transparent;

          &:hover {
            background: #e8f4ff;
            border-color: #1890ff;
            transform: translateY(-2px);
          }

          .equipment-name {
            font-weight: 500;
            font-size: 13px;
            margin-bottom: 4px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }

          .equipment-info {
            font-size: 11px;
            color: #999;
            margin-bottom: 6px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
          }
        }
      }
    }
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.chat-input {
  padding: 16px 20px;
  background: white;
  border-top: 1px solid #e8e8e8;
}

.quick-questions {
  padding: 12px 20px 16px;
  background: white;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;

  .quick-label {
    font-size: 13px;
    color: #999;
    flex-shrink: 0;
  }
}
</style>
