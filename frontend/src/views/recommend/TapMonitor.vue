<!--<template>-->
<!--  <page-header-wrapper>-->
<!--    <div class="mesh-card">-->
<!--      <div class="mesh-container">-->
<!--        <svg ref="svg" class="svg-container"></svg>-->
<!--      </div>-->
<!--    </div>-->
<!--  </page-header-wrapper>-->
<!--</template>-->

<!--<script>-->
<!--/* eslint-disable */-->
<!--import * as d3 from 'd3'-->
<!--import data from './F-city.json'-->

<!--export default {-->
<!--  name: 'TapMonitor',-->
<!--  data () {-->
<!--    return {-->
<!--      polygons: []-->
<!--    }-->
<!--  },-->
<!--  mounted() {-->
<!--    this.handleData()-->
<!--    // 等DOM稳定后再绘制，防止懒加载时偏移-->
<!--    this.$nextTick(() => {-->
<!--      setTimeout(() => {-->
<!--        this.drawSvg()-->
<!--      }, 200)-->
<!--    })-->
<!--  },-->
<!--  methods: {-->
<!--    handleData() {-->
<!--      const list = Array.isArray(data.data) ? data.data : []-->
<!--      this.polygons = list.map((item) => {-->
<!--        const info = item.meshInfo || {}-->
<!--        const gridList = info.meshGridList || []-->
<!--        return {-->
<!--          id: info.id,-->
<!--          code: info.meshCode,-->
<!--          name: info.meshName,-->
<!--          type: info.meshType,-->
<!--          coords: gridList.map(p => [Number(p.x), Number(p.y)])-->
<!--        }-->
<!--      })-->
<!--    },-->
<!--    drawSvg() {-->
<!--      const svgEl = d3.select(this.$refs.svg)-->
<!--      svgEl.selectAll('*').remove()-->

<!--      const mapWidth = 2080-->
<!--      const mapHeight = 1360-->

<!--      svgEl-->
<!--        .attr('preserveAspectRatio', 'xMidYMid meet')-->
<!--        .attr('viewBox', `0 0 ${mapWidth} ${mapHeight}`)-->

<!--      // 主缩放组-->
<!--      const zoomG = svgEl-->
<!--        .append('g')-->
<!--        .attr('class', 'zoom-group')-->

<!--      const scale = 1.3-->
<!--      const offsetX = -1870-->
<!--      const offsetY = -275-->

<!--      zoomG.attr('transform', `translate(${offsetX}, ${offsetY}) scale(${scale})`)-->

<!--      // 绘制网格图形-->
<!--      const groups = zoomG-->
<!--        .selectAll('g')-->
<!--        .data(this.polygons)-->
<!--        .enter()-->
<!--        .append('g')-->
<!--        .attr('class', 'polygon-group')-->

<!--      groups.append('polygon')-->
<!--        .attr('points', (d) => d.coords.map((p) => `${p[0]},${p[1]}`).join(' '))-->
<!--        .attr('fill', () => d3.schemeCategory10[Math.floor(Math.random() * 10)])-->
<!--        .attr('stroke', '#fff')-->
<!--        .attr('stroke-width', 1.5)-->
<!--        .attr('fill-opacity', 0.7)-->

<!--      groups.append('text')-->
<!--        .attr('x', (d) => d3.polygonCentroid(d.coords)[0])-->
<!--        .attr('y', (d) => d3.polygonCentroid(d.coords)[1])-->
<!--        .attr('text-anchor', 'middle')-->
<!--        .attr('dominant-baseline', 'middle')-->
<!--        .attr('fill', '#fff')-->
<!--        .attr('font-size', 14)-->
<!--        .attr('pointer-events', 'none')-->
<!--        .text((d) => d.name)-->
<!--    }-->
<!--  }-->
<!--}-->
<!--</script>-->

<!--<style lang="less" scoped>-->
<!--.mesh-card {-->
<!--  position: relative;-->
<!--  width: 100%;-->
<!--  height: calc(100vh - 200px);-->
<!--  border-radius: 8px;-->
<!--  overflow: hidden;-->

<!--  /* 背景图 */-->
<!--  background-image: url('@/assets/city2.png');-->
<!--  background-size: cover;-->
<!--  background-position: center;-->
<!--  background-repeat: no-repeat;-->
<!--}-->

<!--.mesh-container {-->
<!--  width: 100%;-->
<!--  height: 100%;-->
<!--}-->

<!--.svg-container {-->
<!--  width: 100%;-->
<!--  height: 100%;-->
<!--  display: block;-->
<!--}-->
<!--</style>-->
