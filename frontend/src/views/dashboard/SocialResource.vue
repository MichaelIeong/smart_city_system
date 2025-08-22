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
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <a-table
        :columns="socialColumns"
        :dataSource="filteredData"
        row-key="id"
        :scroll="{ y: 300 }"
        :loading="loading"
      >
        <template #action="{ record }">
          <a @click="deleteRecord(record)" style="color: #ff4d4f;">删除</a>
        </template>
      </a-table>
    </a-card>
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
      filteredData: []
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
          this.filteredData = response.data

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
      this.filteredData = this.socialData
      this.$message.success('查询条件已重置')
    },

    showDetails (record) {
      // 显示详情的逻辑
      console.log('显示详情:', record)
      this.$modal.info({
        title: '资源详情',
        width: 600,
        content: h => h('div', [
          h('p', `资源编号: ${record.resourceId || '-'}`),
          h('p', `资源类型: ${record.resourceType || '-'}`),
          h('p', `资源描述: ${record.description || '-'}`),
          h('p', `状态: ${record.state || '-'}`),
          h('p', `最后更新时间: ${record.lastUpdateTime || '-'}`),
          h('p', [
            '访问地址: ',
            record.url ? h('a', {
              attrs: { href: record.url, target: '_blank' },
              style: { color: '#1890ff' }
            }, record.url) : h('span', '-')
          ])
        ])
      })
    },

    deleteRecord (record) {
      const self = this
      this.$confirm({
        title: '确认删除',
        content: `确定要删除资源编号为 ${record.resourceId} 的记录吗？`,
        okText: '确定',
        okType: 'danger',
        cancelText: '取消',
        onOk () {
          // 从原始数据中删除
          self.socialData = self.socialData.filter(item => item.id !== record.id)
          // 从过滤后的数据中删除
          self.filteredData = self.filteredData.filter(item => item.id !== record.id)
          self.$message.success('删除成功')

          // 如果需要调用后端删除API，可以在这里添加
          // self.deleteFromServer(record.id)
        },
        onCancel () {
          console.log('取消删除')
        }
      })
    },

    // 可选：如果需要调用后端删除API
    async deleteFromServer (id) {
      try {
        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
        await axios.delete(`${baseUrl}/api/socialResources/${id}`)
        console.log('服务器删除成功')
      } catch (error) {
        console.error('服务器删除失败:', error)
        this.$message.error('服务器删除失败，请重试')
      }
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
</style>
