<template>
  <div class="statistics-page">
    <AppHeader mode="main" />

    <main class="page-shell">
      <div class="page-heading">
        <h1>学习统计</h1>
        <p>坚持每天学一点，数据会帮你看见进步</p>
      </div>

      <!-- 加载骨架 -->
      <div v-if="loading" class="loading card">
        <el-icon class="is-loading" :size="34"><Loading /></el-icon>
        <p>正在整理学习数据…</p>
      </div>

      <template v-else>
        <!-- ===== KPI 卡片 ===== -->
        <section class="kpi-row">
          <div class="kpi card">
            <span class="kpi-icon" style="background: var(--color-primary-tint); color: var(--color-primary-deep)">
              <el-icon :size="20"><Document /></el-icon>
            </span>
            <div class="kpi-body">
              <div class="kpi-value">{{ statistics.totalWords }}</div>
              <div class="kpi-label">累计学习单词</div>
            </div>
          </div>
          <div class="kpi card">
            <span class="kpi-icon" style="background: var(--color-primary-tint); color: var(--color-moss)">
              <el-icon :size="20"><CircleCheckFilled /></el-icon>
            </span>
            <div class="kpi-body">
              <div class="kpi-value" style="color: var(--color-moss)">{{ statistics.masteredWords }}</div>
              <div class="kpi-label">已掌握</div>
            </div>
          </div>
          <div class="kpi card">
            <span class="kpi-icon" style="background: #fdf1e3; color: var(--color-amber)">
              <el-icon :size="20"><Loading /></el-icon>
            </span>
            <div class="kpi-body">
              <div class="kpi-value" style="color: var(--color-amber)">{{ statistics.learningWords }}</div>
              <div class="kpi-label">学习中</div>
            </div>
          </div>
          <div class="kpi card" :class="{ 'is-danger': dueCount > 0 }">
            <span class="kpi-icon" :style="dueCount > 0
              ? 'background:#fdecea; color:var(--color-rust)'
              : 'background:#eaf1fb; color:var(--color-blue)'">
              <el-icon :size="20"><AlarmClock /></el-icon>
            </span>
            <div class="kpi-body">
              <div class="kpi-value" :style="dueCount > 0 ? 'color:var(--color-rust)' : 'color:var(--color-blue)'">{{ dueCount }}</div>
              <div class="kpi-label">待复习</div>
            </div>
          </div>
        </section>

        <!-- ===== 图表行 1：掌握分布 + 14 天趋势 ===== -->
        <section class="chart-row">
          <div class="card chart-card">
            <div class="card-title">
              <h3>掌握情况</h3>
              <p>已掌握 {{ statistics.masteredWords }} 词 · 学习中 {{ statistics.learningWords }} 词</p>
            </div>
            <BaseChart :option="masteryOption" height="280px" />
          </div>
          <div class="card chart-card">
            <div class="card-title">
              <h3>近 14 天学习趋势</h3>
              <p>每天新学与复习的数量变化</p>
            </div>
            <BaseChart :option="trendOption" height="280px" />
          </div>
        </section>

        <!-- ===== 图表行 2：30 天打卡热力图 ===== -->
        <section class="card chart-card heat-card">
          <div class="card-title heat-title">
            <div>
              <h3>近 30 天打卡热力图</h3>
              <p>
                <template v-if="overview.checkedInToday">今天已打卡 ✓，连续 {{ streak }} 天</template>
                <template v-else>今天还没打卡，去学几个词吧</template>
                · 累计打卡 {{ totalCheckIns }} 天
              </p>
            </div>
            <div class="streak-chip">
              <el-icon><Odometer /></el-icon> 连续 {{ streak }} 天
            </div>
          </div>
          <BaseChart :option="calendarOption" height="190px" />
        </section>

        <!-- ===== 学习建议 ===== -->
        <section class="tips">
          <div class="tip-item" v-for="tip in tips" :key="tip.text">
            <span class="tip-icon" :style="{ background: tip.bg, color: tip.color }">
              <el-icon :size="16"><component :is="tip.icon" /></el-icon>
            </span>
            <p>{{ tip.text }}</p>
          </div>
        </section>
      </template>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import {
  Document, CircleCheckFilled, Loading, AlarmClock, Warning,
  SuccessFilled, InfoFilled, Odometer, Bell
} from '@element-plus/icons-vue'
import AppHeader from '@/components/AppHeader.vue'
import BaseChart from '@/components/BaseChart.vue'
import { getUserStatistics, getLearningOverview } from '@/api/learning'

