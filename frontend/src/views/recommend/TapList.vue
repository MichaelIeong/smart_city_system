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
                  placeholder="请选择触发事件"
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
              <a-form-item label="应用描述">
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
          <a @click="showDetail(record)">查看详情</a>
          <a-divider type="vertical"/>
          <a @click="handleDelete(record)">删除</a>
        </span>
      </a-table>
    </a-card>

    <a-modal
      :title="detailModalTitle"
      :width="800"
      :visible="detailModalVisible"
      :confirmLoading="detailModalLoading"
      :footer="null"
      @cancel="handleDetailModalClose"
      :bodyStyle="{ height: '500px', overflowY: 'auto' }"
    >
      <a-table
        :columns="detailColumns"
        :data-source="executeDetailData"
        :loading="detailModalLoading"
        rowKey="gridId"
        size="small"
        :pagination="false"
      >
        <span slot="enabled" slot-scope="text">
          <a-badge :status="text ? 'processing' : 'default'" :text="text ? '启用中' : '禁用中'" />
        </span>

        <span slot="action" slot-scope="text, record">
          <a v-if="record.enabled" @click="handleDisable(record)">禁用</a>
          <a v-else @click="handleEnable(record)">启用</a>
        </span>
      </a-table>
    </a-modal>
  </page-header-wrapper>
</template>

<script>
// ⚠️ Vue 2 通常使用选项式 API 或 setup 语法糖的兼容模式，这里改为标准的 setup 模式

/* eslint-disable */
import { message, Modal } from 'ant-design-vue';
import { ref, reactive, onMounted } from 'vue';
import dayjs from 'dayjs';
import { listTapRule, deleteTap, getAppExecuteDetail, setExecuteTapEnabled } from '@/api/manage';

