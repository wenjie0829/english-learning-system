<template>
  <div class="learn-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <el-button @click="goBack" type="primary" plain>
            <el-icon><ArrowLeft /></el-icon> 返回
          </el-button>
          <h2>单词学习</h2>
          <div class="progress">
            学习进度: {{ currentIndex + 1 }} / {{ words.length }}
          </div>
        </div>
      </el-header>
      
      <el-main class="main-content">
        <div v-if="loading" class="loading-container">
          <el-icon class="is-loading" size="48"><Loading /></el-icon>
          <p>加载中...</p>
        </div>
        
        <div v-else-if="words.length === 0" class="empty-container">
          <el-empty description="暂无单词数据" />
        </div>
        
        <div v-else class="word-card">
          <el-card shadow="hover">
            <div class="word-content">
              <div class="word-header">
                <h1 class="word-text">{{ currentWord.word }}</h1>
                <div class="word-header-actions">
                  <el-button
                    @click="toggleFavorite"
                    :type="isFavorited ? 'warning' : 'default'"
                    circle
                    size="large"
                    class="favorite-button"
                    :loading="favoriteLoading"
                    :title="isFavorited ? '取消收藏' : '收藏这个单词'"
                  >
                    <el-icon><StarFilled v-if="isFavorited" /><Star v-else /></el-icon>
                  </el-button>
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
              </div>
              
              <div class="phonetic" v-if="currentWord.phonetic">
                {{ currentWord.phonetic }}
              </div>
              
              <div class="definition">
                <h3>释义:</h3>
                <p>{{ currentWord.definition }}</p>
              </div>
              
              <div class="ai-definition" v-if="currentWord.aiDefinition || aiDefinition">
                <h3>AI详细释义:</h3>
                <p>{{ currentWord.aiDefinition || aiDefinition }}</p>
                <el-button 
                  v-if="!currentWord.aiDefinition && !aiDefinition" 
                  @click="generateAIDefinition" 
                  type="primary" 
                  size="small"
                  :loading="generatingDefinition"
                >
                  <el-icon><MagicStick /></el-icon> 生成AI释义
                </el-button>
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
                    <el-button 
                      @click="playExampleAudio(example)" 
                      type="primary" 
                      circle 
                      size="small"
                      class="example-audio-button"
                    >
                      <el-icon><Microphone /></el-icon>
                    </el-button>
                  </div>
                  <div class="example-translation" v-if="example.translation">
                    {{ example.translation }}
                  </div>
                </div>
              </div>
              
              <div class="action-buttons">
                <el-button @click="markAsWrong" type="danger" size="large">
                  <el-icon><Close /></el-icon> 不认识
                </el-button>
                <el-button @click="markAsKnown" type="success" size="large">
                  <el-icon><Check /></el-icon> 认识
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
import { getAllWords } from '@/api/word'
import { getExampleSentences } from '@/api/word'
import { startLearning, reviewWord, addToFavorites, removeFromFavorites, getUserFavorites } from '@/api/learning'
import { generateDefinition } from '@/api/ai'

const router = useRouter()
const userStore = useUserStore()

const words = ref([])
const exampleSentences = ref([])
const currentIndex = ref(0)
const loading = ref(true)
const aiDefinition = ref('')
const generatingDefinition = ref(false)

// 收藏相关：本地维护一份"已收藏单词ID"的集合，切换单词时用它判断当前单词是否已收藏，
// 避免每切一个单词都单独请求一次后端
const favoriteWordIds = ref(new Set())
const favoriteLoading = ref(false)

const currentWord = computed(() => {
  return words.value[currentIndex.value] || {}
})

