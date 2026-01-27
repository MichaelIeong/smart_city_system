<template>
  <page-header-wrapper>
    <a-card :bordered="false" :style="{ borderRadius: '8px', height: '75vh'}">
      <div class="top-buttons">
        <button class="top-btn" type="primary" @click="openNodeRED">打开Node-RED</button>
      </div>
      <div class="main-container">
        <div class="content-wrapper">
          <div class="chat-wrapper">
            <div class="chat-container">
              <div
                v-for="(msg, index) in chatHistory"
                :key="index"
                :class="['chat-message', msg.role === 'user' ? 'user' : 'assistant']"
              >
                <div class="message-content">
                  <div class="message-bubble" v-html="renderMarkdown(msg.content)"></div>
                  <div v-if="msg.role === 'assistant' && msg.isSuccess" class="action-buttons">
                    <button @click="handleFindSimilarRule(index)" class="match-btn">匹配已有应用</button>
                  </div>
                </div>
              </div>
            </div>
            <div class="input-area">
              <input
                type="text"
                v-model="inputContent"
                placeholder="请输入应用需求描述..."
                @keyup.enter="handleSendMessage"
                class="input-box"
                :disabled="isChatLoading"
              />
              <button @click="handleSendMessage" class="send-btn" :disabled="isChatLoading">
                {{ isChatLoading ? '发送中...' : '发送' }}
              </button>
            </div>
          </div>
          <div class="json-viewer">
            <div class="json-header">
              <h3>应用详情</h3>
              <div class="json-actions" v-if="ruleDetails">
                <button @click="handleCreateAppFromChat" class="submit-btn">提交应用</button>
                <button @click="handleGenerateLLMRule" class="llm-btn">大模型生成</button>
                <button @click="handleViewInNodeRed" class="nodered-btn">在 Node-RED 中查看</button>
              </div>
            </div>
            <div class="json-content">
              <div v-if="ruleDetails" class="rule-section">
                <h4>应用描述</h4>
                <div class="rule-text">{{ ruleDetails.naturalContent }}</div>
              </div>
              <div v-if="ruleDetails && ruleDetails.isSimilar" class="rule-section">
                <h4>匹配到的应用描述</h4>
                <div class="rule-text">{{ ruleDetails.similarNaturalContent }}</div>
              </div>
              <div v-if="ruleDetails" class="rule-section">
                <h4>应用JSON</h4>
                <div class="json-rule-container">
                  <json-viewer
                    :value="ruleDetails.jsonRule"
                    :expand-depth="10"
                    copyable
                    boxed
                    theme="dark"
                  ></json-viewer>
                </div>
              </div>
              <div v-else class="empty-state">
                <p>请点击"匹配已有应用"按钮查看详情</p>
              </div>
            </div>
          </div>
        </div>
      </div>
      <a-modal
        :visible="isAppNameModalVisible"
        title="请输入应用名称"
        @ok="handleConfirmCreateApp"
        @cancel="closeAppNameModal"
        :confirm-loading="isCreateAppLoading"
        destroy-on-close
      >
        <a-form layout="vertical">
          <a-form-item label="应用名称" required>
            <a-input
              v-model="appNameInput"
              placeholder="请输入应用名称，不能超过 30 字"
              allow-clear
              :max-length="30"
            />
            <div
              style="
                margin-top: 4px;
                font-size: 12px;
                color: #999;
                text-align: right;
              "
            >
              {{ appNameInput.length }} / 30
            </div>
          </a-form-item>
        </a-form>
      </a-modal>
    </a-card>
  </page-header-wrapper>
</template>

<script setup>
/* eslint-disable */
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { v4 as uuidv4 } from 'uuid'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'

// 导入所有 API 函数
import {
  generateNaturalRule,
  generateJsonRule,
  findSimilarRules,
  createTapRule,
  convertJsonRule,
  getGridById
} from '@/api/manage'

// --- 1. 常量和配置 ---
const UNIQUE_CHAT_ID = uuidv4()
const NODE_RED_URL = process.env.VUE_APP_NODE_RED_URL
const MAX_APP_NAME_LENGTH = 30

// --- 2. 响应式状态：聊天与规则详情 ---
const chatHistory = ref([
  { role: 'assistant', content: '您好，我是您的智能助手，请描述您的应用需求！', isSuccess: false },
])
const inputContent = ref('')
const isChatLoading = ref(false)
const ruleDetails = ref(null) // 对应原 selectedRule

