<template>
  <div class="wrong-words-page">
    <AppHeader mode="simple" title="错词本" back />
    <main class="page-shell list-main">
      <!-- 加载中 -->
      <div v-if="loading" class="list-state card">
        <el-icon class="is-loading" :size="36"><Loading /></el-icon>
        <p>正在加载错词…</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="wrongWords.length === 0" class="list-state card">
        <el-icon :size="48" color="var(--color-moss)"><CircleCheckFilled /></el-icon>
        <h3>暂无错词记录</h3>
        <p>复习中答错的单词会自动收进这里，方便集中攻克</p>
        <div class="state-btns">
          <el-button type="primary" round @click="goReview">去复习</el-button>
          <el-button round @click="goHome">返回首页</el-button>
        </div>
      </div>

      <!-- 错词列表 -->
      <div v-else class="results-list">
        <div
          v-for="wrongWord in wrongWords"
          :key="wrongWord.id"
          class="word-card card"
          @click="viewDetail(wrongWord.word)"
        >
          <div class="word-card-top">
            <div class="word-head">
              <h3 class="word-text">{{ wrongWord.word.word }}</h3>
              <span v-if="wrongWord.word.phonetic" class="phonetic">{{ wrongWord.word.phonetic }}</span>
              <el-tag type="danger" effect="light" round size="small">
                答错 {{ wrongWord.wrongCount }} 次
              </el-tag>
            </div>
          </div>
          <p class="word-def">{{ wrongWord.word.definition }}</p>
          <div class="word-card-meta">
            最近一次：{{ formatDate(wrongWord.lastWrongAt) }}
          </div>
          <div class="word-card-actions">
            <div class="action-left">
              <el-button size="small" round plain type="primary" @click.stop="playAudio(wrongWord.word)">
                <el-icon><Microphone /></el-icon> 发音
              </el-button>
              <el-button
                class="resolve-btn"
                size="small"
                round
                @click.stop="markAsResolved(wrongWord.word.id)"
              >
                <el-icon><Check /></el-icon> 已掌握
              </el-button>
            </div>
            <span class="view-more">查看详情 <el-icon><ArrowRight /></el-icon></span>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import AppHeader from '@/components/AppHeader.vue'
import { getUserWrongWords, markWrongWordAsResolved } from '@/api/learning'
import {
  Loading, CircleCheckFilled, Microphone, Check, ArrowRight
} from '@element-plus/icons-vue'

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
  speechSynthesis.cancel()
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
  // 跳转到查单词页，自动带出该词的详情
  router.push({ name: 'Search', query: { word: word.word } })
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN')
}

const goReview = () => router.push('/review')
const goHome = () => router.push('/')

onMounted(() => {
  loadWrongWords()
})
</script>

<style scoped>
.list-main {
  max-width: 860px;
}

/* 加载 / 空状态 */
.list-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 360px;
  padding: 40px;
  color: var(--color-ink-soft);
  text-align: center;
}
.list-state h3 {
  margin: 6px 0 0;
  font-size: 17px;
  color: var(--color-ink);
}
.list-state p {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--color-ink-faint);
}
.state-btns {
  display: flex;
  gap: 10px;
  margin-top: 6px;
}

/* 错词列表 */
.results-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.word-card {
  padding: 20px 22px;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}
.word-card:hover {
  transform: translateY(-2px);
  border-color: #f0cfca;
  box-shadow: var(--shadow-card-hover);
}
.word-card-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}
.word-head {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  min-width: 0;
}
.word-text {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: -0.01em;
  color: var(--color-ink);
}
.phonetic {
  font-size: 14px;
  color: var(--color-ink-faint);
}
.word-def {
  margin: 8px 0 0;
  font-size: 14px;
  line-height: 1.6;
  color: var(--color-ink-soft);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.word-card-meta {
  margin-top: 10px;
  font-size: 12px;
  color: var(--color-ink-faint);
}
.word-card-actions {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.action-left {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.resolve-btn {
  --el-button-bg-color: var(--color-primary-tint);
  --el-button-border-color: #b9e2cb;
  --el-button-text-color: var(--color-primary-deep);
  --el-button-hover-bg-color: var(--color-primary);
  --el-button-hover-border-color: var(--color-primary);
  --el-button-hover-text-color: #fff;
}
.view-more {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-primary-deep);
}
</style>
