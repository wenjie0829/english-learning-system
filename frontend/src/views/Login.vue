<template>
  <AuthLayout
    eyebrow="LOGIN"
    title="登录"
    subtitle="欢迎回来，继续你的学习打卡"
  >
    <el-form
      :model="loginForm"
      :rules="rules"
      ref="loginFormRef"
      label-position="top"
      size="large"
      @submit.prevent
    >
      <el-form-item label="用户名" prop="username">
        <el-input
          v-model="loginForm.username"
          placeholder="请输入用户名"
          :prefix-icon="User"
        />
      </el-form-item>

      <el-form-item label="密码" prop="password">
        <el-input
          v-model="loginForm.password"
          type="password"
          placeholder="请输入密码"
          show-password
          :prefix-icon="Lock"
          @keyup.enter="handleLogin"
        />
      </el-form-item>

      <button class="auth-submit-btn" :disabled="loading" @click.prevent="handleLogin">
        <span v-if="!loading">登 录</span>
        <span v-else>登录中…</span>
      </button>

      <p class="auth-switch-line">
        还没有账号？
        <a href="#" @click.prevent="goToRegister">立即注册</a>
      </p>
    </el-form>
  </AuthLayout>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import AuthLayout from '@/components/AuthLayout.vue'
import { useUserStore } from '@/store/user'
import { login } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const response = await login(loginForm)
      userStore.setToken(response.token)
      userStore.setUser({
        id: response.id,
        username: response.username,
        email: response.email,
        role: response.role
      })
      ElMessage.success('登录成功')
      router.push(response.role === 'ADMIN' ? '/admin' : '/')
    } catch (error) {
      console.error('Login error:', error)
    } finally {
      loading.value = false
    }
  })
}

const goToRegister = () => router.push('/register')
</script>
