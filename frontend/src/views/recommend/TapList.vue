<template>
  <page-header-wrapper>
    <a-card :bordered="false" :style="{ borderRadius: '8px', height: 'calc(100vh - 250px)' }">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="6" :sm="24">
              <a-form-item label="事件类型">
                <a-select
                  v-model="searchParams.eventType"
                  placeholder="请选择触发事件"
                  option-filter-prop="children"
                  allow-clear
                >
                  <a-select-option
                    v-for="item in eventOptions"
                    :key="item.value"
                    :value="item.value"
                  >
                    {{ item.label }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>

            <a-col :md="6" :sm="24">
              <a-form-item label="应用描述">
                <a-input v-model="searchParams.description" placeholder="请输入" allow-clear />
              </a-form-item>
            </a-col>

            <a-col :md="12" :sm="24">
              <span>
                <a-button style="margin-left: 20px" type="primary" @click="doSearch">搜索</a-button>
                <a-button style="margin-left: 10px" @click="handleReset">重置</a-button>
                <a-button style="margin-left: 10px" type="dashed" @click="handleBuild">应用构造</a-button>
                <a-button style="margin-left: 10px" type="dashed" @click="handleRecommend">应用推荐</a-button>
                <a-button style="margin-left: 10px" type="dashed" @click="handleCreate">应用创建</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <s-table
        ref="tableRef"
        size="default"
        rowKey="id"
        :columns="columns"
        :data="loadData"
        showPagination="auto"
      >
        <span slot="description" slot-scope="text">
          <ellipsis :length="15" tooltip>{{ text }}</ellipsis>
        </span>

        <span slot="enabled" slot-scope="enabled">
          <a-tag :color="enabled ? 'green' : 'red'">{{ enabled ? '启用中' : '已禁用' }}</a-tag>
        </span>

        <span slot="action" slot-scope="text, record">
          <template>
            <a @click="handleEdit(record)">编辑</a>
            <a-divider type="vertical"/>
            <a @click="handleDelete(record)">删除</a>
            <a-divider type="vertical"/>
            <a-switch
              size="small"
              :checked="record.enabled"
              :loading="toggleLoadingMap[record.id]"
              @change="checked => onToggleEnabled(record, checked)"
              checked-children="启用"
              un-checked-children="禁用"
            />
          </template>
        </span>
      </s-table>

      <a-modal
        :visible="saveVisible"
        title="创建应用"
        :confirm-loading="saveLoading"
        @cancel="closeSave"
        destroy-on-close
      >
        <a-form
          ref="saveFormRef"
          :model="saveForm"
          layout="vertical"
        >
          <a-form-item label="应用描述" name="description">
            <a-textarea
              :rows="4"
              v-model="saveForm.description"
              placeholder="请输入应用的简要描述，不能超过 300 字"
              allow-clear
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
              {{ saveForm.description.length }} / 300
            </span>
          </a-form-item>

          <a-form-item label="Node-RED 导出 JSON" name="flowJson">
            <a-textarea
              v-model="saveForm.flowJson"
              placeholder="请将 Node-RED 导出的 JSON 粘贴到这里"
              :rows="8"
              allow-clear
            />
          </a-form-item>
        </a-form>
        <template slot="footer">
          <a-button @click="closeSave">取消</a-button>
          <a-button type="default" @click="openNodeRed">打开 Node-RED</a-button>
          <a-button type="primary" :loading="saveLoading" @click="submitSave">保存</a-button>
        </template>
      </a-modal>
    </a-card>
  </page-header-wrapper>
</template>

<script setup>
/* eslint-disable */
import { message, Modal } from 'ant-design-vue'
import { ref, reactive } from 'vue'
import dayjs from 'dayjs'
import { listTapRule, deleteTap, createTapRule, getTapDetail, updateTapRule, setTapEnabled } from '@/api/manage'
import { STable, Ellipsis } from '@/components'

// 如果是 Vite，请用 import.meta.env.VITE_NODE_RED_URL；如果是 Vue-CLI，保留原样
const NODE_RED_URL =
  (import.meta && import.meta.env && import.meta.env.VITE_NODE_RED_URL) ||
  process.env.VUE_APP_NODE_RED_URL

// 查询参数（由 STable 的 parameter 合流到接口调用）
const searchParams = reactive({
  eventType: '',
  description: ''
})

// 事件选项
const eventOptions = [
  { value: 'manhole-flooding', label: '井盖水浸' },
  { value: 'manhole-tilte', label: '井盖倾斜' },
  { value: 'truck_dect', label: '渣土车识别' },
  { value: 'ill_parking', label: '机动车违章停车' },
  { value: 'ill_parking2', label: '非机动车违章停车' },
  { value: 'waste_accumulate', label: '垃圾堆积' },
  { value: 'greenbelt_stack', label: '绿化带乱堆乱放' },
  { value: 'road-operate', label: '占道经营' },
  { value: 'out-store', label: '店外经营' },
  { value: 'road-feeding', label: '占道饲养家禽' },
  { value: 'trash_full', label: '垃圾桶满溢' }
]

// 规范化 key，避免大小写/空格问题
const norm = v => (typeof v === 'string' ? v.trim().toLowerCase() : v)

// 映射表：value -> label
const eventLabelMap = Object.fromEntries(eventOptions.map(o => [norm(o.value), o.label]))

// 表格列
const columns = [
  { title: '序号', dataIndex: 'id' },
  { title: '事件类型', dataIndex: 'eventTypeLabel' },
  {
    title: '描述',
    dataIndex: 'description',
    // 兼容某些 STable 封装：声明使用名为 "description" 的插槽
    scopedSlots: { customRender: 'description' }
  },
  {
    title: '状态',
    dataIndex: 'enabled',
    width: '160px',
    scopedSlots: { customRender: 'enabled' } // 声明使用名为 "enabled" 的插槽
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    sorter: (a, b) => new Date(a.updateTime) - new Date(b.updateTime)
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: '200px',
    scopedSlots: { customRender: 'action' }
  }
]

// 表格引用，用于刷新
const tableRef = ref(null)

// STable 期望的“数据加载函数”
// parameter 由 STable 传入，一般包含 pageNo / pageSize / sorter / filters
const loadData = async (parameter = {}) => {
  try {
    const projectId = localStorage.getItem('project_id') || ''
    const pageNo = parameter.pageNo || 1
    const pageSize = parameter.pageSize || 10

    const res = await listTapRule({
      projectId,
      eventType: searchParams.eventType,
      description: searchParams.description,
      pageNo,
      pageSize
    })

    const records = res?.data ?? []
    const rows = records.map(r => ({
      ...r,
      // 事件类型映射 label，找不到就回退原值
      eventTypeLabel: eventLabelMap[norm(r.eventType)] ?? r.eventType,
      // 时间格式化：2025-08-19 15:59:16
      updateTime: r.updateTime ? dayjs(r.updateTime).format('YYYY-MM-DD HH:mm:ss') : ''
    }))

    return {
      data: rows,
      pageNo,
      total: res?.totalCount ?? 0
    }
  } catch (e) {
    message.error('获取应用列表失败')
    // 返回空数据以避免表格挂起
    return {
      data: [],
      pageNo: parameter.pageNo || 1,
      total: 0
    }
  }
}

// 交互：搜索/重置/创建/保存/编辑/删除
function doSearch () {
  // 让 STable 从第一页重新拉取
  tableRef.value?.refresh(true)
}

function handleReset () {
  searchParams.eventType = ''
  searchParams.description = ''
  tableRef.value?.refresh(true)
}

function handleBuild () {
  if (NODE_RED_URL) {
    window.open(NODE_RED_URL, '_blank')
  } else {
    message.error('未配置 NODE_RED_URL')
  }
}

function handleCreate () {
  saveVisible.value = true
}

function handleRecommend () {
  // 新开一个标签页打开 /tap/recommend 页面
  window.open('/tap/recommend', '_blank')
}

async function handleEdit(record) {
  const hide = message.loading('正在获取应用数据，请稍等片刻...', 0)
  try {
    // 调用接口获取详情
    const res = await getTapDetail({ id: record.id })
    if (res) {
      hide()
      // 把返回的数据填充到表单
      saveForm.description = res.description || ''
      saveForm.flowJson = res.flowJson || ''  // 根据后端字段名调整
      saveForm.id = res.id  // 如果要更新时带上 id

      // 打开弹窗
      saveVisible.value = true
    } else {
      hide()
      message.error('未获取到应用数据')
    }
  } catch (error) {
    hide()
    message.error('获取应用数据失败：' + (error.message || '未知错误'))
  }
}

async function openNodeRed () {
  if(saveForm.flowJson) {
    await fetch(`${NODE_RED_URL}/flows`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: saveForm.flowJson
    })
  }
  window.open(`${NODE_RED_URL}`, '_blank')
}

