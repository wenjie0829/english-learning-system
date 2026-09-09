<template>
  <div class="home-page">
    <AppHeader mode="main" />

    <main class="page-shell home-main">
      <!-- ============ Hero：问候 + 今日打卡状态 ============ -->
      <section class="hero">
        <div class="hero-main">
          <p class="hero-greeting">{{ greetingText }}，{{ userStore.user?.username }}</p>
          <h1 class="hero-title">
            {{ todayGoalText }}
          </h1>
          <p class="hero-sub">
            {{ subText }}
          </p>
          <div class="hero-actions">
            <el-button type="primary" round size="large" class="cta-primary" @click="goTo('/learn')">
              <el-icon :size="17" class="cta-icon"><Reading /></el-icon>
              {{ dueReviews > 0 ? '继续学习' : '开始背单词' }}
            </el-button>
            <el-button round size="large" class="cta-secondary" @click="goTo('/review')">
              待复习 {{ dueReviews }}
              <el-icon class="el-icon--right"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>

        <div class="hero-checkin">
          <div class="checkin-ring" :class="{ done: overview.checkedInToday }">
            <template v-if="overview.checkedInToday">
              <el-icon :size="34"><CircleCheckFilled /></el-icon>
              <span class="checkin-label">今日已打卡</span>
            </template>
            <template v-else>
              <span class="ring-num">{{ streakDays }}</span>
              <span class="ring-unit">天连续</span>
            </template>
          </div>
          <p class="checkin-hint" v-if="!overview.checkedInToday">完成一次学习 / 复习即可自动打卡</p>
          <p class="checkin-hint" v-else>今日学了 {{ overview.todayLearned }} 个新词 · 复习 {{ overview.todayReviewed }} 个</p>
        </div>
      </section>

      <!-- ============ 打卡数据三连卡 ============ -->
      <section class="stat-strip">
        <div class="mini-stat card">
          <div class="mini-stat-icon" style="background: var(--color-primary-tint); color: var(--color-primary-deep)">
            <el-icon :size="20"><Odometer /></el-icon>
          </div>
          <div class="mini-stat-body">
            <div class="mini-stat-value">{{ streakDays }}<span class="unit">天</span></div>
            <div class="mini-stat-label">连续打卡</div>
          </div>
        </div>

        <div class="mini-stat card">
          <div class="mini-stat-icon" style="background: var(--color-blue-tint, #eaf1fb); color: var(--color-blue)">
            <el-icon :size="20"><Calendar /></el-icon>
          </div>
          <div class="mini-stat-body">
            <div class="mini-stat-value">{{ totalCheckIns }}<span class="unit">天</span></div>
            <div class="mini-stat-label">累计打卡</div>
          </div>
        </div>

        <div class="mini-stat card">
          <div class="mini-stat-icon" style="background: #fdf1e3; color: var(--color-amber)">
            <el-icon :size="20"><TrendCharts /></el-icon>
          </div>
          <div class="mini-stat-body">
            <div class="mini-stat-value">{{ todayTotal }}<span class="unit">个</span></div>
            <div class="mini-stat-label">今日学习总量</div>
          </div>
        </div>

        <div class="mini-stat card" :class="{ warning: dueReviews > 0 }">
          <div class="mini-stat-icon" :style="dueReviews > 0
            ? 'background:#fdecea; color:var(--color-rust)'
            : 'background:var(--color-primary-tint); color:var(--color-moss)'">
            <el-icon :size="20"><Refresh /></el-icon>
          </div>
          <div class="mini-stat-body">
            <div class="mini-stat-value" :style="dueReviews > 0 ? 'color:var(--color-rust)' : ''">{{ dueReviews }}<span class="unit">个</span></div>
            <div class="mini-stat-label">{{ dueReviews > 0 ? '等待复习' : '暂无待复习' }}</div>
          </div>
        </div>
      </section>

      <div class="home-grid">
        <!-- ============ 近 30 天打卡热力日历 ============ -->
        <section class="card panel heat-panel">
          <div class="panel-head">
            <div>
              <h2>近 30 天打卡</h2>
              <p>学习或复习过的日期即为打卡成功</p>
            </div>
            <div class="heat-legend">
              <span>少</span>
              <i v-for="l in 4" :key="l" class="heat-dot" :class="'lv' + l"></i>
              <span>多</span>
            </div>
          </div>

          <div v-if="heatRows.length" class="heat-board">
            <div class="heat-weekdays">
              <span v-for="w in ['一', '二', '三', '四', '五', '六', '日']" :key="w">{{ w }}</span>
            </div>
            <div class="heat-body">
              <div class="heat-row" v-for="(row, ri) in heatRows" :key="ri">
                <template v-for="(cell, ci) in row" :key="ci">
                  <div
                    v-if="cell"
                    class="heat-cell"
                    :class="[levelOf(cell) === 0 ? 'empty' : 'lv' + levelOf(cell), cell.isToday ? 'today' : '']"
                    :title="cell.title"
                  >
                    <span v-if="cell.isToday" class="today-dot"></span>
                  </div>
                  <div v-else class="heat-cell blank"></div>
                </template>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无学习记录，快去背几个单词吧" :image-size="72" />
        </section>

        <!-- ============ 系统公告 ============ -->
        <section class="card panel announce-panel">
          <div class="panel-head">
            <div>
              <h2>系统公告</h2>
              <p>最新学习动态与系统消息</p>
            </div>
            <router-link to="/statistics" class="more-link">更多 <el-icon><ArrowRight /></el-icon></router-link>
          </div>

          <div v-if="announcements.length" class="announce-list">
            <div v-for="ann in announcements" :key="ann.id" class="announce-item">
              <span class="announce-badge">公告</span>
              <div class="announce-body">
                <h3 class="announce-title">{{ ann.title }}</h3>
                <p class="announce-excerpt">{{ ann.content }}</p>
                <span class="announce-time">{{ formatDate(ann.createdAt) }}</span>
              </div>
            </div>
          </div>
          <div v-else class="announce-empty">
            <el-icon :size="34"><Bell /></el-icon>
            <p>暂无公告</p>
          </div>
        </section>
      </div>

      <!-- ============ 快捷入口 ============ -->
      <section class="quick-section">
        <div class="panel-head">
          <h2>快捷入口</h2>
          <p>选择今天想做的事</p>
        </div>
        <div class="quick-grid">
          <div class="quick-card card" v-for="q in quickLinks" :key="q.to" @click="goTo(q.to)">
            <span class="quick-icon" :style="{ background: q.bg, color: q.color }">
              <el-icon :size="22"><component :is="q.icon" /></el-icon>
            </span>
            <div class="quick-info">
              <h4>{{ q.label }}</h4>
              <p>{{ q.desc }}</p>
            </div>
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
import {
  Reading, Refresh, Search, Star, Warning, TrendCharts, Setting,
  ArrowRight, CircleCheckFilled, Odometer, Calendar, Bell
} from '@element-plus/icons-vue'
import AppHeader from '@/components/AppHeader.vue'
import { getUserStatistics, getLearningOverview } from '@/api/learning'
import { getActiveAnnouncements } from '@/api/announcement'

