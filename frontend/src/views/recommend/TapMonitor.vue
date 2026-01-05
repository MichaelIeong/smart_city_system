<template>
  <page-header-wrapper>
    <div class="mesh-card">
      <div class="mesh-container">
        <svg ref="svg" class="svg-container"></svg>
      </div>

      <div class="event-log-panel">
        <div class="panel-header">
          <div class="header-left">
            <span class="dot-live"></span>
            <span class="title">实时应用日志监控</span>
          </div>
          <span class="device-status">系统运行中</span>
        </div>

        <div class="log-list">
          <div v-for="(item, index) in eventLogs" :key="index" class="log-item">
            <div class="log-content-wrapper">
              <div class="log-info">
                <div class="log-time">{{ item.time }}</div>
                <div class="log-content">{{ item.content }}</div>
              </div>
              <div class="log-actions" v-if="item.type === 'application' && item.status === 'start'">
                <span class="detail-btn" @click="handleDetail(item)">查看详情</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <a-modal
      v-model="logModalVisible"
      title="应用日志详情"
      :footer="null"
      :width="800"
      :destroyOnClose="true"
    >
      <a-spin :spinning="logModalLoading">
        <a-empty
          v-if="!logModalLoading && logModalLogs.length === 0"
          description="暂无日志"
        />
        <div v-else style="max-height: 600px; overflow-y: auto;">
          <div
            v-for="(line, index) in logModalLogs"
            :key="index"
            style="padding: 6px 0; border-bottom: 1px solid #f0f0f0;"
          >
            {{ line }}
          </div>
        </div>
      </a-spin>
    </a-modal>
  </page-header-wrapper>
</template>

<script>
/* eslint-disable */
import * as d3 from 'd3'
import { message } from 'ant-design-vue'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'
import data from './F-city.json'
import { getLog } from '@/api/manage'
import { Str } from 'prelude-ls'