const userStore = useUserStore()

const loading = ref(true)
const statistics = ref({ totalWords: 0, masteredWords: 0, learningWords: 0, dueReviews: 0 })
const overview = ref({ checkedInToday: false, streakDays: 0, totalCheckIns: 0, calendar: [] })

const dueCount = computed(() => statistics.value.dueReviews || 0)
const streak = computed(() => overview.value.streakDays || 0)
const totalCheckIns = computed(() => overview.value.totalCheckIns || 0)
const masteryRate = computed(() => {
  if (!statistics.value.totalWords) return 0
  return Math.round((statistics.value.masteredWords / statistics.value.totalWords) * 100)
})

const axisText = '#7a887f'
const axisLine = '#e3e9e4'

/* ---------- ECharts option：掌握环形图 ---------- */
const masteryOption = computed(() => ({
  color: ['#17a05c', '#e6a23c'],
  tooltip: { trigger: 'item', formatter: '{b}: {c} 词 ({d}%)' },
  legend: {
    bottom: 0,
    icon: 'circle',
    itemWidth: 9,
    itemHeight: 9,
    textStyle: { color: '#5f6f66', fontSize: 12 }
  },
  series: [{
    type: 'pie',
    radius: ['62%', '82%'],
    center: ['50%', '44%'],
    avoidLabelOverlap: true,
    itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
    label: {
      show: true,
      position: 'center',
      formatter: () => `{rate|${masteryRate.value}%}\n{cap|掌握率}`,
      rich: {
        rate: { fontSize: 30, fontWeight: 800, color: '#1c2b24', lineHeight: 38 },
        cap: { fontSize: 12, color: '#93a098', lineHeight: 18 }
      }
    },
    emphasis: { label: { show: true } },
    data: [
      { name: '已掌握', value: statistics.value.masteredWords },
      { name: '学习中', value: statistics.value.learningWords }
    ]
  }]
}))

