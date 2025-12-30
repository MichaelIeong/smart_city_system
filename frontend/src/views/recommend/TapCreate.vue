<template>
  <page-header-wrapper>
    <a-card :bordered="false" :style="{ borderRadius: '8px', height: '75vh'}">
      <div class="top-buttons">
        <button class="top-btn" type="primary" @click="openNodeRED">打开Node-RED</button>
        <button class="top-btn" type="primary" @click="handleOpenManualCreate">创建应用</button>
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
                  <div class="message-bubble" v-html="msg.content"></div>
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
      <a-modal :visible="isGridSelectionModalVisible" title="同层次类型的全部网格列表" @cancel="closeGridSelectionModal" :width="800">
        <div>
          <a-table
            :columns="gridColumns"
            :dataSource="gridList"
            :rowKey="record => record.id"
            :rowSelection="gridSyncRowSelection"
          >
          </a-table>
        </div>
        <template v-slot:footer>
          <a-button @click="handleSyncConfirm" type="primary">同步下发</a-button>
          <a-button @click="closeGridSelectionModal">取消</a-button>
        </template>
      </a-modal>
      <a-modal :visible="isSyncResultModalVisible" title="同步结果" @cancel="closeSyncResultModal" :width="800" :footer="null">
        <div>
          <a-table
            :columns="syncResultColumns"
            :dataSource="gridSyncResults"
            :rowKey="record => record.gridId"
          >
            <template slot="isSuccessSlot" slot-scope="text">
              <span :style="{ color: text === 1 ? 'green' : 'red' }">
                {{ text === 1 ? '成功' : '失败' }}
              </span>
            </template>
          </a-table>
        </div>
      </a-modal>
      <a-modal
        :visible="isManualSaveModalVisible"
        title="创建应用"
        :confirm-loading="isManualSaveLoading"
        @cancel="closeManualSaveModal"
        destroy-on-close
      >
        <a-form
          ref="manualSaveFormRef"
          :model="manualSaveForm"
          layout="vertical"
        >
          <a-form-item label="应用描述" name="description">
            <a-textarea
              :rows="4"
              v-model="manualSaveForm.description"
              placeholder="请输入应用的简要描述，不能超过 300 字"
              allow-clear
              :max-length="300"
            />
            <span
              style="
                position: absolute;
                right: 8px;
                bottom: -18px;
                font-size: 12px;
                color: #999;
              "
            >
              {{ manualSaveForm.description.length }} / 300
            </span>
          </a-form-item>
          <a-form-item label="Node-RED 导出 JSON" name="flowJson">
            <a-textarea
              v-model="manualSaveForm.flowJson"
              placeholder="请将 Node-RED 导出的 JSON 粘贴到这里"
              :rows="8"
              allow-clear
            />
          </a-form-item>
        </a-form>
        <template slot="footer">
          <a-button @click="closeManualSaveModal">取消</a-button>
          <a-button type="primary" :loading="isManualSaveLoading" @click="handleManualSaveApp">保存</a-button>
        </template>
      </a-modal>
    </a-card>
  </page-header-wrapper>
</template>

