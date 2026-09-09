<template>
  <div class="search-page">
    <AppHeader mode="simple" title="查单词" back />
    <main class="page-shell search-main">
      <!-- 搜索框 -->
      <div class="search-box card">
        <el-input
          v-model="searchKeyword"
          placeholder="输入英文单词或中文释义，回车即可搜索"
          size="large"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" size="large" round :loading="loading" @click="handleSearch">
          搜索
        </el-button>
      </div>

      <!-- 搜索中 -->
      <div v-if="loading" class="list-state card">
        <el-icon class="is-loading" :size="36"><Loading /></el-icon>
        <p>正在搜索…</p>
      </div>

      <template v-else>
        <!-- 结果列表 -->
        <div v-if="searchResults.length > 0" class="results-list">
          <div
            v-for="word in searchResults"
            :key="word.id"
            class="word-card card"
            @click="showWordDetail(word)"
          >
            <div class="word-card-top">
              <div class="word-head">
                <h3 class="word-text">{{ word.word }}</h3>
                <span v-if="word.phonetic" class="phonetic">{{ word.phonetic }}</span>
              </div>
              <el-tag :type="getDifficultyType(word.difficultyLevel)" effect="light" round>
                {{ getDifficultyText(word.difficultyLevel) }}
              </el-tag>
            </div>
            <p class="word-def">{{ word.definition }}</p>
            <div class="word-card-foot">
              <span class="view-more">查看详情与例句 <el-icon><ArrowRight /></el-icon></span>
            </div>
          </div>
        </div>

        <!-- 无结果 -->
        <div v-else-if="hasSearched" class="list-state card">
          <el-icon :size="44" color="var(--color-ink-faint)"><Search /></el-icon>
          <h3>没有找到相关单词</h3>
          <p>换个关键词试试，比如只输入单词本身</p>
        </div>

        <!-- 初始引导 -->
        <div v-else class="list-state card">
          <el-icon :size="44" color="var(--color-ink-faint)"><Search /></el-icon>
          <h3>输入关键词开始查找</h3>
          <p>支持英文单词、中文释义的模糊搜索</p>
        </div>
      </template>
    </main>

    <!-- 单词详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="单词详情" width="620px" align-center>
      <div v-if="selectedWord" class="word-detail">
        <div class="detail-head">
          <div>
            <h2 class="detail-word">{{ selectedWord.word }}</h2>
            <div class="detail-meta">
              <span v-if="selectedWord.partOfSpeech" class="pos-chip">{{ selectedWord.partOfSpeech }}</span>
              <span v-if="selectedWord.phonetic" class="phonetic">{{ selectedWord.phonetic }}</span>
            </div>
          </div>
          <el-button class="sound-btn" circle title="播放发音" @click="playWordAudio">
            <el-icon :size="18"><Microphone /></el-icon>
          </el-button>
        </div>

        <div class="detail-block">
          <span class="block-tag">释义</span>
          <p>{{ selectedWord.definition }}</p>
        </div>

        <div v-if="selectedWord.aiDefinition" class="detail-block">
          <span class="block-tag ai">AI 详细释义</span>
          <p>{{ selectedWord.aiDefinition }}</p>
        </div>

        <div v-if="detailExamples.length > 0" class="detail-block">
          <span class="block-tag">例句</span>
          <div v-for="(example, index) in detailExamples" :key="index" class="example-item">
            <p class="example-en">
              <span class="example-no">{{ index + 1 }}</span>{{ example.sentence }}
            </p>
            <p v-if="example.translation" class="example-tr">{{ example.translation }}</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/AppHeader.vue'
import { searchWords, getExampleSentences } from '@/api/word'
import {
  Search, Loading, ArrowRight, Microphone
} from '@element-plus/icons-vue'

const route = useRoute()

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
  detailExamples.value = []
  detailDialogVisible.value = true

  try {
    const examples = await getExampleSentences(word.id)
    detailExamples.value = examples
  } catch (error) {
    console.error('Load examples error:', error)
  }
}

const playWordAudio = () => {
  speechSynthesis.cancel()
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
    EASY: 'success',
    MEDIUM: 'warning',
    HARD: 'danger'
  }
  return types[level] || 'info'
}

const getDifficultyText = (level) => {
  const texts = {
    EASY: '简单',
    MEDIUM: '中等',
    HARD: '困难'
  }
  return texts[level] || level
}

// 从"我的收藏"、"错词本"点"详情"跳转过来时，地址栏会带上 ?word=xxx 这个参数，
// 进页面时检测到就自动填词搜索，命中完全匹配的单词时直接打开详情弹窗
onMounted(async () => {
  const wordFromQuery = route.query.word
  if (wordFromQuery) {
    searchKeyword.value = wordFromQuery
    await handleSearch()

    const exactMatch = searchResults.value.find(
      (w) => w.word.toLowerCase() === wordFromQuery.toLowerCase()
    )
    if (exactMatch) {
      showWordDetail(exactMatch)
    }
  }
})
</script>

<style scoped>
.search-main {
  max-width: 860px;
}

/* 搜索框 */
.search-box {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  margin-bottom: 24px;
}
.search-box .el-input {
  flex: 1;
}

/* 结果列表 */
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
  border-color: #bfe4cf;
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
  align-items: baseline;
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
.word-card-foot {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}
.view-more {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-primary-deep);
}

/* 加载 / 空状态 */
.list-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 300px;
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
  margin: 0;
  font-size: 13px;
  color: var(--color-ink-faint);
}

/* 详情弹窗 */
.word-detail {
  padding: 6px 2px 2px;
}
.detail-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 1px dashed var(--color-border-strong);
}
.detail-word {
  margin: 0;
  font-size: 30px;
  font-weight: 800;
  letter-spacing: -0.01em;
  color: var(--color-ink);
}
.detail-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 6px;
}
.pos-chip {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary-deep);
  background: var(--color-primary-tint);
  padding: 2px 10px;
  border-radius: 999px;
}
.sound-btn {
  --el-button-bg-color: var(--color-primary-tint);
  --el-button-border-color: var(--color-primary-tint);
  --el-button-text-color: var(--color-primary-deep);
  --el-button-hover-bg-color: var(--color-primary);
  --el-button-hover-border-color: var(--color-primary);
  --el-button-hover-text-color: #fff;
}
.detail-block {
  margin-bottom: 16px;
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
  color: var(--color-blue);
  background: #e7f1fc;
}
.detail-block p {
  margin: 0;
  font-size: 14.5px;
  line-height: 1.7;
  color: var(--color-ink);
}
.example-item {
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 12px 14px;
  margin-bottom: 10px;
}
.example-en {
  margin: 0;
  font-size: 14.5px;
  color: var(--color-ink);
  line-height: 1.6;
}
.example-no {
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
.example-tr {
  margin: 6px 0 0;
  padding-left: 28px;
  font-size: 13px;
  color: var(--color-ink-soft);
}
</style>
