<template>
  <page-header-wrapper>
    <a-card bordered :style="{ borderRadius: '8px' }">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="资源编号">
                <a-input v-model="queryId" placeholder="请输入待查找资源编号" />
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="资源类型">
                <a-select v-model="queryResourceType" placeholder="请选择资源类型" default-value="0">
                  <a-select-option value="0">全部</a-select-option>
                  <a-select-option
                    v-for="type in resourceTypes"
                    :key="type"
                    :value="type"
                  >
                    {{ type }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="!advanced && 8 || 24" :sm="24">
              <span class="table-page-search-submitButtons" :style="advanced && { float: 'right', overflow: 'hidden' } || {}">
                <a-button type="primary" @click="filterData">查询</a-button>
                <a-button style="margin-left: 8px" @click="resetQueryParam">重置</a-button>
                <a-button type="primary" style="margin-left: 8px" @click="showAddModal">
                  <a-icon type="plus" />
                  新增信息资源
                </a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <a-table
        :columns="cyberColumns"
        :dataSource="filteredData"
        :rowKey="record => record.id"
        :scroll="{ y: 300 }"
        :loading="loading"
        :pagination="paginationConfig"
      >
        <span slot="action" slot-scope="text, record">
          <a @click="deleteResourceInstance(record)" style="color: #ff4d4f;">删除</a>
        </span>
      </a-table>
    </a-card>

    <a-modal
      v-model="addModalVisible"
      title="新增信息资源"
      :width="600"
      @ok="handleAddSubmit"
      @cancel="handleAddCancel"
      :confirmLoading="addSubmitLoading"
    >
      <a-form :form="addForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="资源编号">
          <a-input
            v-decorator="[
              'resourceId',
              {
                rules: [
                  { required: true, message: '请输入资源编号' },
                  { max: 50, message: '资源编号不能超过50个字符' }
                ]
              }
            ]"
            placeholder="请输入资源编号"
          />
        </a-form-item>

        <a-form-item label="资源类型">
          <a-input
            v-decorator="[
              'resourceType',
              {
                rules: [
                  { required: true, message: '请输入资源类型' },
                  { max: 30, message: '资源类型不能超过30个字符' }
                ]
              }
            ]"
            placeholder="请输入资源类型"
          />
        </a-form-item>

        <a-form-item label="资源描述">
          <a-textarea
            v-decorator="[
              'description',
              {
                rules: [
                  { required: true, message: '请输入资源描述' },
                  { max: 500, message: '资源描述不能超过500个字符' }
                ]
              }
            ]"
            placeholder="请输入资源描述"
            :rows="3"
          />
        </a-form-item>

        <a-form-item label="访问地址">
          <a-input
            v-decorator="[
              'url',
              {
                rules: [
                  { required: true, message: '请输入访问地址' },
                  { max: 200, message: '访问地址不能超过200个字符' },
                  {
                    pattern: /^api\/[a-zA-Z0-9\/_-]+$/,
                    message: '请输入正确的API格式，如：api/meetingroom'
                  }
                ]
              }
            ]"
            placeholder="请输入访问地址，格式：api/xxxxx"
          />
        </a-form-item>

        <a-form-item label="输入参数">
          <a-textarea
            v-decorator="[
              'input',
              {
                rules: [{ required: false, message: '请输入输入参数描述' }]
              }
            ]"
            placeholder="请输入 JSON 格式参数"
            :rows="3"
          />
        </a-form-item>

        <a-form-item label="输出参数">
          <a-textarea
            v-decorator="[
              'output',
              {
                rules: [{ required: false, message: '请输入输出参数描述' }]
              }
            ]"
            placeholder="请输入 JSON 格式参数"
            :rows="3"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model="isDeleteModalVisible"
      title="确认删除"
      :width="400"
      @ok="confirmDeleteResource"
      @cancel="cancelDeleteResource"
      okText="确定"
      cancelText="取消"
      okType="danger"
    >
      <p>确定要删除该资源实例吗？</p>
    </a-modal>
  </page-header-wrapper>
</template>

<script>
import axios from 'axios'

