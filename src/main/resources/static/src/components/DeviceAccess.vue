<template>
  <div class="container">
    <el-form :model="deviceInfo" ref="deviceForm" label-width="120px" class="device-form">
      <el-button type="text" icon="el-icon-arrow-left" @click="goToHomePage" class="return-button"></el-button>
      <el-form-item label="设备名称" prop="name">
        <el-input v-model="deviceInfo.deviceName"></el-input>
      </el-form-item>
      <el-form-item label="所属空间" prop="spaceName">
        <el-select v-model="deviceInfo.spaceName" placeholder="选择一个空间">
          <el-option
              v-for="space in spaces"
              :key="space.spaceName"
              :label="space.spaceName"
              :value="space.spaceName">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="URL" prop="url">
        <el-input v-model="deviceInfo.url"></el-input>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-input v-model="deviceInfo.status"></el-input>
      </el-form-item>
      <el-form-item label="功能" prop="capabilities">
        <el-input v-model="deviceInfo.capabilities"></el-input>
      </el-form-item>
      <el-form-item label="数据" prop="data">
        <el-input v-model="deviceInfo.data"></el-input>
      </el-form-item>
      <el-form-item class="form-buttons">
        <el-button type="primary" @click="submitForm">提交</el-button>
        <el-button @click="resetForm">重置</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import axios from 'axios';
import {Message} from 'element-ui';

export default {
  data() {
    return {
      deviceInfo: {
        spaceName: '',
        deviceName: '',
        url: '',
        status: '',
        capabilities: '',
        data: ''
      },
      spaces: [] // 存储从后端获取的空间数据
    };
  },
  mounted() {
    this.fetchSpaces(); // 获取空间数据
  },
  methods: {
    fetchSpaces() {
      axios.get('/api/spaces/allSpaces')
          .then(response => {
            this.spaces = response.data; // 存储空间数据到spaces数组
          })
          .catch(error => {
            console.error('获取空间数据时出错:', error);
          });
    },
    submitForm() {
      console.log("提交数据:", this.deviceInfo); // 打印提交的数据
      this.$refs.deviceForm.validate((valid) => {
        if (valid) {
          axios.post('/api/device/upload', this.deviceInfo)
              .then(response => {
                Message.success('表单提交成功!');
                console.log('表单提交成功:', response.data);
              })
              .catch(error => {
                Message.error('提交表单时出错: ' + error);
                console.error('提交表单时出错:', error);
              });
        } else {
          console.log('验证失败');
          return false;
        }
      });
    },
    resetForm() {
      this.$refs.deviceForm.resetFields();
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
  flex-direction: column;
  align-items: center;
  margin-top: 100px;
  position: relative;
}

.device-form {
  max-width: 700px;
  width: 100%;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.return-button {
  display: flex;
  top: 5px; /* 调整这个值以适应按钮在表单中的位置 */
  left: 10px;
  bottom: 50px;
  font-size: 30px;
}

.el-form-item {
  width: 95%;
  top: 10px;
}

.el-select {
  width: 100%;
}

.form-buttons {
  display: flex;
  justify-content: center;
}

</style>