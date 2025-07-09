<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="服务编号">
                <a-input v-model="queryParam.ruleId" placeholder="" />
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="使用状态">
                <a-select v-model="queryParam.ruleStatus" placeholder="请选择" default-value="0">
                  <a-select-option value="0">全部</a-select-option>
                  <a-select-option value="1">可用</a-select-option>
                  <a-select-option value="2">不可用</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <template v-if="advanced">
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
        <a-button type="primary" icon="plus" @click="handleAdd">新建</a-button>
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
        <!--        <span slot="description" slot-scope="text">-->
        <!--          <ellipsis :length="4" tooltip>{{ text }}</ellipsis>-->
        <!--        </span>-->

        <span slot="action" slot-scope="text, record">
          <template>
            <a @click="handleEdit(record)">编辑</a>
            <a-divider type="vertical" />
            <a @click="deleteRule(record)">删除</a>
          </template>
        </span>
        <span slot="csp" slot-scope="text, record">
          <template>
            <a @click="handleCsp(record)">查看</a>
          </template>
        </span>
        <span slot="target" slot-scope="text, record">
          <template>
            <a @click="viewTarget(record)">查看目标</a>
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
      <a-modal v-model="cspVisible" title="CSP 详情" @cancel="cspVisible = false">
        <a-button type="primary" @click="showLhaModal">查看LHA</a-button>
        <a-divider />

        <a-textarea v-model="inputValue" rows="4" placeholder="请输入内容" />

        <template slot="footer">
          <a-button @click="cspVisible = false">取消</a-button>
          <a-button type="primary" @click="submitCspData">确定</a-button>
        </template>
      </a-modal>
      <a-modal v-model="lhaVisible" title="LHA 详情" @cancel="lhaVisible = false">
        <a-textarea v-model="lhaContent" rows="4" placeholder="LHA 内容" disabled />
        <template slot="footer">
          <a-button type="primary" @click="lhaVisible = false">关闭</a-button>
        </template>
      </a-modal>
      <a-modal v-model="targetVisible" title="交互顺序详情" @cancel="targetVisible = false">
        <a-table :dataSource="interactionData" :rowKey="'id'" bordered>
          <a-table-column title="序号" dataIndex="id" key="id" />
          <a-table-column title="交互顺序">
            <template slot-scope="text, record">
              <a-button type="primary" @click="console.log(record.sequence)">
                {{ record.sequence }}
              </a-button>
            </template>
          </a-table-column>
          <a-table-column title="查看 UPPAAL 代码">
            <template slot-scope="text, record">
              <a-button type="default" @click="console.log('查看代码:', record.sequence)">
                查看
              </a-button>
            </template>
          </a-table-column>
          <a-table-column title="是否可达">
            <template slot-scope="text, record, index">
              <a-switch
                :checked="record.reachable"
                @click="toggleReachable(index)"
                checked-children="true"
                un-checked-children="false"
                :style="{ backgroundColor: record.reachable ? 'green' : 'gray' }"
              />
            </template>
          </a-table-column>
        </a-table>

        <template slot="footer">
          <a-button type="primary" @click="generateLLMSequence">使用 LLM 生成交互</a-button>
          <a-button @click="targetVisible = false">取消</a-button>
        </template>
      </a-modal>
    </a-card>
  </page-header-wrapper>
</template>

<script>
import moment from 'moment'
import { STable, Ellipsis } from '@/components'

import StepByStepModal from './modules/StepByStepModal'
import CreateForm from './modules/CreateForm'
import { saveCsp, getCSP, getServiceList } from '@/api/manage'

const columns = [
  {
    title: '服务名称',
    dataIndex: 'serviceName',
    scopedSlots: { customRender: 'serviceName' }
  },
  {
    title: '服务编号',
    dataIndex: 'serviceId'
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: '150px',
    scopedSlots: { customRender: 'action' }
  },
  {
    title: 'CSP',
    dataIndex: 'csp',
    width: '150px',
    scopedSlots: { customRender: 'csp' }
  },
  {
    title: '目标',
    dataIndex: 'target',
    width: '150px',
    scopedSlots: { customRender: 'target' }
  }
]

const statusMap = {
  0: {
    ruleStatus: 'default',
    text: '可用'
  },
  1: {
    ruleStatus: 'processing',
    text: '不可用'
  }
}

