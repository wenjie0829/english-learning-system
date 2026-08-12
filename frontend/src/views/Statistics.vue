<template>
  <div class="statistics-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <el-button @click="goBack" type="primary" plain>
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
          <h2>学习统计</h2>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <div v-if="loading" class="loading-container">
          <el-icon class="is-loading" size="48"><Loading /></el-icon>
          <p>加载中...</p>
        </div>
        
        <div v-else class="statistics-content">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-content">
                  <el-icon class="stat-icon" color="#409EFF"><Document /></el-icon>
                  <div class="stat-info">
                    <div class="stat-value">{{ statistics.totalWords }}</div>
                    <div class="stat-label">总单词数</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-content">
                  <el-icon class="stat-icon" color="#67C23A"><SuccessFilled /></el-icon>
                  <div class="stat-info">
                    <div class="stat-value">{{ statistics.masteredWords }}</div>
                    <div class="stat-label">已掌握</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-content">
                  <el-icon class="stat-icon" color="#E6A23C"><Loading /></el-icon>
                  <div class="stat-info">
                    <div class="stat-value">{{ statistics.learningWords }}</div>
                    <div class="stat-label">学习中</div>
                  </div>
                </div>
              </el-card>
            </el-col>
            
            <el-col :span="6">
              <el-card class="stat-card" shadow="hover">
                <div class="stat-content">
                  <el-icon class="stat-icon" color="#F56C6C"><Clock /></el-icon>
                  <div class="stat-info">
                    <div class="stat-value">{{ statistics.dueReviews }}</div>
                    <div class="stat-label">待复习</div>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          
          <el-row :gutter="20" class="progress-section">
            <el-col :span="12">
              <el-card shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>学习进度</span>
                  </div>
                </template>
                <div class="progress-content">
                  <el-progress 
                    :percentage="masteryPercentage" 
                    :color="progressColor"
                    :stroke-width="20"
                    :text-inside="true"
                  />
                  <div class="progress-text">
                    掌握率: {{ masteryPercentage }}%
                  </div>
                </div>
              </el-card>
            </el-col>
            
            <el-col :span="12">
              <el-card shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>学习状态分布</span>
                  </div>
                </template>
                <div class="status-distribution">
                  <div class="status-item">
                    <div class="status-bar mastered" :style="{ width: masteryPercentage + '%' }"></div>
                    <span>已掌握: {{ statistics.masteredWords }}</span>
                  </div>
                  <div class="status-item">
                    <div class="status-bar learning" :style="{ width: learningPercentage + '%' }"></div>
                    <span>学习中: {{ statistics.learningWords }}</span>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
          
          <el-row :gutter="20" class="tips-section">
            <el-col :span="24">
              <el-card shadow="hover">
                <template #header>
                  <div class="card-header">
                    <span>学习建议</span>
                  </div>
                </template>
                <div class="tips-content">
                  <div v-if="statistics.dueReviews > 0" class="tip-item">
                    <el-icon color="#F56C6C"><Warning /></el-icon>
                    <span>您有 {{ statistics.dueReviews }} 个单词需要复习，建议先完成复习再学习新单词。</span>
                  </div>
                  <div v-else class="tip-item">
                    <el-icon color="#67C23A"><SuccessFilled /></el-icon>
                    <span>当前没有需要复习的单词，可以继续学习新单词。</span>
                  </div>
                  
                  <div v-if="masteryPercentage < 30" class="tip-item">
                    <el-icon color="#E6A23C"><InfoFilled /></el-icon>
                    <span>您的掌握率较低，建议多花时间复习和练习。</span>
                  </div>
                  <div v-else-if="masteryPercentage < 70" class="tip-item">
                    <el-icon color="#409EFF"><InfoFilled /></el-icon>
                    <span>您的学习进展良好，继续保持！</span>
                  </div>
                  <div v-else class="tip-item">
                    <el-icon color="#67C23A"><SuccessFilled /></el-icon>
                    <span>您的掌握率很高，表现优秀！</span>
                  </div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getUserStatistics } from '@/api/learning'

