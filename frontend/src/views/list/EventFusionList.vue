<template>
  <page-header-wrapper>
    <a-card :bordered="false" :style="{ borderRadius: '8px' }">
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="6" :sm="24">
              <a-form-item label="事件类型">
                <a-select
                  v-model="searchParams.eventType"
                  placeholder="请选择事件类型"
                  option-filter-prop="children"
                  allow-clear
                >
                  <a-select-option
                    v-for="item in eventOptions"
                    :key="item.value"
                    :value="item.value"
                  >
                    {{ item.label }}
                  </a-select-option>
                </a-select>
              </a-form-item>
            </a-col>

            <a-col :md="6" :sm="24">
              <a-form-item label="事件名称">
                <a-input v-model="searchParams.eventName" placeholder="请输入" allow-clear /> </a-form-item>
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
import { ref, reactive, onMounted, computed } from 'vue';
import dayjs from 'dayjs';
import { listEnvEvent, getAllEnvEvent, getEventFusionDeployDetail, deleteEnvEvent } from '@/api/manage';

export default {
    name: 'EventFusionList',
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
            eventType: '',
            eventName: ''
        });

        // === 数据和列定义 ===
        const eventOptions = ref([]);

        const norm = v => (typeof v === 'string' ? v.trim().toLowerCase() : v);
        const eventLabelMap = computed(() => {
          return Object.fromEntries(eventOptions.value.map(o => [norm(o.value), o.label]));
        });

        const columns = [
            { title: '序号', dataIndex: 'id' },
            { title: '事件类型', dataIndex: 'eventTypeLabel' },
            { title: '事件名称', dataIndex: 'eventName' },
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
        const detailModalTitle = ref('事件融合部署详情');

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
                    eventType: searchParams.eventType,
                    eventName: searchParams.eventName,
                    pageNo,
                    pageSize,
                    sortField: sortField, 
                    sortOrder: sortOrder,
                    ...filters
                };

                const res = await listEnvEvent(params);

                const records = res?.data ?? [];
                const rows = records.map(r => ({
                    ...r,
                    eventTypeLabel: eventLabelMap.value[norm(r.eventType)] ?? r.eventType,
                    createTime: r.createTime ? dayjs(r.createTime).format('YYYY-MM-DD HH:mm:ss') : ''
                }));

                dataSource.value = rows;
                pagination.current = res?.pageNo ?? pageNo;
                pagination.total = res?.totalCount ?? 0;

            } catch (e) {
                message.error('获取事件列表失败');
                dataSource.value = [];
                pagination.total = 0;
            } finally {
                loading.value = false;
            }
        }

        async function fetchEventOptions() {
            try {
                const res = await getAllEnvEvent();
                if (res) {
                    eventOptions.value = res.map(item => ({
                        value: item.eventType,
                        label: item.eventName || item.eventType
                    }));
                }
            } catch (e) {
                console.error('Fetch Event Options Error:', e);
                message.error('加载事件类型下拉框失败');
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
            searchParams.eventType = '';
            searchParams.eventName = '';
            pagination.current = 1;
            currentSorter = {};
            currentFilters = {};
            loadData(pagination.current, pagination.pageSize);
        }

        function handleSync(record) {
            alert(`应用同步事件：${record.eventName || record.id}`);
        }

        // 显示部署详情
        async function showDetail(record) {
            detailModalVisible.value = true;
            detailModalLoading.value = true;
            detailModalTitle.value = `事件融合部署详情 - ${record.eventName} (ID: ${record.id})`;
            
            try {
                const res = await getEventFusionDeployDetail(record.id);
                console.log('deployDetailData', res);
                deployDetailData.value = res || []; 
            } catch (e) {
                message.error('获取事件融合部署详情失败');
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
                content: `删除事件「${record.eventName}」后将无法恢复，请确认是否继续。`,
                onOk () {
                    return deleteEnvEvent(record.id)
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
            await fetchEventOptions();
            loadData(pagination.current, pagination.pageSize, currentSorter, currentFilters);
        });

        return {
          loading,
          dataSource,
          pagination,
          searchParams,
          eventOptions,
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
