<template>
  <div>
    <h2 class="title">Action</h2>
    <el-alert title="Execute the corresponding action according to the current and previous conditions." type="success"
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
          <el-descriptions-item label="action_name">{{ t.action.action_name }}
          </el-descriptions-item>
          <el-descriptions-item label="conditions">
            <span v-for="(c, i) in t.history_condition" :key="'hc' + i">
              {{ `${i === 0 ? 'where ' : c.logicalOperator} ${c.name} ${c.comparator} ${c.value}` }}
            </span>
            <span v-for="(c, i) in t.current_condition" :key="'cc' + i">
              {{ `${i === 0 ? 'where ' : c.logicalOperator}
              ${c.location.locationPreposition}_${c.location.location}.${c.property.join('.')} ${c.comparator}
              ${c.value}`
              }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="location">
            <span v-for="(loc, i) in t.action.action_location" :key="i">
              {{ `${loc.locationPreposition} ${loc.location},` }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="action_param">{{ t.action.action_param }}
          </el-descriptions-item>
        </el-descriptions>

      </el-card>
    </div>
    <!-- 添加按钮和配置对话框 -->
    <el-card style="display: flex;align-content: center;justify-content: center;" class="card-margin">
      <el-button type="text" @click="openEditor()">
        <i class="el-icon-circle-plus" style="font-size: 25px;"> Add Action</i>
      </el-button>
      <el-dialog title="Action" :visible.sync="dialogEditorVisible" @close="cancelUpdateItem">
        <el-form :model="item_now" label-width="auto">

          <div>
            Conditional judgement
          </div>
          <!-- history condition -->
          <el-button type="primary" size="mini" @click="addHistoryCondition">Add History Condition</el-button>
          <el-form-item v-for="(c, i) in item_now.history_condition" label="history_condition" :key="'hc' + i">
            <span v-if="i === 0">Where </span>
            <el-select v-if="i > 0" v-model="c.logicalOperator" filterable style="width: 60px;">
              <el-option v-for="item in logicalOperatorOptions" :key="item.value" :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
            <el-select v-model="c.name" filterable placeholder="func">
              <el-option v-for="item in computedResultList" :key="item.result_name" :label="item.result_name"
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
          <el-button type="primary" size="mini" @click="addCurrentCondition">Add Current Condition</el-button>
          <el-form-item label="current_condition" v-for="(c, i) in item_now.current_condition" :key="'cc' + i">
            <span v-if="i === 0">Where </span>
            <el-select v-if="i > 0" v-model="c.logicalOperator" filterable placeholder="Operator" style="width: 60px;">
              <el-option v-for="item in logicalOperatorOptions" :key="item.value" :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
            <LocationInput v-model="c.location" />

            <el-cascader v-model="c.property" :options="LocationPropertyOptions"
              :props="{ expandTrigger: 'hover' }" clearable filterable placeholder="property" separator=".">
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
            Action selection
          </div>

          <el-form-item label="action_name">
            <el-select v-model="item_now.action.action_name" placeholder="">
              <el-option v-for="(ac, i) in actionOptions" :key="i" :label="ac.label" :value="ac.value"></el-option>
            </el-select>
          </el-form-item>

          <el-button type="primary" size="mini" @click="addLocation">Add Action Location</el-button>
          <el-form-item v-for="(loc, i) in item_now.action.action_location" label="action_location" :key="'loc' + i">
            <LocationInput :value="loc"></LocationInput>
            <el-button @click.prevent="removeLocation(loc)" icon="el-icon-delete" type="text"></el-button>
          </el-form-item>


          <el-form-item label="action_param">
            <el-select v-model="item_now.action.action_param" multiple filterable allow-create default-first-option
              placeholder="">
              <el-option v-for="item in computedResultList" :key="item.result_name" :label="item.result_name"
                :value="item.result_name">
              </el-option>
            </el-select>
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
import { eventTypeNameOptions, locationOptions, actionOptions, comparatorOptions, logicalOperatorOptions, LocationPropertyOptions } from '../data.js'
import LocationInput from './LocationInput.vue'
import bus from '../bus';


const defaultItem = {
  current_condition: [],
  history_condition: [],
  action: {
    action_name: '',
    action_location: [],
    action_param: []
  }
}

export default {
  props: ['value', 'computedResultList'],
  components: {
    LocationInput,
    LocationInput
  },
  data() {
    return {
      dialogEditorVisible: false,
      itemIndex: -1,
      item_now: JSON.parse(JSON.stringify(defaultItem)),
      resultList: [],
      actionLocationOptions: [],
      historyLength: 0,

      eventTypeNameOptions,
      locationOptions,
      actionOptions,
      comparatorOptions,
      logicalOperatorOptions,
      LocationPropertyOptions,
    }
  },
  mounted() {
    this.resultList = this.value;
    this.actionLocationOptions.push({ value: 'trigger_location', label: 'trigger_location' });
    this.actionLocationOptions.push(...this.locationOptions);
  },
  watch: {
    "historyLength": {
      handler: (val, oldVal) => {
        if (val === 0) {
          bus.$emit("hasHistory", false);
        } else if (val === 1 && oldVal === 0) {
          bus.$emit("hasHistory", true);
        }
        // console.log('history_condition.length: ', val)
      },
    }
  },
  methods: {
    test() { },
    getResult() {
      const finalResult = [];
      this.resultList.forEach(item => {
        const action_location = []
        item.action.action_location.forEach(loc => {
          if (loc.locationPreposition === 'is') {
            action_location.push(loc.location);
          } else {
            action_location.push(loc.locationPreposition + ' ' + loc.location);
          }
        })
        var history_condition = "";
        item.history_condition.forEach((hc, i) => {
          if (i > 0) {
            const operator = hc.logicalOperator === 'and' ? '&' : '|';
            history_condition += operator;
          }
          history_condition += hc.name + hc.comparator + hc.value;
        })
        var current_condition = "";
        item.current_condition.forEach((cc, i) => {
          if (i > 0) {
            const operator = cc.logicalOperator === 'and' ? '&' : '|';
            current_condition += operator;
          }
          const location = cc.location.locationPreposition === 'is' ? cc.location.location : cc.location.locationPreposition + '_' + cc.location.location;
          current_condition += location + '.' + cc.property.join('.') + cc.comparator + cc.value;
        })
        const action = {
          action_name: item.action.action_name,
          action_location,
          action_param: item.action.action_param.join(",")
        };
        finalResult.push({
          history_condition,
          current_condition,
          action
        })
      })
      // console.log(finalResult);
      return finalResult;
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
      this.updateHistoryLength();
      this.refresh()
    },
    updateItem() {
      if (this.itemIndex !== -1) {
        this.resultList[this.itemIndex] = this.item_now;
      } else {
        this.resultList.push(this.item_now);
      }
      this.updateHistoryLength();
      this.cancelUpdateItem();
    },
    cancelUpdateItem() {
      this.item_now = JSON.parse(JSON.stringify(defaultItem))
      this.itemIndex = -1
      this.dialogEditorVisible = false;
      this.refresh();
    },

    updateHistoryLength() {
      let historyLength = 0;
      this.resultList.forEach((item) => {
        historyLength += item.history_condition.length
      })
      this.historyLength = historyLength;
    },

    addHistoryCondition() {
      const newCondition = {
        logicalOperator: 'or',
        name: '',
        comparator: '',
        value: ''
      }
      this.item_now.history_condition.push(newCondition)
    },
    removeHistoryCondition(condition) {
      this.item_now.history_condition.splice(this.item_now.history_condition.indexOf(condition), 1)
    },

    addCurrentCondition() {
      const newCondition = {
        logicalOperator: 'or',
        location: { locationPreposition: '', location: '' },
        property: [],
        comparator: '',
        value: ''
      }
      this.item_now.current_condition.push(newCondition)
    },
    removeCurrentCondition(condition) {
      this.item_now.current_condition.splice(this.item_now.current_condition.indexOf(condition), 1)
    },

    addLocation() {
      this.item_now.action.action_location.push({ locationPreposition: '', location: '' })
    },
    removeLocation(loc) {
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