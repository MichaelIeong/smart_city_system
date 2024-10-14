<template>
  <div>
    <h2 class="title">历史条件计算</h2>
    <!-- list 配置结果-->
    <div v-for="(t, i) in resultList" :key="i">
      <el-card style="" class="card-margin">
        <el-descriptions :title="'Computing ' + (i + 1)" :column="1" border>
          <template slot="extra">
            <el-button size="mini" icon="el-icon-edit" @click="openEditor(t, i)">
            </el-button>
            <el-button size="mini" icon="el-icon-delete" @click="removeItem(i)"></el-button>
          </template>
          <el-descriptions-item label="函数名称" :labelStyle="labelStyle">{{ t.function_name }}</el-descriptions-item>
          <el-descriptions-item label="参数" :labelStyle="labelStyle">{{ t.param }}</el-descriptions-item>
          <el-descriptions-item label="结果名称" :labelStyle="labelStyle">{{ t.result_name }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
    </div>
    <!-- 添加按钮和配置对话框 -->
    <el-card style="display: flex;align-content: center;justify-content: center;" class="card-margin">
      <el-button type="text" @click="openEditor()">
        <i class="el-icon-circle-plus" style="font-size: 25px;">新增历史条件计算</i>
      </el-button>
      <el-dialog title="历史条件计算" :visible.sync="dialogEditorVisible" @close="cancelUpdateItem">
        <el-form :model="item_now" label-width="auto">
          <el-form-item label="函数名称">
            <el-select v-model="item_now.function_name" placeholder="">
              <el-option v-for="item in functionOptions" :key="item.value" :label="item.label" :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="参数">
            <el-select
              v-model="item_now.param"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="">
              <el-option v-for="item in historyEventList" :key="item.name" :label="item.name" :value="item.name">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="结果名称">
            <el-input v-model="item_now.result_name" disabled></el-input>
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

import { functionOptions } from './data.js'

var resultCount = 1 // 给每个计算结果编号, func1, func2, func3...

const defaultItem = {
  function_name: '',
  param: [],
  result_name: 'func'
}

export default {
  props: {
    value: {
      type: Array,
      default: () => []
    },
    historyEventList: {
      type: Array,
      default: () => []
    }
  },
  data () {
    return {
      dialogEditorVisible: false,
      itemIndex: -1,
      item_now: JSON.parse(JSON.stringify(defaultItem)),
      resultList: [],
      functionOptions,
      labelStyle: { 'width': '80px' } // 表单样式 (标签列宽固定)
    }
  },
  mounted () {
    this.resultList = this.value
  },
  methods: {
    test () { },
    getResult () {
      const finalResult = JSON.parse(JSON.stringify(this.resultList))
      finalResult.forEach((item) => {
        item.param = item.param.join(',')
      })
      return finalResult
    },
    showResult (computing) {
      if (!computing) return
      const list = []
      computing.forEach(cItem => {
        const item = cItem
        if (cItem.param === '') {
          cItem.param = []
        } else {
          item.param = cItem.param.split(',')
        }
        list.push(item)
      })
      list.forEach(item => {
        this.resultList.push(item)
      })
    },
    refresh () {
      this.$forceUpdate()
      this.$emit('input', this.resultList)
    },
    openEditor (triggerInfo, index) {
      console.log(triggerInfo)
      if (triggerInfo && index >= 0) {
        this.item_now = JSON.parse(JSON.stringify(triggerInfo))
        this.itemIndex = index
      } else {
        this.item_now.result_name = this.item_now.result_name + resultCount
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
        resultCount++
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
</style>
