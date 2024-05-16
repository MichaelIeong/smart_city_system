<template>
  <div class="container">
    <el-form :model="deviceInfo" ref="deviceForm" label-width="120px" class="device-form">
      <el-button icon="el-icon-arrow-left" @click="goToHomePage" class="return-button"></el-button>
      <el-form-item label="Space Id" prop="spaceId">
        <el-input v-model="deviceInfo.spaceId"></el-input>
      </el-form-item>
      <el-form-item label="Name" prop="name">
        <el-input v-model="deviceInfo.name"></el-input>
      </el-form-item>
      <el-form-item label="URL" prop="url">
        <el-input v-model="deviceInfo.url"></el-input>
      </el-form-item>
      <el-form-item label="Status" prop="status">
        <el-input v-model="deviceInfo.status"></el-input>
      </el-form-item>
      <el-form-item label="Capabilities" prop="capabilities">
        <el-input v-model="deviceInfo.capabilities"></el-input>
      </el-form-item>
      <el-form-item label="Data" prop="data">
        <el-input v-model="deviceInfo.data"></el-input>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="submitForm">Submit</el-button>
        <el-button @click="resetForm">Reset</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import axios from 'axios';
import { Message } from 'element-ui';  // 修改这里

export default {
  data() {
    return {
      deviceInfo: {
        spaceId: '',
        name: '',
        url: '',
        status: '',
        capabilities: '',
        data: ''
      }
    };
  },
  methods: {
    submitForm() {
      this.$refs.deviceForm.validate((valid) => {
        if (valid) {
          axios.post('/api/device/upload', this.deviceInfo)
            .then(response => {
              Message.success('Form submitted successfully!');
              console.log('Form submitted successfully:', response.data);
              // Handle successful response here
            })
            .catch(error => {
              Message.error('Error submitting form: ' + error);
              console.error('Error submitting form:', error);
              // Handle error response here
            });
        } else {
          console.log('Validation failed');
          return false;
        }
      });
    },
    resetForm() {
      this.$refs.deviceForm.resetFields();
    },
    goToHomePage() {
      // Logic to navigate to the home page
      this.$router.push({ path: '/' });
    }
  }
};
</script>

<style scoped>
.container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 50px;
  position: relative;
}

.device-form {
  max-width: 600px;
  width: 100%;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.el-form-item {
  margin-bottom: 16px;
  text-align: center;
}

.el-button {
  margin-right: 10px;
}

.return-button {
  position: absolute;
  top: -20px; /* 调整这个值以适应按钮在表单中的位置 */
  left: 10px;
}
</style>
