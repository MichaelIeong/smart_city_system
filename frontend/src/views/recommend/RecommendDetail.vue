<template>
  <page-header-wrapper>
    <div class="main-container">
      <!-- 主内容区域，包含聊天和JSON展示 -->
      <div class="content-wrapper">
        <!-- 聊天区域 -->
        <div class="chat-wrapper">
          <div class="chat-container">
            <div
              v-for="(msg, index) in chatHistory"
              :key="index"
              :class="['chat-message', msg.role === 'user' ? 'user' : 'assistant']"
            >
              <div class="message-content">
                <div class="message-bubble" v-html="msg.content"></div>
                <div v-if="msg.role === 'assistant' && msg.isSuccess" class="action-buttons">
                  <button @click="findSimilarRule(index)" class="generate-btn">匹配已有应用</button>
                </div>
              </div>
            </div>
          </div>
          <!-- 输入框区域 -->
          <div class="input-area">
            <input
              type="text"
              v-model="inputContent"
              placeholder="请输入应用描述..."
              @keyup.enter="sendMessage"
              class="input-box"
            />
            <button @click="sendMessage" class="send-btn">发送</button>
          </div>
        </div>

        <!-- JSON规则展示区域 -->
        <div class="json-viewer">
          <div class="json-header">
            <h3>应用详情</h3>
            <div class="json-actions" v-if="selectedRule">
              <button @click="submitRule" class="action-btn submit-btn">提交应用</button>
              <button @click="regenerateRule" class="action-btn regenerate-btn">大模型生成</button>
            </div>
          </div>
          <div class="json-content">
            <div v-if="selectedRule" class="rule-section">
              <h4>自然语言描述</h4>
              <div class="rule-text">{{ selectedRule.content }}</div>
            </div>
            <div v-if="selectedRule && selectedRule.isSimilar" class="rule-section">
              <h4>匹配到的应用自然语言描述</h4>
              <div class="rule-text">{{ selectedRule.similarContent }}</div>
            </div>
            <div v-if="selectedRule" class="rule-section">
              <h4>JSON规则</h4>
              <div class="json-rule-container">
                <json-viewer
                  :value="selectedRule.jsonRule"
                  :expand-depth="5"
                  copyable
                  boxed
                  theme="dark"
                ></json-viewer>
              </div>
            </div>
            <div v-else class="empty-state">
              <p>请点击"匹配已有应用"按钮查看应用详情</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </page-header-wrapper>
</template>

<script setup>
/* eslint-disable */
import { ref } from 'vue'
import { generateNaturalRule, generateJsonRule, findSimilarRules, createTapRule } from '@/api/manage'
import { v4 as uuidv4 } from 'uuid'
import { message } from 'ant-design-vue'
const uuid = uuidv4()
const chatHistory = ref([
  { role: 'assistant', content: '您好，我是一个生成自然语言描述SCTAP应用的智能助手！', isSuccess: false }
])
const inputContent = ref('')
const isLoading = ref(false)
const selectedRule = ref(null)

async function sendMessage() {
  const content = inputContent.value.trim()
  if (!content) return
  // 添加用户消息
  chatHistory.value.push({ role: 'user', content })
  inputContent.value = ''
  // 添加助手响应
  isLoading.value = true
  const loadingIndex = chatHistory.value.length
  chatHistory.value.push({ role: 'assistant', content: '生成中...', isSuccess: false })
  try {
    const res = await generateNaturalRule(uuid, content )
    chatHistory.value.splice(loadingIndex, 1, {
      role: 'assistant',
      content: res,
      isSuccess: true
    })
  } catch (error) {
    chatHistory.value.splice(loadingIndex, 1, {
      role: 'assistant',
      content: '生成失败，请重新输入',
      isSuccess: false
    })
  } finally {
    isLoading.value = false
  }
}

async function findSimilarRule(index) {
  const message = chatHistory.value[index]
  selectedRule.value = {
    content: message.content,
    jsonRule: '正在匹配应用JSON规则...',
    index: index,
    isSimilar: true,
    similarContent: '正在匹配应用...'
  }
  try {
    const jsonRes = await findSimilarRules(message.content)
    selectedRule.value.similarContent = jsonRes.description
    selectedRule.value.jsonRule = JSON.parse(jsonRes.ruleJson);
  } catch (error) {
    selectedRule.value.jsonRule = 'JSON规则生成失败: ' + error.message
  }
}

async function regenerateRule() {
  if (selectedRule.value) {
    try {
        selectedRule.value.jsonRule = '正在生成应用JSON规则...'
        selectedRule.value.isSimilar = false
        const jsonRule = await generateJsonRule(uuid, selectedRule.value.content )
        selectedRule.value.jsonRule = jsonRule;
    } catch (error) {
        selectedRule.value.jsonRule = 'JSON规则生成失败: ' + error.message
    }
  }
}

async function submitRule() {
  if (selectedRule.value && selectedRule.value.jsonRule) {
    try {
        const projectId = localStorage.getItem('project_id')
        await createTapRule(projectId, selectedRule.value.content, JSON.stringify(selectedRule.value.jsonRule, null, 2))
        message.success('规则创建成功')
    } catch (error) {
        message.error('规则创建失败: ' + error.message)
    }
  }
}
</script>

<style lang="less" scoped>
.main-container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 250px); // 根据你的布局调整
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
  height: 100%;
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

.action-buttons {
  margin-top: 0.5rem;
  display: flex;
  gap: 0.5rem;
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

.generate-btn:hover {
  background-color: #059669;
}

.input-area {
  display: flex;
  padding: 0.5rem 1rem;
  border-top: 1px solid #d1d5db;
  background: #f9fafb;
  align-items: center;
  gap: 0.5rem;
}

.input-box {
  flex: 1;
  padding: 0.5rem 1rem;
  border-radius: 9999px;
  border: 1px solid #cbd5e1;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s ease-in-out;
}

.input-box:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.3);
}

.send-btn {
  padding: 0.5rem 1.2rem;
  background-color: #3b82f6;
  color: white;
  border-radius: 9999px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  user-select: none;
  transition: background-color 0.2s ease-in-out;
}

.send-btn:hover {
  background-color: #2563eb;
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

.rule-text {
  padding: 0.8rem;
  background-color: #f9fafb;
  border-radius: 0.5rem;
  border: 1px solid #e5e7eb;
  font-size: 0.9rem;
  line-height: 1.5;
  white-space: pre-wrap;
}

.rule-json {
  padding: 0.8rem;
  background-color: #1e1e1e;
  color: #d4d4d4;
  border-radius: 0.5rem;
  font-family: 'Courier New', monospace;
  font-size: 0.85rem;
  line-height: 1.5;
  white-space: pre-wrap;
  overflow-x: auto;
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

  /* 可选：添加一些内边距或间距 */
  .vjs-value {
    word-break: break-all;
  }
}
</style>