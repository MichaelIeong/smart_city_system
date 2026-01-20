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
                <a-select v-model="queryResourceType" placeholder="请选择资源类型" default-value="0" :style="{ height: '40px' }">
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
                rules: [
                  { required: false, message: '请输入输入参数描述' }
                ]
              }
            ]"
            placeholder="请输入JSON 格式参数"
            :rows="3"
          />
        </a-form-item>

        <a-form-item label="输出参数">
          <a-textarea
            v-decorator="[
              'output',
              {
                rules: [
                  { required: false, message: '请输入输出参数描述' }
                ]
              }
            ]"
            placeholder="请输入JSON 格式参数"
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
      resourceTypes: [], // 存储从数据库获取的资源类型
      advanced: false,
      // 修改：添加 input 和 output 列
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

      // 新增功能相关数据
      addModalVisible: false,
      addSubmitLoading: false,
      addForm: this.$form.createForm(this),

      // 新增资源临时存储对象
      // 修改：添加 input 和 output 字段
      newResource: {
        id: '',
        resourceId: '',
        resourceType: '',
        description: '',
        url: '',
        input: '',
        output: ''
      },

      // 删除功能相关数据
      selectedResource: null, // 选中要删除的资源
      isDeleteModalVisible: false, // 删除确认弹窗显示状态

      // 分页配置
      paginationConfig: {
        pageSize: 10,
        showSizeChanger: true,
        showQuickJumper: true,
        showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条数据`
      }
    }
  },
  methods: {
    async fetchData (id) {
      try {
        this.loading = true
        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
        const response = await axios.get(`${baseUrl}/api/cyberResources/project/${id}`)
        console.log('API response data:', response.data)

        // 确保数据格式正确
        if (Array.isArray(response.data)) {
          this.cyberData = response.data
          this.filteredData = [...response.data] // 使用展开运算符创建新数组

          // 提取所有不重复的资源类型
          this.extractResourceTypes()

          console.log('表格数据已更新，共', response.data.length, '条记录')
          console.log('资源类型列表:', this.resourceTypes)
        } else {
          console.warn('API返回的数据不是数组格式:', response.data)
          this.cyberData = []
          this.filteredData = []
          this.resourceTypes = []
        }
      } catch (error) {
        console.error('获取数据时发生错误:', error)
        this.$message.error('获取数据失败，请检查网络连接或联系管理员')
        this.cyberData = []
        this.filteredData = []
        this.resourceTypes = []
      } finally {
        this.loading = false
      }
    },

    // 提取数据中的所有资源类型
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
      this.filteredData = [...this.cyberData] // 使用展开运算符创建新数组
      this.$message.success('查询条件已重置')
    },

    // 删除设备实例
    deleteResourceInstance (record) {
      // 保存要删除的资源记录
      this.selectedResource = record
      // 显示删除确认弹窗
      this.isDeleteModalVisible = true
    },

    async confirmDeleteResource () {
      try {
        if (!this.selectedResource || !this.selectedResource.id) {
          this.$message.error('未找到资源ID，无法删除')
          return
        }

        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

        // 调用后端删除接口
        await axios.delete(`${baseUrl}/api/cyberResources/delete/${this.selectedResource.id}`)

        // 关闭弹窗
        this.isDeleteModalVisible = false
        this.selectedResource = null

        this.$message.success('删除成功')

        // 重新拉取数据
        this.fetchData(1)
      } catch (error) {
        console.error('删除资源时出错：', error)
        this.$message.error('删除失败，请稍后重试！')
      }
    },

    // 取消删除
    cancelDeleteResource () {
      this.isDeleteModalVisible = false
      this.selectedResource = null
    },

    // 修复后的删除操作代码
    deleteResource (record) {
      this.$confirm({
        title: '确认删除',
        content: '确定要删除该资源吗？',
        okText: '确定',
        cancelText: '取消',
        okType: 'danger',
        onOk: () => {
          // 使用 filter 方法创建新数组，Vue 能够检测到变化并更新
          this.cyberData = this.cyberData.filter(item => item.id !== record.id)
          this.filteredData = this.filteredData.filter(item => item.id !== record.id)

          // 重新提取资源类型，确保筛选器列表是最新的
          this.extractResourceTypes()

          this.$message.success('删除成功')
        },
        onCancel: () => {
          console.log('取消删除')
        }
      })
    },

    // 新增资源实例
    addResourceInstance () {
      // 将新增的资源实例数据添加到表格中
      this.cyberData.push({
        id: this.newResource.id,
        resourceId: this.newResource.resourceId,
        resourceType: this.newResource.resourceType,
        description: this.newResource.description,
        url: this.newResource.url,
        // 修改：添加 input 和 output
        input: this.newResource.input,
        output: this.newResource.output
      })
      // 清空输入框
      this.newResource = {
        id: '',
        resourceId: '',
        resourceType: '',
        description: '',
        url: '',
        input: '',
        output: ''
      }
    },

    // 新增功能相关方法
    showAddModal () {
      this.addModalVisible = true
      // 重置表单
      this.$nextTick(() => {
        this.addForm.resetFields()
      })
    },

    handleAddCancel () {
      this.addModalVisible = false
      this.addForm.resetFields()
    },

    async handleAddSubmit () {
      this.addForm.validateFields(async (err, values) => {
        if (!err) {
          this.addSubmitLoading = true
          try {
            const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

            // 构造发送给后端的对象
            const payload = {
              resourceId: values.resourceId.trim(),
              resourceType: values.resourceType.trim(),
              description: values.description.trim(),
              url: values.url.trim(),
              // 传递新增的 input 和 output
              input: values.input ? values.input.trim() : '',
              output: values.output ? values.output.trim() : '',
              projectId: 1
            }

            // 发送 POST 请求给后端
            await axios.post(`${baseUrl}/api/cyberResources/add`, payload)

            this.$message.success('新增信息资源成功')

            // 关闭弹窗
            this.addModalVisible = false
            this.addForm.resetFields()

            // 重新从后端获取最新列表，而不是手动 push
            this.fetchData(1)
          } catch (error) {
            console.error('新增失败:', error)
            this.$message.error('新增失败，请检查后端服务')
          } finally {
            this.addSubmitLoading = false
          }
        }
      })
    }
  },

  created () {
    const projectId = '1'
    this.fetchData(projectId)
  }
}
</script>

<style scoped>
.a-form-item {
  margin-bottom: 24px; /* 稍微增加间距以适应 TextArea */
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
  font-size: 14px !important;
  line-height: 1.5 !important;
}

:global(.ant-modal-confirm .ant-modal-confirm-btns) {
  margin-top: 24px !important;
}

:global(.ant-modal-confirm .ant-btn) {
  border-radius: 4px !important;
  height: 32px !important;
  padding: 0 16px !important;
  font-size: 14px !important;
}
</style>
