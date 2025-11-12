<template>
  <page-header-wrapper>
    <a-card
      :bordered="false"
      class="mesh-card"
      :style="{ borderRadius: '8px', height: 'calc(100vh - 250px)' }"
    >
      <div class="mesh-container">
        <svg ref="svg" class="svg-container"></svg>
        <!-- ✅ 右侧日志框 -->
        <transition name="slide">
          <div v-if="showLogPanel" class="log-panel">
            <div class="log-header">
              <span>{{ currentEventTypeLabel }}（{{ currentLocation }}）</span>
              <a-button
                type="text"
                @click="showLogPanel = false"
                style="float: right; color: #999;"
              >
                关闭
              </a-button>
            </div>
            <div class="log-body">
              <a-empty v-if="logs.length === 0" description="暂无日志" />
              <ul v-else>
                <li v-for="(item, idx) in logs" :key="idx">
                  <span class="index">{{ idx + 1 }}.</span> {{ item }}
                </li>
              </ul>
            </div>
          </div>
        </transition>
      </div>
    </a-card>
  </page-header-wrapper>
</template>

<script>
/* eslint-disable */
import * as d3 from 'd3'
import meshData from './meshData.json'
import { message } from 'ant-design-vue'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'
import { getLog } from '@/api/manage'

export default {
  name: 'CenterPanel',
  data() {
    return { 
        polygons: [],
        stompClient: null, // WebSocket客户端
        polygonMap: new Map(), // 存储 meshId -> polygon 元素
        showLogPanel: false,
        currentEventType: '',
        currentEventTypeLabel: '',
        currentLocation: '',
        logs: []
    }
  },
  mounted() {
    this.handleData()
    this.drawSvg()
    this.connectWebSocket() // 连接 WebSocket
  },
  beforeDestroy() {
    this.disconnectWebSocket() // 销毁 WebSocket
  },
  methods: {
    handleData() {
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

        // ✅ 初始化 polygonMap
        this.polygonMap.clear() // 先清空旧数据
        this.polygons.forEach((poly) => {
            this.polygonMap.set(poly.id, poly)
        })

        console.log('✅ 已注册网格Map:', this.polygonMap)
    },

    drawSvg() {
      const svgEl = d3.select(this.$refs.svg)
      svgEl.selectAll('*').remove()

      svgEl
        .attr('preserveAspectRatio', 'xMidYMid meet')
        .attr('viewBox', '0 0 3000 1600')

      // 主缩放组
      const zoomG = svgEl
        .append('g')
        .attr('class', 'zoom-group')
        .attr('transform', 'translate(-1200, 0) scale(0.95)')

      // 绘制网格图形
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
          if (d.is_mainroad) return '#F4A261'
          if (d.is_residential) return '#90CAF9'
          if (d.is_businessdistrict) return '#A5D6A7'
          return '#BDBDBD'
        })
        .attr('stroke', '#ECECEC')
        .attr('stroke-width', 1.5)
        .attr('fill-opacity', 0.8)
        .on('mouseover', function () {
          d3.select(this)
            .transition()
            .duration(200)
            .attr('fill', '#66BB6A')
            .attr('stroke-width', 2.5)
        })
        .on('mouseout', function (event, d) {
          d3.select(this)
            .transition()
            .duration(200)
            .attr('stroke-width', 1.5)
            .attr('fill', (d) => {
                if (d.is_mainroad) return '#F4A261'
                if (d.is_residential) return '#90CAF9'
                if (d.is_businessdistrict) return '#A5D6A7'
                return '#BDBDBD'
            })
        })

        // 冒泡层，位于最上方
        const bubbleLayer = zoomG.append('g').attr('class', 'bubble-layer')

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

    // 连接 WebSocket
    connectWebSocket() {
        const SOCKET_URL = (import.meta && import.meta.env && import.meta.env.VITE_SOCKET_URL) || process.env.VUE_APP_SOCKET_URL
        const socket = new SockJS(SOCKET_URL)
        this.stompClient = new Client({
            webSocketFactory: () => socket,
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onConnect: () => {
                message.success('WebSocket 已连接')
                console.log('WebSocket 已连接')
                // 订阅事件
                this.stompClient.subscribe('/topic/alerts', (message) => {
                    const data = JSON.parse(message.body)
                    console.log('事件类型: ', data.eventType, '位置: ', data.location, '命令: ', data.command, '时间: ', data.time)
                    // 处理事件数据
                    this.handleEventData(data)
                })
            },
            onDisconnect: () => {
                console.warn('WebSocket 已断开')
                message.warning('WebSocket 已断开')
            },
            onStompError: (frame) => {
                console.error('STOMP 错误:', frame.headers['message'])
                message.error('WebSocket 错误，请检查服务是否正常')
            }
        })
        this.stompClient.activate()
    },

    disconnectWebSocket() {
        if (this.stompClient && this.stompClient.deactivate) {
            this.stompClient.deactivate()
            console.log('WebSocket 已断开')
        }
    },

    // 处理WebSocket推送的事件数据
    handleEventData(data) { 
        const { eventType, location, command } = data
        if(!location) return
        const bubbleLayer = d3.select(this.$refs.svg).select('.bubble-layer')

        // 找到对应网格
        const targetPolygon = this.polygonMap.get(location)
        if (!targetPolygon) {
            console.warn(`未找到位置为 ${location} 的网格，跳过事件`)
            return
        }
        // 如果是start命令，添加冒泡
        if (command === 'start') {
            const [cx, cy] = d3.polygonCentroid(targetPolygon.coords)
            const offsetX = 60
            const offsetY = -60

            const bubble = bubbleLayer.append('g')
                .attr('class', 'bubble-label')
                .attr('data-id', location)
                .attr('data-type', eventType)
                .style('cursor', 'pointer') // 鼠标变手型
                .on('click', async () => {  // ✅ 绑定点击事件到整个组，而不是只绑定子元素
                    console.log('🔥 点击冒泡:', eventType, location)
                    try {
                        message.loading({ content: '加载日志中...', key: 'log', duration: 0 })
                        const res = await getLog(eventType, location)
                        message.destroy('log')

                        console.log('🔥 日志面板状态 before:', this.showLogPanel)
                        if (res && res.length > 0) {
                            this.logs = res
                            this.showLogPanel = true
                            this.currentEventType = eventType
                            this.currentEventTypeLabel = label
                            this.currentLocation = location
                            console.log('🔥 日志面板状态 after:', this.showLogPanel)
                        } else {
                            message.warning('暂无日志数据')
                        }
                    } catch (e) {
                        message.error('获取日志失败')
                        console.error(e)
                    }
                })

            // 背景框
            bubble.append('rect')
                .attr('x', cx - 60 + offsetX)
                .attr('y', cy + offsetY)
                .attr('rx', 6)
                .attr('ry', 6)
                .attr('width', 120)
                .attr('height', 40)
                .attr('fill', 'rgba(255,255,255,0.85)')
                .attr('stroke', '#000')
                .attr('stroke-width', 1.5)

            // 文字内容（可根据事件类型动态变化）
            const labelMap = {
                ill_parking: '违章停车',
            }
            const label = labelMap[eventType] || '事件'

            bubble.append('text')
                .attr('x', cx + offsetX)
                .attr('y', cy + offsetY + 25)
                .attr('text-anchor', 'middle')
                .attr('fill', '#000')
                .attr('font-size', 16)
                .attr('font-weight', 'bold')
                .text(label)
        }
        // 如果是end命令，删除对应位置的冒泡
        if (command === 'end') {
            const existing = bubbleLayer.select(`.bubble-label[data-id='${location}']`)
            if (!existing.empty()) {
                existing.transition()
                    .duration(300)
                    .attr('opacity', 0)
                    .remove()
                console.log(`❎ 清除冒泡: ${eventType} (${location})`)
            }
        }
    },
  }
}
</script>

