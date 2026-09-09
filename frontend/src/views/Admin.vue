<template>
  <div class="admin-layout">
    <!-- ============ 左侧导航 ============ -->
    <aside class="admin-side">
      <div class="side-brand">
        <div class="side-logo">词</div>
        <div class="side-brand-text">
          <strong>自在背单词</strong>
          <span>管理后台</span>
        </div>
      </div>

      <nav class="side-nav">
        <button
          class="side-item"
          :class="{ active: active === 'dashboard' }"
          @click="active = 'dashboard'"
        >
          <el-icon :size="17"><DataAnalysis /></el-icon>
          <span>数据看板</span>
        </button>
        <button
          class="side-item"
          :class="{ active: active === 'users' }"
          @click="openUsers"
        >
          <el-icon :size="17"><User /></el-icon>
          <span>用户管理</span>
        </button>
        <button
          class="side-item"
          :class="{ active: active === 'announcements' }"
          @click="openAnnouncements"
        >
          <el-icon :size="17"><Bell /></el-icon>
          <span>系统公告</span>
        </button>
      </nav>

      <div class="side-foot">
        <span class="foot-chip">管理员 {{ adminCount }} 人</span>
        <span class="foot-chip" :class="{ danger: disabledCount > 0 }">封禁 {{ disabledCount }} 人</span>
      </div>
    </aside>

    <!-- ============ 右侧主体 ============ -->
    <div class="admin-body">
      <header class="admin-top">
        <h2>{{ pageTitle }}</h2>
        <div class="top-actions">
          <router-link to="/" class="back-home">
            <el-icon><HomeFilled /></el-icon>
            <span>返回学习端</span>
          </router-link>
          <div class="top-user">
            <span class="top-avatar">{{ avatarText }}</span>
            <span class="top-name">{{ userStore.user?.username }}</span>
            <el-tag size="small" effect="dark" round>管理员</el-tag>
          </div>
          <button class="logout-btn" title="退出登录" @click="handleLogout">
            <el-icon :size="17"><SwitchButton /></el-icon>
          </button>
        </div>
      </header>

      <main class="admin-content">
        <!-- ==================== 数据看板 ==================== -->
        <section v-if="active === 'dashboard'" class="dash-view" v-loading="dashLoading">
          <template v-if="dash">
            <!-- KPI 卡片 -->
            <div class="kpi-grid">
              <div class="kpi-card card">
                <div class="kpi-icon blue"><el-icon :size="19"><User /></el-icon></div>
                <div class="kpi-info">
                  <span class="kpi-label">注册用户</span>
                  <span class="kpi-value">{{ dash.totals.totalUsers }}</span>
                </div>
              </div>
              <div class="kpi-card card">
                <div class="kpi-icon green"><el-icon :size="19"><Reading /></el-icon></div>
                <div class="kpi-info">
                  <span class="kpi-label">学生用户</span>
                  <span class="kpi-value">{{ dash.totals.studentCount }}</span>
                </div>
              </div>
              <div class="kpi-card card">
                <div class="kpi-icon amber"><el-icon :size="19"><Collection /></el-icon></div>
                <div class="kpi-info">
                  <span class="kpi-label">词库单词</span>
                  <span class="kpi-value">{{ dash.totals.totalWords }}</span>
                </div>
              </div>
              <div class="kpi-card card">
                <div class="kpi-icon moss"><el-icon :size="19"><CircleCheckFilled /></el-icon></div>
                <div class="kpi-info">
                  <span class="kpi-label">今日打卡</span>
                  <span class="kpi-value">{{ dash.today.checkIns }}</span>
                  <span class="kpi-sub">活跃用户 {{ dash.today.activeUsers }}</span>
                </div>
              </div>
              <div class="kpi-card card">
                <div class="kpi-icon green"><el-icon :size="19"><TrendCharts /></el-icon></div>
                <div class="kpi-info">
                  <span class="kpi-label">今日新学</span>
                  <span class="kpi-value">{{ dash.today.learned }}</span>
                  <span class="kpi-sub">单词次</span>
                </div>
              </div>
              <div class="kpi-card card">
                <div class="kpi-icon blue"><el-icon :size="19"><Refresh /></el-icon></div>
                <div class="kpi-info">
                  <span class="kpi-label">今日复习</span>
                  <span class="kpi-value">{{ dash.today.reviewed }}</span>
                  <span class="kpi-sub">单词次</span>
                </div>
              </div>
            </div>

            <!-- 图表区 -->
            <div class="chart-grid">
              <div class="chart-card card span-2">
                <div class="chart-head">
                  <h3>近 14 天学习活跃度</h3>
                  <span class="chart-sub">每日新学 / 复习词次 与 活跃用户数</span>
                </div>
                <BaseChart :option="activityOpt" height="300px" />
              </div>

              <div class="chart-card card">
                <div class="chart-head">
                  <h3>词库难度分布</h3>
                  <span class="chart-sub">共 {{ dash.totals.totalWords }} 个单词</span>
                </div>
                <BaseChart :option="difficultyOpt" height="260px" />
              </div>

              <div class="chart-card card span-2">
                <div class="chart-head">
                  <h3>近 14 天新增注册</h3>
                  <span class="chart-sub">每日新注册用户数</span>
                </div>
                <BaseChart :option="growthOpt" height="240px" />
              </div>

              <div class="chart-card card">
                <div class="chart-head">
                  <h3>学习榜 TOP 8</h3>
                  <span class="chart-sub">按累计学习单词数排序</span>
                </div>
                <div v-if="dash.topUsers.length" class="rank-list">
                  <div v-for="(u, i) in dash.topUsers" :key="u.id" class="rank-row">
                    <span class="rank-no" :class="'r' + (i + 1)">{{ i + 1 }}</span>
                    <span class="rank-avatar">{{ (u.username || 'U').charAt(0).toUpperCase() }}</span>
                    <div class="rank-main">
                      <span class="rank-name">{{ u.username }}</span>
                      <div class="rank-bar">
                        <div
                          class="rank-fill"
                          :style="{ width: rankPct(u) + '%' }"
                        ></div>
                      </div>
                    </div>
                    <div class="rank-nums">
                      <strong>{{ u.mastered }}</strong><em>/ {{ u.totalLearned }} 掌握</em>
                    </div>
                  </div>
                </div>
                <el-empty v-else description="暂无学习数据" :image-size="70" />
              </div>
            </div>
          </template>
        </section>

        <!-- ==================== 用户管理 ==================== -->
        <section v-else-if="active === 'users'" class="panel-view card">
          <div class="panel-head">
            <div>
              <h3>学生用户管理</h3>
              <p class="panel-sub">共 {{ users.length }} 位用户，点「详情」查看任意学生的学习情况</p>
            </div>
          </div>
          <el-table :data="users" v-loading="usersLoading" style="width: 100%">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column label="用户名" min-width="140">
              <template #default="{ row }">
                <span class="user-cell">
                  <span class="cell-avatar">{{ (row.username || 'U').charAt(0).toUpperCase() }}</span>
                  {{ row.username }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="邮箱" min-width="170">
              <template #default="{ row }">{{ row.email || '—' }}</template>
            </el-table-column>
            <el-table-column label="角色" width="130">
              <template #default="{ row }">
                <el-select
                  v-model="row.role"
                  size="small"
                  style="width: 100px"
                  @change="(val) => onRoleChange(row, val)"
                >
                  <el-option label="学生" value="STUDENT" />
                  <el-option label="管理员" value="ADMIN" />
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <span class="status-dot" :class="row.enabled ? 'ok' : 'off'">
                  {{ row.enabled ? '正常' : '封禁' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="注册时间" width="160">
              <template #default="{ row }">{{ fmtDateTime(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="primary" text @click="openUserDetail(row)">详情</el-button>
                <el-button
                  size="small"
                  :type="row.enabled ? 'warning' : 'success'"
                  text
                  @click="onToggleStatus(row)"
                >{{ row.enabled ? '封禁' : '解封' }}</el-button>
                <el-button size="small" type="danger" text @click="onDeleteUser(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </section>

        <!-- ==================== 系统公告 ==================== -->
        <section v-else class="panel-view card">
          <div class="panel-head">
            <div>
              <h3>系统公告管理</h3>
              <p class="panel-sub">发布的公告会展示在用户端首页顶部横幅</p>
            </div>
            <el-button type="primary" round @click="openAnnouncementDialog(null)">
              <el-icon><Plus /></el-icon> 新建公告
            </el-button>
          </div>

          <el-table :data="announcements" v-loading="annLoading" style="width: 100%">
            <el-table-column prop="id" label="ID" width="70" />
            <el-table-column prop="title" label="标题" min-width="160">
              <template #default="{ row }">
                <span class="ann-title">{{ row.title }}</span>
              </template>
            </el-table-column>
            <el-table-column label="内容" min-width="260" show-overflow-tooltip>
              <template #default="{ row }">{{ row.content || '—' }}</template>
            </el-table-column>
            <el-table-column label="发布状态" width="110">
              <template #default="{ row }">
                <el-switch
                  :model-value="!!row.enabled"
                  inline-prompt
                  active-text="已发布"
                  inactive-text="已下线"
                  @change="(val) => onToggleAnnouncement(row, val)"
                />
              </template>
            </el-table-column>
            <el-table-column label="更新时间" width="160">
              <template #default="{ row }">{{ fmtDateTime(row.updatedAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button size="small" text @click="openAnnouncementDialog(row)">编辑</el-button>
                <el-button size="small" type="danger" text @click="onDeleteAnnouncement(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </section>
      </main>
    </div>

    <!-- ============ 公告编辑弹窗 ============ -->
    <el-dialog
      v-model="annDialogVisible"
      :title="annForm.id ? '编辑公告' : '新建公告'"
      width="560px"
      align-center
    >
      <el-form :model="annForm" label-position="top">
        <el-form-item label="公告标题" required>
          <el-input v-model="annForm.title" maxlength="60" show-word-limit placeholder="例如：四六级词汇书已更新" />
        </el-form-item>
        <el-form-item label="公告内容">
          <el-input
            v-model="annForm.content"
            type="textarea"
            :rows="6"
            maxlength="500"
            show-word-limit
            placeholder="写点想告诉同学们的内容…"
          />
        </el-form-item>
        <el-form-item label="发布状态">
          <el-switch v-model="annForm.enabled" inline-prompt active-text="立即发布" inactive-text="暂不发布" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button round @click="annDialogVisible = false">取消</el-button>
        <el-button type="primary" round :loading="annSaving" @click="saveAnnouncement">
          {{ annForm.id ? '保存修改' : '创建公告' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ============ 用户学习详情抽屉 ============ -->
    <el-drawer
      v-model="detailVisible"
      :title="detail ? detail.user.username + ' 的学习详情' : '用户学习详情'"
      size="640px"
      :with-header="true"
      class="detail-drawer"
    >
      <div v-if="detail" v-loading="detailLoading" class="drawer-body">
        <!-- 基本信息 -->
        <div class="user-hero">
          <div class="hero-avatar">{{ (detail.user.username || 'U').charAt(0).toUpperCase() }}</div>
          <div class="hero-main">
            <div class="hero-name">
              {{ detail.user.username }}
              <el-tag
                size="small"
                effect="light"
                round
                :type="detail.user.role === 'ADMIN' ? 'warning' : 'success'"
              >{{ detail.user.role === 'ADMIN' ? '管理员' : '学生' }}</el-tag>
              <el-tag
                v-if="!detail.user.enabled"
                size="small"
                effect="dark"
                round
                type="danger"
              >已封禁</el-tag>
            </div>
            <div class="hero-meta">
              <span v-if="detail.user.email">{{ detail.user.email }}</span>
              <span v-else>未填写邮箱</span>
              <span class="dot">·</span>
              <span>注册于 {{ fmtDate(detail.user.createdAt) }}</span>
            </div>
          </div>
        </div>

        <!-- 概览指标 -->
        <div class="sum-grid">
          <div class="sum-item">
            <span class="sum-value">{{ detail.summary.totalRecords }}</span>
            <span class="sum-label">累计学习</span>
          </div>
          <div class="sum-item">
            <span class="sum-value moss">{{ detail.summary.mastered }}</span>
            <span class="sum-label">已掌握</span>
          </div>
          <div class="sum-item">
            <span class="sum-value amber">{{ detail.summary.learning }}</span>
            <span class="sum-label">学习中</span>
          </div>
          <div class="sum-item">
            <span class="sum-value blue">{{ detail.summary.dueReviews }}</span>
            <span class="sum-label">待复习</span>
          </div>
          <div class="sum-item">
            <span class="sum-value">{{ detail.summary.favoriteCount }}</span>
            <span class="sum-label">收藏</span>
          </div>
          <div class="sum-item">
            <span class="sum-value rust">{{ detail.summary.wrongCount }}</span>
            <span class="sum-label">错词</span>
          </div>
        </div>

        <!-- 掌握率进度 -->
        <div class="mastery-bar">
          <div class="mastery-head">
            <span>单词掌握率</span>
            <strong>{{ masteryPct }}%</strong>
          </div>
          <el-progress
            :percentage="masteryPct"
            :show-text="false"
            :stroke-width="10"
            :color="masteryColor"
          />
        </div>

        <!-- 明细切换 -->
        <el-tabs v-model="detailTab" class="detail-tabs">
          <el-tab-pane :label="`学习记录 (${detail.records.length})`" name="records">
            <div v-if="detail.records.length" class="mini-list">
              <div v-for="r in detail.records" :key="r.id" class="mini-row">
                <div class="mini-word">
                  <strong>{{ r.word ? r.word.word : '—' }}</strong>
                  <span class="mini-phonetic" v-if="r.word?.phonetic">{{ r.word.phonetic }}</span>
                  <span
                    class="mini-status"
                    :class="statusClass(r.status)"
                  >{{ statusLabel(r.status) }}</span>
                </div>
                <div class="mini-sub">
                  已复习 {{ r.reviewCount }} 轮 · 对 {{ r.correctCount }} 错 {{ r.wrongCount }}
                  <template v-if="r.nextReviewAt">
                    · 下次复习 {{ fmtDateTime(r.nextReviewAt) }}
                  </template>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无学习记录" :image-size="70" />
          </el-tab-pane>

          <el-tab-pane :label="`错词 (${detail.wrongWords.length})`" name="wrongs">
            <div v-if="detail.wrongWords.length" class="mini-list">
              <div v-for="w in detail.wrongWords" :key="w.id" class="mini-row">
                <div class="mini-word">
                  <strong>{{ w.word ? w.word.word : '—' }}</strong>
                  <span class="mini-phonetic" v-if="w.word?.phonetic">{{ w.word.phonetic }}</span>
                  <span class="mini-status rust">错 {{ w.wrongCount }} 次</span>
                </div>
                <div class="mini-sub" v-if="w.word?.definition">{{ w.word.definition }}</div>
              </div>
            </div>
            <el-empty v-else description="暂无错词" :image-size="70" />
          </el-tab-pane>

          <el-tab-pane :label="`收藏 (${detail.favorites.length})`" name="favorites">
            <div v-if="detail.favorites.length" class="mini-list">
              <div v-for="f in detail.favorites" :key="f.id" class="mini-row">
                <div class="mini-word">
                  <strong>{{ f.word ? f.word.word : '—' }}</strong>
                  <span class="mini-phonetic" v-if="f.word?.phonetic">{{ f.word.phonetic }}</span>
                </div>
                <div class="mini-sub">
                  <span v-if="f.word?.definition">{{ f.word.definition }}</span>
                  <span class="fav-time">{{ fmtDate(f.createdAt) }} 收藏</span>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无收藏" :image-size="70" />
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import BaseChart from '@/components/BaseChart.vue'
import {
  getAdminDashboard,
  getAllUsers,
  updateUserRole,
  updateUserStatus,
  deleteUser,
  getAdminUserDetail,
  getAdminAnnouncements,
  createAdminAnnouncement,
  updateAdminAnnouncement,
  deleteAdminAnnouncement
} from '@/api/admin'
import {
  DataAnalysis, User, Bell, Reading, Collection, Refresh, TrendCharts,
  CircleCheckFilled, HomeFilled, SwitchButton, Plus
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const active = ref('dashboard')

const pageTitle = computed(() => ({
  dashboard: '数据看板',
  users: '用户管理',
  announcements: '系统公告'
}[active.value]))

const avatarText = computed(() => {
  const name = userStore.user?.username || 'U'
  return name.charAt(0).toUpperCase()
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

const fmtDateTime = (s) => (s ? String(s).replace('T', ' ').slice(0, 16) : '—')
const fmtDate = (s) => (s ? String(s).slice(0, 10) : '—')

// ==================== 数据看板 ====================
const dash = ref(null)
const dashLoading = ref(false)

const loadDashboard = async () => {
  dashLoading.value = true
  try {
    dash.value = await getAdminDashboard()
  } catch (e) {
    console.error(e)
    ElMessage.error('看板数据加载失败')
  } finally {
    dashLoading.value = false
  }
}

const adminCount = computed(() => dash.value?.totals?.adminCount ?? 0)
const disabledCount = computed(() => dash.value?.totals?.disabledCount ?? 0)

const PALETTE = { green: '#17a05c', blue: '#4a90e2', amber: '#e6a23c', rust: '#e05d4a' }

const activityOpt = computed(() => {
  const act = dash.value?.activity14 || []
  return {
    tooltip: { trigger: 'axis' },
    legend: {
      top: 0,
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      textStyle: { color: '#5f6f66', fontSize: 12 }
    },
    grid: { left: 8, right: 8, top: 36, bottom: 0, containLabel: true },
    xAxis: {
      type: 'category',
      data: act.map((d) => d.date),
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#e3e9e4' } },
      axisLabel: { color: '#93a098', fontSize: 11, interval: 1 }
    },
    yAxis: [
      {
        type: 'value',
        name: '词次',
        nameTextStyle: { color: '#93a098' },
        splitLine: { lineStyle: { color: '#eef2ee' } },
        axisLabel: { color: '#93a098', fontSize: 11 }
      },
      {
        type: 'value',
        name: '活跃',
        nameTextStyle: { color: '#93a098' },
        splitLine: { show: false },
        axisLabel: { color: '#93a098', fontSize: 11 }
      }
    ],
    series: [
      {
        name: '新学',
        type: 'bar',
        data: act.map((d) => d.learned),
        barWidth: 7,
        itemStyle: { color: PALETTE.green, borderRadius: [3, 3, 0, 0] }
      },
      {
        name: '复习',
        type: 'bar',
        data: act.map((d) => d.reviewed),
        barWidth: 7,
        itemStyle: { color: PALETTE.blue, borderRadius: [3, 3, 0, 0] }
      },
      {
        name: '活跃用户',
        type: 'line',
        yAxisIndex: 1,
        data: act.map((d) => d.activeUsers),
        smooth: true,
        symbol: 'circle',
        symbolSize: 5,
        lineStyle: { color: PALETTE.amber, width: 2.5 },
        itemStyle: { color: PALETTE.amber }
      }
    ]
  }
})

const growthOpt = computed(() => {
  const g = dash.value?.userGrowth14 || []
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 8, right: 12, top: 24, bottom: 0, containLabel: true },
    xAxis: {
      type: 'category',
      data: g.map((d) => d.date),
      axisTick: { show: false },
      axisLine: { lineStyle: { color: '#e3e9e4' } },
      axisLabel: { color: '#93a098', fontSize: 11, interval: 1 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: '#eef2ee' } },
      axisLabel: { color: '#93a098', fontSize: 11 }
    },
    series: [
      {
        name: '新增用户',
        type: 'bar',
        data: g.map((d) => d.count),
        barWidth: 9,
        itemStyle: { color: PALETTE.green, borderRadius: [4, 4, 0, 0] }
      }
    ]
  }
})

const difficultyOpt = computed(() => {
  const dist = dash.value?.difficultyDist || []
  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}：{c} 词（{d}%）'
    },
    legend: {
      bottom: 0,
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      textStyle: { color: '#5f6f66', fontSize: 12 }
    },
    color: [PALETTE.green, PALETTE.amber, PALETTE.rust],
    series: [
      {
        name: '难度分布',
        type: 'pie',
        radius: ['50%', '74%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: '#fff', borderWidth: 4, borderRadius: 6 },
        label: { show: false },
        data: dist
      }
    ]
  }
})

const rankPct = (u) => {
  if (!u.totalLearned) return 0
  return Math.min(100, Math.round((u.mastered / u.totalLearned) * 100))
}

// ==================== 用户管理 ====================
const users = ref([])
const usersLoading = ref(false)
const usersLoaded = ref(false)

const openUsers = async () => {
  active.value = 'users'
  if (usersLoaded.value) return
  await loadUsers()
}

const loadUsers = async () => {
  usersLoading.value = true
  try {
    users.value = await getAllUsers()
    usersLoaded.value = true
  } catch (e) {
    console.error(e)
    ElMessage.error('用户列表加载失败')
  } finally {
    usersLoading.value = false
  }
}

const onRoleChange = async (row, newRole) => {
  try {
    await updateUserRole(row.id, newRole)
    ElMessage.success('角色已更新')
  } catch (e) {
    ElMessage.error('更新失败')
    loadUsers()
  }
}

const onToggleStatus = async (row) => {
  const nextEnabled = !row.enabled
  try {
    await ElMessageBox.confirm(
      nextEnabled
        ? `确认解封用户「${row.username}」？`
        : `确认封禁用户「${row.username}」？封禁后该用户将无法登录。`,
      '请确认',
      { type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await updateUserStatus(row.id, nextEnabled)
    row.enabled = nextEnabled
    ElMessage.success(nextEnabled ? '已解封' : '已封禁')
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  }
}

const onDeleteUser = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认删除用户「${row.username}」？其学习记录、收藏等数据会一并删除，此操作不可恢复。`,
      '请确认',
      { type: 'warning', confirmButtonText: '删除', confirmButtonClass: 'el-button--danger' }
    )
  } catch {
    return
  }
  try {
    await deleteUser(row.id)
    ElMessage.success('已删除')
    loadUsers()
  } catch (e) {
    console.error(e)
    ElMessage.error('删除失败')
  }
}

// ---------- 用户学习详情抽屉 ----------
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)
const detailTab = ref('records')

const openUserDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  detailTab.value = 'records'
  try {
    detail.value = await getAdminUserDetail(row.id)
  } catch (e) {
    console.error(e)
    ElMessage.error('学习详情加载失败')
  } finally {
    detailLoading.value = false
  }
}

const masteryPct = computed(() => {
  const s = detail.value?.summary
  if (!s || !s.totalRecords) return 0
  return Math.min(100, Math.round((s.mastered / s.totalRecords) * 100))
})

const masteryColor = computed(() => {
  if (masteryPct.value >= 60) return '#17a05c'
  if (masteryPct.value >= 30) return '#e6a23c'
  return '#e05d4a'
})

const statusLabel = (st) => ({
  NEW: '新学',
  LEARNING: '学习中',
  REVIEWING: '复习中',
  MASTERED: '已掌握'
}[st] || st)

const statusClass = (st) => ({
  NEW: 'info',
  LEARNING: 'amber',
  REVIEWING: 'blue',
  MASTERED: 'moss'
}[st] || 'info')

// ==================== 系统公告 ====================
const announcements = ref([])
const annLoading = ref(false)
const annLoaded = ref(false)

const openAnnouncements = async () => {
  active.value = 'announcements'
  if (annLoaded.value) return
  await loadAnnouncements()
}

const loadAnnouncements = async () => {
  annLoading.value = true
  try {
    announcements.value = await getAdminAnnouncements()
    annLoaded.value = true
  } catch (e) {
    console.error(e)
    ElMessage.error('公告列表加载失败')
  } finally {
    annLoading.value = false
  }
}

const annDialogVisible = ref(false)
const annSaving = ref(false)
const annForm = reactive({ id: null, title: '', content: '', enabled: true })

const openAnnouncementDialog = (row) => {
  if (row) {
    Object.assign(annForm, { id: row.id, title: row.title, content: row.content || '', enabled: !!row.enabled })
  } else {
    Object.assign(annForm, { id: null, title: '', content: '', enabled: true })
  }
  annDialogVisible.value = true
}

const saveAnnouncement = async () => {
  if (!annForm.title.trim()) {
    ElMessage.warning('请填写公告标题')
    return
  }
  annSaving.value = true
  const payload = { title: annForm.title.trim(), content: annForm.content, enabled: annForm.enabled }
  try {
    if (annForm.id) {
      await updateAdminAnnouncement(annForm.id, payload)
      ElMessage.success('公告已更新')
    } else {
      await createAdminAnnouncement(payload)
      ElMessage.success('公告已创建')
    }
    annDialogVisible.value = false
    loadAnnouncements()
  } catch (e) {
    console.error(e)
    ElMessage.error('保存失败')
  } finally {
    annSaving.value = false
  }
}

const onToggleAnnouncement = async (row, val) => {
  try {
    await updateAdminAnnouncement(row.id, { enabled: val })
    row.enabled = val
    ElMessage.success(val ? '公告已发布' : '公告已下线')
  } catch (e) {
    console.error(e)
    row.enabled = !val
    ElMessage.error('操作失败')
  }
}

const onDeleteAnnouncement = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除公告「${row.title}」？`, '请确认', {
      type: 'warning',
      confirmButtonText: '删除',
      confirmButtonClass: 'el-button--danger'
    })
  } catch {
    return
  }
  try {
    await deleteAdminAnnouncement(row.id)
    ElMessage.success('已删除')
    loadAnnouncements()
  } catch (e) {
    console.error(e)
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  loadDashboard()
})
</script>

<style scoped>
/* ============ 整体布局 ============ */
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: var(--color-bg);
}

/* 左侧栏 */
.admin-side {
  width: 216px;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--color-surface);
  border-right: 1px solid var(--color-border);
}
.side-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 18px;
}
.side-logo {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-deep) 100%);
  color: #fff;
  font-size: 19px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 10px rgba(23, 160, 92, 0.3);
}
.side-brand-text {
  display: flex;
  flex-direction: column;
  line-height: 1.25;
}
.side-brand-text strong {
  font-size: 15px;
  color: var(--color-ink);
}
.side-brand-text span {
  font-size: 12px;
  color: var(--color-ink-faint);
}

