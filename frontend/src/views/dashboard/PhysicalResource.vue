<template>
  <div style="height: 100%">
    <a-row :gutter="24" style="height: 50% ;margin-bottom: 2%; ">
      <a-col :span="24">
        <a-card title="设备类型" bordered :style="{ borderRadius: '8px' }">
          <a-table
            :columns="deviceTypeColumns"
            :dataSource="deviceTypes"
            row-key="id"
            :pagination="false"
            :scroll="{ y: 250 }"
          />

          <a-button type="primary" @click="showAddDeviceTypeModal" style="margin-top: 16px;">新增设备类型</a-button>
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
                <a-form-item label="设备种类">
                  <a-input v-model="newDeviceType.isSensor" placeholder="输入设备种类" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="设备类型能力">
                  <a-input v-model="newDeviceType.actuatingFunctions" placeholder="输入设备类型能力" />
                </a-form-item>
              </a-col>
            </a-row>
          </a-modal>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="24" style="height: 50% ">
      <a-col :span="24">
        <a-card title="设备实例" bordered :style="{ borderRadius: '8px' }">
          <a-row gutter="{16}">
            <a-col :md="8" :sm="24">
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
            <a-col :md="!advanced && 8 || 24" :sm="24">
              <span
                class="table-page-search-submitButtons"
                :style="advanced && { float: 'right', overflow: 'hidden' } || {} ">
                <a-button type="primary" @click="filterData" style="margin-left: 8px;">
                  查询
                </a-button>

              </span>
            </a-col>
          </a-row>

          <!-- 设备实例表格 -->
          <a-table
            :columns="deviceInstanceColumns"
            :dataSource="filteredDeviceInstances"
            row-key="id"
            :pagination="false"
            :scroll="{ y: 300 }"
            style="margin-top: 20px;"
          >
            <span slot="configuration" slot-scope="text, record">
              <template>
                <a @click="showConfigurationForm(record.deviceId)">查看</a>
              </template>
            </span>
            <span slot="lha" slot-scope="text, record">
              <template>
                <a @click="showLhaModel(record.deviceId)">查看</a>
              </template>
            </span>
          </a-table>
          <!-- 新增设备实例按钮 -->
          <a-row style="margin-top: 16px;">
            <a-col>
              <a-button type="primary" @click="showDeviceInstanceModal">
                新增设备实例
              </a-button>
            </a-col>
          </a-row>
        </a-card>
      </a-col>
    </a-row>

    <!-- 设备配置表单弹窗 -->
    <a-modal
      v-model="isConfigurationModalVisible"
      title="设备配置"
      @ok="handleSubmitConfiguration"
      @cancel="handleCancelConfiguration"
      width="800px"
    >
      <a-form :model="currentDeviceConfig">
        <a-form :model="currentDeviceConfig">
          <a-row gutter="16" style="margin-bottom: 12px;">
            <!-- 设备ID -->
            <a-col :span="12">
              <a-form-item label="设备ID" style="margin-bottom: 0;">
                <a-input v-model="currentDeviceConfig.deviceId" disabled placeholder="设备ID" />
              </a-form-item>
            </a-col>

            <!-- 设备名称 -->
            <a-col :span="12">
              <a-form-item label="设备名称" style="margin-bottom: 0;">
                <a-input v-model="currentDeviceConfig.deviceName" placeholder="请输入设备名称" />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>

        <!-- 动态状态配置 -->
        <a-form-item label="状态" style="margin-bottom: 12px;">
          <a-row :gutter="16" style="margin-bottom: 16px;">
            <!-- 这里动态生成状态 -->
            <a-col :span="24" v-for="(state, index) in currentDeviceConfig.states" :key="index">
              <a-card :title="'状态 ' + (index + 1)" bordered style="margin-bottom: 12px; padding: 12px;">
                <a-row gutter="{8}">
                  <!-- 状态名称 -->
                  <a-col :span="8">
                    <a-form-item label="状态名称" style="margin-bottom: 8px;">
                      <a-input v-model="state.stateName" placeholder="请输入状态名称" />
                    </a-form-item>
                  </a-col>
                  <!-- 方程 -->
                  <a-col :span="16">
                    <a-form-item label="方程" style="margin-bottom: 8px;">
                      <a-input v-model="state.equation" placeholder="请输入状态方程" />
                    </a-form-item>
                  </a-col>
                </a-row>

                <!-- 状态转移 -->
                <a-form-item label="状态转移" style="margin-bottom: 8px;">
                  <a-row gutter="{8}">
                    <a-col :span="8">
                      <!-- 目标状态 -->
                      <a-form-item label="目标状态" style="margin-bottom: 8px;">
                        <a-input v-model="state.transitions[0].toState" placeholder="请输入目标状态" />
                      </a-form-item>
                    </a-col>
                    <a-col :span="16">
                      <!-- 转移条件 -->
                      <a-form-item label="转移条件" style="margin-bottom: 8px;">
                        <a-input v-model="state.transitions[0].condition" placeholder="请输入转移条件" />
                      </a-form-item>
                    </a-col>
                  </a-row>
                </a-form-item>

                <!-- 删除状态按钮 -->
                <a-button type="danger" icon="minus" @click="removeState(index)" style="margin-top: 8px;">
                  删除状态
                </a-button>
              </a-card>
            </a-col>
          </a-row>

          <!-- 添加状态按钮 -->
          <a-button type="dashed" icon="plus" @click="addState" style="margin-top: 8px;">
            添加状态
          </a-button>
        </a-form-item>
      </a-form>
    </a-modal>
    <!-- 查看 LHA 模型的弹窗 -->
    <a-modal
      v-model="isLhaModalVisible"
      title="LHA Model"
      @ok="handleSubmitLha"
      @cancel="handleCancelLha"
      width="800px"
    >
      <a-form>
        <!-- 编辑 LHA 模型的文本框 -->
        <a-form-item label="LHA Model" style="margin-bottom: 12px;">
          <a-textarea
            v-model="lhaModelText.lhatext"
            rows="10"
            placeholder="请输入设备的 LHA 模型描述"
          />
        </a-form-item>
      </a-form>
    </a-modal>
    <!-- 弹出窗口 -->
    <a-modal
      v-model="isDeviceInstanceModalVisible"
      title="新增设备实例"
      @ok="handleNewDeviceInstanceSubmit"
      @cancel="handleCancelDeviceInstance"
    >
      <!-- 弹窗中的表单内容 -->
      <a-form @submit.prevent="addDeviceInstance">
        <a-row :gutter="16">
          <!-- 第一行：两个输入框 -->
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
            <a-form-item label="设备坐标">
              <a-input v-model="newDeviceInstance.coordinate" placeholder="输入设备坐标" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <!-- 第二行：两个输入框 -->
          <a-col :span="12">
            <a-form-item label="设备详情">
              <a-input v-model="newDeviceInstance.fixedProperties" placeholder="输入设备详情" />
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
    <!-- 设备配置弹窗 -->

  </div>
