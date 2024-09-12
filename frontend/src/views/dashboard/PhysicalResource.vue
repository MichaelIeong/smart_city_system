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
            :scroll="{ y: 159 }"
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
          <a-form @submit="handleDeviceTypeSubmit">
            <a-form-item label="属性">
              <a-input v-model="selectedDeviceType.attribute1" placeholder="属性1" />
              <a-input v-model="selectedDeviceType.attribute2" placeholder="属性2" />
            </a-form-item>
            <a-form-item label="能力">
              <a-input v-model="selectedDeviceType.event1" placeholder="能力1" />
              <a-input v-model="selectedDeviceType.event2" placeholder="能力2" />
            </a-form-item>
            <a-form-item label="操作">
              <a-button @click="handleOperationClick('operation1')">操作1</a-button>
              <a-button @click="handleOperationClick('operation2')">操作2</a-button>
            </a-form-item>
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
            :scroll="{ y: 150 }"
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
                <a-form-item label="部署位置">
                  <a-input v-model="newDeviceInstance.location" placeholder="输入部署位置" />
                </a-form-item>
              </a-col>
              <a-col :span="8">
                <a-form-item label="设备能力描述">
                  <a-input v-model="newDeviceInstance.capabilities" placeholder="输入设备能力描述" />
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="16">
              <!-- 第二行：两个输入框 -->
              <a-col :span="12">
                <a-form-item label="设备数据">
                  <a-input v-model="newDeviceInstance.data" placeholder="输入设备数据" />
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
export default {
  data () {
    return {
      deviceTypes: [
        { id: 1, name: '温度传感器', attribute1: '属性1', attribute2: '属性2', event1: '温度感知', event2: '高温警报' },
        { id: 2, name: '设备类型B', attribute1: '属性1', attribute2: '属性2', event1: '事件1', event2: '事件2' },
        { id: 3, name: '设备类型C', attribute1: '属性1', attribute2: '属性2', event1: '事件1', event2: '事件2' },
        { id: 4, name: '设备类型D', attribute1: '属性1', attribute2: '属性2', event1: '事件1', event2: '事件2' }

      ], // 设备类型列表
      selectedDeviceType: {}, // 选中的设备类型
      deviceInstances: [], // 设备实例列表
      selectedDeviceInstance: {}, // 选中的设备实例

      newDeviceType: {}, // 新增设备类型
      newDeviceInstance: {}, // 新增设备实例

      deviceTypeColumns: [
        { title: '设备类型名称', dataIndex: 'name', key: 'name' }
      ],

      deviceInstanceColumns: [
        { title: '设备实例名称', dataIndex: 'name', key: 'name' },
        { title: '部署位置', dataIndex: 'location', key: 'location' },
        { title: '能力描述', dataIndex: 'capabilities', key: 'capabilities' },
        { title: '设备数据', dataIndex: 'data', key: 'data' },
        { title: '设备状态', dataIndex: 'status', key: 'status' }
      ]
    }
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
        { id: 1, name: '温度计', location: 'x:245,y:754,z:129', capabilities: '温度感知', data: '26摄氏度', status: '运行中' }
      ]
    },
    handleOperationClick (operation) {
      console.log('操作执行:', operation)
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
