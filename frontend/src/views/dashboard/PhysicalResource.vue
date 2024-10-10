<template>
  <div style="height: 100%">
    <a-row :gutter="24" style="height: 50% ;margin-bottom: 2%; ">
      <a-col :span="12">
        <a-card title="设备类型" >
          <a-table
            :columns="deviceTypeColumns"
            :dataSource="deviceTypes"
            row-key="id"
            @rowClick="handleDeviceTypeClick"
            :pagination="false"
            :scroll="{ y: 250 }"
          />
          <a-divider />
          <a-form @submit="handleNewDeviceTypeSubmit">
            <a-form-item label="新增设备类型">
              <a-input v-model="newDeviceType.name" placeholder="输入设备类型名称" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="handleNewDeviceTypeSubmit">新增设备类型</a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>

      <a-col :span="12">
        <a-card title="设备类型详情" >
          <a-form @submit="handleDeviceTypeSubmit" >
            <a-table
              :columns="deviceTypeDetailColumns"
              :dataSource="deviceTypeDetail"
              :loading="loading"
              row-key="id"
              @rowClick="handleDeviceTypeDetailClick"
              :pagination="false"
              :scroll="{ y: 300 }"
            />
            <a-row :gutter="24" >
              <!-- 第一行：三个输入框 -->
              <a-col :span="12">
                <a-form-item label="新增设备类型能力">
                  <a-input v-model="newDeviceInstance.name" placeholder="输入新增设备类型能力" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="新增设备类型状态">
                  <a-input v-model="newDeviceInstance.location" placeholder="输入新增设备类型状态" />
                </a-form-item>
              </a-col>
            </a-row>
            <a-form-item>
              <a-button type="primary" @click="handleDeviceTypeSubmit">保存</a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="24" style="height: 50% ">
      <a-col :span="24">
        <a-card title="设备实例" >
          <a-table
            :columns="deviceInstanceColumns"
            :dataSource="deviceInstances"
            row-key="id"
            @rowClick="handleDeviceInstanceClick"
            :pagination="false"
            :scroll="{ y: 300 }"
          />
          <a-divider />
          <a-form @submit.prevent="handleNewDeviceInstanceSubmit">
            <a-row :gutter="16">
              <!-- 第一行：三个输入框 -->
              <a-col :span="8">
                <a-form-item label="设备实例名称">
                  <a-input v-model="newDeviceInstance.name" placeholder="输入设备实例名称" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="部署空间">
                  <a-input v-model="newDeviceInstance.location" placeholder="输入部署空间" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="设备属性描述">
                  <a-input v-model="newDeviceInstance.capabilities" placeholder="输入设备属性描述" />
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="16">
              <!-- 第二行：两个输入框 -->
              <a-col :span="12">
                <a-form-item label="设备能力">
                  <a-input v-model="newDeviceInstance.data" placeholder="输入设备能力" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item label="设备状态">
                  <a-input v-model="newDeviceInstance.status" placeholder="输入设备状态" />
                </a-form-item>
              </a-col>
            </a-row>

            <!-- 提交按钮 -->
            <a-form-item>
              <a-button type="primary" html-type="submit">新增设备实例</a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>
    </a-row>
    <!--
    <a-row :gutter="24" style="margin-top: 24px;">
      <a-col :span="24">
        <a-card title="设备实例详情" style="height: 300px;">
          <a-form @submit="handleDeviceInstanceSubmit">
            <a-form-item label="部署位置">
              <a-input v-model="selectedDeviceInstance.location" placeholder="输入部署位置" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="handleDeviceInstanceSubmit">保存</a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-col>
    </a-row>
  -->
  </div>
</template>

<script>
import axios from 'axios'