// --- 3. 响应式状态：网格/应用管理与同步 ---
const gridId = ref(null)

// —— 创建应用名称弹窗 ——
const isAppNameModalVisible = ref(false)
const isCreateAppLoading = ref(false)
const appNameInput = ref('')

// --- 5. 生命周期钩子 ---
onMounted(() => {
  initGridContext() // 对应原 handleGridSelection
})

// --- 6. 核心逻辑函数：网格与初始化 ---
/** 处理网格选择逻辑 (从 URL 获取) */
async function initGridContext() {
  const gridIdFromUrl = new URLSearchParams(window.location.search).get('gridId')
  if (gridIdFromUrl === "crossRegion") {
    gridId.value = gridIdFromUrl
    chatHistory.value.push({
        role: 'assistant',
        content: "您已选择**全局网格**。",
        isSuccess: false,
      })
  }else if (gridIdFromUrl) {
    try {
      gridId.value = gridIdFromUrl
      const grid = await getGridById(gridId.value)
      chatHistory.value.push({
        role: 'assistant',
        content: `您已选择网格: **${grid.meshName}**。`,
        isSuccess: false,
      })
    } catch (error) {
      chatHistory.value.push({
        role: 'assistant',
        content: '获取网格数据失败，请稍后重试。',
        isSuccess: false,
      })
    }
  } else {
    chatHistory.value.push({
      role: 'assistant',
      content: '您还未选择网格，请选择一个网格。',
      isSuccess: false,
    })
  }
}

/** 打开 Node-RED 界面 */
function openNodeRED() {
  if (!gridId.value) {
    message.warning('请先选择网格')
  } else {
    const projectId = localStorage.getItem('project_id')
    window.open(`${NODE_RED_URL}?type=1&gridId=${gridId.value}&projectId=${projectId}`, '_blank')
  }
}

// --- 7. 核心逻辑函数：聊天与规则生成 ---
/** 发送用户消息并触发自然语言规则生成 (对应原 sendMessage) */
async function handleSendMessage() {
  const content = inputContent.value.trim()
  if (!content) return

  // 1. 添加用户消息
  chatHistory.value.push({ role: 'user', content })
  inputContent.value = ''

  // 2. 添加助手加载消息
  isChatLoading.value = true
  const loadingIndex = chatHistory.value.length
  chatHistory.value.push({ role: 'assistant', content: '正在生成自然语言规则...', isSuccess: false })

  try {
    const res = await generateNaturalRule(UNIQUE_CHAT_ID, content, gridId.value)
    chatHistory.value.splice(loadingIndex, 1, {
      role: 'assistant',
      content: res,
      isSuccess: true
    })
  } catch (error) {
    chatHistory.value.splice(loadingIndex, 1, {
      role: 'assistant',
      content: '自然语言规则生成失败，请重新输入',
      isSuccess: false
    })
  } finally {
    isChatLoading.value = false
  }
}

/** 匹配已有应用规则并显示详情 (对应原 findSimilarRule) */
async function handleFindSimilarRule(index) {
  const message = chatHistory.value[index]
  ruleDetails.value = {
    naturalContent: message.content,
    jsonRule: '正在匹配应用JSON...',
    index: index,
    isSimilar: true,
    similarNaturalContent: '正在匹配应用...'
  }
  try {
    const jsonRes = await findSimilarRules(message.content)
    ruleDetails.value.similarNaturalContent = jsonRes.description
    try {
      ruleDetails.value.jsonRule = JSON.parse(jsonRes.ruleJson);
    } catch (e) {
      ruleDetails.value.jsonRule = `匹配到的 JSON 格式错误：${e.message}`;
    }
  } catch (error) {
    ruleDetails.value.jsonRule = '匹配失败: ' + error.message
    ruleDetails.value.similarNaturalContent = '匹配失败'
  }
}

/** 触发大模型生成 JSON 规则 (对应原 generateRule) */
async function handleGenerateLLMRule() {
  if (ruleDetails.value) {
    try {
      ruleDetails.value.jsonRule = '正在生成应用JSON...'
      ruleDetails.value.isSimilar = false
      const jsonRule = await generateJsonRule(UNIQUE_CHAT_ID, ruleDetails.value.naturalContent, gridId.value)
      ruleDetails.value.jsonRule = jsonRule;
    } catch (error) {
      ruleDetails.value.jsonRule = 'JSON规则生成失败: ' + error.message
    }
  }
}

