<template>
  <page-header-wrapper>
    <div style="padding: 0 24px 24px 24px;">
      <a-row :gutter="24" style="height: calc(100vh - 180px);">
        <!-- 左侧：主干规则（沿用你原本的列与筛选） -->
        <a-col :span="12">
          <a-card title="主干规则（FusionRule）" bordered :style="{ borderRadius: '8px', height: '100%' }">
            <div class="table-page-search-wrapper" style="margin-bottom: 12px;">
              <a-form layout="inline">
                <a-row :gutter="48">
                  <a-col :md="12" :sm="24">
                    <a-form-item label="使用状态">
                      <a-select v-model="queryParam.status" placeholder="请选择">
                        <a-select-option value="all">全部</a-select-option>
                        <a-select-option value="active">运行中</a-select-option>
                        <a-select-option value="inactive">已关闭</a-select-option>
                      </a-select>
                    </a-form-item>
                  </a-col>
                  <a-col :md="12" :sm="24">
                    <a-button type="primary" @click="refreshTable">查询</a-button>
                    <a-button style="margin-left: 8px" @click="resetSearchForm">重置</a-button>
                  </a-col>
                </a-row>
              </a-form>
            </div>

            <div style="margin-bottom: 12px;">
              <a-button type="primary" icon="plus" @click="handleAdd">使用Node-Red创建规则</a-button>
              <a-button type="primary" icon="plus" @click="openLLMCreation" style="margin-left:8px;">
                使用大模型创建规则
              </a-button>
            </div>

            <div style="height: calc(100% - 140px); display:flex; flex-direction: column;">
              <a-table
                ref="table"
                size="default"
                rowKey="ruleId"
                :columns="columns"
                :dataSource="data"
                :pagination="pagination"
                :customRow="customRuleRow"
                :scroll="{ y: 'calc(100vh - 460px)' }"
                style="flex:1;"
              >
                <span slot="status" slot-scope="text">
                  <a-badge
                    :status="text === 'active' ? 'processing' : 'default'"
                    :text="text === 'active' ? '运行中' : '已关闭'"/>
                </span>

                <span slot="action" slot-scope="text, record">
                  <a @click="execute(record)">执行</a>
                  <a-divider type="vertical" />
                  <a @click="pause(record)">暂停</a>
                  <a-divider type="vertical" />
                  <a @click="handleEdit(record)">编辑</a>
                  <a-divider type="vertical" />
                  <a @click="deleteRule(record)">删除</a>
                </span>
              </a-table>
            </div>
          </a-card>
        </a-col>

        <!-- 右侧：分支（Branch） -->
        <a-col :span="12">
          <a-card :title="rightTitle" bordered :style="{ borderRadius: '8px', height: '100%' }">
            <div style="height: calc(100% - 60px); display:flex; flex-direction: column;">
              <!-- 分支筛选 -->
              <a-row :gutter="16" style="margin-bottom: 16px;">
                <a-col :md="16" :sm="24">
                  <a-select v-model="branchQuery.status" placeholder="请选择分支状态" style="width:100%;">
                    <a-select-option value="all">全部</a-select-option>
                    <a-select-option value="active">运行中</a-select-option>
                    <a-select-option value="inactive">已关闭</a-select-option>
                  </a-select>
                </a-col>
                <a-col :md="8" :sm="24">
                  <a-button type="primary" block @click="filterBranches">查询</a-button>
                </a-col>
              </a-row>

              <!-- 分支表格 -->
              <a-table
                :columns="branchColumns"
                :dataSource="filteredBranches"
                row-key="branchId"
                :pagination="false"
                :rowSelection="{ selectedRowKeys: selectedBranchKeys, onChange: onBranchSelectChange }"
                :scroll="{ y: 'calc(100vh - 540px)' }"
                style="flex:1;"
              >
                <span slot="status" slot-scope="text">
                  <a-badge
                    :status="text === 'active' ? 'processing' : 'default'"
                    :text="text === 'active' ? '运行中' : '已关闭'"/>
                </span>

                <span slot="action" slot-scope="text, record">
                  <a @click="executeBranch(record)">执行</a>
                  <a-divider type="vertical" />
                  <a @click="pauseBranch(record)">暂停</a>
                  <a-divider type="vertical" />
                  <a @click="editBranch(record)">编辑</a>
                  <a-divider type="vertical" />
                  <a @click="deleteBranch(record)">删除</a>
                </span>
              </a-table>

              <div style="margin-top: 16px;">
                <a-button type="primary" :disabled="!activeRule" @click="showCreateBranchModal">新建分支</a-button>
                <a-button
                  type="default"
                  style="margin-left:8px"
                  :disabled="selectedBranchKeys.length !== 1"
                  @click="openBranchInNodeRed">
                  Node-RED 打开
                </a-button>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 新建/编辑分支 -->
    <a-modal
      v-model="branchModal.visible"
      :title="branchModal.isEdit ? '编辑分支' : '新建分支'"
      @ok="submitBranchModal"
      @cancel="closeBranchModal"
      :confirmLoading="branchModal.loading"
    >
      <a-form :form="branchForm">
        <a-form-item label="分支名称" :labelCol="{span:5}" :wrapperCol="{span:19}">
          <a-input v-decorator="['branchName', { initialValue: branchModal.model.branchName, rules:[{ required:true, message:'请输入分支名称'}]}]" />
        </a-form-item>
        <a-form-item label="状态" :labelCol="{span:5}" :wrapperCol="{span:19}">
          <a-select v-decorator="['status', { initialValue: branchModal.model.status || 'inactive' }]">
            <a-select-option value="active">active</a-select-option>
            <a-select-option value="inactive">inactive</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="目标表" :labelCol="{span:5}" :wrapperCol="{span:19}">
          <a-input v-decorator="['fusionTarget', { initialValue: branchModal.model.fusionTarget }]" placeholder="如：person"/>
        </a-form-item>
        <a-form-item label="Rule JSON" :labelCol="{span:5}" :wrapperCol="{span:19}">
          <a-textarea v-decorator="['ruleJson', { initialValue: branchModal.model.ruleJson }]" :rows="4" />
        </a-form-item>
        <a-form-item label="Flow JSON" :labelCol="{span:5}" :wrapperCol="{span:19}">
          <a-textarea v-decorator="['flowJson', { initialValue: branchModal.model.flowJson }]" :rows="4" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 你已有的 LLM 弹窗 -->
    <LLMCreation
      :modelModalVisible="modelModalVisible"
      @update:modelModalVisible="modelModalVisible = $event"
    />
  </page-header-wrapper>
