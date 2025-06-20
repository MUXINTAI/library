<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">仪表盘</h1>
      <p class="page-subtitle">欢迎使用图书管理系统</p>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-grid" v-if="authStore.isAdmin">
      <div class="stats-card">
        <div class="stats-content">
          <div class="stats-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="stats-info">
            <h3>{{ statistics.totalUsers || 0 }}</h3>
            <p>总用户数</p>
          </div>
        </div>
      </div>
      
      <div class="stats-card">
        <div class="stats-content">
          <div class="stats-icon books">
            <el-icon><Reading /></el-icon>
          </div>
          <div class="stats-info">
            <h3>{{ statistics.totalBooks || 0 }}</h3>
            <p>图书总数</p>
          </div>
        </div>
      </div>
      
      <div class="stats-card">
        <div class="stats-content">
          <div class="stats-icon borrows">
            <el-icon><DocumentCopy /></el-icon>
          </div>
          <div class="stats-info">
            <h3>{{ statistics.totalBorrows || 0 }}</h3>
            <p>借阅总数</p>
          </div>
        </div>
      </div>
      
      <div class="stats-card">
        <div class="stats-content">
          <div class="stats-icon pending">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stats-info">
            <h3>{{ statistics.pendingBorrows || 0 }}</h3>
            <p>待审核</p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="dashboard-content">
      <!-- 管理员视图 -->
      <template v-if="authStore.isAdmin">
        <el-row :gutter="24">
          <!-- 待审核借阅 -->
          <el-col :span="12">
            <div class="card-container">
              <div class="card-header">
                <h3>待审核借阅申请</h3>
                <el-button type="text" @click="$router.push('/admin/borrows')">
                  查看全部
                </el-button>
              </div>
              <div class="card-body">
                <el-table
                  :data="pendingBorrows"
                  style="width: 100%"
                  max-height="300"
                  v-loading="loading"
                >
                  <el-table-column label="用户" width="100">
                    <template #default="{ row }">
                      {{ (row.user && row.user.realName) || (row.user && row.user.username) || '-' }}
                    </template>
                  </el-table-column>
                  <el-table-column label="图书" show-overflow-tooltip>
                    <template #default="{ row }">
                      {{ (row.book && row.book.title) || '-' }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="createdAt" label="申请时间" width="120">
                    <template #default="{ row }">
                      {{ formatDate(row.createdAt) }}
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="120">
                    <template #default="{ row }">
                      <el-button
                        type="primary"
                        size="small"
                        @click="approveBorrow(row.id)"
                      >
                        批准
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
                
                <div v-if="!loading && pendingBorrows.length === 0" class="empty-state">
                  <el-empty description="暂无待审核申请" />
                </div>
              </div>
            </div>
          </el-col>
          
          <!-- 逾期记录 -->
          <el-col :span="12">
            <div class="card-container">
              <div class="card-header">
                <h3>逾期记录</h3>
                <el-button type="text" @click="$router.push('/admin/borrows')">
                  查看全部
                </el-button>
              </div>
              <div class="card-body">
                <el-table
                  :data="overdueRecords"
                  style="width: 100%"
                  max-height="300"
                  v-loading="loading"
                >
                  <el-table-column label="用户" width="100">
                    <template #default="{ row }">
                      {{ (row.user && row.user.realName) || (row.user && row.user.username) || '-' }}
                    </template>
                  </el-table-column>
                  <el-table-column label="图书" show-overflow-tooltip>
                    <template #default="{ row }">
                      {{ (row.book && row.book.title) || '-' }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="dueDate" label="应还日期" width="120">
                    <template #default="{ row }">
                      {{ formatDate(row.dueDate) }}
                    </template>
                  </el-table-column>
                  <el-table-column label="逾期天数" width="100">
                    <template #default="{ row }">
                      <el-tag type="danger">
                        {{ getOverdueDays(row.dueDate) }}天
                      </el-tag>
                    </template>
                  </el-table-column>
                </el-table>
                
                <div v-if="!loading && overdueRecords.length === 0" class="empty-state">
                  <el-empty description="暂无逾期记录" />
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </template>
      
      <!-- 读者视图 -->
      <template v-if="authStore.isReader">
        <!-- 读者统计卡片 -->
        <div class="stats-grid">
          <div class="stats-card">
            <div class="stats-content">
              <div class="stats-icon active">
                <el-icon><DocumentCopy /></el-icon>
              </div>
              <div class="stats-info">
                <h3>{{ readerStats.activeBorrows || 0 }}</h3>
                <p>当前借阅</p>
              </div>
            </div>
          </div>
          
          <div class="stats-card">
            <div class="stats-content">
              <div class="stats-icon pending">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stats-info">
                <h3>{{ readerStats.pendingBorrows || 0 }}</h3>
                <p>待审核</p>
              </div>
            </div>
          </div>
          
          <div class="stats-card">
            <div class="stats-content">
              <div class="stats-icon overdue">
                <el-icon><Warning /></el-icon>
              </div>
              <div class="stats-info">
                <h3>{{ readerStats.overdueBorrows || 0 }}</h3>
                <p>逾期记录</p>
              </div>
            </div>
          </div>
          
          <div class="stats-card">
            <div class="stats-content">
              <div class="stats-icon total">
                <el-icon><Reading /></el-icon>
              </div>
              <div class="stats-info">
                <h3>{{ readerStats.totalBorrows || 0 }}</h3>
                <p>总借阅</p>
              </div>
            </div>
          </div>
        </div>
        
        <el-row :gutter="24">
          <!-- 我的当前借阅 -->
          <el-col :span="12">
            <div class="card-container">
              <div class="card-header">
                <h3>我的当前借阅</h3>
                <el-button type="text" @click="$router.push('/reader/borrows')">
                  查看全部
                </el-button>
              </div>
              <div class="card-body">
                <el-table
                  :data="myActiveBorrows"
                  style="width: 100%"
                  max-height="300"
                  v-loading="loading"
                >
                  <el-table-column label="图书" show-overflow-tooltip>
                    <template #default="{ row }">
                      {{ (row.book && row.book.title) || '-' }}
                    </template>
                  </el-table-column>
                  <el-table-column prop="status" label="状态" width="100">
                    <template #default="{ row }">
                      <el-tag :type="getStatusType(row.status)">
                        {{ getStatusText(row.status) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="dueDate" label="应还日期" width="120">
                    <template #default="{ row }">
                      {{ formatDate(row.dueDate) }}
                    </template>
                  </el-table-column>
                </el-table>
                
                <div v-if="!loading && myActiveBorrows.length === 0" class="empty-state">
                  <el-empty description="暂无借阅记录" />
                </div>
              </div>
            </div>
          </el-col>
          
          <!-- 推荐图书 -->
          <el-col :span="12">
            <div class="card-container">
              <div class="card-header">
                <h3>推荐图书</h3>
                <el-button type="text" @click="$router.push('/reader/books')">
                  浏览更多
                </el-button>
              </div>
              <div class="card-body">
                <div class="book-list">
                  <div
                    v-for="book in recommendedBooks"
                    :key="book.id"
                    class="book-item"
                    @click="viewBookDetail(book)"
                  >
                    <div class="book-cover">
                      <el-icon><Reading /></el-icon>
                    </div>
                    <div class="book-info">
                      <h4 class="book-title">{{ book.title }}</h4>
                      <p class="book-author">{{ book.author }}</p>
                      <p class="book-available">可借：{{ book.availableQuantity }}本</p>
                    </div>
                  </div>
                </div>
                
                <div v-if="!loading && recommendedBooks.length === 0" class="empty-state">
                  <el-empty description="暂无推荐图书" />
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/utils/api'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'

const authStore = useAuthStore()

// 响应式数据
const loading = ref(false)
const statistics = ref({})
const readerStats = ref({})
const pendingBorrows = ref([])
const overdueRecords = ref([])
const myActiveBorrows = ref([])
const recommendedBooks = ref([])

// 获取统计数据
const fetchStatistics = async () => {
  if (!authStore.isAdmin) return
  
  try {
    const response = await api.get('/admin/statistics')
    statistics.value = response.data
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 如果统计接口失败，设置默认值
    statistics.value = {
      totalUsers: 0,
      totalBooks: 0,
      totalBorrows: 0,
      pendingBorrows: 0
    }
  }
}

// 获取待审核借阅
const fetchPendingBorrows = async () => {
  if (!authStore.isAdmin) return
  
  try {
    const response = await api.get('/admin/borrow-records/pending', { 
      params: { page: 0, size: 5 }
    })
    pendingBorrows.value = response.data.content || []
  } catch (error) {
    console.error('获取待审核借阅失败:', error)
  }
}

// 获取逾期记录
const fetchOverdueRecords = async () => {
  if (!authStore.isAdmin) return
  
  try {
    const response = await api.get('/admin/borrow-records/overdue')
    overdueRecords.value = response.data.slice(0, 5) || []
  } catch (error) {
    console.error('获取逾期记录失败:', error)
  }
}

// 获取我的当前借阅
const fetchMyActiveBorrows = async () => {
  if (!authStore.isReader) return
  
  try {
    const response = await api.get('/reader/borrow-records', { 
      params: { page: 0, size: 5, status: 'BORROWED' }
    })
    myActiveBorrows.value = response.data.content || []
  } catch (error) {
    console.error('获取我的借阅失败:', error)
  }
}

// 获取推荐图书
const fetchRecommendedBooks = async () => {
  if (!authStore.isReader) return
  
  try {
    const response = await api.get('/reader/books', { 
      params: { page: 0, size: 6 }
    })
    recommendedBooks.value = response.data.content || []
  } catch (error) {
    console.error('获取推荐图书失败:', error)
  }
}

// 获取读者统计数据
const fetchReaderStats = async () => {
  if (!authStore.isReader) return
  
  try {
    // 获取所有借阅记录进行统计
    const allResponse = await api.get('/reader/borrow-records', { 
      params: { page: 0, size: 1000 } 
    })
    const allRecords = allResponse.data.content || []
    
    readerStats.value = {
      totalBorrows: allRecords.length,
      activeBorrows: allRecords.filter(r => r.status === 'BORROWED').length,
      pendingBorrows: allRecords.filter(r => r.status === 'PENDING').length,
      overdueBorrows: allRecords.filter(r => r.status === 'OVERDUE').length
    }
  } catch (error) {
    console.error('获取读者统计失败:', error)
  }
}

// 批准借阅
const approveBorrow = async (id) => {
  try {
    await api.put(`/admin/borrow-records/${id}/approve`, { remarks: '管理员批准' })
    ElMessage.success('借阅申请已批准')
    fetchPendingBorrows()
    fetchStatistics()
  } catch (error) {
    console.error('批准借阅失败:', error)
  }
}

// 查看图书详情
const viewBookDetail = (book) => {
  // 这里可以打开图书详情对话框或跳转到详情页
  ElMessage.info(`查看《${book.title}》详情`)
}

// 工具函数
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('MM-DD HH:mm')
}

const getOverdueDays = (dueDate) => {
  if (!dueDate) return 0
  return Math.max(0, dayjs().diff(dayjs(dueDate), 'day'))
}

const getStatusType = (status) => {
  const statusMap = {
    'PENDING': 'warning',
    'BORROWED': 'success',
    'OVERDUE': 'danger',
    'RETURNED': 'info'
  }
  return statusMap[status] || 'info'
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审核',
    'BORROWED': '已借出',
    'OVERDUE': '已逾期',
    'RETURNED': '已归还'
  }
  return statusMap[status] || status
}

// 初始化数据
const initData = async () => {
  loading.value = true
  try {
    if (authStore.isAdmin) {
      await Promise.all([
        fetchStatistics(),
        fetchPendingBorrows(),
        fetchOverdueRecords()
      ])
    } else if (authStore.isReader) {
      await Promise.all([
        fetchReaderStats(),
        fetchMyActiveBorrows(),
        fetchRecommendedBooks()
      ])
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  initData()
})
</script>

<style scoped>
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  margin-bottom: 32px;
}

.stats-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stats-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stats-content {
  display: flex;
  align-items: center;
}

.stats-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  background: linear-gradient(135deg, #409eff, #337ecc);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  margin-right: 16px;
}

.stats-icon.books {
  background: linear-gradient(135deg, #67c23a, #529b2e);
}

.stats-icon.borrows {
  background: linear-gradient(135deg, #e6a23c, #cf9236);
}

.stats-icon.pending {
  background: linear-gradient(135deg, #f56c6c, #f78989);
}

.stats-icon.active {
  background: linear-gradient(135deg, #67c23a, #529b2e);
}

.stats-icon.overdue {
  background: linear-gradient(135deg, #f56c6c, #f78989);
}

.stats-icon.total {
  background: linear-gradient(135deg, #409eff, #337ecc);
}

.stats-info h3 {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 4px 0;
}

.stats-info p {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.dashboard-content {
  margin-top: 24px;
}

.card-header {
  display: flex;
  justify-content: between;
  align-items: center;
}

.card-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.empty-state {
  padding: 40px 0;
}

.book-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.book-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 8px;
  background: #fafbfc;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.book-item:hover {
  background: #f0f2f5;
}

.book-cover {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  background: linear-gradient(135deg, #409eff, #337ecc);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  margin-right: 12px;
  flex-shrink: 0;
}

.book-info {
  flex: 1;
  min-width: 0;
}

.book-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 4px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  font-size: 12px;
  color: #606266;
  margin: 0 0 4px 0;
}

.book-available {
  font-size: 12px;
  color: #67c23a;
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  
  .stats-card {
    padding: 16px;
  }
  
  .stats-icon {
    width: 48px;
    height: 48px;
    font-size: 20px;
    margin-right: 12px;
  }
  
  .stats-info h3 {
    font-size: 24px;
  }
}
</style> 