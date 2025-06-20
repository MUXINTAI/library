<template>
  <div class="test-page">
    <h1>测试页面</h1>
    <div class="debug-info">
      <h2>认证状态调试</h2>
      <p>当前路径: {{ $route.path }}</p>
      <p>是否已认证: {{ authStore.isAuthenticated }}</p>
      <p>用户信息: {{ authStore.user }}</p>
      <p>Token存在: {{ !!authStore.token }}</p>
      
      <div class="actions">
        <el-button @click="clearAuth">清除认证</el-button>
        <el-button @click="goToLogin">去登录</el-button>
        <el-button @click="checkLocalStorage">检查本地存储</el-button>
      </div>
      
      <div class="local-storage-info">
        <h3>本地存储信息：</h3>
        <pre>{{ localStorageInfo }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const localStorageInfo = ref('')

const clearAuth = () => {
  authStore.logout()
  checkLocalStorage()
}

const goToLogin = () => {
  router.push('/login')
}

const checkLocalStorage = () => {
  localStorageInfo.value = JSON.stringify({
    token: localStorage.getItem('token'),
    user: localStorage.getItem('user')
  }, null, 2)
}

onMounted(() => {
  checkLocalStorage()
})
</script>

<style scoped>
.test-page {
  padding: 20px;
}

.debug-info {
  background: #f5f5f5;
  padding: 20px;
  border-radius: 8px;
  margin-top: 20px;
}

.actions {
  margin: 20px 0;
}

.local-storage-info {
  margin-top: 20px;
}

pre {
  background: #fff;
  padding: 10px;
  border-radius: 4px;
  border: 1px solid #ddd;
}
</style> 