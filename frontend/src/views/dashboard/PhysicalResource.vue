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
                style="flex: 1; overflow: hidden;"
                :customRow="customRowClick"
                :rowClassName="setRowClassName"
              />

              <div style="margin-top: auto; padding-top: 16px; display: flex; align-items: center; gap: 8px;">
                <a-button type="primary" @click="showAddDeviceTypeModal">
                  新增设备类型
                </a-button>
                <a-button
                  type="danger"
                  :disabled="!selectedDeviceType"
                  @click="confirmDeleteDeviceType"
                >
                  删除设备类型
                </a-button>
              </div>
            </div>

            <a-modal
              v-model="isDeviceTypeModalVisible"
              title="新增设备类型"
              width="800px"
              @ok="handleNewDeviceTypeSubmit"
              @cancel="handleCancel"
              :bodyStyle="{ padding: '24px 40px' }"
            >
              <a-form :model="newDeviceType">
                <a-row :gutter="32">
                  <a-col :span="12">
                    <a-form-item label="设备类型ID" :label-col="{ span: 8 }" :wrapper-col="{ span: 16 }">
                      <a-input v-model="newDeviceType.deviceTypeId" placeholder="请输入唯一标识，如: p_ai_camera" />
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="设备类型名称" :label-col="{ span: 8 }" :wrapper-col="{ span: 16 }">
                      <a-input v-model="newDeviceType.deviceTypeName" placeholder="请输入显示名称，如: AI摄像机" />
                    </a-form-item>
                  </a-col>
                </a-row>

                <a-row :gutter="32">
                  <a-col :span="12">
                    <a-form-item label="设备类型属性" :label-col="{ span: 8 }" :wrapper-col="{ span: 16 }">
                      <a-textarea
                        v-model="newDeviceType.deviceTypeAttributes"
                        placeholder="请输入属性数组，例如: [&quot;视频流地址&quot;, &quot;状态&quot;]"
                        :auto-size="{ minRows: 3, maxRows: 5 }"
                      />
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="设备类型功能" :label-col="{ span: 8 }" :wrapper-col="{ span: 16 }">
                      <a-textarea
                        v-model="newDeviceType.deviceTypeFunction"
                        placeholder="请输入功能数组，例如: [&quot;获取视频流&quot;, &quot;重启&quot;]"
                        :auto-size="{ minRows: 3, maxRows: 5 }"
                      />
                    </a-form-item>
                  </a-col>
                </a-row>

                <a-row :gutter="32">
                  <a-col :span="12">
                    <a-form-item label="设备类型指令" :label-col="{ span: 8 }" :wrapper-col="{ span: 16 }">
                      <a-textarea
                        v-model="newDeviceType.deviceTypeInstruction"
                        placeholder="请输入指令数组，例如: [&quot;开机&quot;, &quot;关机&quot;]"
                        :auto-size="{ minRows: 3, maxRows: 5 }"
                      />
                    </a-form-item>
                  </a-col>
                  <a-col :span="12">
                    <a-form-item label="设备类型事件" :label-col="{ span: 8 }" :wrapper-col="{ span: 16 }">
                      <a-textarea
                        v-model="newDeviceType.deviceTypeEvent"
                        placeholder="请输入事件数组，例如: [&quot;渣土车识别&quot;, &quot;消防通道占用&quot;]"
                        :auto-size="{ minRows: 3, maxRows: 5 }"
                      />
                    </a-form-item>
                  </a-col>
                </a-row>

                <a-row>
                  <a-col :span="24">
                    <a-form-item label="Product JSON" :label-col="{ span: 4 }" :wrapper-col="{ span: 20 }">
                      <a-textarea
                        v-model="newDeviceType.productJson"
                        placeholder="请输入完整的指令 JSON"
                        :auto-size="{ minRows: 4, maxRows: 10 }"
                        style="font-family: monospace;"
                      />
                    </a-form-item>
                  </a-col>
                </a-row>
              </a-form>
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
          <a-col :span="12">
            <a-form-item label="设备状态">
              <a-input v-model="newDeviceInstance.states" placeholder="输入设备状态" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </page-header-wrapper>
</template>

<script>
import axios from 'axios'
// ✅ 1. 显式引入 Modal，解决 this.$confirm 可能无效的问题
import { Modal } from 'ant-design-vue'

