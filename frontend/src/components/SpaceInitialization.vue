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
import {Message} from 'element-ui';

export default {
  props: {
    space: {
      type: Object,
      default: () => ({
        spaceName: '',
        type: '',
        description: ''
      })
    }
  },
  data() {
    return {
      spaceInfo: {...this.space}
    };
  },
  methods: {
    submitForm() {
      this.$refs.spaceForm.validate((valid) => {
        if (valid) {
          const request = this.spaceInfo.spaceId
              ? axios.put(`/api/spaces/${this.spaceInfo.spaceId}`, this.spaceInfo)
              : axios.post('/api/spaces/create', this.spaceInfo);

          request
              .then(response => {
                Message.success('提交成功!');
                this.$emit('formSubmitted');
                this.$emit('dialogClosed');
                console.log('提交成功:', response.data);
              })
              .catch(error => {
                Message.error('提交失败: ' + error);
                console.error('提交失败:', error);
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
  },
  watch: {
    space: {
      handler(newSpace) {
        this.spaceInfo = {...newSpace};
      },
      deep: true
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