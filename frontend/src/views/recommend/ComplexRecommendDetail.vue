<template>
  <page-header-wrapper>
    <div class="main-container">
      <div class="content-wrapper">
        <!-- 左侧聊天区域 -->
        <div class="chat-wrapper">
          <div class="chat-container" ref="chatContainer">
            <div
              v-for="(msg, index) in chatHistory"
              :key="index"
              :class="['chat-message', msg.role]"
              v-if="msg.content.trim() !== ''"
            >
              <div class="message-content">
                <div class="message-bubble">{{ msg.content }}</div>
                <div v-if="msg.role === 'assistant' && !msg.isGenerated && msg.isSuccess" class="action-buttons">
                  <!-- 在聊天区域添加“大模型生成”按钮 -->
                  <button @click="generateComplexRule" class="generate-btn">大模型生成</button>
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
              class="input-box"
            />
            <button @click="sendMessage">发送</button>
          </div>

        </div>

        <!-- 右侧JSON规则展示区域 -->
        <div class="json-viewer">
          <div class="json-header">
            <h3>应用详情</h3>
            <div class="json-actions" v-if="latestJson">
              <button @click="submitRule" class="action-btn submit-btn">提交应用</button>
              <button class="action-btn regenerate-btn" @click="viewInNodeRed(latestJson)">
                在 Node-RED 中查看
              </button>
            </div>
          </div>
          <div class="json-content">
            <div v-if="latestJson" class="rule-section">
              <h4>JSON规则</h4>
              <div class="json-rule-container">
                <json-viewer
                  :value="latestJson"
                  :expand-depth="5"
                  copyable
                  boxed
                  theme="dark"
                />
              </div>
            </div>
            <div v-else class="empty-state">
              <p>请点击"大模型生成"按钮查看应用详情</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </page-header-wrapper>
</template>

<script setup>
/* eslint-disable */
import { ref, nextTick, computed } from 'vue'
import {
  generateComplexJsonRule,
  convertComplexJsonRule,
  generateComplexNaturalRule,
} from '@/api/manage'
import { v4 as uuidv4 } from 'uuid'
import { message } from 'ant-design-vue'

const uuid = uuidv4()
const inputContent = ref('')
const isLoading = ref(false)
const chatContainer = ref(null)
const selectedRule = ref(null)

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
  console.log("发送内容:", content); // 调试输出：显示发送的消息内容
  if (!content) return

  // 添加用户消息
  chatHistory.value.push({ role: 'user', content })

  const loadingIndex = chatHistory.value.length
  console.log("加载消息索引:", loadingIndex); // 调试输出：显示消息的索引
  chatHistory.value.push({
    role: 'assistant',
    content: '生成中...',
    jsonResult: null,
    isSuccess: false  // 初始时设置为false
  })

  inputContent.value = ''
  isLoading.value = true
  console.log("请求状态: 正在加载", isLoading.value); // 调试输出：检查 isLoading 的状态

  try {
    // 只发送消息，不生成 JSON 规则
    const res = await generateComplexNaturalRule(uuid, content)
    console.log("生成的响应:", res); // 调试输出：显示生成的响应内容

    chatHistory.value.splice(loadingIndex, 1, {
      role: 'assistant',
      content: res,
      jsonResult: null,
      isSuccess: true
    })
  } catch (error) {
    console.error("发送失败错误:", error); // 调试输出：显示错误信息
    chatHistory.value.splice(loadingIndex, 1, {
      role: 'assistant',
      content: '发送失败，请重试',
      jsonResult: { error: error.message },
      isSuccess: false
    })
  } finally {
    isLoading.value = false
    console.log("加载状态已解除:", isLoading.value); // 调试输出：检查 isLoading 的状态
    inputContent.value = ''
    await nextTick()
    if (chatContainer.value) {
      console.log("滚动到最新消息"); // 调试输出：显示滚动操作
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  }
}

