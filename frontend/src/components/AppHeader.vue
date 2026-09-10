<template>
  <header class="app-header">
    <div class="header-inner">
      <!-- 品牌 -->
      <router-link to="/" class="brand" title="回到首页">
        <span class="brand-logo">词</span>
        <span class="brand-name">自在背单词</span>
      </router-link>

      <!-- 数据速览条（main 模式：用学习数据替代功能栏目） -->
      <router-link
        v-if="mode === 'main'"
        to="/statistics"
        class="data-strip"
        title="查看完整学习统计"
      >
        <div class="ds-item">
          <span class="ds-value">{{ todayTotal }}</span>
          <span class="ds-label">今日词量</span>
        </div>

        <i class="ds-divider"></i>

        <div class="ds-item" :class="{ alert: dueReviews > 0 }">
          <span class="ds-value">{{ dueReviews }}</span>
          <span class="ds-label">待复习</span>
        </div>

        <i class="ds-divider"></i>

        <div class="ds-item">
          <span class="ds-value">{{ streakDays }}<em>天</em></span>
          <span class="ds-label">连续打卡</span>
        </div>

        <i class="ds-divider"></i>

        <!-- 掌握率环形 -->
        <div class="ds-ring-wrap">
          <svg class="ds-ring" viewBox="0 0 36 36" aria-hidden="true">
            <circle cx="18" cy="18" r="15.5" fill="none"
                    stroke="rgba(23,160,92,.16)" stroke-width="4" />
            <circle cx="18" cy="18" r="15.5" fill="none"
                    stroke="var(--color-primary)" stroke-width="4" stroke-linecap="round"
                    :stroke-dasharray="`${masteryArc} 97.4`" transform="rotate(-90 18 18)" />
          </svg>
          <span class="ds-ring-num">{{ masteryRate }}%</span>
        </div>
        <span class="ds-label ds-ring-label">掌握率</span>

        <!-- 近 7 天迷你趋势图 -->
        <div class="ds-chart">
          <svg class="ds-spark" viewBox="0 0 84 30" preserveAspectRatio="none" aria-hidden="true">
            <rect
              v-for="(bar, i) in weekBars"
              :key="i"
              :x="i * 12"
              :y="30 - bar.h"
              width="8"
              :height="bar.h"
              rx="2.6"
              :fill="bar.isToday ? 'var(--color-primary-deep)' : 'rgba(23,160,92,.34)'"
            />
          </svg>
          <span class="ds-chart-label">近 7 天</span>
        </div>
      </router-link>

      <!-- 简单标题（simple / admin 模式） -->
      <div v-else class="header-title">
        <span v-if="back" class="back-link" @click="goBack">
          <el-icon><ArrowLeft /></el-icon> 返回
        </span>
        <h1>{{ title }}</h1>
      </div>

      <!-- 右侧 -->
      <div class="header-actions">
        <slot name="actions" />

        <router-link v-if="userStore.user?.role === 'ADMIN' && mode !== 'admin'" to="/admin" class="admin-chip">
          <el-icon><Setting /></el-icon>
          <span>管理后台</span>
        </router-link>

        <div class="user-chip">
          <span class="user-avatar">{{ avatarText }}</span>
          <span class="user-name">{{ userStore.user?.username }}</span>
        </div>

        <button class="icon-btn" title="退出登录" @click="handleLogout">
          <el-icon :size="18"><SwitchButton /></el-icon>
        </button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ArrowLeft, Setting, SwitchButton } from '@element-plus/icons-vue'
import { getLearningOverview, getUserStatistics } from '@/api/learning'

const props = defineProps({
  mode: { type: String, default: 'main' }, // main | simple | admin
  title: { type: String, default: '' },
  back: { type: Boolean, default: false }
})

const router = useRouter()
const userStore = useUserStore()

const overview = ref({ todayLearned: 0, todayReviewed: 0, streakDays: 0, calendar: [] })
const statistics = ref({ totalWords: 0, masteredWords: 0, dueReviews: 0 })

const todayTotal = computed(() => (overview.value.todayLearned || 0) + (overview.value.todayReviewed || 0))
const streakDays = computed(() => overview.value.streakDays || 0)
const dueReviews = computed(() => statistics.value.dueReviews || 0)

const masteryRate = computed(() => {
  const total = statistics.value.totalWords || 0
  if (!total) return 0
  return Math.round(((statistics.value.masteredWords || 0) / total) * 100)
})
const masteryArc = computed(() => ((masteryRate.value / 100) * 97.4).toFixed(2))

// 近 7 天学习量（新学 + 复习）迷你柱状图
const weekBars = computed(() => {
  const cal = overview.value.calendar || []
  const recent = cal.slice(-7)
  const values = recent.map(d => ({
    v: (d.learned || 0) + (d.reviewed || 0),
    isToday: d.date === recent[recent.length - 1]?.date
  }))
  const max = Math.max(1, ...values.map(x => x.v))
  return values.map(x => ({ h: x.v > 0 ? Math.max(5, (x.v / max) * 26) : 3, isToday: x.isToday }))
})

const avatarText = computed(() => {
  const name = userStore.user?.username || 'U'
  return name.charAt(0).toUpperCase()
})

