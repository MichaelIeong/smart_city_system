<!-- src/components/Home.vue -->
<template>
  <div class="home-container">
    <div class="button-container">
      <button class="nav-button" @click="goToSctap">sctap</button>
      <button class="nav-button" @click="goToDeviceAccess">设备接入</button>
      <button class="nav-button" @click="goToSpaceInit">空间初始化</button>
    </div>
  </div>
</template>

<script>
export default {
  methods: {
    goToSctap() {
      this.$router.push({name: 'application'});
    },
    goToDeviceAccess() {
      this.$router.push({name: 'DeviceAccess'});
    },
    goToSpaceInit() {
      this.$router.push({name: 'SpaceInitialization'});
    },
  },
};
</script>

<style scoped>
.home-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(to right, #4facfe, #00f2fe);
}

.button-container {
  display: flex;
  gap: 20px;
}

.nav-button {
  padding: 15px 30px;
  font-size: 18px;
  color: white;
  background-color: #007bff;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.nav-button:hover {
  background-color: #0056b3;
}
</style>
