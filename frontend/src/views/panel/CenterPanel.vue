<template>
  <page-header-wrapper>
    <a-card
      :bordered="false"
      class="mesh-card"
      :style="{ borderRadius: '8px', height: 'calc(100vh - 250px)' }"
    >
      <div class="mesh-container">
        <svg ref="svg" class="svg-container"></svg>
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

      const zoomG = svgEl
        .append('g')
        .attr('class', 'zoom-group')
        .attr('transform', 'translate(-1200, 0) scale(0.95)')

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
        // ✅ 点击事件（显示网格ID）
        .on('click', function (event, d) {
          message.info(`网格ID：${d.id}`)
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
