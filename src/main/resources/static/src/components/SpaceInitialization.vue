<template>
  <div class="container">
    <el-form :model="spaceInfo" ref="spaceForm" label-width="120px" class="space-form">
      <el-button icon="el-icon-arrow-left" @click="goToHomePage" class="return-button"></el-button>
      <el-form-item label="Space Name" prop="spaceName">
        <el-input v-model="spaceInfo.spaceName"></el-input>
      </el-form-item>
      <el-form-item label="Type" prop="type">
        <el-input v-model="spaceInfo.type"></el-input>
      </el-form-item>
      <el-form-item label="Description" prop="description">
        <el-input v-model="spaceInfo.description"></el-input>
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
import { Message } from 'element-ui';

export default {
  data() {
    return {
      spaceInfo: {
        spaceName: '',
        type: '',
        description: ''
      }
    };
  },
  methods: {
    submitForm() {
      this.$refs.spaceForm.validate((valid) => {
        if (valid) {
          axios.post('/api/spaces/create', this.spaceInfo)
            .then(response => {
              Message.success('Space created successfully!');
              console.log('Space created successfully:', response.data);
            })
            .catch(error => {
              Message.error('Error creating space: ' + error);
              console.error('Error creating space:', error);
            });
        } else {
          console.log('Validation failed');
          return false;
        }
      });
    },
    resetForm() {
      this.$refs.spaceForm.resetFields();
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
  margin-top: 50px;
  position: relative;
}

.space-form {
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
  top: -20px;
  left: 10px;
}
</style>