<template>
  <div class="space-demo-container">
    <!-- 左侧网格图容器 (70%) -->
    <div class="left-section">
      <a-spin :spinning="isLoading" tip="Loading...">
        <div class="mesh-container">
          <svg ref="svg" class="svg-container"></svg>
        </div>
      </a-spin>
    </div>

    <!-- 右侧表单区域 (30%) -->
    <div class="right-section">
      <div class="form-container">
        <div style="height: 30px;"></div>

        <!-- 下拉框 -->
        <a-row :gutter="16" justify="center" align="middle" class="select-row">
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
        </a-row>

        <div style="height: 30px;"></div>

        <!-- 四张表格 -->
        <div class="table-container">
          <!-- 网格元信息 -->
          <div class="table-wrapper">
            <a-table
              :columns="metaColumns"
              :dataSource="metaData"
              :pagination="false"
              size="small"
            />
          </div>

          <!-- 设备 -->
          <div class="table-wrapper">
            <div class="table-header">
              <span class="table-title">设备</span>
            </div>
            <a-table
              :columns="deviceColumns"
              :dataSource="deviceData"
              :pagination="false"
              size="small"
            />
          </div>

          <!-- 事件 -->
          <div class="table-wrapper">
            <div class="table-header">
              <span class="table-title">事件</span>
              <a-button type="primary" size="small" @click="showEventModal">
                添加事件
              </a-button>
            </div>
            <a-table
              :columns="eventColumns"
              :dataSource="eventData"
              :pagination="false"
              size="small"
            />
          </div>

          <!-- 服务 -->
          <div class="table-wrapper">
            <div class="table-header">
              <span class="table-title">服务</span>
              <a-button type="primary" size="small" @click="showServiceModal">
                添加服务
              </a-button>
            </div>
            <a-table
              :columns="serviceColumns"
              :dataSource="serviceData"
              :pagination="false"
              size="small"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 添加事件弹窗 -->
    <a-modal
      v-model="eventModalVisible"
      title="添加事件"
      @ok="handleEventOk"
      @cancel="handleEventCancel"
      okText="确定"
      cancelText="取消"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="事件名称">
          <a-input v-model="eventForm.name" placeholder="请输入事件名称" />
        </a-form-item>
        <a-form-item label="事件描述">
          <a-textarea v-model="eventForm.description" placeholder="请输入事件描述" :rows="4" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 添加服务弹窗 -->
    <a-modal
      v-model="serviceModalVisible"
      title="添加服务"
      @ok="handleServiceOk"
      @cancel="handleServiceCancel"
      okText="确定"
      cancelText="取消"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="服务名称">
          <a-input v-model="serviceForm.name" placeholder="请输入服务名称" />
        </a-form-item>
        <a-form-item label="服务描述">
          <a-textarea v-model="serviceForm.description" placeholder="请输入服务描述" :rows="4" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
import axios from 'axios'
import * as d3 from 'd3'
import meshData from './meshData.json'

