<template>
  <page-header-wrapper>
    <a-card :bordered="false">
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

            <a-col :md="8" :sm="24">
              <a-form-item label="应用描述">
                <a-input v-model="searchParams.description" placeholder="请输入" allow-clear />
              </a-form-item>
            </a-col>

            <a-col :md="8" :sm="24">
              <span>
                <a-button style="margin-left: 15px" type="primary" @click="doSearch">搜索</a-button>
                <a-button style="margin-left: 10px" @click="handleReset">重置</a-button>
                <a-button style="margin-left: 10px" type="dashed" @click="handleCreate">创建应用</a-button>
                <a-button style="margin-left: 10px" type="dashed" @click="handleSave">保存应用</a-button>
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

        <span slot="action" slot-scope="text, record">
          <template>
            <a @click="handleEdit(record)">编辑</a>
            <a-divider type="vertical"/>
            <a @click="handleDelete(record)">删除</a>
          </template>
        </span>
      </s-table>
    </a-card>
  </page-header-wrapper>
</template>

<script setup>
/* eslint-disable */
import { message, Modal } from 'ant-design-vue'
import { ref, reactive } from 'vue'
import dayjs from 'dayjs'
import { listTapRule, deleteTap } from '@/api/manage'
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
  { value: 'manhole-tilt', label: '井盖倾斜' },
  { value: 'truck_detect', label: '渣土车识别' },
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
    title: '更新时间',
    dataIndex: 'updateTime',
    sorter: (a, b) => new Date(a.updateTime) - new Date(b.updateTime)
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: '150px',
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

function handleCreate () {
  if (NODE_RED_URL) {
    window.open(NODE_RED_URL, '_blank')
  } else {
    message.error('未配置 NODE_RED_URL')
  }
}

function handleSave () {
  console.log('保存应用')
  message.success('已保存（示例）')
}

function handleEdit (record) {
  console.log('编辑', record)
  message.info(`编辑：${record?.id ?? ''}`)
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
</script>

<style lang="less" scoped>
.table-page-search-wrapper {
  margin-bottom: 16px;
}
</style>