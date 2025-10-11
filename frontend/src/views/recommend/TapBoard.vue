<template>
  <page-header-wrapper>
    <div class="tap-board">
      <a-row :gutter="24" style="height: calc(100vh - 250px);">
        <!-- 左侧：运行中应用的事件类型列表 -->
        <a-col :span="12">
          <a-card title="运行中应用的事件类型" bordered :style="{ borderRadius: '8px', height: '600px' }">
            <div style="height: calc(100% - 60px); display: flex; flex-direction: column;">
              <a-table
                :columns="eventTypeColumns"
                :dataSource="eventTypes"
                row-key="eventType"
                :pagination="false"
                :scroll="{ y: 400 }"
                :loading="loading"
                :customRow="customRowClick"
                :row-class-name="rowClassName"
                :locale="{ emptyText: '暂无数据' }"
                style="flex: 1;"
              />
            </div>
          </a-card>
        </a-col>
        <!-- 右侧：应用实例 -->
        <a-col :span="12">
          <a-card title="应用实例" bordered :style="{ borderRadius: '8px', height: '600px' }">
            <a-table
              :columns="instanceColumns"
              :dataSource="instances"
              row-key="id"
              :pagination="false"
              :loading="instancesLoading"
              :locale="{ emptyText: selectedEventType ? '该类型暂无实例' : '请先从左侧选择事件类型' }"
              :scroll="{ y: 440 }"
            >
              <span slot="action" slot-scope="text, record">
                <a @click="showLog(record)">查看日志</a>
              </span>
            </a-table>
          </a-card>
        </a-col>
      </a-row>
    </div>
    <a-modal
      :visible="showLogModal"
      title="应用实例日志"
      width="80%"
      @cancel="handleCancel">
      <pre style="white-space: pre-wrap; word-wrap: break-word;">{{ currentLog.join('\n') }}</pre>
      <template slot="footer">
        <a-button key="submit" type="primary" @click="handleOk">关闭</a-button>
      </template>
    </a-modal>
  </page-header-wrapper>
</template>

<script setup>
/* eslint-disable */
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { getRunningEvents, getWaitValueOfEvent, getLog } from '@/api/manage'

// 可选：事件类型中文映射（按需增补）
const eventTypeLabelMap = {
  'manhole-flooding': '井盖水浸',
  'manhole-tilte': '井盖倾斜',
  'truck_dect': '渣土车识别',
  'ill_parking': '机动车违章停车',
  'ill_parking2': '非机动车违章停车',
  'waste_accumulate': '垃圾堆积',
  'greenbelt_stack': '绿化带乱堆乱放',
  'road-operate': '占道经营',
  'out-store': '店外经营',
  'road-feeding': '占道饲养家禽',
  'trash_full': '垃圾桶满溢'
}

const loading = ref(false)
const eventTypes = ref([]) // [{ eventType, eventTypeLabel}]
const selectedEventType = ref('')

const instancesLoading = ref(false)
const instances = ref([])  

const showLogModal = ref(false)
const currentLog = ref([])

const eventTypeColumns = [
  { title: '事件类型', dataIndex: 'eventTypeLabel', key: 'eventTypeLabel' },
  { title: '实例数量', dataIndex: 'instanceNum', key: 'instanceNum' }
]

const instanceColumns = [
  { title: '实例标志', dataIndex: 'instanceValue', key: 'instanceValue' },
  { title: '操作', dataIndex: 'action', scopedSlots: { customRender: 'action' } }
]

async function fetchEventTypes () {
    loading.value = true
    try {
        const res = await getRunningEvents()
        eventTypes.value = res.map(item => {
          return {
            eventType: item.eventType,
            eventTypeLabel: eventTypeLabelMap[item.eventType] || item.eventType,
            instanceNum: item.instanceNum
          }
        })
    } catch (e) {
      message.error('获取事件类型失败')
    } finally {
      loading.value = false
    }
}

const customRowClick = (record) => ({
  on: {
    click: async () => {
      selectedEventType.value = record.eventType
      if (!selectedEventType) {
          instances.value = []
          return
      }
      instancesLoading.value = true
      try {
        const res = await getWaitValueOfEvent(selectedEventType.value)
        instances.value = res.map(item => {
          return {
            instanceValue: item
          }
        })
      } catch (e) {
        message.error('获取应用实例失败')
        instances.value = []
      } finally {
        instancesLoading.value = false
      }
    }
  }
})

const rowClassName = (record) =>
  record.eventType === selectedEventType.value ? 'selected-row' : ''

onMounted(() => {
  fetchEventTypes()
})

async function showLog(record) {
  try {
    const res = await getLog(selectedEventType.value, record.instanceValue)
    currentLog.value = res
    showLogModal.value = true
  } catch (e) {
    message.error('获取日志失败')
  }
}

const handleCancel = () => {
  showLogModal.value = false
  currentLog.value = [] // 可选：清空日志内容
}

const handleOk = () => {
  showLogModal.value = false
  currentLog.value = [] // 可选：清空日志内容
}
</script>

<style lang="less" scoped>
:deep(.selected-row) {
  background: #e6f7ff !important;
}
</style>