</template>

<script>
import axios from 'axios'
import { saveDeviceConfig, getDeviceConfig, getDevicelha, saveDeviceLha } from '@/api/manage'

const deviceInstanceColumns = [
  { title: '设备序号', dataIndex: 'deviceId', scopedSlots: { customRender: 'deviceId' } },
  { title: '设备名称', dataIndex: 'deviceName', scopedSlots: { customRender: 'deviceName' } },
  { title: '设备坐标', dataIndex: 'coordinate', scopedSlots: { customRender: 'coordinate' } },
  { title: '设备状态', dataIndex: 'states', scopedSlots: { customRender: 'states' } },
  { title: '设备描述', dataIndex: 'configuration', scopedSlots: { customRender: 'configuration' } },
  { title: 'LHA', dataIndex: 'lha', scopedSlots: { customRender: 'lha' } }
]

export default {
  data () {
    return {
      // 控制配置弹窗是否显示
      isConfigurationModalVisible: false,
      isLhaModalVisible: false,
      // 当前设备配置数据
      currentDeviceConfig: {
        deviceId: '',
        deviceName: '',
        states: [],
        parameters: {},
        variables: {}
      },
      lhaModelText: {
        lhatext: '',
        deviceId: ''
      },
      deviceInstanceColumns: deviceInstanceColumns,
      selectedDeviceType: null, // 当前选中的设备类型
      // filteredDeviceInstances: this.deviceInstances,
      queryType: '0',
      isDeviceTypeModalVisible: false, // 控制弹窗显示状态
      isDeviceInstanceModalVisible: false,
      filteredDeviceInstances: [], // 过滤后的设备实例数据
      deviceTypeColumns: [
        { title: '设备类型序号', dataIndex: 'deviceTypeId', key: 'deviceTypeId' },
        { title: '设备类型名称', dataIndex: 'deviceTypeName', key: 'deviceTypeName' },
        { title: '设备种类', dataIndex: 'isSensor', key: 'isSensor' },
        { title: '设备类型能力', dataIndex: 'actuatingFunctions', key: 'actuatingFunctions' }
      ],

      deviceTypes: [], // 后端获取的设备类型列表

      deviceInstances: [], // 后端获取的设备实例列表

      newDeviceType: {
        typeId: '',
        typeName: '',
        isSensor: '',
        actuatingFunctions: ''
      }, // 新增设备类型

      newDeviceInstance: {
        deviceId: '',
        deviceName: '',
        coordinate: '',
        fixedProperties: '',
        states: ''
      }, // 新增设备实例
      loading: false // 用于显示加载状态

    }
  },
  mounted () {
    this.fetchDeviceTypes() // 页面加载时调用API获取设备类型数据
    this.fetchDeviceData()
  },
  methods: {
    async fetchDeviceData () {
      try {
        // 从 localStorage 获取保存的 projectId
        const projectId = localStorage.getItem('project_id')

        // 发起带有 projectId 的 API 请求
        const response = await axios.get(`http://localhost:8080/api/devices`, {
          params: {
            project: projectId // 作为查询参数发送 projectId
          }
        })

        console.log('API 返回的数据:', response.data) // 打印返回的数据
        const rawData = response.data

        // 处理数据，将嵌套结构转换为可读格式
        this.deviceInstances = rawData.map(device => ({
          deviceId: device.deviceId,
          deviceName: device.deviceName,
          coordinate: `${device.coordinate.x}, ${device.coordinate.y}, ${device.coordinate.z}`, // 坐标格式化为字符串
          fixedProperties: this.parseFixedProperties(device.fixedProperties), // 解析 JSON 字符串
          states: this.parseStates(device.states), // 解析设备状态数组
          deviceTypeName: device.deviceTypeName, // 确保这个属性存在
          deviceTypeId: device.deviceTypeId
        }))

        // 初始化 filteredDeviceInstances
        this.filteredDeviceInstances = [...this.deviceInstances]
      } catch (error) {
        console.error('获取设备数据时出错:', error)
      }
    },
    async handleSubmitConfiguration () {
      // 调用 manage.js 中的 submitDeviceConfiguration 函数
      console.log(this.currentDeviceConfig)
      return saveDeviceConfig(this.currentDeviceConfig)
        .then(res => {
          console.log('Data received:', res)
          return res // 确保数据格式是数组
        })
    },
    // 显示设备配置弹窗，并初始化空表单
    async showConfigurationForm (deviceId) {
      try {
        const response = await getDeviceConfig(deviceId)
        console.log(response)

        // 确保 `currentDeviceConfig` 是完整的对象
        this.currentDeviceConfig = response || {
          deviceId: deviceId,
          deviceName: '',
          states: []
        }
      } catch (error) {
        console.error('获取设备配置失败：', error)
        // 发生错误时，提供默认配置
        this.currentDeviceConfig = {
          deviceId: deviceId,
          deviceName: '',
          states: []
        }
      }

      // 显示弹窗
      this.isConfigurationModalVisible = true
    },

    // 增加一个新的状态
    addState () {
      // 确保 states 存在，如果不存在则初始化为空数组
      if (!this.currentDeviceConfig.states) {
        this.currentDeviceConfig.states = []
      }

      // 添加新的状态对象
      this.currentDeviceConfig.states.push({
        stateName: '',
        equation: '',
        transitions: [{ toState: '', condition: '' }]
      })
},

    // 删除指定的状态
    removeState (index) {
      if (this.currentDeviceConfig.states && this.currentDeviceConfig.states.length > index) {
        this.currentDeviceConfig.states.splice(index, 1)
      }
    },

    // 关闭弹窗
    handleCancelConfiguration () {
      this.isConfigurationModalVisible = false
    },
    handleCancelLha () {
      this.isLhaModalVisible = false
    },

    async handleSubmitLha () {
      // 调用 manage.js 中的 submitDeviceConfiguration 函数
      console.log(8888)
      console.log(this.lhaModelText)
      return saveDeviceLha(this.lhaModelText.deviceId, this.lhaModelText.lhatext)
        .then(res => {
          console.log('Data received:', res)
          return res // 确保数据格式是数组
        })
    },
    // 通过设备 ID 获取设备的 LHA 模型文本
    async showLhaModel (deviceId) {
      console.log('查看设备ID:', deviceId)
      this.isLhaModalVisible = true
      // 假设你从后端获取 LHA 模型文本
      const deviceLha = await getDevicelha(deviceId)
      console.log(deviceLha)
      this.lhaModelText.lhatext = deviceLha
      this.lhaModelText.deviceId = deviceId
      this.isLhaModalVisible = true
    },
    filterData () {
      this.filteredDeviceInstances = this.deviceInstances.filter(item => {
        const matchesType = this.queryType === '0' || (item.deviceTypeName && item.deviceTypeName === this.queryType)
        return matchesType
      })
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
        this.newDeviceType.isSensor &&
        this.newDeviceType.actuatingFunctions
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
          isSensor: '',
          actuatingFunctions: ''
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
          coordinate: '',
          fixedProperties: '',
          states: ''
        }

        // 更新过滤后的设备实例
        this.filterData() // 先过滤数据
        // 关闭弹窗
        this.isDeviceInstanceModalVisible = false
      } else {
        console.error('请输入设备编号和设备名称')
      }
    },

    async fetchDeviceTypes () {
      this.loading = true
      try {
        // 从 localStorage 获取保存的 projectId
        const projectId = localStorage.getItem('project_id')

        // 发起带有 projectId 的 API 请求
        const response = await axios.get('http://localhost:8080/api/deviceTypes', {
          params: {
            project: projectId // 作为查询参数发送 projectId
          }
        })

        const deviceData = response.data

        // 映射设备类型的数据（左侧表格）
        this.deviceTypes = deviceData.map(device => ({
          deviceTypeId: device.deviceTypeId,
          deviceTypeName: device.deviceTypeName,
          isSensor: device.isSensor ? '传感器' : '其他', // 映射布尔值为中文
          actuatingFunctions: device.actuatingFunctions.map(func => func.name).join(', ') // 映射多个功能名称为字符串
        }))
      } catch (error) {
        console.error('获取设备类型数据时出错:', error)
      } finally {
        this.loading = false
      }
    },

    // 解析 fixedProperties，返回可读格式
    parseFixedProperties (fixedProperties) {
      try {
        const properties = JSON.parse(fixedProperties)
        return Object.keys(properties)
          .map(key => `${key}: ${properties[key]}`)
          .join(', ') // 格式化为字符串，如 "厂家: 小米, 颜色: 黑色, 型号: XM-38295, 尺寸: 20*20*15"
      } catch (e) {
        console.error('解析 fixedProperties 时出错:', e)
        return fixedProperties // 如果解析失败，直接返回原始字符串
      }
    },

    // 解析 states 数组，返回状态的可读格式
    parseStates (states) {
      return states
        .map(state => `${state.stateKey}: ${state.stateValue}`)
        .join(', ') // 格式化为字符串，如 "电源: ON"
    },

    // 新增设备实例
    addDeviceInstance () {
      // 将新增的设备实例数据添加到表格中
      this.deviceInstances.push({
        deviceId: this.newDevice.deviceId,
        deviceName: this.newDevice.deviceName,
        coordinate: this.newDevice.coordinate,
        fixedProperties: this.newDevice.fixedProperties,
        states: this.newDevice.states
      })
      // 清空输入框
      this.newDevice = {
        deviceId: '',
        deviceName: '',
        coordinate: '',
        fixedProperties: '',
        states: ''
      }
    },

    created () {
      this.fetchDeviceData() // 页面创建时获取数据
      // 初始化过滤后的设备实例为所有设备实例
    }
  }
}
</script>

<style scoped>
.ant-form-item {
  margin-bottom: 12px;
}

.a-table-row {
  cursor: pointer;
}
</style>
