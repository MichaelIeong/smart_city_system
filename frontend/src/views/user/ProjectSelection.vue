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

        <select
          v-model="selectedSceneType"
          class="modal-select"
          :disabled="dictLoading"
        >
          <option disabled value="">
            {{ dictLoading ? '正在加载选项...' : '请选择场景类型' }}
          </option>

          <option
            v-for="opt in sceneOptions"
            :key="opt.value"
            :value="opt.value"
          >
            {{ opt.label }}
          </option>
        </select>

        <div v-if="currentSelectionImage" class="preview-image-box">
          <p class="preview-label">场景预览：</p>
          <img :src="currentSelectionImage" alt="场景预览" class="scene-preview-img" />
        </div>

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
import { addScene, getSceneTypeDict } from '@/api/manage'
import { getProjects } from '@/api/login'
import DefaultSceneImg from '@/assets/DefaultSceneImg.png'

export default {
  data () {
    return {
      allProjects: [],
      isDeleteMode: false,
      loading: false,
      importing: false,
      dictLoading: false,

      showTypeModal: false,
      showPreviewModal: false,

      selectedSceneType: '',
      previewData: [],
      sceneOptions: []
    }
  },

  computed: {
    // 自动获取预览图
    currentSelectionImage () {
      if (!this.selectedSceneType) return null
      const option = this.sceneOptions.find(opt => opt.value === this.selectedSceneType)
      return option ? option.image : null
    }
  },

  created () {
    this.fetchSceneOptions()
    this.loadProjectsFromLocal()
  },

  methods: {
    // ----------------------------------------------------
    // 本地缓存与工具
    // ----------------------------------------------------
    loadProjectsFromLocal () {
      const savedProjects = localStorage.getItem('my_scene_list')
      if (savedProjects) {
        try {
          this.allProjects = JSON.parse(savedProjects)
        } catch (e) {
          console.error('本地缓存解析失败', e)
          this.allProjects = []
        }
      } else {
        this.allProjects = []
      }
    },

    saveProjectsToLocal () {
      localStorage.setItem('my_scene_list', JSON.stringify(this.allProjects))
    },

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
    // 业务逻辑：字典与网格获取
    // ----------------------------------------------------
    async fetchSceneOptions () {
      this.dictLoading = true
      try {
        const res = await getSceneTypeDict()
        const responseBody = res.success ? res : (res.data || {})

        if (responseBody.success) {
          const rootData = responseBody.data || {}
          const dictList = rootData.items || []

          // 名称映射
          const nameMap = {
            'F-city': '永德城区',
            'F-community': '永德社区',
            'F-park': '永德园区'
          }

          this.sceneOptions = dictList.map(item => ({
            label: nameMap[item.dictKey] || item.dictValue,
            value: item.dictKey,
            image: item.dictDesc
          }))
        } else {
          console.error('字典数据获取失败:', responseBody.message)
        }
      } catch (error) {
        console.error('字典接口请求异常:', error)
      } finally {
        this.dictLoading = false
      }
    },

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
        const res = await addScene(this.selectedSceneType)
        let isSuccess = false
        let dataList = []

        if (res && res.success === true) {
          isSuccess = true
          dataList = res.data
        } else if (res && res.data && res.data.success === true) {
          isSuccess = true
          dataList = res.data.data
        }

        if (isSuccess) {
          const rawList = dataList || []
          this.previewData = rawList.map(item => {
            const pointList = this.parseRemarksPoints(item.remarks)
            // 预览图逻辑
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
        alert(`该场景已存在，请勿重复添加！`)
        return
      }

      this.importing = true

      setTimeout(() => {
        const selectedOption = this.sceneOptions.find(opt => opt.value === this.selectedSceneType)

        // 类型 -> 后端 ID 映射
        const typeToIdMap = {
          'F-city': 1,
          'F-community': 2,
          'F-park': 3
        }
        const realSystemId = typeToIdMap[this.selectedSceneType] || 1

        const dynamicImage = selectedOption ? selectedOption.image : this.getSceneImageByType(this.selectedSceneType)
        const sceneName = selectedOption ? selectedOption.label : this.getSceneNameByType(this.selectedSceneType)

        const newScene = {
          // 前端 ID：唯一字符串
          projectId: `scene-${this.selectedSceneType}-${Date.now()}`,
          // 后端 ID：数字 (1, 2, 3)
          systemId: realSystemId,
          projectName: sceneName,
          image: dynamicImage,
          meshData: {
            type: this.selectedSceneType,
            grids: this.previewData
          }
        }

        this.allProjects.push(newScene)
        this.saveProjectsToLocal()
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

    handleProjectClick (projectId) {
      if (this.isDeleteMode) {
        const project = this.allProjects.find(p => p.projectId === projectId)
        if (project) {
          // 如果是系统内置场景(ID<=3)，看需求是否允许删除，这里暂时允许
          this.confirmDelete(project)
        }
      } else {
        this.selectProject(projectId)
      }
    },

    selectProject (projectId) {
      const project = this.allProjects.find(p => p.projectId === projectId)

      if (project) {
        // 1. 获取场景类型
        const type = project.meshData?.type || 'F-city'

        // 2. 获取后端 ID 并存入
        let apiId = 1
        if (type === 'F-city') apiId = 1
        else if (type === 'F-community') apiId = 2
        else if (type === 'F-park') apiId = 3
        else apiId = project.systemId || project.projectId

        localStorage.setItem('project_id', apiId)

        localStorage.setItem('current_scene_type', type)

        // 4. 跳转到“环境表征”页面 (根据您的路由，应该是 /space-scene)
        this.$router.push({
          path: '/space-scene'

        })
      }
    },

    async confirmDelete (project) {
      if (confirm(`确定要移除场景 "${project.projectName}" 吗？`)) {
        this.allProjects = this.allProjects.filter(p => p.projectId !== project.projectId)
        this.saveProjectsToLocal()
      }
    },

    async fetchProjects () {
      // 只有在需要拉取默认数据时才调用
      try {
        const fetchedProjects = await getProjects()

        this.allProjects = fetchedProjects.map((project) => {
          // 处理 project_id / projectId 并转为 Number
          const currentId = project.project_id ? Number(project.project_id) : Number(project.projectId)

          let imagePath = project.image
          let name = project.projectName
          let type = ''

          // 根据 ID 设置默认信息
          if (currentId === 1) { name = '永德城区'; imagePath = require('@/assets/commercial.jpg'); type = 'F-city' } else if (currentId === 2) { name = '永德社区'; imagePath = require('@/assets/residential.jpg'); type = 'F-community' } else if (currentId === 3) { name = '永德园区'; imagePath = require('@/assets/Park.jpg'); type = 'F-park' }

          return {
            ...project,
            // 赋值处理后的 ID
            projectId: currentId,
            // ID 赋给 systemId
            systemId: currentId,

            projectName: name || '新导入场景',
            image: imagePath || DefaultSceneImg,
            meshData: {
              type: type,
              grids: []
            }
          }
        })
        // 拉取完默认值后保存到本地
        this.saveProjectsToLocal()
      } catch (error) {
        console.error('获取项目数据失败:', error)
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

/* ✅ 保持你要求的 Grid 布局 (3列固定) */
.project-grid {
  display: grid;
  padding: 30px 60px;
  /* 强制固定为 3 列，每列 250px */
  grid-template-columns: 250px 250px 250px;
  justify-content: center;
  gap: 30px;
}

.project-item {
  /* 不需要写 width，因为 Grid 控制了列宽 */
  height: 260px;
  cursor: pointer;
  border-radius: 10px;
  overflow: visible;
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
  transition: transform 0.3s;
  position: relative;
  background: #fff;
}
.project-item:hover { transform: translateY(-5px); }

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
  width: 340px;
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
  margin: 10px 0 10px 0;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
}

/* 预览图片容器样式 */
.preview-image-box {
  margin: 10px 0 20px 0;
  text-align: center;
  background: #f9f9f9;
  padding: 10px;
  border-radius: 6px;
  border: 1px dashed #ddd;
}
.preview-label {
  font-size: 12px;
  color: #888;
  margin-bottom: 8px;
  text-align: left;
}
.scene-preview-img {
  max-width: 100%;
  max-height: 150px;
  border-radius: 4px;
  object-fit: cover;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
  display: block;
  margin: 0 auto;
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
