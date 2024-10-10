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
              <a-form-item label="使用状态">
                <a-select v-model="queryStatus" placeholder="请选择资源使用状态" default-value="0" :style="{ height: '40px' }">
                  <a-select-option value="0">全部</a-select-option>
                  <a-select-option value="正常">正常</a-select-option>
                  <a-select-option value="异常">异常</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>

            <a-col :md="!advanced && 8 || 24" :sm="24">
              <span class="table-page-search-submitButtons" :style="advanced && { float: 'right', overflow: 'hidden' } || {}">
                <a-button type="primary" @click="filterData">查询</a-button>
                <a-button style="margin-left: 8px" @click="resetQueryParam">重置</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <a-table
        :columns="cyberColumns"
        :dataSource="filteredData"
        row-key="id"
        :scroll="{ y: 300 }"
      />

    </a-card>
  </page-header-wrapper>
</template>

<script>
import axios from 'axios'

export default {
  name: 'CyberResources',
  data () {
    return {
      queryId: '',
      queryStatus: '0',
      advanced: false,
      mdl: null,
      cyberColumns: [
        { title: '资源编号', dataIndex: 'resourceId', width: 100 },
        { title: '资源类型', dataIndex: 'resourceType', width: 150 },
        { title: '资源描述', dataIndex: 'description', width: 200 },
        { title: '资源状态', dataIndex: 'state', width: 100 },
        { title: '更新时间', dataIndex: 'lastUpdateTime', width: 200 }
      ],
      cyberData: [],
      filteredData: [],
      pagination: { // 分页相关数据
        current: 1, // 当前页码
        pageSize: 10, // 每页显示多少条数据
        total: 0, // 数据总数
        showSizeChanger: true, // 是否允许选择每页显示条数
        showQuickJumper: true, // 是否允许快速跳转到某一页
        pageSizeOptions: ['10', '20', '50', '100'] // 可选的每页条数选项
      },
      selectedRowKeys: [],
      selectedRows: []
    }
  },
  methods: {
    async fetchData (id) {
      try {
        const response = await axios.get(`http://localhost:8080/api/cyberResources/project/${id}`)
        this.cyberData = response.data
        this.filteredData = response.data.slice(0, this.pagination.pageSize) // 初始化第一页数据
        this.pagination.total = response.data.length // 更新总数
      } catch (error) {
        console.error('获取数据时发生错误:', error)
      }
    },
    filterData () {
      // 过滤数据
      const filtered = this.cyberData.filter(item => {
        const matchesId = !this.queryId || (item.resourceId && item.resourceId.includes(this.queryId))
        const matchesStatus = this.queryStatus === '0' || (item.state && item.state === this.queryStatus)
        return matchesId && matchesStatus
      })

      // 更新分页后的数据
      this.pagination.total = filtered.length // 更新匹配到的数据总数
      const start = (this.pagination.current - 1) * this.pagination.pageSize
      const end = start + this.pagination.pageSize
      this.filteredData = filtered.slice(start, end) // 更新显示的当前页数据
    },
    resetQueryParam () {
      this.queryId = ''
      this.queryStatus = '0' // 重置为默认值
      this.filteredData = this.cyberData.slice(0, this.pagination.pageSize) // 重置为第一页
      this.pagination.current = 1 // 重置页码为1
    },
    handleTableChange (pagination) {
      // 处理分页切换
      this.pagination = pagination
      this.filterData() // 重新过滤并显示对应页码的数据
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
</style>
