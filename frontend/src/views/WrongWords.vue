<template>
  <div class="wrong-words-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <el-button @click="goBack" type="primary" plain>
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
          <h2>错词本</h2>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <div v-if="loading" class="loading-container">
          <el-icon class="is-loading" size="48"><Loading /></el-icon>
          <p>加载中...</p>
        </div>
        
        <div v-else-if="wrongWords.length === 0" class="empty-container">
          <el-empty description="暂无错词记录">
            <el-button @click="goBack" type="primary">继续学习</el-button>
          </el-empty>
        </div>
        
        <div v-else class="wrong-words-list">
          <div 
            v-for="wrongWord in wrongWords" 
            :key="wrongWord.id" 
            class="wrong-word-item"
          >
            <el-card shadow="hover">
              <div class="wrong-word-content">
                <div class="wrong-word-header">
                  <h3>{{ wrongWord.word.word }}</h3>
                  <el-tag type="danger">错误次数: {{ wrongWord.wrongCount }}</el-tag>
                </div>
                <div class="wrong-word-phonetic" v-if="wrongWord.word.phonetic">
                  {{ wrongWord.word.phonetic }}
                </div>
                <div class="wrong-word-definition">
                  {{ wrongWord.word.definition }}
                </div>
                <div class="wrong-word-info">
                  <span class="info-item">最后错误: {{ formatDate(wrongWord.lastWrongAt) }}</span>
                </div>
                <div class="wrong-word-actions">
                  <el-button @click="playAudio(wrongWord.word)" type="primary" size="small">
                    <el-icon><Microphone /></el-icon> 发音
                  </el-button>
                <el-button @click="viewDetail(wrongWord.word)" class="detail-btn" size="small">
                <el-icon><View /></el-icon> 详情
                </el-button>
                  <el-button @click="markAsResolved(wrongWord.word.id)" type="success" size="small">
                    <el-icon><Check /></el-icon> 已掌握
                  </el-button>
                </div>
              </div>
            </el-card>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { getUserWrongWords, markWrongWordAsResolved } from '@/api/learning'

const router = useRouter()
const userStore = useUserStore()

const wrongWords = ref([])
const loading = ref(true)

const loadWrongWords = async () => {
  try {
    loading.value = true
    const data = await getUserWrongWords(userStore.user.id)
    wrongWords.value = data
  } catch (error) {
    ElMessage.error('加载错词失败')
    console.error('Load wrong words error:', error)
  } finally {
    loading.value = false
  }
}

const markAsResolved = async (wordId) => {
  try {
    await markWrongWordAsResolved(userStore.user.id, wordId)
    ElMessage.success('已标记为掌握')
    await loadWrongWords()
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('Mark as resolved error:', error)
  }
}

const playAudio = (word) => {
  if (word.audioUrl) {
    const audio = new Audio(word.audioUrl)
    audio.play()
  } else {
    const utterance = new SpeechSynthesisUtterance(word.word)
    utterance.lang = 'en-US'
    speechSynthesis.speak(utterance)
  }
}

const viewDetail = (word) => {
  router.push({ name: 'Search', query: { word: word.word } })
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

const goBack = () => {
  router.push('/')
}

onMounted(() => {
  loadWrongWords()
})
</script>

<style scoped>
.wrong-words-container {
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
  max-width: 800px;
  margin: 0 auto;
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

.wrong-words-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.wrong-word-item {
  transition: transform 0.2s;
}

.wrong-word-item:hover {
  transform: translateY(-3px);
}

.wrong-word-content {
  padding: 15px;
}

.wrong-word-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.wrong-word-header h3 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.wrong-word-phonetic {
  color: #666;
  font-style: italic;
  margin-bottom: 8px;
}

.wrong-word-definition {
  color: #666;
  line-height: 1.6;
  margin-bottom: 10px;
}

.wrong-word-info {
  margin-bottom: 15px;
  font-size: 14px;
  color: #999;
}

.wrong-word-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.detail-btn {
  --el-button-bg-color: var(--color-amber-tint);
  --el-button-border-color: var(--color-amber);
  --el-button-text-color: var(--color-amber);
  --el-button-hover-bg-color: var(--color-amber);
  --el-button-hover-border-color: var(--color-amber);
  --el-button-hover-text-color: #fff;
}
</style>