/* ---------- ECharts option：14 天学习趋势 ---------- */
const trendOption = computed(() => {
  const days = (overview.value.calendar || []).slice(-14)
  const labels = days.map(d => d.date.slice(5))
  const learned = days.map(d => d.learned || 0)
  const reviewed = days.map(d => d.reviewed || 0)
  return {
    color: ['#17a05c', '#4a90e2'],
    tooltip: { trigger: 'axis' },
    legend: {
      top: 0,
      right: 0,
      icon: 'circle',
      itemWidth: 9,
      itemHeight: 9,
      textStyle: { color: '#5f6f66', fontSize: 12 },
      data: ['新学', '复习']
    },
    grid: { left: 8, right: 8, top: 34, bottom: 4, containLabel: true },
    xAxis: {
      type: 'category',
      data: labels,
      axisLine: { lineStyle: { color: axisLine } },
      axisTick: { show: false },
      axisLabel: { color: axisText, fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#eef2ee' } },
      axisLabel: { color: axisText, fontSize: 11 }
    },
    series: [
      {
        name: '新学',
        type: 'bar',
        data: learned,
        barMaxWidth: 14,
        itemStyle: { borderRadius: [5, 5, 0, 0] }
      },
      {
        name: '复习',
        type: 'bar',
        data: reviewed,
        barMaxWidth: 14,
        itemStyle: { borderRadius: [5, 5, 0, 0] }
      }
    ]
  }
})

/* ---------- ECharts option：30 天日历热力图 ---------- */
const calendarOption = computed(() => {
  const entries = overview.value.calendar || []
  const maxV = Math.max(1, ...entries.map(e => (e.learned || 0) + (e.reviewed || 0)))
  return {
    tooltip: {
      formatter: (p) => {
        const d = p.value[0]
        const day = entries.find(e => e.date === d)
        if (!day) return d
        const amount = (day.learned || 0) + (day.reviewed || 0)
        return `<b>${d}</b><br/>${day.checkedIn ? `打卡 ✓ · 新学 ${day.learned} · 复习 ${day.reviewed}` : '未打卡'}${amount ? '' : ''}`
      }
    },
    visualMap: {
      min: 0,
      max: maxV,
      show: false,
      inRange: {
        color: ['#f0f5f1', '#bfe8d2', '#6ecba0', '#1f9e63', '#0c6a3c']
      }
    },
    calendar: {
      top: 16,
      left: 30,
      right: 10,
      cellSize: [20, 20],
      range: [entries.length ? entries[0].date : '2026-01-01', entries.length ? entries[entries.length - 1].date : '2026-01-01'],
      itemStyle: {
        borderWidth: 3,
        borderColor: '#ffffff',
        borderRadius: 5
      },
      splitLine: { show: false },
      dayLabel: { nameMap: ['日', '一', '二', '三', '四', '五', '六'], color: axisText, fontSize: 11 },
      monthLabel: { color: axisText, fontSize: 11, position: 'start' },
      yearLabel: { show: false }
    },
    series: [{
      type: 'heatmap',
      coordinateSystem: 'calendar',
      data: entries.map(e => {
        const amount = (e.learned || 0) + (e.reviewed || 0)
        return [e.date, e.checkedIn ? Math.max(amount, 0.0001) : 0]
      })
    }]
  }
})

/* ---------- 学习建议 ---------- */
const tips = computed(() => {
  const list = []
  if (dueCount.value > 0) {
    list.push({ icon: Warning, text: `有 ${dueCount.value} 个单词到了复习时间，趁热复习记忆更牢。`, bg: '#fdecea', color: 'var(--color-rust)' })
  } else if (statistics.value.totalWords > 0) {
    list.push({ icon: SuccessFilled, text: '当前没有待复习的单词，可以放心学习新词。', bg: 'var(--color-primary-tint)', color: 'var(--color-moss)' })
  } else {
    list.push({ icon: Bell, text: '还没有学习记录，从「开始学习」背下第一批单词吧。', bg: '#eaf1fb', color: 'var(--color-blue)' })
  }
  if (masteryRate.value === 0) {
    list.push({ icon: InfoFilled, text: '掌握率会在你坚持复习后逐步提升，不用心急。', bg: '#fdf1e3', color: 'var(--color-amber)' })
  } else if (masteryRate.value < 40) {
    list.push({ icon: InfoFilled, text: '掌握率目前偏低，建议新学与复习保持 1:2 的节奏。', bg: '#fdf1e3', color: 'var(--color-amber)' })
  } else if (masteryRate.value < 75) {
    list.push({ icon: InfoFilled, text: '学习进展良好，继续保持每天打卡。', bg: '#eaf1fb', color: 'var(--color-blue)' })
  } else {
    list.push({ icon: SuccessFilled, text: '掌握率很高，表现优秀！可以挑战更难的生词。', bg: 'var(--color-primary-tint)', color: 'var(--color-moss)' })
  }
  return list
})

const load = async () => {
  try {
    loading.value = true
    const [st, ov] = await Promise.all([
      getUserStatistics(userStore.user.id),
      getLearningOverview(userStore.user.id)
    ])
    statistics.value = st
    overview.value = ov
  } catch (e) {
    console.error('统计页加载失败:', e)
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-height: 320px;
  color: var(--color-ink-soft);
}
.loading p {
  margin: 0;
  font-size: 13px;
}

/* KPI */
.kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}
.kpi {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
}
.kpi-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.kpi-value {
  font-size: 26px;
  font-weight: 800;
  line-height: 1.15;
  color: var(--color-ink);
}
.kpi-label {
  font-size: 13px;
  color: var(--color-ink-soft);
  margin-top: 2px;
}

/* 图表 */
.chart-row {
  display: grid;
  grid-template-columns: 1fr 1.4fr;
  gap: 20px;
  margin-bottom: 20px;
}
.chart-card {
  padding: 20px 22px;
}
.card-title {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 4px;
}
.card-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--color-ink);
}
.card-title p {
  margin: 3px 0 0;
  font-size: 12.5px;
  color: var(--color-ink-faint);
}

/* 热力卡 */
.heat-card {
  margin-bottom: 20px;
}
.heat-title {
  align-items: center;
  margin-bottom: 8px;
}
.streak-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 999px;
  background: var(--color-primary-tint);
  color: var(--color-primary-deep);
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

/* 建议 */
.tips {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 14px;
}
.tip-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 16px;
  background: var(--color-surface);
  border: 1px solid var(--color-border);
}
.tip-icon {
  width: 34px;
  height: 34px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.tip-item p {
  margin: 0;
  font-size: 13px;
  color: var(--color-ink);
  line-height: 1.5;
}

@media (max-width: 960px) {
  .kpi-row { grid-template-columns: repeat(2, 1fr); }
  .chart-row { grid-template-columns: 1fr; }
  .tips { grid-template-columns: 1fr; }
}
</style>
