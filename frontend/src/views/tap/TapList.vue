<template>
  <page-header-wrapper>
    <a-card :bordered="false">
      <!-- <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="8" :sm="24">
              <a-form-item label="规则编号">
                <a-input v-model="queryParam.id" placeholder=""/>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <a-form-item label="使用状态">
                <a-select v-model="queryParam.status" placeholder="请选择" default-value="-1">
                  <a-select-option value="-1">全部</a-select-option>
                  <a-select-option value="0">已关闭</a-select-option>
                  <a-select-option value="1">已部署</a-select-option>
                  <a-select-option value="2">异常</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :md="8" :sm="24">
              <span class="table-page-search-submitButtons">
                <a-button type="primary" @click="$refs.table.refresh(true)">查询</a-button>
                <a-button style="margin-left: 8px" @click="() => this.queryParam = {}">重置</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div> -->

      <div class="table-operator">
        <a-button type="primary" icon="plus" @click="handleAdd">新建</a-button>
        <a-button icon="delete" v-action:edit v-if="selectedRowKeys.length > 0" @click="handleBatchDelete">删除
        </a-button>
      </div>

      <s-table
        ref="table"
        size="default"
        rowKey="id"
        :columns="columns"
        :data="loadData"
        :rowSelection="rowSelection"
        showPagination="auto"
      >

        <span slot="status" slot-scope="text">
          <a-badge :status="text | statusTypeFilter" :text="text | statusFilter"/>
        </span>
        <span slot="description" slot-scope="text">
          <ellipsis :length="15" tooltip>{{ text }}</ellipsis>
        </span>

        <span slot="action" slot-scope="text, record">
          <template>
            <a @click="handleEdit(record)">编辑</a>
            <a-divider type="vertical"/>
            <a @click="handleDelete(record)">删除</a>
          </template>
        </span>
      </s-table>

    </a-card>
  </page-header-wrapper>
</template>

<script>
import { STable, Ellipsis } from '@/components'
import { deleteTap, deleteTaps, getTapList } from '@/api/manage'
import { Modal, message } from 'ant-design-vue'

const columns = [
  {
    title: '序号',
    dataIndex: 'seq'
  },
  {
    title: '描述',
    dataIndex: 'description',
    scopedSlots: { customRender: 'description' }
  },
  // {
  //   title: '状态',
  //   dataIndex: 'status',
  //   scopedSlots: { customRender: 'status' }
  // },
  {
    title: '更新时间',
    dataIndex: 'updatedAt',
    // sorter: true
    sorter: (a, b) => new Date(a.updatedAt) - new Date(b.updatedAt)
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: '150px',
    scopedSlots: { customRender: 'action' }
  }
]

const statusMap = {
  0: {
    status: 'default',
    text: '已关闭'
  },
  1: {
    status: 'success',
    text: '已部署'
  },
  2: {
    status: 'error',
    text: '异常'
  }
}

export default {
  name: 'TapList',
  components: {
    STable,
    Ellipsis
  },
  data () {
    this.columns = columns
    return {
      // create model
      queryParam: {},
      // 加载数据方法 必须为 Promise 对象
      loadData: parameter => {
        // 将传入的参数（parameter）和组件内的查询参数（this.queryParam）合并成新的对象 requestParameters
        const requestParameters = Object.assign({}, parameter, this.queryParam)
        return getTapList(requestParameters)
          .then(res => {
            // console.log(res)
            res.data = res.data.map((item, index) => {
              item.seq = ((res.pageNo - 1) * res.pageSize) + (index + 1) // TODO: 序号验证: 翻页继续计数
              item.updatedAt = item.updateTime.replace(
                /(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2}):(\d{2}).*/,
                '$1-$2-$3 $4:$5:$6'
              )
              return item
            })
            return res
          })
      },
      selectedRowKeys: [],
      selectedRows: []
    }
  },
  filters: {
    statusFilter (type) {
      return statusMap[type].text
    },
    statusTypeFilter (type) {
      return statusMap[type].status
    }
  },
  created () {
    // getRoleList({ t: new Date() }) // TODO: 一直报错
    console.log(columns)
  },
  computed: {
    rowSelection () {
      return {
        selectedRowKeys: this.selectedRowKeys,
        onChange: this.handleSelectChange
      }
    }
  },
  methods: {
    handleAdd () {
      // this.$router.push({ path: '/tap/tap-detail/0' })
      this.$router.push({ name: 'TapDetail', params: { id: 0 } })
    },
    handleEdit (record) {
      // this.$router.push({ path: '/tap/tap-detail/' + record.id })
      this.$router.push({ name: 'TapDetail', params: { id: record.id } })
    },
    handleBatchDelete () {
      const that = this
      const refs = this.$refs
      Modal.confirm({
        title: '确认删除?',
        content: '删除后将无法恢复，请确认是否继续。',
        onOk () {
          console.log('999')
          return deleteTaps(that.selectedRowKeys).then(() => {
            refs.table.refresh()
            message.success('删除成功')
          }).catch(() => {
            message.error('删除失败')
          })
        }
      })
    },
    handleDelete (record) {
      const refs = this.$refs
      Modal.confirm({
        title: '确认删除?',
        content: '删除后将无法恢复，请确认是否继续。',
        onOk () {
          return deleteTap({ id: record.id }).then(() => {
            refs.table.refresh()
            message.success('删除成功')
          }).catch(() => {
            message.error('删除失败')
          })
        }
      })
    },
    handleSelectChange (selectedRowKeys, selectedRows) {
      this.selectedRowKeys = selectedRowKeys
      this.selectedRows = selectedRows
    }
  }

}
</script>