export default {
  name: 'TapMonitor',
  data () {
    return {
      polygons: [],
      stompClient: null,
      // 模拟日志数据
      eventLogs: [],
      // eventType -> label（气泡显示）
      eventTypeLabelMap: {
        'manhole-flooding': '井盖水浸',
        'manhole-tilte': '井盖倾斜',
        'truck_dect': '渣土车识别',
        'ill_parking': '机动车违章停车',
        'ill_parking2': '非机动车违章停车',
        'waste_accumulate': '垃圾堆积',
        'greenbelt_stack': '绿化带堆放',
        'road-operate': '占道经营',
        'out-store': '店外经营',
        'road-feeding': '占道饲养',
        'trash_full': '垃圾桶满溢'
      },
      logModalVisible: false,
      logModalLoading: false,
      logModalLogs: []
    }
  },

  mounted() {
    // ✅ 非响应式字段：放实例上，避免 Vue2 对 Map/复杂对象的坑
    this.__d3 = { svg: null, zoomG: null, bubbleLayer: null }
    this.__bubbleMap = new Map()
    this.handleData()
    this.$nextTick(() => {
      setTimeout(() => {
        this.drawSvg()
      }, 200)
    })
    this.connectWebSocket()
  },

  beforeDestroy() {
    this.disconnectWebSocket()
    // 清理引用
    this.__d3 = null
    this.__bubbleMap = null
  },

  methods: {
    handleData() {
      const list = Array.isArray(data.data) ? data.data : []
      this.polygons = list.map((item) => {
        const info = item.meshInfo || {}
        const gridList = info.meshGridList || []
        return {
          id: String(info.id || ''), // ✅ UUID string
          code: info.meshCode,
          name: info.meshName,
          type: info.meshType,
          coords: gridList.map(p => [Number(p.x), Number(p.y)])
        }
      })
    },

    drawSvg() {
      // ✅ 兜底初始化，防止 __d3 意外为空
      if (!this.__d3) this.__d3 = { svg: null, zoomG: null, bubbleLayer: null }

      const svgEl = d3.select(this.$refs.svg)
      svgEl.selectAll('*').remove()

      const mapWidth = 2080
      const mapHeight = 1360

      svgEl
        .attr('preserveAspectRatio', 'xMidYMid meet')
        .attr('viewBox', `0 0 ${mapWidth} ${mapHeight}`)

      const zoomG = svgEl.append('g').attr('class', 'zoom-group')

      const scale = 1.3
      const offsetX = -1870
      const offsetY = -275
      zoomG.attr('transform', `translate(${offsetX}, ${offsetY}) scale(${scale})`)

      // 保存引用
      this.__d3.svg = svgEl
      this.__d3.zoomG = zoomG

      // 网格层
      const gridLayer = zoomG.append('g').attr('class', 'grid-layer')

      const groups = gridLayer
        .selectAll('g.polygon-group')
        .data(this.polygons, d => d.id)
        .enter()
        .append('g')
        .attr('class', 'polygon-group')
        .attr('data-id', d => d.id)

      groups.append('polygon')
        .attr('points', (d) => d.coords.map((p) => `${p[0]},${p[1]}`).join(' '))
        .attr('fill', () => d3.schemeCategory10[Math.floor(Math.random() * 10)])
        .attr('stroke', '#fff')
        .attr('stroke-width', 1.5)
        .attr('fill-opacity', 0.7)

      groups.append('text')
        .attr('x', (d) => d3.polygonCentroid(d.coords)[0])
        .attr('y', (d) => d3.polygonCentroid(d.coords)[1])
        .attr('text-anchor', 'middle')
        .attr('dominant-baseline', 'middle')
        .attr('fill', '#fff')
        .attr('font-size', 14)
        .attr('pointer-events', 'none')
        .text((d) => d.name)

      // 气泡层（顶层）
      const bubbleLayer = zoomG.append('g').attr('class', 'bubble-layer')
      this.__d3.bubbleLayer = bubbleLayer

      // 初次渲染
      this.renderBubbles()
    },
    addBubble({ location, eventType }) {
      const loc = String(location || '')
      const et = String(eventType || '')
      if(!loc || !et) return
      const arr = this.__bubbleMap.get(loc) || []
      const idx = arr.findIndex(b => String(b.eventType) === et)
      if(idx >= 0) {
        const existed = arr.splice(idx, 1)[0]
        arr.unshift(existed)
      } else {
        const bubble = {
          id: `${loc}_${et}`,
          location: loc,
          eventType: et
        }
        arr.unshift(bubble)
        this.__bubbleMap.set(loc, arr)
      }
      this.renderBubbles()
    },
    renderBubbles() {
      if (!this.__d3 || !this.__bubbleMap) return

      // 如果还没 drawSvg 或 bubbleLayer 丢了，直接跳过
      if (!this.__d3.bubbleLayer) return

      const layer = this.__d3.bubbleLayer
      const bubbles = []
      this.__bubbleMap.forEach((arr, location) => {
        (arr || []).forEach((b, idx) => {
          bubbles.push({ ...b, stackIndex: idx })
        })
      })

      const sel = layer
        .selectAll('g.event-bubble')
        .data(bubbles, d => d.id)

      sel.exit()
        .transition()
        .duration(200)
        .style('opacity', 0)
        .remove()

      const enter = sel.enter()
        .append('g')
        .attr('class', 'event-bubble')
        .style('pointer-events', 'auto')
        .style('opacity', 0)

      enter.append('rect')
        .attr('rx', 8)
        .attr('ry', 8)
        .attr('fill', 'rgba(24, 144, 255, 0.85)')
        .attr('stroke', 'rgba(255, 255, 255, 0.5)')
        .attr('stroke-width', 1)

      enter.append('text')
        .attr('fill', '#fff')
        .attr('font-size', 12)
        .attr('dominant-baseline', 'middle')

      enter.on('click', (event, d) => {
        const label = this.eventTypeLabelMap[d.eventType] || d.eventType
        this.$message.info(`事件：${label}`)
      })

      const merged = enter.merge(sel)

      merged.each((d, i, nodes) => {
        const g = d3.select(nodes[i])

        const locId = String(d.location || '')
        const poly = this.polygons.find(p => p.id === locId)
        if (!poly) return

        const [cx, cy] = d3.polygonCentroid(poly.coords)

        const label = this.eventTypeLabelMap[d.eventType] || d.eventType

        const yOffset = 26 + (d.stackIndex || 0) * 30

        const textSel = g.select('text')
          .text(label)
          .attr('x', cx)
          .attr('y', cy - yOffset)

        // bbox 计算背景尺寸
        const node = textSel.node()
        if (!node) return
        const bbox = node.getBBox()

        const padX = 10
        const padY = 6
        g.select('rect')
          .attr('x', bbox.x - padX)
          .attr('y', bbox.y - padY)
          .attr('width', bbox.width + padX * 2)
          .attr('height', bbox.height + padY * 2)
      })

      merged
        .transition()
        .duration(200)
        .style('opacity', 1)
    },

    connectWebSocket() {
      const SOCKET_URL = (import.meta && import.meta.env && import.meta.env.VITE_SOCKET_URL) || process.env.VUE_APP_SOCKET_URL
      console.log('SOCKET_URL:', SOCKET_URL)

      const socket = new SockJS(SOCKET_URL)
      this.stompClient = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,

        onConnect: () => {
          message.success('WebSocket 已连接')
          console.log('WebSocket 已连接')

          this.stompClient.subscribe('/topic/alerts', (msg) => {
            const payload = JSON.parse(msg.body)
            console.log('WS payload:', payload)
            switch (payload.type) {
              case 'event':
                this.handleEventMessage(payload)
                break
              case 'application':
                this.handleApplicationMessage(payload)
                break
              default:
                // 其他类型（alarm / log / metric 等）
                break
            }
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

    async handleDetail(item) {
      try {
        this.logModalVisible = true
        this.logModalLoading = true
        this.logModalLogs = []
        const logs = await getLog(item.eventType, item.waitValue)
        this.logModalLogs = logs
      } catch (e) {
        console.error('获取日志失败', e)
        this.$message.error('获取日志失败')
        this.logModalVisible = false
      } finally {
        this.logModalLoading = false
      }
    },
    handleEventMessage(payload) { 
      const location = String(payload.location || '')
      const eventType = payload?.data?.eventType
      const timestamp = payload.timestamp
      const type = payload.type
      if (!location || !eventType) return
      // 写入/覆盖该网格的气泡
      this.addBubble({
        location,
        eventType
      })
      const mesh = this.polygons.find(p => String(p.id) === location)
      const meshName = mesh?.name || `网格(${location.slice(0, 6)}...)`
      const eventLabel = this.eventTypeLabelMap?.[eventType] || eventType
      const timeText = this.formatLogTime(timestamp)
      this.eventLogs.push({
        time: timeText,
        content: `${meshName}发生${eventLabel}事件`,
        type: type
      })
    },
    handleApplicationMessage(payload) {
      const location = String(payload.location || '')
      const data = payload.data || {}
      const status = data.status
      const appName = data.appName
      const eventType = data.eventType
      const waitValue = data.waitValue
      const timestamp = payload.timestamp
      const type = payload.type
      const mesh = this.polygons.find(p => String(p.id) === location)
      const meshName = mesh?.name || `网格(${location.slice(0, 6)}...)`
      const timeText = this.formatLogTime(timestamp)
      if(status === 'start') {
        this.eventLogs.push({
          time: timeText,
          content: `${meshName}开始执行${appName}`,
          type: type,
          status: status,
          eventType: eventType,
          waitValue: waitValue
        })
        return
      }
      if(status === 'end') {
        this.eventLogs.push({
          time: timeText,
          content: `${meshName}${appName}执行结束`
        })
        const et = String(eventType || '')
        if (et && this.__bubbleMap) {
          const arr = this.__bubbleMap.get(location) || []
          const nextArr = arr.filter(b => String(b.eventType) !== et)
          if (nextArr.length > 0) this.__bubbleMap.set(location, nextArr)
          else this.__bubbleMap.delete(location)
          this.renderBubbles()
        }
      }
    },
    formatLogTime(ts) {
      if (!ts) return ''
      // 如果是 ISO 字符串：2026-01-05T09:22:48.94084
      // 直接替换成更友好的格式
      if (typeof ts === 'string') {
        const s = ts.replace('T', ' ')
        // 去掉小数秒（可选）
        return s.split('.')[0]
      }
      // 兜底：如果是 Date 或其他类型
      try {
        const d = new Date(ts)
        if (isNaN(d.getTime())) return String(ts)
        const pad = (n) => String(n).padStart(2, '0')
        return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
      } catch (e) {
        return String(ts)
      }
    }
  }
}
</script>

<style lang="less" scoped>
/* 基础容器 */
.mesh-card {
  position: relative;
  width: 100%;
  height: calc(100vh - 200px);
  border-radius: 12px;
  overflow: hidden;
  background-color: #000c17;
  background-image: url('@/assets/city2.png');
  background-size: cover;
  background-position: center;
}

.mesh-container {
  width: 100%;
  height: 100%;
}

.svg-container {
  width: 100%;
  height: 100%;
  display: block;
}

/* --- 优化后的日志面板样式 --- */
.event-log-panel {
  position: absolute;
  right: 30px;
  top: 80px;

  width: 450px;
  height: 500px;

  z-index: 100;
  display: flex;
  flex-direction: column;

  background: rgba(33, 48, 65, 0.85);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(0, 191, 255, 0.3);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);
  border-radius: 6px;
  overflow: hidden;

  .panel-header {
    padding: 10px 14px;
    background: linear-gradient(to right, rgba(0, 191, 255, 0.15), transparent);
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-left {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .title {
      color: #e6f7ff;
      font-weight: 600;
      font-size: 14px;
      letter-spacing: 0.5px;
    }

    .device-status {
      font-size: 10px;
      color: #52c41a;
      background: rgba(82, 196, 26, 0.1);
      padding: 1px 6px;
      border-radius: 4px;
    }

    .dot-live {
      width: 8px;
      height: 8px;
      background: #ff4d4f;
      border-radius: 50%;
      box-shadow: 0 0 6px #ff4d4f;
      animation: blink 1.5s infinite;
    }
  }

  .log-list {
    padding: 10px;
    overflow-y: auto;
    flex: 1;

    &::-webkit-scrollbar { width: 4px; }
    &::-webkit-scrollbar-thumb { background: rgba(0, 191, 255, 0.2); border-radius: 10px; }

    .log-item {
      padding: 10px;
      margin-bottom: 8px;
      background: rgba(255, 255, 255, 0.03);
      border-left: 3px solid #397a1e;
      border-radius: 2px;
      transition: background 0.3s;

      &:hover { background: rgba(255, 255, 255, 0.07); }

      .log-content-wrapper {
        display: flex;
        justify-content: space-between;
        align-items: flex-end;
      }

      .log-info {
        flex: 1;
        padding-right: 10px;
      }

      .log-time {
        font-size: 11px;
        color: #8c8c8c;
        margin-bottom: 4px;
        font-family: 'Helvetica', sans-serif;
      }

      .log-content {
        font-size: 12px;
        color: #d9d9d9;
        line-height: 1.4;
      }

      .log-actions {
        flex-shrink: 0;

        .detail-btn {
          font-size: 12px;
          color: #1890ff;
          cursor: pointer;
          padding: 2px 4px;
          border-radius: 4px;
          transition: all 0.3s;
          border: 1px solid transparent;

          &:hover {
            color: #40a9ff;
            background: rgba(24, 144, 255, 0.1);
            border-color: rgba(24, 144, 255, 0.3);
          }
        }
      }
    }
  }
}

@keyframes blink {
  0% { opacity: 1; }
  50% { opacity: 0.4; }
  100% { opacity: 1; }
}
</style>
