<template>
  <div class="main">
    <h1 class="center-text">场景选择</h1>

    <div class="button-wrapper">
      <button class="add-project-button" @click="openTypeDialog">新增场景</button>
      <button
        class="delete-mode-button"
        :class="{ 'active-delete': isDeleteMode }"
        @click="isDeleteMode = !isDeleteMode"
      >
        {{ isDeleteMode ? '取消删除' : '删除场景' }}
      </button>
    </div>

    <div class="project-grid">
      <div
        v-for="(project) in allProjects"
        :key="project.projectId"
        class="project-item"
        @click="handleProjectClick(project.projectId)"
      >
        <div
          v-if="isDeleteMode"
          class="delete-badge"
          @click.stop="confirmDelete(project)"
        >
          ×
        </div>

        <div class="item-name">
          <p>{{ project.projectName || '未命名场景' }}</p>
        </div>

        <img :src="project.image" alt="Scene Image" class="item-image" />
      </div>
    </div>

    <div v-if="showTypeModal" class="modal-mask">
      <div class="modal-box">
        <h3>第一步：选择场景</h3>
        <select v-model="selectedSceneType" class="modal-select">
          <option disabled value="">请选择场景类型</option>
          <option v-for="opt in sceneOptions" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </option>
        </select>

        <div class="modal-actions">
          <button @click="showTypeModal = false" :disabled="loading">取消</button>
          <button @click="handleFetchData" :disabled="loading" class="primary-btn">
            {{ loading ? '数据获取中...' : '下一步: 获取数据' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="showPreviewModal" class="modal-mask">
      <div class="modal-box large-modal">
        <h3>第二步：确认网格数据</h3>
        <p class="modal-hint">已从接口获取 {{ previewData.length }} 条网格数据。</p>

        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th width="20%">网格名称</th>
                <th width="15%">类型</th>
                <th width="15%">面积(m²)</th>
                <th width="50%">网格中心点</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in previewData" :key="item.projectId">
                <td>{{ item.projectName }}</td>
                <td>{{ item.meshData.type }}</td>
                <td>{{ item.meshData.area.toFixed(2) }}</td>
                <td :title="item.rawAddress">{{ item.rawAddress || '暂无' }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="modal-actions">
          <button @click="cancelPreview" :disabled="importing">放弃</button>

          <button
            @click="confirmImportFinal"
            class="confirm-btn"
            :disabled="importing"
          >
            <span v-if="importing">导入中...</span>
            <span v-else>确认导入系统</span>
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script>
import { addScene } from '@/api/manage'
import { getProjects } from '@/api/login'
import DefaultSceneImg from '@/assets/DefaultSceneImg.png'

export default {
  data () {
    return {
      allProjects: [],
      isDeleteMode: false,
      loading: false,
      importing: false,

      // 弹窗控制状态
      showTypeModal: false,
      showPreviewModal: false,

      // 数据状态
      selectedSceneType: '',
      previewData: [],

      // 静态选项
      sceneOptions: [
        { label: '永德城区', value: 'F-city' },
        { label: '永德社区', value: 'F-community' },
        { label: '永德园区', value: 'F-park' }
      ]
    }
  },

  created () {
    this.fetchProjects()
  },

  methods: {
    // ----------------------------------------------------
    // 工具函数
    // ----------------------------------------------------
    parseRemarksPoints (remarksStr) {
      if (!remarksStr) return []
      const matches = remarksStr.match(/\[(.*?)\]/g)
      if (!matches) return []
      return matches.map(item => {
        const parts = item.replace(/[[\]]/g, '').split(',')
        return parts.map(num => parseFloat(num))
      })
    },

    getSceneNameByType (type) {
      const map = { 'F-city': '永德城区', 'F-community': '永德社区', 'F-park': '永德园区' }
      return map[type] || '未知场景'
    },

    getSceneImageByType (type) {
      if (type === 'F-city') return require('@/assets/commercial.jpg')
      if (type === 'F-community') return require('@/assets/residential.jpg')
      if (type === 'F-park') return require('@/assets/Park.jpg')
      return DefaultSceneImg
    },

    // ----------------------------------------------------
    // 核心业务逻辑
    // ----------------------------------------------------

    openTypeDialog () {
      this.selectedSceneType = ''
      this.showTypeModal = true
      this.showPreviewModal = false
    },

    async handleFetchData () {
      if (!this.selectedSceneType) {
        alert('请先选择一个场景类型')
        return
      }

      this.loading = true
      try {
        // 调用接口
        const res = await addScene(this.selectedSceneType)
        let isSuccess = false
        let dataList = []

        if (res && res.success === true) {
          // 情况A: res 就是响应体
          isSuccess = true
          dataList = res.data
        } else if (res && res.data && res.data.success === true) {
          // 情况B: res 是 Axios 原始对象
          isSuccess = true
          dataList = res.data.data
        }

        if (isSuccess) {
          const rawList = dataList || []

          this.previewData = rawList.map(item => {
            const pointList = this.parseRemarksPoints(item.remarks)
            let img = DefaultSceneImg
            if (item.meshNature === 'F-city') img = require('@/assets/commercial.jpg')
            else if (item.meshNature === 'F-community') img = require('@/assets/residential.jpg')
            else if (item.meshNature === 'F-park') img = require('@/assets/Park.jpg')

            return {
              projectId: item.id,
              projectName: item.meshName,
              image: img,
              rawAddress: item.address,
              meshData: {
                points: pointList,
                area: item.meshArea,
                type: item.meshNature
              }
            }
          })

          this.showTypeModal = false
          this.showPreviewModal = true
        } else {
          alert('获取数据失败: 状态不正确')
        }
      } catch (error) {
        console.error('API Error:', error)
        alert('网络请求异常')
      } finally {
        this.loading = false
      }
    },

    confirmImportFinal () {
      const isExist = this.allProjects.some(p => p.meshData?.type === this.selectedSceneType)

      if (isExist) {
        alert(`场景 "${this.getSceneNameByType(this.selectedSceneType)}" 已存在，请勿重复添加！`)
        return
      }

      this.importing = true

      setTimeout(() => {
        const newScene = {
          projectId: `scene-${this.selectedSceneType}-${Date.now()}`,
          projectName: this.getSceneNameByType(this.selectedSceneType),
          image: this.getSceneImageByType(this.selectedSceneType),
          meshData: {
            type: this.selectedSceneType,
            grids: this.previewData
          }
        }

        this.allProjects.push(newScene)
        this.$emit('scene-added', newScene)

        this.importing = false
        this.showPreviewModal = false
        this.previewData = []
        alert('导入成功！')
      }, 600)
    },

    cancelPreview () {
      this.showPreviewModal = false
      this.previewData = []
      this.showTypeModal = true
    },

    async fetchProjects () {
      try {
        const fetchedProjects = await getProjects()

        this.allProjects = fetchedProjects.map((project) => {
          const currentId = project.project_id ? Number(project.project_id) : Number(project.projectId)
          let imagePath = project.image
          let name = project.projectName

          let type = ''
          if (currentId === 1) {
            name = '永德城区'; imagePath = require('@/assets/commercial.jpg'); type = 'F-city'
          } else if (currentId === 2) {
            name = '永德社区'; imagePath = require('@/assets/residential.jpg'); type = 'F-community'
          } else if (currentId === 3) {
            name = '永德园区'; imagePath = require('@/assets/Park.jpg'); type = 'F-park'
          }

          return {
            ...project,
            projectId: currentId,
            projectName: name || '新导入场景',
            image: imagePath || DefaultSceneImg,
            meshData: {
              type: type,
              grids: []
            }
          }
        })
      } catch (error) {
        console.error('获取项目数据失败:', error)
      }
    },

    handleProjectClick (projectId) {
      if (this.isDeleteMode) {
        const isSystem = typeof projectId === 'number' && projectId <= 3
        if (!isSystem) {
          const project = this.allProjects.find(p => p.projectId === projectId)
          this.confirmDelete(project)
        } else {
          alert('系统内置场景不可删除')
        }
      } else {
        this.selectProject(projectId)
      }
    },

    selectProject (projectId) {
      localStorage.setItem('project_id', projectId)
      const project = this.allProjects.find(p => p.projectId === projectId)
      const type = project?.meshData?.type || 'F-city'

      this.$router.push({
        path: '/space-scene',
        query: { initialMeshType: type }
      })
    },

    async confirmDelete (project) {
      if (confirm(`确定要移除场景 "${project.projectName}" 吗？`)) {
        this.allProjects = this.allProjects.filter(p => p.projectId !== project.projectId)
        alert('移除成功')
      }
    }
  }
}
</script>

<style scoped>
.main {
  text-align: center;
  max-width: 1200px;
  margin: 0 auto;
}
.center-text {
  color: #184aa1;
  text-align: center;
  margin-top: 40px;
}
.button-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
  gap: 15px;
  padding-right: 60px;
}
.add-project-button {
  padding: 10px 20px;
  background-color: #184aa1;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  transition: background 0.3s;
}
.add-project-button:hover { background-color: #0c3275; }
.delete-mode-button {
  padding: 10px 20px;
  background-color: #d9534f;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  transition: all 0.3s;
}
.delete-mode-button:hover { background-color: #c9302c; }
.active-delete { background-color: #555; transform: scale(0.95); }

.project-grid {
  display: grid;
  padding: 30px 60px;
  /* 强制固定为 3 列，每列 250px */
  grid-template-columns: 250px 250px 250px;
  justify-content: center;
  gap: 30px;
}

.project-item {
  /* 这里不需要写 width，因为 grid 会控制列宽 */
  cursor: pointer;
  border-radius: 10px;
  overflow: visible;
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s;
  height: 260px;
  position: relative;
  background: #fff;
}

.item-image {
  width: 100%;
  height: 210px;
  object-fit: cover;
  border-radius: 10px 10px 0 0;
  border-bottom: 2px solid #184aa1;
}

.item-name {
  background-color: rgba(0, 0, 0, 0.75);
  color: white;
  padding: 0 10px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  bottom: 0;
  width: 100%;
  border-radius: 0 0 10px 10px;
}
.item-name p {
  margin: 0;
  font-size: 1.1em;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.delete-badge {
  position: absolute;
  top: -10px;
  right: -10px;
  width: 28px;
  height: 28px;
  background-color: #d9534f;
  color: white;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
}
.delete-badge:hover { background-color: red; }

/* 弹窗样式 */
.modal-mask {
  position: fixed;
  left: 0; top: 0; right: 0; bottom: 0;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}
.modal-box {
  background: #fff;
  padding: 25px;
  border-radius: 8px;
  width: 320px;
  text-align: center;
  box-shadow: 0 10px 25px rgba(0,0,0,0.3);
}
.modal-hint {
  color: #666;
  font-size: 0.9em;
  margin-bottom: 15px;
}
.modal-select {
  width: 100%;
  padding: 10px;
  margin: 10px 0 20px 0;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}
.modal-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
}
.modal-actions button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: #f5f5f5;
  border-radius: 4px;
  cursor: pointer;
}
.primary-btn, .confirm-btn {
  background: #184aa1 !important;
  color: #fff !important;
  border: none !important;
}
button:disabled {
  background: #ccc !important;
  color: #666 !important;
  cursor: not-allowed;
}

/* 预览表格弹窗 */
.large-modal {
  width: 800px !important;
  max-width: 90vw;
  display: flex;
  flex-direction: column;
  max-height: 85vh;
}
.table-container {
  flex: 1;
  overflow-y: auto;
  margin: 15px 0;
  border: 1px solid #eee;
  border-radius: 4px;
}
.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
.data-table th, .data-table td {
  padding: 12px;
  border-bottom: 1px solid #eee;
  text-align: left;
}
.data-table th {
  background-color: #f7f8fa;
  color: #333;
  font-weight: 600;
  position: sticky;
  top: 0;
  z-index: 1;
}
.data-table tr:hover {
  background-color: #f0f7ff;
}
/* 滚动条 */
.table-container::-webkit-scrollbar { width: 6px; }
.table-container::-webkit-scrollbar-thumb { background: #ccc; border-radius: 3px; }
</style>
