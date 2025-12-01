<template>
  <div class="space-demo-container">
    <!-- 左侧网格图容器 -->
    <div class="left-section">
      <!-- 动态绑定背景图 -->
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

        <!-- 网格元信息 -->
        <div class="table-wrapper">
          <div class="table-header">
            <span class="table-title">网格元信息</span>
          </div>
          <a-table
            :columns="metaColumns"
            :dataSource="metaData"
            :pagination="false"
            size="small"
          />
        </div>

        <div style="height: 20px;"></div>

        <!-- 标签页容器 -->
        <el-tabs type="border-card">
          <!-- 设备 -->
          <el-tab-pane label="设备">
            <a-table
              :columns="deviceColumns"
              :dataSource="deviceData"
              :pagination="false"
              size="small"
            />
          </el-tab-pane>

          <!-- 事件 -->
          <el-tab-pane label="环境级事件">
            <a-table
              :columns="eventColumns"
              :dataSource="eventData"
              :rowKey="record => record.id || record.eventType"
              :pagination="false"
              size="small"
            />
            <a-button type="primary" size="small" @click="showEventModal">
              添加环境级事件
            </a-button>
          </el-tab-pane>

          <!-- 服务 -->
          <el-tab-pane label="环境级服务">
            <a-table
              :columns="serviceColumns"
              :dataSource="serviceData"
              :rowKey="record => record.id || record.serviceName"
              :pagination="false"
              size="small"
            />
            <a-button type="primary" size="small" @click="showServiceModal">
              添加环境级服务
            </a-button>
          </el-tab-pane>

          <!-- 应用 -->
          <el-tab-pane label="应用">
            <a-table
              :columns="applicationColumns"
              :dataSource="applicationData"
              :rowKey="record => record.id || record.eventType"
              :pagination="false"
              size="small"
            />
            <a-button type="primary" size="small" @click="addApplication">
              添加应用
            </a-button>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

  </div>
</template>

<script>
import axios from 'axios'
import * as d3 from 'd3'

// ✅ 导入三种类型的 JSON 数据
import FCity from './F-city.json'
import FCommunity from './F-community.json'
import FPark from './F-park.json'

// 导入三种背景图片
import CityImg from '@/assets/City.png'
import CommunityImg from '@/assets/Community.jpg'
import ParkImg from '@/assets/Park.jpg'

export default {
  name: 'SpaceDemo',
  data () {
    return {
      isLoading: false,
      selectedType: 'F-city', // 默认类型
      backgroundImage: CityImg, // 默认背景
      backgroundOffset: 'calc(50% - 180px) center',

      // 网格类型映射
      meshTypeOptions: {
        'F-city': '城区网格',
        'F-community': '社区网格',
        'F-park': '园区网格'
      },

      // JSON 映射
      meshFiles: {
        'F-city': FCity,
        'F-community': FCommunity,
        'F-park': FPark
      },

      // 背景图映射
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
      // 优化后的设备表格列定义
      deviceColumns: [
        {
          title: '设备名称',
          dataIndex: 'name',
          key: 'name',
          width: 150, // 设定一个固定宽度
          align: 'center' // 内容居中
        },
        {
          title: '设备功能',
          dataIndex: 'info',
          key: 'info',
          align: 'left', // 描述性内容左对齐
          // 添加自定义渲染，优化长文本显示，并提供 Tooltip
          customRender: (text) => {
            const value = text || '无功能描述'
            // 如果文本长度超过 20 个字符，则进行截断
            const shortText = value.length > 20 ? value.substring(0, 18) + '...' : value

            return (
              <a-tooltip placement="topLeft" title={value}>
                <span>{shortText}</span>
              </a-tooltip>
            )
          }
        }
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
  created () {
    // 1. 从 URL 路由参数中读取我们从 ProjectSelection 传递过来的值
    const initialMeshType = this.$route.query.initialMeshType

    // 2. 如果参数存在且是有效的网格类型，则覆盖默认的 selectedType
    if (initialMeshType && this.meshFiles[initialMeshType]) {
      this.selectedType = initialMeshType

      // 调试：确认参数被读取
      console.log('Router param detected. Initializing with:', initialMeshType)
    }
  },

  methods: {
    // 切换网格类型 + 更新背景图
    handleMeshTypeChange (type) {
      if (type === 'F-city') this.backgroundOffset = 'calc(50% - 180px) center'
      if (type === 'F-community') this.backgroundOffset = 'center center'
      if (type === 'F-park') this.backgroundOffset = 'center center'
      // 2. 切换 selectedType (防止手动切换时 data 不更新，但此处在 created/mounted 阶段调用时无需修改 data)
      this.selectedType = type

      this.$message.info(`切换到 ${this.meshTypeOptions[type]} 数据`)

      // 3. 设置背景图片
      this.backgroundImage = this.backgroundMap[type] || CityImg

      // 4. 加载网格数据
      this.loadMeshData(type)
    },

    // 加载指定网格类型数据
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

    // 绘制SVG网格
    drawSvg (meshType) {
      const svgEl = d3.select(this.$refs.svg)
      svgEl.selectAll('*').remove()

      svgEl
        .attr('preserveAspectRatio', 'xMidYMid meet')
        .attr('viewBox', '0 0 3000 1600')

      const zoomG = svgEl.append('g').attr('class', 'zoom-group')
      // 根据背景图片尺寸与坐标系调整缩放和平移
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

          // // 仅当当前为城区网格时启用跳转逻辑
          // if (this.selectedType === 'F-city') {
          //   // 判断点击的网格编号
          //   if (d.id.includes('f-city-11')) {
          //     this.$message.success('进入社区网格页面')
          //     this.selectedType = 'F-community'
          //     this.handleMeshTypeChange('F-community')
          //     return
          //   }
          //   if (d.id.includes('f-city-7')) {
          //     this.$message.success('进入园区网格页面')
          //     this.selectedType = 'F-park'
          //     this.handleMeshTypeChange('F-park')
          //     return
          //   }
          // }

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

    // 获取单个网格信息
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
    showEventModal () {
      const gridId = this.gridId // 获取当前网格的 ID
      if (!gridId) {
        this.$message.warning('未选择网格 ID')
        return
      }
      const nodeRedUrl = `http://10.176.65.202:1880?gridId=${gridId}`

      // 弹出新的 Node-RED 窗口
      window.open(nodeRedUrl, '_blank') // '_blank' 会在新标签页中打开链接
    },

    showServiceModal () {
      const gridId = this.gridId // 获取当前网格的 ID
      if (!gridId) {
        this.$message.warning('未选择网格 ID')
        return
      }
      const nodeRedUrl = `http://10.176.65.202:1880?gridId=${gridId}`

      // 弹出新的 Node-RED 窗口
      window.open(nodeRedUrl, '_blank') // '_blank' 会在新标签页中打开链接
    },

    addApplication () {
      if (this.gridId === null) {
        this.$message.warning('请选择网格')
      } else {
        this.$message.success('选择网格：' + this.gridId)
      }
    }
  },

  mounted () {
    this.handleMeshTypeChange(this.selectedType)
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
  height: 100%;  /* 确保左侧容器的高度为100% */
}
.right-section {
  flex: 0 0 30%;
  background: #f5f5f5;
  overflow-y: auto;
  border-left: 1px solid #ddd;
  height: 100%;  /* 确保右侧容器的高度为100% */
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
.table-wrapper {
  margin-bottom: 10px; /* 适当调整表格之间的间距 */
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
