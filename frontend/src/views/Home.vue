<template>
  <div class="home-page">
    <header class="top-bar">
      <div class="top-bar-inner">
        <div class="brand">
          <div class="brand-mark"><span>词</span></div>
          <span class="brand-name dict-heading">自在背单词</span>
        </div>
        <div class="user-zone">
          <span class="user-greeting">欢迎，{{ userStore.user?.username }}</span>
          <button class="ghost-btn" @click="handleLogout">退出</button>
        </div>
      </div>
    </header>

    <main class="main-content">
      <!-- 统计区：索引卡风格 -->
      <section class="stat-row">
        <div class="stat-card">
          <div class="stat-top">
            <span class="stat-label">总单词数</span>
            <el-icon class="stat-icon" :style="{ color: 'var(--color-ink-soft)' }"><Document /></el-icon>
          </div>
          <div class="stat-value">{{ statistics.totalWords || 0 }}</div>
          <div class="stat-bar"><span :style="{ width: '100%', background: 'var(--color-ink-soft)' }"></span></div>
        </div>

        <div class="stat-card">
          <div class="stat-top">
            <span class="stat-label">已掌握</span>
            <el-icon class="stat-icon" :style="{ color: 'var(--color-moss)' }"><SuccessFilled /></el-icon>
          </div>
          <div class="stat-value" :style="{ color: 'var(--color-moss)' }">{{ statistics.masteredWords || 0 }}</div>
          <div class="stat-bar"><span :style="{ width: masteredPct + '%', background: 'var(--color-moss)' }"></span></div>
        </div>

        <div class="stat-card">
          <div class="stat-top">
            <span class="stat-label">学习中</span>
            <el-icon class="stat-icon" :style="{ color: 'var(--color-amber)' }"><Loading /></el-icon>
          </div>
          <div class="stat-value" :style="{ color: 'var(--color-amber)' }">{{ statistics.learningWords || 0 }}</div>
          <div class="stat-bar"><span :style="{ width: learningPct + '%', background: 'var(--color-amber)' }"></span></div>
        </div>

        <div class="stat-card">
          <div class="stat-top">
            <span class="stat-label">待复习</span>
            <el-icon class="stat-icon" :style="{ color: 'var(--color-rust)' }"><Clock /></el-icon>
          </div>
          <div class="stat-value" :style="{ color: 'var(--color-rust)' }">{{ statistics.dueReviews || 0 }}</div>
          <div class="stat-bar"><span :style="{ width: duePct + '%', background: 'var(--color-rust)' }"></span></div>
        </div>
      </section>

      <!-- 功能区：卡片目录 -->
      <section class="action-section">
        <div class="section-heading">
          <span class="section-eyebrow">CATALOG</span>
          <h3 class="dict-heading">今天想做点什么</h3>
        </div>

        <div class="action-grid">
          <div class="action-card featured" @click="goToLearn">
            <div class="action-tab">01</div>
            <el-icon class="action-icon" :style="{ color: 'var(--color-primary)' }" :size="32"><Reading /></el-icon>
            <h4>开始学习</h4>
            <p>学习新单词</p>
          </div>

          <div class="action-card" @click="goToReview">
            <div class="action-tab">02</div>
            <el-icon class="action-icon" :style="{ color: 'var(--color-moss)' }" :size="32"><Refresh /></el-icon>
            <h4>复习单词</h4>
            <p>艾宾浩斯智能复习</p>
          </div>

          <div class="action-card" @click="goToSearch">
            <div class="action-tab">03</div>
            <el-icon class="action-icon" :style="{ color: 'var(--color-amber)' }" :size="32"><Search /></el-icon>
            <h4>单词查询</h4>
            <p>搜索单词和例句</p>
          </div>

          <div class="action-card" @click="goToFavorites">
            <div class="action-tab">04</div>
            <el-icon class="action-icon" :style="{ color: 'var(--color-rust)' }" :size="32"><Star /></el-icon>
            <h4>我的收藏</h4>
            <p>查看收藏的单词</p>
          </div>

          <div class="action-card" @click="goToWrongWords">
            <div class="action-tab">05</div>
            <el-icon class="action-icon" :style="{ color: 'var(--color-ink-soft)' }" :size="32"><Warning /></el-icon>
            <h4>错词本</h4>
            <p>重点复习错词</p>
          </div>

          <div class="action-card" @click="goToStatistics">
            <div class="action-tab">06</div>
            <el-icon class="action-icon" :style="{ color: 'var(--color-primary)' }" :size="32"><TrendCharts /></el-icon>
            <h4>学习统计</h4>
            <p>查看学习进度</p>
          </div>

          <div v-if="userStore.user?.role === 'ADMIN'" class="action-card admin-card" @click="goToAdmin">
            <div class="action-tab">ADMIN</div>
            <el-icon class="action-icon" :style="{ color: 'var(--color-primary-deep)' }" :size="32"><Setting /></el-icon>
            <h4>管理员后台</h4>
            <p>用户与单词库管理</p>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { getUserStatistics } from '@/api/learning'

