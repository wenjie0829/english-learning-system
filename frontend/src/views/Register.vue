<template>
  <AuthLayout
    eyebrow="REGISTER"
    title="创建账号"
    subtitle="开始记录属于你的第一张打卡"
  >
    <el-form
      :model="registerForm"
      :rules="rules"
      ref="registerFormRef"
      label-position="top"
      size="large"
      @submit.prevent
    >
      <el-form-item label="用户名" prop="username">
        <el-input v-model="registerForm.username" placeholder="3-50 个字符" :prefix-icon="User" />
      </el-form-item>

      <el-form-item label="邮箱（选填）" prop="email">
        <el-input v-model="registerForm.email" placeholder="用于找回密码" :prefix-icon="Message" />
      </el-form-item>

      <el-form-item label="密码" prop="password">
        <el-input
          v-model="registerForm.password"
          type="password"
          placeholder="至少 6 位"
          show-password
          :prefix-icon="Lock"
        />
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="registerForm.confirmPassword"
          type="password"
          placeholder="再次输入密码"
          show-password
          :prefix-icon="Lock"
          @keyup.enter="handleRegister"
        />
      </el-form-item>

      <button class="auth-submit-btn" :disabled="loading" @click.prevent="handleRegister">
        <span v-if="!loading">注 册</span>
        <span v-else>注册中…</span>
      </button>

      <p class="auth-switch-line">
        已经有账号？
        <a href="#" @click.prevent="goToLogin">直接登录</a>
      </p>
    </el-form>
  </AuthLayout>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message } from '@element-plus/icons-vue'
import AuthLayout from '@/components/AuthLayout.vue'
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
    if (!valid) return
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
  })
}

const goToLogin = () => router.push('/login')
</script>
