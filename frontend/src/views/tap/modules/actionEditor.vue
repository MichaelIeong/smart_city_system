<template>
  <div>
    <h2 class="title">动作</h2>
    <el-alert
      title="根据当前和之前的条件执行相应的动作。"
      type="success"
      :closable="false">
    </el-alert>

    <!-- list 配置结果-->
    <div v-for="(t, i) in resultList" :key="i">
      <el-card style="" class="card-margin">
        <el-descriptions :title="'Action ' + (i + 1)" :column="1" border>
          <template slot="extra">
            <el-button size="mini" icon="el-icon-edit" @click="openEditor(t, i)">
            </el-button>
            <el-button size="mini" icon="el-icon-delete" @click="removeItem(i)"></el-button>
          </template>
          <el-descriptions-item label="服务名称" :labelStyle="labelStyle">{{ t.action.action_name }}
          </el-descriptions-item>
          <el-descriptions-item label="条件判断" :labelStyle="labelStyle">
            <span v-if="t.history_condition.length > 0">history:</span>
            <span v-for="(c, index) in t.history_condition" :key="'hc' + index">
              {{ `${index === 0 ? 'where ' : c.logicalOperator} ${c.name} ${c.comparator} ${c.value}` }}
            </span>
            <span v-if="t.current_condition.length > 0">current:</span>
            <span v-for="(c, index) in t.current_condition" :key="'cc' + index">
              {{ `${index === 0 ? 'where ' : c.logicalOperator}
              ${c.location.locationPreposition}_${c.location.location}.${c.property.join('.')} ${c.comparator}
              ${c.value}`
              }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="动作位置" :labelStyle="labelStyle">
            <span v-for="(loc, index) in t.action.action_location" :key="index">
              {{ `${loc.locationPreposition} ${loc.location},` }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="动作参数" :labelStyle="labelStyle">{{ t.action.action_param }}
          </el-descriptions-item>
        </el-descriptions>

      </el-card>
    </div>
    <!-- 添加按钮和配置对话框 -->
    <el-card style="display: flex;align-content: center;justify-content: center;" class="card-margin">
      <el-button type="text" @click="openEditor()">
        <i class="el-icon-circle-plus" style="font-size: 25px;">新增动作</i>
      </el-button>
      <el-dialog title="动作" :visible.sync="dialogEditorVisible" @close="cancelUpdateItem">
        <el-form :model="item_now" label-width="auto">

          <div>
            条件判断
          </div>
          <!-- history condition -->
          <el-button type="primary" size="mini" @click="addHistoryCondition">新增历史条件</el-button>
          <el-form-item v-for="(c, i) in item_now.history_condition" label="历史条件" :key="'hc' + i">
            <span v-if="i === 0">Where </span>
            <el-select v-if="i > 0" v-model="c.logicalOperator" filterable style="width: 60px;">
              <el-option
                v-for="item in logicalOperatorOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
            <el-select v-model="c.name" filterable placeholder="func">
              <el-option
                v-for="item in computedResultList"
                :key="item.result_name"
                :label="item.result_name"
                :value="item.result_name">
              </el-option>
            </el-select>
            <el-select v-model="c.comparator" placeholder="comparator" style="width: 60px;">
              <el-option v-for="item in comparatorOptions" :key="item.value" :label="item.label" :value="item.value">
              </el-option>
            </el-select>
            <el-input v-model="c.value" placeholder="expect value"></el-input>
            <el-button @click.prevent="removeHistoryCondition(c)" icon="el-icon-delete" type="text"></el-button>
          </el-form-item>

          <!-- current condition -->
          <el-button type="primary" size="mini" @click="addCurrentCondition">新增当前条件</el-button>
          <el-form-item label="当前条件" v-for="(c, i) in item_now.current_condition" :key="'cc' + i">
            <span v-if="i === 0">Where </span>
            <el-select v-if="i > 0" v-model="c.logicalOperator" filterable placeholder="Operator" style="width: 60px;">
              <el-option
                v-for="item in logicalOperatorOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
            <LocationInput v-model="c.location" :allowCurrentPosition="true"/>

            <el-cascader
              v-model="c.property"
              :options="propertyOptionList"
              :props="{ expandTrigger: 'hover' }"
              clearable
              filterable
              placeholder="property"
              separator=".">
            </el-cascader>
            <el-select v-model="c.comparator" placeholder="comparator" style="width: 60px;">
              <el-option v-for="item in comparatorOptions" :key="item.value" :label="item.label" :value="item.value">
              </el-option>
            </el-select>
            <el-input v-model="c.value" placeholder="expect value"></el-input>
            <el-button @click.prevent="removeCurrentCondition(c)" icon="el-icon-delete" type="text"></el-button>
          </el-form-item>

          <el-divider></el-divider>
          <div>
            动作选择
          </div>

          <el-form-item label="服务名称">
            <el-select v-model="item_now.action.action_name" placeholder="">
              <el-option v-for="(ac, i) in serviceOptionList" :key="i" :label="ac.label" :value="ac.value"></el-option>
            </el-select>
          </el-form-item>

          <el-button type="primary" size="mini" @click="addLocation">新增动作位置</el-button>
          <el-form-item v-for="(loc, i) in item_now.action.action_location" label="动作位置" :key="'loc' + i">
            <LocationInput :value="loc" :allowCurrentPosition="true"></LocationInput>
            <el-button @click.prevent="removeLocation(loc)" icon="el-icon-delete" type="text"></el-button>
          </el-form-item>

          <!-- <el-form-item label="动作参数">
            <el-select
              v-model="item_now.action.action_param"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="">
              <el-option
                v-for="item in computedResultList"
                :key="item.result_name"
                :label="item.result_name"
                :value="item.result_name">
              </el-option>
            </el-select>
          </el-form-item> -->
          <el-form-item label="动作参数">
            <el-input
              v-model="item_now.action.action_param"
              placeholder="请输入动作参数">
            </el-input>
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
import { eventTypeNameOptions, locationOptions, comparatorOptions, logicalOperatorOptions } from './data.js'
import LocationInput from './locationInput.vue'
import bus from './bus'
import { getProperties, getServices } from '@/api/manage.js'

const defaultItem = {
  current_condition: [],
  history_condition: [],
  action: {
    action_name: '',
    action_location: [],
    action_param: ''
  }
}

export default {
  props: {
    value: {
      type: Array,
      default: () => []
    },
    computedResultList: {
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
      actionLocationOptions: [],
      historyLength: 0,

      eventTypeNameOptions,
      locationOptions,
      // actionOptions,
      comparatorOptions,
      logicalOperatorOptions,
      // LocationPropertyOptions
      propertyOptionList: [],
      serviceOptionList: [],
      labelStyle: { 'width': '80px' } // 表单样式 (标签列宽固定)
    }
  },
  mounted () {
    this.resultList = this.value
    this.actionLocationOptions.push({ value: 'trigger_location', label: 'trigger_location' })
    this.actionLocationOptions.push(...this.locationOptions)
  },
  created () {
    this.getPropertyList()
    this.getServiceList()
  },
  watch: {
    'historyLength': {
      handler: (val, oldVal) => {
        if (val === 0) {
          bus.$emit('hasHistory', false)
        } else if (val === 1 && oldVal === 0) {
          bus.$emit('hasHistory', true)
        }
        // console.log('history_condition.length: ', val)
      }
    }
  },
  methods: {
    test () { },
    getResult () {
      const finalResult = []
      this.resultList.forEach(item => {
        const actionLocation = []
        item.action.action_location.forEach(loc => {
          if (loc.locationPreposition === 'is') {
            actionLocation.push(loc.location)
          } else {
            actionLocation.push(loc.locationPreposition + ' ' + loc.location)
          }
        })
        var historyCondition = ''
        item.history_condition.forEach((hc, i) => {
          if (i > 0) {
            const operator = hc.logicalOperator === 'and' ? '&' : '|'
            historyCondition += operator
          }
          historyCondition += hc.name + hc.comparator + hc.value
        })
        var currentCondition = ''
        item.current_condition.forEach((cc, i) => {
          if (i > 0) {
            const operator = cc.logicalOperator === 'and' ? '&' : '|'
            currentCondition += operator
          }
          const location = cc.location.locationPreposition === 'is' ? cc.location.location : cc.location.locationPreposition + '_' + cc.location.location
          currentCondition += location + '.' + cc.property.join('.') + cc.comparator + cc.value
        })
        const action = {
          action_name: item.action.action_name,
          action_location: actionLocation,
          action_param: item.action.action_param
        }
        finalResult.push({
          history_condition: historyCondition,
          current_condition: currentCondition,
          action
        })
      })
      // console.log(finalResult);
      return finalResult
    },
    showResult (action) {
      if (!action) return
      const list = []
      action.forEach(aItem => {
        const item = {}
        item.action = this.dealAction(aItem.action)
        item.history_condition = this.dealHistoryConditionString(aItem.history_condition)
        item.current_condition = this.dealCurrentConditionString(aItem.current_condition)
        list.push(item)
        console.log(item)
      })
      list.forEach(item => {
        this.resultList.push(item)
      })
    },
    dealAction (action) {
      const result = {}
      result.action_name = action.action_name
      result.action_param = action.action_param
      result.action_location = []
      action.action_location.forEach(loc => {
        const [locationPreposition, ...locationParts] = loc.split(' ')
        const location = {
          locationPreposition: locationPreposition === 'is' ? 'is' : locationPreposition,
          location: locationParts.join(' ')
        }
        result.action_location.push(location)
      })
      return result
    },
    dealHistoryConditionString (string) {
      const result = []
      const conditions = string.split(/([|&])/)
      let logicalOperator = null
      conditions.forEach(part => {
        if (part === '&') {
          logicalOperator = 'and'
        } else if (part === '|') {
          logicalOperator = 'or'
        } else {
          // 匹配 name、comparator 和 value
          const match = part.match(/^(\w+)([><=])(\w+)$/)
          if (match) {
            const [, name, comparator, value] = match
            result.push({
              name,
              comparator,
              value,
              logicalOperator: logicalOperator || 'or' // 默认用 'or'
            })
            logicalOperator = null // 重置逻辑符号
          }
        }
      })
      return result
    },
    dealCurrentConditionString (string) {
      const result = []
      const conditions = string.split(/([|&])/)
      let logicalOperator = null
      conditions.forEach(part => {
        if (part === '&') {
          logicalOperator = 'and'
        } else if (part === '|') {
          logicalOperator = 'or'
        } else {
          // 匹配 location、property、comparator 和 value
          const match = part.match(/^([\w_]+)\.(.+?)([><=])(\w+)$/)
          if (match) {
            const [, locationPart, propertyStr, comparator, value] = match
            // 解析 locationPart
            const [locationPreposition, location] = locationPart.includes('_')
            ? [locationPart.split('_', 1)[0], locationPart.split('_').slice(1).join('_')]
            : ['is', locationPart]
            const property = propertyStr.split('.')
            result.push({
              location: {
                locationPreposition,
                location
              },
              property,
              comparator,
              value,
              logicalOperator: logicalOperator || 'or' // 默认用 'or'
            })
            logicalOperator = null // 重置逻辑符号
          }
        }
      })
      return result
    },
    getPropertyList () {
      const projectId = 1 // TODO: true projectId
      getProperties(projectId).then(res => {
        this.propertyOptionList = res.map(property => ({
          value: property.propertyId,
          label: property.propertyKey
        }))
      })
    },
    getServiceList () {
      const projectId = 1 // TODO: true projectId
      getServices(projectId).then(res => {
        this.serviceOptionList = res.map(service => ({
          value: service.serviceId,
          label: service.serviceName
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
      this.updateHistoryLength()
      this.refresh()
    },
    updateItem () {
      if (this.itemIndex !== -1) {
        this.resultList[this.itemIndex] = this.item_now
      } else {
        this.resultList.push(this.item_now)
      }
      this.updateHistoryLength()
      this.cancelUpdateItem()
    },
    cancelUpdateItem () {
      this.item_now = JSON.parse(JSON.stringify(defaultItem))
      this.itemIndex = -1
      this.dialogEditorVisible = false
      this.refresh()
    },

    updateHistoryLength () {
      let historyLength = 0
      this.resultList.forEach((item) => {
        historyLength += item.history_condition.length
      })
      this.historyLength = historyLength
    },

    addHistoryCondition () {
      const newCondition = {
        logicalOperator: 'or',
        name: '',
        comparator: '',
        value: ''
      }
      this.item_now.history_condition.push(newCondition)
    },
    removeHistoryCondition (condition) {
      this.item_now.history_condition.splice(this.item_now.history_condition.indexOf(condition), 1)
    },

    addCurrentCondition () {
      const newCondition = {
        logicalOperator: 'or',
        location: { locationPreposition: '', location: '' },
        property: [],
        comparator: '',
        value: ''
      }
      this.item_now.current_condition.push(newCondition)
    },
    removeCurrentCondition (condition) {
      this.item_now.current_condition.splice(this.item_now.current_condition.indexOf(condition), 1)
    },

    addLocation () {
      this.item_now.action.action_location.push({ locationPreposition: '', location: '' })
    },
    removeLocation (loc) {
      this.item_now.action.action_location.splice(this.item_now.action.action_location.indexOf(loc), 1)
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
