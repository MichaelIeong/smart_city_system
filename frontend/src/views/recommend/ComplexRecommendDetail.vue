<template>
  <page-header-wrapper>
    <div class="complex-recommend-page">
      <!-- 左侧聊天区域 -->
      <div class="chat-wrapper">
        <div class="chat-container" ref="chatContainer">
          <div
            v-for="(msg, index) in chatHistory"
            :key="index"
            :class="['chat-message', msg.role]"
          >
            <div class="message-bubble">{{ msg.content }}</div>
            <div v-if="msg.role === 'assistant' && msg.jsonResult" class="json-result">
              <div class="json-rule-container">
                <json-viewer
                  :value="msg.jsonResult"
                  :expand-depth="10"
                  copyable
                  boxed
                  theme="dark"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="input-area">
          <input
            v-model="inputContent"
            type="text"
            placeholder="请输入复杂应用描述..."
            @keyup.enter="sendMessage"
            :disabled="isLoading"
          />
          <button @click="sendMessage" :disabled="isLoading">
            {{ isLoading ? '生成中...' : '发送' }}
          </button>
        </div>
      </div>
    </div>
  </page-header-wrapper>
</template>
<script setup>
 /* eslint-disable */
import { ref, nextTick, computed } from 'vue'
import { generateComplexJsonRule } from '@/api/manage'
import { v4 as uuidv4 } from 'uuid'

const uuid = uuidv4()
const inputContent = ref('')
const isLoading = ref(false)
const chatContainer = ref(null)

const chatHistory = ref([
  {
    role: 'assistant',
    content: '您好，我是一个生成复杂应用JSON规则的智能助手！',
    jsonResult: null
  }
])

const latestJson = computed(() => {
  const reversed = [...chatHistory.value].reverse()
  return reversed.find(msg => msg.jsonResult)?.jsonResult || null
})

async function sendMessage() {
  const content = inputContent.value.trim()
  if (!content) return

  chatHistory.value.push({ role: 'user', content })

  const loadingIndex = chatHistory.value.length
  chatHistory.value.push({
    role: 'assistant',
    content: '生成中...',
    jsonResult: null
  })

  inputContent.value = ''
  isLoading.value = true

  try {
    const json = await generateComplexJsonRule(uuid, content)
    chatHistory.value.splice(loadingIndex, 1, {
      role: 'assistant',
      content: 'JSON规则已生成：',
      jsonResult: json
    })
  } catch (error) {
    chatHistory.value.splice(loadingIndex, 1, {
      role: 'assistant',
      content: '生成失败，请重试',
      jsonResult: { error: error.message }
    })
  } finally {
    isLoading.value = false
    await nextTick()
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  }
}
</script>
<style lang="less" scoped>
html, body, #app {
  height: 100%;
  margin: 0;
}

.complex-recommend-page {
  display: flex;
  height: calc(100vh - 250px); // 留出头部高度
  overflow: hidden;
}

.chat-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-right: 1px solid #d1d5db;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.chat-message {
  display: flex;
  flex-direction: column;
  max-width: 90%;
}

.chat-message.user {
  align-self: flex-end;
  .message-bubble {
    background-color: #3b82f6;
    color: white;
    border-bottom-right-radius: 0;
  }
  // 宽度控制：让包含 JSON 的区域放宽
  .json-result {
    margin-top: 0.5rem;
    width: 100%;
    max-width: 100%; // 放开限制
  }

  .json-rule-container {
    width: 100%;
  }
}

.chat-message.assistant {
  align-self: flex-start;
  .message-bubble {
    background-color: #e5e7eb;
    color: #374151;
    border-bottom-left-radius: 0;
  }
}

.message-bubble {
  padding: 0.5rem 1rem;
  border-radius: 1rem;
  font-size: 14px;
  white-space: pre-wrap;
  word-break: break-word;
}

.input-area {
  display: flex;
  padding: 1rem;
  border-top: 1px solid #d1d5db;
  background: #f9fafb;
  gap: 0.5rem;
}

.input-area input {
  flex: 1;
  padding: 0.5rem 1rem;
  border: 1px solid #cbd5e1;
  border-radius: 9999px;
  font-size: 14px;
}

.input-area button {
  padding: 0.5rem 1.2rem;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 9999px;
  font-weight: 600;
  cursor: pointer;
}

.input-area button[disabled] {
  background-color: #93c5fd;
  cursor: not-allowed;
}

.json-rule-container {
  background-color: #1e1e1e; // 深色背景
  color: #d4d4d4; // 主体字体颜色
  padding: 1rem;
  border-radius: 0.5rem;
  overflow-x: auto;
  max-height: 500px; // 限制最大高度，支持滚动
  font-family: 'Courier New', monospace;

  // 统一 vue-json-viewer 字体和颜色风格
  * {
    font-family: 'Courier New', monospace !important;
    color: #d4d4d4 !important;
  }

  // vue-json-viewer 容器背景透明，避免颜色叠加冲突
  .jv-container {
    background: transparent !important;
  }

  // JSON key 颜色
  .jv-key {
    color: #9cdcfe !important;
  }

  // JSON string 颜色
  .jv-string {
    color: #ce9178 !important;
  }

  // JSON number 颜色
  .jv-number {
    color: #b5cea8 !important;
  }

  // JSON boolean 颜色
  .jv-boolean {
    color: #569cd6 !important;
  }

  // JSON null 颜色
  .jv-null {
    color: #dcdcaa !important;
  }

  // 缩进条目样式调整（上下留白）
  .jv-item {
    padding: 2px 0;
  }

  // 拷贝按钮 hover 效果（可选）
  .jv-copy {
    color: #6b7280 !important;
  }

  .jv-copy:hover {
    color: #ffffff !important;
  }
}
</style>
