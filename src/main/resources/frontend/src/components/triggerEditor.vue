<template>
  <div>
    <h2 class="title">Trigger</h2>

    <el-alert title="Select perception events as triggers according to time, location and object." type="success"
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
          <el-descriptions-item label="event_type">{{ t.event_type[0] }}</el-descriptions-item>
          <el-descriptions-item label="filter">{{ t.filter }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
    <!-- 添加按钮和配置对话框 -->
    <el-card style="display: flex;align-content: center;justify-content: center;" class="card-margin">
      <el-button type="text" @click="openEditor()">
        <i class="el-icon-circle-plus" style="font-size: 25px;"> Add Trigger</i>
      </el-button>
      <el-dialog title="Trigger" :visible.sync="dialogEditorVisible" @close="cancelUpdateItem">
        <el-form :model="item_now" label-width="auto">
          <el-form-item label="event_type">
            <el-cascader v-model="item_now.event_type" :options="eventTypeNameOptions" filterable
              :props="{ expandTrigger: 'hover' }" @change="" placeholder="event type"></el-cascader>
          </el-form-item>
          <el-dropdown @command="handleCommand">
            <span>
              add filter<i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="location">location</el-dropdown-item>
              <el-dropdown-item command="timestamp">timestamp</el-dropdown-item>
              <el-dropdown-item command="objectId">objectId</el-dropdown-item>
              <el-dropdown-item command="eventData">eventData</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <el-form-item label="location" v-if='item_now.filterList.includes("location")'>
            <LocationInput v-model="item_now.rawFilter.location" />
          </el-form-item>
          <el-form-item label="timestamp" v-if='item_now.filterList.includes("timestamp")'>
            <span>
              <el-select v-model="item_now.rawFilter.timestamp.comparator" placeholder="comparator">
                <el-option v-for="item in comparatorOptions" :key="item.value" :label="item.label" :value="item.value">
                </el-option>
              </el-select>
              <el-time-picker v-model="item_now.rawFilter.timestamp.value" placeholder="time" value-format="HH:mm:ss">
              </el-time-picker>
            </span>
          </el-form-item>
          <el-form-item label="objectId" v-if='item_now.filterList.includes("objectId")'>
            <span>
              <el-select v-model="item_now.rawFilter.objectIdOperator" placeholder="objectIdOperator" filterable>
                <el-option v-for="item in objectIdOperatorOptions" :key="item.value" :label="item.label"
                  :value="item.value">
                </el-option>
              </el-select>
              <el-select v-model="item_now.rawFilter.objectId" placeholder="objectId" filterable>
                <el-option v-for="item in objectIdOptions" :key="item.value" :label="item.label" :value="item.value">
                </el-option>
              </el-select>
            </span>
          </el-form-item>
          <el-form-item v-if='item_now.filterList.includes("eventData")'>
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
          <el-button @click="cancelUpdateItem">Cancel</el-button>
          <el-button type="primary" @click="updateItem">Confirm</el-button>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>


<script>

import { eventTypeNameOptions, comparatorOptions, objectIdOptions, objectIdOperatorOptions, eventDataOptions } from './data.js'
import LocationInput from './LocationInput.vue'


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
  filterList: [],
}

export default {
  props: ['value'],
  components: {
    LocationInput
  },
  watch: {
    'item_now.rawFilter': {
      handler(newValue, oldValue) {
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
  data() {
    return {
      dialogEditorVisible: false,
      itemIndex: -1,
      item_now: JSON.parse(JSON.stringify(defaultItem)),
      resultList: [],
      comparatorOptions,
      eventTypeNameOptions,
      objectIdOptions,
      objectIdOperatorOptions,
      eventDataOptions
    }
  },
  mounted() {
    this.resultList = this.value;
  },
  methods: {
    test() { },
    getResult() {
      const finalResult = { event_type: [], filter: [] }
      this.resultList.forEach(item => {

        finalResult.event_type.push(item.event_type)

        const filterList = []
        if (item.filterList.includes('location')) {
          // const sufix = item.rawFilter.location.locationPreposition === 'is' ? '' : item.rawFilter.location.locationPreposition;
          const sufix = item.rawFilter.location.locationPreposition;
          filterList.push(`location ${sufix} ${item.rawFilter.location.location}`)
        }
        if (item.filterList.includes('timestamp')) {
          filterList.push(`timestamp ${item.rawFilter.timestamp.comparator} ${item.rawFilter.timestamp.value}`)
        }
        if (item.filterList.includes('objectId')) {
          filterList.push(`objectId ${item.rawFilter.objectIdOperator} ${item.rawFilter.objectId}`)
        }
        if (item.filterList.includes('eventData')) {
          filterList.push(`event_data.${item.rawFilter.eventData.key} ${item.rawFilter.eventData.comparator} ${item.rawFilter.eventData.value},`)
        }
        finalResult.filter.push(filterList.join(','))
      })
      // console.log(finalResult)
      return finalResult;
    },
    handleCommand(command) {
      if (!this.item_now.filterList.includes(command)) this.item_now.filterList.push(command)
    },
    refresh() {
      this.$forceUpdate();
      this.$emit('input', this.resultList);
    },
    openEditor(triggerInfo, index) {
      if (triggerInfo && index >= 0) {
        this.item_now = JSON.parse(JSON.stringify(triggerInfo))
        this.itemIndex = index
      }
      this.dialogEditorVisible = true;
    },
    removeItem(index) {
      this.resultList.splice(index, 1)
      this.refresh()
    },
    updateItem() {
      if (this.itemIndex !== -1) {
        this.resultList[this.itemIndex] = this.item_now;
      } else {
        this.resultList.push(this.item_now);
      }
      this.cancelUpdateItem();
    },
    cancelUpdateItem() {
      this.item_now = JSON.parse(JSON.stringify(defaultItem))
      this.itemIndex = -1
      this.dialogEditorVisible = false;
      this.refresh();
    },
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