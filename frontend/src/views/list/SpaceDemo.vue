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
              show-icon
            />
          </a-card>

          <div style="height: 20px;"></div>

          <div class="table-wrapper">
            <div class="table-header">
              <span class="table-title">场景内设备类型</span>
            </div>
            <a-table
              :columns="globalDeviceColumns"
              :dataSource="globalDeviceData"
              :pagination="false"
              size="small"
              :rowKey="record => record.name"
            />
          </div>

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

            <el-tab-pane label="环境级事件">
              <a-table
                :columns="eventColumns"
                :dataSource="eventData"
                :rowKey="record => record.id || record.eventType"
                :pagination="false"
                size="small"
              />
              <a-button type="primary" size="small" @click="showEventModal" style="margin-top: 10px;">
                添加环境级事件
              </a-button>
            </el-tab-pane>

            <el-tab-pane label="环境级服务">
              <a-table
                :columns="serviceColumns"
                :dataSource="serviceData"
                :rowKey="record => record.id || record.serviceName"
                :pagination="false"
                size="small"
              />
              <a-button type="primary" size="small" @click="showServiceModal" style="margin-top: 10px;">
                添加环境级服务
              </a-button>
            </el-tab-pane>

            <el-tab-pane label="应用">
              <a-table
                :columns="applicationColumns"
                :dataSource="applicationData"
                :rowKey="record => record.id || record.eventType"
                :pagination="false"
                size="small"
              />
              <a-button type="primary" size="small" @click="routeToRecommendApplication" style="margin-top: 10px;">
                添加应用
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

// 导入 JSON 数据 (确保路径正确)
import FCity from './F-city.json'
import FCommunity from './F-community.json'
import FPark from './F-park.json'

