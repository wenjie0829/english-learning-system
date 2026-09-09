<template>
  <header class="app-header">
    <div class="header-inner">
      <!-- 品牌 -->
      <router-link to="/" class="brand" :title="'回到首页'">
        <span class="brand-logo">词</span>
        <span class="brand-name">自在背单词</span>
      </router-link>

      <!-- 主导航（main 模式） -->
      <nav v-if="mode === 'main'" class="nav">
        <router-link
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="nav-link"
          :class="{ active: isActive(item.to) }"
        >
          <el-icon :size="16"><component :is="item.icon" /></el-icon>
          <span class="nav-label">{{ item.label }}</span>
        </router-link>
      </nav>

      <!-- 简单标题（simple 模式） -->
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
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import {
  HomeFilled, Reading, Refresh, TrendCharts, Search, Star, Warning, Setting
} from '@element-plus/icons-vue'

const props = defineProps({
  mode: { type: String, default: 'main' }, // main | simple | admin
  title: { type: String, default: '' },
  back: { type: Boolean, default: false }
})

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const navItems = [
  { to: '/', label: '首页', icon: HomeFilled },
  { to: '/learn', label: '开始学习', icon: Reading },
  { to: '/review', label: '单词复习', icon: Refresh },
  { to: '/statistics', label: '学习统计', icon: TrendCharts },
  { to: '/search', label: '查单词', icon: Search },
  { to: '/favorites', label: '收藏', icon: Star },
  { to: '/wrong-words', label: '错词本', icon: Warning }
]

const isActive = (to) => {
  if (to === '/') return route.path === '/'
  return route.path.startsWith(to)
}

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
</script>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--color-border);
}
.header-inner {
  max-width: 1120px;
  margin: 0 auto;
  height: 62px;
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

/* 导航 */
.nav {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
  min-width: 0;
  overflow-x: auto;
}
.nav-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border-radius: var(--radius-pill);
  color: var(--color-ink-soft);
  font-size: 14px;
  font-weight: 500;
  text-decoration: none;
  white-space: nowrap;
  transition: all 0.15s ease;
}
.nav-link:hover {
  color: var(--color-primary);
  background: var(--color-primary-tint);
}
.nav-link.active {
  color: var(--color-primary-deep);
  background: var(--color-primary-tint);
  font-weight: 600;
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

@media (max-width: 900px) {
  .nav-label {
    display: none;
  }
  .nav-link {
    padding: 7px 10px;
  }
  .user-name {
    display: none;
  }
}
@media (max-width: 720px) {
  .brand-name,
  .admin-chip span {
    display: none;
  }
}
</style>
