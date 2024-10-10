<template>
  <div class="space-demo-container">
    <!-- 3D渲染容器 -->
    <div id="three-container"></div>

    <!-- 表单区域 -->
    <div class="form-container">
      <div style="height: 30px;"></div> <!-- 留白区域 -->

      <!-- 下拉框部分 -->
      <a-row :gutter="16" justify="center" align="middle" class="select-row">
        <a-col :span="8">
          <a-select
            v-model="selectedBuilding"
            placeholder="请选择建筑"
            style="width: 100%"
            allow-clear
            @change="change(selectedBuilding)"
          >
            <a-select-option value="0">D2地下车库</a-select-option>
            <a-select-option value="1">建筑二</a-select-option>
            <a-select-option value="2">建筑三</a-select-option>
          </a-select>
        </a-col>
        <a-col :span="8">
          <a-select
            v-model="selectedFloor"
            placeholder="请选择楼层"
            style="width: 100%"
            allow-clear
          >
            <a-select-option value="floor1">一楼</a-select-option>
            <a-select-option value="floor2">二楼</a-select-option>
            <a-select-option value="floor3">三楼</a-select-option>
            <a-select-option value="floor3">负一楼（车库）</a-select-option>
          </a-select>
        </a-col>
        <a-col :span="8">
          <a-select
            v-model="selectedRoom"
            placeholder="请选择房间"
            style="width: 100%"
            allow-clear
          >
            <a-select-option value="room1">房间一</a-select-option>
            <a-select-option value="room2">房间二</a-select-option>
            <a-select-option value="room3">房间三</a-select-option>
          </a-select>
        </a-col>
      </a-row>

      <!-- 下拉框和表格之间的留白 -->
      <div style="height: 30px;"></div>

      <!-- 四张并列的表格 -->
      <div class="table-container">
        <!-- 表格区域 -->
        <a-row justify="center" gutter={16}>
          <!-- 第一行：属性表和状态表 -->
          <a-col :span="12">
            <a-table
              :columns="propertyColumns"
              :dataSource="propertyData"
              pagination={false}
            />
          </a-col>
          <a-col :span="12">
            <a-table
              :columns="statusColumns"
              :dataSource="statusData"
              pagination={false}
            />
          </a-col>
          <!-- 第二行：事件表和服务表 -->
          <a-col :span="12">
            <a-table
              :columns="eventColumns"
              :dataSource="eventData"
              pagination={false}
            />
          </a-col>
          <a-col :span="12">
            <a-table
              :columns="serviceColumns"
              :dataSource="serviceData"
              pagination={false}
            />
          </a-col>
        </a-row>
      </div>
    </div>
  </div>
</template>

<script>
import { createWebglEngine } from '@tslfe/dt-engine'

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
      selectedBuilding: '0',
      selectedFloor: undefined,
      selectedRoom: undefined,

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
      propertyData: [
      { key: '1', name: '车位总数', info: '100' },
      { key: '2', name: '车库面积', info: '2000 平方米' }
      ],

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
      statusData: [
      { key: '1', name: '空余车位数量', info: '30' },
      { key: '2', name: '已停车位数量', info: '70' },
      { key: '3', name: '今日入库车次', info: '15' }
      ],

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
      eventData: [
      { key: '1', name: '车辆入库', description: '车辆已成功入库，当前车位空余数量为 30。' },
      { key: '2', name: '车辆出库', description: '车辆已成功出库，当前车位空余数量为 31。' },
      { key: '3', name: '车库满', description: '车库已满，无法再入库。' }
      ],

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
      serviceData: [
      { key: '1', name: '入库抬杆', description: '允许车辆进入车库。' },
      { key: '2', name: '入库落杆', description: '阻止车辆进入车库。' },
      { key: '3', name: '出库抬杆', description: '允许车辆离开车库。' },
      { key: '4', name: '出库落杆', description: '阻止车辆离开车库。' },
      { key: '5', name: '保安巡逻', description: '保安在车库内进行巡逻以确保安全。' }
      ]
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
        this.change('0')
      })
    },
    change (type) {
      return this.meta.render(this.FloorMap[type], true).then(() => {
        console.log('场景渲染完成.')
      })
    }
  },
  mounted () {
    setTimeout(() => {
      this.initMeta()
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
}

.space-demo-container {
  display: grid;
  grid-template-rows: 1fr 1fr;
  height: 100vh;
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
