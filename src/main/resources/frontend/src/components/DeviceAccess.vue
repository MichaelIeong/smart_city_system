<template>
  <el-form :model="deviceInfo" ref="deviceForm" label-width="120px">
    <el-form-item label="Device ID" prop="deviceId">
      <el-input v-model="deviceInfo.deviceId"></el-input>
    </el-form-item>
    <el-form-item label="Space ID" prop="spaceId">
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
</template>

<script>
export default {
  data() {
    return {
      deviceInfo: {
        deviceId: 0,
        spaceId: 0,
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
          // Submit form logic here
          console.log('Form submitted:', this.deviceInfo);
        } else {
          console.log('Validation failed');
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
/* Add your styles here */
</style>