const goBack = () => {
  if (window.history.length > 1) router.back()
  else router.push('/')
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

const loadHeaderData = async () => {
  if (props.mode !== 'main' || !userStore.user?.id) return
  try {
    const [ov, st] = await Promise.all([
      getLearningOverview(userStore.user.id),
      getUserStatistics(userStore.user.id)
    ])
    overview.value = ov || overview.value
    statistics.value = st || statistics.value
  } catch (e) {
    console.error('顶栏数据加载失败:', e)
  }
}

onMounted(loadHeaderData)
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.86);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.75);
  box-shadow: 0 1px 0 rgba(20, 48, 28, 0.05), 0 6px 22px rgba(20, 48, 28, 0.05);
}
.header-inner {
  max-width: 1120px;
  margin: 0 auto;
  height: 66px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 18px;
}

/* 品牌 */
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  flex-shrink: 0;
}
.brand-logo {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-deep) 100%);
  color: #fff;
  font-size: 18px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(23, 160, 92, 0.3);
}
.brand-name {
  font-size: 18px;
  font-weight: 700;
  color: var(--color-ink);
  letter-spacing: 0.01em;
  white-space: nowrap;
}

/* ---------- 数据速览条 ---------- */
.data-strip {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 14px;
  height: 46px;
  padding: 0 16px;
  border-radius: 14px;
  text-decoration: none;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(238, 250, 243, 0.9));
  border: 1px solid var(--color-border);
  transition: box-shadow 0.18s ease, border-color 0.18s ease;
  overflow: hidden;
}
.data-strip:hover {
  border-color: var(--color-primary);
  box-shadow: 0 6px 18px rgba(23, 160, 92, 0.14);
}
.ds-item {
  display: flex;
  flex-direction: column;
  justify-content: center;
  line-height: 1.15;
  flex-shrink: 0;
  min-width: 46px;
}
.ds-value {
  font-size: 17px;
  font-weight: 800;
  color: var(--color-ink);
  letter-spacing: -0.01em;
}
.ds-value em {
  font-size: 11px;
  font-style: normal;
  font-weight: 600;
  color: var(--color-ink-faint);
  margin-left: 2px;
}
.ds-item.alert .ds-value {
  color: var(--color-rust);
}
.ds-label {
  font-size: 11px;
  color: var(--color-ink-faint);
  margin-top: 2px;
  white-space: nowrap;
}
.ds-divider {
  width: 1px;
  height: 22px;
  background: var(--color-border);
  flex-shrink: 0;
}

/* 掌握率环 */
.ds-ring-wrap {
  position: relative;
  width: 34px;
  height: 34px;
  flex-shrink: 0;
}
.ds-ring {
  width: 34px;
  height: 34px;
  display: block;
}
.ds-ring-num {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 800;
  color: var(--color-primary-deep);
}
.ds-ring-label {
  margin-top: 0;
  flex-shrink: 0;
}

/* 迷你趋势图 */
.ds-chart {
  margin-left: auto;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  flex-shrink: 0;
}
.ds-spark {
  width: 84px;
  height: 30px;
  display: block;
}
.ds-chart-label {
  font-size: 10.5px;
  color: var(--color-ink-faint);
}

/* simple / admin 标题 */
.header-title {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}
.header-title h1 {
  margin: 0;
  font-size: 19px;
  font-weight: 700;
  white-space: nowrap;
}
.back-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: var(--radius-pill);
  border: 1px solid var(--color-border);
  color: var(--color-ink-soft);
  font-size: 13px;
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.15s ease;
}
.back-link:hover {
  color: var(--color-primary);
  border-color: var(--color-primary);
  background: var(--color-primary-tint);
}

/* 右侧 */
.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
.admin-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 6px 14px;
  border-radius: var(--radius-pill);
  background: linear-gradient(135deg, #0e7c46, #0a5e35);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  box-shadow: 0 4px 12px rgba(14, 124, 70, 0.3);
  transition: transform 0.15s ease;
}
.admin-chip:hover {
  transform: translateY(-1px);
}
.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 12px 4px 4px;
  border-radius: var(--radius-pill);
  border: 1px solid var(--color-border);
  background: var(--color-surface);
}
.user-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--color-primary-tint);
  color: var(--color-primary-deep);
  font-weight: 700;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.user-name {
  font-size: 13px;
  color: var(--color-ink);
  max-width: 110px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.icon-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid var(--color-border);
  background: var(--color-surface);
  color: var(--color-ink-soft);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.15s ease;
}
.icon-btn:hover {
  color: var(--color-rust);
  border-color: var(--color-rust);
}

@media (max-width: 1080px) {
  .ds-chart {
    display: none;
  }
}
@media (max-width: 920px) {
  .ds-ring-wrap,
  .ds-ring-label {
    display: none;
  }
  .user-name {
    display: none;
  }
}
@media (max-width: 760px) {
  .brand-name,
  .admin-chip span {
    display: none;
  }
  .data-strip {
    gap: 10px;
    padding: 0 12px;
  }
  .ds-item {
    min-width: 40px;
  }
}
@media (max-width: 560px) {
  .ds-divider,
  .ds-item:nth-of-type(2) {
    display: none;
  }
}
</style>
