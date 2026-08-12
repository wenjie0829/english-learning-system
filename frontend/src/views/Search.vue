<template>
  <div class="search-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <el-button @click="goBack" type="primary" plain>
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
          <h2>单词查询</h2>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <div class="search-section">
          <el-input
            v-model="searchKeyword"
            placeholder="请输入单词或释义关键词"
            size="large"
            @keyup.enter="handleSearch"
            clearable
          >
            <template #append>
              <el-button @click="handleSearch" type="primary">
                <el-icon><Search /></el-icon> 搜索
              </el-button>
            </template>
          </el-input>
        </div>
        
        <div v-if="loading" class="loading-container">
          <el-icon class="is-loading" size="48"><Loading /></el-icon>
          <p>搜索中...</p>
        </div>
        
        <div v-else-if="searchResults.length > 0" class="results-section">
          <div 
            v-for="word in searchResults" 
            :key="word.id" 
            class="word-result-item"
            @click="showWordDetail(word)"
          >
            <el-card shadow="hover">
              <div class="word-result-content">
                <div class="word-result-header">
                  <h3 class="word-result-text">{{ word.word }}</h3>
                  <el-tag :type="getDifficultyType(word.difficultyLevel)">
                    {{ getDifficultyText(word.difficultyLevel) }}
                  </el-tag>
                </div>
                <div class="word-result-phonetic" v-if="word.phonetic">
                  {{ word.phonetic }}
                </div>
                <div class="word-result-definition">
                  {{ word.definition }}
                </div>
              </div>
            </el-card>
          </div>
        </div>
        
        <div v-else-if="hasSearched" class="empty-container">
          <el-empty description="未找到相关单词" />
        </div>
        
        <div v-else class="empty-container">
          <el-empty description="请输入关键词进行搜索" />
        </div>
      </el-main>
    </el-container>
    
    <!-- Word Detail Dialog -->
    <el-dialog v-model="detailDialogVisible" title="单词详情" width="600px">
      <div v-if="selectedWord" class="word-detail">
        <div class="detail-header">
          <h2>{{ selectedWord.word }}</h2>
          <el-button @click="playWordAudio" type="primary" circle>
            <el-icon><Microphone /></el-icon>
          </el-button>
        </div>
        <div class="detail-phonetic" v-if="selectedWord.phonetic">
          {{ selectedWord.phonetic }}
        </div>
        <div class="detail-definition">
          <h4>释义:</h4>
          <p>{{ selectedWord.definition }}</p>
        </div>
        <div class="detail-ai-definition" v-if="selectedWord.aiDefinition">
          <h4>AI详细释义:</h4>
          <p>{{ selectedWord.aiDefinition }}</p>
        </div>
        <div class="detail-examples" v-if="detailExamples.length > 0">
          <h4>例句:</h4>
          <div v-for="(example, index) in detailExamples" :key="index" class="detail-example-item">
            <p>{{ example.sentence }}</p>
            <p class="example-translation">{{ example.translation }}</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { searchWords, getExampleSentences } from '@/api/word'

const router = useRouter()

const searchKeyword = ref('')
const searchResults = ref([])
const loading = ref(false)
const hasSearched = ref(false)
const detailDialogVisible = ref(false)
const selectedWord = ref(null)
const detailExamples = ref([])

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  try {
    loading.value = true
    hasSearched.value = true
    const data = await searchWords(searchKeyword.value)
    searchResults.value = data
  } catch (error) {
    ElMessage.error('搜索失败')
    console.error('Search error:', error)
  } finally {
    loading.value = false
  }
}

const showWordDetail = async (word) => {
  selectedWord.value = word
  detailDialogVisible.value = true
  
  try {
    const examples = await getExampleSentences(word.id)
    detailExamples.value = examples
  } catch (error) {
    console.error('Load examples error:', error)
  }
}

const playWordAudio = () => {
  if (selectedWord.value.audioUrl) {
    const audio = new Audio(selectedWord.value.audioUrl)
    audio.play()
  } else {
    const utterance = new SpeechSynthesisUtterance(selectedWord.value.word)
    utterance.lang = 'en-US'
    speechSynthesis.speak(utterance)
  }
}

const getDifficultyType = (level) => {
  const types = {
    'EASY': 'success',
    'MEDIUM': 'warning',
    'HARD': 'danger'
  }
  return types[level] || 'info'
}

const getDifficultyText = (level) => {
  const texts = {
    'EASY': '简单',
    'MEDIUM': '中等',
    'HARD': '困难'
  }
  return texts[level] || level
}

const goBack = () => {
  router.push('/')
}
</script>

<style scoped>
.search-container {
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

.search-section {
  margin-bottom: 30px;
}

.loading-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #666;
}

.results-section {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.word-result-item {
  cursor: pointer;
  transition: transform 0.2s;
}

.word-result-item:hover {
  transform: translateX(5px);
}

.word-result-content {
  padding: 15px;
}

.word-result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.word-result-text {
  margin: 0;
  font-size: 24px;
  color: #333;
}

.word-result-phonetic {
  color: #666;
  font-style: italic;
  margin-bottom: 8px;
}

.word-result-definition {
  color: #666;
  line-height: 1.6;
}

.word-detail {
  padding: 10px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.detail-header h2 {
  margin: 0;
  font-size: 32px;
  color: #333;
}

.detail-phonetic {
  font-size: 18px;
  color: #666;
  font-style: italic;
  margin-bottom: 15px;
}

.detail-definition,
.detail-ai-definition,
.detail-examples {
  margin-bottom: 15px;
}

.detail-definition h4,
.detail-ai-definition h4,
.detail-examples h4 {
  margin: 0 0 8px 0;
  color: #333;
}

.detail-definition p,
.detail-ai-definition p {
  margin: 0;
  color: #666;
  line-height: 1.6;
}

.detail-ai-definition {
  background: #f0f9ff;
  padding: 12px;
  border-radius: 8px;
  border-left: 4px solid #409EFF;
}

.detail-example-item {
  margin-bottom: 12px;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 6px;
}

.detail-example-item p {
  margin: 4px 0;
}

.example-translation {
  color: #666;
  font-style: italic;
}
</style>
