<template>
  <div class="container">
    <div class="header">
      <el-button type="text" icon="el-icon-arrow-left" @click="goToHomePage" class="return-button"></el-button>
      <h2 class="form-title">所有设备</h2>
    </div>
    <el-table :data="devices" style="width: 100%">
      <el-table-column prop="deviceName" label="设备名称" width="180"></el-table-column>
      <el-table-column prop="spaceName" label="所属空间" width="180"></el-table-column>
      <el-table-column prop="url" label="设备URL" width="200"></el-table-column>
      <el-table-column prop="status" label="状态" width="100"></el-table-column>
      <el-table-column prop="capabilities" label="功能" width="200"></el-table-column>
      <el-table-column prop="data" label="数据" width="200"></el-table-column>
    </el-table>
  </div>
</template>

<script>
import axios from 'axios';
import {Message} from 'element-ui';

export default {
  data() {
    return {
      devices: [] // Array to store device data fetched from the backend
    };
  },
  mounted() {
    this.fetchDevices();
  },
  methods: {
    fetchDevices() {
      axios.get('/api/devices/allDevices') // Adjust URL as needed
        .then(response => {
          this.devices = response.data;
        })
        .catch(error => {
          Message.error('获取设备列表失败: ' + error.message);
          console.error('Error fetching devices:', error);
        });
    },
    goToHomePage() {
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
  justify-content: center;
  height: 100vh; /* Full viewport height */
  width: 100vw; /* Full viewport width */
  margin: 0;
}

.return-button {
  margin-right: 10px;
  font-size: 30px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between; /* Ensures the title and button are spaced apart */
  width: 100%;
  margin-bottom: 20px;
}

.form-title {
  flex-grow: 1;
  text-align: center;
  font-size: 24px;
  font-weight: bold;
}

.device-form {
  max-width: 800px;
  width: 100%;
  background: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}
</style>