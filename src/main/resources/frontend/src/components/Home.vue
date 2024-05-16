<!-- src/components/Home.vue -->
<template>
  <div>
    <h1>Home</h1>
    <button @click="goToSctap">sctap</button>
    <button @click="goToDeviceAccess">设备接入</button>
  </div>
</template>

<script>
export default {
  methods: {
    goToSctap() {
      this.$router.push({ name: 'application' });
    },
    goToDeviceAccess() {
      this.$router.push({ name: 'DeviceAccess' });
    },
  },
};
</script>