function handleDelete (record) {
  Modal.confirm({
    title: '确认删除?',
    content: '删除后将无法恢复，请确认是否继续。',
    onOk () {
      return deleteTap({ id: record.id })
        .then(() => {
          tableRef.value?.refresh()
          message.success('删除成功')
        })
        .catch((err) => {
          console.error('删除失败:', err)   // 打印详细错误堆栈
          message.error(`删除失败: ${err?.message || '未知错误'}`)
        })
    }
  })
}

const toggleLoadingMap = reactive({})
async function onToggleEnabled (record, checked) {
  const id = record.id
  const prev = record.enabled

  // 乐观更新，失败再回滚
  record.enabled = checked
  toggleLoadingMap[id] = true
  try {
    const success = await setTapEnabled(id, checked)
    if(success) {
      message.success(`已${checked ? '启用' : '禁用'}（ID: ${id}）`)
      tableRef.value?.refresh()
    }
    else {
      message.error(`操作失败：${err?.message || '未知错误'}`)
    }
  } catch (err) {
    record.enabled = prev // 回滚
    message.error(`操作失败：${err?.message || '未知错误'}`)
  } finally {
    toggleLoadingMap[id] = false
  }
}

// 保存应用： 弹窗和表单
const saveVisible = ref(false)
const saveLoading = ref(false)
const saveFormRef = ref(null)

