<template>
  <page-header-wrapper>
    <a-card :bordered="false">
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
        :pagination="false"
        :scroll="{ y: 300 }"
      />

    </a-card></page-header-wrapper>
</template>

<script>
import axios from 'axios'

export default {
  name: 'TableList',

  data () {
    return {
      visible: false,
      confirmLoading: false,
      mdl: null,
      advanced: false,
      queryId: '',
      queryStatus: '',
      socialColumns: [
        { title: '资源编号', dataIndex: 'resourceId' },
        { title: '资源类型', dataIndex: 'resourceType' },
        { title: '资源描述', dataIndex: 'description' },
        { title: '资源状态', dataIndex: 'state' },
        { title: '更新时间', dataIndex: 'lastUpdateTime' }
      ],
      socialData: [],
      selectedRowKeys: [],
      selectedRows: [],
      filteredData: []
    }
  },
  computed: {
    rowSelection () {
      return {
        selectedRowKeys: this.selectedRowKeys,
        onChange: this.onSelectChange
      }
    }
  },
  methods: {
    // 定义一个方法来获取数据
    async fetchData (id) {
      try {
        const response = await axios.get(`http://localhost:8080/api/socialResources/project/${id}`)
        console.log('API response data:', response.data) // 调试信息，查看数据是否正确返回
        this.socialData = response.data // 确保将 API 返回的数据赋值给 cyberData
        this.filteredData = response.data // 初始化 filteredData 为全部数据
        console.log('cyberData after assignment:', this.socialData) // 查看数据是否成功赋值给 cyberData
      } catch (error) {
        console.error('获取数据时发生错误:', error)
      }
    },
    filterData () {
      // 过滤数据并更新 filteredData
      this.filteredData = this.socialData.filter(item => {
        // 检查资源编号是否匹配
        const matchesId = !this.queryId || (item.resourceId && item.resourceId.includes(this.queryId))
        // 检查资源状态是否匹配
        const matchesStatus = this.queryStatus === '0' || (item.state && item.state === this.queryStatus)
        // 返回匹配条件
        return matchesId && matchesStatus
      })
      // 如果没有匹配的结果，则确保 filteredData 是空数组
      if (this.filteredData.length === 0) {
        this.filteredData = []
      }
    },
    resetQueryParam () {
      this.queryId = ''
      this.queryStatus = '0' // 重置为默认值
      this.filteredData = this.socialData // 重置为全部数据
    },
    handleAdd () {
      this.mdl = null
      this.visible = true
    },
    handleEdit (record) {
      this.mdl = { ...record }
      this.visible = true
    },
    handleOk () {
      // 表单提交逻辑
    },
    handleCancel () {
      this.visible = false
    },
    onSelectChange (selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
    },
    toggleAdvanced () {
      this.advanced = !this.advanced
    }
  },
  created () {
    const projectId = '1'
    this.fetchData(projectId) // 调用方法获取数据
  }
}
</script>
