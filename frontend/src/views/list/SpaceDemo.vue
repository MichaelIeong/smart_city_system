<template>
  <div class="space-demo-container">
    <div class="left-section">
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

    <div class="right-section">
      <div class="form-container">
        <div style="height: 30px;"></div>

        <div v-if="!gridId" class="global-info-panel">

          <a-card title="场景全局信息" :bordered="true" style="width: 100%; background: #fafafa;">
            <p style="margin-bottom: 12px;">
              <b>当前场景：</b> <span style="font-size: 16px; color: #1890ff;">{{ globalInfo.sceneName }}</span>
            </p>
            <p style="margin-bottom: 12px;">
              <b>网格数量：</b> {{ globalInfo.gridCount }} 个
            </p>
            <p style="margin-bottom: 24px;">
              <b>设备总数：</b> {{ globalInfo.deviceTotal }} 台
            </p>
            <a-alert
              message="操作提示"
              description="点击左侧地图上的网格区域，可查看详细设备与服务信息。"
              type="info"
            />
          </a-card>

          <div style="height: 20px;"></div>

          <el-tabs type="border-card">

            <el-tab-pane label="设备类型">
              <a-table
                :columns="globalDeviceColumns"
                :dataSource="globalDeviceData"
                :pagination="false"
                size="small"
                :rowKey="record => record.name"
              />
            </el-tab-pane>

            <el-tab-pane label="全局事件">
              <a-table
                :columns="eventColumns"
                :dataSource="globalEventData"
                :rowKey="record => record.id || record.eventType"
                :pagination="false"
                size="small"
              />
              <a-button type="primary" size="small" @click="showGlobalEventModal" style="margin-top: 10px;">
                添加全局事件
              </a-button>
            </el-tab-pane>

            <el-tab-pane label="全局服务">
              <a-table
                :columns="serviceColumns"
                :dataSource="globalServiceData"
                :rowKey="record => record.id || record.serviceName"
                :pagination="false"
                size="small"
              />
              <a-button type="primary" size="small" @click="showGlobalServiceModal" style="margin-top: 10px;">
                添加全局服务
              </a-button>
            </el-tab-pane>

            <el-tab-pane label="全局应用">
              <a-table
                :columns="applicationColumns"
                :dataSource="globalApplicationData"
                :rowKey="record => record.id || record.eventType"
                :pagination="false"
                size="small"
              />
              <a-button type="primary" size="small" @click="routeToGlobalApplication" style="margin-top: 10px;">
                添加全局应用
              </a-button>
            </el-tab-pane>
          </el-tabs>

        </div>

        <div v-else class="grid-detail-panel">
          <div class="table-wrapper">
            <div class="table-header">
              <span class="table-title">区域信息</span>
              <a-button type="link" size="small" icon="rollback" @click="resetToGlobal">
                返回全局
              </a-button>
            </div>
            <a-table
              :columns="metaColumns"
              :dataSource="metaData"
              :pagination="false"
              :showHeader="false"
              size="small"
            />
          </div>

          <div style="height: 20px;"></div>

          <el-tabs type="border-card">
            <el-tab-pane label="设备类型">
              <a-table
                :columns="deviceTypeColumns"
                :dataSource="deviceTypeData"
                :pagination="false"
                size="small"
              />
            </el-tab-pane>

            <el-tab-pane label="网格事件">
              <a-table
                :columns="eventColumns"
                :dataSource="eventData"
                :rowKey="record => record.id || record.eventType"
                :pagination="false"
                size="small"
              />
              <a-button type="primary" size="small" @click="showEventModal" style="margin-top: 10px;">
                添加网格事件
              </a-button>
            </el-tab-pane>

            <el-tab-pane label="网格服务">
              <a-table
                :columns="serviceColumns"
                :dataSource="serviceData"
                :rowKey="record => record.id || record.serviceName"
                :pagination="false"
                size="small"
              />
              <a-button type="primary" size="small" @click="showServiceModal" style="margin-top: 10px;">
                添加网格服务
              </a-button>
            </el-tab-pane>

            <el-tab-pane label="网格应用">
              <a-table
                :columns="applicationColumns"
                :dataSource="applicationData"
                :rowKey="record => record.id || record.eventType"
                :pagination="false"
                size="small"
              />
              <a-button type="primary" size="small" @click="routeToRecommendApplication" style="margin-top: 10px;">
                添加网格应用
              </a-button>
            </el-tab-pane>
          </el-tabs>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import * as d3 from 'd3'