export default {
  beforeCreate () {
    this.formDeviceType = this.$form.createForm(this, { name: 'form_device_type' })
    this.formDeviceInstance = this.$form.createForm(this, { name: 'form_device_instance' })
  },
  data () {
    return {
      currentScene: '',
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
        },
        {
          title: '设备类型指令',
          dataIndex: 'deviceTypeInstruction',
          key: 'deviceTypeInstruction',
          align: 'center',
          width: 200,
          customRender: (text) => <div style="white-space: pre-line; max-height: 100px; overflow-y: auto;">{text || '-'}</div>
        },
        {
          title: '设备类型事件',
          dataIndex: 'deviceTypeEvent',
          key: 'deviceTypeEvent',
          align: 'center',
          width: 200,
          customRender: (text) => <div style="white-space: pre-line; max-height: 100px; overflow-y: auto;">{text || '-'}</div>
        },
        {
          title: 'Product JSON',
          dataIndex: 'productJson',
          key: 'productJson',
          align: 'center',
          width: 200,
          ellipsis: true,
          customRender: (text) => <a-tooltip title={text}><span>{text ? 'JSON数据' : '-'}</span></a-tooltip>
        }
      ],
      deviceTypes: [],
      deviceInstanceColumns: [
        { title: '设备序号', dataIndex: 'deviceId', key: 'deviceId', width: 100, align: 'center' },
        { title: '设备名称', dataIndex: 'deviceName', key: 'deviceName', width: 120, align: 'center' },
        { title: '设备所属区域', dataIndex: 'deviceRegion', key: 'deviceRegion', width: 120, align: 'center' },
        { title: '设备状态', dataIndex: 'states', key: 'states', width: 100, align: 'center' },
        { title: '设备可用时间', dataIndex: 'deviceTime', key: 'deviceTime', width: 140, align: 'center' }
      ],
      deviceInstances: [],
      newDeviceType: { deviceTypeId: '', deviceTypeName: '', deviceTypeAttributes: '', deviceTypeFunction: '', deviceTypeInstruction: '', deviceTypeEvent: '', productJson: '' },
      newDeviceInstance: { deviceId: '', deviceName: '', deviceRegion: '', deviceTime: '', states: '' },
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
    let scene = this.$route.query.scene
    if (!scene) {
      scene = localStorage.getItem('current_scene_type')
    }
    if (!scene) {
      scene = 'F-city'
    }
    this.currentScene = scene
    if (this.$route.query.scene !== scene) {
      this.$router.replace({
        path: this.$route.path,
        query: { ...this.$route.query, scene: scene }
      })
    }
    this.fetchDeviceTypes()
    this.fetchDeviceData()
    this.fetchGridList()
  },
  methods: {
    async fetchGridList () {
      try {
        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
        const response = await axios.get(`${baseUrl}/api/meshes/all`, {
          params: { mesh_nature: this.currentScene }
        })
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
        const response = await axios.get(`${baseUrl}/api/devices`, {
          params: {
            project: projectId,
            mesh_nature: this.currentScene
          }
        })
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
        const response = await axios.get(`${baseUrl}/api/deviceTypes/fromTslProduct`, {
          params: { mesh_nature: this.currentScene }
        })
        this.deviceTypes = response.data.map(item => ({
          deviceTypeId: item.deviceTypeId,
          deviceTypeName: item.deviceTypeName,
          deviceTypeAttributes: this.formatArrayField(item.deviceTypeAttributes, '未知属性'),
          deviceTypeFunction: this.formatArrayField(item.deviceTypeFunction, '无特定功能'),
          deviceTypeInstruction: this.formatArrayField(item.deviceTypeInstruction || item.productInstruction, '无指令'),
          deviceTypeEvent: this.formatArrayField(item.deviceTypeEvent || item.productEvent, '无事件'),
          productJson: item.productJson || ''
        }))
      } catch (error) { console.error('获取设备类型失败:', error) }
    },
    async fetchDeviceInstancesByType (prodId) {
      try {
        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
        const response = await axios.get(`${baseUrl}/api/devices/instances`, {
          params: {
            prodId,
            mesh_nature: this.currentScene
          }
        })
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

    // 统一处理行点击逻辑
    handleDeviceTypeClick (record) {
      console.log('点击选中:', record.deviceTypeName)
      this.selectedDeviceType = record
      if (this.fetchDeviceInstancesByType) {
        this.fetchDeviceInstancesByType(record.deviceTypeId)
      }
    },

    // 自定义行点击事件 (绑定到 customRow)
    customRowClick (record) {
      return {
        on: {
          click: () => {
            this.handleDeviceTypeClick(record)
          }
        },
        style: {
          cursor: 'pointer'
        }
      }
    },

    // 设置行样式
    setRowClassName (record) {
      return (this.selectedDeviceType && record.deviceTypeId === this.selectedDeviceType.deviceTypeId)
        ? 'selected-row'
        : ''
    },

    // 触发删除确认弹窗 (使用 Modal.confirm 确保能弹出)
    confirmDeleteDeviceType () {
      if (!this.selectedDeviceType) return

      const that = this
      const typeName = this.selectedDeviceType.deviceTypeName
      const typeId = this.selectedDeviceType.deviceTypeId

      Modal.confirm({
        title: '确认删除',
        content: `确定要删除设备类型“${typeName}”吗？此操作不可恢复。`,
        okText: '确认删除',
        okType: 'danger',
        cancelText: '取消',
        onOk () {
          // 调用真正的删除逻辑
          return that.handleDeleteDeviceType(typeId)
        },
        onCancel () {}
      })
    },

    async handleDeleteDeviceType (deviceTypeId) {
      try {
        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
        await axios.delete(`${baseUrl}/api/deviceTypes/tsl/${deviceTypeId}`)

        this.$message.success('删除成功')

        // 关键：清空选中状态，防止删除后还能点击
        this.selectedDeviceType = null

        // 刷新列表
        this.fetchDeviceTypes()

        // 清空右侧的实例列表
        this.deviceInstances = []
      } catch (error) {
        console.error('删除设备类型失败:', error)
        const msg = error.response?.data?.message || '删除失败，请稍后重试'
        this.$message.error(msg)
      }
    },
    /**
     * 新增设备实例
     */
    async handleNewDeviceInstanceSubmit () {
      // 1. 校验必填项
      if (!this.newDeviceInstance.deviceId || !this.newDeviceInstance.deviceName) {
        this.$message.warning('请填写设备序号和设备名称')
        return
      }

      // 2. 校验是否已选中左侧设备类型
      if (!this.selectedDeviceType) {
        this.$message.warning('请先在左侧选择一个设备类型')
        return
      }

      try {
        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

        // 3. 构造提交参数 (完全匹配后端 Service 接收的 Map)
        const payload = {
          // 必填项
          deviceId: this.newDeviceInstance.deviceId,
          deviceName: this.newDeviceInstance.deviceName,
          deviceTypeId: this.selectedDeviceType.deviceTypeId, // 关联左侧选中的类型ID
          mesh_nature: this.currentScene, // 当前场景 (F-city 等)

          // 选填项
          deviceRegion: this.newDeviceInstance.deviceRegion, // 对应后端的 meshName
          deviceTime: this.newDeviceInstance.deviceTime, // 对应 createdAt
          states: this.newDeviceInstance.states // 对应 status (支持输入 "在线" 或 "1")
        }

        console.log('正在提交设备实例:', payload)

        // 4. 发送 POST 请求
        const res = await axios.post(`${baseUrl}/api/devices/instances`, payload)

        if (res.status === 200 || res.status === 201) {
          this.$message.success('设备实例添加成功')
          this.isDeviceInstanceModalVisible = false

          // 5. 清空表单
          this.newDeviceInstance = {
            deviceId: '',
            deviceName: '',
            deviceRegion: '',
            deviceTime: '',
            states: ''
          }

          // 6. 刷新右侧列表
          this.fetchDeviceInstancesByType(this.selectedDeviceType.deviceTypeId)
        }
      } catch (error) {
        console.error(error)
        const msg = error.response?.data?.message || '添加失败，请检查设备序号是否重复'
        this.$message.error(msg)
      }
    },
    /**
     * 批量删除设备实例
     */
    async confirmDeleteDevice () {
      if (this.selectedInstanceKeys.length === 0) return

      try {
        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

        console.log('正在删除设备实例:', this.selectedInstanceKeys)

        // 发送 DELETE 请求
        // 注意：axios.delete 的第二个参数是 config，body 需要放在 data 字段里
        await axios.delete(`${baseUrl}/api/devices/batch`, {
          data: this.selectedInstanceKeys
        })

        this.$message.success('删除成功')
        this.isDeleteModalVisible = false
        this.selectedInstanceKeys = [] // 清空选中状态

        // 刷新列表
        if (this.selectedDeviceType) {
          this.fetchDeviceInstancesByType(this.selectedDeviceType.deviceTypeId)
        }
      } catch (error) {
        console.error('删除失败:', error)
        const msg = error.response?.data?.message || '删除失败'
        this.$message.error(msg)
      }
    },

    showAddDeviceTypeModal () { this.isDeviceTypeModalVisible = true },
    showDeviceInstanceModal () { this.isDeviceInstanceModalVisible = true },

    async handleNewDeviceTypeSubmit () {
      if (!this.newDeviceType.deviceTypeId || !this.newDeviceType.deviceTypeName) {
        this.$message.warning('请填写完整的设备类型信息')
        return
      }
      try {
        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
        const payload = {
          ...this.newDeviceType,
          mesh_nature: this.currentScene
        }
        const res = await axios.post(`${baseUrl}/api/deviceTypes/add`, payload)
        if (res.status === 200 || res.status === 201) {
          this.$message.success('设备类型添加成功')
          this.isDeviceTypeModalVisible = false
          this.newDeviceType = {
            deviceTypeId: '', deviceTypeName: '', deviceTypeAttributes: '', deviceTypeFunction: '', deviceTypeInstruction: '', deviceTypeEvent: '', productJson: ''
          }
          this.fetchDeviceTypes()
        }
      } catch (error) {
        console.error(error)
        const msg = error.response?.data?.message || '添加失败'
        this.$message.error(msg)
      }
    },

    handleCancel () { this.isDeviceTypeModalVisible = false },
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

.full-height-card >>> .ant-card-body {
  height: calc(100% - 58px);
  display: flex;
  flex-direction: column;
}
.selected-row {
  background-color: #e6f7ff !important;
  cursor: pointer;
}
.ant-table-tbody > tr:hover:not(.ant-table-expanded-row):not(.ant-table-row-selected) > td {
  background-color: #f5f5f5;
  cursor: pointer;
}

@media (max-width: 768px) { .ant-col-12 { width: 100% !important; margin-bottom: 16px; } }
</style>
