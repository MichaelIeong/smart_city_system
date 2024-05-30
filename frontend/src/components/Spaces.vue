<template>
  <div class="container">
    <h2 class="form-title">所有空间</h2>
    <div class="button-container">
      <el-button type="primary" class="init-button" @click="dialogVisible = true">新增设备</el-button>
    </div>
    <el-table :data="spaces" stripe class="custom-table">
      <el-table-column prop="spaceName" label="空间名称" align="center"></el-table-column>
      <el-table-column prop="type" label="空间类型" align="center"></el-table-column>
      <el-table-column prop="description" label="空间描述" align="center"></el-table-column>
      <el-table-column label="设备列表" align="center">
        <template slot-scope="scope">
          <div class="device-list">
            <div v-for="device in scope.row.spaceDevices" :key="device.deviceName" class="device-card">
              <div class="device-name">{{ device.deviceName }}</div>
              <div class="device-status">{{ device.status }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :visible.sync="dialogVisible" title="空间初始化" width="70%" custom-class="custom-dialog">
      <SpaceInitialization @formSubmitted="fetchSpaces" @dialogClosed="dialogVisible = false"/>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios';
import {Message} from 'element-ui';
import SpaceInitialization from './SpaceInitialization.vue'; // Import the SpaceInitialization component

export default {
  components: {
    SpaceInitialization
  },
  data() {
    return {
      spaces: [], // Array to store space data fetched from the backend
      dialogVisible: false
    };
  },
  mounted() {
    this.fetchSpaces();
  },
  methods: {
    fetchSpaces() {
      axios.get('/api/spaces/allSpaces') // Adjust URL as needed
          .then(response => {
            this.spaces = response.data;
          })
          .catch(error => {
            Message.error('获取空间及设备列表失败: ' + error.message);
            console.error('Error fetching spaces and devices:', error);
          });
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

/* 设备卡片样式调整 */
.device-card {
  background: #f9f9f9;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin: 10px;
  padding: 10px; /* 减小内边距 */
  width: 120px; /* 减小卡片宽度 */
  text-align: center;
}

.device-name {
  font-size: 14px; /* 调整字体大小 */
  font-weight: bold;
  margin-bottom: 5px; /* 调整下边距 */
}

.device-status {
  font-size: 12px; /* 调整字体大小 */
  color: #409EFF;
}

/* 卡片列表的布局调整，使其更紧凑 */
.device-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: start; /* 调整对齐方式，使卡片靠左对齐 */
}

/* Add custom styles for the dialog */
.custom-dialog .el-dialog__header,
.custom-dialog .el-dialog__body,
.custom-dialog .el-dialog__footer {
  border-radius: 10px;
}
</style>