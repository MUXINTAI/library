<template>
  <div class="page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">图书浏览</h1>
      <p class="page-subtitle">浏览和借阅图书馆藏书</p>
    </div>
    
    <!-- 搜索和筛选栏 -->
    <div class="search-container">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索图书名称、作者或ISBN"
            size="large"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
            <template #append>
              <el-button type="primary" @click="handleSearch">
                搜索
              </el-button>
            </template>
          </el-input>
        </el-col>
        <el-col :span="6">
          <el-select 
            v-model="categoryFilter" 
            placeholder="选择分类"
            size="large"
            clearable
            @change="handleCategoryChange"
          >
            <el-option label="全部分类" value="" />
            <el-option 
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select 
            v-model="sortBy" 
            placeholder="排序方式"
            size="large"
            @change="handleSortChange"
          >
            <el-option label="最新上架" value="createdAt_desc" />
            <el-option label="最早上架" value="createdAt_asc" />
            <el-option label="书名 A-Z" value="title_asc" />
            <el-option label="书名 Z-A" value="title_desc" />
            <el-option label="作者 A-Z" value="author_asc" />
            <el-option label="作者 Z-A" value="author_desc" />
          </el-select>
        </el-col>
      </el-row>
    </div>
    
    <!-- 图书列表 -->
    <div class="card-container">
      <div class="card-header">
        <h3>图书列表</h3>
        <div class="view-controls">
          <el-radio-group v-model="viewMode" @change="handleViewModeChange">
            <el-radio-button label="grid">
              <el-icon><Grid /></el-icon>
              卡片
            </el-radio-button>
            <el-radio-button label="list">
              <el-icon><List /></el-icon>
              列表
            </el-radio-button>
          </el-radio-group>
        </div>
      </div>
      
      <div class="card-body">
        <!-- 卡片视图 -->
        <div v-if="viewMode === 'grid'" class="books-grid" v-loading="loading">
          <div 
            v-for="book in books" 
            :key="book.id" 
            class="book-card"
            @click="viewBookDetail(book)"
          >
            <div class="book-cover">
              <el-icon><Reading /></el-icon>
            </div>
            <div class="book-info">
              <h4 class="book-title" :title="book.title">{{ book.title }}</h4>
              <p class="book-author">{{ book.author }}</p>
              <p class="book-category">{{ (book.category && book.category.name) || '-' }}</p>
              <div class="book-stats">
                <span class="available-count">
                  可借：{{ book.availableQuantity || 0 }}/{{ book.totalQuantity || 0 }}
                </span>
              </div>
            </div>
            <div class="book-actions">
              <el-button 
                type="primary" 
                size="small"
                :disabled="!book.availableQuantity || book.availableQuantity <= 0"
                @click.stop="borrowBook(book)"
              >
                {{ book.availableQuantity > 0 ? '借阅' : '无库存' }}
              </el-button>
            </div>
          </div>
          
          <!-- 空状态 -->
          <div v-if="!loading && books.length === 0" class="empty-state">
            <el-empty description="没有找到相关图书">
              <el-button type="primary" @click="resetSearch">重置搜索</el-button>
            </el-empty>
          </div>
        </div>
        
        <!-- 列表视图 -->
        <div v-if="viewMode === 'list'" v-loading="loading">
          <el-table 
            :data="books"
            style="width: 100%"
            row-key="id"
            @row-click="viewBookDetail"
          >
            <el-table-column label="图书信息" min-width="300">
              <template #default="{ row }">
                <div class="book-info-row">
                  <div class="book-cover-small">
                    <el-icon><Reading /></el-icon>
                  </div>
                  <div class="book-details">
                    <div class="book-title-row">{{ row.title }}</div>
                    <div class="book-meta">{{ row.author }} · {{ row.isbn }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column label="分类" width="120">
              <template #default="{ row }">
                {{ (row.category && row.category.name) || '-' }}
              </template>
            </el-table-column>
            
            <el-table-column label="出版社" width="150" show-overflow-tooltip>
              <template #default="{ row }">
                {{ row.publisher || '-' }}
              </template>
            </el-table-column>
            
            <el-table-column label="库存" width="100" align="center">
              <template #default="{ row }">
                <span :class="{ 'out-of-stock': !row.availableQuantity || row.availableQuantity <= 0 }">
                  {{ row.availableQuantity || 0 }}/{{ row.totalQuantity || 0 }}
                </span>
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="120" align="center" fixed="right">
              <template #default="{ row }">
                <el-button 
                  type="primary" 
                  size="small"
                  :disabled="!row.availableQuantity || row.availableQuantity <= 0"
                  @click.stop="borrowBook(row)"
                >
                  {{ row.availableQuantity > 0 ? '借阅' : '无库存' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 空状态 -->
          <div v-if="!loading && books.length === 0" class="empty-container">
            <el-empty description="没有找到相关图书">
              <el-button type="primary" @click="resetSearch">重置搜索</el-button>
            </el-empty>
          </div>
        </div>
        
        <!-- 分页 -->
        <div class="pagination-container" v-if="total > 0">
          <el-pagination
            :current-page="currentPage"
            :page-size="pageSize"
            :page-sizes="[12, 24, 48, 96]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
    
    <!-- 图书详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="图书详情" 
      width="700px"
      :destroy-on-close="true"
    >
      <div v-if="selectedBook" class="book-detail">
        <el-row :gutter="24">
          <el-col :span="8">
            <div class="detail-cover">
              <el-icon><Reading /></el-icon>
            </div>
          </el-col>
          <el-col :span="16">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="书名">
                {{ selectedBook.title }}
              </el-descriptions-item>
              <el-descriptions-item label="作者">
                {{ selectedBook.author }}
              </el-descriptions-item>
              <el-descriptions-item label="ISBN">
                {{ selectedBook.isbn }}
              </el-descriptions-item>
              <el-descriptions-item label="分类">
                {{ (selectedBook.category && selectedBook.category.name) || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="出版社">
                {{ selectedBook.publisher || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="库存状态">
                <span :class="{ 'out-of-stock': !selectedBook.availableQuantity || selectedBook.availableQuantity <= 0 }">
                  可借：{{ selectedBook.availableQuantity || 0 }} 本 / 总计：{{ selectedBook.totalQuantity || 0 }} 本
                </span>
              </el-descriptions-item>
            </el-descriptions>
            
            <div v-if="selectedBook.description" class="book-description">
              <h4>图书简介</h4>
              <p>{{ selectedBook.description }}</p>
            </div>
          </el-col>
        </el-row>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
          <el-button 
            type="primary"
                         :disabled="!selectedBook || !selectedBook.availableQuantity || selectedBook.availableQuantity <= 0"
            @click="borrowBook(selectedBook)"
          >
                         {{ selectedBook && selectedBook.availableQuantity > 0 ? '借阅图书' : '无库存' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 借阅确认对话框 -->
    <el-dialog 
      v-model="borrowDialogVisible" 
      title="借阅确认" 
      width="500px"
      :destroy-on-close="true"
    >
      <div v-if="borrowingBook">
        <p>确定要借阅《{{ borrowingBook.title }}》吗？</p>
        <el-form :model="borrowForm" label-width="80px">
          <el-form-item label="备注">
            <el-input 
              v-model="borrowForm.remarks" 
              type="textarea" 
              placeholder="可选：添加借阅备注"
              :rows="3"
            />
          </el-form-item>
        </el-form>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="borrowDialogVisible = false">取消</el-button>
          <el-button 
            type="primary" 
            :loading="borrowing"
            @click="confirmBorrow"
          >
            确认借阅
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/utils/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const borrowing = ref(false)
const books = ref([])
const categories = ref([])
const selectedBook = ref(null)
const borrowingBook = ref(null)

// 对话框控制
const detailDialogVisible = ref(false)
const borrowDialogVisible = ref(false)

// 搜索和筛选
const searchKeyword = ref('')
const categoryFilter = ref('')
const sortBy = ref('createdAt_desc')
const viewMode = ref('grid') // 'grid' | 'list'

// 分页
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

// 借阅表单
const borrowForm = reactive({
  remarks: ''
})

// 查询参数
const queryParams = computed(() => {
  const [sortField, sortDir] = sortBy.value.split('_')
  return {
    page: currentPage.value - 1,
    size: pageSize.value,
    keyword: searchKeyword.value,
    categoryId: categoryFilter.value,
    sortBy: sortField,
    sortDir: sortDir
  }
})

// 获取图书列表
const fetchBooks = async () => {
  loading.value = true
  try {
    const response = await api.get('/reader/books', { params: queryParams.value })
    
    books.value = response.data.content || []
    total.value = response.data.totalElements || 0
    
  } catch (error) {
    console.error('获取图书列表失败:', error)
    ElMessage.error('获取图书列表失败')
  } finally {
    loading.value = false
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await api.get('/reader/categories')
    categories.value = response.data || []
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  fetchBooks()
}

// 分类筛选
const handleCategoryChange = () => {
  currentPage.value = 1
  fetchBooks()
}

// 排序变更
const handleSortChange = () => {
  currentPage.value = 1
  fetchBooks()
}

// 视图模式切换
const handleViewModeChange = () => {
  // 视图切换时可能需要调整分页大小
  if (viewMode.value === 'grid') {
    pageSize.value = 12
  } else {
    pageSize.value = 10
  }
  currentPage.value = 1
  fetchBooks()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  fetchBooks()
}

const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchBooks()
}

// 重置搜索
const resetSearch = () => {
  searchKeyword.value = ''
  categoryFilter.value = ''
  sortBy.value = 'createdAt_desc'
  currentPage.value = 1
  fetchBooks()
}

// 查看图书详情
const viewBookDetail = (book) => {
  selectedBook.value = book
  detailDialogVisible.value = true
}

// 借阅图书
const borrowBook = (book) => {
  if (!book.availableQuantity || book.availableQuantity <= 0) {
    ElMessage.warning('该图书暂无库存')
    return
  }
  
  borrowingBook.value = book
  borrowForm.remarks = ''
  borrowDialogVisible.value = true
  detailDialogVisible.value = false
}

// 确认借阅
const confirmBorrow = async () => {
  if (!borrowingBook.value) return
  
  borrowing.value = true
  try {
    await api.post('/reader/borrow-requests', null, {
      params: {
        bookId: borrowingBook.value.id,
        remarks: borrowForm.remarks || undefined
      }
    })
    
    ElMessage.success('借阅申请提交成功，请等待管理员审核')
    borrowDialogVisible.value = false
    
    // 刷新图书列表以更新库存
    fetchBooks()
    
  } catch (error) {
    console.error('提交借阅申请失败:', error)
    ElMessage.error(error.response?.data?.message || '提交借阅申请失败')
  } finally {
    borrowing.value = false
  }
}

// 初始化
onMounted(() => {
  fetchBooks()
  fetchCategories()
})
</script>

<style scoped>
.search-container {
  margin-bottom: 24px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.view-controls {
  display: flex;
  align-items: center;
  gap: 12px;
}

.books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  padding: 4px;
}

.book-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
}

.book-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.book-cover {
  width: 100%;
  height: 120px;
  background: #f5f7fa;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 12px;
  font-size: 48px;
  color: #c0c4cc;
}

.book-info {
  margin-bottom: 12px;
}

.book-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 8px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.book-author {
  color: #606266;
  font-size: 14px;
  margin: 0 0 4px 0;
}

.book-category {
  color: #909399;
  font-size: 12px;
  margin: 0 0 8px 0;
}

.book-stats {
  font-size: 13px;
}

.available-count {
  color: #67c23a;
  font-weight: bold;
}

.book-actions {
  text-align: center;
}

.book-info-row {
  display: flex;
  align-items: center;
}

.book-cover-small {
  width: 40px;
  height: 50px;
  background: #f5f7fa;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  color: #c0c4cc;
  font-size: 20px;
}

.book-details {
  flex: 1;
}

.book-title-row {
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.book-meta {
  color: #909399;
  font-size: 13px;
}

.out-of-stock {
  color: #f56c6c;
}

.detail-cover {
  width: 100%;
  height: 200px;
  background: #f5f7fa;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 80px;
  color: #c0c4cc;
}

.book-description {
  margin-top: 16px;
}

.book-description h4 {
  margin: 0 0 8px 0;
  color: #303133;
}

.book-description p {
  color: #606266;
  line-height: 1.6;
  margin: 0;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

.empty-state,
.empty-container {
  padding: 60px 20px;
  text-align: center;
}

.dialog-footer {
  text-align: right;
}
</style> 