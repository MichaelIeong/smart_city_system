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
              <div class="log-actions" v-if="item.content.includes('执行') && item.content.includes('应用')">
                <span class="detail-btn" @click="handleDetail(item)">查看详情</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </page-header-wrapper>
</template>

<script>
/* eslint-disable */
import * as d3 from 'd3'
import data from './F-city.json'

export default {
  name: 'TapMonitor',
  data () {
    return {
      polygons: [],
      // 新增：模拟日志数据
      eventLogs: [
        { time: '2025-12-20 15:30', content: '永德城区02网格执行跨区域渣土车抛洒处理应用' },
        { time: '2025-12-20 15:29', content: '永德城区02网格发生跨区域渣土车抛洒事件' },
        { time: '2025-12-20 15:21', content: '永德城区03网格执行渣土车抛洒处理应用' },
        { time: '2025-12-20 15:20', content: '永德城区03网格发生渣土车抛洒事件' },
        { time: '2025-12-20 15:11', content: '永德城区04网格执行渣土车抛洒处理应用' },
        { time: '2025-12-20 15:10', content: '永德城区04网格发生渣土车抛洒事件' }
      ]
    }
  },
  mounted() {
    this.handleData()
    // 等DOM稳定后再绘制，防止懒加载时偏移
    this.$nextTick(() => {
      setTimeout(() => {
        this.drawSvg()
      }, 200)
    })
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
    handleDetail(item) {
      console.log('查看详情:', item);
      // 这里可以添加弹窗逻辑或跳转逻辑
      this.$message.info(`正在查看：${item.content}`);
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

      // --- 修改：区分掉渣程度的配置 ---
      const alertConfig = {
        '永德城区02网格': { text: '跨区域渣土车抛洒事件'}, // 红色
        '永德城区03网格': { text: '渣土车抛洒事件'}, // 橙黄色
        '永德城区04网格': { text: '渣土车抛洒事件'}  // 橙黄色
      }

      const alertTargets = Object.keys(alertConfig)
      const alertData = this.polygons.filter(p => alertTargets.includes(p.name))

      const bubbleGroups = zoomG.selectAll('.bubble-group')
        .data(alertData)
        .enter()
        .append('g')
        .attr('class', 'bubble-group')
        .attr('transform', d => {
          const center = d3.polygonCentroid(d.coords)
          return `translate(${center[0]}, ${center[1] - 10})`
        })

      // 2. 绘制冒泡框背景 (颜色动态化)
      bubbleGroups.append('rect')
        .attr('x', -50)
        .attr('y', -45)
        .attr('width', 120)
        .attr('height', 25)
        .attr('rx', 12)
        .attr('fill', 'rgba(255, 77, 79, 0.9)')
        .attr('class', 'floating-bubble')

      // 3. 绘制冒泡框文字 (文字内容动态化)
      bubbleGroups.append('text')
        .attr('text-anchor', 'middle')
        .attr('x', 10)
        .attr('y', -28)
        .attr('fill', '#fff')
        .attr('font-size', 12)
        .attr('font-weight', 'bold')
        .attr('pointer-events', 'none')
        .text(d => alertConfig[d.name].text) // 根据配置获取文字
        .attr('class', 'floating-bubble')
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

/* --- 优化后的日志面板样式 --- */
.event-log-panel {
  position: absolute;
  right: 50px;  /* 靠右显示 */
  top: 100px;    /* 距离顶部距离 */
  
  /* 固定宽高 */
  width: 400px; 
  height: 500px; 
  
  z-index: 100;
  display: flex;
  flex-direction: column;

  /* 配色适配：深色半透明玻璃质感 */
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
      font-size: 14px; /* 缩小标题字体 */
      letter-spacing: 0.5px;
    }

    .device-status {
      font-size: 10px; /* 缩小状态字体 */
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

    /* 定制滚动条 */
    &::-webkit-scrollbar { width: 4px; }
    &::-webkit-scrollbar-thumb { background: rgba(0, 191, 255, 0.2); border-radius: 10px; }

    /* --- 修改后的日志项样式 --- */
    .log-item {
      padding: 10px;
      margin-bottom: 8px;
      background: rgba(255, 255, 255, 0.03);
      border-left: 3px solid #397a1e; 
      border-radius: 2px;
      transition: background 0.3s;

      &:hover {
        background: rgba(255, 255, 255, 0.07);
      }

      .log-content-wrapper {
        display: flex;
        justify-content: space-between;
        align-items: flex-end; // 按钮对齐到底部，或者 center 对齐居中
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
        flex-shrink: 0; // 防止按钮被压缩
        
        .detail-btn {
          font-size: 12px;
          color: #1890ff; // 科技蓝
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
