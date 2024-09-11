<template>
  <div class="app">
    <h2>'menu.projectSelection'</h2>
    <!-- 項目列表 -->
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
export default {
  data () {
    return {
      allProjects: [
        {
          id: 'p1',
          name: '項目一',
          image: 'https://via.placeholder.com/800x400.png?text=項目一'
        },
        {
          id: 'p2',
          name: '項目二',
          image: 'https://via.placeholder.com/800x400.png?text=項目二'
        },
        {
          id: 'p3',
          name: '項目三',
          image: 'https://via.placeholder.com/800x400.png?text=項目三'
        },
        {
          id: 'p4',
          name: '項目四',
          image: 'https://via.placeholder.com/800x400.png?text=項目四'
        },
        {
          id: 'p5',
          name: '項目五',
          image: 'https://via.placeholder.com/800x400.png?text=項目五'
        },
        {
          id: 'p6',
          name: '項目六',
          image: 'https://via.placeholder.com/800x400.png?text=項目六'
        },
        {
          id: 'p7',
          name: '項目七',
          image: 'https://via.placeholder.com/800x400.png?text=項目七'
        },
        {
          id: 'p8',
          name: '項目八',
          image: 'https://via.placeholder.com/800x400.png?text=項目八'
        },
        {
          id: 'p9',
          name: '項目九',
          image: 'https://via.placeholder.com/800x400.png?text=項目九'
        }
      ]
    }
  },
  methods: {
    selectProject (projectId) {
      // 處理點擊項目後的邏輯，比如跳轉到詳細頁
      console.log(`選擇的項目ID是: ${projectId}`)
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
</style>