const router = useRouter()
const userStore = useUserStore()
const statistics = ref({
  totalWords: 0,
  masteredWords: 0,
  learningWords: 0,
  dueReviews: 0
})

const masteredPct = computed(() => {
  if (!statistics.value.totalWords) return 0
  return Math.min(100, Math.round((statistics.value.masteredWords / statistics.value.totalWords) * 100))
})
const learningPct = computed(() => {
  if (!statistics.value.totalWords) return 0
  return Math.min(100, Math.round((statistics.value.learningWords / statistics.value.totalWords) * 100))
})
const duePct = computed(() => {
  if (!statistics.value.totalWords) return 0
  return Math.min(100, Math.round((statistics.value.dueReviews / statistics.value.totalWords) * 100))
})

const loadStatistics = async () => {
  try {
    const data = await getUserStatistics(userStore.user.id)
    statistics.value = data
  } catch (error) {
    console.error('Failed to load statistics:', error)
  }
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

const goToLearn = () => router.push('/learn')
const goToReview = () => router.push('/review')
const goToSearch = () => router.push('/search')
const goToFavorites = () => router.push('/favorites')
const goToWrongWords = () => router.push('/wrong-words')
const goToStatistics = () => router.push('/statistics')
const goToAdmin = () => router.push('/admin')

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  background: var(--color-paper);
}

/* ---------- 顶部条 ---------- */
.top-bar {
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border-soft);
  position: sticky;
  top: 0;
  z-index: 10;
}
.top-bar-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 14px var(--space-6);
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.brand {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}
.brand-mark {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  background: var(--color-primary-deep);
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-mark span {
  font-family: var(--font-display);
  font-weight: 700;
  color: #fff;
  font-size: 16px;
}
.brand-name {
  font-size: 18px;
  font-weight: 700;
}
.user-zone {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}
.user-greeting {
  font-size: 14px;
  color: var(--color-ink-soft);
}
.ghost-btn {
  border: 1px solid var(--color-border);
  background: transparent;
  color: var(--color-ink-soft);
  padding: 7px 16px;
  border-radius: var(--radius-sm);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.15s ease;
}
.ghost-btn:hover {
  border-color: var(--color-rust);
  color: var(--color-rust);
}

/* ---------- 主体 ---------- */
.main-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-7) var(--space-6);
}

/* ---------- 统计卡 ---------- */
.stat-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-5);
  margin-bottom: var(--space-8);
}
.stat-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-card);
  padding: var(--space-5);
  box-shadow: var(--shadow-card);
}
.stat-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-3);
}
.stat-label {
  font-size: 13px;
  color: var(--color-ink-soft);
  font-weight: 500;
}
.stat-icon {
  font-size: 18px;
}
.stat-value {
  font-family: var(--font-mono);
  font-size: 34px;
  font-weight: 500;
  color: var(--color-ink);
  line-height: 1;
  margin-bottom: var(--space-4);
}
.stat-bar {
  height: 4px;
  border-radius: 2px;
  background: var(--color-border-soft);
  overflow: hidden;
}
.stat-bar span {
  display: block;
  height: 100%;
  border-radius: 2px;
  transition: width 0.4s ease;
}

