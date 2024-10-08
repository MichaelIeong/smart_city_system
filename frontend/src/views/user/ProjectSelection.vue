<template>
  <div class="main">
    <!-- 使用 $t() 函数来引用多语言键值 -->
    <h2>{{ $t('menu.projectSelection') }}</h2>

    <!-- 导入按钮，点击后显示模态框 -->
    <a-button type="primary" icon="plus" @click="showImportModal = true">新建</a-button>

    <!-- 项目导入模态框 -->
    <div v-if="showImportModal" class="modal">
      <div class="modal-content">
        <h3>导入项目</h3>

        <!-- 表单 -->
        <form @submit.prevent="importProject">
          <div>
            <label for="project-name">项目名称:</label>
            <input type="text" v-model="newProject.name" id="project-name" required />
          </div>

          <div>
            <label for="project-zip">选择zip文件:</label>
            <input type="file" @change="handleFileUpload" id="project-zip" accept=".zip" required />
          </div>

          <div class="modal-actions">
            <a-button type="primary" htmlType="submit">确定</a-button>
            <a-button @click="showImportModal = false">取消</a-button>
          </div>
        </form>
      </div>
    </div>

    <!-- 项目列表 -->
    <div class="project-grid">
      <div
        v-for="(project) in allProjects"
        :key="project.id"
        class="project-item"
        @click="selectProject(project.id)"
      >
        <img :src="project.image" alt="Project Image" class="item-image" />
        <div class="item-name">{{ project.name }}</div>
      </div>
    </div>
  </div>
</template>

<script>
import { postProject } from '@/api/manage'
export default {
  data () {
    return {
      showImportModal: false, // 控制模态框显示隐藏
      newProject: {
        name: '',
        file: null
      },
      allProjects: [
        // 示例项目数据
        { id: 'p1', name: '项目一', image: 'https://via.placeholder.com/800x400.png?text=项目一' },
        { id: 'p2', name: '项目二', image: 'https://via.placeholder.com/800x400.png?text=项目二' }
        // 其他项目数据...
      ]
    }
  },
  methods: {
    handleFileUpload (event) {
      this.newProject.file = event.target.files[0]
    },
    async importProject () {
      try {
        if (!this.newProject.file) {
          alert('请选择文件')
          return
        }

        // 使用 FormData 构建请求数据
        const formData = new FormData()
        formData.append('name', this.newProject.name)
        formData.append('file', this.newProject.file)

        // 发送 POST 请求到后端，调用 postProject
        const response = await postProject(formData)

        // 处理响应
        console.log('导入成功', response.data)
        this.showImportModal = false // 关闭模态框
        // 可在此处刷新项目列表
      } catch (error) {
        console.error('导入失败', error)
      }
    }
  }
}
</script>

<style scoped>
.main {
  padding: 20px;
}

.project-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  padding: 20px;
}

.project-item {
  cursor: pointer;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
}

.project-item:hover {
  transform: translateY(-5px);
}

.item-image {
  width: 100%;
  height: auto;
  border-bottom: 2px solid #007bff;
}

.item-name {
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 10px;
  font-size: 1.2em;
  text-align: center;
}

/* 模态框样式 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-content {
  background-color: white;
  padding: 20px;
  border-radius: 10px;
  width: 400px;
}

.modal-actions {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
}
</style>
