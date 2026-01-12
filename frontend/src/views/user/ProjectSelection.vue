<template>
  <div class="main">
    <h1 class="center-text">{{ $t('menu.projectSelection') }}</h1>

    <div class="button-wrapper">
      <button class="add-project-button" @click="triggerFileInput">新增场景</button>
      <button
        class="delete-mode-button"
        :class="{ 'active-delete': isDeleteMode }"
        @click="isDeleteMode = !isDeleteMode"
      >
        {{ isDeleteMode ? '取消删除' : '删除场景' }}
      </button>

      <input type="file" ref="fileInput" @change="handleFileChange" style="display: none;" />
    </div>

    <div class="project-grid">
      <div
        v-for="(project) in allProjects"
        :key="project.projectId"
        class="project-item"
        @click="handleProjectClick(project.projectId)"
      >
        <div
          v-if="isDeleteMode && project.projectId > 3"
          class="delete-badge"
          @click.stop="confirmDelete(project)"
        >
          ×
        </div>

        <div class="item-name">
          <p>{{ project.projectName || '无项目名称' }}</p>
        </div>

        <img :src="project.image" alt="Project Image" class="item-image" />
      </div>
    </div>

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
import DefaultSceneImg from '@/assets/DefaultSceneImg.png'

export default {
  data () {
    return {
      allProjects: [],
      isDeleteMode: false,
      showModal: false,
      newProjectName: '',
      uploadMeshData: null
    }
  },

  created () {
    this.fetchProjects()
  },

  methods: {
    // 1. 获取并处理项目数据
    async fetchProjects () {
      try {
        const fetchedProjects = await getProjects()

        this.allProjects = fetchedProjects.map((project) => {
          const currentId = project.project_id ? Number(project.project_id) : Number(project.projectId)

          let imagePath = project.image
          let name = project.projectName

          // 静态资源映射
          if (currentId === 1) {
            name = '永德城区'
            imagePath = require('@/assets/commercial.jpg')
          } else if (currentId === 2) {
            name = '永德社区'
            imagePath = require('@/assets/residential.jpg')
          } else if (currentId === 3) {
            name = '永德园区'
            imagePath = require('@/assets/Park.jpg')
          }

          // 处理新增场景默认图
          const isNewScene = currentId > 3
          if (isNewScene && !imagePath) {
            imagePath = DefaultSceneImg
          }

          return {
            ...project,
            projectId: currentId, // 统一存为驼峰格式供前端使用
            projectName: name || '新导入场景',
            image: imagePath || DefaultSceneImg
          }
        })
      } catch (error) {
        console.error('获取项目数据失败:', error)
      }
    },

    // 2. 处理点击事件
    handleProjectClick (projectId) {
      if (this.isDeleteMode) {
        if (projectId > 3) {
          const project = this.allProjects.find(p => p.projectId === projectId)
          this.confirmDelete(project)
        } else {
          alert('系统内置场景不可删除')
        }
      } else {
        this.selectProject(projectId)
      }
    },

    // 3. 执行跳转逻辑
    selectProject (projectId) {
      // 存储 ID
      localStorage.setItem('project_id', projectId)

      // 【关键修正】：确保比较的是数字类型
      const id = Number(projectId)
      console.log('正在跳转，场景ID:', id) // 调试用

      if (id === 2) {
        // 社区
        this.$router.push({
          path: '/space-scene',
          query: { initialMeshType: 'F-community' }
        })
      } else if (id === 3) {
        // 园区
        this.$router.push({
          path: '/space-scene',
          query: { initialMeshType: 'F-park' }
        })
      } else {
        // 默认（城区）
        this.$router.push({
          path: '/space-scene',
          query: { initialMeshType: 'F-city' }
        })
      }
    },

    // 4. 删除逻辑
    async confirmDelete (project) {
      if (confirm(`确定要永久删除场景 "${project.projectName}" 吗？`)) {
        try {
          // 这里根据后端要求决定传 projectId 还是 project_id
          await axios.delete(`/api/projects/${project.projectId}`)
          alert('删除成功')
          this.fetchProjects()
        } catch (error) {
          console.error('删除项目失败:', error)
          alert('删除失败')
        }
      }
    },

    // 其余上传逻辑保持不变...
    triggerFileInput () {
      this.$nextTick(() => { this.$refs.fileInput && this.$refs.fileInput.click() })
    },
    async handleFileChange (event) {
      const file = event.target.files[0]
      event.target.value = ''
      if (!file || !file.name.endsWith('.json')) return
      try {
        const text = await file.text()
        const json = JSON.parse(text)
        this.uploadMeshData = json.data.filter(item => item.meshInfo).map(item => item.meshInfo)
        this.showModal = true
      } catch (err) { alert('JSON解析失败') }
    },
    async confirmImport () {
      if (!this.newProjectName.trim()) return
      try {
        await axios.post('/api/projects/importJson', {
          projectName: this.newProjectName,
          meshes: this.uploadMeshData
        })
        this.showModal = false
        this.fetchProjects()
      } catch (error) { alert('导入失败') }
    },
    cancelImport () {
      this.showModal = false
      this.newProjectName = ''
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

.button-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
  gap: 15px; /* 🚀 增加按钮间距 */
}

/* 🚀 新增：删除场景按钮样式 */
.delete-mode-button {
  padding: 10px 20px;
  background-color: #d9534f; /* 红色背景 */
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  font-size: 1em;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.delete-mode-button:hover {
  background-color: #c9302c;
}

.active-delete {
  background-color: #444; /* 激活时变为深灰色提示取消 */
  transform: scale(0.95);
}

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

/* 🚀 新增：卡片上的红色叉号角标 */
.delete-badge {
  position: absolute;
  top: -10px;
  right: -10px;
  width: 30px;
  height: 30px;
  background-color: #d9534f;
  color: white;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
  z-index: 100;
  box-shadow: 0 2px 5px rgba(0,0,0,0.3);
  cursor: pointer;
}

.delete-badge:hover {
  background-color: #ff0000;
  transform: scale(1.1);
}

.project-grid {
  display: grid;
  padding: 30px 60px;
  grid-template-columns: 250px 250px 250px;
  justify-content: center;
  gap: 30px;
}

.project-item {
  cursor: pointer;
  border-radius: 10px;
  overflow: visible; /* 🚀 修改为visible以显示删除角标 */
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s;
  width: 100%;
  height: 260px;
  position: relative;
}

.project-item:hover {
  transform: translateY(-5px);
}

.item-image {
  width: 100%;
  height: 210px;
  border-bottom: 2px solid #184aa1;
  border-radius: 10px 10px 0 0;
}

.item-name {
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 8px;
  font-size: 1.4em;
  height: 50px;
  text-align: center;
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  border-radius: 0 0 10px 10px;
}

/* 弹窗相关样式保留不变... */
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
