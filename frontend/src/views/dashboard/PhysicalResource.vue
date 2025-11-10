<template>
  <page-header-wrapper>
    <!-- 导航栏区域 - 参考信息资源页面的格式 -->
    <!-- 主要内容区域 - 两个表格并排显示 -->
    <div style="padding: 0 12px 24px 12px;">
      <a-row :gutter="16" style="height: calc(100vh - 180px);">
        <!-- 左侧：设备类型表格 -->
        <a-col :span="12">
          <a-card title="设备类型" bordered :style="{ borderRadius: '8px', height: '600px' }">
            <div style="height: calc(100% - 60px); display: flex; flex-direction: column;">
              <a-table
                :columns="deviceTypeColumns"
                :dataSource="deviceTypes"
                row-key="deviceTypeId"
                :pagination="false"
                :scroll="{ x: 900, y: 400 }"
                @click="handleDeviceTypeClick"
                :customRow="customRowClick"
                style="flex: 1;"
                :row-class-name="rowClassName"
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
          <a-card
            :title="deviceInstanceTitle"
            bordered
            :style="{ borderRadius: '8px', height: '600px' }"
          >
            <div style="height: calc(100% - 60px); display: flex; flex-direction: column;">

              <!-- 设备实例表格 -->
              <a-table
                :columns="deviceInstanceColumns"
                :dataSource="filteredDeviceInstances"
                row-key="deviceId"
                :pagination="false"
                :scroll="{ x: 700, y: 350 }"
                :row-selection="{ selectedRowKeys: selectedInstanceKeys, onChange: onSelectChange}"
                style="flex: 1; margin-bottom: 16px;"
                :locale="{ emptyText: selectedDeviceType ? '该设备类型暂无设备实例' : '请先选择设备类型' }"
              />

              <!-- 操作按钮区域 -->
              <div style="display: flex; justify-content: flex-start; gap: 8px;">
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

    <!-- 删除确认弹窗 -->
    <a-modal
      v-model="isDeleteModalVisible"
      title="确认删除"
      @ok="confirmDeleteDevice"
      @cancel="cancelDeleteDevice"
      ok-text="确认"
      cancel-text="取消"
    >
      <p>确定要删除选中的 {{ selectedInstanceKeys.length }} 个设备实例吗？此操作不可恢复。</p>
      <ul v-if="selectedDeviceNames.length > 0">
        <li v-for="name in selectedDeviceNames" :key="name">{{ name }}</li>
      </ul>
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
      isDeleteModalVisible: false, // 控制删除确认弹窗
      refreshedDeviceData: {},
      queryType: '0',
      isDeviceTypeModalVisible: false, // 控制弹窗显示状态
      isDeviceInstanceModalVisible: false,

      // 设备类型表格列定义
      deviceTypeColumns: [
        {
          title: '设备类型ID',
          dataIndex: 'deviceTypeId',
          key: 'deviceTypeId',
          align: 'center',
          width: 180,
          ellipsis: false
        },
        {
          title: '设备类型名称',
          dataIndex: 'deviceTypeName',
          key: 'deviceTypeName',
          align: 'center',
          width: 180,
          ellipsis: false
        },
        {
          title: '设备类型属性',
          dataIndex: 'deviceTypeAttributes',
          key: 'deviceTypeAttributes',
          align: 'center',
          width: 250,
          customRender: (text) => {
            if (!text) return '-'
            // 将换行符替换为 <br> 标签
            return <div style="white-space: pre-line;">{text}</div>
          }
        },
        {
          title: '设备类型功能',
          dataIndex: 'deviceTypeFunction',
          key: 'deviceTypeFunction',
          align: 'center',
          width: 250,
          customRender: (text) => {
            if (!text) return '-'
            return <div style="white-space: pre-line;">{text}</div>
          }
        }
      ],

      deviceTypes: [], // 后端获取的设备类型列表

      // 设备实例表格列定义（保持原有的列）
      deviceInstanceColumns: [
        { title: '设备序号', dataIndex: 'deviceId', key: 'deviceId', width: 100, align: 'center' },
        { title: '设备名称', dataIndex: 'deviceName', key: 'deviceName', width: 120, align: 'center' },
        { title: '设备所属区域', dataIndex: 'deviceRegion', key: 'deviceRegion', width: 120, align: 'center' },
        { title: '设备状态', dataIndex: 'states', key: 'states', width: 100, align: 'center' },
        { title: '设备可用时间', dataIndex: 'deviceTime', key: 'deviceTime', width: 140, align: 'center' },
        { title: '操作', dataIndex: 'operation', key: 'operation', width: 120, align: 'center' }
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
  computed: {
    // 根据选中的设备类型过滤设备实例
    filteredDeviceInstances () {
      if (!this.selectedDeviceType) {
        return [] // 没有选中设备类型时，不显示任何设备实例
      }
      return this.deviceInstances.filter(
        instance => instance.deviceTypeId === this.selectedDeviceType.deviceTypeId
      )
    },

    // 动态设备实例表格标题
    deviceInstanceTitle () {
      if (this.selectedDeviceType) {
        return `设备实例 (${this.selectedDeviceType.deviceTypeName})`
      }
      return '设备实例'
    },

    // 获取选中设备的名称列表，用于删除确认弹窗显示
    selectedDeviceNames () {
      return this.filteredDeviceInstances
        .filter(device => this.selectedInstanceKeys.includes(device.deviceId))
        .map(device => device.deviceName)
    }
  },
  watch: {
    // 当选中的设备类型改变时，清空设备实例的选中状态
    selectedDeviceType () {
      this.selectedInstanceKeys = []
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
        // const projectId = localStorage.getItem('project_id')
        const projectId = '1'
        if (!projectId) {
          console.error('未找到 project_id')
          this.$message.error('未找到项目ID，请先选择项目')
          return
        }

        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
        console.log('正在请求设备数据，URL:', `${baseUrl}/api/devices?project=${projectId}`)

        // 发起带有 projectId 的 API 请求
        const response = await axios.get(`${baseUrl}/api/devices`, {
          params: {
            project: projectId // 作为查询参数发送 projectId
          }
        })

        console.log('设备实例API 返回的数据:', response.data) // 打印返回的数据
        const rawData = response.data

        // 检查返回的数据是否为数组
        if (!Array.isArray(rawData)) {
          console.error('API返回的数据不是数组格式:', rawData)
          this.$message.error('数据格式错误')
          return
        }

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

        console.log('处理后的设备实例数据:', this.deviceInstances)
      } catch (error) {
        console.error('获取设备数据时出错:', error)
        console.error('错误详情:', error.response?.data || error.message)
        this.$message.error(`获取设备数据失败: ${error.response?.data?.message || error.message}`)
      }
    },

    async fetchDeviceTypes () {
      this.loading = true
      try {
        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
        const response = await axios.get(`${baseUrl}/api/deviceTypes/fromTslProduct`)
        // tsl_product 中字段：product_id, product_name, product_describe, product_function
        this.deviceTypes = response.data.map(item => ({
          deviceTypeId: item.deviceTypeId,
          deviceTypeName: item.deviceTypeName,
          deviceTypeAttributes: this.formatArrayField(item.deviceTypeAttributes, '未知属性'),
          deviceTypeFunction: this.formatArrayField(item.deviceTypeFunction, '无特定功能')
        }))
      } catch (error) {
        console.error('获取设备类型失败:', error)
      } finally {
        this.loading = false
      }
    },

    async fetchDeviceInstancesByType (prodId) {
      try {
        this.loading = true
        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
        console.log('请求设备实例接口:', `${baseUrl}/api/devices/instances?prodId=${prodId}`)

        const response = await axios.get(`${baseUrl}/api/devices/instances`, {
          params: { prodId }
        })

        const rawData = response.data?.data || response.data || []
        console.log('设备实例接口返回:', rawData)

        // 解析接口返回数据，映射成表格字段
        this.deviceInstances = rawData.map(device => ({
          deviceId: device.deviceId || device.devId || '-',
          deviceName: device.deviceName || device.devName || '未命名设备',
          deviceRegion: device.region || '未知区域',
          deviceTime: device.lastUpdateTime || '未知时间',
          states: Array.isArray(device.states)
            ? device.states.map(s => `${s.stateKey}: ${s.stateValue}`).join(', ')
            : '离线',
          operation: Array.isArray(device.functions)
            ? device.functions.map(f => f.functionName).join(', ')
            : '无可用操作',
          deviceTypeId: prodId
        }))
      } catch (error) {
        console.error('获取设备实例失败:', error)
        this.$message.error('加载设备实例失败，请稍后重试')
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

    onSelectChange (selectedRowKeys) {
      this.selectedInstanceKeys = selectedRowKeys
    },

    // 删除设备实例
    deleteDeviceInstance () {
      if (this.selectedInstanceKeys.length === 0) {
        this.$message.warning('请选择要删除的设备实例')
        return
      }
      // 显示删除确认弹窗
      this.isDeleteModalVisible = true
    },

    formatArrayField (field, fallbackText) {
      if (!field) return fallbackText

      // 如果是数组，直接用换行连接
      if (Array.isArray(field)) {
        return field.join('\n')
      }

      // 如果是字符串，尝试解析 JSON
      if (typeof field === 'string') {
        try {
          const parsed = JSON.parse(field)
          if (Array.isArray(parsed)) {
            return parsed.join('\n')
          }
          return String(parsed)
        } catch (e) {
          // 如果不是 JSON 格式，去掉括号和引号再换行分隔
          return field
            .replace(/[\\[\]"]/g, '')
            .replace(/,/g, '\n')
            .trim()
        }
      }

      // 其他情况直接转为字符串
      return String(field)
    },

    // 确认删除设备
    async confirmDeleteDevice () {
      try {
        // 从本地数据中删除选中的设备实例
        this.deviceInstances = this.deviceInstances.filter(
          device => !this.selectedInstanceKeys.includes(device.deviceId)
        )

        // 清空选中状态
        this.selectedInstanceKeys = []

        // 关闭删除确认弹窗
        this.isDeleteModalVisible = false

        // 显示成功消息
        this.$message.success('设备实例删除成功')
      } catch (error) {
        console.error('删除设备实例时出错:', error)
        this.$message.error('删除失败，请稍后重试')
      }
    },

    // 取消删除
    cancelDeleteDevice () {
      this.isDeleteModalVisible = false
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

    // 设备类型行点击事件
    async handleDeviceTypeClick (record) {
      this.selectedDeviceType = record
      this.selectedRowKeys = [record.deviceTypeId]
      this.deviceInstances = [] // 清空旧数据
      this.$message.info(`加载 ${record.deviceTypeName} 的设备实例...`)
      await this.fetchDeviceInstancesByType(record.deviceTypeId)
    },

    // 设备类型行样式设置
    customRowClick (record) {
      return {
        on: {
          click: () => this.handleDeviceTypeClick(record)
        },
        style: {
          cursor: 'pointer'
        }
      }
    },

    // 设备类型行类名设置（用于高亮选中行）
    rowClassName (record) {
      return this.selectedRowKeys.includes(record.deviceTypeId) ? 'selected-row' : ''
    },

    showAddDeviceTypeModal () {
      // 打开新增设备类型的弹窗
      this.isDeviceTypeModalVisible = true
    },

    showDeviceInstanceModal () {
      if (!this.selectedDeviceType) {
        this.$message.warning('请先选择设备类型')
        return
      }
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

        this.$message.success('设备类型添加成功')
      } else {
        // 如果输入框有空值，显示提示
        this.$message.error('请完整填写设备类型信息')
      }
    },

    handleCancel () {
      // 关闭弹窗并清空表单
      this.isDeviceTypeModalVisible = false
      this.newDeviceType = {
        deviceTypeId: '',
        deviceTypeName: '',
        deviceTypeAttributes: '',
        deviceTypeFunction: ''
      }
    },

    handleNewDeviceInstanceSubmit () {
      // 校验必填字段是否为空
      if (this.newDeviceInstance.deviceId && this.newDeviceInstance.deviceName) {
        // 向设备实例列表中添加新设备
        this.deviceInstances.push({
          ...this.newDeviceInstance,
          deviceTypeId: this.selectedDeviceType.deviceTypeId, // 关联到当前选中的设备类型
          deviceTypeName: this.selectedDeviceType.deviceTypeName
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

        // 关闭弹窗
        this.isDeviceInstanceModalVisible = false

        this.$message.success('设备实例添加成功')
      } else {
        this.$message.error('请输入设备编号和设备名称')
      }
    },

    handleCancelDeviceInstance () {
      this.isDeviceInstanceModalVisible = false
      this.newDeviceInstance = {
        deviceId: '',
        deviceName: '',
        deviceRegion: '',
        deviceTime: '',
        states: '',
        operation: ''
      }
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
/* 自定义页面内容边距 */
.custom-page-content {
  margin: 0 -12px;
}

.ant-form-item {
  margin-bottom: 12px;
}

.ant-table-row {
  cursor: pointer;
}

/* 选中行的高亮样式 */
.ant-table-tbody .selected-row {
  background-color: #e6f7ff !important;
}

.ant-table-tbody .selected-row:hover {
  background-color: #bae7ff !important;
}

/* 表格单元格支持换行显示内容 */
.ant-table-tbody > tr > td {
  white-space: pre-line !important;
  text-align: center;
  padding: 8px 12px;
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

/* 表格标题样式 - 防止换行 */
.ant-table-thead > tr > th {
  white-space: nowrap;
  text-align: center;
  padding: 8px 4px;
  font-size: 13px;
}

/* 表格内容居中对齐 */
.ant-table-tbody > tr > td {
  text-align: center;
  padding: 8px 12px;
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

/* 保证表格内容不换行 */
.ant-table-thead > tr > th,
.ant-table-tbody > tr > td {
  white-space: nowrap !important;
  overflow: hidden;
  text-overflow: ellipsis;
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
