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
                <a-select v-model="queryStatus" placeholder="请选择资源使用状态" default-value="0">
                  <a-select-option value="0">全部</a-select-option>
                  <a-select-option value="正常">正常</a-select-option>
                  <a-select-option value="异常">异常</a-select-option>
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
      >
        <template #action="{ record }">
          <a @click="showDetails(record)">详情</a>
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
      queryStatus: '0',
      socialColumns: [
        { title: '资源编号', dataIndex: 'resourceId', width: 100 },
        { title: '资源类型', dataIndex: 'resourceType', width: 150 },
        { title: '资源描述', dataIndex: 'description', width: 200 },
        { title: '资源状态', dataIndex: 'state', width: 100 },
        { title: '更新时间', dataIndex: 'lastUpdateTime', width: 200 },
        { title: '', key: 'action', scopedSlots: { customRender: 'action' }, width: 100 } // 添加动作列
      ],
      socialData: [],
      filteredData: []
    }
  },
  methods: {
    async fetchData (id) {
      try {
        const response = await axios.get(`http://localhost:8080/api/socialResources/project/${id}`)
        console.log('API response data:', response.data)
        this.socialData = response.data
        this.filteredData = response.data
      } catch (error) {
        console.error('获取数据时发生错误:', error)
      }
    },
    filterData () {
      this.filteredData = this.socialData.filter(item => {
        const matchesId = !this.queryId || (item.resourceId && item.resourceId.includes(this.queryId))
        const matchesStatus = this.queryStatus === '0' || (item.state && item.state === this.queryStatus)
        return matchesId && matchesStatus
      })
      if (this.filteredData.length === 0) {
        this.filteredData = []
      }
    },
    resetQueryParam () {
      this.queryId = ''
      this.queryStatus = '0'
      this.filteredData = this.socialData
    },
    showDetails (record) {
      // 这里添加显示详情的逻辑
      console.log('显示详情:', record)
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
