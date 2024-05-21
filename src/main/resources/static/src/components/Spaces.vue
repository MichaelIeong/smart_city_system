<template>
  <div class="container">
    <div class="header">
      <el-button type="text" icon="el-icon-arrow-left" @click="goToHomePage" class="return-button"></el-button>
      <h2 class="form-title">所有空间</h2>
    </div>
    <el-table :data="spaces" style="width: 100%">
      <el-table-column prop="spaceName" label="空间名称" width="180"></el-table-column>
      <el-table-column prop="type" label="空间类型" width="100"></el-table-column>
      <el-table-column prop="description" label="空间描述" width="200"></el-table-column>
      <el-table-column label="设备列表" width="300">
        <template slot-scope="scope">
          <el-table :data="scope.row.spaceDevices" style="width: 100%">
            <el-table-column prop="deviceName" label="设备名称"></el-table-column>
            <el-table-column prop="status" label="状态"></el-table-column>
          </el-table>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import axios from 'axios';
import { Message } from 'element-ui';

export default {
  data() {
    return {
      spaces: []
    };
  },
  mounted() {
    this.fetchSpaces();
  },
  methods: {
    fetchSpaces() {
      axios.get('/api/spaces/allSpaces')
        .then(response => {
          this.spaces = response.data;
        })
        .catch(error => {
          Message.error('获取空间及设备列表失败: ' + error.message);
          console.error('Error fetching spaces and devices:', error);
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

