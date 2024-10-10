<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <!-- 资源编号 -->
            <a-col :md="8" :sm="24">
              <a-form-item label="资源编号">
                <a-input v-model="queryId" placeholder="请输入待查找资源编号" />
              </a-form-item>
            </a-col>

            <!-- 资源状态下拉框 -->
            <a-col :md="8" :sm="24">
              <a-form-item label="使用状态">
                <a-select v-model="queryStatus" placeholder="请选择资源使用状态" default-value="0">
                  <a-select-option value="0">全部</a-select-option>
                  <a-select-option value="正常">正常</a-select-option>
                  <a-select-option value="异常">异常</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>

            <!-- 查询与重置按钮 -->
            <a-col :md="!advanced && 8 || 24" :sm="24">
              <span class="table-page-search-submitButtons" :style="advanced && { float: 'right', overflow: 'hidden' } || {}">
                <a-button type="primary" @click="filterData">查询</a-button>
                <a-button style="margin-left: 8px" @click="resetQueryParam">重置</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <!-- 表格显示 -->
      <a-table
        :columns="cyberColumns"
        :dataSource="filteredData"
        :pagination="false"
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
      queryId: '', // 输入的资源编号
      queryStatus: '0', // 选择的资源状态，默认显示全部
      cyberColumns: [
        { title: '资源编号', dataIndex: 'resourceId' },
        { title: '资源类型', dataIndex: 'resourceType' },
        { title: '资源描述', dataIndex: 'description' },
        { title: '资源状态', dataIndex: 'state' },
        { title: '更新时间', dataIndex: 'lastUpdateTime' }
      ],
      cyberData: [], // 从后端获取的原始数据
      filteredData: [] // 过滤后的数据
    }
  },
  methods: {
    // 获取数据
    async fetchData (id) {
      try {
        const response = await axios.get(`http://localhost:8080/api/cyberResources/project/${id}`)
        console.log('API response data:', response.data)
        this.cyberData = response.data // 将数据存储在 cyberData 中
        this.filteredData = response.data // 初始化 filteredData 为全部数据
      } catch (error) {
        console.error('获取数据时发生错误:', error)
      }
    },

    // 查询并过滤数据
    filterData () {
      // 根据输入的资源编号和状态进行独立过滤
      this.filteredData = this.cyberData.filter(item => {
        const matchesId = !this.queryId || (item.resourceId && item.resourceId.includes(this.queryId))
        const matchesStatus = this.queryStatus === '0' || (item.state && item.state === this.queryStatus)
        return matchesId && matchesStatus
      })

      // 如果没有匹配的结果，确保显示空数组
      if (this.filteredData.length === 0) {
        this.filteredData = []
      }
    },

    // 重置查询条件
    resetQueryParam () {
      this.queryId = '' // 清空资源编号输入框
      this.queryStatus = '0' // 重置状态为 "全部"
      this.filteredData = this.cyberData // 重置为全部数据
    }
  },

  created () {
    const projectId = '1'
    this.fetchData(projectId) // 页面加载时获取数据
  }
}
</script>
