<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="使用状态">
                <a-select v-model="queryParam.ruleStatus" placeholder="请选择" default-value="0">
                  <a-select-option value="0">全部</a-select-option>
                  <a-select-option value="1">关闭</a-select-option>
                  <a-select-option value="2">运行中</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <template v-if="advanced">
              <a-col :md="8" :sm="24">
                <a-form-item label="调用次数">
                  <a-input-number v-model="queryParam.callCount" style="width: 100%" />
                </a-form-item>
              </a-col>
              <a-col :md="8" :sm="24">
                <a-form-item label="更新日期">
                  <a-date-picker v-model="queryParam.date" style="width: 100%" placeholder="请输入更新日期" />
                </a-form-item>
              </a-col>
            </template>
            <a-col :md="!advanced && 8 || 24" :sm="24">
              <span
                class="table-page-search-submitButtons"
                :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
                <a-button type="primary" @click="refreshTable">查询</a-button>
                <a-button style="margin-left: 8px" @click="resetSearchForm">重置</a-button>
                <a @click="toggleAdvanced" style="margin-left: 8px">
                  {{ advanced ? '收起' : '展开' }}
                  <a-icon :type="advanced ? 'up' : 'down'" />
                </a>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <div class="table-operator">
        <a-button type="primary" icon="plus" @click="handleAdd">使用Node-Red创建规则</a-button>
        <a-button type="primary" icon="plus" @click="openLLMCreation">使用大模型创建规则</a-button>
        <a-dropdown v-action:edit v-if="selectedRowKeys.length > 0">
          <a-menu slot="overlay">
            <a-menu-item key="1">
              <a-icon type="delete" />
              删除
            </a-menu-item>
            <a-menu-item key="2">
              <a-icon type="lock" />
              锁定
            </a-menu-item>
          </a-menu>
          <a-button style="margin-left: 8px">
            批量操作
            <a-icon type="down" />
          </a-button>
        </a-dropdown>
      </div>

      <a-table
        ref="table"
        size="default"
        rowKey="ruleId"
        :columns="columns"
        :dataSource="data"
        :rowSelection="rowSelection"
        :pagination="pagination"
      >
        <span slot="ruleStatus" slot-scope="text">
          <a-badge :status="text | statusTypeFilter" :text="text | statusFilter" />
        </span>

        <span slot="action" slot-scope="text, record">
          <template>
            <a @click="execute(record)">执行</a>
            <a-divider type="vertical" />
            <a @click="handleEdit(record)">编辑</a>
            <a-divider type="vertical" />
            <a @click="deleteRule(record)">删除</a>
          </template>
        </span>
      </a-table>

      <create-form
        ref="createModal"
        :visible="visible"
        :loading="confirmLoading"
        :model="mdl"
        @cancel="handleCancel"
        @ok="handleOk"
      />
      <step-by-step-modal ref="modal" @ok="handleOk" />

      <!-- 引入 LLMCreation 组件 -->
      <LLMCreation
        :modelModalVisible="modelModalVisible"
        @update:modelModalVisible="modelModalVisible = $event"
      />
    </a-card>
  </page-header-wrapper>
</template>

<script>
import moment from 'moment'
import { STable, Ellipsis } from '@/components'
import { getRuleList, executeRuleById, deleteRuleById } from '@/api/manage'
import { Modal, message } from 'ant-design-vue'

import StepByStepModal from './modules/StepByStepModal'
import CreateForm from './modules/CreateForm'
import LLMCreation from './modules/LLMCreation'

const columns = [
  {
    title: '规则名称',
    dataIndex: 'ruleName',
    scopedSlots: { customRender: 'ruleName' }
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: '150px',
    scopedSlots: { customRender: 'action' }
  }
]

const statusMap = {
  0: {
    ruleStatus: 'default',
    text: '关闭'
  },
  1: {
    ruleStatus: 'processing',
    text: '运行中'
  },
  2: {
    ruleStatus: 'success',
    text: '已上线'
  },
  3: {
    ruleStatus: 'error',
    text: '异常'
  }
}

export default {
  name: 'TableList',
  components: {
    STable,
    Ellipsis,
    CreateForm,
    StepByStepModal,
    LLMCreation
  },
  data () {
    return {
      columns: columns,
      data: [],
      visible: false,
      confirmLoading: false,
      mdl: null,
      advanced: false,
      queryParam: {},
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
  filters: {
    statusFilter (type) {
      return statusMap[type].text
    },
    statusTypeFilter (type) {
      return statusMap[type].ruleStatus
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
      this.loadData().then(data => {
        this.data = data
        this.pagination.total = data.length
      })
    },
    handleAdd () {
      window.open('http://127.0.0.1:1880/', '_blank')
    },
    handleEdit (record) {
      let flowJson
      try {
        flowJson = JSON.parse(record.flowJson)
      } catch (e) {
        console.error('解析 flowJson 时出错:', e)
        return
      }

      fetch('http://127.0.0.1:1880/flows', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: flowJson
      })
        .finally(() => {
          window.open('http://127.0.0.1:1880/', '_blank')
        })
        .catch(error => {
          console.error('网络错误:', error)
        })
    },
    handleOk () {
      const form = this.$refs.createModal.form
      this.confirmLoading = true
      form.validateFields((errors, values) => {
        if (!errors) {
          const isEdit = values.id > 0
          new Promise(resolve => {
            setTimeout(() => resolve(), 1000)
          }).then(() => {
            this.visible = false
            this.confirmLoading = false
            form.resetFields()
            this.refreshTable()
            this.$message.success(isEdit ? '修改成功' : '新增成功')
          })
        } else {
          this.confirmLoading = false
        }
      })
    },
    handleCancel () {
      this.visible = false
      const form = this.$refs.createModal.form
      form.resetFields()
    },
    handleSub (record) {
      if (record.ruleStatus !== 0) {
        this.$message.info(`${record.ruleId} 订阅成功`)
      } else {
        this.$message.error(`${record.ruleId} 订阅失败，规则已关闭`)
      }
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
            .catch(() => {
              message.error('删除失败，请稍后再试')
            })
        },
        onCancel: () => {
          message.info('已取消删除')
        }
      })
    },
    onSelectChange (selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
    },
    toggleAdvanced () {
      this.advanced = !this.advanced
    },
    resetSearchForm () {
      this.queryParam = {
        date: moment(new Date())
      }
    },
    loadData () {
      return getRuleList().then(res => res)
    },
    openLLMCreation () {
      this.modelModalVisible = true
    },

    // 执行规则
    execute (record) {
      const hide = message.loading('正在执行规则...', 0) // 显示 loading

      executeRuleById(record.ruleId)
        .then(() => {
          hide() // 关闭 loading
          message.success('规则执行成功!')
        })
        .catch(() => {
          hide()
          message.error('规则执行失败!')
        })
    }
  }
}
</script>
