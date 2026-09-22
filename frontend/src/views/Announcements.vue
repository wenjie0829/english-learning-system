<template>
  <div class="announcements-page">
    <AppHeader mode="simple" title="系统公告" back />

    <main class="page-shell list-main">
      <!-- 加载中 -->
      <div v-if="loading" class="list-state card">
        <el-icon class="is-loading" :size="36"><Loading /></el-icon>
        <p>正在加载公告…</p>
      </div>

      <!-- 分支：暂无公告 -->
      <div v-else-if="announcements.length === 0" class="list-state card">
        <el-icon :size="48" color="var(--color-amber)"><Bell /></el-icon>
        <h3>暂无公告</h3>
        <p>系统还没有发布任何公告，先去背几个单词吧</p>
        <div class="state-btns">
          <el-button type="primary" round @click="goHome">返回首页</el-button>
        </div>
      </div>

      <!-- 分支：有公告 → 展示公告列表 -->
      <div v-else class="announce-body">
        <div class="announce-summary card">
          <div class="summary-left">
            <el-icon :size="17"><Bell /></el-icon>
            <span>共 <strong>{{ announcements.length }}</strong> 条公告</span>
          </div>
          <span class="summary-hint">点击任意公告查看完整内容</span>
        </div>

        <div class="announce-list">
          <article
            v-for="(ann, idx) in announcements"
            :key="ann.id"
            class="announce-card card"
            @click="openDetail(ann)"
          >
            <div class="announce-head">
              <span class="announce-badge">公告</span>
              <h3 class="announce-title">{{ ann.title }}</h3>
              <span v-if="idx === 0" class="announce-new">最新</span>
            </div>

            <p class="announce-excerpt">{{ ann.content || '（本条公告暂无正文）' }}</p>

            <div class="announce-foot">
              <span class="announce-time">
                <el-icon><Clock /></el-icon>
                {{ formatDateTime(ann.createdAt) }}
              </span>
              <span class="announce-more">查看详情 <el-icon><ArrowRight /></el-icon></span>
            </div>
          </article>
        </div>
      </div>
    </main>

    <!-- 点击某条公告 → 弹出公告详情 -->
    <el-dialog
      v-model="detailVisible"
      width="640px"
      align-center
      :show-close="true"
    >
      <template #header>
        <div class="detail-head">
          <span class="announce-badge">公告</span>
          <h3 class="detail-title">{{ current?.title || '公告详情' }}</h3>
        </div>
      </template>

      <div v-if="current" class="detail-body">
        <div class="detail-meta">
          <el-icon><Clock /></el-icon>
          <span>发布时间：{{ formatDateTime(current.createdAt) }}</span>
        </div>
        <div class="detail-content">{{ current.content || '（本条公告暂无正文）' }}</div>
      </div>

      <template #footer>
        <el-button type="primary" round @click="detailVisible = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/AppHeader.vue'
import { getAnnouncementList } from '@/api/announcement'
import { Loading, Bell, Clock, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()

const announcements = ref([])
const loading = ref(true)
const detailVisible = ref(false)
const current = ref(null)

// LocalDateTime 序列化形如 2026-09-22T15:30:12.345 → 取到分钟
const formatDateTime = (s) => {
  if (!s) return '—'
  return String(s).replace('T', ' ').slice(0, 16)
}

const openDetail = (ann) => {
  current.value = ann
  detailVisible.value = true
}

const goHome = () => router.push('/')

const load = async () => {
  loading.value = true
  try {
    const res = await getAnnouncementList()
    announcements.value = Array.isArray(res) ? res : []
  } catch (e) {
    console.error('公告加载失败:', e)
    announcements.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.list-main {
  max-width: 860px;
}

/* ---------- 加载 / 空状态 ---------- */
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

/* ---------- 顶部汇总条 ---------- */
.announce-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.announce-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 18px;
}
.summary-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--color-ink);
}
.summary-left strong {
  color: var(--color-primary-deep);
  font-size: 17px;
  margin: 0 2px;
}
.summary-hint {
  font-size: 12.5px;
  color: var(--color-ink-faint);
}

/* ---------- 公告列表 ---------- */
.announce-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.announce-card {
  padding: 18px 20px;
  cursor: pointer;
  transition: box-shadow 0.18s ease, border-color 0.18s ease, transform 0.18s ease;
}
.announce-card:hover {
  border-color: var(--color-primary);
  box-shadow: var(--shadow-card);
  transform: translateY(-1px);
}

.announce-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.announce-badge {
  flex-shrink: 0;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-deep));
  padding: 2px 9px;
  border-radius: 999px;
}
.announce-title {
  margin: 0;
  font-size: 15.5px;
  font-weight: 700;
  color: var(--color-ink);
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.announce-new {
  flex-shrink: 0;
  margin-left: auto;
  font-size: 11px;
  font-weight: 700;
  color: var(--color-amber);
  background: #fdf1e3;
  padding: 2px 9px;
  border-radius: 999px;
}

.announce-excerpt {
  margin: 0 0 12px;
  font-size: 13px;
  line-height: 1.65;
  color: var(--color-ink-soft);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}

.announce-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px dashed var(--color-border);
}
.announce-time {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--color-ink-faint);
}
.announce-more {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--color-primary-deep);
}

/* ---------- 详情弹窗 ---------- */
.detail-head {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-right: 28px;
}
.detail-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--color-ink);
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.detail-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.detail-meta {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12.5px;
  color: var(--color-ink-faint);
}
.detail-content {
  padding: 16px 18px;
  border-radius: 14px;
  background: var(--color-surface-2);
  border: 1px solid var(--color-border);
  font-size: 14px;
  line-height: 1.85;
  color: var(--color-ink);
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 52vh;
  overflow-y: auto;
}

@media (max-width: 640px) {
  .announce-summary {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  .announce-foot {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
}
</style>