<script setup>
/* eslint-disable */
import { ref, onMounted, computed, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { v4 as uuidv4 } from 'uuid'

// 导入所有 API 函数
import {
  generateNaturalRule,
  generateJsonRule,
  findSimilarRules,
  createTapRule,
  convertJsonRule,
  getGridById,
  getGridListByType,
  syncAppRule
} from '@/api/manage'

// --- 1. 常量和配置 ---
const UNIQUE_CHAT_ID = uuidv4()
const NODE_RED_URL = process.env.VUE_APP_NODE_RED_URL
const MAX_DESCRIPTION_LENGTH = 300

// --- 2. 响应式状态：聊天与规则详情 ---
const chatHistory = ref([
  { role: 'assistant', content: '您好，我是您的智能助手，请描述您的应用需求！', isSuccess: false },
])
const inputContent = ref('')
const isChatLoading = ref(false)
const ruleDetails = ref(null) // 对应原 selectedRule

// --- 3. 响应式状态：网格/应用管理与同步 ---
const gridId = ref(null)
const appId = ref(null)

// 同步网格列表模态框 (原 isModalVisible)
const isGridSelectionModalVisible = ref(false)
const gridList = ref([])
const selectedSyncGridKeys = ref([]) // 对应原 selectedRowKeys
const gridColumns = [ // 对应原 columns
  { title: '网格编号', dataIndex: 'meshNo', key: 'meshNo' },
  { title: '网格名称', dataIndex: 'meshName', key: 'meshName' },
  { title: '网格层次', dataIndex: 'meshNature', key: 'meshNature' },
  { title: '网格类型', dataIndex: 'meshType', key: 'meshType' }
]

// 同步结果模态框 (原 isResultModalVisible)
const isSyncResultModalVisible = ref(false)
const gridSyncResults = ref([]) // 对应原 gridResults
const syncResultColumns = [ // 对应原 resultColumns
  { title: '网格编号', dataIndex: 'meshNo', key: 'meshNo' },
  { title: '网格名称', dataIndex: 'meshName', key: 'meshName' },
  { title: '是否成功', dataIndex: 'isSuccess', key: 'isSuccess', scopedSlots: { customRender: 'isSuccessSlot' }},
  { title: '消息', dataIndex: 'message', key: 'message' }
]

// --- 4. 响应式状态：手动创建应用模态框 (原 saveXXX) ---
const isManualSaveModalVisible = ref(false) // 对应原 saveVisible
const isManualSaveLoading = ref(false) // 对应原 saveLoading
const manualSaveFormRef = ref(null) // 对应原 saveFormRef
const manualSaveForm = reactive({ // 对应原 saveForm
  description: '',
  flowJson: ''
})

// --- 5. 生命周期钩子 ---
onMounted(() => {
  initGridContext() // 对应原 handleGridSelection
})

// --- 6. 核心逻辑函数：网格与初始化 ---
/** 处理网格选择逻辑 (从 URL 获取) */
async function initGridContext() {
  const gridIdFromUrl = new URLSearchParams(window.location.search).get('gridId')
  if (gridIdFromUrl) {
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
    window.open(`${NODE_RED_URL}?gridId=${gridId.value}`, '_blank')
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

  try {
    const projectId = localStorage.getItem('project_id')
    const jsonRuleString = JSON.stringify(ruleDetails.value.jsonRule, null, 2)

    const appIdNew = await createTapRule(
      projectId,
      ruleDetails.value.naturalContent,
      jsonRuleString,
      "", // flowJson 留空
      gridId.value
    )

    if (appIdNew !== 0) {
      appId.value = appIdNew
      message.success('应用创建成功')
      
      // 准备同步
      const gridListData = await getGridListByType(gridId.value)
      gridList.value = gridListData
      isGridSelectionModalVisible.value = true // 显示网格选择弹窗
    } else {
      message.error('应用创建失败，请稍后重试')
    }
  } catch (error) {
    message.error('应用创建失败: ' + error.message)
  }
}

/** 打开手动创建应用的模态框 (对应原 handleCreate) */
function handleOpenManualCreate () {
  isManualSaveModalVisible.value = true
}

/** 关闭手动创建应用的模态框 (对应原 closeSave) */
function closeManualSaveModal () {
  isManualSaveModalVisible.value = false
  // 重置表单
  manualSaveForm.description = ''
  manualSaveForm.flowJson = ''
}

/** 手动创建应用 (对应原 submitSave) */
async function handleManualSaveApp () {
  if (!gridId.value) return message.warning('请先选择网格。')

  try {
    isManualSaveLoading.value = true
    
    // --- 手动校验 ---
    if (!manualSaveForm.description || manualSaveForm.description.trim() === '') {
      return message.error('请输入应用描述')
    }
    if (manualSaveForm.description.length > MAX_DESCRIPTION_LENGTH) {
      return message.error(`描述不能超过 ${MAX_DESCRIPTION_LENGTH} 个字符`)
    }
    if (!manualSaveForm.flowJson || manualSaveForm.flowJson.trim() === '') {
      return message.error('请粘贴 Node-RED 导出的 JSON')
    }
    let flowJsonObj
    try {
      flowJsonObj = JSON.parse(manualSaveForm.flowJson)
    } catch (e) {
      return message.error('Node-RED JSON 格式不正确，请检查后再试')
    }
    
    const hide = message.loading('正在创建应用，请稍等片刻...', 0)
    
    try {
      const projectId = localStorage.getItem('project_id')
      const appIdNew = await createTapRule(
        projectId, 
        manualSaveForm.description, 
        "", // jsonRule 留空
        JSON.stringify(flowJsonObj, null, 2), 
        gridId.value
      )

      if (appIdNew !== 0) {
        hide()
        message.success('应用创建成功')
        appId.value = appIdNew
        closeManualSaveModal()
        
        // 获取同类型网格数据并准备同步
        const gridListData = await getGridListByType(gridId.value)
        gridList.value = gridListData
        isGridSelectionModalVisible.value = true
      } else {
        hide()
        message.error('应用创建失败，请稍后重试')
      }
    } catch (error) {
      hide()
      message.error('应用创建失败: ' + error.message)
    }
  } finally {
    isManualSaveLoading.value = false
  }
}


// --- 9. 核心逻辑函数：Node-RED 交互 ---
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

    // 2. 推送到 Node-RED
    await fetch(`${NODE_RED_URL}/flows`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(flowJson)
    })

    hide()
    message.success('已成功推送至 Node-RED！')
    window.open(`${NODE_RED_URL}?gridId=${gridId.value}`, '_blank')
  } catch (error) {
    hide()
    message.error('推送失败，请稍后重试: ' + error.message)
  }
}

