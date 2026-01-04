<template>
  <page-header-wrapper>
    <div class="mesh-card">
      <div class="mesh-container">
        <svg ref="svg" class="svg-container"></svg>
      </div>
    </div>
  </page-header-wrapper>
</template>

<script>
/* eslint-disable */
import * as d3 from 'd3'
import { message } from 'ant-design-vue'
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs'
import data from './F-city.json'

export default {
  name: 'TapMonitor',
  data () {
    return {
      polygons: [],
      stompClient: null  // WebSocket客户端

    }
  },
  mounted() {
    this.handleData()
    // 等DOM稳定后再绘制，防止懒加载时偏移
    this.$nextTick(() => {
      setTimeout(() => {
        this.drawSvg()
      }, 200)
    }),
    this.connectWebSocket()  // 连接WebSocket
  },
  beforeDestroy() {
    this.disconnectWebSocket() // 销毁WebSocket
  },
  methods: {
    handleData() {
      const list = Array.isArray(data.data) ? data.data : []
      this.polygons = list.map((item) => {
        const info = item.meshInfo || {}
        const gridList = info.meshGridList || []
        return {
          id: info.id,
          code: info.meshCode,
          name: info.meshName,
          type: info.meshType,
          coords: gridList.map(p => [Number(p.x), Number(p.y)])
        }
      })
    },
    drawSvg() {
      const svgEl = d3.select(this.$refs.svg)
      svgEl.selectAll('*').remove()

      const mapWidth = 2080
      const mapHeight = 1360

      svgEl
        .attr('preserveAspectRatio', 'xMidYMid meet')
        .attr('viewBox', `0 0 ${mapWidth} ${mapHeight}`)

      // 主缩放组
      const zoomG = svgEl
        .append('g')
        .attr('class', 'zoom-group')

      const scale = 1.3
      const offsetX = -1870
      const offsetY = -275

      zoomG.attr('transform', `translate(${offsetX}, ${offsetY}) scale(${scale})`)

      // 绘制网格图形
      const groups = zoomG
        .selectAll('g')
        .data(this.polygons)
        .enter()
        .append('g')
        .attr('class', 'polygon-group')

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
    },
    // 连接WebSocket
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
    // 销毁WebSocket
    disconnectWebSocket() {
      if (this.stompClient && this.stompClient.deactivate) {
          this.stompClient.deactivate()
          console.log('WebSocket 已断开')
      }
    },
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
  background-color: #000c17; // 兜底深色背景
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
</style>
