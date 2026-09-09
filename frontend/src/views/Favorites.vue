<template>
  <div class="favorites-page">
    <AppHeader mode="simple" title="我的收藏" back />
    <main class="page-shell list-main">
      <!-- 加载中 -->
      <div v-if="loading" class="list-state card">
        <el-icon class="is-loading" :size="36"><Loading /></el-icon>
        <p>正在加载收藏…</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="favorites.length === 0" class="list-state card">
        <el-icon :size="48" color="var(--color-amber)"><Star /></el-icon>
        <h3>还没有收藏的单词</h3>
        <p>学习或查单词时点一下收藏，就能在这里集中复习</p>
        <div class="state-btns">
          <el-button type="primary" round @click="goSearch">去查单词</el-button>
          <el-button round @click="goHome">返回首页</el-button>
        </div>
      </div>

      <!-- 收藏列表 -->
      <div v-else class="results-list">
        <div
          v-for="favorite in favorites"
          :key="favorite.id"
          class="word-card card"
          @click="viewDetail(favorite.word)"
        >
          <div class="word-card-top">
            <div class="word-head">
              <h3 class="word-text">{{ favorite.word.word }}</h3>
              <span v-if="favorite.word.phonetic" class="phonetic">{{ favorite.word.phonetic }}</span>
            </div>
            <el-button
              class="del-btn"
              circle
              title="取消收藏"
              @click.stop="handleRemoveFromFavorites(favorite.word.id)"
            >
              <el-icon :size="15"><Delete /></el-icon>
            </el-button>
          </div>
          <p class="word-def">{{ favorite.word.definition }}</p>
          <div class="word-card-actions">
            <el-button size="small" round plain type="primary" @click.stop="playAudio(favorite.word)">
              <el-icon><Microphone /></el-icon> 发音
            </el-button>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import AppHeader from '@/components/AppHeader.vue'
import { getUserFavorites, removeFromFavorites as removeFavoriteApi } from '@/api/learning'
import {
  Loading, Star, Delete, Microphone, View, ArrowRight
} from '@element-plus/icons-vue'

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
    await ElMessageBox.confirm('取消收藏后，需要重新查找才能再次收藏该单词。', '取消收藏', {
      confirmButtonText: '取消收藏',
      cancelButtonText: '再想想',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
  } catch {
    return // 用户点"再想想"，什么都不做
  }

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

const goSearch = () => router.push({ name: 'Search' })
const goHome = () => router.push('/')

onMounted(() => {
  loadFavorites()
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

/* 收藏列表 */
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
.del-btn {
  flex-shrink: 0;
  --el-button-bg-color: #fff;
  --el-button-border-color: var(--color-border);
  --el-button-text-color: var(--color-ink-faint);
  --el-button-hover-bg-color: #fdecea;
  --el-button-hover-border-color: var(--color-rust);
  --el-button-hover-text-color: var(--color-rust);
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
.word-card-actions {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
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