// --- 8. 核心逻辑函数：应用创建与提交 ---
/** 从右侧规则详情创建应用 (对应原 submitRule) */
async function handleCreateAppFromChat() {
  if (!ruleDetails.value || !ruleDetails.value.jsonRule || typeof ruleDetails.value.jsonRule !== 'object') {
    return message.warning('请先匹配或生成正确的 JSON 应用规则。')
  }
  if (!gridId.value) return message.warning('请先选择网格。')
  // 重置名称并打开弹窗
  appNameInput.value = ''
  isAppNameModalVisible.value = true
}

async function handleConfirmCreateApp() {
  const appName = appNameInput.value.trim()

  if (!appName) {
    return message.error('请输入应用名称')
  }
  if (appName.length > MAX_APP_NAME_LENGTH) {
    return message.error(`应用名称不能超过 ${MAX_APP_NAME_LENGTH} 个字符`)
  }

  try {
    isCreateAppLoading.value = true

    const projectId = localStorage.getItem('project_id')
    const jsonRuleString = JSON.stringify(
      ruleDetails.value.jsonRule,
      null,
      2
    )
    const appIdNew = await createTapRule(
      projectId,                      
      ruleDetails.value.naturalContent,
      jsonRuleString,
      "",
      gridId.value,
      appName
    )

    if (appIdNew !== 0) {
      isAppNameModalVisible.value = false
      message.success('应用创建成功')
    } else {
      message.error('应用创建失败')
    }
  } catch (e) {
    message.error('应用创建失败：' + e.message)
  } finally {
    isCreateAppLoading.value = false
  }
}

function closeAppNameModal() {
  isAppNameModalVisible.value = false
  appNameInput.value = ''
}

/** 将 JSON 规则推送到 Node-RED 并打开编辑器 (对应原 viewInNodeRed) */
async function handleViewInNodeRed() {
  if (!ruleDetails.value || !ruleDetails.value.jsonRule || typeof ruleDetails.value.jsonRule !== 'object') {
    return message.warning('请先确保右侧有正确的 JSON 规则。')
  }

  const hide = message.loading('正在推送至 Node-RED，请稍等片刻...', 0)
  try {
    // 1. 将规则 JSON 转换为 Node-RED Flow JSON
    const ruleJsonString = JSON.stringify(ruleDetails.value.jsonRule)
    const flowJson = await convertJsonRule(ruleJsonString)
    const projectId = localStorage.getItem('project_id')

    // 2. 推送到 Node-RED
    await fetch(`${NODE_RED_URL}/flows`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(flowJson)
    })

    hide()
    message.success('已成功推送至 Node-RED！')
    window.open(`${NODE_RED_URL}?type=1&gridId=${gridId.value}&projectId=${projectId}`, '_blank')
  } catch (error) {
    hide()
    message.error('推送失败，请稍后重试: ' + error.message)
  }
}

// 初始化 markdown-it
const md = new MarkdownIt({
  html: true,        // 允许 HTML 标签
  linkify: true,     // 自动链接
  typographer: true,
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' +
               hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
               '</code></pre>';
      } catch (__) {}
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>';
  }
})

/** 封装渲染函数 */
function renderMarkdown(content) {
  if (!content) return ''
  return md.render(content)
}
</script>

<style lang="less" scoped>
/* --- 基础布局 --- */
.main-container {
  display: flex;
  flex-direction: column;
  height: 70vh;
}

.content-wrapper {
  display: flex;
  flex: 1;
  gap: 1rem;
  height: 100%;
}

/* --- 聊天区域 --- */
.chat-wrapper {
  flex: 5;
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
  gap: 0.2rem; /* 增大消息条目之间的间距 */
}

.chat-message {
  max-width: 85%; /* 稍微加宽，适合展示长列表 */
  display: flex;
  margin-bottom: 2px;
}

/* 移除 chat-message 上的 white-space: pre-wrap，由渲染后的 MD 标签自行处理 */
.chat-message.user {
  align-self: flex-end;
}

.chat-message.assistant {
  align-self: flex-start;
}