export default {
  data () {
    return {
      deviceTypes: [
        { id: 1, name: '摄像头' },
        { id: 2, name: '信号灯' },
        { id: 3, name: '雷达' },
        { id: 4, name: '动作传感器' },
        { id: 5, name: '喇叭' }
      ], // 后端获取的设备类型列表
      deviceTypeColumns: [
        { title: '设备类型名称', dataIndex: 'name', key: 'name' }
      ],

      selectedDeviceType: {}, // 选中的设备类型
      selectedDeviceTypeDetail: [], // 选中的设备类型详情
      deviceInstances: [{
        id: 1,
        name: '探照灯-01',
        location: '车库-停车场C3',
        capabilities: '车辆检测',
        data: '活动检测中',
        status: '正常'
      },
        {
          id: 2,
          name: '显示器-03',
          location: '2号车库出口',
          capabilities: '显示车辆信息',
          data: '停车位已满',
          status: '警告'
        },
        {
          id: 4,
          name: '门禁系统-02',
          location: '大门口-入口D',
          capabilities: '门禁识别与控制',
          data: '最后通行：2024-09-22 12:34:56',
          status: '正常'
        },
        {
          id: 3,
          name: '喇叭-07',
          location: '楼层1-保卫处监控室',
          capabilities: '异常警告',
          data: '活动检测中',
          status: '正常'
        },
        {
          id: 5,
          name: '雷达-01',
          location: '楼库-停车场C3',
          capabilities: '停车异常检测',
          data: '停车状况正常',
          status: '正常'
        },
        {
          id: 6,
          name: '二氧化碳传感器-04',
          location: '楼层2-实验室F',
          capabilities: '实时监测CO2浓度',
          data: '700 ppm',
          status: '正常'
        },
        {
          id: 7,
          name: '空气质量传感器-08',
          location: '楼层1-大厅G',
          capabilities: '监测PM2.5、PM10、甲醛等',
          data: 'PM2.5: 30 µg/m³, PM10: 40 µg/m³',
          status: '警告'
        },
        {
          id: 8,
          name: '智能灯光控制-05',
          location: '楼层3-会议室H',
          capabilities: '自动调节灯光强度',
          data: '亮度: 80%',
          status: '正常'
        }], // 设备实例列表
      selectedDeviceInstance: {}, // 选中的设备实例

      newDeviceType: {}, // 新增设备类型
      newDeviceInstance: {}, // 新增设备实例
      loading: false, // 用于显示加载状态

      deviceTypeDetailColumns: [
        { title: '能力', dataIndex: 'attribute', key: 'attribute' },
        { title: '状态', dataIndex: 'capabilities', key: 'capabilities' }
      ],
      deviceTypeDetail: [

        {
          id: 4,
          attribute: '通信协议',
          capabilities: '设备使用的通信协议，如：ZigBee、Wi-Fi'
        },
        {
          id: 5,
          attribute: '功耗',
          capabilities: '设备在工作时的功耗情况，如：5W'
        },
        {
          id: 6,
          attribute: '数据精度',
          capabilities: '设备数据的测量精度，如：±0.1°C'
        },
        {
          id: 7,
          attribute: '工作温度范围',
          capabilities: '设备在工作时支持的温度范围，如：-10°C至50°C'
        },
        {
          id: 9,
          attribute: '数据刷新频率',
          capabilities: '设备数据的更新频率，如：每5秒刷新一次'
        },
        {
          id: 10,
          attribute: '安装位置',
          capabilities: '设备通常安装的推荐位置，如：室内、室外'
        }
      ],

      deviceInstanceColumns: [
        { title: '设备实例名称', dataIndex: 'name', key: 'name' },
        { title: '部署空间', dataIndex: 'location', key: 'location' },
        { title: '设备功能', dataIndex: 'capabilities', key: 'capabilities' },
        { title: '设备属性', dataIndex: 'properties', key: 'properties' },
        { title: '设备状态', dataIndex: 'status', key: 'status' }
      ]
    }
  },
  mounted () {
    // 在组件挂载时请求数据
    this.fetchData()
  },
  methods: {
    handleDeviceTypeClick (record) {
      this.selectedDeviceType = { ...record }
      // 更新设备实例表格
      this.deviceInstances = this.getDeviceInstances(record.id)
    },
    handleDeviceInstanceClick (record) {
      this.selectedDeviceInstance = { ...record }
    },
    handleDeviceTypeSubmit () {
      // 保存设备类型属性、事件、操作
      console.log('设备类型已保存:', this.selectedDeviceType)
    },
    handleDeviceInstanceSubmit () {
      // 保存设备实例部署位置
      console.log('设备实例已保存:', this.selectedDeviceInstance)
    },
    handleNewDeviceTypeSubmit () {
      // 新增设备类型逻辑
      this.deviceTypes.push({
        id: this.deviceTypes.length + 1,
        ...this.newDeviceType
      })
      this.newDeviceType = {}
      console.log('新增设备类型:', this.deviceTypes)
    },
    handleNewDeviceInstanceSubmit () {
      // 新增设备实例逻辑
      this.deviceInstances.push({
        id: this.deviceInstances.length + 1,
        ...this.newDeviceInstance
      })
      this.newDeviceInstance = {}
      console.log('新增设备实例:', this.deviceInstances)
    },
    getDeviceInstances (deviceTypeId) {
      // 模拟获取设备实例
      return [
        { id: 1, name: '温度计', location: 'x:245,y:754,z:129', capabilities: '温度感知', properties: '26摄氏度', status: '运行中' }
      ]
    },
    handleOperationClick (operation) {
      console.log('操作执行:', operation)
    },
    async fetchData () {
      try {
        const response = await axios.get('/api/device-types/allTypes') // 发送GET请求
        this.deviceTypes = response.data // 将数据赋值给items
      } catch (error) {
        console.error('Error fetching data:', error)
      }
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
