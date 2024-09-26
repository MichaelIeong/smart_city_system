<template>
  <span>
    <el-select v-model="value.locationPreposition" placeholder="prefix" style="width: 125px;">
      <el-option v-for="item in locationPrepositionOptions" :key="item.value" :label="item.label" :value="item.value">
      </el-option>
    </el-select>
    <el-select v-model="value.location" placeholder="location">
      <el-option v-for="item in locationOptions" :key="item.value" :label="item.label" :value="item.value">
      </el-option>
    </el-select>
  </span>
</template>

<script>

import { locationOptions, locationPrepositionOptions } from './data.js'

export default {
  props: {
    value: {
      type: Object,
      default: () => ({
        locationPreposition: '',
        location: ''
      })
    }
  },
  data () {
    return {
      locationOptions,
      locationPrepositionOptions
    }
  },
  mounted () {
    // this.value = this.value || { locationPreposition: '', location: '' }
  },
  watch: {},
  methods: {
    refresh () {
      this.$forceUpdate()
      this.$emit('input', this.value)
    },
    getResult () {
      return this.value.locationPreposition + ' ' + this.value.location
    }
  }
}
</script>