const router = useRouter()
const userStore = useUserStore()

const statistics = ref({
  totalWords: 0,
  masteredWords: 0,
  learningWords: 0,
  dueReviews: 0
})
const loading = ref(true)

const masteryPercentage = computed(() => {
  if (statistics.value.totalWords === 0) return 0
  return Math.round((statistics.value.masteredWords / statistics.value.totalWords) * 100)
})

const learningPercentage = computed(() => {
  if (statistics.value.totalWords === 0) return 0
  return Math.round((statistics.value.learningWords / statistics.value.totalWords) * 100)
})

const progressColor = computed(() => {
  const percentage = masteryPercentage.value
  if (percentage < 30) return '#F56C6C'
  if (percentage < 70) return '#E6A23C'
  return '#67C23A'
})

const loadStatistics = async () => {
  try {
    loading.value = true
    const data = await getUserStatistics(userStore.user.id)
    statistics.value = data
  } catch (error) {
    ElMessage.error('加载统计数据失败')
    console.error('Load statistics error:', error)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/')
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.statistics-container {
  min-height: 100vh;
}

.header {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0;
  color: #333;
}

.main-content {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: #666;
}

.stat-card {
  margin-bottom: 20px;
  border-radius: 12px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  font-size: 32px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

.progress-section {
  margin-top: 20px;
}

.card-header {
  font-weight: bold;
  color: #333;
}

.progress-content {
  padding: 20px 0;
}

.progress-text {
  text-align: center;
  margin-top: 15px;
  font-size: 16px;
  color: #666;
}

.status-distribution {
  padding: 20px 0;
}

.status-item {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-bar {
  height: 8px;
  border-radius: 4px;
  transition: width 0.3s;
}

.status-bar.mastered {
  background: #67C23A;
}

.status-bar.learning {
  background: #E6A23C;
}

.tips-section {
  margin-top: 20px;
}

.tips-content {
  padding: 10px 0;
}

.tip-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
}

.tip-item:last-child {
  margin-bottom: 0;
}
</style>

<style>
.statistics-container{min-height:100vh;background:#edf3e4;background-image:radial-gradient(circle at 8% 10%,rgba(194,239,146,.42),transparent 24rem),radial-gradient(circle at 92% 86%,rgba(173,207,145,.38),transparent 28rem)}.statistics-container .header{height:72px;max-width:1120px;width:calc(100% - 40px);margin:22px auto 0;border-radius:18px;background:#fffdf8;box-shadow:0 8px 22px rgba(22,50,29,.12)}.statistics-container .header-content h2{font-family:var(--font-display);color:#102318;font-size:28px}.statistics-container .main-content{width:100%;max-width:1120px;margin:0 auto;padding:30px 20px 72px}.statistics-content{width:100%;margin:0 auto}.statistics-container .el-row{margin-bottom:20px}.statistics-container .el-card{height:100%;border:1px solid #d5ddcd;border-radius:20px;background:#fffdf8;box-shadow:0 12px 30px rgba(33,71,42,.09)}.statistics-container .el-card__header{padding:20px 26px;border-bottom-color:#e4e9df}.statistics-container .stat-card{margin:0}.statistics-container .stat-card .el-card__body{padding:22px}.statistics-container .stat-value{color:#173523!important;font-size:34px}.statistics-container .stat-label{color:#526b58!important;font-weight:700}.statistics-container .status-item{display:grid;grid-template-columns:minmax(80px,1fr) auto;gap:12px}.statistics-container .status-bar{min-width:8px}.statistics-container .tip-item{background:#f3f6ee;color:#173523}.statistics-container .el-progress-bar__outer{background:#e6ecdf}
@media(max-width:760px){.statistics-container .el-col{width:100%;max-width:100%;flex:0 0 100%;margin-bottom:14px}}
</style>