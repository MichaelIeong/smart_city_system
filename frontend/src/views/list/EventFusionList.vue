<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- 搜尋條件 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="使用状态">
                <a-select v-model="queryParam.status" placeholder="请选择">
                  <a-select-option value="all">全部</a-select-option>
                  <a-select-option value="active">运行中</a-select-option>
                  <a-select-option value="inactive">已关闭</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-button type="primary" @click="refreshTable">查询</a-button>
              <a-button style="margin-left: 8px" @click="resetSearchForm">重置</a-button>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <!-- 操作區 -->
      <div class="table-operator">
        <a-button type="primary" icon="plus" @click="handleAdd">使用Node-Red创建规则</a-button>
        <a-button type="primary" icon="plus" @click="openLLMCreation">使用大模型创建规则</a-button>
      </div>

      <!-- 表格 -->
      <a-table
        ref="table"
        size="default"
        rowKey="ruleId"
        :columns="columns"
        :dataSource="data"
        :rowSelection="rowSelection"
        :pagination="pagination"
      >
        <span slot="status" slot-scope="text">
          <a-badge :status="text === 'active' ? 'processing' : 'default'" :text="text === 'active' ? '运行中' : '已关闭'" />
        </span>

        <span slot="action" slot-scope="text, record">
          <template>
            <a @click="execute(record)">执行</a>
            <a-divider type="vertical" />
            <a @click="pause(record)">暂停</a>
            <a-divider type="vertical" />
            <a @click="handleEdit(record)">编辑</a>
            <a-divider type="vertical" />
            <a @click="deleteRule(record)">删除</a>
          </template>
        </span>
      </a-table>

      <LLMCreation
        :modelModalVisible="modelModalVisible"
        @update:modelModalVisible="modelModalVisible = $event"
      />
    </a-card>
  </page-header-wrapper>
</template>

<script>
import { getRuleList, executeRuleById, deleteRuleById, pauseRuleById } from '@/api/manage'
import { Modal, message } from 'ant-design-vue'
import LLMCreation from './modules/LLMCreation'

export default {
  name: 'TableList',
  components: {
    LLMCreation
  },
  data () {
    return {
      columns: [
        { title: '规则名称', dataIndex: 'ruleName' },
        { title: '状态', dataIndex: 'status', scopedSlots: { customRender: 'status' } },
        { title: '操作', dataIndex: 'action', width: '220px', scopedSlots: { customRender: 'action' } }
      ],
      data: [],
      queryParam: { status: 'all' },
      selectedRowKeys: [],
      selectedRows: [],
      pagination: {
        current: 1,
        pageSize: 10,
        total: 0
      },
      modelModalVisible: false
    }
  },
  created () {
    this.refreshTable()
  },
  computed: {
    rowSelection () {
      return {
        selectedRowKeys: this.selectedRowKeys,
        onChange: this.onSelectChange
      }
    }
  },
  methods: {
    refreshTable () {
      getRuleList().then(res => {
        const { status } = this.queryParam
        this.data = (status === 'all') ? res : res.filter(r => r.status === status)
        this.pagination.total = this.data.length
      })
    },
    resetSearchForm () {
      this.queryParam.status = 'all'
      this.refreshTable()
    },
    handleAdd () {
      window.open(process.env.VUE_APP_NODE_RED_URL, '_blank')
    },
    handleEdit (record) {
      try {
        const flowJson = JSON.parse(record.flowJson)
        fetch(`${process.env.VUE_APP_NODE_RED_URL}/flows`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: flowJson
        }).finally(() => window.open(process.env.VUE_APP_NODE_RED_URL, '_blank'))
      } catch (e) {
        console.error('解析 flowJson 出错:', e)
      }
    },
    execute (record) {
      const hide = message.loading('执行中...', 0)
      executeRuleById(record.ruleId)
        .then(() => {
          hide()
          message.success('执行成功')
          this.refreshTable()
        })
        .catch(() => {
          hide()
          message.error('执行失败')
        })
    },
    pause (record) {
      Modal.confirm({
        title: '确认暂停？',
        content: `暂停规则：${record.ruleName}`,
        okText: '确定',
        cancelText: '取消',
        onOk: () => {
          return pauseRuleById(record.ruleId)
            .then(() => {
              message.success('规则已暂停')
              this.refreshTable()
            })
            .catch(() => message.error('暂停失败'))
        }
      })
    },
    deleteRule (record) {
      Modal.confirm({
        title: '确认删除该规则？',
        content: `是否删除规则：${record.ruleName}`,
        okText: '确定',
        cancelText: '取消',
        onOk: () => {
          return deleteRuleById(record.ruleId)
            .then(() => {
              message.success('删除成功')
              this.refreshTable()
            })
            .catch(() => message.error('删除失败'))
        }
      })
    },
    onSelectChange (keys, rows) {
      this.selectedRowKeys = keys
      this.selectedRows = rows
    },
    openLLMCreation () {
      this.modelModalVisible = true
    }
  }
}
</script>
