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
        <a-form-item label="请输入您的要求">
          <a-textarea
            v-decorator="['prompt', {rules: [{required: true, message: '请输入要求!'}]}]"
            placeholder="请输入您的要求..."
            rows="4"
          />
        </a-form-item>

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
      response: ''
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
  methods: {
    handleCancelModal () {
      this.$emit('update:modelModalVisible', false)
    },
    handleOk () {},
    sendQuestion () {
      this.form.validateFields((errors, values) => {
        if (errors) {
          this.response = '请输入有效的要求！'
          return
        }

        const promptTemplate = `请先参考这个json，并生成新的规则： {\\"steps\\": 3, \\"0c53393282d602d7\\": {\\"type\\": \\"Operator\\", \\"step\\": 3, \\"value\\": null, \\"operator\\": \\"AND\\", \\"output\\": \\"Boolean\\"}, \\"bd3998b67e605caa\\": {\\"type\\": \\"Operator\\", \\"step\\": 2, \\"value\\": \\"50\\", \\"operator\\": \\"Greater than\\", \\"output\\": \\"Boolean\\"}, \\"87e4ec7c57203b0c\\": {\\"type\\": \\"Sensor\\", \\"step\\": 1, \\"location\\": \\"Ai Park\\", \\"sensorId\\": \\"6\\", \\"deviceName\\": \\"湿度传感器\\", \\"sensingFunction\\": \\"humidity\\"}, \\"6647f3ca070b28fd\\": {\\"type\\": \\"Operator\\", \\"step\\": 2, \\"value\\": \\"20\\\\", \\"operator\\": \\"Greater than\\", \\"output\\": \\"Boolean\\"}, \\"6a6566b5851c475b\\": {\\"type\\": \\"Sensor\\", \\"step\\": 1, \\"location\\": \\"Ai Park\\", \\"sensorId\\": \\"4\\", \\"deviceName\\": \\"温度传感器2\\", \\"sensingFunction\\": \\"temperature\\"}, \\"rulename\\": \\"温湿度过高\\"}`

        const requestData = {
          model: 'qwen2.5:32b',
          stream: false,
          prompt: promptTemplate + ' 根据以下规则生成用户需要的规则，首先每个字段冒号前的id随机就行，step的编号是执行次序，type只有sensor和operator，value表示需要该operator计算的输入，目前的operator有这些Greater than、Less than、Equal to、Greater than or equal to、Less than or equal to、AND、OR，location先不要更改。只需要输出json文件，以JSON格式输出答案。用户需求：' + values.prompt
        }

        this.loading = true
        fetch('http://10.177.29.226:11434/api/generate', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(requestData)
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

a-form-item {
  margin-bottom: 16px;
}

a-button {
  width: 100%;
}

a-spin {
  display: flex;
  justify-content: center;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 20px;
}
</style>
