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
            :rows="4"
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
      </a-form>
    </a-modal>

    <a-modal
      v-model="isDeleteModalVisible"
      title="确认删除"
      :width="400"
      @ok="confirmDeleteDevice"
      @cancel="cancelDeleteDevice"
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
  name: 'SocialResource',

  data () {
    return {
      queryId: '',
      queryResourceType: '0',
      loading: false,
      resourceTypes: [], // 存储从数据库获取的资源类型
      socialColumns: [
        {
          title: '资源编号',
          dataIndex: 'resourceId',
          key: 'resourceId',
          width: 120
        },
        {
          title: '资源类型',
          dataIndex: 'resourceType',
          key: 'resourceType',
          width: 120
        },
        {
          title: '资源描述',
          dataIndex: 'description',
          key: 'description',
          width: 200
        },
        {
          title: '访问地址',
          dataIndex: 'url',
          key: 'url',
          width: 200
        },
        {
          title: '操作',
          key: 'action',
          width: 80,
          scopedSlots: { customRender: 'action' }
        }
      ],
      socialData: [],
      filteredData: [],

      // 新增功能相关数据
      addModalVisible: false,
      addSubmitLoading: false,
      addForm: this.$form.createForm(this),

      // 删除功能相关数据 (新增)
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
        const response = await axios.get(`${baseUrl}/api/socialResources/project/${id}`)
        console.log('API response data:', response.data)

        // 确保数据格式正确
        if (Array.isArray(response.data)) {
          this.socialData = response.data
          this.filteredData = [...response.data] // 使用展开运算符创建新数组

          // 提取所有不重复的资源类型
          this.extractResourceTypes()

          console.log('表格数据已更新，共', response.data.length, '条记录')
          console.log('资源类型列表:', this.resourceTypes)
        } else {
          console.warn('API返回的数据不是数组格式:', response.data)
          this.socialData = []
          this.filteredData = []
          this.resourceTypes = []
        }
      } catch (error) {
        console.error('获取数据时发生错误:', error)
        this.$message.error('获取数据失败，请检查网络连接或联系管理员')
        this.socialData = []
        this.filteredData = []
        this.resourceTypes = []
      } finally {
        this.loading = false
      }
    },

    // 提取数据中的所有资源类型
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

      if (this.filteredData.length === 0 && this.socialData.length > 0) {
        this.$message.info('未找到匹配的数据')
      }
    },

    resetQueryParam () {
      this.queryId = ''
      this.queryResourceType = '0'
      this.filteredData = [...this.socialData]
      this.$message.success('查询条件已重置')
    },

    // 删除设备实例 (新增)
    deleteResourceInstance (record) {
      // 保存要删除的资源记录
      this.selectedResource = record
      // 显示删除确认弹窗
      this.isDeleteModalVisible = true
    },

    // 确认删除设备 (新增)
    async confirmDeleteDevice () {
      try {
        // 从本地数据中删除选中的设备实例
        this.socialData = this.socialData.filter(
          item => item.id !== this.selectedResource.id
        )
        this.filteredData = this.filteredData.filter(
          item => item.id !== this.selectedResource.id
        )

        // 清空选中状态
        this.selectedResource = null

        // 关闭删除确认弹窗
        this.isDeleteModalVisible = false

        // 重新提取资源类型
        this.extractResourceTypes()

        // 显示成功消息
        this.$message.success('设备实例删除成功！')
      } catch (error) {
        console.error('删除设备实例时出错：', error)
        this.$message.error('删除失败，请稍后重试！')
      }
    },

    // 取消删除 (新增)
    cancelDeleteDevice () {
      this.isDeleteModalVisible = false
      this.selectedResource = null
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

    handleAddSubmit () {
      this.addForm.validateFields((err, values) => {
        if (!err) {
          this.addSubmitLoading = true

          // 模拟提交延迟，提升用户体验
          setTimeout(() => {
            // 检查资源编号是否已存在
            const existingResource = this.socialData.find(item =>
              item.resourceId === values.resourceId.trim()
            )

            if (existingResource) {
              this.$message.error('资源编号已存在，请使用其他编号')
              this.addSubmitLoading = false
              return
            }

            // 生成新的ID（使用时间戳确保唯一性）
            const newId = Date.now()

            // 构造新资源数据
            const newResource = {
              id: newId,
              resourceId: values.resourceId.trim(),
              resourceType: values.resourceType.trim(),
              description: values.description.trim(),
              url: values.url.trim()
            }

            // 添加到本地数据
            this.socialData.push(newResource)
            this.filteredData = [...this.socialData] // 刷新过滤后的数据

            // 重新提取资源类型
            this.extractResourceTypes()

            // 关闭对话框并重置表单
            this.addModalVisible = false
            this.addForm.resetFields()
            this.addSubmitLoading = false

            this.$message.success('新增社会资源成功')
          }, 500) // 500ms延迟，模拟网络请求
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
  height: 50px; /* 调整表单项的高度 */
}

.table-page-search-wrapper {
  margin-bottom: 16px;
}

.table-page-search-submitButtons {
  display: flex;
  align-items: center;
}

/* 自定义分页样式 */
:global(.ant-table-pagination.ant-pagination) {
  margin: 16px 0;
  text-align: right;
}

:global(.ant-pagination-total-text) {
  display: inline-block;
  height: 32px;
  margin-right: 8px;
  line-height: 32px;
  vertical-align: top;
  color: rgba(0, 0, 0, 0.85);
}

:global(.ant-pagination-options-size-changer.ant-select) {
  margin-left: 8px;
}

:global(.ant-pagination-options-size-changer .ant-select-selector) {
  padding: 0 8px;
}

:global(.ant-pagination-item) {
  margin-right: 8px;
}

:global(.ant-pagination-prev),
:global(.ant-pagination-next) {
  margin-right: 8px;
}
</style>
