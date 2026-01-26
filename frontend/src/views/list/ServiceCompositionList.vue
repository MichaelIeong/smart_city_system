<template>
  <page-header-wrapper>
    <a-card :bordered="false" :style="{ borderRadius: '8px' }">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="6" :sm="24">
              <a-form-item label="服务名称">
                <a-input v-model="searchParams.name" placeholder="请输入" allow-clear />
              </a-form-item>
            </a-col>

            <a-col :md="6" :sm="24">
              <a-form-item label="描述">
                <a-input v-model="searchParams.description" placeholder="请输入" allow-clear /> </a-form-item>
            </a-col>

            <a-col :md="12" :sm="24">
              <span>
                <a-button style="margin-left: 20px" type="primary" @click="doSearch">搜索</a-button>
                <a-button style="margin-left: 10px" @click="handleReset">重置</a-button>
              </span>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <a-table
        :columns="columns"
        :data-source="dataSource"
        :pagination="pagination"
        :loading="loading"
        rowKey="id"
        @change="handleTableChange"
        size="default"
      >
        <span slot="description" slot-scope="text">
          <span
            :title="text"
            class="ellipsis-50-chars"
          >
            {{ text }}
          </span>
        </span>

        <span slot="action" slot-scope="text, record">
          <a @click="handleSync(record)">应用同步</a>
          <a-divider type="vertical"/>
          <a @click="showDetail(record)">部署详情</a>
          <a-divider type="vertical"/>
          <a @click="handleDelete(record)">删除</a>
        </span>
      </a-table>
    </a-card>

    <a-modal
      :title="detailModalTitle"
      :width="600"
      :visible="detailModalVisible"
      :confirmLoading="detailModalLoading"
      :footer="null"
      @cancel="handleDetailModalClose"
      :bodyStyle="{ height: '400px', overflowY: 'auto' }"
    >
      <a-table
        :columns="detailColumns"
        :data-source="deployDetailData"
        :loading="detailModalLoading"
        rowKey="gridId"
        size="small"
        :pagination="false"
      >
      </a-table>
    </a-modal>
  </page-header-wrapper>
</template>

<script>
/* eslint-disable */
import { message, Modal } from 'ant-design-vue';
import { ref, reactive, onMounted } from 'vue';
import dayjs from 'dayjs';
import { listEnvService, getAllEnvService, getServiceGroupDeployDetail, deleteEnvService } from '@/api/manage';

export default {
    name: 'ServiceCompositionList',
    setup() {
        // === 表格状态管理 ===
        const loading = ref(false);
        const dataSource = ref([]);
        const pagination = reactive({
            current: 1,
            pageSize: 10,
            total: 0,
            showSizeChanger: true,
            showQuickJumper: true,
            pageSizeOptions: ['10', '20', '50', '100'],
        });
        let currentSorter = {};
        let currentFilters = {};

        // 查询参数
        const searchParams = reactive({
            name: '',
            description: ''
        });

        const columns = [
            { title: '序号', dataIndex: 'id' },
            { title: '服务名称', dataIndex: 'name' },
            {
                title: '描述',
                dataIndex: 'description',
                scopedSlots: { customRender: 'description' } 
            },
            {
                title: '创建时间',
                dataIndex: 'createTime',
                sorter: true,
            },
            {
                title: '操作',
                dataIndex: 'action',
                width: '250px',
                scopedSlots: { customRender: 'action' }
            }
        ];

        // === 部署详情弹窗状态 ===
        const detailModalVisible = ref(false);
        const detailModalLoading = ref(false);
        const deployDetailData = ref([]);
        const detailModalTitle = ref('服务组部署详情');

        // === 部署详情表格列定义 ===
        const detailColumns = [
          { title: '网格编号', dataIndex: 'meshNo', key: 'meshNo' },
          { title: '网格名称', dataIndex: 'meshName', key: 'meshName' }
        ];

        // === 核心数据加载函数 ===
        async function loadData(pageNo, pageSize, sorter = {}, filters = {}) {
            loading.value = true;
            try {
                // 提取排序字段和方向
                const sortField = sorter.field;
                const sortOrder = sorter.order;

                const params = {
                    name: searchParams.name,
                    description: searchParams.description,
                    pageNo,
                    pageSize,
                    sortField: sortField, 
                    sortOrder: sortOrder,
                    ...filters
                };

                const res = await listEnvService(params);

                const records = res?.data ?? [];
                const rows = records.map(r => ({
                    ...r,
                    createTime: r.createTime ? dayjs(r.createTime).format('YYYY-MM-DD HH:mm:ss') : ''
                }));

                dataSource.value = rows;
                pagination.current = res?.pageNo ?? pageNo;
                pagination.total = res?.totalCount ?? 0;

            } catch (e) {
                message.error('获取服务列表失败');
                dataSource.value = [];
                pagination.total = 0;
            } finally {
                loading.value = false;
            }
        }

        // === a-table 事件处理函数 ===
        function handleTableChange(p, filters, sorter) {
            currentSorter = sorter;
            currentFilters = filters;
            
            pagination.pageSize = p.pageSize;
            pagination.current = p.current;
            
            loadData(p.current, p.pageSize, sorter, filters);
        }

        // === 交互操作 ===
        function doSearch () {
            pagination.current = 1;
            loadData(pagination.current, pagination.pageSize, currentSorter, currentFilters);
        }

        function handleReset () {
            searchParams.name = '';
            searchParams.description = '';
            pagination.current = 1;
            currentSorter = {};
            currentFilters = {};
            loadData(pagination.current, pagination.pageSize);
        }

        function handleSync(record) {
            alert(`应用同步服务：${record.name || record.id}`);
        }

        // 显示部署详情
        async function showDetail(record) {
            detailModalVisible.value = true;
            detailModalLoading.value = true;
            detailModalTitle.value = `服务组部署详情 - ${record.name} (ID: ${record.id})`;
            
            try {
                const res = await getServiceGroupDeployDetail(record.id);
                console.log('deployDetailData', res);
                deployDetailData.value = res || []; 
            } catch (e) {
                message.error('获取服务组部署详情失败');
                deployDetailData.value = [];
            } finally {
                detailModalLoading.value = false;
            }
        }

        // 弹窗关闭事件
        function handleDetailModalClose() {
            detailModalVisible.value = false;
            deployDetailData.value = [];
        }

        function handleDelete(record) {
            Modal.confirm({
                title: '确认删除?',
                content: `删除服务「${record.name}」后将无法恢复，请确认是否继续。`,
                onOk () {
                    return deleteEnvService(record.id)
                        .then(() => {
                            loadData(pagination.current, pagination.pageSize, currentSorter, currentFilters);
                            message.success('删除成功');
                        })
                        .catch((err) => {
                            message.error(`删除失败: ${err?.message || '未知错误'}`);
                        });
                }
            });
        }

        onMounted(async () => {
            loadData(pagination.current, pagination.pageSize, currentSorter, currentFilters);
        });

        return {
          loading,
          dataSource,
          pagination,
          searchParams,
          columns,
          doSearch,
          handleReset,
          handleTableChange,
          handleSync,
          showDetail,
          handleDelete,
          detailModalVisible,
          detailModalLoading,
          deployDetailData,
          detailModalTitle,
          detailColumns,
          handleDetailModalClose
        }
    }
}
</script>

<style lang="less" scoped>
.table-page-search-wrapper {
  margin-bottom: 16px;
}
/* 强制单行省略号截断样式 */
.ellipsis-50-chars {
    display: inline-block; 
    max-width: 250px; 
    white-space: nowrap; 
    overflow: hidden; 
    text-overflow: ellipsis; 
}
</style>
