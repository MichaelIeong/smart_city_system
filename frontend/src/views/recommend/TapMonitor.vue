<template>
  <page-header-wrapper>
    <a-card
      :bordered="false"
      class="mesh-card"
      :style="{ borderRadius: '8px', height: 'calc(100vh - 250px)' }"
    >
      <div class="mesh-container">
        <svg ref="svg" class="svg-container"></svg>
        <button class="add-bubble-btn" @click="showBubble">显示冒泡</button>
      </div>
    </a-card>
  </page-header-wrapper>
</template>

<script>
/* eslint-disable */
import * as d3 from 'd3'
import meshData from './meshData.json'
import { message } from 'ant-design-vue'

export default {
  name: 'CenterPanel',
  data() {
    return { polygons: [] }
  },
  mounted() {
    this.handleData()
    this.drawSvg()
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

        // 点击事件
        groups.on('click', function (event, d) {
            // message.info(`网格ID：${d.id}`)
            // 获取网格中心点
            const [cx, cy] = d3.polygonCentroid(d.coords)

            // 添加冒泡分组（仍放在 bubbleLayer 里）
            const bubble = bubbleLayer.append('g').attr('class', 'bubble-label')

            const offsetX = 60 // 往右偏移
            const offsetY = -60 // 向上偏移

            // 背景框
            bubble
                .append('rect')
                .attr('x', cx - 60 + offsetX)
                .attr('y', cy + offsetY)
                .attr('rx', 6)
                .attr('ry', 6)
                .attr('width', 120)
                .attr('height', 40)
                .attr('fill', 'rgba(255,255,255,0.85)')
                .attr('stroke', '#000')
                .attr('stroke-width', 1.5)

            // 文字
            bubble
                .append('text')
                .attr('x', cx + offsetX)
                .attr('y', cy + offsetY + 25)
                .attr('text-anchor', 'middle')
                .attr('fill', '#000')
                .attr('font-size', 16)
                .attr('font-weight', 'bold')
                .text(`机动车违章停车`)

            // 自动移除
            // setTimeout(() => bubble.remove(), 10000)
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

    // 新增：封装添加冒泡的函数
    addBubble(d, bubbleLayer) {
      const eventType = 'illegalParking'

      // 获取网格中心点
      const [cx, cy] = d3.polygonCentroid(d.coords)

      // 添加冒泡分组（仍放在 bubbleLayer 里）
      const bubble = bubbleLayer.append('g')
        .attr('class', 'bubble-label')
        .attr('eventType', eventType)

      const offsetX = 60 // 往右偏移
      const offsetY = -60 // 向上偏移

      // 背景框
      bubble
        .append('rect')
        .attr('x', cx - 60 + offsetX)
        .attr('y', cy + offsetY)
        .attr('rx', 6)
        .attr('ry', 6)
        .attr('width', 120)
        .attr('height', 40)
        .attr('fill', 'rgba(255,255,255,0.85)')
        .attr('stroke', '#000')
        .attr('stroke-width', 1.5)

      // 文字
      bubble
        .append('text')
        .attr('x', cx + offsetX)
        .attr('y', cy + offsetY + 25)
        .attr('text-anchor', 'middle')
        .attr('fill', '#000')
        .attr('font-size', 16)
        .attr('font-weight', 'bold')
        .text('机动车违章停车')

      // 新增：为冒泡添加点击事件（例如，点击移除冒泡）
      bubble.on('click', () => {
        message.info(`事件类型: ${eventType}, 网格ID: ${d.id}`)
      });

      // 自动移除（可选，如果需要）
      // setTimeout(() => bubble.remove(), 10000)
    },

    // 新增：按钮点击方法
    showBubble() {
      // 假设为第一个网格添加冒泡（可根据需求调整）
      if (this.polygons.length > 0) {
        const d = this.polygons[0]  // 示例：选择第一个网格
        console.log(this.polygons)
        const bubbleLayer = d3.select(this.$refs.svg).select('.bubble-layer')
        this.addBubble(d, bubbleLayer)
      } else {
        message.warning('没有可用的网格数据')
      }
    }
  }
}
</script>

<style lang="less" scoped>
/* ✅ 在 mesh-card 上添加背景图 */
.mesh-card {
  position: relative;
  box-shadow: none !important;
  padding: 0;
  background-image: url('@/assets/screen_bg.png'); /* <-- 替换成你的图片路径 */
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
  justify-content: flex-start; /* 靠左显示 */
  align-items: center;
}

/* ✅ SVG 在背景图上层 */
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
</style>
