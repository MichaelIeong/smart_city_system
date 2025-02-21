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
        <!-- 问题输入 -->
        <a-form-item label="请输入您的问题">
          <a-input
            v-decorator="['question', {rules: [{required: true, message: '请输入问题!'}]}]"
            placeholder="请输入您的问题..."
          />
        </a-form-item>

        <!-- 显示响应 -->
        <a-form-item label="模型回答">
          <div class="response">
            <div v-html="response"></div>
          </div>
        </a-form-item>

        <a-form-item>
          <a-button type="primary" @click="sendQuestion">发送问题</a-button>
        </a-form-item>
      </a-form>
    </a-spin>
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
      response: '',
      question: ''
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
    handleOk () {
      // 处理OK事件，关闭弹窗或其他逻辑
    },
    sendQuestion () {
      if (!this.question.trim()) {
        this.response = '请输入有效的问题！'
        return
      }

      const requestData = {
        model: 'deepseek-r1:70b',
        stream: false,
        prompt: this.question
      }

      this.loading = true
      fetch('http://10.177.29.226:11434/api/generate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestData)
      })
        .then(response => response.json())
        .then(data => {
          this.response = data && data.response ? `<strong>回答:</strong> ${data.response}` : '没有得到有效的回答，请稍后再试。'
        })
        .catch(error => {
          this.response = '请求失败，请检查网络或稍后再试。'
          console.error('Error:', error)
        })
        .finally(() => {
          this.loading = false
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
</style>
