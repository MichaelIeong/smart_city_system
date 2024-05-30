<template>
    <el-form :model="spaceInfo" ref="spaceForm" label-width="70px">
      <el-form-item label="空间名称" prop="spaceName">
        <el-input v-model="spaceInfo.spaceName"></el-input>
      </el-form-item>
      <el-form-item label="空间类型" prop="type">
        <el-input v-model="spaceInfo.type"></el-input>
      </el-form-item>
      <el-form-item label="空间描述" prop="description">
        <el-input v-model="spaceInfo.description"></el-input>
      </el-form-item>
      <div>
        <el-button class="button" type="primary" @click="submitForm">提交</el-button>
      </div>
    </el-form>
</template>

<script>
import axios from 'axios';
import { Message } from 'element-ui';

export default {
  data() {
    return {
      spaceInfo: {
        spaceName: '',
        type: '',
        description: ''
      }
    };
  },
  methods: {
    submitForm() {
      this.$refs.spaceForm.validate((valid) => {
        if (valid) {
          axios.post('/api/spaces/create', this.spaceInfo)
            .then(response => {
              Message.success('空间创建成功!');
              this.$emit('formSubmitted'); // Emit event to inform parent component
              this.$emit('dialogClosed'); // Emit event to close the dialog
              console.log('空间创建成功:', response.data);
            })
            .catch(error => {
              Message.error('创建空间时出错: ' + error);
              console.error('创建空间时出错:', error);
            });
        } else {
          console.log('验证失败');
          return false;
        }
      });
    },
    resetForm() {
      this.$refs.spaceForm.resetFields();
    }
  }
};
</script>

<style scoped>
.el-form-item {
  width: 95%;
  margin-top: 10px;
}

.button {
  width: 100px;
  margin-right: 30px;
  margin-left: 30px;
}
</style>