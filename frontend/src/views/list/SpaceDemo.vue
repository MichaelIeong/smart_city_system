<template>
  <div class="space-demo-container">
    <!-- 左侧网格图容器 -->
    <div class="left-section">
      <!-- ✅ 动态绑定背景图 -->
      <div
        class="background-layer"
        :style="{
          backgroundImage: `url(${backgroundImage})`,
          backgroundPosition: backgroundOffset
        }"
      ></div>

      <div class="content-layer">
        <a-spin :spinning="isLoading" tip="Loading...">
          <div class="mesh-container">
            <svg ref="svg" class="svg-container"></svg>
          </div>
        </a-spin>
      </div>
    </div>

    <!-- 右侧控制区域 -->
    <div class="right-section">
      <div class="form-container">
        <div style="height: 30px;"></div>

        <!-- 下拉框：选择网格类型 -->
        <a-row :gutter="16" justify="center" align="middle" class="select-row">
          <a-select
            v-model="selectedType"
            placeholder="请选择网格类型"
            style="width: 100%"
            allow-clear
            @change="handleMeshTypeChange"
          >
            <a-select-option
              v-for="(label, key) in meshTypeOptions"
              :key="key"
              :value="key"
            >
              {{ label }}
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
              <span class="table-title">环境级事件</span>
              <a-button type="primary" size="small" @click="showEventModal">
                添加环境级事件
              </a-button>
            </div>
            <a-table
              :columns="eventColumns"
              :dataSource="eventData"
              :rowKey="record => record.id || record.eventType"
              :pagination="false"
              size="small"
            />
          </div>

          <!-- 服务 -->
          <div class="table-wrapper">
            <div class="table-header">
              <span class="table-title">环境级服务</span>
              <a-button type="primary" size="small" @click="showServiceModal">
                添加环境级服务
              </a-button>
            </div>
            <a-table
              :columns="serviceColumns"
              :dataSource="serviceData"
              :rowKey="record => record.id || record.serviceName"
              :pagination="false"
              size="small"
            />
          </div>

          <!-- 属性 -->
          <div class="table-wrapper">
            <div class="table-header">
              <span class="table-title">环境级属性</span>
              <a-button type="primary" size="small" @click="showPropertyModal">
                添加环境级属性
              </a-button>
            </div>
            <a-table
              :columns="propertyColumns"
              :dataSource="propertyData"
              :rowKey="record => record.id || record.propertyName"
              :pagination="false"
              size="small"
            />
          </div>

          <!-- 应用 -->
          <div class="table-wrapper">
            <div class="table-header">
              <span class="table-title">应用</span>
              <a-button type="primary" size="small" @click="addApplication">
                添加应用
              </a-button>
            </div>
            <a-table
              :columns="applicationColumns"
              :dataSource="applicationData"
              :rowKey="record => record.id || record.eventType"
              :pagination="false"
              size="small"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 事件弹窗 -->
    <a-modal
      v-model="eventModalVisible"
      title="添加事件"
      @ok="handleEventOk"
      @cancel="handleEventCancel"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="事件名称">
          <a-input v-model="eventForm.name" placeholder="请输入事件名称" />
        </a-form-item>
        <a-form-item label="事件描述">
          <a-textarea
            v-model="eventForm.description"
            placeholder="请输入事件描述"
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 服务弹窗 -->
    <a-modal
      v-model="serviceModalVisible"
      title="添加服务"
      @ok="handleServiceOk"
      @cancel="handleServiceCancel"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="服务名称">
          <a-input v-model="serviceForm.name" placeholder="请输入服务名称" />
        </a-form-item>
        <a-form-item label="服务描述">
          <a-textarea
            v-model="serviceForm.description"
            placeholder="请输入服务描述"
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 属性弹窗 -->
    <a-modal
      v-model="propertyModalVisible"
      title="添加属性"
      @ok="handlePropertyOk"
      @cancel="handlePropertyCancel"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 16 }">
        <a-form-item label="属性名称">
          <a-input v-model="propertyForm.name" placeholder="请输入属性名称" />
        </a-form-item>
        <a-form-item label="属性描述">
          <a-textarea
            v-model="propertyForm.description"
            placeholder="请输入属性描述"
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script>
import axios from 'axios'
import * as d3 from 'd3'

