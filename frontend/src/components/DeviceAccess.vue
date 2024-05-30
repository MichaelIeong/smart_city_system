<template>
    <el-form :model="deviceInfo" ref="deviceForm" label-width="70px">
      <el-form-item label="设备名称" prop="deviceName">
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
      <div>
        <el-button class="button" type="primary" @click="submitForm">提交</el-button>
      </div>
    </el-form>
</template>

<script>
import axios from 'axios';
import { Message } from 'element-ui';

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
      this.$refs.deviceForm.validate((valid) => {
        if (valid) {
          axios.post('/api/devices/upload', this.deviceInfo)
            .then(response => {
              Message.success('表单提交成功!');
              this.$emit('formSubmitted'); // Emit event to inform parent component
              this.$emit('dialogClosed'); // Emit event to close the dialog
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
    }
  }
};
</script>

<style scoped>

.el-form-item {
  width: 95%;
  margin-top: 10px;
}

.el-select {
  width: 100%;
}

.el-select .el-input .el-input__inner {
  text-align: center;
}

.button {
  width: 100px;
  margin-right: 30px;
  margin-left: 30px;
}
</style>