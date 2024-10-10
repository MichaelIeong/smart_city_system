<template>
  <div>
    <h2 class="title">触发器</h2>

    <el-alert
      title="根据时间、地点和对象选择感知事件作为触发器。"
      type="success"
      :closable="false">
    </el-alert>

    <!-- list 配置结果-->
    <div v-for="(t, i) in resultList" :key="i">
      <el-card style="" class="card-margin">
        <el-descriptions :title="'Trigger ' + (i + 1)" :column="1" border>
          <template slot="extra">
            <el-button size="mini" icon="el-icon-edit" @click="openEditor(t, i)">
            </el-button>
            <el-button size="mini" icon="el-icon-delete" @click="removeItem(i)"></el-button>
          </template>
          <el-descriptions-item label="事件类型" :labelStyle="labelStyle">{{ t.event_type[0] }}</el-descriptions-item>
          <el-descriptions-item label="过滤器" :labelStyle="labelStyle">{{ t.filter }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
    <!-- 添加按钮和配置对话框 -->
    <el-card style="display: flex;align-content: center;justify-content: center;" class="card-margin">
      <el-button type="text" @click="openEditor()">
        <i class="el-icon-circle-plus" style="font-size: 25px;">新增触发器</i>
      </el-button>
      <el-dialog title="触发器" :visible.sync="dialogEditorVisible" @close="cancelUpdateItem">
        <el-form :model="item_now" label-width="auto">
          <el-form-item label="事件类型">
            <el-cascader
              v-model="item_now.event_type"
              :options="eventOptionList"
              filterable
              :props="{ expandTrigger: 'hover' }"
              placeholder="事件类型"></el-cascader>
          </el-form-item>
          <el-dropdown @command="handleCommand">
            <span>
              新增过滤器<i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="location">位置</el-dropdown-item>
              <el-dropdown-item command="timestamp">时间戳</el-dropdown-item>
              <!-- <el-dropdown-item command="objectId">objectId</el-dropdown-item> -->
              <!-- <el-dropdown-item command="eventData">eventData</el-dropdown-item> -->
            </el-dropdown-menu>
          </el-dropdown>
          <el-form-item label="位置" v-if="item_now.filterList.includes(&quot;location&quot;)">
            <LocationInput v-model="item_now.rawFilter.location" :allowCurrentPosition="true" />
          </el-form-item>
          <el-form-item label="时间戳" v-if="item_now.filterList.includes(&quot;timestamp&quot;)">
            <span>
              <el-select v-model="item_now.rawFilter.timestamp.comparator" placeholder="比较关系">
                <el-option v-for="item in comparatorOptions" :key="item.value" :label="item.label" :value="item.value">
                </el-option>
              </el-select>
              <el-time-picker v-model="item_now.rawFilter.timestamp.value" placeholder="时间" value-format="HH:mm:ss">
              </el-time-picker>
            </span>
          </el-form-item>
          <el-form-item label="objectId" v-if="item_now.filterList.includes(&quot;objectId&quot;)">
            <span>
              <el-select v-model="item_now.rawFilter.objectIdOperator" placeholder="objectIdOperator" filterable>
                <el-option
                  v-for="item in objectIdOperatorOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
              <el-select v-model="item_now.rawFilter.objectId" placeholder="objectId" filterable>
                <el-option v-for="item in objectIdOptions" :key="item.value" :label="item.label" :value="item.value">
                </el-option>
              </el-select>
            </span>
          </el-form-item>
          <el-form-item v-if="item_now.filterList.includes(&quot;eventData&quot;)">
            <span>
              <el-select v-model="item_now.rawFilter.eventData.key" placeholder="eventData" filterable>
                <el-option v-for="item in eventDataOptions" :key="item.value" :label="item.label" :value="item.value">
                </el-option>
              </el-select>
              <el-select v-model="item_now.rawFilter.eventData.comparator" placeholder="comparator" filterable>
                <el-option v-for="item in comparatorOptions" :key="item.value" :label="item.label" :value="item.value">
                </el-option>
              </el-select>
              <el-input v-model="item_now.rawFilter.eventData.value" placeholder="value"></el-input>
            </span>
          </el-form-item>
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

import { comparatorOptions, objectIdOptions, objectIdOperatorOptions, eventDataOptions } from './data.js'
import LocationInput from './locationInput.vue'
import { getEvents } from '@/api/manage.js'

const defaultItem = {
  event_type: '',
  filter: '',
  rawFilter: {
    location: { locationPreposition: '', location: '' },
    timestamp: { comparator: '', value: '' },
    objectId: '',
    objectIdOperator: '',
    eventData: { key: '', comparator: '', value: '' }

  },
  filterList: []
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
  watch: {
    'item_now.rawFilter': {
      handler (newValue, oldValue) {
        var result = ''
        if (this.item_now.filterList.includes('location')) {
          result += `location ${newValue.location.locationPreposition} ${newValue.location.location},`
        }
        if (this.item_now.filterList.includes('timestamp')) {
          result += `timestamp ${newValue.timestamp.comparator} ${newValue.timestamp.value},`
        }
        if (this.item_now.filterList.includes('objectId')) {
          result += `objectId ${newValue.objectIdOperator} ${newValue.objectId},`
        }
        if (this.item_now.filterList.includes('eventData')) {
          result += `eventData.${newValue.eventData.key} ${newValue.eventData.comparator} ${newValue.eventData.value},`
        }
        this.item_now.filter = result
      },
      immediate: true,
      deep: true
    }
  },
  data () {
    return {
      dialogEditorVisible: false,
      itemIndex: -1,
      item_now: JSON.parse(JSON.stringify(defaultItem)),
      resultList: [],
      comparatorOptions,
      eventOptionList: [],
      objectIdOptions,
      objectIdOperatorOptions,
      eventDataOptions,
      labelStyle: { 'width': '80px' } // 表单样式 (标签列宽固定)
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
      const finalResult = { event_type: [], filter: [] }
      this.resultList.forEach(item => {
        finalResult.event_type.push(item.event_type)

        const filterList = []
        if (item.filterList.includes('location')) {
          // const sufix = item.rawFilter.location.locationPreposition === 'is' ? '' : item.rawFilter.location.locationPreposition;
          const sufix = item.rawFilter.location.locationPreposition
          filterList.push(`location ${sufix} ${item.rawFilter.location.location}`)
        }
        if (item.filterList.includes('timestamp')) {
          filterList.push(`timestamp ${item.rawFilter.timestamp.comparator} ${item.rawFilter.timestamp.value}`)
        }
        // if (item.filterList.includes('objectId')) {
        //   filterList.push(`objectId ${item.rawFilter.objectIdOperator} ${item.rawFilter.objectId}`)
        // }
        if (item.filterList.includes('eventData')) {
          filterList.push(`event_data.${item.rawFilter.eventData.key} ${item.rawFilter.eventData.comparator} ${item.rawFilter.eventData.value},`)
        }
        finalResult.filter.push(filterList.join(','))
      })
      // console.log(finalResult)
      return finalResult
    },
    showResult (trigger) {
      if (!trigger) return
      const list = []
      trigger.event_type.forEach(eType => {
        const item = {
          event_type: '',
          filter: '',
          rawFilter: {},
          filterList: []
        }
        item.event_type = eType
        list.push(item)
      })
      trigger.filter.forEach((filterRule, index) => {
        this.dealFilterString(filterRule, list[index])
      })
      list.forEach(item => {
        this.resultList.push(item)
      })
    },
    dealFilterString (string, item) {
      item.filter = string
      const parts = string.split(',')
      parts.forEach(part => {
        part = part.trim()
        if (part.startsWith('location')) {
          item.filterList.push('location')
          const words = part.split(' ')
          item.rawFilter.location = {
            locationPreposition: words[1],
            location: words[2]
          }
        } else if (part.startsWith('timestamp')) {
          item.filterList.push('timestamp')
          const words = part.split(' ')
          item.rawFilter.timestamp = {
            comparator: words[1],
            value: words[2]
          }
        }
      })
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
    handleCommand (command) {
      if (!this.item_now.filterList.includes(command)) this.item_now.filterList.push(command)
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