const saveForm = reactive({
  id: '',
  description: '',
  flowJson: ''
})

function closeSave () {
  saveVisible.value = false
  // 可选：关闭时重置表单
  saveForm.id = ''
  saveForm.description = ''
  saveForm.flowJson = ''
}

// 提交保存
async function submitSave () {
  try {
    saveLoading.value = true
    // --- 手动校验 ---
    if (!saveForm.description || saveForm.description.trim() === '') {
      message.error('请输入应用描述')
      return
    }
    if (saveForm.description.length > 300) {
      message.error('描述不能超过 300 个字符')
      return
    }
    if (!saveForm.flowJson || saveForm.flowJson.trim() === '') {
      message.error('请粘贴 Node-RED 导出的 JSON')
      return
    }
    try {
      JSON.parse(saveForm.flowJson)
    } catch (e) {
      message.error('JSON 格式不正确，请检查后再试')
      return
    }
    // 修改应用
    if(saveForm.id) {
      const hide = message.loading('正在更新应用，请稍等片刻...', 0)
      try {
        const success = await updateTapRule(saveForm.id, saveForm.description, saveForm.flowJson)
        if (success) {
          hide()
          message.success('应用更新成功')
          saveVisible.value = false
          saveForm.id = ''
          saveForm.description = ''
          saveForm.flowJson = ''
          tableRef.value?.refresh?.()
        } else {
          hide()
          message.error('应用更新失败，请稍后重试')
        }
      } catch (error) {
        hide()
        message.error('应用更新失败: ' + error.message)
      }
    }
    // 创建应用
    else {
      const hide = message.loading('正在创建应用，请稍等片刻...', 0)
      try {
        const projectId = localStorage.getItem('project_id')
        const success = await createTapRule(projectId, saveForm.description, "", saveForm.flowJson)
        if (success) {
          hide()
          message.success('应用创建成功')
          saveVisible.value = false
          saveForm.id = ''
          saveForm.description = ''
          saveForm.flowJson = ''
          tableRef.value?.refresh?.()
        } else {
          hide()
          message.error('应用创建失败，请稍后重试')
        }
      } catch (error) {
        hide()
        message.error('应用创建失败: ' + error.message)
      }
    }
  } catch (err) {
    if (err?.errorFields) {
      // 表单校验错误由 antdv 弹出
    } else {
      message.error(`创建失败：${err?.message || '未知错误'}`)
    }
  } finally {
    saveLoading.value = false
  }
}
</script>
<style lang="less" scoped>
.table-page-search-wrapper {
  margin-bottom: 16px;
}
</style>