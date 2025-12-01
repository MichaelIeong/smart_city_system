<template>
  <div class="main">
    <h1 class="center-text">{{ $t('menu.projectSelection') }}</h1>

    <!-- 新增项目按钮的容器 -->
    <div class="button-wrapper">
      <button class="add-project-button" @click="triggerFileInput">新增场景</button>
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
    <!-- 输入项目名称弹窗（不影响原布局） -->
    <div v-if="showModal" class="modal-mask">
      <div class="modal-box">
        <h3>请输入场景名称</h3>
        <input v-model="newProjectName" class="modal-input" placeholder="场景名称" />
        <div class="modal-actions">
          <button @click="cancelImport">取消</button>
          <button @click="confirmImport">确认</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import { getProjects } from '@/api/login'

export default {
  data () {
    return {
      allProjects: [],

      // 新增字段（不影响原结构）
      showModal: false,
      newProjectName: '',
      uploadMeshData: null // 存储从 JSON 文件中解析出的 meshInfo 列表
    }
  },

  created () {
    this.fetchProjects()
  },

  methods: {
    selectProject (projectId) {
      localStorage.setItem('project_id', projectId)

      // ID 2 对应“永德社区”
      const isYongdeCommunity = projectId === 2
      const isYongdePark = projectId === 3

      if (isYongdeCommunity) {
        // 🎯 目标：点击 ID 为 2 的项目时，携带参数跳转到社区网格 (F-community)
        this.$router.push({
          path: '/space-scene',
          query: { initialMeshType: 'F-community' }
        })
      } else if (isYongdePark) {
        // 跳转到园区网格 (F-park)
        this.$router.push({
          path: '/space-scene',
          query: { initialMeshType: 'F-park' }
        })
      } else {
        // 其他项目，正常跳转
        this.$router.push({ path: '/space-scene' })
      }
    },

    async fetchProjects () {
      try {
        const fetchedProjects = await getProjects()
        let projectsToDisplay = fetchedProjects

        // 检查后端数据是否已经包含 ID=3 的项目
        const hasParkProject = fetchedProjects.some(p => p.projectId === 3)

        // 如果后端数据中缺少 ID=3 的项目，我们手动添加一个模拟对象
        if (!hasParkProject) {
          // 假设项目ID 3 是下一个可用的 ID
          const parkProject = {
            projectId: 3,
            projectName: '占位符 - 永德园区' // 暂时使用占位符名称
            // 其他可能需要的字段，例如：
            // projectCode: 'park-scene'
          }
          projectsToDisplay = [...fetchedProjects, parkProject]
        }
        this.allProjects = projectsToDisplay.map((project) => {
          if (project.projectId === 1) {
            project.projectName = '永德城区'
            project.image = require('@/assets/commercial.jpg')
          } else if (project.projectId === 2) {
            project.projectName = '永德社区'
            project.image = require('@/assets/residential.jpg')
          } else if (project.projectId === 3) {
            // 无论是后端返回的还是模拟的，在这里统一设置最终展示的名称和图片
            project.projectName = '永德园区'
            project.image = require('@/assets/Park.jpg')
          }
          return project
        })
      } catch (error) {
        console.error('从 API 获取项目数据失败:', error)
      }
    },

    triggerFileInput () {
      this.$nextTick(() => {
        this.$refs.fileInput && this.$refs.fileInput.click()
      })
    },

    // JSON 文件解析
    async handleFileChange (event) {
      const file = event.target.files[0]
      // 清空文件输入框，防止用户选择同一个文件不触发 change 事件
      event.target.value = ''
      if (!file) return

      if (!file.name.endsWith('.json')) {
        alert('请选择 JSON 文件')
        return
      }

      try {
        const text = await file.text()
        const json = JSON.parse(text)

        // 检查 JSON 根结构是否符合预期
        if (!json.data || !Array.isArray(json.data)) {
          alert('JSON 格式错误：缺少 data 数组')
          return
        }

        // 1. 从 data 数组中提取 meshInfo 列表
        // 确保每个 item 都有 meshInfo 字段
        this.uploadMeshData = json.data
          .filter(item => item.meshInfo) // 过滤掉没有 meshInfo 的项
          .map(item => item.meshInfo) // 提取 meshInfo 对象

        if (this.uploadMeshData.length === 0) {
          alert('JSON 文件中没有有效的网格数据 (meshInfo)')
          return
        }

        // 2. 打开项目名输入弹窗
        this.showModal = true
      } catch (err) {
        console.error('JSON 解析失败:', err)
        alert('JSON 文件解析失败')
      }
    },

    // 用户确认导入
    async confirmImport () {
      if (!this.newProjectName.trim()) {
        alert('场景名称不能为空')
        return
      }

      if (!this.uploadMeshData) {
        alert('网格数据为空，请重新上传文件')
        return
      }

      try {
        // 🚀 修改 API 路径和请求体以匹配后端处理逻辑
        // 请求体包含场景名称和解析出的网格数据列表
        await axios.post('/api/projects/addSceneWithMeshes', {
          // 场景的基本信息
          projectName: this.newProjectName,
          // 网格数据列表，后端接收后需分别处理保存
          meshes: this.uploadMeshData
        })

        alert('场景导入成功！')

        // 重置状态
        this.showModal = false
        this.newProjectName = ''
        this.uploadMeshData = null

        // 刷新项目列表
        this.fetchProjects()
      } catch (error) {
        // 打印详细错误信息
        console.error('场景导入失败:', error.response ? error.response.data : error.message)
        alert(`场景导入失败：${error.response ? error.response.data.message : '请检查后端服务'}`)
      }
    },

    cancelImport () {
      this.showModal = false
      this.newProjectName = ''
      this.uploadMeshData = null
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
  padding: 30px 60px;
  /*grid-template-columns: repeat(3, 1fr); /* 每行显示2个项目 */
  grid-template-columns: 250px 250px 250px;

  /* 确保整个网格容器在父容器中居中显示 */
  justify-content: center;
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
/* 最小遮罩层 */
.modal-mask {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0,0,0,0.35);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10000;
}

/* 中间的小白框 */
.modal-box {
  background: #fff;
  padding: 20px;
  border-radius: 6px;
  width: 280px;
  text-align: center;
}

.modal-input {
  width: 100%;
  padding: 8px;
  margin-top: 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.modal-actions {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
}

.modal-actions button {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.modal-actions button:last-child {
  background: #184aa1;
  color: #fff;
}
</style>
