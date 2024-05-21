<template>
  <div class="container">
    <el-form :model="deviceInfo" ref="deviceForm" label-width="120px" class="device-form">
      <el-button icon="el-icon-arrow-left" @click="goToHomePage" class="return-button"></el-button>
      <el-form-item label="Space Name" prop="spaceName">
        <el-select v-model="deviceInfo.spaceName" placeholder="Select a space">
          <el-option
              v-for="space in spaces"
              :key="space.spaceName"
              :label="space.spaceName"
              :value="space.spaceName">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="Device Name" prop="name">
        <el-input v-model="deviceInfo.deviceName"></el-input>
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
      spaces: []  // 存储从后端获取的空间数据
    };
  },
  mounted() {
    this.fetchSpaces();  // 获取空间数据
  },
  methods: {
    fetchSpaces() {
      axios.get('/api/spaces/allSpaces')
          .then(response => {
            this.spaces = response.data;  // 存储空间数据到spaces数组
          })
          .catch(error => {
            console.error('Error fetching spaces:', error);
          });
    },
    submitForm() {
      console.log("Submitting with deviceInfo:", this.deviceInfo);  // 打印提交的数据
      this.$refs.deviceForm.validate((valid) => {
        if (valid) {
          axios.post('/api/device/upload', this.deviceInfo)
              .then(response => {
                Message.success('Form submitted successfully!');
                console.log('Form submitted successfully:', response.data);
              })
              .catch(error => {
                Message.error('Error submitting form: ' + error);
                console.error('Error submitting form:', error);
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
