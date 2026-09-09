<template>
  <div class="learn-page">
    <AppHeader mode="simple" title="开始学习" back />

    <main class="flash-shell">
      <div v-if="loading" class="flash-state card">
        <el-icon class="is-loading" :size="36"><Loading /></el-icon>
        <p>正在准备今天的单词…</p>
      </div>

      <div v-else-if="words.length === 0" class="flash-state card">
        <el-icon :size="44" color="var(--color-ink-faint)"><Box /></el-icon>
        <h3>词库还是空的</h3>
        <p>等管理员导入单词后，就可以开始学习了</p>
        <el-button type="primary" round @click="goBack">返回首页</el-button>
      </div>

      <template v-else>
        <!-- 进度条 -->
        <div class="flash-progress">
          <span class="flash-count">{{ currentIndex + 1 }} / {{ words.length }}</span>
          <div class="flash-track">
            <div class="flash-fill" :style="{ width: progressPct + '%' }"></div>
          </div>
        </div>

        <!-- 单词卡 -->
        <div class="flash-card card">
          <div class="flash-actions">
            <el-button
              circle
              class="round-btn"
              :class="{ 'is-starred': isFavorited }"
              :loading="favoriteLoading"
              :title="isFavorited ? '取消收藏' : '收藏这个单词'"
              @click="toggleFavorite"
            >
              <el-icon :size="18"><StarFilled v-if="isFavorited" /><Star v-else /></el-icon>
            </el-button>
            <el-button circle class="round-btn" title="播放发音" @click="playAudio">
              <el-icon :size="18"><Microphone /></el-icon>
            </el-button>
          </div>

          <div class="word-main">
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

            <div
              v-if="currentWord.aiDefinition || aiDefinition || generatingDefinition"
              class="meaning-block ai-block"
            >
              <span class="block-tag ai">AI 详解</span>
              <p v-if="currentWord.aiDefinition || aiDefinition">{{ currentWord.aiDefinition || aiDefinition }}</p>
              <el-button
                v-if="generatingDefinition"
                type="primary"
                size="small"
                round
                :loading="true"
              >正在生成…</el-button>
            </div>
            <el-button
              v-if="!currentWord.aiDefinition && !aiDefinition"
              class="ai-trigger"
              type="primary"
              plain
              round
              size="small"
              :loading="generatingDefinition"
              @click="generateAIDefinition"
            >
              <el-icon :size="14"><MagicStick /></el-icon> 生成 AI 详细释义
            </el-button>

            <div v-if="exampleSentences.length > 0" class="examples-block">
              <span class="block-tag">例句</span>
              <div v-for="(example, index) in exampleSentences" :key="index" class="example-item">
                <div class="example-sentence">
                  <span class="example-en">
                    <span class="example-number">{{ index + 1 }}</span>
                    {{ example.sentence }}
                  </span>
                  <el-button
                    circle
                    class="round-btn sm"
                    size="small"
                    title="播放例句"
                    @click="playExampleAudio(example)"
                  >
                    <el-icon :size="14"><Microphone /></el-icon>
                  </el-button>
                </div>
                <div v-if="example.translation" class="example-translation">{{ example.translation }}</div>
              </div>
            </div>
          </div>

          <!-- 自评 -->
          <div class="self-check">
            <p class="self-check-hint">这个单词你认识吗？</p>
            <div class="self-check-btns">
              <el-button class="check-btn no" round size="large" :loading="actionLoading" @click="markAsWrong">
                <el-icon :size="18"><Close /></el-icon> 不认识
              </el-button>
              <el-button class="check-btn yes" round size="large" :loading="actionLoading" @click="markAsKnown">
                <el-icon :size="18"><Check /></el-icon> 认识
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
import { getAllWords, getExampleSentences } from '@/api/word'
import { startLearning, reviewWord, addToFavorites, removeFromFavorites, getUserFavorites } from '@/api/learning'
import { generateDefinition } from '@/api/ai'
import {
  Star, StarFilled, Microphone, Close, Check, Loading, Box, MagicStick
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const words = ref([])
const exampleSentences = ref([])
const currentIndex = ref(0)
const loading = ref(true)
const actionLoading = ref(false)
const aiDefinition = ref('')
const generatingDefinition = ref(false)

// 收藏相关：本地维护一份"已收藏单词ID"的集合，切换单词时用它判断当前单词是否已收藏
const favoriteWordIds = ref(new Set())
const favoriteLoading = ref(false)

const currentWord = computed(() => words.value[currentIndex.value] || {})
const isFavorited = computed(() => currentWord.value.id != null && favoriteWordIds.value.has(currentWord.value.id))
const progressPct = computed(() => {
  if (!words.value.length) return 0
  return Math.round(((currentIndex.value + 1) / words.value.length) * 100)
})

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

const speak = (text) => {
  // 清空浏览器TTS队列，让新朗读立即打断旧内容，避免Chrome重复循环朗读的老毛病
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

const playExampleAudio = (example) => {
  if (example.audioUrl) {
    speechSynthesis.cancel()
    const audio = new Audio(example.audioUrl)
    audio.play()
  } else {
    speak(example.sentence)
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
      favoriteWordIds.value = new Set(favoriteWordIds.value)
      ElMessage.success('已取消收藏')
    } else {
      await addToFavorites(userStore.user.id, wordId)
      favoriteWordIds.value.add(wordId)
      favoriteWordIds.value = new Set(favoriteWordIds.value)
      ElMessage.success('已加入收藏夹')
    }
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('Toggle favorite error:', error)
  } finally {
    favoriteLoading.value = false
  }
}

const markAsKnown = async () => {
  if (actionLoading.value) return
  actionLoading.value = true
  try {
    await startLearning(userStore.user.id, currentWord.value.id)
    await reviewWord(userStore.user.id, currentWord.value.id, true)
    ElMessage.success('认识 ✓ 已安排后续复习')
    nextWord()
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('Mark as known error:', error)
  } finally {
    actionLoading.value = false
  }
}

const markAsWrong = async () => {
  if (actionLoading.value) return
  actionLoading.value = true
  try {
    await startLearning(userStore.user.id, currentWord.value.id)
    await reviewWord(userStore.user.id, currentWord.value.id, false)
    ElMessage.warning('不认识，已记入错词本')
    nextWord()
  } catch (error) {
    ElMessage.error('操作失败')
    console.error('Mark as wrong error:', error)
  } finally {
    actionLoading.value = false
  }
}

const generateAIDefinition = async () => {
  try {
    generatingDefinition.value = true
    const response = await generateDefinition(currentWord.value.word, currentWord.value.partOfSpeech)
    aiDefinition.value = response.definition
    ElMessage.success('AI 释义生成成功')
  } catch (error) {
    ElMessage.error('AI 释义生成失败')
    console.error('Generate AI definition error:', error)
  } finally {
    generatingDefinition.value = false
  }
}

const nextWord = () => {
  if (currentIndex.value < words.value.length - 1) {
    currentIndex.value++
    aiDefinition.value = ''
    loadExampleSentences(words.value[currentIndex.value].id)
  } else {
    ElMessage.success('恭喜！已完成本轮学习')
    router.push('/')
  }
}

const goBack = () => router.push('/')

onMounted(() => {
  loadWords()
  loadFavorites()
})
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
  background: linear-gradient(90deg, var(--color-primary), #3eb579);
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
  --el-button-hover-bg-color: var(--color-primary-tint);
  --el-button-hover-border-color: var(--color-primary);
  --el-button-hover-text-color: var(--color-primary-deep);
}
.round-btn.is-starred {
  --el-button-bg-color: #fdf1e3;
  --el-button-border-color: #f0d8ae;
  --el-button-text-color: #e6a23c;
}
.round-btn.sm {
  width: 26px;
  height: 26px;
}

.word-main {
  text-align: center;
  margin: 18px 0 22px;
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
.block-tag.ai {
  color: #2f6db8;
  background: #e7f1fc;
}
.meaning-block p {
  margin: 0;
  font-size: 15px;
  line-height: 1.7;
  color: var(--color-ink);
}
.ai-block {
  background: linear-gradient(180deg, #f4f9ff, #eef6ff);
  border: 1px solid #d9e9fb;
  border-radius: 14px;
  padding: 14px 16px;
}
.ai-trigger {
  margin-bottom: 14px;
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
.example-sentence {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
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
  background: var(--color-primary);
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