async function generateComplexRule() {
  // 获取最后一条消息的内容
  const lastMessage = chatHistory.value[chatHistory.value.length - 1]
  console.log("获取的最后一条消息内容:", lastMessage.content); // 调试输出：显示最后一条消息内容
  const messageContent = lastMessage.content
  const hide = message.loading('大模型生成中...', 0)

  try {
    // 调用 API 生成复杂规则
    const jsonRes = await generateComplexJsonRule(uuid, messageContent)
    console.log("生成的 JSON 规则:", jsonRes); // 调试输出：显示生成的 JSON 规则
    latestJson.value = jsonRes  // 更新生成的 JSON 规则
    hide()
    message.success('大模型生成完成！')

    chatHistory.value.push({
      role: 'assistant',
      content: '对应json规则已生成',
      jsonResult: jsonRes,
      isSuccess: true,
      isGenerated: true
    })
    inputContent.value = '' // 清空输入框
    isLoading.value = false  // 解除加载状态
    console.log("加载状态已解除:", isLoading.value); // 调试输出：检查 isLoading 的状态
  } catch (error) {
    console.error("生成复杂规则失败错误:", error); // 调试输出：显示生成失败的错误信息
    latestJson.value = '生成复杂应用JSON规则失败: ' + error.message
    message.error('生成失败：' + error.message)
    isLoading.value = false  // 即使失败，也确保解除加载状态
  }
}

async function viewInNodeRed(json) {
  const hide = message.loading('正在推送至 Node-RED，请等待片刻...', 0)

  try {
    const flowJson = await convertComplexJsonRule(JSON.stringify(json))

    await fetch('http://127.0.0.1:1880/flows', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(flowJson)
    })

    hide()
    message.success('已成功推送至 Node-RED！')
    window.open('http://127.0.0.1:1880/', '_blank')
  } catch (error) {
    hide()
    message.error('推送失败，请稍后重试')
  }
}

</script>

<style lang="less" scoped>
.main-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 250px); /* 根据你的布局调整 */
}

.content-wrapper {
  display: flex;
  flex: 1;
  gap: 1rem;
  height: 100%;
}

.chat-wrapper {
  flex: 6;
  display: flex;
  flex-direction: column;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chat-container {
  flex: 1;
  padding: 1rem;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.chat-message {
  max-width: 70%;
  display: flex;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.chat-message.user {
  align-self: flex-end;
  text-align: right;
}

.chat-message.assistant {
  align-self: flex-start;
  text-align: left;
}

.message-bubble {
  padding: 0.5rem 1rem;
  border-radius: 1rem;
  font-size: 14px;
  line-height: 1.4;
}

.chat-message.user .message-bubble {
  background-color: #3b82f6;
  color: white;
  border-bottom-right-radius: 0;
}

.chat-message.assistant .message-bubble {
  background-color: #e5e7eb;
  color: #374151;
  border-bottom-left-radius: 0;
}

.input-area {
  display: flex;
  padding: 0.5rem 1rem;
  border-top: 1px solid #d1d5db;
  background: #f9fafb;
  align-items: center;
  gap: 0.5rem;
}

.input-area input {
  flex: 1;
  padding: 0.5rem 1rem;
  border-radius: 9999px;
  border: 1px solid #cbd5e1;
  font-size: 14px;
}

.input-area button {
  padding: 0.5rem 1.2rem;
  background-color: #3b82f6;
  color: white;
  border-radius: 9999px;
  border: none;
  font-weight: 600;
  cursor: pointer;
}

.input-area button[disabled] {
  background-color: #93c5fd;
  cursor: not-allowed;
}

/* JSON展示区域样式 */
.json-viewer {
  flex: 4;
  height: 100%;
  border: 1px solid #d1d5db;
  border-radius: 0.5rem;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
}

.json-header {
  padding: 1rem;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.json-header h3 {
  margin: 0;
  font-size: 1.1rem;
  color: #1f2937;
}

.json-actions {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  padding: 0.4rem 0.8rem;
  border-radius: 0.5rem;
  border: none;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s;
}

.submit-btn {
  background-color: #3b82f6;
  color: white;
}

.submit-btn:hover {
  background-color: #2563eb;
}

.regenerate-btn {
  background-color: #f59e0b;
  color: white;
}

.regenerate-btn:hover {
  background-color: #d97706;
}

.json-content {
  flex: 1;
  padding: 1rem;
  overflow-y: auto;
}

.rule-section {
  margin-bottom: 1.5rem;
}

.rule-section h4 {
  margin: 0 0 0.5rem 0;
  font-size: 0.9rem;
  color: #4b5563;
}

.empty-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  font-size: 0.9rem;
}

.json-rule-container {
  background-color: #1e1e1e;
  color: #d4d4d4;
  padding: 1rem;
  border-radius: 0.5rem;
  overflow-x: auto;
  font-family: 'Courier New', monospace;
}

.generate-btn {
  padding: 0.3rem 0.8rem;
  background-color: #3b82f6;
  color: white;
  border-radius: 0.5rem;
  border: none;
  font-size: 0.8rem;
  cursor: pointer;
  transition: background-color 0.2s;
}
</style>