export default {
  name: 'CyberResources',
  data () {
    return {
      queryId: '',
      queryResourceType: '0',
      loading: false,
      resourceTypes: [],
      // ★★★ 存储当前项目ID，默认为1 ★★★
      currentProjectId: '1',
      currentScene: '',

      // 表格列定义
      cyberColumns: [
        { title: '资源编号', dataIndex: 'resourceId', key: 'resourceId', width: 120 },
        { title: '资源类型', dataIndex: 'resourceType', key: 'resourceType', width: 150 },
        { title: '资源描述', dataIndex: 'description', key: 'description', width: 200, ellipsis: true },
        { title: '访问地址', dataIndex: 'url', key: 'url', width: 150 },
        { title: '输入', dataIndex: 'input', key: 'input', width: 150, ellipsis: true },
        { title: '输出', dataIndex: 'output', key: 'output', width: 150, ellipsis: true },
        { title: '操作', key: 'action', width: 100, scopedSlots: { customRender: 'action' } }
      ],
      cyberData: [],
      filteredData: [],

      // 新增功能
      addModalVisible: false,
      addSubmitLoading: false,
      addForm: this.$form.createForm(this),

      // 删除功能
      selectedResource: null,
      isDeleteModalVisible: false,

      // 分页配置
      paginationConfig: {
        pageSize: 10,
        showSizeChanger: true,
        showQuickJumper: true,
        showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条数据`
      }
    }
  },

  mounted () {
    // 1. 同步场景信息 (保持和物理资源一致)
    let scene = this.$route.query.scene
    if (!scene) {
      scene = localStorage.getItem('current_scene_type')
    }
    if (!scene) {
      scene = 'F-city'
    }
    this.currentScene = scene

    // 路由同步
    if (this.$route.query.scene !== scene) {
      this.$router.replace({
        path: this.$route.path,
        query: { ...this.$route.query, scene: scene }
      })
    }

    // 2. ★★★ 获取 Project ID (从本地存储) ★★★
    const storedProjectId = localStorage.getItem('project_id')
    if (storedProjectId) {
      this.currentProjectId = storedProjectId
    }

    console.log(`初始化信息资源: Scene=${this.currentScene}, ProjectId=${this.currentProjectId}`)

    // 3. 加载数据
    this.fetchData()
  },

  methods: {
    async fetchData () {
      try {
        this.loading = true
        // 使用当前 Project ID
        const targetId = this.currentProjectId
        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

        // ★★★ 核心：调用 cyberResources 接口 ★★★
        const response = await axios.get(`${baseUrl}/api/cyberResources/project/${targetId}`)

        if (Array.isArray(response.data)) {
          this.cyberData = response.data
          this.filteredData = [...response.data]
          this.extractResourceTypes()
          console.log(`成功获取信息资源，共 ${response.data.length} 条`)
        } else {
          this.cyberData = []
          this.filteredData = []
        }
      } catch (error) {
        console.error('获取数据时发生错误:', error)
        this.$message.error('获取数据失败，请检查网络连接')
        this.cyberData = []
        this.filteredData = []
      } finally {
        this.loading = false
      }
    },

    extractResourceTypes () {
      const types = new Set()
      this.cyberData.forEach(item => {
        if (item.resourceType && item.resourceType.trim()) {
          types.add(item.resourceType.trim())
        }
      })
      this.resourceTypes = Array.from(types).sort()
    },

    filterData () {
      this.filteredData = this.cyberData.filter(item => {
        const matchesId = !this.queryId || (item.resourceId && item.resourceId.toString().includes(this.queryId))
        const matchesResourceType = this.queryResourceType === '0' || (item.resourceType && item.resourceType === this.queryResourceType)
        return matchesId && matchesResourceType
      })
      if (this.filteredData.length === 0 && this.cyberData.length > 0) {
        this.$message.info('未找到匹配的数据')
      }
    },

    resetQueryParam () {
      this.queryId = ''
      this.queryResourceType = '0'
      this.filteredData = [...this.cyberData]
      this.$message.success('查询条件已重置')
    },

    deleteResourceInstance (record) {
      this.selectedResource = record
      this.isDeleteModalVisible = true
    },

    async confirmDeleteResource () {
      try {
        if (!this.selectedResource || !this.selectedResource.id) return

        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

        // 调用删除接口
        await axios.delete(`${baseUrl}/api/cyberResources/delete/${this.selectedResource.id}`)

        this.selectedResource = null
        this.isDeleteModalVisible = false
        this.$message.success('删除成功')

        // 刷新列表
        this.fetchData()
      } catch (error) {
        console.error('删除实例时出错：', error)
        this.$message.error('删除失败，请稍后重试！')
      }
    },

    cancelDeleteResource () {
      this.isDeleteModalVisible = false
      this.selectedResource = null
    },

    showAddModal () {
      this.addModalVisible = true
      this.$nextTick(() => {
        this.addForm.resetFields()
      })
    },

    handleAddCancel () {
      this.addModalVisible = false
      this.addForm.resetFields()
    },

    handleAddSubmit () {
      this.addForm.validateFields(async (err, values) => {
        if (!err) {
          this.addSubmitLoading = true
          try {
            const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

            // 构造对象
            const payload = {
              resourceId: values.resourceId.trim(),
              resourceType: values.resourceType.trim(),
              description: values.description.trim(),
              url: values.url.trim(),
              input: values.input ? values.input.trim() : '',
              output: values.output ? values.output.trim() : '',
              // ★★★ 关键：传入当前 Project ID ★★★
              projectId: this.currentProjectId
            }

            // 调用新增接口
            await axios.post(`${baseUrl}/api/cyberResources/add`, payload)

            this.$message.success('新增信息资源成功')
            this.addModalVisible = false
            this.addForm.resetFields()

            // 刷新列表
            this.fetchData()
          } catch (error) {
            console.error('新增失败:', error)
            this.$message.error('新增失败，请检查资源编号是否重复')
          } finally {
            this.addSubmitLoading = false
          }
        }
      })
    }
  }
}
</script>

<style scoped>
/* CSS 修复：使用 margin-bottom 替代固定 height，确保 TextArea 能撑开 */
.a-form-item {
  margin-bottom: 24px;
}

.table-page-search-wrapper {
  margin-bottom: 16px;
}

.table-page-search-submitButtons {
  display: inline-block;
}

/* 自定义确认对话框样式 */
:global(.ant-modal-confirm) {
  border-radius: 8px !important;
}
:global(.ant-modal-confirm .ant-modal-header) {
  border-bottom: 1px solid #f0f0f0 !important;
  padding: 16px 24px !important;
}
:global(.ant-modal-confirm .ant-modal-body) {
  padding: 24px !important;
}
:global(.ant-modal-confirm .ant-btn) {
  border-radius: 4px !important;
  height: 32px !important;
  padding: 0 16px !important;
}
</style>
