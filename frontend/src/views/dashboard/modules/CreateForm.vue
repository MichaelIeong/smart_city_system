<template>
  <a-modal
    title="社会资源"
    :width="640"
    :visible="visible"
    :confirmLoading="loading"
    @ok="() => { $emit('ok') }"
    @cancel="() => { $emit('cancel') }"
  >
    <a-spin :spinning="loading">
      <a-form :form="form" v-bind="formLayout">
        <!-- 检查是否有 id 并且大于0，大于0是修改。其他是新增，新增不显示主键ID -->
        <a-form-item v-show="model && model.id > 0" label="主键ID">
          <a-input v-decorator="['id', { initialValue: 0 }]" disabled />
        </a-form-item>
        <a-form-item label="资源类型">
          <a-input v-decorator="['type', {rules: [{required: true, min: 1, message: '请输入待添加资源类型！'}]}]" />
        </a-form-item>
        <a-form-item label="资源编号">
          <a-input v-decorator="['number', {rules: [{required: true, min: 5, message: '请输入待添加资源编号！'}]}]" />
        </a-form-item>
        <a-form-item label="资源描述">
          <a-input v-decorator="['description', {rules: [{required: false}]}]" />
        </a-form-item>
        <a-form-item
          label="资源状态"
          :rules="[{ required: true, message: '请选择一个选项!' }]">
          <a-select v-decorator="['status']" placeholder="请选择资源使用状态">
            <a-select-option value="1">关闭</a-select-option>
            <a-select-option value="2">运行中</a-select-option>
            <a-select-option value="3">已上线</a-select-option>
            <a-select-option value="4">异常</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-spin>
  </a-modal>
</template>

<script>
import pick from 'lodash.pick'

// 表单字段
const fields = ['description', 'id']

export default {
  props: {
    visible: {
      type: Boolean,
      required: true
    },
    loading: {
      type: Boolean,
      default: () => false
    },
    model: {
      type: Object,
      default: () => null
    }
  },
  data () {
    this.formLayout = {
      labelCol: {
        xs: { span: 24 },
        sm: { span: 7 }
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 13 }
      }
    }
    return {
      form: this.$form.createForm(this)
    }
  },
  created () {
    console.log('custom modal created')

    // 防止表单未注册
    fields.forEach(v => this.form.getFieldDecorator(v))

    // 当 model 发生改变时，为表单设置值
    this.$watch('model', () => {
      this.model && this.form.setFieldsValue(pick(this.model, fields))
    })
  }
}
</script>