/* ---------- 功能目录区 ---------- */
.section-heading {
  margin-bottom: var(--space-5);
}
.section-eyebrow {
  display: block;
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 0.16em;
  color: var(--color-primary);
  margin-bottom: 4px;
}
.section-heading h3 {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-5);
}
.action-card {
  position: relative;
  background: var(--color-surface);
  border: 1px solid var(--color-border-soft);
  border-radius: var(--radius-card);
  padding: var(--space-6) var(--space-5) var(--space-5);
  cursor: pointer;
  transition: box-shadow 0.2s ease, transform 0.2s ease, border-color 0.2s ease;
  box-shadow: var(--shadow-card);
}
.action-card:hover {
  transform: translateY(-3px);
  box-shadow: var(--shadow-card-hover);
  border-color: var(--color-primary-tint);
}
.action-card.featured {
  background: var(--color-primary-tint);
  border-color: transparent;
}
.action-card.admin-card {
  border-style: dashed;
  border-color: var(--color-primary);
  background: var(--color-paper-warm);
}
.action-tab {
  position: absolute;
  top: var(--space-4);
  right: var(--space-4);
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--color-border);
  letter-spacing: 0.06em;
}
.action-icon {
  margin-bottom: var(--space-4);
}
.action-card h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-ink);
}
.action-card p {
  margin: 0;
  font-size: 13px;
  color: var(--color-ink-soft);
}

@media (max-width: 900px) {
  .stat-row {
    grid-template-columns: repeat(2, 1fr);
  }
  .action-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 560px) {
  .stat-row {
    grid-template-columns: 1fr;
  }
  .action-grid {
    grid-template-columns: 1fr;
  }
}
</style>
<style>
.home-page{min-height:100vh;background:#f4f1e8;background-image:radial-gradient(circle at 100% 0%,#dff3bc 0 12%,transparent 12.4%),radial-gradient(circle at 0% 100%,#d9e9cd 0 15%,transparent 15.4%)}.top-bar{margin:18px auto 0;width:calc(100% - 36px);max-width:1320px;border:0;border-radius:18px;background:#0d2617;color:#fff;box-shadow:0 12px 30px rgba(13,38,23,.16)}.top-bar-inner{padding:16px 28px}.brand-mark{background:#c6f19d;border-radius:50%}.brand-mark span{color:#102318}.brand-name{color:#fff}.user-greeting{color:#d6e4cf}.ghost-btn{border-color:rgba(198,241,157,.55);color:#c6f19d;border-radius:999px}.main-content{max-width:1260px;padding-top:42px}.stat-card,.action-card{border:1px solid #d6ddc9;border-radius:22px;background:#fffdf7;box-shadow:0 12px 30px rgba(24,47,30,.08)}.stat-card:first-child{background:#0d2617;color:#fff}.stat-card:first-child .stat-label{color:#d6e4cf}.action-card{min-height:172px;padding:26px;position:relative;overflow:hidden}.action-card.featured{background:#c6f19d;border-color:#c6f19d}.action-card.featured::after{content:'Aa';position:absolute;right:18px;bottom:-26px;font:800 112px/1 var(--font-display);color:rgba(13,38,23,.12)}.action-tab{background:#0d2617;color:#c6f19d;border-radius:999px;padding:4px 9px}.section-eyebrow{color:#4d7a41;letter-spacing:.15em}.section-heading h3{font-size:34px}
</style>
<style>
.home-page{background:#edf3e4!important;background-image:radial-gradient(circle at 8% 10%,rgba(194,239,146,.42),transparent 24rem),radial-gradient(circle at 92% 86%,rgba(173,207,145,.38),transparent 28rem)!important}.home-page .main-content{max-width:1240px!important;margin:0 auto}.home-page .stat-card:first-child .stat-value,.home-page .stat-card:first-child .stat-icon{color:#fff!important}.home-page .stat-card:first-child .stat-bar{background:rgba(255,255,255,.25)!important}.home-page .stat-card:first-child .stat-bar span{background:#dff6a8!important}.home-page .stat-card{min-height:174px}.home-page .action-grid{align-items:stretch}.home-page .action-card{min-height:190px}
</style>