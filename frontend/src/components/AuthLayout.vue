<template>
  <div class="auth-page">
    <div class="auth-card">
      <!-- 左侧品牌区（深墨绿 + 植物插画） -->
      <aside class="brand-side">
        <!-- 背景图案：枝叶插画 -->
        <svg
          class="brand-art"
          viewBox="0 0 300 260"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
          aria-hidden="true"
        >
          <!-- 主枝干 -->
          <path
            d="M150 258C147 214 152 170 150 120"
            stroke="#9a6236"
            stroke-width="11"
            stroke-linecap="round"
          />
          <!-- 左右分枝 -->
          <path
            d="M150 200C124 182 100 172 74 156"
            stroke="#9a6236"
            stroke-width="7.5"
            stroke-linecap="round"
          />
          <path
            d="M150 176C178 158 202 152 228 138"
            stroke="#9a6236"
            stroke-width="7.5"
            stroke-linecap="round"
          />
          <path
            d="M150 142C132 126 118 112 108 92"
            stroke="#a8d94f"
            stroke-width="3.6"
            stroke-linecap="round"
          />
          <path
            d="M150 128C168 112 184 102 200 84"
            stroke="#a8d94f"
            stroke-width="3.6"
            stroke-linecap="round"
          />
          <path
            d="M74 156C60 146 50 132 42 116"
            stroke="#c3e88a"
            stroke-width="3.2"
            stroke-linecap="round"
          />
          <path
            d="M228 138C238 126 244 112 248 96"
            stroke="#c3e88a"
            stroke-width="3.2"
            stroke-linecap="round"
          />
          <path
            d="M150 84C148 68 152 52 150 38"
            stroke="#c97a3f"
            stroke-width="4"
            stroke-linecap="round"
          />
          <!-- 叶果 -->
          <circle cx="42" cy="112" r="9" fill="#a8d94f" />
          <circle cx="108" cy="88" r="7.5" fill="#c3e88a" />
          <circle cx="150" cy="32" r="8" fill="#d98f4a" />
          <circle cx="200" cy="80" r="9" fill="#a8d94f" />
          <circle cx="248" cy="91" r="7" fill="#c3e88a" />
          <circle cx="74" cy="152" r="8" fill="#d98f4a" />
          <circle cx="228" cy="134" r="7" fill="#c3e88a" />
        </svg>

        <div class="brand-top">
          <span class="brand-logo">词</span>
          <span class="brand-name">自在背单词</span>
        </div>

        <h1 class="brand-slogan">{{ slogan }}</h1>
        <p class="brand-desc">{{ desc }}</p>

        <ul class="brand-points">
          <li v-for="p in points" :key="p.text">
            <span class="point-icon">
              <el-icon :size="15"><component :is="p.icon" /></el-icon>
            </span>
            <div>
              <b>{{ p.text }}</b>
              <span>{{ p.sub }}</span>
            </div>
          </li>
        </ul>

        <div class="brand-foot">
          <span class="foot-line"></span>
          <span class="foot-text">单词 · 例句 · 打卡 · 统计，一站搞定</span>
        </div>
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
  desc: { type: String, default: '新词学习、复习与数据打卡一体化的英语学习工具。' }
})

const points = [
  { icon: Calendar, text: '学习即打卡', sub: '学完自动记录，无需手动签到' },
  { icon: Refresh, text: '按节奏复习', sub: '系统安排复习时机，记得更牢' },
  { icon: TrendCharts, text: '进度可视化', sub: '趋势图、热力图随时查看' }
]
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 28px 20px;
  background: var(--color-bg-gradient);
  background-attachment: fixed;
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
  box-shadow: 0 28px 70px rgba(20, 48, 28, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.6);
}

