<template>
  <el-container>
    <el-main>
      <h1> Scenario-Centric TAP </h1>
      <div style="display: flex;flex-direction: row-reverse;">
        <el-button type="primary" @click="dialogVisible = true" style="margin-right: 30px;">UPLOAD</el-button>
      </div>
      <el-dialog title="UPLOAD CONFIGURATION" :visible.sync="dialogVisible">
        <el-form ref="form" :model="form" :rules="rules">
          <el-form-item label="user name" prop="user">
            <el-input v-model="form.user" placeholder="user name"></el-input>
          </el-form-item>
          <el-form-item label="app name" prop="app">
            <el-select v-model="form.app" placeholder="app name" filterable>
              <el-option v-for="item in appOptions" :key="item.value" :label="item.label" :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="upload">Confirm</el-button>
        </span>
      </el-dialog>
      <el-row :gutter="30">

        <el-col :span="12">
          <triggerEditor ref="trigger" v-model="triggerList" />
        </el-col>
        <el-col :span="12">
          <actionEditor ref="action" v-model="actionList" :computedResultList="computedResultList" />
        </el-col>

        <el-col :span="24" v-if="hasHistory">
          <el-card>
            <h3> </h3>
            <el-row :gutter="10">
              <el-col :span="12">
                <historyEventEditor ref="history" v-model="historyEventList" />
              </el-col>
              <el-col :span="12">
                <computedResultEditor
                  ref="computing"
                  v-model="computedResultList"
                  :historyEventList="historyEventList" />
              </el-col>
            </el-row>
          </el-card>
        </el-col>

      </el-row>
    </el-main>
  </el-container>
</template>

<script>

import { appOptions } from './modules/data'
import triggerEditor from './modules/triggerEditor.vue'
import actionEditor from './modules/actionEditor.vue'
import historyEventEditor from './modules/historyEventEditor.vue'
import computedResultEditor from './modules/computedResultEditor.vue'
import LocationInput from './modules/locationInput.vue'
import bus from './modules/bus'
import { saveTap } from '@/api/manage'

export default {
  name: 'TapDetail',
  components: {
    triggerEditor,
    actionEditor,
    historyEventEditor,
    computedResultEditor,
    LocationInput
  },
  data () {
    return {
      triggerList: [],
      actionList: [],
      historyEventList: [],
      computedResultList: [],

      form: {
        user: '',
        app: ''
      },
      rules: {
        user: [
          { required: true, message: 'please input your name.', trigger: 'blur' }
        ],
        app: [
          { required: true, message: 'Please select the application name.', trigger: 'blur' }
        ]
      },

      dialogVisible: false,
      hasHistory: false,
      startTime: 0,
      appOptions
    }
  },
  watch: {},
  methods: {
    upload () {
      const finalResult = this.getResult()
      const uploadResult = {
        user: this.form.user,
        dsl: finalResult,
        endTime: '',
        startTime: this.startTime,
        app: this.form.app
      }

      this.$refs.form.validate((valid) => {
        if (valid) {
          this.dialogVisible = false
          saveTap(uploadResult).then(res => {
            if (res.code === 0) {
              this.$message.success('Upload successfully!')
            } else {
              this.$message.error(res)
            }
          })
        } else {
          this.$message.warning('Please fill in the form correctly!')
        }
      })
    },
    getResult () {
      if (this.hasHistory) {
        const finalResult = {
          Scenario_Trigger: this.$refs.trigger.getResult(),
          Scenario_Action: this.$refs.action.getResult(),
          Scenario_Description: this.$refs.history.getResult()
        }
        finalResult.Scenario_Description.result = this.$refs.computing.getResult()
        return finalResult
      } else {
        const finalResult = {
          Scenario_Trigger: this.$refs.trigger.getResult(),
          Scenario_Action: this.$refs.action.getResult(),
          Scenario_Description: {
            event_list: [],
            location: [[]],
            time_zone: [{ type: '', start_time: '', end_time: '' }],
            object_id: [],
            result: [{ result_name: '', function_name: '', param: '' }]
          }
        }
        return finalResult
      }
    }
  },
  created () { },
  mounted () {
    this.startTime = new Date().getTime()
    bus.$on('hasHistory', e => {
      this.hasHistory = e
      if (this.hasHistory) {
        this.$message.warning('Please fill in the history event and computing result!')
      }
    })
  }
}
</script>
