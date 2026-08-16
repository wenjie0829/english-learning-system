<template>
  <div class="review-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <el-button @click="goBack" type="primary" plain>
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
          <h2>艾宾浩斯复习</h2>
          <div class="progress">
            复习进度: {{ currentIndex + 1 }} / {{ reviewWords.length }}
          </div>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <div v-if="loading" class="loading-container">
          <el-icon class="is-loading" size="48"><Loading /></el-icon>
          <p>加载中...</p>
        </div>
        
        <div v-else-if="reviewWords.length === 0" class="empty-container">
          <el-empty description="暂无需要复习的单词">
            <el-button @click="goBack" type="primary">返回首页</el-button>
          </el-empty>
        </div>
        
        <div v-else class="review-card">
          <el-card shadow="hover">
            <div class="review-content">
              <div class="review-info">
                <el-tag type="info">阶段: {{ getStageDescription(currentReviewRecord.ebbinghausStage) }}</el-tag>
                <el-tag type="warning">复习次数: {{ currentReviewRecord.reviewCount }}</el-tag>
              </div>
              
              <div class="word-header">
                <h1 class="word-text">{{ currentWord.word }}</h1>
                <el-button 
                  @click="playAudio" 
                  type="primary" 
                  circle 
                  size="large"
                  class="audio-button"
                >
                  <el-icon><Microphone /></el-icon>
                </el-button>
              </div>
              
              <div class="phonetic" v-if="currentWord.phonetic">
                {{ currentWord.phonetic }}
              </div>
              
              <div class="definition">
                <h3>释义:</h3>
                <p>{{ currentWord.definition }}</p>
              </div>
              
              <div class="examples" v-if="exampleSentences.length > 0">
                <h3>例句:</h3>
                <div 
                  v-for="(example, index) in exampleSentences" 
                  :key="index" 
                  class="example-item"
                >
                  <div class="example-sentence">
                    <span class="example-number">{{ index + 1 }}.</span>
                    <span>{{ example.sentence }}</span>
                  </div>
                  <div class="example-translation" v-if="example.translation">
                    {{ example.translation }}
                  </div>
                </div>
              </div>
              
              <div class="action-buttons">
                <el-button @click="handleReview(false)" type="danger" size="large">
                  <el-icon><Close /></el-icon> 不记得
                </el-button>
                <el-button @click="handleReview(true)" type="success" size="large">
                  <el-icon><Check /></el-icon> 记得
                </el-button>
              </div>
            </div>
          </el-card>
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
import { getDueReviews } from '@/api/learning'
import { getExampleSentences } from '@/api/word'
import { reviewWord } from '@/api/learning'

const router = useRouter()
const userStore = useUserStore()

const reviewWords = ref([])
const exampleSentences = ref([])
const currentIndex = ref(0)
const loading = ref(true)

const currentReviewRecord = computed(() => {
  return reviewWords.value[currentIndex.value] || {}
})

const currentWord = computed(() => {
  return currentReviewRecord.value.word || {}
})

// Fisher-Yates 洗牌算法，把数组顺序完全打乱，每次刷新结果都不一样
const shuffleArray = (arr) => {
  const result = [...arr]
  for (let i = result.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[result[i], result[j]] = [result[j], result[i]]
  }
  return result
}

const loadReviewWords = async () => {
  try {
    loading.value = true
    const data = await getDueReviews(userStore.user.id)
    reviewWords.value = shuffleArray(data)
    if (reviewWords.value.length > 0) {
      await loadExampleSentences(currentWord.value.id)
    }
  } catch (error) {
    ElMessage.error('加载复习单词失败')
    console.error('Load review words error:', error)
  } finally {
    loading.value = false
  }
}

const loadExampleSentences = async (wordId) => {
  try {
    const data = await getExampleSentences(wordId)
    exampleSentences.value = data
  } catch (error) {
    console.error('Load example sentences error:', error)
  }
}

const playAudio = () => {
  if (currentWord.value.audioUrl) {
    const audio = new Audio(currentWord.value.audioUrl)
    audio.play()
  } else {
    const utterance = new SpeechSynthesisUtterance(currentWord.value.word)
    utterance.lang = 'en-US'
    speechSynthesis.speak(utterance)
  }
}

const handleReview = async (isCorrect) => {
  try {
    await reviewWord(userStore.user.id, currentWord.value.id, isCorrect)
    
    if (isCorrect) {
      ElMessage.success('记得很棒！')
    } else {
      ElMessage.warning('已重置学习进度')
    }
    
    nextWord()
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('Review error:', error)
  }
}

const nextWord = () => {
  if (currentIndex.value < reviewWords.value.length - 1) {
    currentIndex.value++
    loadExampleSentences(currentWord.value.id)
  } else {
    ElMessage.success('恭喜！已完成本次复习')
    router.push('/')
  }
}

const getStageDescription = (stage) => {
  const descriptions = ['5分钟', '30分钟', '12小时', '1天', '2天', '4天', '7天', '15天']
  return descriptions[stage] || '已掌握'
}

const goBack = () => {
  router.push('/')
}

onMounted(() => {
  loadReviewWords()
})
</script>

<style scoped>
.review-container {
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

.progress {
  font-size: 16px;
  color: #666;
}

.main-content {
  padding: 20px;
  display: flex;
  justify-content: center;
}

.loading-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: #666;
}

.review-card {
  width: 100%;
  max-width: 800px;
}

.review-content {
  padding: 20px;
}

.review-info {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.word-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.word-text {
  margin: 0;
  font-size: 48px;
  color: #333;
}

.audio-button {
  flex-shrink: 0;
}

.phonetic {
  font-size: 20px;
  color: #666;
  margin-bottom: 20px;
  font-style: italic;
}

.definition {
  margin-bottom: 20px;
}

.definition h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.definition p {
  margin: 0;
  color: #666;
  line-height: 1.6;
}

.examples {
  margin-bottom: 30px;
}

.examples h3 {
  margin: 0 0 15px 0;
  color: #333;
}

.example-item {
  margin-bottom: 15px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 8px;
}

.example-sentence {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.example-number {
  font-weight: bold;
  color: #409EFF;
}

.example-translation {
  color: #666;
  font-style: italic;
  padding-left: 25px;
}

.action-buttons {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-top: 30px;
}

.action-buttons .el-button {
  flex: 1;
  max-width: 200px;
}
</style>

<style>
.review-container{min-height:100vh;background:#edf3e4;background-image:radial-gradient(circle at 8% 10%,rgba(194,239,146,.46),transparent 24rem),radial-gradient(circle at 92% 86%,rgba(173,207,145,.43),transparent 28rem)}.review-container .header{height:72px;max-width:1120px;width:calc(100% - 40px);margin:22px auto 0;border-radius:18px;background:#fffdf8;box-shadow:0 8px 22px rgba(22,50,29,.12)}.review-container .header-content h2{font-family:var(--font-display);color:#102318;font-size:28px}.review-container .progress{color:#526b58}.review-container .main-content{width:100%;max-width:1120px;margin:0 auto;padding:30px 20px 72px;display:block}.review-container .review-card{width:min(100%,880px);margin:0 auto}.review-container .el-card{border:1px solid #d5ddcd;border-radius:22px;background:#fffdf8;box-shadow:0 18px 46px rgba(33,71,42,.12)}.review-container .review-content{padding:40px 52px}.review-container .word-text{font-family:var(--font-display);font-size:64px;color:#102318}.review-container .definition{background:#f0f5e8;border-left:5px solid #b8e994}.review-container .action-buttons .el-button{border-radius:999px;height:50px}
</style>