const router = useRouter()
const userStore = useUserStore()

const overview = ref({ checkedInToday: false, streakDays: 0, totalCheckIns: 0, todayLearned: 0, todayReviewed: 0, calendar: [] })
const statistics = ref({ dueReviews: 0 })
const announcements = ref([])

const streakDays = computed(() => overview.value.streakDays || 0)
const totalCheckIns = computed(() => overview.value.totalCheckIns || 0)
const dueReviews = computed(() => statistics.value.dueReviews || 0)
const todayTotal = computed(() => (overview.value.todayLearned || 0) + (overview.value.todayReviewed || 0))

const hour = new Date().getHours()
const greetingText = hour < 6 ? '夜深了' : hour < 12 ? '早上好' : hour < 14 ? '中午好' : hour < 18 ? '下午好' : '晚上好'
const todayGoalText = computed(() => {
  if (!overview.value.checkedInToday) return '今天还没有学习，开始今天的打卡吧'
  return dueReviews.value > 0 ? '今天已经打过卡，还有单词等着复习' : '今天打过卡啦，继续保持这份节奏'
})
const subText = computed(() => {
  if (!overview.value.checkedInToday) return '学习新词或完成复习会自动记录为打卡，无需手动操作'
  if (dueReviews.value > 0) return `有 ${dueReviews.value} 个单词进入复习队列，趁热打铁效果更好`
  return '学有余力的话，也可以去查一查单词、逛逛错词本'
})

