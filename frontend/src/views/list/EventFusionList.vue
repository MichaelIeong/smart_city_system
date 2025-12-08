<template>
  <page-header-wrapper>
    <div class="page-content">
      <a-row :gutter="24" style="height: calc(100vh - 250px);">
        <!-- 左侧：通用规则 -->
        <a-col :span="12">
          <a-card title="通用规则" bordered :style="{ borderRadius: '8px', height: '100%' }">
            <div class="table-page-search-wrapper" style="margin-bottom: 12px;"></div>

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
                  <a @click.stop.prevent="openApplyModal(record)">套用到可达空间</a>
                  <a-divider type="vertical" />
                  <a @click.stop.prevent="handleEdit(record)">编辑</a>
                  <a-divider type="vertical" />
                  <a @click.stop.prevent="deleteRule(record)">删除</a>
                </span>
              </a-table>
            </div>
          </a-card>
        </a-col>

        <!-- 右侧：实例（Branch） -->
        <a-col :span="12">
          <a-card :title="rightTitle" bordered :style="{ borderRadius: '8px', height: '100%' }">
            <div style="height: calc(100% - 60px); display:flex; flex-direction: column;">
              <!-- 实例筛选 -->
              <a-row :gutter="16" style="margin-bottom: 16px;">
                <a-col :md="16" :sm="24">
                  <a-select v-model="branchQuery.status" placeholder="请选择实例状态" style="width:100%;">
                    <a-select-option value="all">全部</a-select-option>
                    <a-select-option value="active">运行中</a-select-option>
                    <a-select-option value="inactive">已关闭</a-select-option>
                  </a-select>
                </a-col>
                <a-col :md="8" :sm="24">
                  <a-button type="primary" block @click="filterBranches">查询</a-button>
                </a-col>
              </a-row>

              <!-- 实例表格 -->
              <a-table
                :columns="branchColumns"
                :dataSource="filteredBranches"
                row-key="branchId"
                :pagination="false"
                :scroll="{ x: 400, y: 'calc(100vh - 540px)' }"
                style="flex:1;"
              >
                <span slot="branchName" slot-scope="text">
                  <a-tooltip :title="text">
                    <span class="one-line-ellipsis">{{ text }}</span>
                  </a-tooltip>
                </span>

                <span slot="status" slot-scope="text">
                  <a-badge
                    :status="text === 'active' ? 'processing' : 'default'"
                    :text="text === 'active' ? '运行中' : '已关闭'" />
                </span>

                <span slot="action" slot-scope="text, record">
                  <a @click.stop.prevent="toggleBranchStatus(record)">
                    {{ (record.status || 'inactive') === 'active' ? '暂停' : '执行' }}
                  </a>
                  <a-divider type="vertical" />
                  <a @click.stop.prevent="editBranch(record)">编辑</a>
                  <a-divider type="vertical" />
                  <a @click.stop.prevent="deleteBranch(record)">删除</a>
                </span></a-table>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 编辑实例（名称 + DSL + Node-RED 跳转） -->
    <a-modal
      v-model="branchModal.visible"
      title="编辑实例"
      @ok="submitBranchModal"
      @cancel="closeBranchModal"
      :confirmLoading="branchModal.loading"
    >
      <a-form :form="branchForm">
        <!-- 实例空间 -->
        <a-form-item label="实例空间" :labelCol="{span:5}" :wrapperCol="{span:19}">
          <a-input
            v-decorator="[
              'branchName',
              { initialValue: branchModal.model.branchName, rules:[{ required:true, message:'请输入实例空间'}]}
            ]"
            @pressEnter.prevent
          />
        </a-form-item>

        <!-- 规则 DSL（ruleJson） -->
        <a-form-item label="规则 DSL" :labelCol="{span:5}" :wrapperCol="{span:19}">
          <a-spin :spinning="branchModal.dslLoading">
            <a-textarea
              v-model="branchModal.ruleJson"
              :rows="10"
              :disabled="!branchModal.dslEditable"
              placeholder="点击下方『解锁 DSL 编辑』按钮后可修改，内容为 ruleJson（JSON 格式）"
            />
          </a-spin>
          <div style="margin-top: 8px; text-align: right;">
            <a-button size="small" @click="toggleDslEditable">
              {{ branchModal.dslEditable ? '锁定 DSL' : '解锁 DSL 编辑' }}
            </a-button>
          </div>
        </a-form-item>

        <!-- Node-RED 跳转 -->
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
              :value="Number(sp.id)"
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
        { title: '规则名称', dataIndex: 'ruleName', width: '30%' },
        { title: '操作', dataIndex: 'action', width: '35%', scopedSlots: { customRender: 'action' } }
      ],
      data: [],
      queryParam: { status: 'all' },
      pagination: { current: 1, pageSize: 10, total: 0 },
      selectedRowKeys: [],
      selectedRows: [],
      modelModalVisible: false,

      activeRule: null,

      // 实例
      branchColumns: [
        { title: '实例空间', dataIndex: 'branchName', width: '30%', scopedSlots: { customRender: 'branchName' } },
        { title: '状态', dataIndex: 'status', width: '25%', scopedSlots: { customRender: 'status' } },
        { title: '操作', dataIndex: 'action', width: '35%', scopedSlots: { customRender: 'action' } }
      ],
      branches: [],
      filteredBranches: [],
      branchQuery: { status: 'all' },

      // 实例弹窗（名称 + DSL）
      branchModal: {
        visible: false,
        loading: false, // 点击“确定”时 loading
        dslLoading: false, // 加载 DSL 时 loading
        dslEditable: false, // 是否允许编辑 DSL（需要点按钮解锁）
        model: { branchId: null, branchName: '' },
        ruleJson: '' // 当前实例的 DSL（ruleJson）
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
      if (!this.activeRule) return '实例（请选择左侧主干）'
      return `实例 - ${this.activeRule.ruleName}`
    }
  },
  created () {
    this.fetchSpaceMap()
    this.refreshTable()
  },
  methods: {
    _nrBase () {
      if (!NODE_RED_URL) {
        message.error('未配置 NODE_RED_URL')
        throw new Error('NODE_RED_URL missing')
      }
      return String(NODE_RED_URL).replace(/\/$/, '')
    },
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
    async pushFlowAndOpen (flowJson, { deployType = 'flows', branchId = null } = {}) {
      const base = this._nrBase()
      let normalized = this._normalizeFlow(flowJson)

      // ===== 重点：如果带了 branchId，就把它写进 Publish 节点的配置里 =====
      if (branchId != null) {
        try {
          if (Array.isArray(normalized)) {
            normalized = normalized.map(node => {
              if (node && node.type === 'Publish') {
                // 写成字符串，跟 Node-RED 默认配置类型一致
                return { ...node, branchId: String(branchId) }
              }
              return node
            })
          } else if (normalized && typeof normalized === 'object') {
            Object.keys(normalized).forEach(key => {
              const node = normalized[key]
              if (node && node.type === 'Publish') {
                normalized[key] = { ...node, branchId: String(branchId) }
              }
            })
          }
        } catch (e) {
          console.error('注入 branchId 到 flowJson 失败', e)
        }
      }

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
    async handleAdd () {
      if (!NODE_RED_URL) {
        message.error('未配置 NODE_RED_URL')
        return
      }
      try {
        const hide = message.loading('正在清空 Node-RED...', 0)
        await this.clearNodeRed()
        hide()
        message.success('Node-RED 已清空')

        const params = new URLSearchParams({ source: 'frontend', action: 'create' })
        window.open(`${NODE_RED_URL}?${params.toString()}`, '_blank')
      } catch (e) {
        console.error(e)
        message.error(e?.message || '清空 Node-RED 失败')
      }
    },

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
          console.debug('[executableSpaces]', list)

          if (list.length > 0 && typeof list[0] === 'number') {
            this.applyModal.spaces = list.map(id => ({
              id,
              name: this.spaceMap[id] || `空间 #${id}`
            }))
          } else {
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
        const ids = this.applyModal.selectedSpaceIds.map(x => Number(x)).filter(x => !Number.isNaN(x))

        const res = await axios.post(
          `${BASE}/api/fusion/rules/${ruleId}/applyToExecutableSpaces`,
          { spaceIds: ids },
          { params: { activate: false } }
        )

        const created = res?.data?.created || []
        const errors = res?.data?.errors || []
        const okCount = res?.data?.createdBranches ?? created.length

        if (okCount > 0) {
          message.success(`已套用：新建 ${okCount} 个实例`)
        }
        if (errors.length > 0) {
          Modal.error({
            title: '部分空间套用失败',
            width: 700,
            content: (
              <div style="max-height:40vh; overflow:auto;">
                <ul>
                  {errors.map((e, i) => (
                    <li key={i}>spaceId={e.spaceId}，错误：{String(e.error)}</li>
                  ))}
                </ul>
              </div>
            )
          })
        }

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
    async clearNodeRed () {
      const base = this._nrBase()
      const resp = await fetch(`${base}/flows`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-Node-RED-Deployment-Type': 'full' // 更彻底
        },
        body: '[]'
      })
      if (!resp.ok) {
        const text = await resp.text().catch(() => '')
        throw new Error(`清空 Node-RED 失败：HTTP ${resp.status} ${text}`)
      }
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

    // ===== 实例接口 =====
    async fetchBranches (ruleId) {
      if (!ruleId) return
      try {
        const res = await axios.get(`${BASE}/api/fusion/rules/${ruleId}/branches`)
        this.branches = (res.data || []).map(b => ({ ...b, status: b.status || 'inactive' }))
        this.filteredBranches = [...this.branches]
      } catch (e) {
        console.error(e)
        message.error('获取实例列表失败')
      }
    },
    // ===== 分支 JSON 接口（给 Node-RED 用） =====
    async fetchBranchJson (branchId) {
      if (!branchId) throw new Error('branchId 不能为空')
      const { data } = await axios.get(`${BASE}/api/fusion/branches/${branchId}/json`)
      return data || {}
    },
    filterBranches () {
      const s = this.branchQuery.status
      this.filteredBranches = (s === 'all') ? [...this.branches] : this.branches.filter(b => (b.status || 'inactive') === s)
    },

    // ===== 实例操作 =====
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
        content: `暂停实例：${record.branchName}`,
        okText: '确定',
        cancelText: '取消',
        onOk: async () => {
          try {
            await axios.put(`${BASE}/api/fusion/pauseBranch/${record.branchId}`)
            message.success('实例已暂停')
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
        title: '确认删除该实例？',
        content: `是否删除实例：${record.branchName}`,
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

    // ====== 编辑实例（名称 + DSL） ======
    editBranch (record) {
      this.branchModal.model = {
        branchId: record.branchId,
        branchName: record.branchName
      }
      this.openBranchModal()
    },
    openBranchModal () {
      this.branchModal.visible = true
      this.branchModal.loading = false
      this.branchModal.dslLoading = false
      this.branchModal.dslEditable = false
      this.branchModal.ruleJson = ''
      this.$nextTick(() => {
        this.branchForm = this.$form.createForm(this, { name: 'branchForm' })
        const { branchName } = this.branchModal.model
        this.branchForm.setFieldsValue({ branchName })
      })
      // 打开弹窗时加载该实例的 DSL
      this.loadBranchDsl(this.branchModal.model.branchId)
    },
    closeBranchModal () {
      this.branchModal.visible = false
      this.branchModal.loading = false
      this.branchModal.dslLoading = false
      this.branchModal.dslEditable = false
      this.branchModal.ruleJson = ''
    },
    // 加载某个实例的 DSL（ruleJson）
    async loadBranchDsl (branchId) {
      if (!branchId) return
      this.branchModal.dslLoading = true
      try {
        const { data } = await axios.get(`${BASE}/api/fusion/branches/${branchId}/json`)
        const raw = data?.ruleJson

        if (typeof raw === 'string') {
          const s = raw.trim()
          // 尝试格式化成缩进 JSON，方便阅读和编辑
          if (s.startsWith('{') || s.startsWith('[')) {
            try {
              const obj = JSON.parse(s)
              this.branchModal.ruleJson = JSON.stringify(obj, null, 2)
            } catch (e) {
              this.branchModal.ruleJson = raw
            }
          } else {
            this.branchModal.ruleJson = raw
          }
        } else if (raw && typeof raw === 'object') {
          this.branchModal.ruleJson = JSON.stringify(raw, null, 2)
        } else {
          this.branchModal.ruleJson = ''
        }
      } catch (e) {
        console.error(e)
        message.error('加载规则 DSL 失败')
      } finally {
        this.branchModal.dslLoading = false
      }
    },
    // 切换 DSL 是否可编辑
    toggleDslEditable () {
      this.branchModal.dslEditable = !this.branchModal.dslEditable
    },
    // 名称 +（如果解锁）DSL 一起提交
    submitBranchModal () {
      this.branchForm.validateFields(async (err, values) => {
        if (err) return
        this.branchModal.loading = true
        const branchId = this.branchModal.model.branchId

        try {
          // 1) 更新实例空间
          await axios.put(`${BASE}/api/fusion/branches/${branchId}`, {
            branchName: values.branchName
          })

          // 2) 若 DSL 已解锁，则更新 ruleJson
          if (this.branchModal.dslEditable) {
            const text = (this.branchModal.ruleJson || '').trim()
            if (!text) {
              message.error('规则 DSL 不能为空')
              this.branchModal.loading = false
              return
            }

            let parsed
            try {
              parsed = JSON.parse(text)
            } catch (e) {
              message.error('规则 DSL 不是合法 JSON，请检查格式')
              this.branchModal.loading = false
              return
            }

            await axios.put(`${BASE}/api/fusion/branches/${branchId}/json`, {
              ruleJson: parsed
            })
          }

          message.success('更新成功')
          this.closeBranchModal()
          if (this.activeRule && this.activeRule.ruleId) {
            await this.fetchBranches(this.activeRule.ruleId)
            this.filterBranches()
          }
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
        if (!branchId) {
          message.error('无效的实例 ID')
          return
        }

        let branch = this.branches.find(b => b.branchId === branchId)

        if (!branch || !branch.flowJson) {
          const { data } = await axios.get(`${BASE}/api/fusion/branches/${branchId}/json`)
          branch = { ...(branch || {}), ...(data || {}) }
        }

        if (!branch || !branch.flowJson) {
          message.error('该实例缺少 flowJson，无法推送到 Node-RED')
          return
        }

        // 1. 带着 branchId 把当前分支的 flow 推给 Node-RED（在 Publish 节点里注入 branchId）
        await this.pushFlowAndOpen(branch.flowJson, {
          deployType: 'flows',
          branchId
        })

        // 2. 打开 Node-RED 编辑器（不再依赖 URL 传 branchId）
        const base = this._nrBase() // 比如 http://127.0.0.1:1880
        window.open(base, '_blank')
      } catch (e) {
        console.error(e)
        message.error('推送 Node-RED 失败')
      }
    },

    // 合并执行/暂停
    toggleBranchStatus (record) {
      const status = record.status || 'inactive'
      if (status === 'active') {
        // 当前运行中 -> 调用暂停逻辑（里面已有确认弹窗）
        this.pauseBranch(record)
      } else {
        // 当前关闭 -> 执行
        this.executeBranch(record)
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

.one-line-ellipsis {
  display: inline-block;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (max-width: 768px) {
  .ant-col-12 {
    width: 100% !important;
    margin-bottom: 16px;
  }
}
</style>
