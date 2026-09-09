<template>
  <div class="auth-page">
    <div class="auth-card">
      <!-- 左侧品牌区 -->
      <aside class="brand-side">
        <div class="brand-top">
          <span class="brand-logo">词</span>
          <span class="brand-name">自在背单词</span>
        </div>
        <h1 class="brand-slogan">{{ slogan }}</h1>
        <p class="brand-desc">{{ desc }}</p>

        <ul class="brand-points">
          <li v-for="p in points" :key="p.text">
            <span class="point-icon" :style="{ background: p.bg, color: p.color }">
              <el-icon :size="15"><component :is="p.icon" /></el-icon>
            </span>
            <div>
              <b>{{ p.text }}</b>
              <span>{{ p.sub }}</span>
            </div>
          </li>
        </ul>

        <div class="brand-foot">单词 · 例句 · 打卡 · 统计，一站搞定</div>
      </aside>

      <!-- 右侧表单区 -->
      <section class="form-side">
        <div class="form-card">
          <span class="form-eyebrow">{{ eyebrow }}</span>
          <h2>{{ title }}</h2>
          <p class="form-sub">{{ subtitle }}</p>
          <slot />
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { Calendar, Refresh, TrendCharts } from '@element-plus/icons-vue'

defineProps({
  eyebrow: { type: String, default: 'WELCOME' },
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  slogan: { type: String, default: '每天学一点，坚持看得见' },
  desc: { type: String, default: '新词学习、智能复习与数据打卡一体化的英语学习工具。' }
})

const points = [
  { icon: Calendar, text: '学习即打卡', sub: '学完自动记录，无需手动签到', bg: 'rgba(255,255,255,.16)', color: '#fff' },
  { icon: Refresh, text: '按节奏复习', sub: '系统安排复习时机，记得更牢', bg: 'rgba(255,255,255,.16)', color: '#fff' },
  { icon: TrendCharts, text: '进度可视化', sub: '趋势图、热力图随时查看', bg: 'rgba(255,255,255,.16)', color: '#fff' }
]
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 28px 20px;
  background: var(--color-bg);
  background-image:
    radial-gradient(520px 260px at 8% 6%, rgba(23, 160, 92, 0.08), transparent 60%),
    radial-gradient(600px 320px at 96% 96%, rgba(74, 144, 226, 0.07), transparent 60%);
}
.auth-card {
  width: 100%;
  max-width: 1060px;
  min-height: 640px;
  display: grid;
  grid-template-columns: 1.05fr 1fr;
  background: var(--color-surface);
  border-radius: 28px;
  overflow: hidden;
  box-shadow: 0 24px 64px rgba(20, 40, 28, 0.14);
  border: 1px solid var(--color-border);
}

/* ---- 左侧 ---- */
.brand-side {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 46px 48px 40px;
  color: #fff;
  background:
    radial-gradient(640px 300px at 110% -10%, rgba(255, 255, 255, 0.18), transparent 55%),
    linear-gradient(140deg, #0e7c46 0%, #17a05c 58%, #25b06b 100%);
  overflow: hidden;
}
.brand-side::after {
  content: 'Aa';
  position: absolute;
  right: -26px;
  bottom: -70px;
  font-size: 240px;
  font-weight: 800;
  line-height: 1;
  color: rgba(255, 255, 255, 0.09);
  letter-spacing: -0.08em;
  font-family: var(--font-display);
  pointer-events: none;
}
.brand-top {
  display: flex;
  align-items: center;
  gap: 12px;
  position: relative;
  z-index: 1;
}
.brand-logo {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.95);
  color: var(--color-primary-deep);
  font-size: 20px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-name {
  font-size: 19px;
  font-weight: 700;
}
.brand-slogan {
  position: relative;
  z-index: 1;
  margin: 54px 0 10px;
  font-size: 27px;
  font-weight: 700;
  line-height: 1.4;
  letter-spacing: 0.01em;
}
.brand-desc {
  position: relative;
  z-index: 1;
  margin: 0 0 34px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.82);
  max-width: 300px;
  line-height: 1.7;
}
.brand-points {
  position: relative;
  z-index: 1;
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.brand-points li {
  display: flex;
  align-items: center;
  gap: 12px;
}
.point-icon {
  width: 36px;
  height: 36px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.brand-points b {
  display: block;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.4;
}
.brand-points li > div > span {
  display: block;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.72);
  line-height: 1.4;
  margin-top: 1px;
}
.brand-foot {
  position: relative;
  z-index: 1;
  margin-top: auto;
  padding-top: 30px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

/* ---- 右侧 ---- */
.form-side {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 56px;
}
.form-card {
  width: 100%;
  max-width: 360px;
}
.form-eyebrow {
  display: inline-block;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  color: var(--color-primary-deep);
  background: var(--color-primary-tint);
  border-radius: 999px;
  padding: 4px 12px;
  margin-bottom: 16px;
}
.form-card h2 {
  margin: 0 0 6px;
  font-size: 26px;
  font-weight: 800;
  color: var(--color-ink);
  letter-spacing: -0.01em;
}
.form-sub {
  margin: 0 0 26px;
  font-size: 13px;
  color: var(--color-ink-soft);
}
.form-card :deep(.el-form-item) {
  margin-bottom: 20px;
}
.form-card :deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--color-ink);
  padding-bottom: 6px;
}
.form-card :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 4px 14px;
}
.form-card .auth-submit-btn {
  width: 100%;
  height: 46px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-deep));
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.18s ease;
  box-shadow: 0 10px 22px rgba(23, 160, 92, 0.28);
  margin-top: 4px;
}
.form-card .auth-submit-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 14px 28px rgba(23, 160, 92, 0.34);
}
.form-card .auth-submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.form-card .auth-switch-line {
  text-align: center;
  font-size: 13px;
  color: var(--color-ink-soft);
  margin: 20px 0 0;
}
.form-card .auth-switch-line a {
  color: var(--color-primary-deep);
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
}
.form-card .auth-switch-line a:hover {
  text-decoration: underline;
}

@media (max-width: 880px) {
  .auth-card {
    grid-template-columns: 1fr;
    max-width: 480px;
  }
  .brand-side {
    padding: 32px 36px 36px;
  }
  .brand-slogan {
    margin-top: 30px;
    font-size: 22px;
  }
  .brand-points {
    display: none;
  }
  .brand-foot {
    display: none;
  }
  .form-side {
    padding: 38px 32px;
  }
}
</style>
