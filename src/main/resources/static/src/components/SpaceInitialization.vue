<template>
  <div class="container">
    <el-form :model="spaceInfo" ref="spaceForm" label-width="120px" class="space-form">
      <div class="header">
        <el-button type="text" icon="el-icon-arrow-left" @click="goToHomePage" class="return-button"></el-button>
        <h2 class="form-title">空间初始化</h2>
      </div>
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
        <el-button class="button" @click="resetForm">重置</el-button>
      </div>
    </el-form>
  </div>
</template>

<script>
import axios from 'axios';
import {Message} from 'element-ui';

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
    },
    goToHomePage() {
      this.$router.push({path: '/'});
    }
  }
};
</script>


<style scoped>
.container {
  display: flex;
  align-items: center; /* 垂直居中 */
  justify-content: center; /* 水平居中 */
  height: 100vh; /* 视口高度 */
  width: 100vw; /* 视口宽度 */
  margin: 0; /* 去掉默认的 margin */
}

.return-button {
  font-size: 30px;
}

.header {
  display: flex;
  align-items: center;
  width: 100%;
  margin-bottom: 20px;
}

.space-form {
  max-width: 600px;
  width: 100%;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.form-title {
  display: flex;
  margin-left: 35%;
  flex-grow: 1;
  text-align: center;
  font-size: 24px;
  font-weight: bold;
}

.el-form-item {
  width: 95%;
  top: 10px;
}

.button {
  align-items: center;
  width: 100px;
  margin-right: 20px;
  margin-left: 20px;
}
</style>