const isFavorited = computed(() => {
  return currentWord.value.id != null && favoriteWordIds.value.has(currentWord.value.id)
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

const loadWords = async () => {
  try {
    loading.value = true
    const data = await getAllWords()
    words.value = shuffleArray(data)
    if (words.value.length > 0) {
      await loadExampleSentences(words.value[0].id)
    }
  } catch (error) {
    ElMessage.error('加载单词失败')
    console.error('Load words error:', error)
  } finally {
    loading.value = false
  }
}

const loadFavorites = async () => {
  try {
    const data = await getUserFavorites(userStore.user.id)
    favoriteWordIds.value = new Set(data.map((f) => f.word?.id).filter((id) => id != null))
  } catch (error) {
    console.error('Load favorites error:', error)
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
  // 每次开始朗读前先清空浏览器TTS的播放队列：
  // 一是让新点击能立刻打断上一个还没读完的内容，而不是排队等待；
  // 二是规避Chrome自身一个众所周知的老毛病——不清空队列有时会导致同一句话反复循环朗读。
  speechSynthesis.cancel()

  if (currentWord.value.audioUrl) {
    const audio = new Audio(currentWord.value.audioUrl)
    audio.play()
  } else {
    const utterance = new SpeechSynthesisUtterance(currentWord.value.word)
    utterance.lang = 'en-US'
    speechSynthesis.speak(utterance)
  }
}

const playExampleAudio = (example) => {
  speechSynthesis.cancel()

  if (example.audioUrl) {
    const audio = new Audio(example.audioUrl)
    audio.play()
  } else {
    const utterance = new SpeechSynthesisUtterance(example.sentence)
    utterance.lang = 'en-US'
    speechSynthesis.speak(utterance)
  }
}

const toggleFavorite = async () => {
  const wordId = currentWord.value.id
  if (!wordId) return

  favoriteLoading.value = true
  try {
    if (isFavorited.value) {
      await removeFromFavorites(userStore.user.id, wordId)
      favoriteWordIds.value.delete(wordId)
      // Set 内部变化 Vue 侦测不到，手动触发一次响应式更新
      favoriteWordIds.value = new Set(favoriteWordIds.value)
      ElMessage.success('已取消收藏')
    } else {
      await addToFavorites(userStore.user.id, wordId)
      favoriteWordIds.value.add(wordId)
      favoriteWordIds.value = new Set(favoriteWordIds.value)
      ElMessage.success('已收藏')
    }
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('Toggle favorite error:', error)
  } finally {
    favoriteLoading.value = false
  }
}

const markAsKnown = async () => {
  try {
    await startLearning(userStore.user.id, currentWord.value.id)
    await reviewWord(userStore.user.id, currentWord.value.id, true)
    ElMessage.success('已标记为认识')
    nextWord()
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('Mark as known error:', error)
  }
}

const markAsWrong = async () => {
  try {
    await startLearning(userStore.user.id, currentWord.value.id)
    await reviewWord(userStore.user.id, currentWord.value.id, false)
    // Note: addToWrongWords is called automatically in the backend when review is incorrect
    ElMessage.warning('已添加到错词本')
    nextWord()
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('Mark as wrong error:', error)
  }
}

const generateAIDefinition = async () => {
  try {
    generatingDefinition.value = true
    const response = await generateDefinition(currentWord.value.word, currentWord.value.partOfSpeech)
    aiDefinition.value = response.definition
    ElMessage.success('AI释义生成成功')
  } catch (error) {
    ElMessage.error('AI释义生成失败')
    console.error('Generate AI definition error:', error)
  } finally {
    generatingDefinition.value = false
  }
}

const nextWord = () => {
  if (currentIndex.value < words.value.length - 1) {
    currentIndex.value++
    loadExampleSentences(words.value[currentIndex.value].id)
  } else {
    ElMessage.success('恭喜！已完成所有单词学习')
    router.push('/')
  }
}

const goBack = () => {
  router.push('/')
}

onMounted(() => {
  loadWords()
  loadFavorites()
})
</script>

<style scoped>
.learn-container {
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

.word-card {
  width: 100%;
  max-width: 800px;
}

.word-content {
  padding: 20px;
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

.word-header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.audio-button,
.favorite-button {
  flex-shrink: 0;
}

.favorite-button.el-button--warning {
  --el-button-bg-color: #f7ba2a;
  --el-button-border-color: #f7ba2a;
}

.phonetic {
  font-size: 20px;
  color: #666;
  margin-bottom: 20px;
  font-style: italic;
}

.definition,
.ai-definition {
  margin-bottom: 20px;
}

.definition h3,
.ai-definition h3 {
  margin: 0 0 10px 0;
  color: #333;
}

.definition p,
.ai-definition p {
  margin: 0;
  color: #666;
  line-height: 1.6;
}

.ai-definition {
  background: #f0f9ff;
  padding: 15px;
  border-radius: 8px;
  border-left: 4px solid #409EFF;
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

.example-audio-button {
  flex-shrink: 0;
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
.learn-container{min-height:100vh;background:#bff28d;background-image:radial-gradient(ellipse at 16% 88%,#3f7d48 0 7%,transparent 7.3%),radial-gradient(ellipse at 84% 17%,#e8ffc4 0 9%,transparent 9.3%)}.header{margin:20px auto 0;width:calc(100% - 40px);max-width:1060px;height:72px;border-radius:18px;background:#0d2617;box-shadow:none}.header-content h2{font-family:var(--font-display);color:#fff;font-size:26px}.progress{color:#dff3bc}.header .el-button{border-radius:999px;--el-button-bg-color:#c6f19d;--el-button-border-color:#c6f19d;--el-button-text-color:#102318}.main-content{padding:42px 20px 64px}.word-card{max-width:920px}.word-card .el-card{border:10px solid #0d2617;border-radius:28px;background:#fffdf7;overflow:hidden;box-shadow:0 28px 60px rgba(13,38,23,.22)}.word-content{padding:44px 52px}.word-text{font-family:var(--font-display);font-size:clamp(48px,8vw,86px);color:#0d2617;letter-spacing:-.05em}.phonetic{color:#4d7a41}.definition{padding:20px 24px;border-left:5px solid #c6f19d;background:#f0f5e8;border-radius:0 14px 14px 0}.ai-definition{background:#e6f4d5;border-left-color:#4d7a41;border-radius:14px}.example-item{background:#f4f1e8;border:1px solid #e2e6d7;border-radius:14px}.action-buttons .el-button{border-radius:999px;height:50px}.action-buttons .el-button--success{--el-button-bg-color:#245b3c;--el-button-border-color:#245b3c}.action-buttons .el-button--danger{--el-button-bg-color:#a94a3b;--el-button-border-color:#a94a3b}
</style>
<style>
.learn-container{background:#edf3e4!important;background-image:radial-gradient(circle at 8% 10%,rgba(194,239,146,.46),transparent 24rem),radial-gradient(circle at 92% 86%,rgba(173,207,145,.43),transparent 28rem)!important}.learn-container .main-content{width:100%;max-width:1120px;margin:0 auto;padding:38px 20px 72px!important}.learn-container .word-card{width:min(100%,880px)!important;margin:0 auto}.learn-container .header{max-width:1120px!important;width:calc(100% - 40px)!important;margin:22px auto 0!important}.learn-container .word-card .el-card{border:1px solid #d5ddcd!important;box-shadow:0 18px 46px rgba(33,71,42,.14)!important}.learn-container .word-content{padding:42px 52px!important}
</style>