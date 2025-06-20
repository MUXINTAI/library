<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
      <p class="page-subtitle">管理系统中的所有用户</p>
    </div>
    
    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :model="searchForm" class="search-form" inline>
        <el-form-item label="用户名">
          <el-input
            v-model="searchForm.username"
            placeholder="请输入用户名"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select
            v-model="searchForm.role"
            placeholder="请选择角色"
            clearable
            style="width: 150px"
          >
            <el-option label="管理员" value="ADMIN" />
            <el-option label="读者" value="READER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select
            v-model="searchForm.enabled"
            placeholder="请选择状态"
            clearable
            style="width: 150px"
          >
            <el-option label="启用" :value="true" />
            <el-option label="禁用" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <div class="button-group">
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetSearch">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
            <el-button type="success" @click="showAddUserDialog = true">
              <el-icon><Plus /></el-icon>
              添加管理员
            </el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>
    
    <!-- 用户列表 -->
    <div class="card-container">
      <div class="card-body">
        <el-table
          :data="userList"
          style="width: 100%"
          v-loading="loading"
          row-key="id"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="用户名" width="120" />
          <el-table-column prop="realName" label="真实姓名" width="120" />
          <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
          <el-table-column prop="phone" label="手机号" width="120" />
          <el-table-column prop="role" label="角色" width="100">
            <template #default="{ row }">
              <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'primary'">
                {{ row.role === 'ADMIN' ? '管理员' : '读者' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="enabled" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'danger'">
                {{ row.enabled ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <div class="button-group">
                <el-button
                  type="primary"
                  size="small"
                  @click="editUser(row)"
                >
                  编辑
                </el-button>
                <el-button
                  :type="row.enabled ? 'warning' : 'success'"
                  size="small"
                  @click="toggleUserStatus(row)"
                >
                  {{ row.enabled ? '禁用' : '启用' }}
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="deleteUser(row)"
                  :disabled="row.role === 'ADMIN'"
                >
                  删除
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            :current-page="pagination.page"
            :page-size="pagination.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
    
    <!-- 添加管理员对话框 -->
    <el-dialog
      v-model="showAddUserDialog"
      title="添加管理员"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="addUserFormRef"
        :model="addUserForm"
        :rules="userRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="addUserForm.username"
            placeholder="请输入用户名（3-20个字符）"
            clearable
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="addUserForm.password"
            type="password"
            placeholder="请输入密码（至少6个字符）"
            show-password
            clearable
          />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input
            v-model="addUserForm.realName"
            placeholder="请输入真实姓名"
            clearable
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="addUserForm.email"
            placeholder="请输入邮箱地址"
            clearable
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="addUserForm.phone"
            placeholder="请输入手机号（可选）"
            clearable
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddUserDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAddUser">
          确定
        </el-button>
      </template>
    </el-dialog>
    
    <!-- 编辑用户对话框 -->
    <el-dialog
      v-model="showEditUserDialog"
      title="编辑用户"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="editUserFormRef"
        :model="editUserForm"
        :rules="editUserRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="editUserForm.username"
            placeholder="请输入用户名（3-20个字符）"
            clearable
          />
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input
            v-model="editUserForm.password"
            type="password"
            placeholder="留空则不修改密码"
            show-password
            clearable
          />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input
            v-model="editUserForm.realName"
            placeholder="请输入真实姓名"
            clearable
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="editUserForm.email"
            placeholder="请输入邮箱地址"
            clearable
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="editUserForm.phone"
            placeholder="请输入手机号（可选）"
            clearable
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showEditUserDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleEditUser">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { userApi, authApi } from '@/utils/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const userList = ref([])
const showAddUserDialog = ref(false)
const showEditUserDialog = ref(false)
const currentEditUser = ref(null)

// 表单引用
const addUserFormRef = ref()
const editUserFormRef = ref()

// 搜索表单
const searchForm = reactive({
  username: '',
  role: '',
  enabled: null
})

// 分页数据
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 添加用户表单
const addUserForm = reactive({
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: ''
})

// 编辑用户表单
const editUserForm = reactive({
  username: '',
  password: '',
  realName: '',
  email: '',
  phone: ''
})

// 表单验证规则
const userRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少 6 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
})

const editUserRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { min: 6, message: '密码长度至少 6 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
})

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      ...searchForm
    }
    // 过滤空值
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null) {
        delete params[key]
      }
    })
    
    const response = await userApi.getUsers(params)
    userList.value = response.data.content || []
    pagination.total = response.data.totalElements || 0
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchUsers()
}

// 重置搜索
const resetSearch = () => {
  Object.keys(searchForm).forEach(key => {
    searchForm[key] = key === 'enabled' ? null : ''
  })
  pagination.page = 1
  fetchUsers()
}

// 分页处理
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchUsers()
}

const handleCurrentChange = (page) => {
  pagination.page = page
  fetchUsers()
}

// 添加管理员
const handleAddUser = async () => {
  try {
    await addUserFormRef.value.validate()
    submitting.value = true
    await authApi.registerAdmin(addUserForm)
    ElMessage.success('管理员添加成功')
    showAddUserDialog.value = false
    // 清空表单
    Object.keys(addUserForm).forEach(key => {
      addUserForm[key] = ''
    })
    fetchUsers()
  } catch (error) {
    if (error.fields) {
      return
    }
    console.error('添加管理员失败:', error)
  } finally {
    submitting.value = false
  }
}

// 编辑用户
const editUser = (user) => {
  currentEditUser.value = user
  Object.keys(editUserForm).forEach(key => {
    editUserForm[key] = user[key] || ''
  })
  editUserForm.password = '' // 密码字段清空
  showEditUserDialog.value = true
}

const handleEditUser = async () => {
  try {
    await editUserFormRef.value.validate()
    submitting.value = true
    
    const updateData = { ...editUserForm }
    // 如果密码为空，则删除密码字段
    if (!updateData.password) {
      delete updateData.password
    }
    
    await userApi.updateUser(currentEditUser.value.id, updateData)
    ElMessage.success('用户信息更新成功')
    showEditUserDialog.value = false
    fetchUsers()
  } catch (error) {
    if (error.fields) {
      return
    }
    console.error('更新用户失败:', error)
  } finally {
    submitting.value = false
  }
}

// 切换用户状态
const toggleUserStatus = async (user) => {
  try {
    const action = user.enabled ? '禁用' : '启用'
    await ElMessageBox.confirm(`确定要${action}用户 "${user.username}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await userApi.toggleUserStatus(user.id)
    ElMessage.success(`用户${action}成功`)
    fetchUsers()
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    console.error('切换用户状态失败:', error)
  }
}

// 删除用户
const deleteUser = async (user) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${user.username}" 吗？此操作不可恢复！`, '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'error'
    })
    
    await userApi.deleteUser(user.id)
    ElMessage.success('用户删除成功')
    fetchUsers()
  } catch (error) {
    if (error === 'cancel') {
      return
    }
    console.error('删除用户失败:', error)
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 初始化
onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.pagination-container {
  margin-top: 20px;
  text-align: right;
}

.button-group {
  display: flex;
  gap: 8px;
}

.button-group .el-button {
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .search-form {
    flex-direction: column;
  }
  
  .search-form .el-form-item {
    margin-bottom: 16px;
  }
  
  .button-group {
    flex-wrap: wrap;
  }
}
</style> 