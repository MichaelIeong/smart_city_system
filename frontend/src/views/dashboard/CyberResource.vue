<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="资源编号">
                <a-input v-model="queryParam.id" placeholder="请输入待查找资源编号" />
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="使用状态">
                <a-select v-model="queryParam.status" placeholder="请选择资源使用状态" default-value="0">
                  <a-select-option value="0">全部</a-select-option>
                  <a-select-option value="1">关闭</a-select-option>
                  <a-select-option value="2">运行中</a-select-option>
                  <a-select-option value="3">已上线</a-select-option>
                  <a-select-option value="4">异常</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <template v-if="advanced">
              <a-col :md="8" :sm="24">
                <a-form-item label="更新日期">
                  <a-date-picker v-model="queryParam.date" style="width: 100%" placeholder="请选择更新日期" />
                </a-form-item>
              </a-col>
            </template>
            <a-col :md="!advanced && 8 || 24" :sm="24">
              <span class="table-page-search-submitButtons" :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
                <a-button type="primary" @click="filterData">查询</a-button>
                <a-button style="margin-left: 8px" @click="resetQueryParam">重置</a-button>
                <a @click="toggleAdvanced" style="margin-left: 8px">
                  {{ advanced ? '收起' : '展开' }}
                  <a-icon :type="advanced ? 'up' : 'down'" />
                </a>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <div class="table-operator">
        <a-button type="primary" icon="plus" @click="handleAdd">新建</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <a-menu slot="overlay">
            <a-menu-item key="1"><a-icon type="delete" />删除</a-menu-item>
          </a-menu>
          <a-button style="margin-left: 8px">
            批量操作 <a-icon type="down" />
          </a-button>
        </a-dropdown>
      </div>

      <a-table
        :columns="cyberColumns"
        :dataSource="cyberData"
        row-key="id"
        @rowClick="handleDeviceTypeDetailClick"
        :pagination="false"
        :scroll="{ y: 300 }"
      />
      <span
        slot="action"
        slot-scope="text, record">
        <template>
          <a @click="handleEdit(record)">修改</a>
        </template>
      </span>

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
      queryParam: {},
      cyberColumns: [
        { title: '资源类型', dataIndex: 'resourceType' },
        { title: '资源编号', dataIndex: 'resourceId' },
        { title: '描述', dataIndex: 'description' },
        { title: '状态', dataIndex: 'state' },
        { title: '更新时间', dataIndex: 'lastUpdateTime' }
      ],
      cyberData: [],
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
    async fetchData () {
      try {
        // 替换 `id` 为实际的资源ID
        const response = await axios.get(`/api/cyberResources/project/{id}`)

        // 假设数据返回的格式就是数组形式
        this.dataSource = response.data
      } catch (error) {
        console.error('获取数据时发生错误:', error)
      }
    },
    filterData () {
      // 根据查询条件过滤数据
      this.filteredData = this.listData.filter(item => {
        const matchesId = !this.queryParam.id || item.no.includes(this.queryParam.id)
        const matchesStatus = !this.queryParam.status || item.status === this.queryParam.status
        return matchesId && matchesStatus
      })
    },
    resetQueryParam () {
      this.queryParam = {}
      this.filteredData = this.listData
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
  // 在组件加载时自动调用
  created () {
    this.fetchData() // 调用方法获取数据
  }
}
</script>
