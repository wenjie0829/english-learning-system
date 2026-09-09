<template>
  <div class="review-page">
    <AppHeader mode="simple" title="单词复习" back />

    <main class="flash-shell">
      <div v-if="loading" class="flash-state card">
        <el-icon class="is-loading" :size="36"><Loading /></el-icon>
        <p>正在整理待复习的单词…</p>
      </div>

      <div v-else-if="reviewWords.length === 0" class="flash-state card">
        <el-icon :size="44" color="var(--color-moss)"><CircleCheckFilled /></el-icon>
        <h3>暂时没有待复习的单词</h3>
        <p>去学几个新词，明天它们就会出现在这里</p>
        <el-button type="primary" round @click="goLearn">去学习</el-button>
        <el-button round @click="goBack">返回首页</el-button>
      </div>

      <template v-else>
        <!-- 进度 -->
        <div class="flash-progress">
          <span class="flash-count">{{ currentIndex + 1 }} / {{ reviewWords.length }}</span>
          <div class="flash-track">
            <div class="flash-fill" :style="{ width: progressPct + '%' }"></div>
          </div>
          <span class="flash-done">还剩 {{ reviewWords.length - currentIndex - 1 }} 个</span>
        </div>

        <!-- 单词卡 -->
        <div class="flash-card card">
          <div class="flash-actions">
            <el-button circle class="round-btn" title="播放发音" @click="playAudio">
              <el-icon :size="18"><Microphone /></el-icon>
            </el-button>
          </div>

          <div class="word-main">
            <span class="seen-chip">第 {{ currentReviewRecord.reviewCount }} 轮复习</span>
            <h1 class="word-text">{{ currentWord.word }}</h1>
            <div class="word-meta">
              <span v-if="currentWord.partOfSpeech" class="pos-chip">{{ currentWord.partOfSpeech }}</span>
              <span v-if="currentWord.phonetic" class="phonetic">{{ currentWord.phonetic }}</span>
            </div>
          </div>

          <div class="word-meaning">
            <div class="meaning-block">
              <span class="block-tag">释义</span>
              <p>{{ currentWord.definition }}</p>
            </div>

            <div v-if="exampleSentences.length > 0" class="examples-block">
              <span class="block-tag">例句</span>
              <div v-for="(example, index) in exampleSentences" :key="index" class="example-item">
                <div class="example-en">
                  <span class="example-number">{{ index + 1 }}</span>
                  {{ example.sentence }}
                </div>
                <div v-if="example.translation" class="example-translation">{{ example.translation }}</div>
              </div>
            </div>
          </div>

          <!-- 自评 -->
          <div class="self-check">
            <p class="self-check-hint">现在还能想起来这个词的意思吗？</p>
            <div class="self-check-btns">
              <el-button class="check-btn no" round size="large" :loading="actionLoading" @click="handleReview(false)">
                <el-icon :size="18"><Close /></el-icon> 不记得
              </el-button>
              <el-button class="check-btn yes" round size="large" :loading="actionLoading" @click="handleReview(true)">
                <el-icon :size="18"><Check /></el-icon> 记得
              </el-button>
            </div>
          </div>
        </div>
      </template>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import AppHeader from '@/components/AppHeader.vue'
import { getDueReviews, reviewWord } from '@/api/learning'
import { getExampleSentences } from '@/api/word'
import {
  Microphone, Close, Check, Loading, CircleCheckFilled
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const reviewWords = ref([])
const exampleSentences = ref([])
const currentIndex = ref(0)
const loading = ref(true)
const actionLoading = ref(false)

const currentReviewRecord = computed(() => reviewWords.value[currentIndex.value] || {})
const currentWord = computed(() => currentReviewRecord.value.word || {})
const progressPct = computed(() => {
  if (!reviewWords.value.length) return 0
  return Math.round(((currentIndex.value + 1) / reviewWords.value.length) * 100)
})

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

const speak = (text) => {
  speechSynthesis.cancel()
  const utterance = new SpeechSynthesisUtterance(text)
  utterance.lang = 'en-US'
  utterance.rate = 0.95
  speechSynthesis.speak(utterance)
}

const playAudio = () => {
  if (currentWord.value.audioUrl) {
    speechSynthesis.cancel()
    const audio = new Audio(currentWord.value.audioUrl)
    audio.play()
  } else {
    speak(currentWord.value.word)
  }
}

const handleReview = async (isCorrect) => {
  if (actionLoading.value) return
  actionLoading.value = true
  try {
    // 后端仍会按照记忆曲线安排这个词的下一次出现时间，前端只负责收集对错
    await reviewWord(userStore.user.id, currentWord.value.id, isCorrect)

    if (isCorrect) {
      ElMessage.success('记得很棒！')
    } else {
      ElMessage.warning('已记录，稍后会再见到它')
    }
    nextWord()
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('Review error:', error)
  } finally {
    actionLoading.value = false
  }
}

const nextWord = () => {
  if (currentIndex.value < reviewWords.value.length - 1) {
    currentIndex.value++
    loadExampleSentences(currentWord.value.id)
  } else {
    ElMessage.success('本轮复习完成，继续保持！')
    router.push('/')
  }
}

const goBack = () => router.push('/')
const goLearn = () => router.push('/learn')

onMounted(loadReviewWords)
</script>

<style scoped>
.flash-shell {
  max-width: 760px;
  margin: 0 auto;
  padding: 20px 20px 72px;
}

/* 加载 / 空状态 */
.flash-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 360px;
  color: var(--color-ink-soft);
  padding: 32px;
  text-align: center;
}
.flash-state h3 {
  margin: 6px 0 0;
  color: var(--color-ink);
  font-size: 18px;
}
.flash-state p {
  margin: 0 0 10px;
  font-size: 13px;
}

