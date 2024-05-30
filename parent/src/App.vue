<template>
  <div id="app">
    <div class="frame-container">
      <div class="frame-wrapper" v-show="!isRightFullScreen">
        <iframe src="http://localhost:8082" class="project-frame"></iframe>
        <el-button class="toggle-button" @click="toggleFullScreen('left')" plain circle>
          <el-icon :size="20">
            <component :is="isLeftFullScreen ? closeIcon : fullScreenIcon" />
          </el-icon>
        </el-button>
      </div>
      <div class="frame-wrapper" v-show="!isLeftFullScreen">
        <iframe src="http://localhost:8083" class="project-frame"></iframe>
        <el-button class="toggle-button" @click="toggleFullScreen('right')" plain circle>
          <el-icon :size="20">
            <component :is="isRightFullScreen ? closeIcon : fullScreenIcon" />
          </el-icon>
        </el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { ElButton, ElIcon } from 'element-plus'; // 引入 Element Plus 的按钮和图标组件
import { Close, FullScreen } from '@element-plus/icons-vue'; // 引入需要的图标

export default {
  name: 'App',
  components: {
    ElButton, // 注册组件
    ElIcon,
    Close,
    FullScreen
  },
  data() {
    return {
      isLeftFullScreen: false,
      isRightFullScreen: false,
      closeIcon: 'Close',
      fullScreenIcon: 'FullScreen'
    };
  },
  methods: {
    toggleFullScreen(side) {
      if (side === 'left') {
        this.isLeftFullScreen = !this.isLeftFullScreen;
        this.isRightFullScreen = false;
      } else if (side === 'right') {
        this.isRightFullScreen = !this.isRightFullScreen;
        this.isLeftFullScreen = false;
      }
    }
  }
};
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  width: 100%;
  margin: 0;
  padding: 0;
}

.frame-container {
  display: flex;
  flex: 1;
  width: 100%;
  height: 100%;
}

.frame-wrapper {
  position: relative;
  flex: 1;
  transition: flex 0.3s ease;
}

.project-frame {
  width: 100%;
  height: 100%;
  border: none;
}

.toggle-button {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>