export default {
    // 假设您在项目中启用了 setup 语法
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
            description: ''
        });

        // === 数据和列定义 ===
        const eventOptions = [
            { value: 'manhole-flooding', label: '井盖水浸' },
            { value: 'manhole-tilte', label: '井盖倾斜' },
            { value: 'truck_dect', label: '渣土车识别' },
            { value: 'ill_parking', label: '机动车违章停车' },
            { value: 'ill_parking2', label: '非机动车违章停车' },
            { value: 'waste_accumulate', label: '垃圾堆积' },
            { value: 'greenbelt_stack', label: '绿化带乱堆乱放' },
            { value: 'road-operate', label: '占道经营' },
            { value: 'out-store', label: '店外经营' },
            { value: 'road-feeding', label: '占道饲养家禽' },
            { value: 'trash_full', label: '垃圾桶满溢' }
        ];

        const norm = v => (typeof v === 'string' ? v.trim().toLowerCase() : v);
        const eventLabelMap = Object.fromEntries(eventOptions.map(o => [norm(o.value), o.label]));

        const columns = [
            { title: '序号', dataIndex: 'id' },
            { title: '事件类型', dataIndex: 'eventTypeLabel' },
            { title: '应用名称', dataIndex: 'appName' },
            {
                title: '描述',
                dataIndex: 'description',
                // ⚠️ Vue 2 兼容：使用 scopedSlots
                scopedSlots: { customRender: 'description' } 
            },
            {
                title: '更新时间',
                dataIndex: 'updateTime',
                sorter: true,
            },
            {
                title: '操作',
                dataIndex: 'action',
                width: '200px',
                // ⚠️ Vue 2 兼容：使用 scopedSlots
                scopedSlots: { customRender: 'action' }
            }
        ];

        // === 弹窗和详情状态 (新增) ===
        const detailModalVisible = ref(false);
        const detailModalLoading = ref(false);
        const executeDetailData = ref([]);
        const detailModalTitle = ref('应用执行详情');
        
        // 记录当前查看详情的 Rule ID
        const currentDetailRuleId = ref(null);

        // === 表格列定义 (新增详情表格的列) ===
        const detailColumns = [
          { title: '网格编号', dataIndex: 'meshNo', key: 'meshNo' },
          { title: '网格名称', dataIndex: 'meshName', key: 'meshName' },
          {
            title: '状态',
            dataIndex: 'enabled',
            key: 'enabled',
            // Vue 2 兼容：使用 scopedSlots
            scopedSlots: { customRender: 'enabled' }
          },
          {
            title: '操作',
            key: 'action',
            // Vue 2 兼容：使用 scopedSlots
            scopedSlots: { customRender: 'action' }
          }
        ];

        // === 核心数据加载函数 ===
        async function loadData(pageNo, pageSize, sorter = {}, filters = {}) {
            loading.value = true;
            try {
                const projectId = localStorage.getItem('project_id') || '';
                // 提取排序字段和方向
                const sortField = sorter.field;
                const sortOrder = sorter.order;

                const params = {
                    projectId,
                    eventType: searchParams.eventType,
                    description: searchParams.description,
                    pageNo,
                    pageSize,
                    sortField: sortField, 
                    sortOrder: sortOrder,
                    ...filters
                };

                const res = await listTapRule(params);

                const records = res?.data ?? [];
                const rows = records.map(r => ({
                    ...r,
                    eventTypeLabel: eventLabelMap[norm(r.eventType)] ?? r.eventType,
                    updateTime: r.updateTime ? dayjs(r.updateTime).format('YYYY-MM-DD HH:mm:ss') : ''
                }));

                dataSource.value = rows;
                pagination.current = res?.pageNo ?? pageNo;
                pagination.total = res?.totalCount ?? 0;

            } catch (e) {
                message.error('获取应用列表失败');
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
            searchParams.eventType = '';
            searchParams.description = '';
            pagination.current = 1;
            currentSorter = {};
            currentFilters = {};
            loadData(pagination.current, pagination.pageSize);
        }

        function handleDelete (record) {
            Modal.confirm({
                title: '确认删除?',
                content: '删除后将无法恢复，请确认是否继续。',
                onOk () {
                    return deleteTap({ id: record.id })
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

        onMounted(() => {
            loadData(pagination.current, pagination.pageSize, currentSorter, currentFilters);
        });

        async function showDetail(record) {
            detailModalVisible.value = true;
            detailModalLoading.value = true;
            detailModalTitle.value = `应用执行详情 - ID: ${record.id}`;
            currentDetailRuleId.value = record.id;
            
            try {
                // 1. 调用 API 获取执行详情数据，使用 record.id 作为 appId
                const res = await getAppExecuteDetail(record.id);
                console.log('executeDetailData', res);
                executeDetailData.value = res || []; 
            } catch (e) {
                message.error('获取应用执行详情失败');
                executeDetailData.value = [];
            } finally {
                detailModalLoading.value = false;
            }
        }
        
        // 弹窗关闭事件
        function handleDetailModalClose() {
            detailModalVisible.value = false;
            executeDetailData.value = []; // 清空数据
            currentDetailRuleId.value = null;
        }

        // 启用操作 (占位函数)
        async function handleEnable(record) {
          if (detailModalLoading.value) return;
          detailModalLoading.value = true;
          try {
              const res = await setExecuteTapEnabled(record.id, true); 
              if (res) {
                record.enabled = true;
                message.success(`应用网格【${record.meshName}】启用成功`);
              } else{
                message.error(`应用网格【${record.meshName}】启用失败`);
              }

          } catch (e) {
              message.error(`启用失败: ${e?.message || '未知错误'}`);
          } finally {
              detailModalLoading.value = false;
          }
        }

        // 禁用操作 (占位函数)
        async function handleDisable(record) {
          if (detailModalLoading.value) return;
          detailModalLoading.value = true;
          try {
              // 1. 调用后端 API，将状态设置为 false (禁用)
              // 这里的 record.id 对应后端 @PathVariable Integer id (AppGrid ID)
              const res = await setExecuteTapEnabled(record.id, false);
              if (res) {
                record.enabled = false;
                message.success(`应用网格【${record.meshName}】禁用成功`);
              } else {
                message.error(`应用网格【${record.meshName}】禁用失败`);
              }
          } catch (e) {
              message.error(`禁用失败: ${e?.message || '未知错误'}`);
          } finally {
              detailModalLoading.value = false;
          }
        }
        
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
          handleDelete,
          detailColumns,
          executeDetailData,
          detailModalVisible,
          detailModalLoading,
          detailModalTitle,
          handleDetailModalClose,
          handleEnable,
          handleDisable,
          showDetail
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
    /* 1. 设置为 inline-block 或 block，以便设置宽度 */
    display: inline-block; 
    
    /* 2. 必须设置最大宽度，以触发溢出 */
    /* 请根据您的实际表格布局调整这个值 */
    max-width: 250px; 

    /* 3. 强制内容不换行 */
    white-space: nowrap; 
    
    /* 4. 隐藏溢出内容 */
    overflow: hidden; 
    
    /* 5. 显示省略号 */
    text-overflow: ellipsis; 
}
</style>