export default {
  name: 'TableList',
  components: {
    STable,
    Ellipsis,
    CreateForm,
    StepByStepModal
  },
  data () {
    return {
      targetVisible: false, // 控制目标弹框的显示
      selectedTargetRecord: null, // 选中的记录
      interactionData: [], // 存储从后端获取的交互顺序数据
      cspVisible: false, // 控制CSP弹窗的显示
      cspData: null, // 存储获取的CSP数据
      lhaVisible: false,
      lhaContent: '', // 存储LHA的内容
      inputValue: '', // 输入框的内容
      selectedRecord: null,
      columns: columns,
      data: [], // 初始化 data 为一个空数组
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
      }
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
    // getRoleList({ t: new Date() })
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
    viewTarget (record) {
      this.selectedTargetRecord = record
      this.targetVisible = true

      // 模拟从后端获取交互顺序数据
      this.fetchInteractionData(record.serviceId)
    },

    fetchInteractionData (serviceId) {
      // 模拟从后端获取数据
      this.interactionData = [
        { id: 1, sequence: 'move_command', reachable: true },
        { id: 2, sequence: 'approach_door', reachable: false },
        { id: 3, sequence: 'door_opened', reachable: true },
        { id: 4, sequence: 'robot_passed', reachable: true },
        { id: 5, sequence: 'door_closed', reachable: false }
      ]
    },

    toggleReachable (index) {
      this.interactionData[index].reachable = !this.interactionData[index].reachable
    },

    generateLLMSequence () {
      this.$message.success('使用 LLM 生成交互顺序')
      // 这里可以调用后端 API 或 LLM 生成逻辑
    },
    handleCsp (record) {
      this.selectedRecord = record
      this.cspVisible = true

      // 调用 getCSP 函数从后端获取数据
      getCSP(record.serviceId) // 假设传递 record.id 或其他参数给 getCSP
        .then(data => {
          const formattedCspData = JSON.stringify(data, null, 2) // 第二个参数为缩进空格数
          // 将转换后的字符串赋值给 inputValue
          this.inputValue = formattedCspData
          console.log('获取的CSP数据:', data)
        })
        .catch(error => {
          console.error('获取CSP数据失败:', error)
        })
    },

    showLhaModal () {
      // 模拟从后端获取LHA内容
      this.lhaContent = '这里是从后端获取的LHA内容'
      this.lhaVisible = true
    },

    submitCspData () {
      // 校验输入内容
      if (!this.inputValue.trim()) {
        this.$message.error('请输入内容')
        return
      }

      // 校验是否选中 serviceId
      if (!this.selectedRecord || !this.selectedRecord.serviceId) {
        this.$message.error('未选择有效规则')
        return
      }

      try {
        // 调用 manage.js 里的 saveCsp 方法
        saveCsp(
          this.selectedRecord.serviceId,
          this.inputValue
        )

        // 成功提示
        this.$message.success('提交成功')

        // 关闭弹窗 & 清空输入框
        this.cspVisible = false
        this.inputValue = ''
      } catch (error) {
        this.$message.error('提交失败，请重试')
        console.error(error)
      }
    },
    refreshTable () {
      this.loadData().then(data => {
        this.data = data
        this.pagination.total = data.length // 设置总记录数
      })
    },
    handleAdd () {
      window.open(process.env.VUE_APP_NODE_RED_URL, '_blank')
    },
    handleEdit (record) {
      console.log(record)
      if (typeof record.serviceJson === 'object') {
        console.warn('⚠️ record.flowJson 是对象，尝试转换为 JSON 字符串')
      }
      const updatedFlowJson = this.addServiceIdToFlowJson(JSON.parse(record.serviceJson), record.serviceId)
      console.log(updatedFlowJson)
      fetch(`${process.env.VUE_APP_NODE_RED_URL}/flows`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(updatedFlowJson)
      })
        .finally(() => {
          window.open(process.env.VUE_APP_NODE_RED_URL, '_blank')
        })
        .catch(error => {
          console.error('网络错误:', error)
        })
    },

    addServiceIdToFlowJson (flowJson, serviceId) {
      return flowJson.map(node => {
        // 只在 "tab"（flow 画布）类型的节点中添加 serviceId
        if (node.type === 'tab') {
          // 确保 `env` 存在
          node.env = node.env || []

          // 检查 `env` 是否已经包含 `serviceId`
          const existingServiceId = node.env.find(envVar => envVar.name === 'serviceId')
          if (existingServiceId) {
            existingServiceId.value = serviceId // 更新已有的值
          } else {
            node.env.push({ name: 'serviceId', value: serviceId }) // 添加新的 serviceId
          }
        }
        return node
      })
    },
    handleOk () {
      const form = this.$refs.createModal.form
      this.confirmLoading = true
      form.validateFields((errors, values) => {
        if (!errors) {
          if (values.id > 0) {
            new Promise((resolve, reject) => {
              setTimeout(() => {
                resolve()
              }, 1000)
            }).then(() => {
              this.visible = false
              this.confirmLoading = false
              form.resetFields()
              this.refreshTable()
              this.$message.info('修改成功')
            })
          } else {
            new Promise((resolve, reject) => {
              setTimeout(() => {
                resolve()
              }, 1000)
            }).then(() => {
              this.visible = false
              this.confirmLoading = false
              form.resetFields()
              this.refreshTable()
              this.$message.info('新增成功')
            })
          }
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
        this.$message.info(`${record.serviceId} 订阅成功`)
      } else {
        this.$message.error(`${record.serviceId} 订阅失败，规则已关闭`)
      }
    },
    deleteRule (record) {
      if (record.ruleStatus !== 0) {
        this.$message.info(`${record.serviceId} 订阅成功`)
      } else {
        this.$message.error(`${record.serviceId} 订阅失败，规则已关闭`)
      }
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
      console.log(99999)
      // 增加假数据
      // const fakeData = [
      //   {
      //     ruleId: 1,
      //     ruleName: '机器人过门',
      //     callCount: 3,
      //     ruleStatus: 0, // 0 表示可用
      //     projectName: '2024-05-10'
      //   },
      //   {
      //     ruleId: 2,
      //     ruleName: '车辆进出停车场服务',
      //     callCount: 3,
      //     ruleStatus: 0, // 0 表示可用
      //     projectName: '2024-05-10'
      //   }
      // ]
      const project = localStorage.getItem('project_id')
      console.log(project)
      return getServiceList(project)
        .then(res => {
          console.log('Data received:', res)
          return res // 确保数据格式是数组
        })
      // return new Promise((resolve) => {
      //   // 模拟 API 延迟
      //   setTimeout(() => {
      //     resolve(fakeData)
      //   }, 500)
      // })
    }
  }
}
</script>
