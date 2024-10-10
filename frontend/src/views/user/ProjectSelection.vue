<template>
  <div class="main">
    <h1 class="center-text">{{ $t('menu.projectSelection') }}</h1>

    <!-- 新增项目按钮的容器 -->
    <div class="button-wrapper">
      <button class="add-project-button" @click="triggerFileInput">新增项目</button>
      <!-- 隐藏的文件输入框 -->
      <input type="file" ref="fileInput" @change="handleFileChange" style="display: none;" />
    </div>

    <!-- 项目卡片的容器 -->
    <div class="project-grid">
      <div
        v-for="(project) in allProjects"
        :key="project.projectId"
        class="project-item"
        @click="selectProject(project.projectId)"
      >
        <!-- 显示项目名称 -->
        <div class="item-name">
          <p>{{ project.projectName || '无项目名称' }}</p>
        </div>

        <!-- 显示项目图片 -->
        <img :src="project.image" alt="Project Image" class="item-image" />
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

export default {
  data () {
    return {
      allProjects: [] // 初始项目数组为空，通过后端API获取数据
    }
  },
  created () {
    // 在页面加载时获取项目数据
    this.fetchProjects()
  },
  methods: {
    // 用户点击项目时，选择该项目并保存到 localStorage
    selectProject (projectId) {
      localStorage.setItem('selectedProjectId', projectId) // 保存选择的项目ID
      this.$router.push({ path: '/space-scene' })
    },

    // 获取所有项目数据
    async fetchProjects () {
      try {
        // 向后端发起请求，获取所有项目
        const response = await axios.get(
          'http://localhost:8080/api/projects/allProjects'
        )
        const fetchedProjects = response.data

        // 检查如果数据为空
        if (!fetchedProjects || fetchedProjects.length === 0) {
          console.warn('API 返回的数据为空或格式不正确')
        }

        // 将本地图片与获取的项目数据结合
        this.allProjects = fetchedProjects.map((project) => {
          // 判断 projectId 是否为 1 或 2 并设置本地图片
          if (project.projectId === 1) {
            project.image = require('@/assets/commercial.jpg') // ID 为 1 使用 commercial.jpg
          } else if (project.projectId === 2) {
            project.image = require('@/assets/residential.jpg') // ID 为 2 使用 residential.jpg
          }
          return project
        })
      } catch (error) {
        // 打印错误信息到控制台
        console.error('从API获取项目数据失败:', error)
      }
    },

    // 新增项目的按钮点击事件，触发文件选择框
    triggerFileInput () {
      // 确保文件输入框已经渲染好后触发点击
      this.$nextTick(() => {
        if (this.$refs.fileInput) {
          console.log('文件选择框已找到，准备触发点击')
          this.$refs.fileInput.click()
        } else {
          console.error('文件选择框不存在')
        }
      })
    },

    // 处理文件选择事件
    handleFileChange (event) {
      const file = event.target.files[0]
      if (file) {
        console.log('已选择文件:', file.name)
        // 这里可以执行文件上传逻辑
      }
    }
  }
}
</script>

<style scoped>
.app {
  text-align: center;
  max-width: 1200px;
  margin: 0 auto;
}

.center-text {
  color: #184aa1;
  text-align: center;
}

/* 新增项目的按钮容器 */
.button-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px; /* 按钮与项目卡片的间距 */
}

/* 新增项目的按钮样式 */
.add-project-button {
  padding: 10px 20px;
  background-color: #184aa1;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 1em;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  transition: background-color 0.3s ease;
}

.add-project-button:hover {
  background-color: #0c3275;
}

/* 项目网格 */
.project-grid {
  display: grid;
  padding: 30px;
  grid-template-columns: repeat(2, 1fr); /* 每行显示2个项目 */
  gap: 30px;
}

/* 项目卡片样式 */
.project-item {
  cursor: pointer;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s;
  width: 100%;
  height: 260px;
  position: relative; /* 为绝对定位的子元素做准备 */
}

.project-item:hover {
  transform: translateY(-5px);
}

.item-image {
  width: 100%;
  height: 210px; /* 固定图片高度 */
  border-bottom: 2px solid #184aa1;
}

.item-name {
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 8px;
  font-size: 1.4em;
  height: 50px;
  text-align: center;
  position: absolute; /* 绝对定位 */
  bottom: 0; /* 放置在卡片的最底部 */
  left: 0;
  right: 0;
}
</style>
