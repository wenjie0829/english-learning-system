<template>
  <div class="auth-page">
    <div class="auth-layout">
      <div class="brand-panel">
        <div class="brand-mark">
          <span class="brand-mark-glyph">词</span>
        </div>
        <h1 class="brand-title dict-heading">智能英语学习系统</h1>
        <p class="brand-tagline">建一张属于你的单词卡片目录</p>

        <div class="curve-wrap">
          <svg viewBox="0 0 520 290" class="brand-tree" aria-hidden="true">
  <defs><linearGradient id="leaf" x1="0" x2="1"><stop stop-color="#dff6a8"/><stop offset="1" stop-color="#83c970"/></linearGradient></defs>
  <path d="M258 280 C250 226 258 187 252 142 C246 105 251 70 261 28" fill="none" stroke="#a77345" stroke-width="18" stroke-linecap="round"/>
  <path d="M255 166 C200 158 150 123 94 70" fill="none" stroke="#a77345" stroke-width="9" stroke-linecap="round"/>
  <path d="M256 130 C311 118 360 84 420 39" fill="none" stroke="#a77345" stroke-width="9" stroke-linecap="round"/>
  <path d="M253 204 C203 198 152 177 98 132" fill="none" stroke="#a77345" stroke-width="8" stroke-linecap="round"/>
  <path d="M258 194 C313 183 364 158 438 116" fill="none" stroke="#a77345" stroke-width="8" stroke-linecap="round"/>
  <g fill="none" stroke="url(#leaf)" stroke-width="5" stroke-linecap="round">
    <path d="M258 54 C211 51 185 72 161 102 C136 132 112 126 84 93"/><path d="M255 80 C306 60 342 78 364 104 C391 135 424 119 450 83"/><path d="M255 120 C213 111 184 135 157 161 C132 184 105 174 78 149"/><path d="M259 151 C305 133 337 145 365 168 C394 193 430 180 459 153"/><path d="M253 202 C220 187 196 199 173 219"/><path d="M258 214 C297 198 329 205 355 224"/>
  </g>
  <g fill="#dff6a8"><circle cx="84" cy="93" r="8"/><circle cx="161" cy="102" r="7"/><circle cx="450" cy="83" r="8"/><circle cx="364" cy="104" r="7"/><circle cx="78" cy="149" r="7"/><circle cx="157" cy="161" r="7"/><circle cx="459" cy="153" r="7"/><circle cx="365" cy="168" r="7"/><circle cx="173" cy="219" r="6"/><circle cx="355" cy="224" r="6"/></g>
  <path d="M52 280 H470" stroke="rgba(223,246,168,.35)" stroke-width="2" stroke-dasharray="5 8"/>
</svg>
          <div class="curve-caption">
            <span class="stage-dots">
              <span></span><span></span><span></span><span></span>
              <span></span><span></span><span></span><span></span>
            </span>
            <span class="curve-caption-text">你的第 0 阶段，从这里开始</span>
          </div>
        </div>
      </div>

      <div class="form-panel">
        <div class="index-card">
          <div class="index-card-tab">REGISTER</div>
          <h2 class="index-card-title dict-heading">注册</h2>
          <p class="index-card-sub">创建账号，开始你的学习曲线</p>

          <el-form
            :model="registerForm"
            :rules="rules"
            ref="registerFormRef"
            label-position="top"
            class="auth-form"
          >
            <el-form-item label="用户名" prop="username">
              <el-input v-model="registerForm.username" placeholder="3-50 个字符" size="large" />
            </el-form-item>

            <el-form-item label="邮箱（选填）" prop="email">
              <el-input v-model="registerForm.email" placeholder="用于找回密码" size="large" />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="至少 6 位"
                size="large"
                show-password
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="再次输入密码"
                size="large"
                show-password
                @keyup.enter="handleRegister"
              />
            </el-form-item>

            <button class="primary-btn" :disabled="loading" @click.prevent="handleRegister">
              <span v-if="!loading">注册</span>
              <span v-else>注册中…</span>
            </button>

            <p class="switch-line">
              已经有账号？<a href="#" @click.prevent="goToLogin">直接登录</a>
            </p>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度需在 3-50 个字符之间', trigger: 'blur' }
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await register({
          username: registerForm.username,
          email: registerForm.email || null,
          password: registerForm.password
        })
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } catch (error) {
        console.error('Register error:', error)
      } finally {
        loading.value = false
      }
    }
  })
}

const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  background: var(--color-paper);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-6);
}

.auth-layout {
  width: 100%;
  max-width: 980px;
  min-height: 620px;
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  background: var(--color-surface);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: var(--shadow-card-hover);
  border: 1px solid var(--color-border-soft);
}

.brand-panel {
  background: var(--color-primary-deep);
  background-image: radial-gradient(circle at 10% 10%, rgba(255,255,255,0.06), transparent 40%);
  color: #F3F6FA;
  padding: var(--space-8) var(--space-7);
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.brand-mark {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-5);
}
.brand-mark-glyph {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 24px;
  color: #F3F6FA;
}

