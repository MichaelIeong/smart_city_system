<template>
  <div class="container">
    <h2 class="form-title">所有设备</h2>
    <div class="button-container">
      <el-button type="primary" class="init-button" @click="dialogVisible = true">新增设备</el-button>
    </div>
    <el-table :data="devices" stripe class="custom-table">
      <el-table-column prop="deviceName" label="设备名称" align="center"></el-table-column>
      <el-table-column prop="spaceName" label="所属空间" align="center"></el-table-column>
      <el-table-column prop="url" label="设备URL" align="center"></el-table-column>
      <el-table-column prop="status" label="状态" align="center"></el-table-column>
      <el-table-column prop="capabilities" label="功能" align="center"></el-table-column>
      <el-table-column prop="data" label="数据" align="center"></el-table-column>
    </el-table>

    <el-dialog :visible.sync="dialogVisible" title="新增设备" width="70%" custom-class="custom-dialog">
      <DeviceAccess @formSubmitted="fetchDevices" @dialogClosed="dialogVisible = false"/>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios';
import {Message} from 'element-ui';
import DeviceAccess from './DeviceAccess.vue'; // Import the DeviceAccess component

export default {
  components: {
    DeviceAccess
  },
  data() {
    return {
      devices: [], // Array to store device data fetched from the backend
      dialogVisible: false
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
      this.$router.push({path: '/'});
    }
  }
};
</script>

<style scoped>
.container {
  width: 90%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  max-width: 1000px; /* Set a narrower max-width for the container */
  margin: 0 auto; /* Center the container */
}

.form-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 10px;
  text-align: center;
}

.button-container {
  width: 100%;
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.init-button {
  margin-left: auto;
}

.custom-table {
  margin: 0 10px; /* 左右各添加 10px 的外边距 */
  background: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  overflow: hidden;
}

.el-table th, .el-table td {
  text-align: center;
}

/* Add custom styles for the dialog */
.custom-dialog .el-dialog__header,
.custom-dialog .el-dialog__body,
.custom-dialog .el-dialog__footer {
  border-radius: 10px;
}
</style>