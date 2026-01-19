<template>
  <page-header-wrapper>
    <div style="padding: 0 12px 24px 12px;">
      <a-row :gutter="16" style="height: calc(100vh - 180px);">

        <a-col :span="12">
          <a-card title="设备类型" bordered :style="{ borderRadius: '8px', height: '600px' }" class="full-height-card">

            <div style="height: 100%; display: flex; flex-direction: column;">
              <a-table
                :columns="deviceTypeColumns"
                :dataSource="deviceTypes"
                row-key="deviceTypeId"
                :pagination="false"
                :scroll="{ x: 900, y: 400 }"
                @click="handleDeviceTypeClick"
                :customRow="customRowClick"
                style="flex: 1; overflow: hidden;"
                :row-class-name="rowClassName"
              />

              <div style="margin-top: auto; padding-top: 16px; display: flex; align-items: center;">
                <a-button type="primary" @click="showAddDeviceTypeModal">新增设备类型</a-button>
              </div>
            </div>

            <a-modal
              v-model="isDeviceTypeModalVisible"
              title="新增设备类型"
              @ok="handleNewDeviceTypeSubmit"
              @cancel="handleCancel"
            >
              <a-row :gutter="24">
                <a-col :span="12">
                  <a-form-item label="设备类型序号">
                    <a-input v-model="newDeviceType.deviceTypeId" placeholder="输入设备类型序号" />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="设备类型名称">
                    <a-input v-model="newDeviceType.deviceTypeName" placeholder="输入设备类型名称" />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-row :gutter="24">
                <a-col :span="12">
                  <a-form-item label="设备类型属性">
                    <a-input v-model="newDeviceType.deviceTypeAttributes" placeholder="输入设备类型属性" />
                  </a-form-item>
                </a-col>
                <a-col :span="12">
                  <a-form-item label="设备类型功能">
                    <a-input v-model="newDeviceType.deviceTypeFunction" placeholder="输入设备类型功能" />
                  </a-form-item>
                </a-col>
              </a-row>
            </a-modal>
          </a-card>
        </a-col>

        <a-col :span="12">
          <a-card
            :title="deviceInstanceTitle"
            bordered
            :style="{ borderRadius: '8px', height: '600px' }"
            class="full-height-card"
          >
            <template slot="extra">
              <div style="display: flex; align-items: center; gap: 8px;">
                <span style="font-weight: normal; font-size: 14px;">筛选网格:</span>
                <a-select
                  v-model="selectedGridId"
                  placeholder="全部网格"
                  style="width: 160px"
                  allowClear
                  size="small"
                  @change="handleGridChange"
                >
                  <a-select-option v-for="grid in gridList" :key="grid.meshCode" :value="grid.meshName">
                    {{ grid.meshName }}
                  </a-select-option>
                </a-select>
              </div>
            </template>

            <div style="height: 100%; display: flex; flex-direction: column;">
              <a-table
                :columns="deviceInstanceColumns"
                :dataSource="filteredDeviceInstances"
                row-key="deviceId"
                :pagination="false"
                :scroll="{ x: 700, y: 350 }"
                :row-selection="{ selectedRowKeys: selectedInstanceKeys, onChange: onSelectChange}"
                style="flex: 1; overflow: hidden;"
                :locale="{ emptyText: selectedDeviceType ? '当前条件下暂无设备实例' : '请先选择设备类型' }"
              />

              <div style="margin-top: auto; padding-top: 16px; display: flex; justify-content: flex-start; gap: 8px;">
                <a-button type="primary" @click="showDeviceInstanceModal" :disabled="!selectedDeviceType">
                  新增设备实例
                </a-button>
                <a-button type="default" @click="refreshDevice" :disabled="selectedInstanceKeys.length !== 1">
                  刷新
                </a-button>
                <a-button type="danger" @click="deleteDeviceInstance" :disabled="selectedInstanceKeys.length === 0">
                  删除
                </a-button>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <a-modal v-model="isRefreshModelVisible" title="设备实例最新信息" @ok="handleRefreshOk" @cancel="handleRefreshCancel">
      <a-descriptions bordered :column="1">
        <a-descriptions-item label="设备序号">{{ refreshedDeviceData.deviceId }}</a-descriptions-item>
        <a-descriptions-item label="设备名称">{{ refreshedDeviceData.deviceName }}</a-descriptions-item>
        <a-descriptions-item label="最新状态">{{ refreshedDeviceData.states }}</a-descriptions-item>
        <a-descriptions-item label="详细信息">{{ refreshedDeviceData.fixedProperties }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <a-modal
      v-model="isDeleteModalVisible"
      title="确认删除"
      @ok="confirmDeleteDevice"
      @cancel="cancelDeleteDevice"
      ok-text="确认"
      cancel-text="取消">
      <p>确定要删除选中的 {{ selectedInstanceKeys.length }} 个设备实例吗？此操作不可恢复。</p>
      <ul v-if="selectedDeviceNames.length > 0">
        <li v-for="name in selectedDeviceNames" :key="name">{{ name }}</li>
      </ul>
    </a-modal>

    <a-modal v-model="isDeviceInstanceModalVisible" title="新增设备实例" @ok="handleNewDeviceInstanceSubmit" @cancel="handleCancelDeviceInstance">
      <a-form @submit.prevent="addDeviceInstance">
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="设备序号">
              <a-input v-model="newDeviceInstance.deviceId" placeholder="输入设备序号" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="设备名称">
              <a-input v-model="newDeviceInstance.deviceName" placeholder="输入设备名称" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="设备所属区域">
              <a-input v-model="newDeviceInstance.deviceRegion" placeholder="输入设备所属区域" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="设备可用时间">
              <a-input v-model="newDeviceInstance.deviceTime" placeholder="输入设备可用时间" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="设备状态">
              <a-input v-model="newDeviceInstance.states" placeholder="输入设备状态" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="操作">
              <a-input v-model="newDeviceInstance.operation" placeholder="输入设备操作" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </page-header-wrapper>
</template>

<script>
import axios from 'axios'

export default {
  beforeCreate () {
    this.formDeviceType = this.$form.createForm(this, { name: 'form_device_type' })
    this.formDeviceInstance = this.$form.createForm(this, { name: 'form_device_instance' })
  },
  data () {
    return {
      selectedDeviceType: null,
      selectedRowKeys: [],
      selectedInstanceKeys: [],
      selectedGridId: undefined,
      gridList: [],
      isRefreshModelVisible: false,
      isDeleteModalVisible: false,
      refreshedDeviceData: {},
      isDeviceTypeModalVisible: false,
      isDeviceInstanceModalVisible: false,

      deviceTypeColumns: [
        { title: '设备类型ID', dataIndex: 'deviceTypeId', key: 'deviceTypeId', align: 'center', width: 180 },
        { title: '设备类型名称', dataIndex: 'deviceTypeName', key: 'deviceTypeName', align: 'center', width: 180 },
        {
          title: '设备类型属性',
          dataIndex: 'deviceTypeAttributes',
          key: 'deviceTypeAttributes',
          align: 'center',
          width: 250,
          customRender: (text) => <div style="white-space: pre-line;">{text || '-'}</div>
        },
        {
          title: '设备类型功能',
          dataIndex: 'deviceTypeFunction',
          key: 'deviceTypeFunction',
          align: 'center',
          width: 250,
          customRender: (text) => <div style="white-space: pre-line;">{text || '-'}</div>
        }
      ],
      deviceTypes: [],
      deviceInstanceColumns: [
        { title: '设备序号', dataIndex: 'deviceId', key: 'deviceId', width: 100, align: 'center' },
        { title: '设备名称', dataIndex: 'deviceName', key: 'deviceName', width: 120, align: 'center' },
        { title: '设备所属区域', dataIndex: 'deviceRegion', key: 'deviceRegion', width: 120, align: 'center' },
        { title: '设备状态', dataIndex: 'states', key: 'states', width: 100, align: 'center' },
        { title: '设备可用时间', dataIndex: 'deviceTime', key: 'deviceTime', width: 140, align: 'center' },
        { title: '操作', dataIndex: 'operation', key: 'operation', width: 120, align: 'center' }
      ],
      deviceInstances: [],
      newDeviceType: { deviceTypeId: '', deviceTypeName: '', deviceTypeAttributes: '', deviceTypeFunction: '' },
      newDeviceInstance: { deviceId: '', deviceName: '', deviceRegion: '', deviceTime: '', states: '', operation: '' },
      loading: false
    }
  },
  computed: {
    filteredDeviceInstances () {
      if (!this.selectedDeviceType) return []
      return this.deviceInstances.filter(instance => {
        const matchType = instance.deviceTypeId === this.selectedDeviceType.deviceTypeId
        const matchGrid = !this.selectedGridId || instance.deviceRegion === this.selectedGridId
        return matchType && matchGrid
      })
    },
    deviceInstanceTitle () {
      return this.selectedDeviceType ? `设备实例 (${this.selectedDeviceType.deviceTypeName})` : '设备实例'
    },
    selectedDeviceNames () {
      return this.filteredDeviceInstances
        .filter(device => this.selectedInstanceKeys.includes(device.deviceId))
        .map(device => device.deviceName)
    }
  },
  watch: {
    selectedDeviceType () {
      this.selectedInstanceKeys = []
    }
  },
  mounted () {
    this.fetchDeviceTypes()
    this.fetchDeviceData()
    this.fetchGridList()
  },
  methods: {
    async fetchGridList () {
      try {
        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
        const response = await axios.get(`${baseUrl}/api/meshes/all`)
        this.gridList = (response.data || []).map(item => ({
          meshCode: item.mesh_no,
          meshName: item.mesh_name
        }))
      } catch (error) {
        console.error('获取全局网格列表失败:', error)
      }
    },
    handleGridChange (value) { this.selectedInstanceKeys = [] },
    async fetchDeviceData () {
      try {
        const projectId = localStorage.getItem('project_id') || '1'
        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
        const response = await axios.get(`${baseUrl}/api/devices`, { params: { project: projectId } })
        this.deviceInstances = (response.data || []).map(device => ({
          deviceId: device.deviceId,
          deviceName: device.deviceName,
          deviceRegion: device.meshCode || '未设置',
          states: this.parseStates(device.states || []),
          deviceTime: device.lastUpdateTime || '未知',
          operation: this.parseFunctions(device.functions || []),
          deviceTypeName: device.deviceTypeName,
          deviceTypeId: device.deviceTypeId
        }))
      } catch (error) { console.error('获取设备数据失败:', error) }
    },
    async fetchDeviceTypes () {
      try {
        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
        const response = await axios.get(`${baseUrl}/api/deviceTypes/fromTslProduct`)
        this.deviceTypes = response.data.map(item => ({
          deviceTypeId: item.deviceTypeId,
          deviceTypeName: item.deviceTypeName,
          deviceTypeAttributes: this.formatArrayField(item.deviceTypeAttributes, '未知属性'),
          deviceTypeFunction: this.formatArrayField(item.deviceTypeFunction, '无特定功能')
        }))
      } catch (error) { console.error('获取设备类型失败:', error) }
    },
    async fetchDeviceInstancesByType (prodId) {
      try {
        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
        const response = await axios.get(`${baseUrl}/api/devices/instances`, { params: { prodId } })
        const rawData = response.data?.data || response.data || []
        this.deviceInstances = rawData.map(device => ({
          deviceId: device.deviceId || device.devId || '-',
          deviceName: device.deviceName || device.devName || '未命名设备',
          deviceRegion: device.mesh_name || device.meshName || device.deviceRegion || '未设置',
          deviceTime: device.lastUpdateTime || '全天',
          states: this.parseStates(device.states),
          operation: device.operation || '无操作指令',
          deviceTypeId: prodId
        }))
      } catch (error) { console.error('获取设备实例失败:', error) }
    },
    parseStates (states) {
      if (Array.isArray(states) && states.length > 0) {
        return states.map(s => `${s.stateKey || 'Unknown'}: ${s.stateValue || 'Unknown'}`).join(', ')
      }
      return '离线'
    },
    parseFunctions (functions) {
      if (Array.isArray(functions) && functions.length > 0) {
        return functions.map(func => func.functionName || 'Unknown').join(', ')
      }
      return '无可用操作'
    },
    formatArrayField (field, fallbackText) {
      if (!field) return fallbackText
      if (Array.isArray(field)) return field.join('\n')
      try {
        const parsed = JSON.parse(field)
        return Array.isArray(parsed) ? parsed.join('\n') : String(parsed)
      } catch (e) {
        return field.replace(/[\\[\]"]/g, '').replace(/,/g, '\n').trim()
      }
    },
    onSelectChange (selectedRowKeys) { this.selectedInstanceKeys = selectedRowKeys },
    deleteDeviceInstance () { this.isDeleteModalVisible = true },
    confirmDeleteDevice () {
      this.deviceInstances = this.deviceInstances.filter(d => !this.selectedInstanceKeys.includes(d.deviceId))
      this.selectedInstanceKeys = []; this.isDeleteModalVisible = false; this.$message.success('删除成功')
    },
    cancelDeleteDevice () { this.isDeleteModalVisible = false },
    async refreshDevice () {
      const deviceId = this.selectedInstanceKeys[0]
      const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
      try {
        const response = await axios.get(`${baseUrl}/api/devices/${deviceId}`)
        this.refreshedDeviceData = {
          deviceId: response.data.deviceId,
          deviceName: response.data.deviceName,
          states: this.parseStates(response.data.states || []),
          fixedProperties: response.data.fixedProperties || '无详细信息'
        }
        this.isRefreshModelVisible = true
      } catch (error) { this.$message.error('刷新失败') }
    },
    handleRefreshOk () { this.isRefreshModelVisible = false },
    handleRefreshCancel () { this.isRefreshModelVisible = false },
    async handleDeviceTypeClick (record) {
      this.selectedDeviceType = record
      this.selectedRowKeys = [record.deviceTypeId]
      this.$message.info(`加载 ${record.deviceTypeName}...`)
      await this.fetchDeviceInstancesByType(record.deviceTypeId)
    },
    customRowClick (record) {
      return { on: { click: () => this.handleDeviceTypeClick(record) }, style: { cursor: 'pointer' } }
    },
    rowClassName (record) { return this.selectedRowKeys.includes(record.deviceTypeId) ? 'selected-row' : '' },
    showAddDeviceTypeModal () { this.isDeviceTypeModalVisible = true },
    showDeviceInstanceModal () { this.isDeviceInstanceModalVisible = true },
    handleNewDeviceTypeSubmit () {
      this.deviceTypes.push({ ...this.newDeviceType })
      this.isDeviceTypeModalVisible = false; this.$message.success('添加成功')
    },
    handleCancel () { this.isDeviceTypeModalVisible = false },
    handleNewDeviceInstanceSubmit () {
      this.deviceInstances.push({ ...this.newDeviceInstance, deviceTypeId: this.selectedDeviceType.deviceTypeId })
      this.isDeviceInstanceModalVisible = false; this.$message.success('添加成功')
    },
    handleCancelDeviceInstance () { this.isDeviceInstanceModalVisible = false },
    addDeviceInstance () {}
  }
}
</script>

<style scoped>
.ant-form-item { margin-bottom: 12px; }
.selected-row { background-color: #e6f7ff !important; }
.ant-table-tbody > tr > td { text-align: center; padding: 8px 12px; white-space: nowrap !important; overflow: hidden; text-overflow: ellipsis; }
.ant-table-thead > tr > th { white-space: nowrap; text-align: center; padding: 8px 4px; font-size: 13px; }

/* 强制卡片Body区域占满高度并使用Flex布局 */
.full-height-card >>> .ant-card-body {
  height: calc(100% - 58px); /* 减去Header高度 */
  display: flex;
  flex-direction: column;
}

@media (max-width: 768px) { .ant-col-12 { width: 100% !important; margin-bottom: 16px; } }
</style>
