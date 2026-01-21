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
              <span class="table-page-search-submitButtons" :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
                <a-button type="primary" @click="filterData">查询</a-button>
                <a-button style="margin-left: 8px" @click="resetQueryParam">重置</a-button>
                <a-button type="primary" style="margin-left: 8px" @click="showAddModal">
                  <a-icon type="plus" />
                  新增社会资源
                </a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <a-table
        :columns="socialColumns"
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
      title="新增社会资源"
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
                    message: '请输入正确的API格式，如：api/socialservice'
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
            placeholder="请输入 JSON 格式参数"
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
      <p>确定要删除该资源吗？</p>
    </a-modal>
  </page-header-wrapper>
</template>

<script>
import axios from 'axios'

export default {
  name: 'SocialResource',

  data () {
    return {
      queryId: '',
      queryResourceType: '0',
      loading: false,
      resourceTypes: [],
      // 表格列定义：包含输入和输出
      socialColumns: [
        { title: '资源编号', dataIndex: 'resourceId', key: 'resourceId', width: 120 },
        { title: '资源类型', dataIndex: 'resourceType', key: 'resourceType', width: 120 },
        { title: '资源描述', dataIndex: 'description', key: 'description', width: 200, ellipsis: true },
        { title: '访问地址', dataIndex: 'url', key: 'url', width: 150 },
        { title: '输入', dataIndex: 'input', key: 'input', width: 150, ellipsis: true },
        { title: '输出', dataIndex: 'output', key: 'output', width: 150, ellipsis: true },
        { title: '操作', key: 'action', width: 100, scopedSlots: { customRender: 'action' } }
      ],
      socialData: [],
      filteredData: [],

      addModalVisible: false,
      addSubmitLoading: false,
      addForm: this.$form.createForm(this),

      selectedResource: null,
      isDeleteModalVisible: false,

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
        const response = await axios.get(`${baseUrl}/api/socialResources/project/${id}`)

        if (Array.isArray(response.data)) {
          this.socialData = response.data
          this.filteredData = [...response.data]
          this.extractResourceTypes()
        } else {
          this.socialData = []
          this.filteredData = []
        }
      } catch (error) {
        console.error('获取数据失败:', error)
        this.$message.error('获取数据失败')
      } finally {
        this.loading = false
      }
    },

    extractResourceTypes () {
      const types = new Set()
      this.socialData.forEach(item => {
        if (item.resourceType && item.resourceType.trim()) {
          types.add(item.resourceType.trim())
        }
      })
      this.resourceTypes = Array.from(types).sort()
    },

    filterData () {
      this.filteredData = this.socialData.filter(item => {
        const matchesId = !this.queryId || (item.resourceId && item.resourceId.toString().includes(this.queryId))
        const matchesResourceType = this.queryResourceType === '0' || (item.resourceType && item.resourceType === this.queryResourceType)
        return matchesId && matchesResourceType
      })
    },

    resetQueryParam () {
      this.queryId = ''
      this.queryResourceType = '0'
      this.filteredData = [...this.socialData]
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
        await axios.delete(`${baseUrl}/api/socialResources/delete/${this.selectedResource.id}`)

        this.isDeleteModalVisible = false
        this.selectedResource = null
        this.$message.success('删除成功')
        this.fetchData(1) // 刷新
      } catch (error) {
        console.error('删除出错:', error)
        this.$message.error('删除失败')
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

            const payload = {
              resourceId: values.resourceId.trim(),
              resourceType: values.resourceType.trim(),
              description: values.description.trim(),
              url: values.url.trim(),
              input: values.input ? values.input.trim() : '',
              output: values.output ? values.output.trim() : '',
              projectId: 1 // 关键参数
            }

            await axios.post(`${baseUrl}/api/socialResources/add`, payload)

            this.$message.success('新增成功')
            this.addModalVisible = false
            this.addForm.resetFields()
            this.fetchData(1) // 刷新
          } catch (error) {
            console.error('新增失败:', error)
            this.$message.error('新增失败，请检查资源编号是否重复')
          } finally {
            this.addSubmitLoading = false
          }
        }
      })
    }
  },

  created () {
    this.fetchData(1)
  }
}
</script>

<style scoped>
/* 关键修改：改用 margin-bottom，允许 TextArea 撑开高度 */
.a-form-item {
  margin-bottom: 24px;
}

.table-page-search-wrapper {
  margin-bottom: 16px;
}

.table-page-search-submitButtons {
  display: flex;
  align-items: center;
}
</style>
