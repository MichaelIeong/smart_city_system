<template>
  <page-header-wrapper>
    <div style="padding: 0 24px 24px 24px;">
      <a-row :gutter="24" style="height: calc(100vh - 180px);">
        <!-- 左侧：主干规则 -->
        <a-col :span="12">
          <a-card title="主干规则" bordered :style="{ borderRadius: '8px', height: '100%' }">
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
              <a-button type="primary" icon="plus" @click="handleAdd">
                使用Node-Red创建规则
              </a-button>
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
                    :text="text === 'active' ? '运行中' : '已关闭'" />
                </span>

                <span slot="action" slot-scope="text, record">
                  <a @click.stop.prevent="handleEdit(record)">编辑</a>
                  <a-divider type="vertical" />
                  <a @click.stop.prevent="openApplyModal(record)">套用到可达空间</a>
                  <a-divider type="vertical" />
                  <a @click.stop.prevent="deleteRule(record)">删除</a>
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
                :scroll="{ y: 'calc(100vh - 540px)' }"
                style="flex:1;"
              >
                <span slot="status" slot-scope="text">
                  <a-badge
                    :status="text === 'active' ? 'processing' : 'default'"
                    :text="text === 'active' ? '运行中' : '已关闭'" />
                </span>

                <span slot="action" slot-scope="text, record">
                  <a @click.stop.prevent="executeBranch(record)">执行</a>
                  <a-divider type="vertical" />
                  <a @click.stop.prevent="pauseBranch(record)">暂停</a>
                  <a-divider type="vertical" />
                  <a @click.stop.prevent="editBranch(record)">编辑</a>
                  <a-divider type="vertical" />
                  <a @click.stop.prevent="deleteBranch(record)">删除</a>
                </span>
              </a-table>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 编辑分支（仅名称 + 纯跳转到 Node-RED） -->
    <a-modal
      v-model="branchModal.visible"
      title="编辑分支"
      @ok="submitBranchModal"
      @cancel="closeBranchModal"
      :confirmLoading="branchModal.loading"
    >
      <a-form :form="branchForm">
        <a-form-item label="分支名称" :labelCol="{span:5}" :wrapperCol="{span:19}">
          <a-input
            v-decorator="[
              'branchName',
              { initialValue: branchModal.model.branchName, rules:[{ required:true, message:'请输入分支名称'}]}
            ]"
            @pressEnter.prevent
          />
        </a-form-item>

        <div style="display: flex; justify-content: center; align-items: center; margin-top: 12px;">
          <a-button type="primary" @click="goToNodeRed(branchModal.model)">
            在 Node-RED 中编辑
          </a-button>
          <a-tooltip placement="right" style="margin-left: 8px;">
            <template slot="title">
              跳转到 Node-RED，提交与部署由 Node-RED 端处理。
            </template>
            <a-icon type="info-circle" />
          </a-tooltip>
        </div>
      </a-form>
    </a-modal>

    <!-- 套用到可达空间（显示 name，选择值为 ID） -->
    <a-modal
      v-model="applyModal.visible"
      title="套用到可达空间"
      @ok="confirmApply"
      @cancel="closeApplyModal"
      :confirmLoading="applyModal.loading"
      :okButtonProps="{ disabled: applyModal.selectedSpaceIds.length === 0 }"
    >
      <p style="margin-bottom: 12px;">
        将规则 <b>{{ applyModal.rule?.ruleName }}</b> 复制到所选可达空间。
      </p>

      <a-spin :spinning="applyModal.loadingPreview">
        <template v-if="applyModal.spaces && applyModal.spaces.length">
          <a-checkbox-group
            v-model="applyModal.selectedSpaceIds"
            style="display:flex; flex-direction:column; gap:8px;"
          >
            <a-checkbox
              v-for="sp in applyModal.spaces"
              :key="sp.id"
              :value="sp.id"
            >
              {{ sp.name }}
            </a-checkbox>
          </a-checkbox-group>
        </template>
        <a-empty v-else description="未检测到可达空间" />
      </a-spin>
    </a-modal>

    <!-- 主干改名弹窗 -->
    <a-modal
      v-model="ruleModal.visible"
      title="编辑主干名称"
      @ok="submitRuleModal"
      @cancel="closeRuleModal"
      :confirmLoading="ruleModal.loading"
    >
      <a-form :form="ruleForm">
        <a-form-item label="规则名称" :labelCol="{span:5}" :wrapperCol="{span:19}">
          <a-input
            v-decorator="[
              'ruleName',
              { initialValue: ruleModal.model.ruleName, rules:[{ required:true, message:'请输入规则名称'}]}
            ]"
          />
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
        { title: '规则名称', dataIndex: 'ruleName' },
        { title: '操作', dataIndex: 'action', width: '320px', scopedSlots: { customRender: 'action' } }
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
        { title: '分支名称', dataIndex: 'branchName' },
        { title: '目标表', dataIndex: 'fusionTarget', width: 120 },
        { title: '状态', dataIndex: 'status', width: 120, scopedSlots: { customRender: 'status' } },
        { title: '操作', dataIndex: 'action', width: 240, scopedSlots: { customRender: 'action' } }
      ],
      branches: [],
      filteredBranches: [],
      branchQuery: { status: 'all' },

      // 分支弹窗（仅编辑名称）
      branchModal: {
        visible: false,
        loading: false,
        model: { branchId: null, branchName: '' }
      },
      branchForm: null,

      // 套用到可达空间
      applyModal: {
        visible: false,
        loading: false,
        loadingPreview: false,
        rule: null,
        spaces: [], // [{ id, name }]
        selectedSpaceIds: [] // 仅存选中的 ID
      },

      // 主干改名弹窗
      ruleModal: {
        visible: false,
        loading: false,
        model: { ruleId: null, ruleName: '' }
      },
      ruleForm: null,

      // Space ID -> 名称 映射
      spaceMap: {} // { [id:number]: name:string }
    }
  },
  computed: {
    rowSelection () {
      return { selectedRowKeys: this.selectedRowKeys, onChange: this.onSelectChange }
    },
    rightTitle () {
      if (!this.activeRule) return '分支（请选择左侧主干）'
      return `分支 - ${this.activeRule.ruleName}`
    }
  },
  created () {
    this.fetchSpaceMap()
    this.refreshTable()
  },
  methods: {
    /** 规范化 Node-RED 基地址（去掉尾部 /） */
    _nrBase () {
      if (!NODE_RED_URL) {
        message.error('未配置 NODE_RED_URL')
        throw new Error('NODE_RED_URL missing')
      }
      return String(NODE_RED_URL).replace(/\/$/, '')
    },

    /** 规范化 flow：把被 stringify 的 JSON（最多两层）还原为对象/数组 */
    _normalizeFlow (fj) {
      let v = fj
      for (let i = 0; i < 2 && typeof v === 'string'; i++) {
        const s = v.trim()
        const looksJson =
          (s.startsWith('{') && s.endsWith('}')) ||
          (s.startsWith('[') && s.endsWith(']')) ||
          (s.startsWith('"') && s.endsWith('"'))
        if (!looksJson) break
        try {
          v = JSON.parse(s)
        } catch (e) {
          break
        }
      }
      if (!Array.isArray(v) && typeof v !== 'object') {
        throw new Error('flowJson 不是对象或数组，格式不符合 Node-RED 要求')
      }
      return v
    },

    /** 推送 flow 到 Node-RED Admin API 然后打开编辑器 */
    async pushFlowAndOpen (flowJson, { deployType = 'flows' } = {}) {
      const base = this._nrBase()
      const normalized = this._normalizeFlow(flowJson)
      const bodyStr = JSON.stringify(normalized)

      const headers = {
        'Content-Type': 'application/json',
        'X-Node-RED-Deployment-Type': deployType
      }

      const resp = await fetch(`${base}/flows`, {
        method: 'POST',
        headers,
        body: bodyStr
      })
      if (!resp.ok) {
        const text = await resp.text().catch(() => '')
        throw new Error(`推送到 Node-RED 失败：HTTP ${resp.status} ${text}`)
      }
      window.open(`${base}`, '_blank')
    },
    // ===== Space 映射 =====
    async fetchSpaceMap () {
      try {
        const res = await axios.get(`${BASE}/api/spaces/list`)
        this.spaceMap = {}
        ;(res.data || []).forEach(s => {
          this.spaceMap[s.spaceId] = s.spaceName
        })
      } catch (e) {
        console.error('获取空间列表失败', e)
      }
    },

    // ===== 主干 =====
    refreshTable () {
      getRuleList().then(res => {
        const { status } = this.queryParam
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
      if (!NODE_RED_URL) {
        message.error('未配置 NODE_RED_URL')
        return
      }
      const params = new URLSearchParams({ source: 'frontend', action: 'create' })
      window.open(`${NODE_RED_URL}?${params.toString()}`, '_blank')
    },

    // 仅“主干改名”
    handleEdit (record) {
      this.ruleModal.model = { ruleId: record.ruleId, ruleName: record.ruleName }
      this.openRuleModal()
    },
    openRuleModal () {
      this.ruleModal.visible = true
      this.$nextTick(() => {
        this.ruleForm = this.$form.createForm(this, { name: 'ruleForm' })
        const { ruleName } = this.ruleModal.model
        this.ruleForm.setFieldsValue({ ruleName })
      })
    },
    closeRuleModal () {
      this.ruleModal.visible = false
      this.ruleModal.loading = false
    },
    submitRuleModal () {
      this.ruleForm.validateFields(async (err, values) => {
        if (err) return
        this.ruleModal.loading = true
        try {
          const { ruleId } = this.ruleModal.model
          await axios.put(`${BASE}/api/fusion/rules/${ruleId}`, {
            ruleName: values.ruleName
          })
          message.success('主干名称已更新')
          this.closeRuleModal()
          this.refreshTable()
        } catch (e) {
          console.error(e)
          message.error('更新失败')
          this.ruleModal.loading = false
        }
      })
    },

    // ====== 套用到可达空间 ======
    openApplyModal (rule) {
      this.applyModal.rule = rule
      this.applyModal.visible = true
      this.applyModal.loadingPreview = true
      this.applyModal.spaces = []
      this.applyModal.selectedSpaceIds = []

      axios.get(`${BASE}/api/fusion/executableSpaces/${rule.ruleId}`)
        .then(res => {
          const list = Array.isArray(res.data) ? res.data : []
          // 兼容两种返回格式：
          // 1) 老： [number, number, ...]
          // 2) 新： [{ id, name }, ...]
          if (list.length > 0 && typeof list[0] === 'number') {
            // 老格式：只有 ID，继续用 spaceMap 映射名称
            this.applyModal.spaces = list.map(id => ({
              id,
              name: this.spaceMap[id] || `空间 #${id}`
            }))
          } else {
            // 新格式：后端已给出 name，直接使用；缺失时再兜底 spaceMap
            this.applyModal.spaces = list.map(it => ({
              id: it.id,
              name: it.name || this.spaceMap[it.id] || `空间 #${it.id}`
            }))
          }
        })
        .catch(() => {
          this.applyModal.spaces = []
        })
        .finally(() => {
          this.applyModal.loadingPreview = false
        })
    },
    closeApplyModal () {
      this.applyModal.visible = false
      this.applyModal.loading = false
      this.applyModal.rule = null
      this.applyModal.selectedSpaceIds = []
    },
    async confirmApply () {
      if (!this.applyModal.rule) return
      if (this.applyModal.selectedSpaceIds.length === 0) {
        message.warning('请先勾选至少一个空间')
        return
      }
      this.applyModal.loading = true
      try {
        const { ruleId } = this.applyModal.rule
        const res = await axios.post(
          `${BASE}/api/fusion/rules/${ruleId}/applyToExecutableSpaces`,
          { spaceIds: this.applyModal.selectedSpaceIds },
          { params: { activate: false } }
        )
        message.success(`已套用：新建 ${res.data?.createdBranches || 0} 个分支`)
        this.closeApplyModal()
        if (this.activeRule && this.activeRule.ruleId === ruleId) {
          await this.fetchBranches(ruleId)
          this.filterBranches()
        }
      } catch (e) {
        console.error(e)
        message.error('套用失败')
        this.applyModal.loading = false
      }
    },

    // ===== 规则操作 =====
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

    // ===== 分支接口 =====
    async fetchBranches (ruleId) {
      if (!ruleId) return
      try {
        const res = await axios.get(`${BASE}/api/fusion/rules/${ruleId}/branches`)
        this.branches = (res.data || []).map(b => ({ ...b, status: b.status || 'inactive' }))
        this.filteredBranches = [...this.branches]
      } catch (e) {
        console.error(e)
        message.error('获取分支列表失败')
      }
    },
    filterBranches () {
      const s = this.branchQuery.status
      this.filteredBranches = (s === 'all') ? [...this.branches] : this.branches.filter(b => (b.status || 'inactive') === s)
    },

    // ===== 分支操作 =====
    async executeBranch (record) {
      const hide = message.loading('执行中...', 0)
      try {
        await axios.post(`${BASE}/api/fusion/executeBranch/${record.branchId}`)
        hide()
        message.success('执行成功')
        await this.fetchBranches(this.activeRule.ruleId)
        this.filterBranches()
      } catch (e) {
        hide()
        message.error('执行失败')
      }
    },
    async pauseBranch (record) {
      Modal.confirm({
        title: '确认暂停？',
        content: `暂停分支：${record.branchName}`,
        okText: '确定',
        cancelText: '取消',
        onOk: async () => {
          try {
            await axios.put(`${BASE}/api/fusion/pauseBranch/${record.branchId}`)
            message.success('分支已暂停')
            await this.fetchBranches(this.activeRule.ruleId)
            this.filterBranches()
          } catch (e) {
            message.error('暂停失败')
          }
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
            await this.fetchBranches(this.activeRule.ruleId)
            this.filterBranches()
          } catch (e) {
            message.error('删除失败')
          }
        }
      })
    },

    // ====== 仅编辑“分支名称”，按钮进入 Node-RED（纯跳转） ======
    editBranch (record) {
      this.branchModal.model = {
        branchId: record.branchId,
        branchName: record.branchName
      }
      this.openBranchModal()
    },
    openBranchModal () {
      this.branchModal.visible = true
      this.$nextTick(() => {
        this.branchForm = this.$form.createForm(this, { name: 'branchForm' })
        const { branchName } = this.branchModal.model
        this.branchForm.setFieldsValue({ branchName })
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
          await axios.put(`${BASE}/api/fusion/branches/${this.branchModal.model.branchId}`, {
            branchName: values.branchName
          })
          message.success('更新成功')
          this.closeBranchModal()
          await this.fetchBranches(this.activeRule.ruleId)
          this.filterBranches()
        } catch (e) {
          console.error(e)
          message.error('更新失败')
          this.branchModal.loading = false
        }
      })
    },

    // 只跳转到 Node-RED，不提交任何数据
    async goToNodeRed (model) {
      try {
        const branchId = model?.branchId
        let branch = this.branches.find(b => b.branchId === branchId)

        // 如果列表项里没有 flowJson，则补拉一次详情
        if (!branch || !branch.flowJson) {
          const { data } = await axios.get(`${BASE}/api/fusion/branches/${branchId}`)
          branch = { ...(branch || {}), ...(data || {}) }
        }

        if (!branch || !branch.flowJson) {
          message.error('该分支缺少 flowJson，无法推送到 Node-RED')
          return
        }

        await this.pushFlowAndOpen(branch.flowJson, { deployType: 'flows' })
      } catch (e) {
        console.error(e)
        message.error('推送 Node-RED 失败')
      }
    },

    // LLM
    openLLMCreation () {
      this.modelModalVisible = true
    }
  }
}
</script>

<style scoped>
.ant-form-item {
  margin-bottom: 12px;
}

.ant-table-row {
  cursor: pointer;
}

.ant-card-body {
  height: calc(100% - 57px);
  padding: 24px;
}

.ant-table-wrapper {
  height: 100%;
}

@media (max-width: 768px) {
  .ant-col-12 {
    width: 100% !important;
    margin-bottom: 16px;
  }
}
</style>
