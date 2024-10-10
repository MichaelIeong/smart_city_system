<template>
  <div class="space-demo-container">
    <!-- 3D渲染容器 -->
    <div id="three-container"></div>

    <!-- 表单区域 -->
    <div class="form-container">
      <div style="height: 30px;"></div> <!-- 留白区域 -->

      <!-- 下拉框部分 -->
      <a-row :gutter="16" justify="center" align="middle" class="select-row">
        <!-- <a-col :span="8"> -->
        <a-select
          v-model="selectedSpace"
          placeholder="请选择空间"
          style="width: 100%"
          allow-clear
          @change="change(selectedSpace)"
        >
          <a-select-option
            v-for="space in spaces"
            :key="space.id"
            :value="space.id"
          >
            {{ space.spaceName }}
          </a-select-option>
        </a-select>
        <!-- </a-col>-->
      </a-row>

      <!-- 下拉框和表格之间的留白 -->
      <div style="height: 30px;"></div>

      <!-- 四张并列的表格 -->
      <div class="table-container">
        <!-- 表格区域 -->
        <a-row justify="center" gutter="{16}">
          <!-- 第一行：属性表和状态表 -->
          <a-col :span="12">
            <a-table
              :columns="propertyColumns"
              :dataSource="propertyData"
              pagination="{false}"
            />
          </a-col>
          <a-col :span="12">
            <a-table
              :columns="statusColumns"
              :dataSource="statusData"
              pagination="{false}"
            />
          </a-col>
          <!-- 第二行：事件表和服务表 -->
          <a-col :span="12">
            <a-table
              :columns="eventColumns"
              :dataSource="eventData"
              pagination="{false}"
            />
          </a-col>
          <a-col :span="12">
            <a-table
              :columns="serviceColumns"
              :dataSource="serviceData"
              pagination="{false}"
            />
          </a-col>
        </a-row>
      </div>
    </div>
  </div>
</template>

<script>
import { createWebglEngine } from '@tslfe/dt-engine'
import axios from 'axios'

export default {
  name: 'SpaceDemo',
  data () {
    return {
      meta: null,
      FloorMap: {
        0: '/Park_e50d76e1b0bd4a91869076afc36e6a01/Building_48b5fb64ad0340a1b2121478b20a9369/Floor_5bc543e44c994054b3ff843e6da0695c/graphic.glb',
        1: '/Park_e50d76e1b0bd4a91869076afc36e6a01/graphic.glb',
        2: '/Park_e50d76e1b0bd4a91869076afc36e6a01/Building_48b5fb64ad0340a1b2121478b20a9369/graphic.glb'
      },
      selectedSpace: undefined,
      spaces: [],

      // 表格1: 属性
      propertyColumns: [
        {
          title: '属性名称',
          dataIndex: 'name',
          key: 'name'
        },
        {
          title: '属性信息',
          dataIndex: 'info',
          key: 'info'
        }
      ],
      propertyData: [],

      // 表格2: 状态
      statusColumns: [
        {
          title: '状态名称',
          dataIndex: 'name',
          key: 'name'
        },
        {
          title: '状态信息',
          dataIndex: 'info',
          key: 'info'
        }
      ],
      statusData: [],

      // 表格3: 事件
      eventColumns: [
        {
          title: '事件名称',
          dataIndex: 'name',
          key: 'name'
        },
        {
          title: '事件描述',
          dataIndex: 'description',
          key: 'description'
        }
      ],
      eventData: [],

      // 表格4: 服务
      serviceColumns: [
        {
          title: '服务名称',
          dataIndex: 'name',
          key: 'name'
        },
        {
          title: '服务描述',
          dataIndex: 'description',
          key: 'description'
        }
      ],
      serviceData: []
    }
  },
  methods: {
    initMeta () {
      createWebglEngine((config) => {
        config.scene.cache = true
        config.scene.cacheType = (url) => {
          if (url === this.FloorMap.yuanqu) {
            return true
          } else {
            return false
          }
        }
        return config
      }).then((app) => {
        app.amount('three-container')
        this.meta = app
        this.changeDemo('0')
      })
    },
    change (selectedSpace) {
      this.changeDemo(selectedSpace)
      this.changeSpace(selectedSpace)
      this.fetchData(selectedSpace)
    },
    changeSpace (selectedSpace) {
      console.log('选中的空间 ID:', selectedSpace)
    },
    changeDemo (type) {
      return this.meta.render(this.FloorMap[type], true).then(() => {
        console.log('场景渲染完成.')
      })
    },
    async fetchData (spaceID) {
      try {
        const response = await axios.get(`http://localhost:8080/api/spaces/${spaceID}`)
        console.log('response data:', response.data)
        const data = response.data

        // 处理固定属性（对应属性表）
        this.propertyData = Object.entries(data.fixedProperties).map(([key, value]) => ({
          name: key,
          info: value
        }))

        // 处理状态（对应状态表）
        this.statusData = data.properties.map(property => ({
          name: property.propertyKey,
          info: property.propertyValue
        }))

        // 处理事件（对应事件表）
        this.eventData = data.events.map(event => ({
          name: event.eventType,
          description: `事件 ID: ${event.eventId}`
        }))

        // 处理服务（对应服务表）
        this.serviceData = data.services.map(service => ({
          name: service.serviceName,
          description: `服务 ID: ${service.serviceId}`
        }))
      } catch (error) {
        console.error('Error fetching data:', error)
      }
    },
    async fetchSpaces (spaceID) {
      try {
        const response = await axios.get(`http://localhost:8080/api/spaces?project=${spaceID}`)
        this.spaces = response.data
      } catch (error) {
        console.error('Error fetching spaces:', error)
      }
    }
  },
  mounted () {
    setTimeout(() => {
      this.initMeta()
      this.fetchSpaces(1)
      this.fetchData()
    }, 1000)
  }
}
</script>

<style lang="less">
* {
  margin: 0;
  padding: 0;
}

html, body {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
  overflow-y: auto;
}

.space-demo-container {
  display: grid;
  grid-template-rows: 1fr 3fr;
  height: 100vh;
  overflow: hidden;
}

#three-container {
  flex: 2;
  display: flex;
  justify-content: center;
  align-items: center;
}

.form-container {
  background-color: #ffffff;
  width: 100%;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  padding-left: 50px;
  padding-right: 50px;
  overflow-y: auto;
}

.select-row {
  margin-left: 50px;
  margin-right: 50px;
}

.table-container {
  display: flex;
  justify-content: space-between; /* 平均分配空间 */
  margin: 20px 0; /* 上下间距 */
}

.table-container .ant-table {
  margin-right: 15px; /* 每个表格右侧留白 */
}

.table-container .ant-table:last-child {
  margin-right: 0; /* 最后一个表格右侧不留白 */
}

</style>
