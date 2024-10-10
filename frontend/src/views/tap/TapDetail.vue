<template>
  <el-container>
    <el-main>
      <h1> 应用构造 </h1>
      <div style="display: flex;flex-direction: row-reverse;">
        <el-button type="primary" @click="dialogVisible = true" style="margin-right: 30px;">提交</el-button>
      </div>
      <el-dialog title="提交表单" :visible.sync="dialogVisible">
        <!-- <el-form ref="form" :model="form" :rules="rules">
          <el-form-item label="user name" prop="user">
            <el-input v-model="form.user" placeholder="user name"></el-input>
          </el-form-item>
          <el-form-item label="app name" prop="app">
            <el-select v-model="form.app" placeholder="app name" filterable>
              <el-option v-for="item in appOptions" :key="item.value" :label="item.label" :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
        </el-form> -->
        <div style="text-align: center;">
          确定提交？
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="upload">确认</el-button>
        </span>
      </el-dialog>
      <el-row :gutter="30">

        <el-col :span="12">
          <triggerEditor ref="trigger" v-model="triggerList" />
        </el-col>
        <el-col :span="12">
          <actionEditor ref="action" v-model="actionList" :computedResultList="computedResultList" />
        </el-col>

        <el-col :span="24">
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
      <el-card class="box-card">
        <el-row>
          <el-col :span="24">
            <el-form label-width="80px">
              <h2 class="title">应用描述</h2>
              <el-input
                type="textarea"
                v-model="description"
                placeholder="请输入描述..."
                rows="4"
                clearable
              />
            </el-form>
          </el-col>
        </el-row>
      </el-card>
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
import { getTapDetail, saveTap } from '@/api/manage'

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
      description: '',

      form: {
        user: '',
        app: ''
      },
      rules: {
        user: [
          { required: true, message: '请输入您的名字。', trigger: 'blur' }
        ],
        app: [
          { required: true, message: '请选择应用名称。', trigger: 'blur' }
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
      const finalResultString = JSON.stringify(finalResult)
      const uploadResult = {
        projectId: 1, // TODO: 获取
        id: this.$route.params.id,
        description: this.description,
        ruleJson: finalResultString
      }
      console.log(uploadResult)
      this.dialogVisible = false
      saveTap(uploadResult).then(res => {
        this.$message.success('上传成功！')
        // TODO: res为空，失败了怎么办（
        // if (res.code === 0) {
        //   this.$message.success('上传成功！')
        // } else {
        //   this.$message.error(res)
        // }
      })
      this.$router.push({ name: 'TapListWrapper' }).then(() => {
        this.$router.go(0)
      })
    },
    getResult () {
      // if (this.hasHistory) {
      //   const finalResult = {
      //     Scenario_Trigger: this.$refs.trigger.getResult(),
      //     Scenario_Action: this.$refs.action.getResult(),
      //     Scenario_Description: this.$refs.history.getResult()
      //   }
      //   finalResult.Scenario_Description.result = this.$refs.computing.getResult()
      //   return finalResult
      // } else {
      //   const finalResult = {
      //     Scenario_Trigger: this.$refs.trigger.getResult(),
      //     Scenario_Action: this.$refs.action.getResult(),
      //     Scenario_Description: {
      //       event_list: [],
      //       location: [[]],
      //       time_zone: [{ type: '', start_time: '', end_time: '' }],
      //       object_id: [],
      //       result: [{ result_name: '', function_name: '', param: '' }]
      //     }
      //   }
      //   return finalResult
      const finalResult = {
        Scenario_Trigger: this.$refs.trigger.getResult(),
        Scenario_Action: this.$refs.action.getResult(),
        Scenario_Description: this.$refs.history.getResult()
      }
      finalResult.Scenario_Description.result = this.$refs.computing.getResult()
      return finalResult
    }
  },
  created () { },
  mounted () {
    this.startTime = new Date().getTime()
    bus.$on('hasHistory', e => {
      this.hasHistory = e
      if (this.hasHistory) {
        this.$message.warning('请填写场景描述和场景计算！') // TODO: 单次提醒只显示一条（now：有可能多条堆叠
      }
    })

    const tapID = this.$route.params.id
    if (tapID !== 0) {
      getTapDetail({ id: tapID })
        .then(res => {
          if (res.description) {
            this.description = res.description
          }
          if (res.ruleJson) {
            const info = JSON.parse(res.ruleJson)
            // console.log(info)
            // 处理ruleJson
            this.$refs.trigger.showResult(info.Scenario_Trigger)
            this.$refs.action.showResult(info.Scenario_Action)
            this.$refs.history.showResult(info.Scenario_Description)
            this.$refs.computing.showResult(info.Scenario_Description?.result || [])
          }
        })
    }
  }
}
</script>
<style scoped>
.box-card {
  margin: 10px auto;
}
</style>
