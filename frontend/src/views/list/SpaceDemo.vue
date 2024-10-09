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
            <a-select-option value="0">建筑一</a-select-option>
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

      <!-- 设备实例表格 -->
      <a-row justify="center" class="table-row">
        <a-col :span="18"> <!-- 限制表格的宽度 -->
          <a-table
            size="default"
            rowKey="key"
            :columns="columns"
            :dataSource="data"
            scroll="{ x: 1200 }"
          >
          </a-table>
        </a-col>
      </a-row>
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
      // 表格列数据
      columns: [
        {
          title: '设备实例名称',
          dataIndex: 'name',
          key: 'name',
          width: 200
        },
        {
          title: '部署位置',
          dataIndex: 'location',
          key: 'location',
          width: 200
        },
        {
          title: '能力描述',
          dataIndex: 'capability',
          key: 'capability',
          width: 200
        },
        {
          title: '设备数据',
          dataIndex: 'data',
          key: 'data',
          width: 200
        },
        {
          title: '设备状态',
          dataIndex: 'status',
          key: 'status',
          width: 200
        }
      ],
      // 表格数据
      data: [
        {
          key: '1',
          name: '探照灯-01',
          location: '车库-停车场C3',
          capability: '车辆检测',
          data: '活动检测中',
          status: '正常'
        },
        {
          key: '2',
          name: '显示器-03',
          location: '2号车库出口',
          capability: '显示车辆信息',
          data: '停车位已满',
          status: '警告'
        },
        {
          key: '3',
          name: '咖啡-07',
          location: '楼层1-保卫处监控室',
          capability: '异常警告',
          data: '活动检测中',
          status: '正常'
        },
        {
          key: '4',
          name: '门禁系统-02',
          location: '大门-入口D',
          capability: '门禁识别与控制',
          data: '最后通行：2024-09-22 12:34:56',
          status: '正常'
        },
        {
          key: '5',
          name: '烟雾报警器-01',
          location: '楼层3-储物间E',
          capability: '烟雾检测与报警',
          data: '无烟雾检测',
          status: '正常'
        }
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

</style>
