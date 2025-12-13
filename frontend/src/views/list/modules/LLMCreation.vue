<template>
  <a-modal
    title="使用大模型创建规则"
    :width="640"
    :visible="modelModalVisible"
    :confirmLoading="loading"
    @ok="handleOk"
    @cancel="handleCancelModal"
  >
    <a-spin :spinning="loading">
      <a-form :form="form" v-bind="formLayout">
        <!-- 空间筛选 -->
        <a-form-item label="空间">
          <a-select
            placeholder="请选择空间"
            v-decorator="[
              'location',
              { rules: [{ required: true, message: '请选择空间!' }] }
            ]"
            @change="handleLocationChange"
          >
            <a-select-option
              v-for="loc in locations"
              :key="loc"
              :value="loc"
            >
              {{ loc }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <!-- 多选设备 -->
        <a-form-item label="选择参与的设备">
          <a-select
            mode="multiple"
            :disabled="!form.getFieldValue('location')"
            placeholder="请先选择空间"
            v-decorator="[
              'selectedDevice',
              { rules: [{ required: true, message: '请选择参与的设备!' }] }
            ]"
          >
            <a-select-option
              v-for="device in filteredDevices"
              :key="device.sensorId"
              :value="device.deviceName"
            >
              {{ device.deviceName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <!-- 用户需求输入 -->
        <a-form-item label="请输入你的需求">
          <a-textarea
            v-decorator="['requirement', { rules: [{ required: true, message: '请输入需求!' }] }]"
            placeholder="请输入你的需求..."
            rows="3"
          />
        </a-form-item>

        <!-- 模型回答 -->
        <a-form-item label="模型回答">
          <div class="response">
            <div v-html="response"></div>
          </div>
        </a-form-item>
      </a-form>
    </a-spin>

    <div class="modal-footer">
      <a-button type="primary" @click="sendQuestion">发送</a-button>
    </div>
  </a-modal>
</template>

<script>
import { getSensors } from '@/api/manage.js'

const MODEL_SERVER_URL = process.env.VUE_APP_MODEL_SERVER_URL
const MODEL_NAME = process.env.VUE_APP_MODEL_NAME

export default {
  props: {
    modelModalVisible: {
      type: Boolean,
      required: true
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      form: this.$form.createForm(this),
      response: '',
      devices: [], // 全量 sensors
      filteredDevices: [], // 根据 location 过滤后的设备
      locations: [] // 所有空间
    }
  },
  watch: {
    modelModalVisible (val) {
      if (val) {
        // 每次打开弹窗都清空
        this.resetModalState()
      }
    }
  },
  computed: {
    formLayout () {
      return {
        labelCol: { xs: { span: 24 }, sm: { span: 7 } },
        wrapperCol: { xs: { span: 24 }, sm: { span: 13 } }
      }
    }
  },
  mounted () {
    this.loadDevices()
  },
  methods: {
    handleLocationChange (location) {
      // 清空已选设备（避免跨空间脏数据）
      this.form.setFieldsValue({ selectedDevice: [] })
      if (!location) {
        // 清空 location → 显示全部设备
        this.filteredDevices = []
        return
      }
      // 选择空间 → 显示该空间下的设备
      this.filteredDevices = this.devices.filter(
        device => device.location === location
      )
    },

    handleCancelModal () {
      this.$emit('update:modelModalVisible', false)
    },

    handleOk () {
    },

    resetModalState () {
      this.form.resetFields()
      this.response = ''
      this.filteredDevices = []
    },

    loadDevices () {
      getSensors(1)
        .then(res => {
          let data = []

          if (Array.isArray(res)) {
            data = res
          } else if (res && Array.isArray(res.data)) {
            data = res.data
          }

          this.devices = data

          // 提取唯一 location
          this.locations = Array.from(
            new Set(data.map(item => item.location).filter(Boolean))
          )
          // 未选空间前，不显示任何设备
          this.filteredDevices = []
        })
        .catch(error => {
          console.error('加载设备失败：', error)
        })
    },

    sendQuestion () {
      this.form.validateFields((errors, values) => {
        if (errors) {
          this.response = '请输入有效的需求！'
          return
        }

        const selectedDevices = values.selectedDevice.join(', ')

        const promptTemplate = `请先参考这个json，并生成新的规则：{\\"steps\\":3,\\"0c53393282d602d7\\":{\\"type\\":\\"Operator\\",\\"step\\":3,\\"value\\":null,\\"operator\\":\\"AND\\",\\"output\\":\\"Boolean\\"},\\"bd3998b67e605caa\\":{\\"type\\":\\"Operator\\",\\"step\\":2,\\"value\\":\\"50\\",\\"operator\\":\\"Greater than\\",\\"output\\":\\"Boolean\\"},\\"87e4ec7c57203b0c\\":{\\"type\\":\\"Sensor\\",\\"step\\":1,\\"location\\":\\"Ai Park\\",\\"sensorId\\":\\"6\\",\\"deviceName\\":\\"湿度传感器\\",\\"sensingFunction\\":\\"humidity\\"},\\"6647f3ca070b28fd\\":{\\"type\\":\\"Operator\\",\\"step\\":2,\\"value\\":\\"20\\",\\"operator\\":\\"Greater than\\",\\"output\\":\\"Boolean\\"},\\"6a6566b5851c475b\\":{\\"type\\":\\"Sensor\\",\\"step\\":1,\\"location\\":\\"Ai Park\\",\\"sensorId\\":\\"4\\",\\"deviceName\\":\\"温度传感器2\\",\\"sensingFunction\\":\\"temperature\\"},\\"rulename\\":\\"温湿度过高\\"}`

        const promptDetail = ` 根据以下规则生成用户需要的规则，每个字段冒号前的id随机即可，step的编号是执行次序，type只有sensor和operator，value表示operator计算的输入，目前operator有Greater than、Less than、Equal to、Greater than or equal to、Less than or equal to、AND、OR，location不要更改。只需输出json不需其他解释语句，以JSON格式输出答案。用户选择的设备是：${selectedDevices}。用户需求：`

        const requestPayload = {
          model: MODEL_NAME, // 用環境變量
          stream: false,
          prompt: promptTemplate + promptDetail + values.requirement
        }

        console.log('发送给大模型:', requestPayload.prompt)

        this.loading = true

        fetch(MODEL_SERVER_URL, { // 用環境變量
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(requestPayload)
        })
          .then(response => response.json())
          .then(data => {
            this.response = data && data.response ? `${data.response}` : '没有得到有效的回答，请稍后再试。'
          })
          .catch(error => {
            this.response = '请求失败，请检查网络或稍后再试。'
            console.error('Error:', error)
          })
          .finally(() => {
            this.loading = false
          })
      })
    }
  }
}
</script>

<style scoped>
.response {
  margin-top: 10px;
  padding: 15px;
  background-color: #f1f1f1;
  border-radius: 5px;
  min-height: 100px;
  line-height: 1.5;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 20px;
}

a-button {
  width: 100%;
}
</style>
