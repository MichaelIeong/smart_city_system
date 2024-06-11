<template>
  <div class="container">
    <div class="button-container">
      <div class="left-buttons">
        <el-button type="primary" plain @click="dialogVisible = true">
          新增设备
          <el-icon class="el-icon-plus"></el-icon>
        </el-button>
      </div>
      <div class="right-buttons">
        <el-button plain @click="triggerFileInput">
          导入
          <el-icon class="el-icon-document-add"></el-icon>
        </el-button>
        <el-button plain @click="exportJson">
          导出
          <el-icon class="el-icon-download"></el-icon>
        </el-button>
      </div>
      <input type="file" @change="handleFileUpload" accept=".json" style="display: none;" ref="fileInput">
    </div>
    <div class="table-container">
      <el-table :data="devices" stripe class="custom-table">
        <el-table-column prop="deviceName" label="设备名称" align="center" :width="150"></el-table-column>
        <el-table-column prop="spaceName" label="所属空间" align="center" :width="150"></el-table-column>
        <el-table-column prop="status" label="状态" align="center" :width="150"></el-table-column>
        <el-table-column prop="capabilities" label="功能" align="center" :width="150"></el-table-column>
        <el-table-column prop="url" label="设备URL" align="center" :width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="data" label="数据" align="center" :width="200" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" align="center" :width="100">
          <template slot-scope="scope">
            <el-button type="danger" plain size="mini" @click="confirmDeleteDevice(scope.row.deviceId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog :visible.sync="dialogVisible" title="新增设备" width="70%" custom-class="custom-dialog">
      <DeviceAccess @formSubmitted="fetchDevices" @dialogClosed="dialogVisible = false"/>
    </el-dialog>

    <el-dialog :visible.sync="confirmDialogVisible" title="确认删除" width="30%" custom-class="custom-dialog">
      <el-button @click="confirmDialogVisible = false">取消</el-button>
      <el-button type="danger" @click="deleteDevice">删除</el-button>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios';
import { Message, Loading } from 'element-ui';
import DeviceAccess from './DeviceAccess.vue';
import jsonUtil from './utils/jsonUtil'; // Import the JSON utility

export default {
  components: {
    DeviceAccess
  },
  data() {
    return {
      devices: [],
      dialogVisible: false,
      confirmDialogVisible: false,
      deviceToDelete: null,
      jsonString: '',
      loadingInstance: null // Loading instance
    };
  },
  mounted() {
    this.fetchDevices();
  },
  methods: {
    fetchDevices() {
      axios.get('/api/devices/allDevices')
          .then(response => {
            this.devices = response.data;
          })
          .catch(error => {
            Message.error('获取设备列表失败: ' + error.message);
            console.error('Error fetching devices:', error);
          });
    },
    triggerFileInput() {
      this.$refs.fileInput.click();
    },
    handleFileUpload(event) {
      const file = event.target.files[0];
      if (file) {
        this.loadingInstance = Loading.service({ text: '正在导入...' }); // Show loading
        jsonUtil.readFileAsJson(file)
            .then(json => {
              this.jsonString = JSON.stringify(json);
              return axios.post('/api/devices/import', this.jsonString, {
                headers: {
                  'Content-Type': 'application/json'
                }
              });
            })
            .then(() => {
              this.loadingInstance.close(); // Hide loading
              Message.success('导入成功');
              this.fetchDevices();
            })
            .catch(error => {
              this.loadingInstance.close(); // Hide loading
              Message.error('导入失败: ' + error.message);
              console.error('Error importing JSON:', error);
            });
      } else {
        Message.error('请选择一个文件');
      }
    },
    exportJson() {
      axios.get('/api/devices/export')
          .then(response => {
            Message.success('导出成功');
            jsonUtil.downloadJson(response.data, 'devices.json');
          })
          .catch(error => {
            Message.error('导出失败: ' + error.message);
            console.error('Error exporting JSON:', error);
          });
    },
    confirmDeleteDevice(deviceId) {
      this.deviceToDelete = deviceId;
      this.confirmDialogVisible = true;
    },
    deleteDevice() {
      axios.delete(`/api/devices/${this.deviceToDelete}`)
          .then(() => {
            Message.success('删除成功');
            this.confirmDialogVisible = false;
            this.deviceToDelete = null;
            this.fetchDevices();
          })
          .catch(error => {
            Message.error('删除失败: ' + error.message);
            console.error('Error deleting device:', error);
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

.button-container {
  width: 100%;
  display: flex;
  justify-content: space-between; /* 分开左右两边 */
  margin-bottom: 20px;
}

.left-buttons {
  display: flex;
  align-items: center;
}

.right-buttons {
  display: flex;
  align-items: center;
}

.table-container {
  width: 100%;
  overflow-x: auto; /* 允许水平滚动 */
}

.custom-table {
  width: 100%;
  background: white;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  overflow: hidden;
}

.el-table {
  width: 100%;
}

.el-table th, .el-table td {
  text-align: center;
}

.el-table__body-wrapper {
  overflow-x: auto; /* 始终显示水平滚动条 */
}

.el-table__body-wrapper::-webkit-scrollbar {
  height: 8px; /* 滚动条高度 */
}

.el-table__body-wrapper::-webkit-scrollbar-thumb {
  background-color: #c1c1c1; /* 滚动条颜色 */
  border-radius: 4px; /* 滚动条圆角 */
}

.el-table__body-wrapper::-webkit-scrollbar-track {
  background-color: #f1f1f1; /* 滚动条轨道颜色 */
}

/* Add custom styles for the dialog */
.custom-dialog .el-dialog__header,
.custom-dialog .el-dialog__body,
.custom-dialog .el-dialog__footer {
  border-radius: 10px;
}
</style>