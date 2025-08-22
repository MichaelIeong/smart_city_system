<template>
  <page-header-wrapper>
    <!-- 导航栏区域 - 参考信息资源页面的格式 -->
    <!-- 主要内容区域 - 两个表格并排显示 -->
    <div style="padding: 0 24px 24px 24px;">
      <a-row :gutter="24" style="height: calc(100vh - 180px);">
        <!-- 左侧：设备类型表格 -->
        <a-col :span="12">
          <a-card title="设备类型" bordered :style="{ borderRadius: '8px', height: '100%' }">
            <div style="height: calc(100% - 60px); display: flex; flex-direction: column;">
              <a-table
                :columns="deviceTypeColumns"
                :dataSource="deviceTypes"
                row-key="deviceTypeId"
                :pagination="false"
                :scroll="{ y: 'calc(100vh - 430px)' }"
                @click="handleDeviceTypeClick"
                :customRow="customRowClick"
                style="flex: 1;"
              />

              <div style="margin-top: 16px;">
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

        <!-- 右侧：设备实例表格 -->
        <a-col :span="12">
          <a-card title="设备实例" bordered :style="{ borderRadius: '8px', height: '100%' }">
            <div style="height: calc(100% - 60px); display: flex; flex-direction: column;">
              <!-- 查询条件 -->
              <a-row gutter="{16}" style="margin-bottom: 16px;">
                <a-col :md="16" :sm="24">
                  <a-select
                    v-model="queryType"
                    placeholder="请选择设备类型"
                    default-value="0"
                    style="width: 100%;"
                  >
                    <a-select-option value="0">全部</a-select-option>
                    <a-select-option
                      v-for="device in deviceTypes"
                      :key="device.deviceTypeId"
                      :value="device.deviceTypeName">
                      {{ device.deviceTypeName }}
                    </a-select-option>
                  </a-select>
                </a-col>
                <a-col :md="8" :sm="24">
                  <a-button type="primary" @click="filterData" style="width: 100%;">
                    查询
                  </a-button>
                </a-col>
              </a-row>

              <!-- 设备实例表格 -->
              <a-table
                :columns="deviceInstanceColumns"
                :dataSource="filteredDeviceInstances"
                row-key="deviceId"
                :pagination="false"
                :scroll="{ y: 'calc(100vh - 530px)' }"
                :row-selection="{ selectedRowKeys: selectedInstanceKeys, onChange: onSelectChange}"
                style="flex: 1;"
              />

              <!-- 操作按钮区域 -->
              <div style="margin-top: 16px;">
                <a-button type="primary" @click="showDeviceInstanceModal">
                  新增设备实例
                </a-button>
                <a-button type="default" style="margin-left: 8px;" @click="refreshDevice" :disabled="selectedInstanceKeys.length !== 1">
                  刷新
                </a-button>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>

    <!-- 刷新设备信息弹窗 -->
    <a-modal
      v-model="isRefreshModelVisible"
      title="设备实例最新信息"
      @ok="handleRefreshOk"
      @cancel="handleRefreshCancel"
    >
      <a-descriptions bordered :column="1">
        <a-descriptions-item label="设备序号">{{ refreshedDeviceData.deviceId }}</a-descriptions-item>
        <a-descriptions-item label="设备名称">{{ refreshedDeviceData.deviceName }}</a-descriptions-item>
        <a-descriptions-item label="最新状态">{{ refreshedDeviceData.states }}</a-descriptions-item>
        <a-descriptions-item label="详细信息">{{ refreshedDeviceData.fixedProperties }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 新增设备实例弹窗 -->
    <a-modal
      v-model="isDeviceInstanceModalVisible"
      title="新增设备实例"
      @ok="handleNewDeviceInstanceSubmit"
      @cancel="handleCancelDeviceInstance"
    >
      <!-- 弹窗中的表单内容 -->
      <a-form @submit.prevent="addDeviceInstance">
        <a-row :gutter="16">
          <!-- 第一行：三个输入框 -->
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
          <!-- 第二行：三个输入框 -->
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
  // 添加 form 属性用于 Ant Design Vue 表单校验
  beforeCreate () {
    this.formDeviceType = this.$form.createForm(this, { name: 'form_device_type' })
    this.formDeviceInstance = this.$form.createForm(this, { name: 'form_device_instance' })
  },
  data () {
    return {
      selectedDeviceType: null, // 当前选中的设备类型
      selectedRowKeys: [],
      selectedInstanceKeys: [], // 存储选中的设备实例key
      isRefreshModelVisible: false,
      refreshedDeviceData: {},
      queryType: '0',
      isDeviceTypeModalVisible: false, // 控制弹窗显示状态
      isDeviceInstanceModalVisible: false,
      filteredDeviceInstances: [], // 过滤后的设备实例数据

      // 设备类型表格列定义（保持原有的列）
      deviceTypeColumns: [
        { title: '设备类型序号', dataIndex: 'deviceTypeId', key: 'deviceTypeId' },
        { title: '设备类型名称', dataIndex: 'deviceTypeName', key: 'deviceTypeName' },
        { title: '设备类型属性', dataIndex: 'deviceTypeAttributes', key: 'deviceTypeAttributes' },
        { title: '设备类型功能', dataIndex: 'deviceTypeFunction', key: 'deviceTypeFunction' }
      ],

      deviceTypes: [], // 后端获取的设备类型列表

      // 设备实例表格列定义（保持原有的列）
      deviceInstanceColumns: [
        { title: '设备序号', dataIndex: 'deviceId', key: 'deviceId', width: 100 },
        { title: '设备名称', dataIndex: 'deviceName', key: 'deviceName', width: 150 },
        { title: '设备所属区域', dataIndex: 'deviceRegion', key: 'deviceRegion', width: 150 },
        { title: '设备状态', dataIndex: 'states', key: 'states', width: 100 },
        { title: '设备可用时间', dataIndex: 'deviceTime', key: 'deviceTime', width: 200 },
        { title: '操作', dataIndex: 'operation', key: 'operation', width: 200 }
      ],

      deviceInstances: [], // 后端获取的设备实例列表

      newDeviceType: {
        deviceTypeId: '',
        deviceTypeName: '',
        deviceTypeAttributes: '',
        deviceTypeFunction: ''
      }, // 新增设备类型

      newDeviceInstance: {
        deviceId: '',
        deviceName: '',
        deviceRegion: '',
        deviceTime: '',
        states: '',
        operation: ''
      }, // 新增设备实例
      loading: false // 用于显示加载状态
    }
  },
  mounted () {
    this.fetchDeviceTypes() // 页面加载时调用API获取设备类型数据
    this.fetchDeviceData() // 获取设备实例数据
  },
  methods: {
    async fetchDeviceData () {
      try {
        // 从 localStorage 获取保存的 projectId
        const projectId = localStorage.getItem('project_id')
        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

        // 发起带有 projectId 的 API 请求
        const response = await axios.get(`${baseUrl}/api/devices`, {
          params: {
            project: projectId // 作为查询参数发送 projectId
          }
        })

        console.log('设备实例API 返回的数据:', response.data) // 打印返回的数据
        const rawData = response.data

        // 将API返回的数据映射到原始代码的列结构
        this.deviceInstances = rawData.map(device => ({
          deviceId: device.deviceId,
          deviceName: device.deviceName,
          deviceRegion: '未设置', // API中没有此字段，设置默认值
          states: this.parseStates(device.states || []), // 解析设备状态数组
          deviceTime: device.lastUpdateTime || '未知', // 使用lastUpdateTime作为可用时间
          operation: this.parseFunctions(device.functions || []), // 使用functions作为操作
          // 保留一些必要的隐藏字段用于过滤等功能
          deviceTypeName: device.deviceTypeName,
          deviceTypeId: device.deviceTypeId
        }))

        // 初始化 filteredDeviceInstances
        this.filteredDeviceInstances = [...this.deviceInstances]
      } catch (error) {
        console.error('获取设备数据时出错:', error)
        this.$message.error('获取设备数据失败')
      }
    },

    async fetchDeviceTypes () {
      this.loading = true
      try {
        // 从 localStorage 获取保存的 projectId
        const projectId = localStorage.getItem('project_id')
        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

        // 发起带有 projectId 的 API 请求
        const response = await axios.get(`${baseUrl}/api/deviceTypes`, {
          params: {
            project: projectId // 作为查询参数发送 projectId
          }
        })

        console.log('设备类型API 返回的数据:', response.data)
        const deviceData = response.data

        // 将API返回的数据映射到原始代码的列结构
        this.deviceTypes = deviceData.map(device => ({
          deviceTypeId: device.deviceTypeId,
          deviceTypeName: device.deviceTypeName,
          deviceTypeAttributes: device.isSensor ? '传感器设备' : '执行器设备', // 将isSensor映射为属性
          deviceTypeFunction: device.actuatingFunctions && device.actuatingFunctions.length > 0
            ? device.actuatingFunctions.map(func => func.name || 'Unknown').join(', ')
            : '无特定功能'
        }))
      } catch (error) {
        console.error('获取设备类型数据时出错:', error)
        this.$message.error('获取设备类型数据失败')
      } finally {
        this.loading = false
      }
    },

    // 解析设备状态数组，返回状态的可读格式
    parseStates (states) {
      if (Array.isArray(states) && states.length > 0) {
        return states
          .map(state => `${state.stateKey || 'Unknown'}: ${state.stateValue || 'Unknown'}`)
          .join(', ')
      }
      return '离线'
    },

    // 解析设备功能，作为操作列显示
    parseFunctions (functions) {
      if (Array.isArray(functions) && functions.length > 0) {
        return functions
          .map(func => func.functionName || 'Unknown')
          .join(', ')
      }
      return '无可用操作'
    },

    filterData () {
      this.filteredDeviceInstances = this.deviceInstances.filter(item => {
        const matchesType = this.queryType === '0' || (item.deviceTypeName && item.deviceTypeName === this.queryType)
        return matchesType
      })
    },

    onSelectChange (selectedRowKeys) {
      this.selectedInstanceKeys = selectedRowKeys
    },

    async refreshDevice () {
      if (this.selectedInstanceKeys.length !== 1) {
        this.$message.warning('请选择一个设备实例进行刷新')
        return
      }
      const deviceId = this.selectedInstanceKeys[0]
      const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

      try {
        // 调用刷新设备信息的API
        const response = await axios.get(`${baseUrl}/api/devices/${deviceId}`)
        const refreshedData = response.data

        console.log('刷新设备API 返回的数据:', refreshedData)

        // 格式化数据显示在弹窗中
        this.refreshedDeviceData = {
          deviceId: refreshedData.deviceId,
          deviceName: refreshedData.deviceName,
          states: this.parseStates(refreshedData.states || []),
          fixedProperties: refreshedData.fixedProperties || '无详细信息'
        }
        this.isRefreshModelVisible = true
      } catch (error) {
        console.error('刷新设备数据时出错:', error)
        this.$message.error('刷新失败，请稍后重试')
      }
    },

    handleRefreshOk () {
      this.isRefreshModelVisible = false
      this.refreshedDeviceData = {}
    },

    handleRefreshCancel () {
      this.isRefreshModelVisible = false
      this.refreshedDeviceData = {}
    },

    handleDeviceTypeClick (record) {
      // 点击设备类型行，根据deviceTypeId过滤设备实例
      this.filteredDeviceInstances = this.deviceInstances.filter(
        (instance) => instance.deviceTypeId === record.deviceTypeId
      )
      // 更新选中行的key
      this.selectedRowKeys = [record.deviceTypeId]
    },

    customRowClick (record) {
      return {
        on: {
          click: () => this.handleDeviceTypeClick(record)
        },
        style: {
          cursor: 'pointer',
          backgroundColor: this.selectedRowKeys.includes(record.deviceTypeId) ? '#e6f7ff' : ''
        }
      }
    },

    showAddDeviceTypeModal () {
      // 打开新增设备类型的弹窗
      this.isDeviceTypeModalVisible = true
    },

    showDeviceInstanceModal () {
      this.selectedDeviceType = null // 重置选中的设备类型
      this.isDeviceInstanceModalVisible = true
    },

    handleNewDeviceTypeSubmit () {
      // 验证输入框是否有值，确保不会提交空值
      if (
        this.newDeviceType.deviceTypeId &&
        this.newDeviceType.deviceTypeName &&
        this.newDeviceType.deviceTypeAttributes &&
        this.newDeviceType.deviceTypeFunction
      ) {
        // 将输入的数据新增到表单
        this.deviceTypes.push({
          id: this.deviceTypes.length + 1, // 模拟唯一id
          ...this.newDeviceType
        })

        // 打印新增的设备类型
        console.log('新增设备类型:', this.deviceTypes)

        // 重置输入框内容
        this.newDeviceType = {
          deviceTypeId: '',
          deviceTypeName: '',
          deviceTypeAttributes: '',
          deviceTypeFunction: ''
        }

        // 关闭弹窗
        this.isDeviceTypeModalVisible = false
      } else {
        // 如果输入框有空值，显示提示
        this.$message.error('请完整填写设备类型信息')
      }
    },

    handleCancel () {
      // 关闭弹窗并清空表单
      this.isDeviceTypeModalVisible = false
    },

    handleNewDeviceInstanceSubmit () {
      // 校验必填字段是否为空
      if (this.newDeviceInstance.deviceId && this.newDeviceInstance.deviceName) {
        // 向设备实例列表中添加新设备
        this.deviceInstances.push({
          id: this.deviceInstances.length + 1, // 假设设备实例的 ID 为列表长度 + 1
          ...this.newDeviceInstance,
          deviceTypeId: this.selectedDeviceType // 确保这个属性存在并且有值
        })

        // 清空表单数据
        this.newDeviceInstance = {
          deviceId: '',
          deviceName: '',
          deviceRegion: '',
          deviceTime: '',
          states: '',
          operation: ''
        }

        // 更新过滤后的设备实例
        this.filterData() // 先过滤数据
        // 关闭弹窗
        this.isDeviceInstanceModalVisible = false
      } else {
        console.error('请输入设备编号和设备名称')
      }
    },

    handleCancelDeviceInstance () {
      this.isDeviceInstanceModalVisible = false
    },

    // 新增设备实例
    addDeviceInstance () {
      // 将新增的设备实例数据添加到表格中
      this.deviceInstances.push({
        deviceId: this.newDevice.deviceId,
        deviceName: this.newDevice.deviceName,
        deviceRegion: this.newDevice.deviceRegion,
        deviceTime: this.newDevice.deviceTime,
        states: this.newDevice.states,
        operation: this.newDevice.operation
      })
      // 清空输入框
      this.newDevice = {
        deviceId: '',
        deviceName: '',
        deviceRegion: '',
        deviceTime: '',
        states: '',
        operation: ''
      }
    }
  }
}
</script>

<style scoped>
.ant-form-item {
  margin-bottom: 12px;
}

.ant-table-row {
  cursor: pointer;
}

/* 确保卡片内容区域高度合适 */
.ant-card-body {
  height: calc(100% - 57px);
  padding: 24px;
}

/* 表格容器样式 */
.ant-table-wrapper {
  height: 100%;
}

/* 面包屑导航样式 */
.ant-breadcrumb {
  font-size: 14px;
}

.ant-breadcrumb a {
  color: #1890ff;
  text-decoration: none;
}

.ant-breadcrumb a:hover {
  color: #40a9ff;
}

/* 页面标题样式 */
h1 {
  font-size: 20px;
  font-weight: 500;
  margin: 0;
  color: rgba(0, 0, 0, 0.85);
  line-height: 1.35;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .ant-col-12 {
    width: 100% !important;
    margin-bottom: 16px;
  }
}
</style>