// 导入背景图片 (确保路径正确)
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
          title: '设备功能',
          dataIndex: 'info',
          key: 'info',
          align: 'left',
          customRender: (text) => {
            // 1. 尝试解析 JSON 字符串 (去除中括号和引号)
            let cleanText = text || '无功能描述'
            try {
              // 如果是 JSON 数组字符串，解析并用顿号连接
              if (cleanText.startsWith('[') || cleanText.startsWith('{')) {
                const parsed = JSON.parse(cleanText)
                if (Array.isArray(parsed)) {
                  cleanText = parsed.join('、') // 用顿号分隔：功能A、功能B
                } else if (typeof parsed === 'object') {
                  // 如果是对象数组 [{"key":...}, {"key":...}]，尝试提取 desc 或其他字段，或者直接序列化
                  // 这里针对你的数据特点（大部分是字符串数组）做简单处理
                  cleanText = JSON.stringify(parsed)
                }
              }
            } catch (e) {
              // 解析失败，说明可能本身就是普通字符串，不做处理
              // 或者去除首尾的 " 符号
              cleanText = cleanText.replace(/^"|"$/g, '')
            }

            // 2. 截断长文本用于显示
            const shortText = cleanText.length > 15 ? cleanText.substring(0, 15) + '...' : cleanText

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

      // 当前选中的网格ID (null表示全局视图)
      gridId: null,

      // 网格详情 - 区域信息
      metaColumns: [{ title: '属性内容', dataIndex: 'info', key: 'info' }],
      metaData: [],

      // 网格详情 - 设备类型 (聚合数据)
      deviceTypeData: [],
      deviceTypeColumns: [
        {
          title: '设备类型',
          dataIndex: 'name',
          key: 'name',
          width: 120,
          align: 'center'
        },
        {
          title: '设备功能',
          dataIndex: 'info',
          key: 'info',
          align: 'left',
          customRender: (text) => {
            let cleanText = text || '无功能描述'
            try {
              if (cleanText.startsWith('[') || cleanText.startsWith('{')) {
                const parsed = JSON.parse(cleanText)
                if (Array.isArray(parsed)) {
                  cleanText = parsed.join('、')
                }
              }
            } catch (e) {
              cleanText = cleanText.replace(/^"|"$/g, '')
            }

            const shortText = cleanText.length > 20 ? cleanText.substring(0, 18) + '...' : cleanText
            return (
              <a-tooltip placement="topLeft" title={cleanText}>
                <span>{shortText}</span>
              </a-tooltip>
            )
          }
        },
        {
          title: '设备实例数量',
          dataIndex: 'count',
          key: 'count',
          width: 100,
          align: 'center',
          customRender: (text) => {
            return <span style="color: #1890ff; font-weight: bold;">{text}</span>
          }
        }
      ],

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
    // 根据路由参数初始化场景
    const initialMeshType = this.$route.query.initialMeshType
    if (initialMeshType && this.meshFiles[initialMeshType]) {
      this.selectedType = initialMeshType
    }
  },

  mounted () {
    this.handleMeshTypeChange(this.selectedType)
  },

  methods: {
    // 切换场景
    handleMeshTypeChange (type) {
      if (type === 'F-city') this.backgroundOffset = 'calc(50% - 180px) center'
      else this.backgroundOffset = 'center center'

      this.selectedType = type
      this.backgroundImage = this.backgroundMap[type] || CityImg
      this.loadMeshData(type)
    },

    // 加载场景数据 (几何数据来自JSON，设备数据来自后端API)
    async loadMeshData (meshType) {
      this.isLoading = true

      // 1. 重置界面到全局视图
      this.resetToGlobal()

      // 2. 获取几何数据 (多边形坐标)
      const data = this.meshFiles[meshType]?.data || []

      // 初始化基础信息
      this.globalInfo = {
        sceneName: this.meshTypeOptions[meshType],
        gridCount: data.length,
        deviceTotal: 0
      }

      // 3. 调用后端 API 获取真实的全局设备统计
      try {
        const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

        // 发起请求：sceneType 参数对应后端的 meshNature (如 F-city)
        const res = await axios.get(`${baseUrl}/api/devices/global-summary`, {
          params: { sceneType: meshType }
        })

        // 赋值给表格数据源
        this.globalDeviceData = res.data

        // 累加计算设备总数
        if (this.globalDeviceData && this.globalDeviceData.length > 0) {
          this.globalInfo.deviceTotal = this.globalDeviceData.reduce((sum, item) => sum + item.count, 0)
        }
      } catch (err) {
        console.error('获取全局设备数据失败', err)
        this.globalDeviceData = []
      }

      // 4. 处理多边形渲染
      this.polygons = data.map(item => {
        const mesh = item.meshInfo
        return {
          // 关键：ID 必须与数据库 mesh_no 一致
          id: mesh.meshCode,
          name: mesh.meshName,
          coords: mesh.meshGridList.map(p => [Number(p.x), Number(p.y)])
        }
      })

      this.drawSvg(meshType)
      this.isLoading = false
    },

    // 重置到全局视图
    resetToGlobal () {
      this.gridId = null
      const svgEl = d3.select(this.$refs.svg)
      svgEl.selectAll('polygon')
        .attr('stroke', '#fff')
        .attr('stroke-width', 1.5)
        .attr('fill-opacity', 0.7)
    },

    // 绘制地图
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

    // 获取网格详情
    async fetchGridInfo (gridId) {
      const baseUrl = process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080'

      // 1. 获取网格元信息
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

      // 2. 调用后端 API 获取该网格的设备聚合统计
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
      window.open(`${NODE_RED_URL}?type=2&gridId=${this.gridId}`, '_blank')
    },

    showServiceModal () {
      if (!this.gridId) return this.$message.warning('未选择网格 ID')
      const NODE_RED_URL = process.env.VUE_APP_NODE_RED_URL
      window.open(`${NODE_RED_URL}?type=3&gridId=${this.gridId}`, '_blank')
    },

    routeToRecommendApplication () {
      if (!this.gridId) return this.$message.warning('请选择网格')
      this.$router.push(`/tap/create?gridId=${this.gridId}`)
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