</template>

<script>
import axios from 'axios'
import { getRuleList, executeRuleById, deleteRuleById, pauseRuleById } from '@/api/manage'
import { Modal, message } from 'ant-design-vue'
import LLMCreation from './modules/LLMCreation'

const BASE = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
const NODE_RED_URL = process.env.VUE_APP_NODE_RED_URL

export default {
  name: 'EventFusionMasterDetail',
  components: { LLMCreation },
  data () {
    return {
      // 主干
      columns: [
        { title: '规则名称', dataIndex: 'ruleName' }

      ],
      data: [],
      queryParam: { status: 'all' },
      pagination: { current: 1, pageSize: 10, total: 0 },
      selectedRowKeys: [],
      selectedRows: [],
      modelModalVisible: false,

      activeRule: null, // 当前选中的主干

      // 分支
      branchColumns: [
        { title: '分支ID', dataIndex: 'branchId', width: 90 },
        { title: '序号', dataIndex: 'branchIndex', width: 80 },
        { title: '分支名称', dataIndex: 'branchName' },
        { title: '目标表', dataIndex: 'fusionTarget', width: 120 },
        { title: '状态', dataIndex: 'status', width: 120, scopedSlots: { customRender: 'status' } },
        { title: '操作', dataIndex: 'action', width: 240, scopedSlots: { customRender: 'action' } }
      ],
      branches: [],
      filteredBranches: [],
      branchQuery: { status: 'all' },
      selectedBranchKeys: [],

      // 分支弹窗
      branchModal: {
        visible: false,
        isEdit: false,
        loading: false,
        model: { branchId: null, branchName: '', status: 'inactive', fusionTarget: '', ruleJson: '', flowJson: '' }
      },
      branchForm: null
    }
  },
  computed: {
    rowSelection () {
      return { selectedRowKeys: this.selectedRowKeys, onChange: this.onSelectChange }
    },
    rightTitle () {
      if (!this.activeRule) return '分支（请选择左侧主干）'
      return `分支 - ${this.activeRule.ruleName}（Rule #${this.activeRule.ruleId}）`
    }
  },
  created () {
    this.refreshTable()
  },
  methods: {
    // ===== 主干（沿用你原来的逻辑） =====
    refreshTable () {
      getRuleList().then(res => {
        const { status } = this.queryParam
        // 主干现在只有名字；状态来自默认分支/或你后端回传。此处保持兼容你的原字段。
        this.data = (status === 'all') ? res : res.filter(r => r.status === status)
        this.pagination.total = this.data.length
        if (this.data.length > 0) this.onPickRule(this.data[0])
      })
    },
    resetSearchForm () {
      this.queryParam.status = 'all'
      this.refreshTable()
    },
    handleAdd () {
      window.open(NODE_RED_URL, '_blank')
    },
    handleEdit (record) {
      // 编辑“主干”仍按你原逻辑（向 Node-RED 推flowJson），如果主干没有 flowJson，可以提示从右侧选择分支编辑
      if (!record.flowJson) {
        return message.info('主干不含具体流程，请选择右侧某个分支进行编辑')
      }
      try {
        const flowJson = JSON.parse(record.flowJson)
        fetch(`${NODE_RED_URL}/flows`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: flowJson
        }).finally(() => window.open(NODE_RED_URL, '_blank'))
      } catch (e) {
        console.error('解析 flowJson 出错:', e)
      }
    },
    execute (record) {
      const hide = message.loading('执行中...', 0)
      executeRuleById(record.ruleId)
        .then(() => { hide(); message.success('执行成功'); this.refreshTable() })
        .catch(() => { hide(); message.error('执行失败') })
    },
    pause (record) {
      Modal.confirm({
        title: '确认暂停？',
        content: `暂停规则：${record.ruleName}`,
        okText: '确定',
        cancelText: '取消',
        onOk: () => {
          return pauseRuleById(record.ruleId)
            .then(() => { message.success('规则已暂停'); this.refreshTable() })
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
            .then(() => { message.success('删除成功'); this.refreshTable() })
            .catch(() => message.error('删除失败'))
        }
      })
    },
    onSelectChange (keys, rows) {
      this.selectedRowKeys = keys
      this.selectedRows = rows
    },
    customRuleRow (record) {
      return {
        on: { click: () => this.onPickRule(record) },
        style: {
          cursor: 'pointer',
          backgroundColor: (this.activeRule && this.activeRule.ruleId === record.ruleId) ? '#e6f7ff' : ''
        }
      }
    },
    async onPickRule (rule) {
      this.activeRule = rule
      await this.fetchBranches(rule.ruleId)
      this.filterBranches()
    },

    // ===== 分支接口（axios直调新后端） =====
    async fetchBranches (ruleId) {
      if (!ruleId) return
      try {
        const res = await axios.get(`${BASE}/api/fusion/rules/${ruleId}/branches`)
        this.branches = (res.data || []).map(b => ({ ...b, status: b.status || 'inactive' }))
        this.filteredBranches = [...this.branches]
        this.selectedBranchKeys = []
      } catch (e) {
        console.error(e)
        message.error('获取分支列表失败')
      }
    },
    filterBranches () {
      const s = this.branchQuery.status
      this.filteredBranches = (s === 'all') ? [...this.branches] : this.branches.filter(b => (b.status || 'inactive') === s)
    },
    onBranchSelectChange (keys) { this.selectedBranchKeys = keys },

    // 分支操作
    async executeBranch (record) {
      const hide = message.loading('执行中...', 0)
      try {
        await axios.post(`${BASE}/api/fusion/branches/${record.branchId}/execute`)
        hide(); message.success('执行成功')
        await this.fetchBranches(this.activeRule.ruleId); this.filterBranches()
      } catch (e) { hide(); message.error('执行失败') }
    },
    async pauseBranch (record) {
      Modal.confirm({
        title: '确认暂停？',
        content: `暂停分支：${record.branchName}`,
        okText: '确定',
cancelText: '取消',
        onOk: async () => {
          try {
            await axios.put(`${BASE}/api/fusion/branches/${record.branchId}/pause`)
            message.success('分支已暂停')
            await this.fetchBranches(this.activeRule.ruleId); this.filterBranches()
          } catch (e) { message.error('暂停失败') }
        }
      })
    },
    async deleteBranch (record) {
      Modal.confirm({
        title: '确认删除该分支？',
        content: `是否删除分支：${record.branchName}`,
        okText: '确定',
cancelText: '取消',
        onOk: async () => {
          try {
            await axios.delete(`${BASE}/api/fusion/branches/${record.branchId}`)
            message.success('删除成功')
            await this.fetchBranches(this.activeRule.ruleId); this.filterBranches()
          } catch (e) { message.error('删除失败') }
        }
      })
    },
    editBranch (record) {
      this.branchModal.isEdit = true
      this.branchModal.model = { ...record }
      this.openBranchModal()
    },

    // 分支弹窗
    showCreateBranchModal () {
      if (!this.activeRule) return
      this.branchModal.isEdit = false
      this.branchModal.model = {
        branchId: null,
        branchName: `${this.activeRule.ruleName} ${this.branches.length + 1}`,
        status: 'inactive',
        fusionTarget: '',
        ruleJson: '',
        flowJson: ''
      }
      this.openBranchModal()
    },
    openBranchModal () {
      this.branchModal.visible = true
      this.$nextTick(() => {
        this.branchForm = this.$form.createForm(this, { name: 'branchForm' })
        const { branchName, status, fusionTarget, ruleJson, flowJson } = this.branchModal.model
        this.branchForm.setFieldsValue({ branchName, status, fusionTarget, ruleJson, flowJson })
      })
    },
    closeBranchModal () {
      this.branchModal.visible = false
      this.branchModal.loading = false
    },
    submitBranchModal () {
      this.branchForm.validateFields(async (err, values) => {
        if (err) return
        this.branchModal.loading = true
        try {
          if (this.branchModal.isEdit) {
            await axios.put(`${BASE}/api/fusion/branches/${this.branchModal.model.branchId}`, values)
            message.success('更新成功')
          } else {
            await axios.post(`${BASE}/api/fusion/rules/${this.activeRule.ruleId}/branches`, values)
            message.success('创建成功')
          }
          this.closeBranchModal()
          await this.fetchBranches(this.activeRule.ruleId); this.filterBranches()
        } catch (e) {
          console.error(e)
          message.error(this.branchModal.isEdit ? '更新失败' : '创建失败')
          this.branchModal.loading = false
        }
      })
    },

    // Node-RED 打开分支
    async openBranchInNodeRed () {
      if (this.selectedBranchKeys.length !== 1) return message.warning('请选择一个分支')
      const branch = this.filteredBranches.find(b => b.branchId === this.selectedBranchKeys[0])
      if (!branch) return
      try {
        const flowJson = branch.flowJson ? JSON.parse(branch.flowJson) : null
        if (flowJson) {
          await fetch(`${NODE_RED_URL}/flows`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(flowJson)
          })
        }
      } catch (e) {
        console.error('解析/推送 flowJson 出错：', e)
      } finally {
        window.open(NODE_RED_URL, '_blank')
      }
    },

    // LLM
    openLLMCreation () { this.modelModalVisible = true }
  }
}
</script>

<style scoped>
.ant-form-item { margin-bottom: 12px; }
.ant-table-row { cursor: pointer; }
.ant-card-body { height: calc(100% - 57px); padding: 24px; }
.ant-table-wrapper { height: 100%; }
@media (max-width: 768px) {
  .ant-col-12 { width: 100% !important; margin-bottom: 16px; }
}
</style>
