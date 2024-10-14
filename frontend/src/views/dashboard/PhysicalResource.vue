<template>
  <div style="height: 100%">
    <a-row :gutter="24" style="height: 50% ;margin-bottom: 2%; ">
      <a-col :span="24">
        <a-card title="设备类型" bordered :style="{ borderRadius: '8px' }" >
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
                <a-select-option v-for="device in deviceTypes" :key="device.deviceTypeId" :value="device.deviceTypeName">
                  {{ device.deviceTypeName }}
                </a-select-option>
              </a-select>
            </a-col>
            <a-col :md="!advanced && 8 || 24" :sm="24">
              <span class="table-page-search-submitButtons" :style="advanced && { float: 'right', overflow: 'hidden' } || {} " >
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
          />
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

  </div>
</template>

<script>
import axios from 'axios'

export default {
  data () {
    return {
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

      deviceInstanceColumns: [
        { title: '设备序号', dataIndex: 'deviceId', key: 'deviceId', width: 100 },
        { title: '设备名称', dataIndex: 'deviceName', key: 'deviceName', width: 150 },
        { title: '设备坐标', dataIndex: 'coordinate', key: 'coordinate', width: 150 },
        { title: '设备状态', dataIndex: 'states', key: 'states', width: 100 },
        { title: '设备详情', dataIndex: 'fixedProperties', key: 'fixedProperties', width: 200 }
      ],

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
        const response = await axios.get('http://localhost:8080/api/devices?project=1')
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
        const response = await axios.get('http://localhost:8080/api/deviceTypes?project=1') // 调用API
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