/* --- 消息气泡（核心修改区） --- */
.message-bubble {
  padding: 0.5rem 1rem;
  border-radius: 1rem;
  font-size: 14px;
  line-height: 1.6; /* 优化行高 */
  word-wrap: break-word;
  white-space: normal; /* 关键：重置为 normal，由 MD 的 <p> 标签控制换行 */

  /* 消除 Markdown 产生的段落默认边距 */
  :deep(p) {
    margin: 0 0 8px 0; 
    &:last-child {
      margin-bottom: 0;
    }
  }

  /* 优化列表展示 */
  :deep(ul), :deep(ol) {
    margin: 4px 0 8px 0;
    padding-left: 1.4rem;
  }

  :deep(li) {
    margin-bottom: 4px;
    &:last-child {
      margin-bottom: 0;
    }
  }

  /* 优化标题展示 */
  :deep(h1), :deep(h2), :deep(h3), :deep(h4) {
    margin: 12px 0 6px 0;
    font-size: 15px;
    font-weight: bold;
    line-height: 1.4;
  }

  /* 行内代码 */
  :deep(code) {
    background-color: rgba(0, 0, 0, 0.06);
    padding: 2px 4px;
    border-radius: 4px;
    font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
    font-size: 0.9em;
  }

  /* 代码块 */
  :deep(pre) {
    background-color: #1e1e1e;
    padding: 0.8rem;
    border-radius: 0.5rem;
    overflow-x: auto;
    margin: 8px 0;
    
    code {
      background-color: transparent;
      padding: 0;
      color: #d4d4d4;
      font-size: 13px;
    }
  }

  /* 强引用/加粗 */
  :deep(strong) {
    font-weight: 600;
    color: inherit;
  }
}

/* 颜色适配 */
.chat-message.user .message-bubble {
  background-color: #3b82f6;
  color: white;
  border-bottom-right-radius: 0;
  
  :deep(code) {
    background-color: rgba(255, 255, 255, 0.2);
    color: #fff;
  }
}

.chat-message.assistant .message-bubble {
  background-color: #e5e7eb; /* 稍浅的灰色 */
  color: #374151;
  border-bottom-left-radius: 0;
}

/* --- 底部输入区 --- */
.input-area {
  display: flex;
  padding: 0.8rem 1rem;
  border-top: 1px solid #e5e7eb;
  background: #fff;
  align-items: center;
  gap: 0.6rem;
}

.input-box {
  flex: 1;
  padding: 0.6rem 1.2rem;
  border-radius: 9999px;
  border: 1px solid #d1d5db;
  font-size: 14px;
  outline: none;
  transition: all 0.2s ease;
}

.input-box:focus {
  border-color: #3b82f6;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.send-btn {
  padding: 0.5rem 1.4rem;
  background-color: #3b82f6;
  color: white;
  border-radius: 9999px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.send-btn:hover:not(:disabled) {
  background-color: #2563eb;
}

.send-btn:disabled {
  background-color: #93c5fd;
  cursor: not-allowed;
}

/* --- JSON展示区域（右侧） --- */
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
  font-weight: 600;
}

.json-actions {
  display: flex;
  gap: 0.5rem;
}

.json-content {
  flex: 1;
  padding: 1rem;
  overflow-y: auto;
}

.rule-section {
  margin-bottom: 1.2rem;
}

.rule-section h4 {
  margin-bottom: 0.5rem;
  font-size: 14px;
  font-weight: 600;
  color: #4b5563;
}

.rule-text {
  padding: 0.8rem;
  background-color: #f9fafb;
  border-radius: 0.5rem;
  border: 1px solid #e5e7eb;
  font-size: 13.5px;
  line-height: 1.6;
}

.json-rule-container {
  background-color: #1e1e1e;
  color: #d4d4d4;
  padding: 0.8rem;
  border-radius: 0.5rem;
  :deep(.vjs-value) { word-break: break-all; }
}

/* --- 按钮样式 --- */
.match-btn, .submit-btn, .llm-btn, .nodered-btn, .top-btn {
  padding: 4px 12px;
  background-color: #3b82f6;
  color: white;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.match-btn:hover, .submit-btn:hover, .llm-btn:hover, .nodered-btn:hover, .top-btn:hover {
  background-color: #2563eb;
  transform: translateY(-1px);
}

.action-buttons {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.top-buttons {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
  padding-right: 1rem;
}

.empty-state {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
}

::v-deep .ant-card-body {
  padding: 8px 12px; 
}
</style>