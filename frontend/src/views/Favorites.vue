<template>
  <div class="favorites-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <el-button @click="goBack" type="primary" plain>
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
          <h2>我的收藏</h2>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <div v-if="loading" class="loading-container">
          <el-icon class="is-loading" size="48"><Loading /></el-icon>
          <p>加载中...</p>
        </div>
        
        <div v-else-if="favorites.length === 0" class="empty-container">
          <el-empty description="暂无收藏的单词">
            <el-button @click="goBack" type="primary">去学习</el-button>
          </el-empty>
        </div>
        
        <div v-else class="favorites-list">
          <div 
            v-for="favorite in favorites" 
            :key="favorite.id" 
            class="favorite-item"
          >
            <el-card shadow="hover">
              <div class="favorite-content">
                <div class="favorite-header">
                  <h3>{{ favorite.word.word }}</h3>
                  <el-button 
                    @click="handleRemoveFromFavorites(favorite.word.id)" 
                    type="danger" 
                    circle
                    size="small"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
                <div class="favorite-phonetic" v-if="favorite.word.phonetic">
                  {{ favorite.word.phonetic }}
                </div>
                <div class="favorite-definition">
                  {{ favorite.word.definition }}
                </div>
                <div class="favorite-actions">
                  <el-button @click="playAudio(favorite.word)" type="primary" size="small">
                    <el-icon><Microphone /></el-icon> 发音
                  </el-button>
                  <el-button @click="viewDetail(favorite.word)" type="info" size="small">
                    <el-icon><View /></el-icon> 详情
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
import { getUserFavorites, removeFromFavorites as removeFavoriteApi } from '@/api/learning'

const router = useRouter()
const userStore = useUserStore()

const favorites = ref([])
const loading = ref(true)

const loadFavorites = async () => {
  try {
    loading.value = true
    const data = await getUserFavorites(userStore.user.id)
    favorites.value = data
  } catch (error) {
    ElMessage.error('加载收藏失败')
    console.error('Load favorites error:', error)
  } finally {
    loading.value = false
  }
}

const handleRemoveFromFavorites = async (wordId) => {
  try {
    await removeFavoriteApi(userStore.user.id, wordId)
    ElMessage.success('已取消收藏')
    await loadFavorites()
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('Remove from favorites error:', error)
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
  // Navigate to search with the word as query
  router.push({ name: 'Search', query: { word: word.word } })
}

const goBack = () => {
  router.push('/')
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.favorites-container {
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

.favorites-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.favorite-item {
  transition: transform 0.2s;
}

.favorite-item:hover {
  transform: translateY(-3px);
}

.favorite-content {
  padding: 15px;
}

.favorite-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.favorite-header h3 {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.favorite-phonetic {
  color: #666;
  font-style: italic;
  margin-bottom: 8px;
}

.favorite-definition {
  color: #666;
  line-height: 1.6;
  margin-bottom: 15px;
}

.favorite-actions {
  display: flex;
  gap: 10px;
}
</style>
