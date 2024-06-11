<template>
  <div class="container">
    <div class="button-container">
      <div class="left-buttons">
        <el-button type="primary" plain @click="dialogVisible = true">
          新增空间
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
      <el-table-column label="操作" align="center" :width="100">
        <template slot-scope="scope">
          <el-button type="danger" plain size="mini" @click="confirmDeleteSpace(scope.row.spaceId)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :visible.sync="dialogVisible" title="空间初始化" width="70%" custom-class="custom-dialog">
      <SpaceInitialization @formSubmitted="fetchSpaces" @dialogClosed="dialogVisible = false"/>
    </el-dialog>

    <el-dialog :visible.sync="confirmDialogVisible" title="确认删除" width="30%" custom-class="custom-dialog">
      <el-button @click="confirmDialogVisible = false">取消</el-button>
      <el-button type="danger" @click="deleteSpace">删除</el-button>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios';
import { Message, Loading } from 'element-ui';
import SpaceInitialization from './SpaceInitialization.vue'; // Import the SpaceInitialization component
import jsonUtil from './utils/jsonUtil'; // Import the JSON utility

export default {
  components: {
    SpaceInitialization
  },
  data() {
    return {
      spaces: [], // Array to store space data fetched from the backend
      dialogVisible: false,
      confirmDialogVisible: false,
      spaceToDelete: null,
      jsonString: '',
      loadingInstance: null // Loading instance
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
              return axios.post('/api/spaces/import', this.jsonString, {
                headers: {
                  'Content-Type': 'application/json'
                }
              });
            })
            .then(() => {
              this.loadingInstance.close(); // Hide loading
              Message.success('导入成功');
              this.fetchSpaces();
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
      axios.get('/api/spaces/export')
          .then(response => {
            Message.success('导出成功');
            jsonUtil.downloadJson(response.data, 'spaces.json');
          })
          .catch(error => {
            Message.error('导出失败: ' + error.message);
            console.error('Error exporting JSON:', error);
          });
    },
    confirmDeleteSpace(spaceId) {
      this.spaceToDelete = spaceId;
      this.confirmDialogVisible = true;
    },
    deleteSpace() {
      axios.delete(`/api/spaces/${this.spaceToDelete}`)
          .then(() => {
            Message.success('删除成功');
            this.confirmDialogVisible = false;
            this.spaceToDelete = null;
            this.fetchSpaces();
          })
          .catch(error => {
            Message.error('删除失败: ' + error.message);
            console.error('Error deleting space:', error);
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

.device-name {
  font-size: 14px; /* 调整字体大小 */
  font-weight: bold;
  margin-bottom: 5px; /* 调整下边距 */
}

.device-status {
  font-size: 12px; /* 调整字体大小 */
  color: #409EFF;
}

/* 设备列表横向排列样式调整 */
.device-list {
  display: flex;
  overflow-x: auto; /* 超出容器部分可滚动查看 */
  overflow-y: auto; /* 始终显示滚动条 */
  align-items: center; /* 项目在交叉轴上居中对齐 */
  justify-content: flex-start; /* 在主轴上靠左对齐 */
}

.device-card {
  min-width: 80px; /* 保持设备卡片的最小宽度 */
  margin: 5px; /* 调整边距，保持布局紧凑 */
  padding: 10px; /* 内边距 */
}

/* Add custom styles for the dialog */
.custom-dialog .el-dialog__header,
.custom-dialog .el-dialog__body,
.custom-dialog .el-dialog__footer {
  border-radius: 10px;
}
</style>