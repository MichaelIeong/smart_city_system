<template>
  <span>
    <el-select v-model="value.locationPreposition" placeholder="匹配条件" style="width: 125px;">
      <el-option v-for="item in locationPrepositionOptions" :key="item.value" :label="item.label" :value="item.value">
      </el-option>
    </el-select>
    <el-select v-model="value.location" placeholder="地点">
      <el-option v-for="item in locationOptionList" :key="item.value" :label="item.label" :value="item.value">
      </el-option>
    </el-select>
  </span>
</template>

<script>

import { locationPrepositionOptions } from './data.js'
import { getSpaces } from '@/api/manage.js'

export default {
  props: {
    value: {
      type: Object,
      default: () => ({
        locationPreposition: '',
        location: ''
      })
    },
    allowCurrentPosition: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      // locationOptions,
      locationOptionList: [],
      locationPrepositionOptions
    }
  },
  mounted () {
  },
  created () {
    this.getLocationList(this.allowCurrentPosition)
  },
  watch: {},
  methods: {
    refresh () {
      this.$forceUpdate()
      this.$emit('input', this.value)
    },
    getResult () {
      return this.value.locationPreposition + ' ' + this.value.location
    },
    getLocationList (allowCurrentPosition) {
      const projectId = 1 // TODO: true projectId
      const options = []
      getSpaces(projectId).then(res => {
        if (allowCurrentPosition) {
          options.push({
            value: 'trigger_location', // TODO: id? 命名?
            label: '事件触发位置'
          })
        }
        // console.log(res)
        res.forEach(space => {
          options.push({
            value: space.spaceId,
            label: space.spaceName
          })
        })
        this.locationOptionList = options
      })
    }
  }
}
</script>