// 近 30 天打卡热力
const parseDate = (s) => {
  const [y, m, d] = s.split('-').map(Number)
  return new Date(y, m - 1, d)
}
const dateKey = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`

const heatRows = computed(() => {
  const todayKey = dateKey(new Date())
  const entries = (overview.value.calendar || []).map(e => {
    const date = parseDate(e.date)
    const amount = (e.learned || 0) + (e.reviewed || 0)
    const level = e.checkedIn ? (amount >= 10 ? 4 : amount >= 5 ? 3 : amount >= 2 ? 2 : 1) : 0
    return {
      ...e,
      isToday: e.date === todayKey,
      level,
      title: `${e.date}${e.checkedIn ? ` 打卡✓ 学${e.learned} · 复习${e.reviewed}` : ' 未打卡'}`
    }
  })
  const rows = []
  let cur = null
  for (const cell of entries) {
    const col = (parseDate(cell.date).getDay() + 6) % 7 // 周一起始
    if (!cur) {
      cur = { cells: new Array(col).fill(null) }
    } else if (col === 0) {
      rows.push(cur)
      cur = { cells: [] }
    }
    cur.cells.push(cell)
  }
  if (cur) {
    while (cur.cells.length < 7) cur.cells.push(null)
    rows.push(cur)
  }
  return rows
})

const levelOf = (cell) => cell.level

const quickLinks = computed(() => {
  const links = [
    { to: '/learn', label: '开始学习', desc: '学习一批新单词', icon: Reading, bg: 'var(--color-primary-tint)', color: 'var(--color-primary-deep)' },
    { to: '/review', label: '单词复习', desc: `${dueReviews.value} 个待复习`, icon: Refresh, bg: '#fdf1e3', color: 'var(--color-amber)' },
    { to: '/search', label: '查单词', desc: '搜索单词与例句', icon: Search, bg: '#eaf1fb', color: 'var(--color-blue)' },
    { to: '/favorites', label: '收藏夹', desc: '收藏的单词', icon: Star, bg: '#fdecea', color: 'var(--color-rust)' },
    { to: '/wrong-words', label: '错词本', desc: '攻克易错词', icon: Warning, bg: '#f0f0f0', color: 'var(--color-ink-soft)' },
    { to: '/statistics', label: '学习统计', desc: '掌握进度图表', icon: TrendCharts, bg: 'var(--color-primary-tint)', color: 'var(--color-primary-deep)' }
  ]
  if (userStore.user?.role === 'ADMIN') {
    links.push({ to: '/admin', label: '管理后台', desc: '数据看板与用户', icon: Setting, bg: '#eef2e4', color: 'var(--color-primary-dark)' })
  }
  return links
})

const formatDate = (s) => (s || '').slice(0, 10)

const goTo = (path) => router.push(path)

const load = async () => {
  try {
    const [ov, st, ann] = await Promise.all([
      getLearningOverview(userStore.user.id),
      getUserStatistics(userStore.user.id),
      getActiveAnnouncements().catch(() => [])
    ])
    overview.value = ov
    statistics.value = st
    announcements.value = ann || []
  } catch (e) {
    console.error('首页数据加载失败:', e)
  }
}

onMounted(load)
</script>

<style scoped>
.home-main {
  padding-top: 24px;
}

/* ================= Hero ================= */
.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 32px;
  border-radius: 24px;
  padding: 36px 40px;
  color: #fff;
  background:
    radial-gradient(1200px 300px at 85% -60%, rgba(255, 255, 255, 0.22), transparent 60%),
    linear-gradient(118deg, #0f7c48 0%, #17a05c 55%, #34b87a 100%);
  box-shadow: 0 18px 44px rgba(15, 124, 72, 0.28);
  overflow: hidden;
  position: relative;
}
.hero::after {
  content: 'Aa';
  position: absolute;
  right: -12px;
  bottom: -58px;
  font-size: 190px;
  font-weight: 800;
  line-height: 1;
  color: rgba(255, 255, 255, 0.10);
  font-family: var(--font-display);
  pointer-events: none;
  letter-spacing: -0.06em;
}
.hero-main {
  position: relative;
  z-index: 1;
  max-width: 640px;
}
.hero-greeting {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.86);
}
.hero-title {
  margin: 0 0 10px;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.35;
  letter-spacing: 0.01em;
}
.hero-sub {
  margin: 0 0 26px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.82);
}
.hero-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.cta-primary {
  --el-button-bg-color: #ffffff;
  --el-button-border-color: #ffffff;
  --el-button-text-color: #0f7c48;
  --el-button-hover-bg-color: #ecf8f2;
  --el-button-hover-border-color: #ecf8f2;
  --el-button-hover-text-color: #0a5e35;
  font-weight: 700;
  box-shadow: 0 8px 18px rgba(6, 66, 37, 0.25);
}
.cta-icon {
  margin-right: 2px;
}
.cta-secondary {
  --el-button-bg-color: rgba(255, 255, 255, 0.16);
  --el-button-border-color: rgba(255, 255, 255, 0.55);
  --el-button-text-color: #ffffff;
  --el-button-hover-bg-color: rgba(255, 255, 255, 0.26);
  --el-button-hover-border-color: #ffffff;
  --el-button-hover-text-color: #ffffff;
  font-weight: 600;
  backdrop-filter: blur(4px);
}

/* 右侧打卡环 */
.hero-checkin {
  position: relative;
  z-index: 1;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.checkin-ring {
  width: 148px;
  height: 148px;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  background: rgba(255, 255, 255, 0.12);
  border: 2px dashed rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(4px);
}
.checkin-ring.done {
  border-style: solid;
  background: rgba(255, 255, 255, 0.94);
  color: #0f7c48;
  box-shadow: 0 10px 30px rgba(6, 66, 37, 0.22);
}
.ring-num {
  font-size: 52px;
  font-weight: 800;
  line-height: 1;
  color: #fff;
}
.ring-unit {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
}
.checkin-label {
  font-size: 16px;
  font-weight: 700;
  color: #0f7c48;
  margin-top: 2px;
}
.checkin-hint {
  margin: 0;
  font-size: 12.5px;
  color: rgba(255, 255, 255, 0.82);
  max-width: 180px;
  text-align: center;
}

/* ================= 统计条 ================= */
.stat-strip {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin: 20px 0;
}
.mini-stat {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}
.mini-stat:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card-hover);
}
.mini-stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.mini-stat-value {
  font-size: 26px;
  font-weight: 800;
  line-height: 1.1;
  color: var(--color-ink);
}
.mini-stat-value .unit {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-ink-faint);
  margin-left: 3px;
}
.mini-stat-label {
  font-size: 13px;
  color: var(--color-ink-soft);
  margin-top: 2px;
}
.mini-stat.warning {
  border-color: #f6cdc7;
}

/* ================= 网格主体 ================= */
.home-grid {
  display: grid;
  grid-template-columns: 1.5fr 1fr;
  gap: 20px;
  margin-bottom: 26px;
  align-items: stretch;
}
.panel {
  padding: 22px 24px;
}
.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}
.panel-head h2 {
  margin: 0;
  font-size: 17px;
  font-weight: 700;
  color: var(--color-ink);
}
.panel-head p {
  margin: 3px 0 0;
  font-size: 12.5px;
  color: var(--color-ink-faint);
}
.more-link {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 13px;
  color: var(--color-primary-deep);
  text-decoration: none;
  flex-shrink: 0;
}
.more-link:hover {
  color: var(--color-primary);
}

/* -------- 热力日历 -------- */
.heat-legend {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--color-ink-faint);
}
.heat-dot {
  width: 12px;
  height: 12px;
  border-radius: 4px;
  display: inline-block;
}
.heat-board {
  display: flex;
  gap: 14px;
}
.heat-weekdays {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 0 0 0 2px;
  color: var(--color-ink-faint);
  font-size: 12px;
}
.heat-weekdays span {
  height: 24px;
  line-height: 24px;
}
.heat-body {
  flex: 1;
}
.heat-row {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}
.heat-row:last-child {
  margin-bottom: 0;
}
.heat-cell {
  flex: 1;
  aspect-ratio: 1;
  max-width: 30px;
  border-radius: 7px;
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  position: relative;
  cursor: default;
  transition: transform 0.12s ease;
}
.heat-cell:hover {
  transform: scale(1.16);
  z-index: 2;
}
.heat-cell.lv1 { background: #c9ecd9; border-color: #c9ecd9; }
.heat-cell.lv2 { background: #8fd5b1; border-color: #8fd5b1; }
.heat-cell.lv3 { background: #3eb579; border-color: #3eb579; }
.heat-cell.lv4 { background: #0e7c46; border-color: #0e7c46; }
.heat-cell.empty { background: #fbfcfb; }
.heat-cell.today {
  box-shadow: 0 0 0 2px #fff, 0 0 0 4px var(--color-amber);
}
.today-dot {
  position: absolute;
  right: 3px;
  top: 3px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--color-amber);
}

/* -------- 公告 -------- */
.announce-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.announce-item {
  display: flex;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  transition: box-shadow 0.15s ease;
}
.announce-item:hover {
  box-shadow: var(--shadow-card);
}
.announce-badge {
  flex-shrink: 0;
  align-self: flex-start;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-deep));
  padding: 2px 9px;
  border-radius: 999px;
  margin-top: 3px;
}
.announce-body {
  min-width: 0;
}
.announce-title {
  margin: 0 0 4px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-ink);
}
.announce-excerpt {
  margin: 0 0 6px;
  font-size: 12.5px;
  color: var(--color-ink-soft);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.announce-time {
  font-size: 11.5px;
  color: var(--color-ink-faint);
}
.announce-empty {
  padding: 30px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: var(--color-ink-faint);
}
.announce-empty p {
  margin: 0;
  font-size: 13px;
}

/* ================= 快捷入口 ================= */
.quick-section {
  margin-bottom: 8px;
}
.quick-section .panel-head {
  margin-bottom: 14px;
}
.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}
.quick-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}
.quick-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card-hover);
  border-color: var(--color-primary-tint);
}
.quick-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.quick-info {
  min-width: 0;
}
.quick-info h4 {
  margin: 0 0 2px;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-ink);
}
.quick-info p {
  margin: 0;
  font-size: 12.5px;
  color: var(--color-ink-faint);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ================= 响应式 ================= */
@media (max-width: 1000px) {
  .stat-strip { grid-template-columns: repeat(2, 1fr); }
  .home-grid { grid-template-columns: 1fr; }
  .quick-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 720px) {
  .hero { flex-direction: column; align-items: flex-start; padding: 28px 24px; }
  .hero-checkin { align-self: center; }
  .quick-grid { grid-template-columns: 1fr; }
}
</style>