/* 进度 */
.flash-progress {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 18px;
}
.flash-count {
  font-size: 14px;
  font-weight: 700;
  color: var(--color-ink-soft);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}
.flash-done {
  font-size: 12px;
  color: var(--color-ink-faint);
  white-space: nowrap;
}
.flash-track {
  flex: 1;
  height: 8px;
  border-radius: 999px;
  background: var(--color-border);
  overflow: hidden;
}
.flash-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #2f9ed8, #4a90e2);
  transition: width 0.35s ease;
}

/* 单词卡 */
.flash-card {
  position: relative;
  padding: 28px 32px 32px;
}
.flash-actions {
  position: absolute;
  top: 18px;
  right: 20px;
  display: flex;
  gap: 8px;
}
.round-btn {
  --el-button-bg-color: var(--color-surface-2);
  --el-button-border-color: var(--color-border);
  --el-button-text-color: var(--color-ink-soft);
  --el-button-hover-bg-color: #e7f1fc;
  --el-button-hover-border-color: var(--color-blue);
  --el-button-hover-text-color: var(--color-blue);
}

.word-main {
  text-align: center;
  margin: 14px 0 22px;
}
.seen-chip {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-blue);
  background: #e7f1fc;
  border-radius: 999px;
  padding: 3px 12px;
  margin-bottom: 10px;
}
.word-text {
  margin: 0;
  font-size: clamp(40px, 7vw, 58px);
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1.15;
  color: var(--color-ink);
}
.word-meta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 8px;
  flex-wrap: wrap;
}
.pos-chip {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary-deep);
  background: var(--color-primary-tint);
  padding: 2px 10px;
  border-radius: 999px;
}
.phonetic {
  font-size: 17px;
  color: var(--color-ink-faint);
}

/* 释义区 */
.word-meaning {
  border-top: 1px dashed var(--color-border-strong);
  padding-top: 20px;
}
.meaning-block {
  margin-bottom: 14px;
}
.block-tag {
  display: inline-block;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: var(--color-primary-deep);
  background: var(--color-primary-tint);
  border-radius: 6px;
  padding: 2px 8px;
  margin-bottom: 8px;
}
.meaning-block p {
  margin: 0;
  font-size: 15px;
  line-height: 1.7;
  color: var(--color-ink);
}

/* 例句 */
.examples-block {
  margin-top: 4px;
}
.example-item {
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 12px 14px;
  margin-bottom: 10px;
}
.example-en {
  font-size: 14.5px;
  color: var(--color-ink);
  line-height: 1.6;
}
.example-number {
  display: inline-block;
  min-width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  background: var(--color-blue);
  border-radius: 6px;
  margin-right: 8px;
}
.example-translation {
  margin-top: 6px;
  font-size: 13px;
  color: var(--color-ink-soft);
  padding-left: 28px;
}

/* 自评按钮 */
.self-check {
  border-top: 1px solid var(--color-border);
  margin-top: 22px;
  padding-top: 18px;
  text-align: center;
}
.self-check-hint {
  margin: 0 0 12px;
  font-size: 13px;
  color: var(--color-ink-faint);
}
.self-check-btns {
  display: flex;
  gap: 14px;
  justify-content: center;
}
.check-btn {
  flex: 1;
  max-width: 190px;
  height: 46px;
  font-weight: 700;
  font-size: 15px;
}
.check-btn.yes {
  --el-button-bg-color: var(--color-primary);
  --el-button-border-color: var(--color-primary);
  --el-button-text-color: #fff;
  --el-button-hover-bg-color: var(--color-primary-deep);
  --el-button-hover-border-color: var(--color-primary-deep);
  box-shadow: 0 8px 18px rgba(23, 160, 92, 0.28);
}
.check-btn.no {
  --el-button-bg-color: #fff;
  --el-button-border-color: #f0c6bf;
  --el-button-text-color: var(--color-rust);
  --el-button-hover-bg-color: #fdecea;
  --el-button-hover-border-color: var(--color-rust);
}
</style>