export default {
  name: 'SpaceDemo',
  data () {
    return {
      isLoading: true,
      selectedSpace: 1,
      spaces: [],
      polygons: [],

      // 表格定义
      metaColumns: [{ title: '网格元信息', dataIndex: 'info', key: 'info' }],
      metaData: [],

      deviceColumns: [
        { title: '设备名称', dataIndex: 'name', key: 'name' },
        { title: '设备功能', dataIndex: 'info', key: 'info' }
      ],
      deviceData: [],

      eventColumns: [
        { title: '事件名称', dataIndex: 'name', key: 'name' },
        { title: '事件描述', dataIndex: 'description', key: 'description' }
      ],
      eventData: [],

      serviceColumns: [
        { title: '服务名称', dataIndex: 'name', key: 'name' },
        { title: '服务描述', dataIndex: 'description', key: 'description' }
      ],
      serviceData: [],

      // 弹窗数据
      eventModalVisible: false,
      serviceModalVisible: false,
      eventForm: { name: '', description: '' },
      serviceForm: { name: '', description: '' }
    }
  },
  methods: {
    // 初始化
    initMesh () {
      this.isLoading = true
      this.handleData()
      this.drawSvg()
      this.isLoading = false
    },

    // 解析 meshData.json
    handleData () {
      const data = meshData.data || []
      this.polygons = data.map(item => {
        const mesh = item.meshInfo
        return {
          id: mesh.meshCode,
          name: mesh.meshName,
          coords: mesh.meshGridList.map(p => [Number(p.x), Number(p.y)])
        }
      })
    },

    // 绘制 SVG 网格
    drawSvg () {
      const svgEl = d3.select(this.$refs.svg)
      svgEl.selectAll('*').remove()

      svgEl
        .attr('preserveAspectRatio', 'xMidYMid meet')
        .attr('viewBox', '0 0 3000 1600')

      const zoomG = svgEl
        .append('g')
        .attr('class', 'zoom-group')
        .attr('transform', 'translate(-1200, 0) scale(0.95)')
      // 添加坐标平移，假设需要进行平移修正
      const offsetX = -200 // 例如：向右平移50像素
      const offsetY = -200 // 例如：向下平移100像素
      zoomG.attr('transform', `translate(${offsetX}, ${offsetY}) scale(1)`)

      const zoom = d3.zoom()
        .scaleExtent([0.5, 5])
        .on('zoom', (event) => {
          zoomG.attr('transform', event.transform)
        })
      svgEl.call(zoom)

      const groups = zoomG
        .selectAll('g')
        .data(this.polygons)
        .enter()
        .append('g')
        .attr('class', 'polygon-group')

      groups
        .append('polygon')
        .attr('points', (d) => d.coords.map((p) => `${p[0]},${p[1]}`).join(' '))
        .attr('fill', () => d3.schemeCategory10[Math.floor(Math.random() * 10)])
        .attr('stroke', '#ffffff')
        .attr('stroke-width', 1.5)
        .attr('fill-opacity', 0.7)
        .on('mouseover', function () {
          d3.select(this)
            .transition()
            .duration(200)
            .attr('fill', '#2ECC71')
            .attr('stroke-width', 3)
        })
        .on('mouseout', function (event, d) {
          d3.select(this)
            .transition()
            .duration(200)
            .attr('stroke-width', 1.5)
            .attr('fill', () => d3.schemeCategory10[Math.floor(Math.random() * 10)])
        })
        .on('click', async (event, d) => {
          try {
            d3.selectAll('polygon').attr('stroke', '#ffffff').attr('stroke-width', 1.5)
            d3.select(event.currentTarget)
              .attr('stroke', '#000000')
              .attr('stroke-width', 3)

            this.$message.info(`加载网格 ID：${d.id}`)
            await this.fetchGridInfo(d.id)
          } catch (error) {
            console.error('点击加载失败:', error)
          }
        })

      groups
        .append('text')
        .attr('x', (d) => d3.polygonCentroid(d.coords)[0])
        .attr('y', (d) => d3.polygonCentroid(d.coords)[1])
        .attr('text-anchor', 'middle')
        .attr('dominant-baseline', 'middle')
        .attr('fill', '#fff')
        .attr('font-size', 14)
        .attr('pointer-events', 'none')
        .text((d) => d.name)
    },

    // 选择空间
    change (selectedSpace) {
      this.changeSpace(selectedSpace)
      this.fetchData(selectedSpace)
    },

    changeSpace (selectedSpace) {
      console.log('选中的空间 ID:', selectedSpace)
    },

    // 拉取空间信息
    async fetchData (spaceID) {
      try {
        const baseUrl = process.env.VUE_APP_API_BASE_URL
        const response = await axios.get(`${baseUrl}/api/spaces/${spaceID}`)
        const data = response.data

        this.metaData = data.properties.map(property => ({
          info: `${property.propertyKey}: ${property.propertyValue}`
        }))
        this.deviceData = Object.entries(data.fixedProperties).map(([key, value]) => ({
          name: key,
          info: value
        }))
        this.eventData = data.events.map(event => ({
          name: event.eventType,
          description: `事件 ID: ${event.eventId}`
        }))
        this.serviceData = data.services.map(service => ({
          name: service.serviceName,
          description: `服务 ID: ${service.serviceId}`
        }))
      } catch (error) {
        console.error('Error fetching data:', error)
      }
    },

    // 获取单个网格信息
    async fetchGridInfo (gridId) {
      try {
        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
        const response = await axios.get(`${baseUrl}/api/grid/${gridId}`)
        const data = response.data

        this.metaData = Object.entries(data.meta || {}).map(([key, value]) => ({
          info: `${key}: ${value}`
        }))
        this.deviceData = (data.devices || []).map(device => ({
          name: device.name,
          info: device.info
        }))
        this.eventData = data.events || []
        this.serviceData = data.services || []
      } catch (error) {
        console.error('获取网格信息失败:', error)
        this.$message.error('加载网格信息失败')
      }
    },

    async fetchSpaces () {
      try {
        const projectID = localStorage.getItem('project_id')
        const baseUrl = process.env.VUE_APP_API_BASE_URL
        const response = await axios.get(`${baseUrl}/api/spaces?project=${projectID}`)
        this.spaces = response.data
      } catch (error) {
        console.error('Error fetching spaces:', error)
      }
    },

    // 弹窗控制逻辑
    showEventModal () { this.eventModalVisible = true },
    handleEventOk () {
      if (!this.eventForm.name || !this.eventForm.description) {
        this.$message.warning('请填写完整的事件信息')
        return
      }
      this.eventData.push({ ...this.eventForm })
      this.eventForm = { name: '', description: '' }
      this.eventModalVisible = false
      this.$message.success('事件添加成功')
    },
    handleEventCancel () { this.eventModalVisible = false },

    showServiceModal () { this.serviceModalVisible = true },
    handleServiceOk () {
      if (!this.serviceForm.name || !this.serviceForm.description) {
        this.$message.warning('请填写完整的服务信息')
        return
      }
      this.serviceData.push({ ...this.serviceForm })
      this.serviceForm = { name: '', description: '' }
      this.serviceModalVisible = false
      this.$message.success('服务添加成功')
    },
    handleServiceCancel () { this.serviceModalVisible = false }
  },

  mounted () {
    setTimeout(() => {
      this.initMesh()
      this.fetchSpaces()
      this.fetchData(1)
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
  overflow: hidden;
}
.space-demo-container {
  display: flex;
  flex-direction: row;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}
.left-section {
  flex: 0 0 70%;
  height: 100vh;
  background-image: url('@/assets/screen_bg.png');
  background-size: contain;  /* ✅ 保持背景图原始比例 */
  background-position: center; /* 保证背景居中 */
}
.mesh-container {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: flex-start;
  align-items: center;
}
.svg-container {
  width: 100%;
  height: 100%;
  background-color: transparent;
  border: none;
  cursor: grab;
}
.svg-container:active { cursor: grabbing; }

.right-section {
  flex: 0 0 30%;
  background-color: #f5f5f5;
  overflow-y: auto;
  border-left: 1px solid #e8e8e8;
}
.form-container {
  background-color: #fff;
  width: 100%;
  padding: 20px;
}
.table-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background-color: #fafafa;
  border: 1px solid #f0f0f0;
  border-bottom: none;
  border-radius: 8px 8px 0 0;
}
.table-title {
  font-size: 14px;
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
}
</style>