.side-nav {
  flex: 1;
  padding: 8px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.side-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 14px;
  border: none;
  background: transparent;
  border-radius: 12px;
  color: var(--color-ink-soft);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  text-align: left;
  transition: all 0.15s ease;
}
.side-item:hover {
  background: var(--color-surface-2);
  color: var(--color-primary-deep);
}
.side-item.active {
  background: var(--color-primary-tint);
  color: var(--color-primary-deep);
  font-weight: 700;
}

.side-foot {
  padding: 14px 18px;
  border-top: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.foot-chip {
  font-size: 12px;
  color: var(--color-ink-faint);
}
.foot-chip.danger {
  color: var(--color-rust);
}

/* 右侧 */
.admin-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
.admin-top {
  position: sticky;
  top: 0;
  z-index: 50;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 28px;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--color-border);
}
.admin-top h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 800;
  color: var(--color-ink);
}
.top-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.back-home {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 14px;
  border-radius: var(--radius-pill);
  border: 1px solid var(--color-border);
  color: var(--color-ink-soft);
  font-size: 13px;
  text-decoration: none;
  transition: all 0.15s ease;
}
.back-home:hover {
  color: var(--color-primary);
  border-color: var(--color-primary);
  background: var(--color-primary-tint);
}
.top-user {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px 4px 4px;
  border-radius: var(--radius-pill);
  border: 1px solid var(--color-border);
  background: var(--color-surface);
}
.top-avatar {
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
.top-name {
  font-size: 13px;
  color: var(--color-ink);
}
.logout-btn {
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
.logout-btn:hover {
  color: var(--color-rust);
  border-color: var(--color-rust);
}

.admin-content {
  flex: 1;
  padding: 24px 28px 64px;
}

/* ============ 数据看板 ============ */
.dash-view {
  min-height: 60vh;
}
.kpi-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 14px;
  margin-bottom: 20px;
}
.kpi-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
}
.kpi-icon {
  width: 42px;
  height: 42px;
  border-radius: 13px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.kpi-icon.green { background: var(--color-primary-tint); color: var(--color-primary-deep); }
.kpi-icon.blue { background: #e7f1fc; color: var(--color-blue); }
.kpi-icon.amber { background: #fdf3e2; color: var(--color-amber); }
.kpi-icon.moss { background: #e7f6ee; color: var(--color-moss); }
.kpi-icon.rust { background: #fdecea; color: var(--color-rust); }

.kpi-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.kpi-label {
  font-size: 12px;
  color: var(--color-ink-soft);
}
.kpi-value {
  font-size: 24px;
  font-weight: 800;
  line-height: 1.2;
  color: var(--color-ink);
  font-variant-numeric: tabular-nums;
}
.kpi-sub {
  font-size: 11px;
  color: var(--color-ink-faint);
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.chart-card {
  padding: 18px 20px 14px;
}
.chart-card.span-2 {
  grid-column: span 2;
}
.chart-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}
.chart-head h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--color-ink);
}
.chart-sub {
  font-size: 12px;
  color: var(--color-ink-faint);
}

/* 学习榜 */
.rank-list {
  display: flex;
  flex-direction: column;
}
.rank-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 7px 0;
}
.rank-no {
  width: 20px;
  height: 20px;
  border-radius: 6px;
  background: var(--color-surface-2);
  color: var(--color-ink-faint);
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.rank-no.r1 { background: #fdf3e2; color: var(--color-amber); }
.rank-no.r2 { background: #eef1f3; color: #8a97a1; }
.rank-no.r3 { background: #fdece5; color: #d0804f; }
.rank-avatar {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--color-primary-tint);
  color: var(--color-primary-deep);
  font-size: 13px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.rank-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}
.rank-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rank-bar {
  height: 5px;
  border-radius: 999px;
  background: var(--color-surface-2);
  overflow: hidden;
}
.rank-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, var(--color-primary), #3ec47f);
}
.rank-nums {
  font-size: 12px;
  color: var(--color-ink-soft);
  white-space: nowrap;
}
.rank-nums strong {
  font-size: 15px;
  color: var(--color-ink);
  font-variant-numeric: tabular-nums;
}

/* ============ 通用面板（用户/公告） ============ */
.panel-view {
  padding: 20px 22px;
}
.panel-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 16px;
}
.panel-head h3 {
  margin: 0 0 2px;
  font-size: 17px;
  font-weight: 800;
  color: var(--color-ink);
}
.panel-sub {
  margin: 0;
  font-size: 12.5px;
  color: var(--color-ink-faint);
}
.user-cell {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.cell-avatar {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: var(--color-primary-tint);
  color: var(--color-primary-deep);
  font-size: 12px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.status-dot {
  display: inline-block;
  font-size: 12px;
  font-weight: 600;
  padding: 3px 10px;
  border-radius: 999px;
}
.status-dot.ok {
  color: var(--color-moss);
  background: var(--color-primary-tint);
}
.status-dot.off {
  color: var(--color-rust);
  background: #fdecea;
}
.ann-title {
  font-weight: 600;
  color: var(--color-ink);
}

/* ============ 用户详情抽屉 ============ */
.user-hero {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--color-primary-tint), #ffffff 70%);
  border: 1px solid var(--color-border);
}
.hero-avatar {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-deep));
  color: #fff;
  font-size: 22px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 6px 14px rgba(23, 160, 92, 0.25);
}
.hero-main {
  min-width: 0;
}
.hero-name {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 17px;
  font-weight: 800;
  color: var(--color-ink);
}
.hero-meta {
  margin-top: 4px;
  font-size: 12.5px;
  color: var(--color-ink-soft);
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.hero-meta .dot {
  color: var(--color-ink-faint);
}

.sum-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 10px;
  margin: 16px 0;
}
.sum-item {
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 12px 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}
.sum-value {
  font-size: 20px;
  font-weight: 800;
  color: var(--color-ink);
  font-variant-numeric: tabular-nums;
  line-height: 1.2;
}
.sum-value.moss { color: var(--color-moss); }
.sum-value.amber { color: var(--color-amber); }
.sum-value.blue { color: var(--color-blue); }
.sum-value.rust { color: var(--color-rust); }
.sum-label {
  font-size: 11px;
  color: var(--color-ink-faint);
}

.mastery-bar {
  padding: 12px 14px;
  border: 1px solid var(--color-border);
  border-radius: 14px;
  margin-bottom: 8px;
}
.mastery-head {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--color-ink-soft);
  margin-bottom: 8px;
}
.mastery-head strong {
  color: var(--color-ink);
}

.mini-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 360px;
  overflow-y: auto;
  padding-right: 4px;
}
.mini-row {
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 10px 12px;
  background: var(--color-surface);
}
.mini-word {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.mini-word strong {
  font-size: 15px;
  color: var(--color-ink);
}
.mini-phonetic {
  font-size: 12px;
  color: var(--color-ink-faint);
}
.mini-status {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 999px;
}
.mini-status.moss { background: var(--color-primary-tint); color: var(--color-moss); }
.mini-status.amber { background: #fdf3e2; color: var(--color-amber); }
.mini-status.blue { background: #e7f1fc; color: var(--color-blue); }
.mini-status.info { background: var(--color-surface-2); color: var(--color-ink-soft); }
.mini-status.rust { background: #fdecea; color: var(--color-rust); }
.mini-sub {
  margin-top: 4px;
  font-size: 12px;
  color: var(--color-ink-soft);
}
.fav-time {
  color: var(--color-ink-faint);
}

/* ============ 响应式 ============ */
@media (max-width: 1280px) {
  .kpi-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  .sum-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 980px) {
  .admin-layout {
    flex-direction: column;
  }
  .admin-side {
    position: static;
    width: 100%;
    height: auto;
    flex-direction: row;
    align-items: center;
    border-right: none;
    border-bottom: 1px solid var(--color-border);
    padding: 8px 14px;
    gap: 10px;
  }
  .side-brand {
    padding: 4px 6px;
  }
  .side-nav {
    flex-direction: row;
    flex: 1;
    padding: 0;
  }
  .side-item {
    flex: 1;
    justify-content: center;
    padding: 8px 6px;
  }
  .side-foot {
    display: none;
  }
  .chart-grid {
    grid-template-columns: 1fr;
  }
  .chart-card.span-2 {
    grid-column: span 1;
  }
  .admin-top {
    padding: 12px 16px;
  }
  .admin-content {
    padding: 16px 16px 56px;
  }
  .back-home span {
    display: none;
  }
}
</style>
