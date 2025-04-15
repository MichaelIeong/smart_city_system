<template>
  <page-header-wrapper>
    <a-card bordered :style="{ borderRadius: '8px' }">
      <!-- 用户输入框 -->
      <div class="table-page-search-wrapper">
        <a-form layout="inline">
          <a-row :gutter="48">
            <a-col :md="24" :sm="24">
              <a-form-item label="输入内容">
                <a-input v-model="userInput" placeholder="请输入内容" />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </div>

      <!-- 图形化展示组件 -->
      <div class="graphic-display">
        <a-card>
          <p>这里将展示图形化内容</p>
        </a-card>
      </div>

      <!-- 结果显示框 -->
      <div class="result-box">
        <a-textarea v-model="generatedResult" placeholder="系统生成的结果将在这里显示" auto-size />
        <div class="table-page-search-submitButtons">
          <a-button type="primary" @click="confirmAction">确认</a-button>
          <a-button style="margin-left: 8px" @click="regenerateResult">再次生成</a-button>
        </div>
      </div>
    </a-card>
  </page-header-wrapper>
</template>

<script>
export default {
  name: 'RecommendDetail',

  data () {
    return {
      userInput: '',
      generatedResult: ''
    }
  },
  methods: {
    confirmAction () {
      console.log('用户确认操作:', this.userInput)
    },
    regenerateResult () {
      console.log('重新生成结果...')
      this.generatedResult = '新生成的推荐内容'
    }
  }
}
</script>

<style scoped>
.table-page-search-wrapper {
  margin-bottom: 20px;
}

.graphic-display {
  width: 100%;
  height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
}

.result-box {
  width: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
}

.table-page-search-submitButtons {
  display: flex;
  justify-content: center;
  width: 100%;
  margin-top: 10px;
}
</style>
