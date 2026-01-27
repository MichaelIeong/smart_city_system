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

    <div v-if="importing" class="modal-mask">
      <div class="modal-box" style="width: 400px; padding: 40px;">
        <h3 style="margin-bottom: 20px;">正在导入场景设备...</h3>

        <div class="progress-container">
          <div
            class="progress-bar"
            :style="{ width: importProgress + '%' }"
          ></div>
        </div>

        <p style="margin-top: 15px; color: #666;">
          当前进度：{{ importProgress }}%
        </p>
      </div>
    </div>

  </div>
</template>

<script>
import { addScene, getSceneTypeDict, deleteProjectById, batchAddDevices } from '@/api/manage'
import { getProjects } from '@/api/login'
import DefaultSceneImg from '@/assets/DefaultSceneImg.png'
import { message } from 'ant-design-vue'
import localDeviceData from '@/assets/tsl_devices.json'

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
      sceneOptions: [],

      // ✅ 新增：进度百分比
      importProgress: 0
    }
  },

  computed: {
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

    async fetchSceneOptions () {
      this.dictLoading = true
      try {
        const res = await getSceneTypeDict()
        const responseBody = res.success ? res : (res.data || {})

        if (responseBody.success) {
          const rootData = responseBody.data || {}
          const dictList = rootData.items || []

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
        message.error('请先选择场景类型')
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
          message.error('获取数据失败：状态不正确')
        }
      } catch (error) {
        console.error('API Error:', error)
        message.error('网络请求异常')
      } finally {
        this.loading = false
      }
    },

    // ✅ 重写：包含分片上传和进度条逻辑
    confirmImportFinal () {
      const isExist = this.allProjects.some(p => p.meshData?.type === this.selectedSceneType)
      if (isExist) {
        message.error('该场景已存在，请勿重复添加！')
        return
      }

      this.importing = true
      this.importProgress = 0 // 重置进度

      setTimeout(async () => {
        try {
          const selectedOption = this.sceneOptions.find(opt => opt.value === this.selectedSceneType)
          const typeToIdMap = { 'F-city': 1, 'F-community': 2, 'F-park': 3 }
          const realSystemId = typeToIdMap[this.selectedSceneType] || 1

          const dynamicImage = selectedOption ? selectedOption.image : this.getSceneImageByType(this.selectedSceneType)
          const sceneName = selectedOption ? selectedOption.label : this.getSceneNameByType(this.selectedSceneType)

          const newScene = {
            projectId: `scene-${this.selectedSceneType}-${Date.now()}`,
            systemId: realSystemId,
            projectName: sceneName,
            image: dynamicImage,
            meshData: {
              type: this.selectedSceneType,
              grids: this.previewData
            }
          }

          // --- 设备同步逻辑 (分片版) ---
          console.log(`正在筛选场景 [${this.selectedSceneType}] 的设备...`)

          const targetDevices = localDeviceData.filter(device =>
            device.mesh_nature === this.selectedSceneType
          )

          if (targetDevices.length > 0) {
            console.log(`找到 ${targetDevices.length} 个设备，准备分批上传...`)

            const allDevicesToSend = targetDevices.map(d => ({
              id: d.id,
              projectId: realSystemId,
              deviceName: d.device_name,
              deviceId: d.device_id,
              productId: d.product_id,
              status: d.status,
              meshId: d.mesh_id,
              meshNo: d.mesh_no,
              meshName: d.mesh_name,
              meshNature: d.mesh_nature,
              meshArea: d.mesh_area,
              address: d.address,
              createdAt: d.created_at
            }))

            // ✅ 分片逻辑
            const BATCH_SIZE = 200
            const totalCount = allDevicesToSend.length
            const totalBatches = Math.ceil(totalCount / BATCH_SIZE)

            for (let i = 0; i < totalBatches; i++) {
              const start = i * BATCH_SIZE
              const end = Math.min((i + 1) * BATCH_SIZE, totalCount)
              const batchData = allDevicesToSend.slice(start, end)

              await batchAddDevices(batchData)

              // 更新进度条
              this.importProgress = Math.round(((i + 1) / totalBatches) * 100)

              // 可选：稍微停顿让UI渲染
              // await new Promise(resolve => setTimeout(resolve, 20))
            }
            console.log('所有批次上传完成')
          } else {
            console.log('本地无对应设备，跳过同步')
            this.importProgress = 100
          }

          // 完成添加
          this.allProjects.push(newScene)
          this.saveProjectsToLocal()
          this.$emit('scene-added', newScene)

          // 延迟关闭，让用户看到 100%
          setTimeout(() => {
            this.importing = false
            this.showPreviewModal = false
            this.previewData = []
            message.success('导入成功！')
          }, 500)
        } catch (error) {
          console.error('设备同步异常:', error)
          message.error('同步设备时发生错误，请检查网络或日志')
          this.importing = false
        }
      }, 100)
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
          this.confirmDelete(project)
        }
      } else {
        this.selectProject(projectId)
      }
    },

    selectProject (projectId) {
      const project = this.allProjects.find(p => p.projectId === projectId)

      if (project) {
        const type = project.meshData?.type || 'F-city'

        // 强制类型映射 ID
        let apiId = 1
        if (type === 'F-city') apiId = 1
        else if (type === 'F-community') apiId = 2
        else if (type === 'F-park') apiId = 3
        else apiId = project.systemId || project.projectId

        localStorage.setItem('project_id', apiId)
        localStorage.setItem('current_scene_type', type)

        this.$router.push({
          path: '/space-scene'
        })
      }
    },

    async confirmDelete (project) {
      console.log(project)
      if (confirm(`确定要移除场景 "${project.projectName}" 吗？`)) {
        const hide = message.loading('正在删除中...', 0)
        try {
          // 2. 调用接口（注意：这里先调接口，成功后再删本地数据，防止删错）
          const res = await deleteProjectById(project.systemId)

          if (res) {
            // 3. 接口成功：更新本地状态
            this.allProjects = this.allProjects.filter(p => p.projectId !== project.projectId)
            this.saveProjectsToLocal()
            message.success('删除成功！')
          } else {
            message.error('删除失败')
          }
        } catch (error) {
          console.error(error)
          message.error('网络错误或服务器异常')
        } finally {
          // 4. 无论成功失败，都关闭 Loading
          hide()
        }
      }
    },

    async fetchProjects () {
      // 备用：拉取默认数据逻辑
      try {
        const fetchedProjects = await getProjects()
        this.allProjects = fetchedProjects.map((project) => {
          const currentId = project.project_id ? Number(project.project_id) : Number(project.projectId)
          let imagePath = project.image
          let name = project.projectName
          let type = ''

          if (currentId === 1) { name = '永德城区'; imagePath = require('@/assets/commercial.jpg'); type = 'F-city' } else if (currentId === 2) { name = '永德社区'; imagePath = require('@/assets/residential.jpg'); type = 'F-community' } else if (currentId === 3) { name = '永德园区'; imagePath = require('@/assets/Park.jpg'); type = 'F-park' }

          return {
            ...project,
            projectId: currentId,
            systemId: currentId,
            projectName: name || '新导入场景',
            image: imagePath || DefaultSceneImg,
            meshData: { type: type, grids: [] }
          }
        })
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

/* ✅ 修改：按钮居中显示 */
.button-wrapper {
  display: flex;
  justify-content: center; /* 关键修改：从 flex-end 改为 center */
  margin-bottom: 20px;
  gap: 15px;
  /* padding-right: 60px;  删除这行，避免居中偏左 */
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
  grid-template-columns: 250px 250px 250px;
  justify-content: center;
  gap: 30px;
}

.project-item {
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
.table-container::-webkit-scrollbar { width: 6px; }
.table-container::-webkit-scrollbar-thumb { background: #ccc; border-radius: 3px; }

/* ✅ 新增：进度条样式 */
.progress-container {
  width: 100%;
  height: 20px;
  background-color: #f0f0f0;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: inset 0 1px 3px rgba(0,0,0,0.2);
}

.progress-bar {
  height: 100%;
  background-color: #184aa1;
  width: 0%;
  transition: width 0.3s ease;
  background-image: linear-gradient(45deg,rgba(255,255,255,.15) 25%,transparent 25%,transparent 50%,rgba(255,255,255,.15) 50%,rgba(255,255,255,.15) 75%,transparent 75%,transparent);
  background-size: 1rem 1rem;
}
</style>