// ✅ 导入三种类型的 JSON 数据
import FCity from './F-city.json'
import FCommunity from './F-community.json'
import FPark from './F-park.json'

// ✅ 导入三种背景图片
import CityImg from '@/assets/City.png'
import CommunityImg from '@/assets/Community.jpg'
import ParkImg from '@/assets/Park.jpg'

export default {
  name: 'SpaceDemo',
  data () {
    return {
      isLoading: false,
      selectedType: 'F-city', // 默认类型
      backgroundImage: CityImg, // ✅ 默认背景
      backgroundOffset: 'calc(50% - 180px) center',

      // ✅ 网格类型映射
      meshTypeOptions: {
        'F-city': '城区网格',
        'F-community': '社区网格',
        'F-park': '园区网格'
      },

      // ✅ JSON 映射
      meshFiles: {
        'F-city': FCity,
        'F-community': FCommunity,
        'F-park': FPark
      },

      // ✅ 背景图映射
      backgroundMap: {
        'F-city': CityImg,
        'F-community': CommunityImg,
        'F-park': ParkImg
      },

      polygons: [],

      // 表格定义
      metaColumns: [{ title: '网格元信息', dataIndex: 'info', key: 'info' }],
      gridId: null,
      metaData: [],
      deviceColumns: [
        { title: '设备名称', dataIndex: 'name', key: 'name' },
        { title: '设备功能', dataIndex: 'info', key: 'info' }
      ],
      deviceData: [],
      eventColumns: [
        { title: '事件名称', dataIndex: 'eventType', key: 'name' },
        {
          title: '事件描述',
          dataIndex: 'description',
          key: 'description',
          customRender: (text, record) => {
            const value = text || ''
            const shortText = value.length > 15 ? value.substring(0, 15) + '...' : value

            return (
              <a-tooltip placement="topLeft" title={value}>
                <span>{shortText}</span>
              </a-tooltip>
            )
          }
        }
      ],
      eventData: [],
      serviceColumns: [
        { title: '服务名称', dataIndex: 'serviceName', key: 'name' },
        {
          title: '服务描述',
          dataIndex: 'description',
          key: 'description',
          customRender: (text, record) => {
            const value = text || ''
            const shortText = value.length > 15 ? value.substring(0, 15) + '...' : value

            return (
              <a-tooltip placement="topLeft" title={value}>
                <span>{shortText}</span>
              </a-tooltip>
            )
          }
        }
      ],
      serviceData: [],
      propertyColumns: [
        { title: '属性名称', dataIndex: 'propertyName', key: 'name' },
        {
          title: '属性描述',
          dataIndex: 'description',
          key: 'description',
          customRender: (text, record) => {
            const value = text || ''
            const shortText = value.length > 15 ? value.substring(0, 15) + '...' : value

            return (
              <a-tooltip placement="topLeft" title={value}>
                <span>{shortText}</span>
              </a-tooltip>
            )
          }
        }
      ],
      propertyData: [],
      applicationColumns: [
        { title: '触发事件类型', dataIndex: 'eventType', key: 'name' },
        {
          title: '应用描述',
          dataIndex: 'description',
          key: 'description',
          customRender: (text, record) => {
            const value = text || ''
            const shortText = value.length > 15 ? value.substring(0, 15) + '...' : value

            return (
              <a-tooltip placement="topLeft" title={value}>
                <span>{shortText}</span>
              </a-tooltip>
            )
          }
        }
      ],
      applicationData: [],

      // 弹窗
      eventModalVisible: false,
      serviceModalVisible: false,
      propertyModalVisible: false,
      eventForm: { name: '', description: '' },
      serviceForm: { name: '', description: '' },
      propertyForm: { name: '', description: '' }
    }
  },

  methods: {
    // ✅ 切换网格类型 + 更新背景图
    handleMeshTypeChange (type) {
      if (type === 'F-city') this.backgroundOffset = 'calc(50% - 180px) center'
      if (type === 'F-community') this.backgroundOffset = 'center center'
      if (type === 'F-park') this.backgroundOffset = 'center center'
      this.$message.info(`切换到 ${this.meshTypeOptions[type]} 数据`)
      this.backgroundImage = this.backgroundMap[type] || CityImg
      this.loadMeshData(type)
    },

    // ✅ 加载指定网格类型数据
    async loadMeshData (meshType) {
      this.isLoading = true
      this.drawSvg(meshType)
      const data = this.meshFiles[meshType]?.data || []
      this.polygons = data.map(item => {
        const mesh = item.meshInfo
        return {
          id: mesh.meshCode,
          name: mesh.meshName,
          coords: mesh.meshGridList.map(p => [Number(p.x), Number(p.y)])
        }
      })
      this.drawSvg(meshType)
      this.isLoading = false
    },

    // ✅ 绘制SVG网格
    drawSvg (meshType) {
      const svgEl = d3.select(this.$refs.svg)
      svgEl.selectAll('*').remove()

      svgEl
        .attr('preserveAspectRatio', 'xMidYMid meet')
        .attr('viewBox', '0 0 3000 1600')

      const zoomG = svgEl.append('g').attr('class', 'zoom-group')
      // ✅ 根据背景图片尺寸与坐标系调整缩放和平移
      // 示例参数：scale=1.8 表示放大，translate 正数表示向右/下平移，负数向左/上偏移
      let scale = 2.7; let offsetX = -3830; let offsetY = -1230
      if (meshType === 'F-community') { scale = 1.8; offsetX = -20; offsetY = -750 }
      if (meshType === 'F-park') { scale = 1.5; offsetX = -50; offsetY = -700 }

      // 设置初始平移和缩放，让网格与底图重合
      zoomG.attr('transform', `translate(${offsetX}, ${offsetY}) scale(${scale})`)

      // 初始化缩放交互
      // const zoom = d3.zoom()
      //   .scaleExtent([0.5, 5])
      //   .on('zoom', (event) => zoomG.attr('transform', event.transform))
      // svgEl.call(zoom)

      const groups = zoomG
        .selectAll('g')
        .data(this.polygons)
        .enter()
        .append('g')
        .attr('class', 'polygon-group')

      groups.append('polygon')
        .attr('points', d => d.coords.map(p => `${p[0]},${p[1]}`).join(' '))
        .attr('fill', () => d3.schemeCategory10[Math.floor(Math.random() * 10)])
        .attr('stroke', '#fff')
        .attr('stroke-width', 1.5)
        .attr('fill-opacity', 0.7)
        .on('click', async (event, d) => {
          d3.selectAll('polygon').attr('stroke', '#fff').attr('stroke-width', 1.5)
          d3.select(event.currentTarget)
            .attr('stroke', '#000')
            .attr('stroke-width', 3)

          // 打印调试信息
          console.log('点击网格:', d.id)

          // 仅当当前为城区网格时启用跳转逻辑
          if (this.selectedType === 'F-city') {
            // 判断点击的网格编号
            if (d.id.includes('f-city-11')) {
              this.$message.success('进入社区网格页面')
              this.selectedType = 'F-community'
              this.handleMeshTypeChange('F-community')
              return
            }
            if (d.id.includes('f-city-7')) {
              this.$message.success('进入园区网格页面')
              this.selectedType = 'F-park'
              this.handleMeshTypeChange('F-park')
              return
            }
          }

          // 其他网格：正常加载详情
          this.$message.info(`加载网格 ID：${d.id}`)
          await this.fetchGridInfo(d.id)
        })

      groups.append('text')
        .attr('x', d => d3.polygonCentroid(d.coords)[0])
        .attr('y', d => d3.polygonCentroid(d.coords)[1])
        .attr('text-anchor', 'middle')
        .attr('dominant-baseline', 'middle')
        .attr('fill', '#fff')
        .attr('font-size', 14)
        .text(d => d.name)
    },

    // ✅ 获取单个网格信息
    async fetchGridInfo (gridId) {
      try {
        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
        const response = await axios.get(`${baseUrl}/api/grid/${gridId}`)
        const data = response.data
        this.gridId = data.id
        this.metaData = Object.entries(data.meta || {}).map(([k, v]) => ({ info: `${k}: ${v}` }))
        this.deviceData = (data.devices || []).map(dev => ({ name: dev.name, info: dev.info }))
        this.eventData = data.events || []
        this.serviceData = data.services || []
        this.propertyData = data.properties || []
        this.applicationData = data.applications || []
      } catch (err) {
        console.error('加载网格信息失败', err)
      }
    },

    // 弹窗控制逻辑
    showEventModal () { this.eventModalVisible = true },
    handleEventOk () {
      if (!this.eventForm.name || !this.eventForm.description) {
        this.$message.warning('请填写完整事件信息')
        return
      }
      this.eventData.push({ ...this.eventForm })
      this.eventForm = { name: '', description: '' }
      this.eventModalVisible = false
      this.$message.success('环境级事件添加成功')
    },
    handleEventCancel () { this.eventModalVisible = false },

    showServiceModal () { this.serviceModalVisible = true },
    handleServiceOk () {
      if (!this.serviceForm.name || !this.serviceForm.description) {
        this.$message.warning('请填写完整服务信息')
        return
      }
      this.serviceData.push({ ...this.serviceForm })
      this.serviceForm = { name: '', description: '' }
      this.serviceModalVisible = false
      this.$message.success('环境级服务添加成功')
    },
    handleServiceCancel () { this.serviceModalVisible = false },

    showPropertyModal () { this.propertyModalVisible = true },
    handlePropertyOk () {
      if (!this.propertyForm.name || !this.propertyForm.description) {
        this.$message.warning('请填写完整属性信息')
        return
      }
      this.propertyData.push({ ...this.propertyForm })
      this.propertyForm = { name: '', description: '' }
      this.propertyModalVisible = false
      this.$message.success('环境级属性添加成功')
    },
    handlePropertyCancel () { this.propertyModalVisible = false },

    addApplication () {
      if (this.gridId === null) {
        this.$message.warning('请选择网格')
      } else {
        this.$router.push(`/tap/recommend?gridId=${this.gridId}`)
      }
    }
  },

  mounted () {
    this.loadMeshData(this.selectedType)
  }
}
</script>

<style lang="less">
.space-demo-container {
  display: flex;
  flex-direction: row;
  width: 100%;
  height: 100vh;
}
.left-section {
  flex: 0 0 70%;
  position: relative;
}
.background-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center center;
  background-repeat: no-repeat;
  transition: background-image 0.5s ease;
  z-index: 1;
}
.content-layer {
  position: relative;
  width: 100%;
  height: 100%;
  z-index: 10;
}
.mesh-container {
  width: 100%;
  height: 100vh;
}
.svg-container {
  width: 100%;
  height: 100%;
}
.right-section {
  flex: 0 0 30%;
  background: #f5f5f5;
  overflow-y: auto;
  border-left: 1px solid #ddd;
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
  padding: 8px 12px;
  background: #fafafa;
  border: 1px solid #eee;
}
.table-title {
  font-weight: 500;
  color: rgba(0, 0, 0, 0.85);
}
</style>
