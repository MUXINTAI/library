<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">借阅管理</h1>
      <p class="page-subtitle">管理借阅申请和记录</p>
    </div>
    
    <!-- 统计卡片 -->
    <div class="stats-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.pendingCount || 0 }}</div>
              <div class="stat-label">待审核</div>
            </div>
            <el-icon class="stat-icon pending"><Clock /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.borrowedCount || 0 }}</div>
              <div class="stat-label">借阅中</div>
            </div>
            <el-icon class="stat-icon borrowed"><Reading /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.overdueCount || 0 }}</div>
              <div class="stat-label">已逾期</div>
            </div>
            <el-icon class="stat-icon overdue"><Warning /></el-icon>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-content">
              <div class="stat-number">{{ statistics.totalCount || 0 }}</div>
              <div class="stat-label">总记录</div>
            </div>
            <el-icon class="stat-icon total"><DataAnalysis /></el-icon>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="card-container">
      <div class="card-body">
        <div class="search-section">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-input
                v-model="searchForm.keyword"
                placeholder="搜索用户名或图书标题"
                @keyup.enter="handleSearch"
                clearable
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </el-col>
            <el-col :span="4">
              <el-select
                v-model="searchForm.status"
                placeholder="选择状态"
                clearable
              >
                <el-option label="待审核" value="PENDING" />
                <el-option label="已批准" value="APPROVED" />
                <el-option label="借阅中" value="BORROWED" />
                <el-option label="已归还" value="RETURNED" />
                <el-option label="已逾期" value="OVERDUE" />
                <el-option label="已拒绝" value="REJECTED" />
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-date-picker
                v-model="searchForm.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                clearable
              />
            </el-col>
            <el-col :span="8">
              <el-button type="primary" @click="handleSearch" :icon="Search">
                搜索
              </el-button>
              <el-button @click="resetSearch" :icon="Refresh">
                重置
              </el-button>
              <el-button type="warning" @click="fetchOverdueRecords" :icon="Warning">
                查看逾期
              </el-button>
            </el-col>
          </el-row>
        </div>
      </div>
    </div>

    <!-- 借阅记录列表 -->
    <div class="card-container">
      <div class="card-body">
        <el-table
          v-loading="loading"
          :data="borrowRecords"
          stripe
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="user.username" label="用户" width="120" />
          <el-table-column prop="user.realName" label="真实姓名" width="120" />
          <el-table-column prop="book.title" label="图书标题" min-width="200" />
          <el-table-column prop="book.author" label="作者" width="120" />
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="申请时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="borrowDate" label="借阅时间" width="180">
            <template #default="scope">
              {{ scope.row.borrowDate ? formatDate(scope.row.borrowDate) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="dueDate" label="应还时间" width="180">
            <template #default="scope">
              {{ scope.row.dueDate ? formatDate(scope.row.dueDate) : '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="returnDate" label="归还时间" width="180">
            <template #default="scope">
              {{ scope.row.returnDate ? formatDate(scope.row.returnDate) : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button
                v-if="scope.row.status === 'PENDING'"
                type="success"
                size="small"
                @click="showApproveDialog(scope.row)"
                :icon="Check"
              >
                批准
              </el-button>
              <el-button
                v-if="scope.row.status === 'PENDING'"
                type="danger"
                size="small"
                @click="showRejectDialog(scope.row)"
                :icon="Close"
              >
                拒绝
              </el-button>
              <el-button
                v-if="scope.row.status === 'BORROWED' || scope.row.status === 'OVERDUE'"
                type="primary"
                size="small"
                @click="showReturnDialog(scope.row)"
                :icon="Check"
              >
                归还
              </el-button>
              <el-button
                type="info"
                size="small"
                @click="showDetailDialog(scope.row)"
                :icon="View"
              >
                详情
              </el-button>
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

    <!-- 审核对话框 -->
    <el-dialog
      v-model="approveDialogVisible"
      title="审核借阅申请"
      width="500px"
    >
      <el-form label-width="80px">
        <el-form-item label="用户">
          {{ currentRecord && currentRecord.user && currentRecord.user.realName }} ({{ currentRecord && currentRecord.user && currentRecord.user.username }})
        </el-form-item>
        <el-form-item label="图书">
          {{ currentRecord && currentRecord.book && currentRecord.book.title }}
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="approveForm.remarks"
            type="textarea"
            :rows="3"
            placeholder="请输入审核备注（可选）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="approveDialogVisible = false">取消</el-button>
          <el-button type="success" @click="handleApprove" :loading="submitting">
            批准借阅
          </el-button>
          <el-button type="danger" @click="handleReject" :loading="submitting">
            拒绝申请
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 归还对话框 -->
    <el-dialog
      v-model="returnDialogVisible"
      title="归还图书"
      width="500px"
    >
      <el-form label-width="80px">
        <el-form-item label="用户">
          {{ currentRecord && currentRecord.user && currentRecord.user.realName }} ({{ currentRecord && currentRecord.user && currentRecord.user.username }})
        </el-form-item>
        <el-form-item label="图书">
          {{ currentRecord && currentRecord.book && currentRecord.book.title }}
        </el-form-item>
        <el-form-item label="借阅时间">
          {{ currentRecord && currentRecord.borrowDate ? formatDate(currentRecord.borrowDate) : '-' }}
        </el-form-item>
        <el-form-item label="应还时间">
          {{ currentRecord && currentRecord.dueDate ? formatDate(currentRecord.dueDate) : '-' }}
        </el-form-item>
        <el-form-item label="归还备注">
          <el-input
            v-model="returnForm.remarks"
            type="textarea"
            :rows="3"
            placeholder="请输入归还备注（可选）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="returnDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleReturn" :loading="submitting">
            确认归还
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Refresh, Warning, Check, Close, View, Clock, 
  Reading, DataAnalysis 
} from '@element-plus/icons-vue'
import api from '@/utils/api'
import dayjs from 'dayjs'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const borrowRecords = ref([])
const statistics = ref({})

// 对话框状态
const approveDialogVisible = ref(false)
const returnDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const currentRecord = ref(null)

// 搜索表单
const searchForm = reactive({
  keyword: '',
  status: '',
  dateRange: null
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 审核表单
const approveForm = reactive({
  remarks: ''
})

// 归还表单
const returnForm = reactive({
  remarks: ''
})

// 获取借阅记录列表
const fetchBorrowRecords = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      ...searchForm
    }
    
    // 处理日期范围
    if (searchForm.dateRange && searchForm.dateRange.length === 2) {
      params.startDate = searchForm.dateRange[0]
      params.endDate = searchForm.dateRange[1]
    }
    
    const response = await api.get('/admin/borrow-records', { params })
    borrowRecords.value = response.data.content
    pagination.total = response.data.totalElements
  } catch (error) {
    ElMessage.error('获取借阅记录失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 获取统计信息
const fetchStatistics = async () => {
  try {
    const response = await api.get('/admin/statistics')
    statistics.value = response.data
  } catch (error) {
    console.error('获取统计信息失败：', error)
  }
}

// 获取逾期记录
const fetchOverdueRecords = async () => {
  try {
    const response = await api.get('/admin/borrow-records/overdue')
    borrowRecords.value = response.data
    pagination.total = response.data.length
    ElMessage.warning(`发现 ${response.data.length} 条逾期记录`)
  } catch (error) {
    ElMessage.error('获取逾期记录失败：' + error.message)
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchBorrowRecords()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    status: '',
    dateRange: null
  })
  pagination.page = 1
  fetchBorrowRecords()
}

// 分页处理
const handleSizeChange = (val) => {
  pagination.size = val
  pagination.page = 1
  fetchBorrowRecords()
}

const handleCurrentChange = (val) => {
  pagination.page = val
  fetchBorrowRecords()
}

// 显示审核对话框
const showApproveDialog = (record) => {
  currentRecord.value = record
  approveForm.remarks = ''
  approveDialogVisible.value = true
}

const showRejectDialog = (record) => {
  currentRecord.value = record
  approveForm.remarks = ''
  approveDialogVisible.value = true
}

// 显示归还对话框
const showReturnDialog = (record) => {
  currentRecord.value = record
  returnForm.remarks = ''
  returnDialogVisible.value = true
}

// 显示详情对话框
const showDetailDialog = (record) => {
  currentRecord.value = record
  detailDialogVisible.value = true
}

// 批准借阅
const handleApprove = async () => {
  try {
    submitting.value = true
    await api.put(`/admin/borrow-records/${currentRecord.value.id}/approve`, {
      remarks: approveForm.remarks
    })
    ElMessage.success('借阅申请已批准')
    approveDialogVisible.value = false
    fetchBorrowRecords()
    fetchStatistics()
  } catch (error) {
    ElMessage.error('批准失败：' + error.message)
  } finally {
    submitting.value = false
  }
}

// 拒绝申请
const handleReject = async () => {
  try {
    submitting.value = true
    await api.put(`/admin/borrow-records/${currentRecord.value.id}/reject`, {
      remarks: approveForm.remarks
    })
    ElMessage.success('借阅申请已拒绝')
    approveDialogVisible.value = false
    fetchBorrowRecords()
    fetchStatistics()
  } catch (error) {
    ElMessage.error('拒绝失败：' + error.message)
  } finally {
    submitting.value = false
  }
}

// 归还图书
const handleReturn = async () => {
  try {
    submitting.value = true
    await api.put(`/admin/borrow-records/${currentRecord.value.id}/return`, {
      remarks: returnForm.remarks
    })
    ElMessage.success('图书归还成功')
    returnDialogVisible.value = false
    fetchBorrowRecords()
    fetchStatistics()
  } catch (error) {
    ElMessage.error('归还失败：' + error.message)
  } finally {
    submitting.value = false
  }
}

// 状态显示
const getStatusType = (status) => {
  const statusMap = {
    'PENDING': '',
    'APPROVED': 'success',
    'BORROWED': 'primary',
    'RETURNED': 'success',
    'OVERDUE': 'danger',
    'REJECTED': 'danger'
  }
  return statusMap[status] || ''
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审核',
    'APPROVED': '已批准',
    'BORROWED': '借阅中',
    'RETURNED': '已归还',
    'OVERDUE': '已逾期',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || status
}

// 日期格式化
const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm:ss') : '-'
}

// 组件挂载时获取数据
onMounted(() => {
  fetchBorrowRecords()
  fetchStatistics()
})
</script>

<style scoped>
.stats-container {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-content {
  padding: 10px;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 5px;
}

.stat-icon {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 40px;
  opacity: 0.3;
}

.stat-icon.pending {
  color: #e6a23c;
}

.stat-icon.borrowed {
  color: #409eff;
}

.stat-icon.overdue {
  color: #f56c6c;
}

.stat-icon.total {
  color: #67c23a;
}

.search-section {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 