<style lang="less" scoped>
.mesh-card {
  position: relative;
  box-shadow: none !important;
  padding: 0;
  background-image: url('@/assets/screen_bg.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.mesh-container {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: visible; /* ✅ 避免日志被裁掉 */
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
}

.log-panel {
  position: absolute;
  right: 40px;
  top: 50%; /* 从垂直中线开始 */
  transform: translateY(-50%); /* 向上偏移自身一半实现居中 */
  width: 360px;
  min-height: 300px; /* 可以让日志框有固定最小高度 */
  max-height: 80%; /* 限制高度，防止超出 */
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(8px);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  padding: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  border: 1px solid rgba(255, 255, 255, 0.4);
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateX(50px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.log-header {
  font-size: 16px;
  font-weight: 600;
  color: #222;
  margin-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.5);
  padding-bottom: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.log-header span {
  display: flex;
  align-items: center;
  gap: 8px;
}

.log-header span::before {
  content: "📋";
  font-size: 18px;
}

.log-body {
  flex: 1;
  overflow-y: auto;
  padding-right: 4px;
  scrollbar-width: thin;
}

.log-body::-webkit-scrollbar {
  width: 6px;
}
.log-body::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 4px;
}

.log-body ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.log-body li {
  margin-bottom: 10px;
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.9);
  border-left: 4px solid #409EFF;
  border-radius: 8px;
  font-size: 14px;
  color: #333;
  line-height: 1.4;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: all 0.2s ease;
}

.log-body li:hover {
  transform: translateX(3px);
  background: #f0f8ff;
}

.log-body li .index {
  color: #999;
  margin-right: 6px;
  font-weight: bold;
}
</style>
