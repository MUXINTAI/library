<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">个人信息</h1>
      <p class="page-subtitle">管理个人账户信息</p>
    </div>
    
    <div class="card-container">
      <div class="card-header">
        <h3>基本信息</h3>
        <el-button 
          type="primary" 
          @click="editMode = !editMode"
          v-if="!editMode"
        >
          编辑资料
        </el-button>
        <div v-else>
          <el-button type="success" @click="saveProfile" :loading="saving">保存</el-button>
          <el-button @click="cancelEdit">取消</el-button>
        </div>
      </div>
      
      <div class="card-body" v-loading="loading">
        <el-row :gutter="24" v-if="!loading">
          <el-col :span="8">
            <div class="profile-avatar">
              <el-avatar :size="120" class="user-avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="avatar-info">
                <h3>{{ userInfo.realName }}</h3>
                <el-tag :type="userInfo.enabled ? 'success' : 'danger'">
                  {{ userInfo.enabled ? '正常' : '已禁用' }}
                </el-tag>
              </div>
            </div>
          </el-col>
          
          <el-col :span="16">
            <el-form 
              :model="formData" 
              :rules="formRules" 
              ref="profileForm"
              label-width="100px"
              class="profile-form"
            >
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="用户名" prop="username">
                    <el-input 
                      v-model="formData.username" 
                      :disabled="true"
                      placeholder="用户名"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="真实姓名" prop="realName">
                    <el-input 
                      v-model="formData.realName" 
                      :disabled="!editMode"
                      placeholder="请输入真实姓名"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="邮箱" prop="email">
                    <el-input 
                      v-model="formData.email" 
                      :disabled="!editMode"
                      placeholder="请输入邮箱"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="手机号" prop="phone">
                    <el-input 
                      v-model="formData.phone" 
                      :disabled="!editMode"
                      placeholder="请输入手机号"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="16">
                <el-col :span="12">
                  <el-form-item label="用户角色">
                    <el-tag type="info">{{ getRoleText(userInfo.role) }}</el-tag>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="注册时间">
                    <span class="info-text">{{ formatDate(userInfo.createdAt) }}</span>
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-divider content-position="left">修改密码</el-divider>
              
              <el-row :gutter="16" v-if="editMode">
                <el-col :span="8">
                  <el-form-item label="当前密码" prop="currentPassword">
                    <el-input 
                      type="password" 
                      v-model="formData.currentPassword" 
                      placeholder="请输入当前密码"
                      show-password
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="新密码" prop="newPassword">
                    <el-input 
                      type="password" 
                      v-model="formData.newPassword" 
                      placeholder="请输入新密码"
                      show-password
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="8">
                  <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input 
                      type="password" 
                      v-model="formData.confirmPassword" 
                      placeholder="请确认新密码"
                      show-password
                    />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-col>
        </el-row>
        
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="6" animated />
        </div>
      </div>
    </div>
    
    <!-- 借阅统计信息 -->
    <div class="card-container">
      <div class="card-header">
        <h3>借阅统计</h3>
      </div>
      <div class="card-body">
        <el-row :gutter="24">
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-number">{{ borrowStats.totalBorrows || 0 }}</div>
              <div class="stat-label">总借阅次数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-number active">{{ borrowStats.activeBorrows || 0 }}</div>
              <div class="stat-label">当前借阅</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-number overdue">{{ borrowStats.overdueBorrows || 0 }}</div>
              <div class="stat-label">逾期记录</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-number returned">{{ borrowStats.returnedBorrows || 0 }}</div>
              <div class="stat-label">已归还</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/utils/api'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const editMode = ref(false)
const profileForm = ref()

const userInfo = ref({})
const borrowStats = ref({})

const formData = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const formRules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  newPassword: [
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    {
      validator: (rule, value, callback) => {
        if (formData.newPassword && value !== formData.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取个人信息
const fetchProfile = async () => {
  loading.value = true
  try {
    const response = await api.get('/reader/profile')
    userInfo.value = response.data
    
    // 初始化表单数据
    formData.username = userInfo.value.username
    formData.realName = userInfo.value.realName
    formData.email = userInfo.value.email || ''
    formData.phone = userInfo.value.phone || ''
  } catch (error) {
    console.error('获取个人信息失败:', error)
    ElMessage.error('获取个人信息失败')
  } finally {
    loading.value = false
  }
}

// 获取借阅统计
const fetchBorrowStats = async () => {
  try {
    // 获取所有借阅记录来统计
    const allResponse = await api.get('/reader/borrow-records', { params: { size: 1000 } })
    const records = allResponse.data.content || []
    
    borrowStats.value = {
      totalBorrows: records.length,
      activeBorrows: records.filter(r => r.status === 'BORROWED').length,
      overdueBorrows: records.filter(r => r.status === 'OVERDUE').length,
      returnedBorrows: records.filter(r => r.status === 'RETURNED').length
    }
  } catch (error) {
    console.error('获取借阅统计失败:', error)
  }
}

// 保存个人信息
const saveProfile = async () => {
  if (!profileForm.value) return
  
  try {
    await profileForm.value.validate()
    saving.value = true
    
    // 先更新基本信息
    const profileData = {
      realName: formData.realName,
      email: formData.email,
      phone: formData.phone
    }
    
    await api.put('/reader/profile', profileData)
    
    // 如果填写了新密码，单独更新密码
    if (formData.newPassword) {
      if (!formData.currentPassword) {
        ElMessage.error('修改密码时必须填写当前密码')
        saving.value = false
        return
      }
      
      const passwordData = {
        currentPassword: formData.currentPassword,
        newPassword: formData.newPassword
      }
      
      await api.put('/reader/password', passwordData)
    }
    
    ElMessage.success('个人信息更新成功')
    editMode.value = false
    
    // 清空密码字段
    formData.currentPassword = ''
    formData.newPassword = ''
    formData.confirmPassword = ''
    
    // 重新获取信息
    await fetchProfile()
    
  } catch (error) {
    console.error('更新个人信息失败:', error)
    ElMessage.error(error.response?.data?.message || '更新失败')
  } finally {
    saving.value = false
  }
}

// 取消编辑
const cancelEdit = () => {
  editMode.value = false
  // 重置表单数据
  formData.realName = userInfo.value.realName
  formData.email = userInfo.value.email || ''
  formData.phone = userInfo.value.phone || ''
  formData.currentPassword = ''
  formData.newPassword = ''
  formData.confirmPassword = ''
  
  // 清除验证
  if (profileForm.value) {
    profileForm.value.clearValidate()
  }
}

// 工具函数
const getRoleText = (role) => {
  const roleMap = {
    'ADMIN': '管理员',
    'READER': '读者'
  }
  return roleMap[role] || role
}

const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 初始化
onMounted(() => {
  fetchProfile()
  fetchBorrowStats()
})
</script>

<style scoped>
.profile-avatar {
  text-align: center;
  padding: 20px;
}

.user-avatar {
  margin-bottom: 16px;
  background: #f5f7fa;
  color: #909399;
}

.avatar-info h3 {
  margin: 8px 0;
  color: #303133;
}

.profile-form {
  padding: 20px 0;
}

.info-text {
  color: #606266;
}

.loading-container {
  padding: 40px;
}

.stat-item {
  text-align: center;
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fff;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-number.active {
  color: #67c23a;
}

.stat-number.overdue {
  color: #f56c6c;
}

.stat-number.returned {
  color: #909399;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}
</style> 