// --- 10. 核心逻辑函数：网格同步 (Modal Actions) ---
/** 关闭网格选择弹窗 (对应原 closeModal) */
function closeGridSelectionModal() {
  isGridSelectionModalVisible.value = false
  selectedSyncGridKeys.value = []
}

/** 关闭同步结果弹窗 (对应原 closeResultModal) */
function closeSyncResultModal() {
  isSyncResultModalVisible.value = false
  gridSyncResults.value = []
}

/** 确认同步下发应用规则 (对应原 handleConfirm) */
async function handleSyncConfirm() {
  if (!appId.value) return message.error('应用 ID 丢失。')
  if (selectedSyncGridKeys.value.length === 0) return message.warning('请选择至少一个网格进行同步。')

  try {
    const result = await syncAppRule(appId.value, selectedSyncGridKeys.value)
    gridSyncResults.value = result
    isSyncResultModalVisible.value = true
  } catch (error) {
    message.error('同步失败: ' + error.message)
  } finally {
    closeGridSelectionModal(); // 关闭选择弹窗
  }
}

// --- 11. Computed/Modal 配置 ---
/** 网格同步表格的 rowSelection 配置 (对应原 rowSelection) */
const gridSyncRowSelection = computed(() => ({
  type: 'checkbox',
  selectedRowKeys: selectedSyncGridKeys.value,
  onChange: (selectedKeys) => {
    selectedSyncGridKeys.value = selectedKeys
  }
}))
</script>

<style lang="less" scoped>
// 样式保持原样，以确保布局和外观不变
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
  gap: 0.2rem;
}

.chat-message {
  max-width: 70%;
  display: flex;
  word-wrap: break-word;
  white-space: pre-wrap;
  margin-bottom: 2px;
}

.chat-message.user {
  align-self: flex-end;
  text-align: left;
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
  margin-top: 0.2rem; 	 
  display: flex;
  gap: 0.2rem; 			 
}

.match-btn {
  padding: 0.2rem 0.6rem; 
  background-color: #3b82f6;
  color: white;
  border-radius: 0.4rem; 
  font-size: 0.7rem; 	 
  cursor: pointer;
  transition: background-color 0.3s, box-shadow 0.3s;
  border: none; 		 
}

.match-btn:hover {
  background-color: #059669;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2); 
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
  background-color: #059669;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2);
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

.json-content {
  flex: 1;
  padding: 0.5rem;
  overflow-y: auto;
}

.rule-section {
  margin-bottom: 0.75rem;
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
  font-size: 14px;
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
  ::v-deep .vjs-value {
    word-break: break-all;
  }
}

.submit-btn {
  padding: 0.2rem 0.6rem; 
  background-color: #3b82f6;
  color: white;
  border-radius: 0.4rem; 
  font-size: 0.7rem; 	 
  cursor: pointer;
  transition: background-color 0.3s, box-shadow 0.3s;
  border: none; 		 
}

.submit-btn:hover {
  background-color: #059669;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2); 
}

.llm-btn {
  padding: 0.2rem 0.6rem; 
  background-color: #3b82f6;
  color: white;
  border-radius: 0.4rem; 
  font-size: 0.7rem; 	 
  cursor: pointer;
  transition: background-color 0.3s, box-shadow 0.3s;
  border: none; 		 
}

.llm-btn:hover {
  background-color: #059669;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2); 
}

.nodered-btn {
  padding: 0.2rem 0.6rem; 
  background-color: #3b82f6;
  color: white;
  border-radius: 0.4rem; 
  font-size: 0.7rem; 	 
  cursor: pointer;
  transition: background-color 0.3s, box-shadow 0.3s;
  border: none; 		 
}

.nodered-btn:hover {
  background-color: #059669;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2); 
}

.top-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-bottom: 4px;
  margin-right: 1rem;
}

.top-btn {
  padding: 0.2rem 0.6rem;
  background-color: #3b82f6;
  color: white;
  border-radius: 0.4rem;
  font-size: 0.7rem;
  cursor: pointer;
  transition: background-color 0.3s box-shadow 0.3s;
  border: none;
}

.top-btn:hover {
  background-color: #059669;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2); 
}

::v-deep .ant-card-body {
  padding: 4px 8px; 
}
</style>