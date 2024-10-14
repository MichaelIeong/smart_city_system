<template>
  <div>
    <h2 class="title">历史条件选择</h2>
    <!-- <el-alert title="添加历史事件" type="success" :closable="false"> -->
    <!-- </el-alert> -->
    <!-- list 配置结果-->
    <div v-for="(t, i) in resultList" :key="i">
      <el-card style="" class="card-margin">
        <el-descriptions :title="'historyEvent ' + (i + 1)" :column="1" border>
          <template slot="extra">
            <el-button size="mini" icon="el-icon-edit" @click="openEditor(t, i)">
            </el-button>
            <el-button size="mini" icon="el-icon-delete" @click="removeItem(i)"></el-button>
          </template>
          <el-descriptions-item label="名称" :labelStyle="labelStyle">{{ t.name }}</el-descriptions-item>
          <el-descriptions-item label="位置" :labelStyle="labelStyle">
            <span v-for="(loc, index) in t.location" :key="index">
              {{ loc.locationPreposition + ' ' + loc.location + ',' }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="时区" :labelStyle="labelStyle">
            {{ (t.startTime ? `last ${t.startTime} ` : '') + t.startTimeUnit + ' ~ ' +
              (t.endTime ? `last ${t.endTime} `: '') + t.endTimeUnit }}
          </el-descriptions-item>
          <!-- <el-descriptions-item label="objectId">{{ t.objectId }}</el-descriptions-item> -->
        </el-descriptions>
      </el-card>
    </div>
    <!-- 添加按钮和配置对话框 -->
    <el-card style="display: flex;align-content: center;justify-content: center;" class="card-margin">
      <el-button type="text" @click="openEditor()">
        <i class="el-icon-circle-plus" style="font-size: 25px;">新增历史条件选择</i>
      </el-button>
      <el-dialog title="历史条件选择" :visible.sync="dialogEditorVisible" @close="cancelUpdateItem">
        <el-form :model="item_now" label-width="auto">
          <el-form-item label="名称">
            <el-select v-model="item_now.name" placeholder="名称">
              <el-option
                v-for="item in eventOptionList"
                :key="item.value"
                :label="item.label"
                :value="item.value"
                filterable>
              </el-option>
            </el-select>
          </el-form-item>

          <el-button type="primary" @click="addLocation" size="mini">新增位置</el-button>
          <el-form-item v-for="(loc, i) in item_now.location" label="位置" :key="i">
            <LocationInput :value="loc" :allowCurrentPosition="true"></LocationInput>
            <el-button @click.prevent="removeLocation(loc)" icon="el-icon-delete" size="mini"></el-button>
          </el-form-item>
          <el-form-item label="起始时间">
            <span v-if="item_now.startTimeUnit !== 'today' && item_now.startTimeUnit !== 'this week'">
              <span>过去</span>
              <el-input-number v-model="item_now.startTime" controls-position="right" :min="1">
              </el-input-number>
            </span>
            <el-select v-model="item_now.startTimeUnit" placeholder="unit" style="width: 120px">
              <el-option label="次数" value="times"></el-option>
              <el-option label="分钟" value="min"></el-option>
              <el-option label="今天" value="today"></el-option>
              <el-option label="这周" value="this week"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="结束时间">
            <span v-if="item_now.endTimeUnit !== 'trigger_timestamp'">
              <span>过去</span>
              <el-input-number v-model="item_now.endTime" controls-position="right" :min="1">
              </el-input-number>
            </span>
            <el-select v-model="item_now.endTimeUnit" placeholder="unit" style="width: 120px">
              <el-option label="分钟" value="min"></el-option>
              <el-option label="触发时" value="trigger_timestamp"></el-option>
            </el-select>
          </el-form-item>
          <!-- <el-form-item label="objectId">
            <el-select v-model="item_now.objectId" placeholder="unit" multiple filterable>
              <el-option v-for="obid in objectIdOptions" :label="obid.label" :value="obid.value" :key="obid.value">
              </el-option>
            </el-select>
          </el-form-item> -->
        </el-form>
        <div slot="footer">
          <el-button @click="cancelUpdateItem">取消</el-button>
          <el-button type="primary" @click="updateItem">确认</el-button>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>

import { locationOptions, objectIdOptions } from './data.js'
import LocationInput from './locationInput.vue'
import { getEvents } from '@/api/manage.js'

const defaultItem = {
  name: '',
  location: [],
  startTime: '',
  startTimeUnit: 'min',
  endTime: '',
  endTimeUnit: 'trigger_timestamp',
  objectId: []
}

export default {
  props: {
    value: {
      type: Array,
      default: () => []
    }
  },
  components: {
    LocationInput
  },
  data () {
    return {
      dialogEditorVisible: false,
      itemIndex: -1,
      item_now: JSON.parse(JSON.stringify(defaultItem)),
      resultList: [],
      objectIdOptions: objectIdOptions,
      locationOptions,
      // eventTypeNameOptions
      eventOptionList: [],
      labelStyle: { 'width': '80px' } // 表单样式 (标签列宽固定)
    }
  },
  watch: {
    'item_now.startTimeUnit': {
      handler (newValue, oldValue) {
        if (newValue === 'today' || newValue === 'this week') {
          this.item_now.startTime = ''
        }
      },
      immediate: true,
      deep: false
    },
    'item_now.endTimeUnit': {
      handler (newValue, oldValue) {
        if (newValue === 'trigger_timestamp') {
          this.item_now.endTime = ''
        }
      },
      immediate: true,
      deep: false
    }
  },
  mounted () {
    this.resultList = this.value
  },
  created () {
    this.getEventList()
  },
  methods: {
    test () { },
    getResult () {
      const finalResult = {
        event_list: [],
        location: [],
        time_zone: [],
        object_id: []
      }
      this.resultList.forEach(item => {
        finalResult.event_list.push(item.name)
        // finalResult.object_id.push(item.objectId)
        // 处理location
        finalResult.location.push(item.location.map((loc) => {
          if (loc.locationPreposition === 'is') {
            return loc.location
          } else {
            return `${loc.locationPreposition} ${loc.location}`
          }
        }))
        // 处理time_zone
        const type = item.startTimeUnit === 'times' ? 'times' : 'time'
        var startTime = ''
        if (item.startTimeUnit === 'today' || item.startTimeUnit === 'this week') {
          startTime = item.startTimeUnit
        } else {
          startTime = `last ${item.startTime} ${item.startTimeUnit}`
        }
        const endTime = item.endTimeUnit === 'trigger_timestamp' ? 'trigger_timestamp' : `last ${item.endTime} ${item.endTimeUnit}`
        finalResult.time_zone.push({
          type,
          start_time: startTime,
          end_time: endTime
        })
      })
      // console.log(finalResult)
      return finalResult
    },
    showResult (descreption) {
      if (!descreption) return
      const list = []
      descreption.event_list.forEach(eName => {
        const item = {
          name: '',
          location: [],
          startTime: '',
          startTimeUnit: 'min',
          endTime: '',
          endTimeUnit: 'trigger_timestamp'
        }
        item.name = eName
        list.push(item)
      })
      descreption.location.forEach((locArray, index) => {
        list[index].location = this.dealLocArray(locArray)
      })
      descreption.time_zone.forEach((time, index) => {
        this.dealTimeZone(time, list[index])
      })
      list.forEach(item => {
        this.resultList.push(item)
      })
    },
    dealLocArray (locations) {
      return locations.map(locationStr => {
        const [locationPreposition, ...locationParts] = locationStr.split(' ')
        return {
          locationPreposition: locationParts.length > 0 ? locationPreposition : 'is',
          location: locationParts.join(' ') || locationPreposition
        }
      })
    },
    dealTimeZone (timeZone, item) {
      item.type = timeZone.type === 'times' ? 'times' : 'time'

      // 解析 start_time
      let startTime, startTimeUnit
      if (timeZone.start_time === 'today' || timeZone.start_time === 'this week') {
        startTime = ''
        startTimeUnit = timeZone.start_time
      } else {
        const [, start, unit] = timeZone.start_time.match(/last (\d+) (.+)/)
        startTime = parseInt(start)
        startTimeUnit = unit
      }

      // 解析 end_time
      let endTime, endTimeUnit
      if (timeZone.end_time === 'trigger_timestamp') {
        endTime = ''
        endTimeUnit = 'trigger_timestamp'
      } else {
        const [, end, unit] = timeZone.end_time.match(/last (\d+) (.+)/)
        endTime = parseInt(end)
        endTimeUnit = unit
      }
      item.startTime = startTime
      item.startTimeUnit = startTimeUnit
      item.endTime = endTime
      item.endTimeUnit = endTimeUnit
    },
    getEventList () {
      const projectId = 1 // TODO: true projectId
      getEvents(projectId).then(res => {
        // console.log(res)
        this.eventOptionList = res.map(event => ({
          value: event.eventId,
          label: event.eventType
        }))
      })
    },
    refresh () {
      this.$forceUpdate()
      this.$emit('input', this.resultList)
    },
    openEditor (triggerInfo, index) {
      if (triggerInfo && index >= 0) {
        this.item_now = JSON.parse(JSON.stringify(triggerInfo))
        this.itemIndex = index
      }
      this.dialogEditorVisible = true
    },
    removeItem (index) {
      this.resultList.splice(index, 1)
      this.refresh()
    },
    updateItem () {
      if (this.itemIndex !== -1) {
        this.resultList[this.itemIndex] = this.item_now
      } else {
        this.resultList.push(this.item_now)
      }
      this.cancelUpdateItem()
    },
    cancelUpdateItem () {
      this.item_now = JSON.parse(JSON.stringify(defaultItem))
      this.itemIndex = -1
      this.dialogEditorVisible = false
      this.refresh()
    },
    addLocation () {
      this.item_now.location.push({ locationPreposition: '', location: '' })
    },
    removeLocation (loc) {
      this.item_now.location.splice(this.item_now.location.indexOf(loc), 1)
    }

  }
}
</script>

<style scoped>
.card-margin {
  margin: 10px 20px;
}

.title {
  margin-bottom: 10px;
}
</style>
