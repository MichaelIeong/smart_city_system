<template>
  <div>
    <a-row :gutter="24">
      <a-col :span="8">
        <a-card title="设备类型" style="height: 550px;">
          <a-table
            :columns="deviceTypeColumns"
            :dataSource="deviceTypes"
            row-key="id"
            @rowClick="handleDeviceTypeClick"
            :pagination="false"
            :scroll="{ y: 200 }"
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

      <a-col :span="8">
        <a-card title="设备类型详情" style="height: 550px;">
          <a-form @submit="handleDeviceTypeSubmit">
            <a-form-item label="属性">
              <a-input v-model="selectedDeviceType.attribute1" placeholder="属性1" />
              <a-input v-model="selectedDeviceType.attribute2" placeholder="属性2" />
            </a-form-item>
            <a-form-item label="事件">
              <a-input v-model="selectedDeviceType.event1" placeholder="事件1" />
              <a-input v-model="selectedDeviceType.event2" placeholder="事件2" />
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

      <a-col :span="8">
        <a-card title="设备实例" style="height: 550px;">
          <a-table
            :columns="deviceInstanceColumns"
            :dataSource="deviceInstances"
            row-key="id"
            @rowClick="handleDeviceInstanceClick"
            :pagination="false"
            :scroll="{ y: 100 }"
          />
          <a-divider />
          <a-form @submit="handleNewDeviceInstanceSubmit">
            <a-form-item label="新增设备实例">
              <a-input v-model="newDeviceInstance.name" placeholder="输入设备实例名称" />
              <a-input v-model="newDeviceInstance.location" placeholder="输入部署位置" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="handleNewDeviceInstanceSubmit">新增设备实例</a-button>
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
        { id: 1, name: '设备类型A', attribute1: '属性1', attribute2: '属性2', event1: '事件1', event2: '事件2' },
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
        { title: '部署位置', dataIndex: 'location', key: 'location' }
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
        { id: 1, name: '设备实例A1', location: '位置1' },
        { id: 2, name: '设备实例B1', location: '位置2' }
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
