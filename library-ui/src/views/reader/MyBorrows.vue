<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">我的借阅</h1>
      <p class="page-subtitle">查看和管理我的借阅记录</p>
    </div>
    
    <!-- 快速统计 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon active">
          <el-icon><DocumentCopy /></el-icon>
        </div>
        <div class="stat-info">
          <h3>{{ statistics.activeBorrows || 0 }}</h3>
          <p>当前借阅</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon pending">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <h3>{{ statistics.pendingBorrows || 0 }}</h3>
          <p>待审核</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon overdue">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-info">
          <h3>{{ statistics.overdueBorrows || 0 }}</h3>
          <p>逾期记录</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon total">
          <el-icon><Reading /></el-icon>
        </div>
        <div class="stat-info">
          <h3>{{ statistics.totalBorrows || 0 }}</h3>
          <p>总借阅次数</p>
        </div>
      </div>
    </div>
    
    <!-- 主要内容 -->
    <div class="card-container">
      <div class="card-header">
        <h3>借阅记录</h3>
        <div class="header-actions">
          <el-input 
            v-model="searchKeyword" 
            placeholder="搜索图书名称或作者"
            class="search-input"
            clearable
            @input="handleSearch"
          />
          <el-select 
            v-model="statusFilter" 
            placeholder="状态筛选"
            class="status-filter"
            @change="handleStatusChange"
          >
            <el-option label="全部状态" value="" />
            <el-option label="待审核" value="PENDING" />
            <el-option label="已借出" value="BORROWED" />
            <el-option label="已逾期" value="OVERDUE" />
            <el-option label="已归还" value="RETURNED" />
          </el-select>
          <el-button type="primary" @click="$router.push('/reader/books')">
            <el-icon><Plus /></el-icon>
            借阅图书
          </el-button>
        </div>
      </div>
      
      <div class="card-body">
        <el-table 
          :data="borrowRecords" 
          v-loading="loading"
          style="width: 100%"
          row-key="id"
        >
          <el-table-column label="图书信息" min-width="200">
            <template #default="{ row }">
              <div class="book-info">
                <div class="book-cover">
                  <el-icon><Reading /></el-icon>
                </div>
                <div class="book-details">
                  <div class="book-title">{{ (row.book && row.book.title) || '-' }}</div>
                  <div class="book-author">{{ (row.book && row.book.author) || '-' }}</div>
                  <div class="book-isbn">ISBN: {{ (row.book && row.book.isbn) || '-' }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="申请时间" width="140" align="center">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          
          <el-table-column label="借阅时间" width="140" align="center">
            <template #default="{ row }">
              {{ formatDate(row.borrowDate) }}
            </template>
          </el-table-column>
          
          <el-table-column label="应还时间" width="140" align="center">
            <template #default="{ row }">
              <span :class="{ 'overdue-date': isOverdue(row.dueDate) && row.status === 'BORROWED' }">
                {{ formatDate(row.dueDate) }}
              </span>
            </template>
          </el-table-column>
          
          <el-table-column label="归还时间" width="140" align="center">
            <template #default="{ row }">
              {{ formatDate(row.returnDate) }}
            </template>
          </el-table-column>
          
          <el-table-column label="备注" min-width="120" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.remarks || '-' }}
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <el-button 
                type="primary" 
                size="small"
                @click="viewDetail(row)"
              >
                详情
              </el-button>
              <el-button 
                v-if="row.status === 'PENDING'"
                type="danger" 
                size="small"
                @click="cancelBorrow(row)"
              >
                取消
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 分页 -->
        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            :current-page="currentPage"
            :page-size="pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
        
        <!-- 空状态 -->
        <div v-if="!loading && borrowRecords.length === 0" class="empty-container">
          <el-empty description="暂无借阅记录">
            <el-button type="primary" @click="$router.push('/reader/books')">
              去借阅图书
            </el-button>
          </el-empty>
        </div>
      </div>
    </div>
    
    <!-- 详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="借阅详情" 
      width="600px"
      :destroy-on-close="true"
    >
      <div v-if="selectedRecord" class="detail-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="图书名称">
            {{ (selectedRecord.book && selectedRecord.book.title) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="作者">
            {{ (selectedRecord.book && selectedRecord.book.author) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="ISBN">
            {{ (selectedRecord.book && selectedRecord.book.isbn) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="分类">
            {{ (selectedRecord.book && selectedRecord.book.category && selectedRecord.book.category.name) || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(selectedRecord.status)">
              {{ getStatusText(selectedRecord.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">
            {{ formatDate(selectedRecord.createdAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="借阅时间">
            {{ formatDate(selectedRecord.borrowDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="应还时间">
            <span :class="{ 'overdue-date': isOverdue(selectedRecord.dueDate) && selectedRecord.status === 'BORROWED' }">
              {{ formatDate(selectedRecord.dueDate) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="归还时间">
            {{ formatDate(selectedRecord.returnDate) }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">
            {{ selectedRecord.remarks || '无' }}
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 逾期提醒 -->
        <el-alert
          v-if="isOverdue(selectedRecord.dueDate) && selectedRecord.status === 'BORROWED'"
          title="逾期提醒"
          :description="`该图书已逾期 ${getOverdueDays(selectedRecord.dueDate)} 天，请尽快归还！`"
          type="error"
          :closable="false"
          style="margin-top: 16px"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/utils/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const borrowRecords = ref([])
const statistics = ref({})
const detailDialogVisible = ref(false)
const selectedRecord = ref(null)

// 搜索和筛选
const searchKeyword = ref('')
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 查询参数
const queryParams = reactive({
  page: 0,
  size: 10,
  sortBy: 'createdAt',
  sortDir: 'desc',
  keyword: '',
  status: ''
})

// 获取借阅记录
const fetchBorrowRecords = async () => {
  loading.value = true
  try {
    queryParams.page = currentPage.value - 1
    queryParams.size = pageSize.value
    queryParams.keyword = searchKeyword.value
    queryParams.status = statusFilter.value
    
    const response = await api.get('/reader/borrow-records', { params: queryParams })
    
    borrowRecords.value = response.data.content || []
    total.value = response.data.totalElements || 0
    
  } catch (error) {
    console.error('获取借阅记录失败:', error)
    ElMessage.error('获取借阅记录失败')
  } finally {
    loading.value = false
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    // 获取所有记录进行统计
    const allResponse = await api.get('/reader/borrow-records', { 
      params: { page: 0, size: 1000 } 
    })
    const allRecords = allResponse.data.content || []
    
    statistics.value = {
      totalBorrows: allRecords.length,
      activeBorrows: allRecords.filter(r => r.status === 'BORROWED').length,
      pendingBorrows: allRecords.filter(r => r.status === 'PENDING').length,
      overdueBorrows: allRecords.filter(r => r.status === 'OVERDUE').length
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchBorrowRecords()
}

// 状态筛选
const handleStatusChange = () => {
  currentPage.value = 1
  fetchBorrowRecords()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchBorrowRecords()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchBorrowRecords()
}

// 查看详情
const viewDetail = (record) => {
  selectedRecord.value = record
  detailDialogVisible.value = true
}

// 取消借阅申请
const cancelBorrow = async (record) => {
  try {
    await ElMessageBox.confirm(
      `确定要取消《${record.book?.title}》的借阅申请吗？`,
      '确认取消',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用后端API取消借阅申请
    await api.delete(`/reader/borrow-requests/${record.id}`)
    
    ElMessage.success('借阅申请已取消')
    
    // 刷新数据
    fetchBorrowRecords()
    fetchStatistics()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消借阅失败:', error)
      ElMessage.error(error.response?.data?.message || '取消借阅失败')
    }
  }
}

// 工具函数
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

const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('MM-DD HH:mm')
}

const isOverdue = (dueDate) => {
  if (!dueDate) return false
  return dayjs().isAfter(dayjs(dueDate))
}

const getOverdueDays = (dueDate) => {
  if (!dueDate) return 0
  return Math.max(0, dayjs().diff(dayjs(dueDate), 'day'))
}

// 初始化
onMounted(() => {
  fetchBorrowRecords()
  fetchStatistics()
})
</script>

<style scoped>
.stats-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: #fff;
}

.stat-icon.active {
  background: #67c23a;
}

.stat-icon.pending {
  background: #e6a23c;
}

.stat-icon.overdue {
  background: #f56c6c;
}

.stat-icon.total {
  background: #409eff;
}

.stat-info h3 {
  margin: 0 0 4px 0;
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-info p {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.search-input {
  width: 250px;
}

.status-filter {
  width: 120px;
}

.book-info {
  display: flex;
  align-items: center;
}

.book-cover {
  width: 40px;
  height: 50px;
  background: #f5f7fa;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  color: #909399;
}

.book-details {
  flex: 1;
}

.book-title {
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.book-author {
  color: #606266;
  font-size: 13px;
  margin-bottom: 2px;
}

.book-isbn {
  color: #909399;
  font-size: 12px;
}

.overdue-date {
  color: #f56c6c;
  font-weight: bold;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.empty-container {
  padding: 40px;
  text-align: center;
}

.detail-content {
  padding: 8px 0;
}
</style> 