// 导入 JSON 数据
import FCity from './F-city.json'
import FCommunity from './F-community.json'
import FPark from './F-park.json'

// 导入背景图片
import CityImg from '@/assets/City.png'
import CommunityImg from '@/assets/Community.jpg'
import ParkImg from '@/assets/Park.jpg'

export default {
  name: 'SpaceDemo',
  data () {
    return {
      isLoading: false,
      selectedType: 'F-city',
      backgroundImage: CityImg,
      backgroundOffset: 'calc(50% - 180px) center',

      meshTypeOptions: {
        'F-city': '城区网格',
        'F-community': '社区网格',
        'F-park': '园区网格'
      },

      meshFiles: {
        'F-city': FCity,
        'F-community': FCommunity,
        'F-park': FPark
      },

      backgroundMap: {
        'F-city': CityImg,
        'F-community': CommunityImg,
        'F-park': ParkImg
      },

      polygons: [],

      // 全局概览信息
      globalInfo: {
        sceneName: '',
        gridCount: 0,
        deviceTotal: 0
      },

      // 全局设备列表数据
      globalDeviceData: [],
      // 全局设备列定义
      globalDeviceColumns: [
        { title: '设备类型', dataIndex: 'name', key: 'name', width: 120, align: 'center' },
        {
          title: '设备实例数量',
          dataIndex: 'count',
          key: 'count',
          width: 100,
          align: 'center',
          customRender: (text) => <span style="color: #1890ff; font-weight: bold;">{text}</span>
        }
      ],

      // 全局 事件/服务/应用 数据
      globalEventData: [],
      globalServiceData: [],
      globalApplicationData: [],

      // 核心状态
      gridId: null,

      // 网格详情 - 区域信息
      metaColumns: [{ title: '属性内容', dataIndex: 'info', key: 'info' }],
      metaData: [],

      // 网格详情 - 设备类型
      deviceTypeData: [],
      deviceTypeColumns: [
        { title: '设备类型', dataIndex: 'name', key: 'name', width: 120, align: 'center' },
        {
          title: '设备功能',
          dataIndex: 'info',
          key: 'info',
          width: 120,
          align: 'center',
          customRender: (text) => {
            let cleanText = text || '无功能描述'
            try {
              if (cleanText.startsWith('[') || cleanText.startsWith('{')) {
                const parsed = JSON.parse(cleanText)
                if (Array.isArray(parsed)) cleanText = parsed.join('、')
              }
            } catch (e) { cleanText = cleanText.replace(/^"|"$/g, '') }
            const shortText = cleanText.length > 20 ? cleanText.substring(0, 18) + '...' : cleanText
            return <a-tooltip placement="topLeft" title={cleanText}><span>{shortText}</span></a-tooltip>
          }
        },
        {
          title: '设备实例数量',
          dataIndex: 'count',
          key: 'count',
          width: 100,
          align: 'center',
          customRender: (text) => <span style="color: #1890ff; font-weight: bold;">{text}</span>
        }
      ],

      // 公用列定义
      eventColumns: [
        { title: '事件名称', dataIndex: 'eventType', key: 'name' },
        {
          title: '事件描述',
          dataIndex: 'description',
          key: 'description',
          customRender: (text) => {
            const value = text || ''
            const shortText = value.length > 15 ? value.substring(0, 15) + '...' : value
            return <a-tooltip placement="topLeft" title={value}><span>{shortText}</span></a-tooltip>
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
          customRender: (text) => {
            const value = text || ''
            const shortText = value.length > 15 ? value.substring(0, 15) + '...' : value
            return <a-tooltip placement="topLeft" title={value}><span>{shortText}</span></a-tooltip>
          }
        }
      ],
      serviceData: [],

      applicationColumns: [
        { title: '触发事件类型', dataIndex: 'eventType', key: 'name' },
        {
          title: '应用描述',
          dataIndex: 'description',
          key: 'description',
          customRender: (text) => {
            const value = text || ''
            const shortText = value.length > 15 ? value.substring(0, 15) + '...' : value
            return <a-tooltip placement="topLeft" title={value}><span>{shortText}</span></a-tooltip>
          }
        }
      ],
      applicationData: [],

      eventModalVisible: false,
      serviceModalVisible: false
    }
  },
  created () {
    // 1. 尝试从 URL 获取参数 (优先级最高)
    let initialMeshType = this.$route.query.initialMeshType

    // 2. 如果 URL 没参数，尝试从 LocalStorage 读取
    if (!initialMeshType) {
      initialMeshType = localStorage.getItem('current_scene_type')
    }

    // 3. 如果有值且在我们的支持列表中，就使用它
    if (initialMeshType && this.meshFiles[initialMeshType]) {
      this.selectedType = initialMeshType
    } else {
      // 4. 兜底：如果啥都没有，默认 F-city
      this.selectedType = 'F-city'
    }
  },

  mounted () {
    // 根据 created 里计算出的 selectedType 加载数据
    this.handleMeshTypeChange(this.selectedType)
  },

  methods: {
    handleMeshTypeChange (type) {
      // 根据不同的场景类型设置对应的 projectId
      const projectMap = {
        'F-city': 1,
        'F-community': 2,
        'F-park': 3
      }

      if (projectMap[type]) {
        localStorage.setItem('project_id', projectMap[type])
      }
      if (type === 'F-city') this.backgroundOffset = 'calc(50% - 180px) center'
      else this.backgroundOffset = 'center center'

      this.selectedType = type
      this.backgroundImage = this.backgroundMap[type] || CityImg
      this.loadMeshData(type)
    },

    async loadMeshData (meshType) {
      this.isLoading = true
      // 1. 重置视图
      this.resetToGlobal()

      // 2. 基础信息初始化
      const data = this.meshFiles[meshType]?.data || []
      this.globalInfo = {
        sceneName: this.meshTypeOptions[meshType],
        gridCount: data.length,
        deviceTotal: 0
      }

      const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
      const currentProjectId = localStorage.getItem('project_id')

      try {
        // 使用 Promise.all 同时发起请求，加快加载速度
        const [deviceRes, eventRes, serviceRes, appRes] = await Promise.all([
          // (1) 全局设备统计
          axios.get(`${baseUrl}/api/devices/global-summary`, { params: { sceneType: meshType } }),
          // (2) 全局事件 - 添加 projectId 参数
          axios.get(`${baseUrl}/api/devices/global-events`, { params: { projectId: currentProjectId } }),

          // (3) 全局服务 - 添加 projectId 参数
          axios.get(`${baseUrl}/api/devices/global-services`, { params: { projectId: currentProjectId } }),

          // (4) 全局应用 - 添加 projectId 参数
          axios.get(`${baseUrl}/api/devices/global-applications`, { params: { projectId: currentProjectId } })
        ])

        // --- 赋值设备数据 ---
        this.globalDeviceData = deviceRes.data || []
        // 计算设备总数
        if (this.globalDeviceData.length > 0) {
          this.globalInfo.deviceTotal = this.globalDeviceData.reduce((sum, item) => sum + item.count, 0)
        }

        // --- 赋值 全局事件/服务/应用 ---
        this.globalEventData = eventRes.data || []
        this.globalServiceData = serviceRes.data || []
        this.globalApplicationData = appRes.data || [] // 这里已经是 AppRuleInfo 列表，包含 eventType 和 description
      } catch (err) {
        console.error('加载全局数据失败', err)
        // 容错处理：如果某个接口挂了，确保界面不会崩，给空数组
        if (!this.globalDeviceData) this.globalDeviceData = []
        this.globalEventData = []
        this.globalServiceData = []
        this.globalApplicationData = []
        this.$message.warning('部分数据加载失败，请检查网络或后端服务')
      }

      // 4. 渲染地图多边形 (保持不变)
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

    resetToGlobal () {
      this.gridId = null
      const svgEl = d3.select(this.$refs.svg)
      svgEl.selectAll('polygon')
        .attr('stroke', '#fff')
        .attr('stroke-width', 1.5)
        .attr('fill-opacity', 0.7)
    },

    drawSvg (meshType) {
      const svgEl = d3.select(this.$refs.svg)
      svgEl.selectAll('*').remove()
      svgEl
        .attr('preserveAspectRatio', 'xMidYMid meet')
        .attr('viewBox', '0 0 3000 1600')

      const zoomG = svgEl.append('g').attr('class', 'zoom-group')
      let scale = 2.7; let offsetX = -3830; let offsetY = -1230
      if (meshType === 'F-community') { scale = 1.8; offsetX = -20; offsetY = -750 }
      if (meshType === 'F-park') { scale = 1.5; offsetX = -50; offsetY = -700 }

      zoomG.attr('transform', `translate(${offsetX}, ${offsetY}) scale(${scale})`)

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
        .style('cursor', 'pointer')
        .on('click', async (event, d) => {
          event.stopPropagation()
          if (this.gridId === d.id) {
            this.$message.info('返回全局视图')
            this.resetToGlobal()
          } else {
            d3.selectAll('polygon')
              .attr('stroke', '#fff')
              .attr('stroke-width', 1.5)
              .attr('fill-opacity', 0.7)

            d3.select(event.currentTarget)
              .attr('stroke', '#000')
              .attr('stroke-width', 3)
              .attr('fill-opacity', 0.9)

            this.$message.success(`加载网格：${d.name}`)
            await this.fetchGridInfo(d.id)
          }
        })

      groups.append('text')
        .attr('x', d => d3.polygonCentroid(d.coords)[0])
        .attr('y', d => d3.polygonCentroid(d.coords)[1])
        .attr('text-anchor', 'middle')
        .attr('dominant-baseline', 'middle')
        .attr('fill', '#fff')
        .attr('font-size', 14)
        .style('pointer-events', 'none')
        .text(d => d.name)
    },

    async fetchGridInfo (gridId) {
      const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'
      try {
        const response = await axios.get(`${baseUrl}/api/grid/${gridId}`)
        const data = response.data
        this.gridId = data.id
        this.metaData = Object.entries(data.meta || {}).map(([k, v]) => ({ info: `${k}: ${v}` }))
        this.eventData = data.events || []
        this.serviceData = data.services || []
        this.applicationData = data.applications || []
      } catch (err) {
        console.warn('网格元信息加载失败', err)
        this.gridId = gridId
        this.metaData = []
      }
      try {
        const res = await axios.get(`${baseUrl}/api/devices/grid-summary`, {
          params: { gridId: gridId }
        })
        this.deviceTypeData = res.data
      } catch (err) {
        console.error('获取网格设备统计失败', err)
        this.deviceTypeData = []
      }
    },

    showEventModal () {
      if (!this.gridId) return this.$message.warning('未选择网格 ID')
      const NODE_RED_URL = process.env.VUE_APP_NODE_RED_URL
      const projectId = localStorage.getItem('project_id')
      window.open(`${NODE_RED_URL}?type=2&gridId=${this.gridId}&projectId=${projectId}`, '_blank')
    },
    showServiceModal () {
      if (!this.gridId) return this.$message.warning('未选择网格 ID')
      const NODE_RED_URL = process.env.VUE_APP_NODE_RED_URL
      const projectId = localStorage.getItem('project_id')
      window.open(`${NODE_RED_URL}?type=3&gridId=${this.gridId}&projectId=${projectId}`, '_blank')
    },
    routeToRecommendApplication () {
      if (!this.gridId) return this.$message.warning('请选择网格')
      this.$router.push(`/tap/create?gridId=${this.gridId}`)
    },

    // 全局操作方法 (传递 crossRegion)
    showGlobalEventModal () {
      const NODE_RED_URL = process.env.VUE_APP_NODE_RED_URL
      const projectId = localStorage.getItem('project_id')
      window.open(`${NODE_RED_URL}?type=2&gridId=crossRegion&projectId=${projectId}`, '_blank')
    },

    showGlobalServiceModal () {
      const NODE_RED_URL = process.env.VUE_APP_NODE_RED_URL
      const projectId = localStorage.getItem('project_id')
      window.open(`${NODE_RED_URL}?type=3&gridId=crossRegion&projectId=${projectId}`, '_blank')
    },

    routeToGlobalApplication () {
      const projectId = localStorage.getItem('project_id')
      this.$router.push(`/tap/create?gridId=crossRegion&projectId=${projectId}`)
    }
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
  height: 100%;
  overflow: hidden;
}
.right-section {
  flex: 0 0 30%;
  background: #f5f5f5;
  overflow-y: auto;
  border-left: 1px solid #ddd;
  height: 100%;
}
.background-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-size: cover;
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
  display: block;
}
.form-container {
  background-color: #fff;
  width: 100%;
  min-height: 100%;
  padding: 20px;
}
.table-wrapper {
  margin-bottom: 10px;
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

.global-info-panel {
  animation: fadeIn 0.3s ease-in-out;
}
.grid-detail-panel {
  animation: fadeIn 0.3s ease-in-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(5px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
