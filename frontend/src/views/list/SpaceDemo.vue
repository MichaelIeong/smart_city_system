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
        <div style="height: 30px;"></div> <!-- 留白区域 -->

        <!-- 下拉框部分 -->
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

        <!-- 下拉框和表格之间的留白 -->
        <div style="height: 30px;"></div>

        <!-- 四张表格 -->
        <div class="table-container">
          <!-- 网格元信息表（原状态表，移到第一位） -->
          <div class="table-wrapper">
            <a-table
              :columns="metaColumns"
              :dataSource="metaData"
              :pagination="false"
              size="small"
            />
          </div>

          <!-- 设备表（原属性表） -->
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

          <!-- 事件表 -->
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

          <!-- 服务表 -->
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

      // 表格1: 网格元信息（原状态表，调整到第一位）
      metaColumns: [
        {
          title: '网格元信息',
          dataIndex: 'info',
          key: 'info'
        }
      ],
      metaData: [],

      // 表格2: 设备（原属性表）
      deviceColumns: [
        {
          title: '设备名称',
          dataIndex: 'name',
          key: 'name'
        },
        {
          title: '设备功能',
          dataIndex: 'info',
          key: 'info'
        }
      ],
      deviceData: [],

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
      serviceData: [],

      // 弹窗控制
      eventModalVisible: false,
      serviceModalVisible: false,
      eventForm: {
        name: '',
        description: ''
      },
      serviceForm: {
        name: '',
        description: ''
      }
    }
  },
  methods: {
    initMesh () {
      this.isLoading = true
      this.handleData()
      this.drawSvg()
      this.isLoading = false
    },
    handleData () {
      const data = meshData.data || []
      this.polygons = data.map((item) => {
        const params = {}
        item.paramInfos.forEach((p) => (params[p.code] = +p.value || 0))
        return {
          id: item.meshInfo.meshCode,
          name: item.meshInfo.meshName,
          coords: item.meshInfo.meshGridList.map((p) => [Number(p.x), Number(p.y)]),
          is_mainroad: params.is_mainroad,
          is_residential: params.is_residential,
          is_businessdistrict: params.is_businessdistrict,
          is_other: params.is_other
        }
      })
    },
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

      // 添加缩放行为
      const zoom = d3.zoom()
        .scaleExtent([0.5, 5]) // 设置缩放范围：最小0.5倍，最大5倍
        .on('zoom', (event) => {
          zoomG.attr('transform', event.transform)
        })

      // 将缩放行为应用到SVG
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
        .attr('fill', (d) => {
          if (d.is_mainroad) return '#ff9f1c'
          if (d.is_residential) return '#cbf3f0'
          if (d.is_businessdistrict) return '#2ec4b6'
          return '#1a659e'
        })
        .attr('stroke', '#ffffff')
        .attr('stroke-width', 1.5)
        .attr('fill-opacity', 0.6)
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
            .attr('fill', () => {
              if (d.is_mainroad) return '#ff9f1c'
              if (d.is_residential) return '#cbf3f0'
              if (d.is_businessdistrict) return '#2ec4b6'
              return '#1a659e'
            })
        })
        .on('click', async (event, d) => {
          try {
            // 点击后高亮当前网格
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
    change (selectedSpace) {
      this.changeSpace(selectedSpace)
      this.fetchData(selectedSpace)
    },
    changeSpace (selectedSpace) {
      console.log('选中的空间 ID:', selectedSpace)
    },
    async fetchData (spaceID) {
      try {
        const baseUrl = process.env.VUE_APP_API_BASE_URL
        const response = await axios.get(`${baseUrl}/api/spaces/${spaceID}`)
        console.log('response data:', response.data)
        const data = response.data

        // 处理网格元信息（原状态数据，合并为单列）
        this.metaData = data.properties.map(property => ({
          info: `${property.propertyKey}: ${property.propertyValue}`
        }))

        // 处理设备信息（原固定属性）
        this.deviceData = Object.entries(data.fixedProperties).map(([key, value]) => ({
          name: key,
          info: value
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
    async fetchGridInfo (gridId) {
      try {
        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
        const response = await axios.get(`${baseUrl}/api/grid/${gridId}`)
        const data = response.data

        // === 网格元信息 ===
        this.metaData = Object.entries(data.meta || {}).map(([key, value]) => ({
          info: `${key}: ${value}`
        }))

        // === 设备信息 ===
        this.deviceData = (data.devices || []).map(device => ({
          name: device.name,
          info: device.info
        }))

        // === 事件、服务（目前为空） ===
        this.eventData = data.events || []
        this.serviceData = data.services || []

        console.log('网格信息加载完成:', data)
      } catch (error) {
        console.error('获取网格信息失败:', error)
        this.$message.error('加载网格信息失败')
      }
    },
    async fetchSpaces () {
      try {
        // 从 localStorage 获取保存的 projectId
        const projectID = localStorage.getItem('project_id')
        const baseUrl = process.env.VUE_APP_API_BASE_URL
        const response = await axios.get(`${baseUrl}/api/spaces?project=${projectID}`)
        this.spaces = response.data
      } catch (error) {
        console.error('Error fetching spaces:', error)
      }
    },
    // 显示添加事件弹窗
    showEventModal () {
      this.eventModalVisible = true
    },
    // 处理事件添加确认
    handleEventOk () {
      if (!this.eventForm.name || !this.eventForm.description) {
        this.$message.warning('请填写完整的事件信息')
        return
      }

      // 添加到事件列表
      this.eventData.push({
        name: this.eventForm.name,
        description: this.eventForm.description
      })

      // 这里可以添加 API 调用保存到后端
      console.log('添加事件:', this.eventForm)

      // 重置表单并关闭弹窗
      this.eventForm = { name: '', description: '' }
      this.eventModalVisible = false
      this.$message.success('事件添加成功')
    },
    // 取消添加事件
    handleEventCancel () {
      this.eventForm = { name: '', description: '' }
      this.eventModalVisible = false
    },
    // 显示添加服务弹窗
    showServiceModal () {
      this.serviceModalVisible = true
    },
    // 处理服务添加确认
    handleServiceOk () {
      if (!this.serviceForm.name || !this.serviceForm.description) {
        this.$message.warning('请填写完整的服务信息')
        return
      }

      // 添加到服务列表
      this.serviceData.push({
        name: this.serviceForm.name,
        description: this.serviceForm.description
      })

      // 这里可以添加 API 调用保存到后端
      console.log('添加服务:', this.serviceForm)

      // 重置表单并关闭弹窗
      this.serviceForm = { name: '', description: '' }
      this.serviceModalVisible = false
      this.$message.success('服务添加成功')
    },
    // 取消添加服务
    handleServiceCancel () {
      this.serviceForm = { name: '', description: '' }
      this.serviceModalVisible = false
    }
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

/* 左侧网格图容器 - 70% */
.left-section {
  flex: 0 0 70%;
  width: 70%;
  height: 100vh;
  overflow: hidden;
  position: relative;
  background-image: url('@/assets/screen_bg.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.mesh-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
  display: flex;
  justify-content: flex-start;
  align-items: center;
}

.svg-container {
  position: relative;
  width: 100%;
  height: 100%;
  background-color: transparent;
  border: none;
  box-shadow: none;
  z-index: 2;
  transition: all 0.3s;
  cursor: grab;
}

.svg-container:active {
  cursor: grabbing;
}

.ant-spin-nested-loading,
.ant-spin-container {
  width: 100%;
  height: 100%;
}

/* 右侧表单区域 - 30% */
.right-section {
  flex: 0 0 30%;
  width: 30%;
  height: 100vh;
  background-color: #f5f5f5;
  overflow-y: auto;
  border-left: 1px solid #e8e8e8;
}

.form-container {
  background-color: #ffffff;
  width: 100%;
  padding: 20px;
  box-sizing: border-box;
}

.select-row {
  width: 100%;
}

.table-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding-bottom: 20px;
}

.table-wrapper {
  width: 100%;
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

/* 响应式调整 - 小屏幕时调整比例 */
@media screen and (max-width: 1200px) {
  .left-section {
    flex: 0 0 60%;
    width: 60%;
  }

  .right-section {
    flex: 0 0 40%;
    width: 40%;
  }
}
</style>
