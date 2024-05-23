<template>
  <div id="app">
    <el-header class="index_header">
      <el-menu
        :default-active="activeIndex"
        mode="horizontal"
        @select="handleSelect"
      >
        <el-menu-item index="1" @click="goToSctap">sctap</el-menu-item>
        <el-menu-item index="4" @click="goToDevices">所有设备</el-menu-item>
        <el-menu-item index="5" @click="goToSpaces">所有空间</el-menu-item>
      </el-menu>
    </el-header>
    <router-view></router-view>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      activeIndex: '1',
    };
  },
  methods: {
    handleSelect(key, keyPath) {
      console.log(key, keyPath);
    },
    goToSctap() {
      this.$router.push({name: 'application'});
    },
    goToDevices() {
      this.$router.push({name: 'Devices'});
    },
    goToSpaces() {
      this.$router.push({name: 'Spaces'});
    },
  },
};
</script>

<style>
/* 设置整个页面的背景颜色 */
html, body, #app {
  height: 100%;
  margin: 0;
  background-color: #f0f2f5; /* 设置背景颜色 */
}

#app {
  display: flex;
  flex-direction: column;
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

/* 美化顶部导航栏 */
.index_header {
  width: 100%;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 0 20px;
}

/* 设置菜单项样式 */
.el-menu {
  line-height: 64px;
}

.el-menu-item {
  font-size: 16px;
  color: #333;
}

.el-menu-item:hover,
.el-menu-item.is-active {
  color: #409EFF; /* 修改活动菜单项和悬停菜单项颜色 */
}
</style>