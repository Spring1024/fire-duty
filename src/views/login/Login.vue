<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <div class="login-logo">🔥 消防履职系统</div>
        <p class="login-desc">消防设备巡检与隐患整改管理平台</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        class="login-form"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <div class="login-options">
          <el-checkbox v-model="rememberMe">记住我</el-checkbox>
          <el-link type="primary" :underline="false">忘记密码?</el-link>
        </div>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const appStore = useAppStore()
const formRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const form = reactive({
  username: 'admin',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

// Load remembered username
onMounted(() => {
  const saved = localStorage.getItem('remembered_username')
  if (saved) {
    form.username = saved
    rememberMe.value = true
  }
})

function handleLogin() {
  formRef.value?.validate((valid) => {
    if (!valid) return
    loading.value = true

    // Simulate login delay
    setTimeout(() => {
      try {
        appStore.login({ username: form.username, password: form.password })

        if (rememberMe.value) {
          localStorage.setItem('remembered_username', form.username)
        } else {
          localStorage.removeItem('remembered_username')
        }

        ElMessage.success('登录成功！')
        router.push('/dashboard')
      } catch (err) {
        ElMessage.error('登录失败，请检查用户名和密码')
      } finally {
        loading.value = false
      }
    }, 600)
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0c0c1d 0%, #1a1a3e 40%, #162447 100%);
  position: relative;
  overflow: hidden;
}

/* Decorative circles */
.login-container::before {
  content: '';
  position: absolute;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(24, 144, 255, 0.12) 0%, transparent 70%);
  top: -150px;
  right: -100px;
}

.login-container::after {
  content: '';
  position: absolute;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(24, 144, 255, 0.08) 0%, transparent 70%);
  bottom: -100px;
  left: -80px;
}

.login-card {
  width: 400px;
  padding: 40px 36px 28px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.35);
  position: relative;
  z-index: 1;
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-logo {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a3e;
  letter-spacing: 2px;
}

.login-desc {
  font-size: 13px;
  color: #999;
  margin-top: 8px;
}

.login-form {
  margin-top: 4px;
}

.login-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  margin-top: -4px;
}

.login-btn {
  width: 100%;
  font-size: 16px;
  letter-spacing: 4px;
  height: 44px;
  border-radius: 6px;
}
</style>
