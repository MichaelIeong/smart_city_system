<template>
  <div class="container">
    <h2 class="form-title">所有空间</h2>
    <div class="button-container">
      <el-button type="primary" class="init-button" @click="dialogVisible = true">新增设备</el-button>
    </div>
    <el-table :data="spaces" stripe style="width: 80%; margin: 0 auto;">
      <el-table-column prop="spaceName" label="空间名称" align="center" width="180"></el-table-column>
      <el-table-column prop="type" label="空间类型" align="center" width="100"></el-table-column>
      <el-table-column prop="description" label="空间描述" align="center" width="200"></el-table-column>
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

    <el-dialog :visible.sync="dialogVisible" title="空间初始化" width="50%">
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
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  width: 100vw; /* Full viewport width */
  margin: 0;
}

.form-title {
  font-size: 24px;
  font-weight: bold;
  text-align: center;
  margin-bottom: 10px;
}

.button-container {
  width: 80%;
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.init-button {
  margin-left: auto;
}

.el-table {
  background: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  overflow: hidden;
  margin-top: 60px; /* To ensure table is below the button */
}

.el-table th, .el-table td {
  text-align: center;
}

/* 新增的设备卡片样式 */
.device-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
}

.device-card {
  background: #f9f9f9;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  margin: 10px;
  padding: 20px;
  width: 150px;
  text-align: center;
}

.device-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 10px;
}

.device-status {
  font-size: 14px;
  color: #409EFF;
}

.space-form {
  width: 100%;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
}

.button {
  margin-right: 10px;
}
</style>