/* ---- 左侧品牌区 ---- */
.brand-side {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 44px 48px 38px;
  color: #fff;
  background:
    radial-gradient(620px 320px at 108% -12%, rgba(168, 217, 79, 0.18), transparent 58%),
    linear-gradient(158deg, #0a2718 0%, #0e3520 46%, #12492b 100%);
  overflow: hidden;
}

/* 背景图案：枝叶插画（右下角，低透明度） */
.brand-art {
  position: absolute;
  right: -34px;
  bottom: -26px;
  width: 330px;
  height: auto;
  opacity: 0.55;
  pointer-events: none;
}

.brand-top {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}
.brand-logo {
  width: 44px;
  height: 44px;
  border-radius: 13px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(168, 217, 79, 0.45);
  color: #d9f0a8;
  font-size: 21px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-name {
  font-size: 19px;
  font-weight: 700;
  letter-spacing: 0.02em;
}
.brand-slogan {
  position: relative;
  z-index: 1;
  margin: 52px 0 12px;
  font-size: 30px;
  font-weight: 800;
  line-height: 1.42;
  letter-spacing: 0.01em;
}
.brand-desc {
  position: relative;
  z-index: 1;
  margin: 0 0 36px;
  font-size: 14px;
  color: rgba(226, 246, 210, 0.78);
  max-width: 306px;
  line-height: 1.75;
}
.brand-points {
  position: relative;
  z-index: 1;
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 17px;
}
.brand-points li {
  display: flex;
  align-items: center;
  gap: 13px;
}
.point-icon {
  width: 38px;
  height: 38px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: rgba(168, 217, 79, 0.16);
  border: 1px solid rgba(168, 217, 79, 0.3);
  color: #b9e26a;
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
  color: rgba(226, 246, 210, 0.62);
  line-height: 1.4;
  margin-top: 1px;
}
.brand-foot {
  position: relative;
  z-index: 1;
  margin-top: auto;
  padding-top: 28px;
}
.foot-line {
  display: block;
  width: 100%;
  height: 1px;
  margin-bottom: 14px;
  background: repeating-linear-gradient(
    90deg,
    rgba(168, 217, 79, 0.5) 0 7px,
    transparent 7px 15px
  );
}
.foot-text {
  font-size: 12px;
  color: rgba(226, 246, 210, 0.55);
}

/* ---- 右侧表单区 ---- */
.form-side {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 56px;
  background: #fff;
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

/* ---- 提交按钮（:deep 才能作用到父组件传入的 slot 内容） ---- */
.form-card :deep(.auth-submit-btn) {
  position: relative;
  width: 100%;
  height: 50px;
  margin-top: 6px;
  border: none;
  border-radius: 13px;
  overflow: hidden;
  background: linear-gradient(135deg, #12894a 0%, #0b5f33 55%, #0a4d2a 100%);
  color: #fff;
  font-family: var(--font-body);
  font-size: 15.5px;
  font-weight: 700;
  letter-spacing: 0.22em;
  text-indent: 0.22em;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, filter 0.18s ease;
  box-shadow: 0 10px 24px rgba(11, 95, 51, 0.3);
}
.form-card :deep(.auth-submit-btn::before) {
  content: '';
  position: absolute;
  inset: 0 0 50% 0;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.16), rgba(255, 255, 255, 0));
  pointer-events: none;
}
.form-card :deep(.auth-submit-btn:hover:not(:disabled)) {
  transform: translateY(-2px);
  filter: brightness(1.06);
  box-shadow: 0 16px 32px rgba(11, 95, 51, 0.36);
}
.form-card :deep(.auth-submit-btn:active:not(:disabled)) {
  transform: translateY(0);
  box-shadow: 0 6px 16px rgba(11, 95, 51, 0.28);
}
.form-card :deep(.auth-submit-btn:disabled) {
  opacity: 0.62;
  cursor: not-allowed;
  box-shadow: none;
}
.form-card :deep(.auth-switch-line) {
  text-align: center;
  font-size: 13px;
  color: var(--color-ink-soft);
  margin: 20px 0 0;
}
.form-card :deep(.auth-switch-line a) {
  color: var(--color-primary-deep);
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
}
.form-card :deep(.auth-switch-line a:hover) {
  text-decoration: underline;
}

@media (max-width: 880px) {
  .auth-card {
    grid-template-columns: 1fr;
    max-width: 480px;
    min-height: auto;
  }
  .brand-side {
    padding: 30px 32px 32px;
  }
  .brand-slogan {
    margin-top: 26px;
    font-size: 23px;
  }
  .brand-points,
  .brand-foot {
    display: none;
  }
  .brand-art {
    width: 220px;
    right: -40px;
    bottom: -30px;
    opacity: 0.4;
  }
  .form-side {
    padding: 34px 30px 38px;
  }
}
</style>