.brand-title {
  font-size: 30px;
  font-weight: 700;
  margin: 0 0 var(--space-2) 0;
  line-height: 1.3;
}

.brand-tagline {
  font-family: var(--font-body);
  font-size: 15px;
  color: rgba(243, 246, 250, 0.72);
  margin: 0 0 var(--space-7) 0;
}

.curve-wrap {
  margin-top: var(--space-4);
}
.curve-svg {
  width: 100%;
  height: auto;
  display: block;
}
.curve-grid line {
  stroke: rgba(255, 255, 255, 0.08);
  stroke-width: 1;
}
.curve-forget {
  stroke: rgba(243, 246, 250, 0.3);
  stroke-width: 2;
  stroke-dasharray: 3 5;
}
.curve-node-start {
  fill: #E8A63C;
  stroke: var(--color-primary-deep);
  stroke-width: 2;
}
.curve-caption {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  margin-top: var(--space-4);
}
.curve-caption .stage-dots span {
  background: rgba(255, 255, 255, 0.18);
}
.curve-caption-text {
  font-size: 13px;
  color: rgba(243, 246, 250, 0.62);
}

.form-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-7);
  background: var(--color-paper-warm);
}

.index-card {
  width: 100%;
  max-width: 340px;
}

.index-card-tab {
  display: inline-block;
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 0.12em;
  color: var(--color-primary);
  background: var(--color-primary-tint);
  border-radius: 999px;
  padding: 4px 10px;
  margin-bottom: var(--space-4);
}

.index-card-title {
  font-size: 26px;
  font-weight: 600;
  margin: 0 0 4px 0;
}

.index-card-sub {
  font-size: 13px;
  color: var(--color-ink-soft);
  margin: 0 0 var(--space-6) 0;
}

.auth-form :deep(.el-form-item__label) {
  font-family: var(--font-body);
  font-weight: 500;
  color: var(--color-ink);
  padding-bottom: 4px;
}

.primary-btn {
  width: 100%;
  height: 44px;
  border: none;
  border-radius: var(--radius-sm);
  background: var(--color-primary);
  color: #fff;
  font-family: var(--font-body);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s ease;
  margin-top: var(--space-2);
}
.primary-btn:hover:not(:disabled) {
  background: var(--color-primary-deep);
}
.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.switch-line {
  text-align: center;
  font-size: 13px;
  color: var(--color-ink-soft);
  margin-top: var(--space-5);
}
.switch-line a {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 600;
}
.switch-line a:hover {
  text-decoration: underline;
}

@media (max-width: 860px) {
  .auth-layout {
    grid-template-columns: 1fr;
  }
  .brand-panel {
    padding: var(--space-6);
  }
  .curve-wrap {
    display: none;
  }
}
</style>
<style>
.auth-page{padding:28px;background:#bff28d;background-image:radial-gradient(circle at 12% 12%,#e7ffb9 0 4%,transparent 4.2%),radial-gradient(circle at 82% 84%,#8ecb68 0 8%,transparent 8.2%)}.auth-layout{max-width:1120px;min-height:640px;border:12px solid #0d2617;border-radius:30px;box-shadow:0 28px 70px rgba(13,38,23,.28)}.brand-panel{background:#0d2617;padding:68px 58px}.brand-mark{background:#c6f19d;border:0;border-radius:50%}.brand-mark-glyph{color:#102318}.brand-title{font-size:42px;letter-spacing:-.05em}.form-panel{background:#fffdf7}.index-card-tab{background:#dff3bc;color:#0d2617}.primary-btn{border-radius:999px;background:#0d2617;height:50px}
</style>
<style>
.auth-page{background:#edf3e4!important;background-image:radial-gradient(circle at 9% 14%,rgba(201,239,158,.55),transparent 25rem),radial-gradient(circle at 92% 86%,rgba(177,211,147,.4),transparent 28rem)!important}.auth-layout{grid-template-columns:1fr 1fr!important;max-width:1180px!important;min-height:670px!important;border:0!important;border-radius:28px!important;box-shadow:0 22px 60px rgba(16,35,24,.16)!important}.brand-panel{padding:50px 56px!important;background:linear-gradient(145deg,#112b1b,#0b2114)!important}.brand-panel::before{display:none!important}.brand-title{color:#fff!important;font-size:38px!important;position:relative;z-index:2}.brand-tagline{color:#dcebd1!important;position:relative;z-index:2}.curve-wrap{margin-top:8px!important;height:285px!important;overflow:hidden}.brand-tree{display:block;width:100%;height:285px}.curve-caption{display:none!important}.form-panel{padding:70px 64px!important;background:#fffdf8!important}.index-card{max-width:390px!important}.index-card-title{color:#102318!important}.auth-form .el-input__wrapper{min-height:50px;background:#f7f7f1;box-shadow:0 0 0 1px #d7dece inset}.primary-btn{background:#245b3c!important}
@media(max-width:860px){.auth-layout{grid-template-columns:1fr!important}.brand-panel{min-height:430px}.form-panel{padding:48px 30px!important}}
</style>