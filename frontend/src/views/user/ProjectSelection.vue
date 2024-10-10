<template>
  <div class="main">
    <!-- 使用 $t() 函數來引用多語言鍵值 -->
    <h1 class="center-text">{{ $t('menu.projectSelection') }}</h1>

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
          name: 'Ai Park',
          image: require('@/assets/commercial.jpg')
        },
        {
          id: 'p2',
          name: '凯州新城',
          image: require('@/assets/residential.jpg')
        }
      ]
    }
  },
  methods: {
    selectProject (projectId) {
      console.log(`选择的项目ID是: ${projectId}`)
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

.project-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr); /* 每行显示2个项目 */
  gap: 30px;
  padding: 30px;
}

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
  padding: 10px;
  font-size: 1.4em;
  text-align: center;
  position: absolute; /* 绝对定位 */
  bottom: 0; /* 放置在卡片的最底部 */
  left: 0;
  right: 0;
}
</style>
