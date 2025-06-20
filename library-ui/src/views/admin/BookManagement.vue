<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">图书管理</h1>
      <p class="page-subtitle">管理图书信息和库存</p>
    </div>
    
    <!-- 搜索和操作区域 -->
    <div class="card-container">
      <div class="card-body">
        <div class="search-section">
          <el-row :gutter="20">
            <el-col :span="6">
              <el-input
                v-model="searchForm.keyword"
                placeholder="搜索图书标题或作者"
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
                v-model="searchForm.categoryId"
                placeholder="选择分类"
                clearable
              >
                <el-option
                  v-for="category in categories"
                  :key="category.id"
                  :label="category.name"
                  :value="category.id"
                />
              </el-select>
            </el-col>
            <el-col :span="4">
              <el-select
                v-model="searchForm.status"
                placeholder="选择状态"
                clearable
              >
                <el-option label="可借阅" value="AVAILABLE" />
                <el-option label="已下架" value="UNAVAILABLE" />
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-button type="primary" @click="handleSearch" :icon="Search">
                搜索
              </el-button>
              <el-button @click="resetSearch" :icon="Refresh">
                重置
              </el-button>
              <el-button type="success" @click="showAddDialog" :icon="Plus">
                添加图书
              </el-button>
            </el-col>
          </el-row>
        </div>
      </div>
    </div>

    <!-- 图书列表 -->
    <div class="card-container">
      <div class="card-body">
        <el-table
          v-loading="loading"
          :data="books"
          stripe
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="图书标题" min-width="200" />
          <el-table-column prop="author" label="作者" width="120" />
          <el-table-column prop="isbn" label="ISBN" width="140" />
          <el-table-column label="分类" width="100">
            <template #default="{ row }">
              {{ (row.category && row.category.name) || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="publisher" label="出版社" width="120" />
          <el-table-column prop="totalQuantity" label="总数量" width="80" align="center" />
          <el-table-column prop="availableQuantity" label="可借数量" width="80" align="center" />
          <el-table-column prop="price" label="价格" width="80" align="center">
            <template #default="scope">
              ¥{{ scope.row.price }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'AVAILABLE' ? 'success' : 'danger'">
                {{ scope.row.status === 'AVAILABLE' ? '可借阅' : '已下架' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button
                type="primary"
                size="small"
                @click="showEditDialog(scope.row)"
                :icon="Edit"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                size="small"
                @click="handleDelete(scope.row)"
                :icon="Delete"
              >
                删除
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

    <!-- 添加/编辑图书对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑图书' : '添加图书'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="bookForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="图书标题" prop="title">
          <el-input v-model="bookForm.title" placeholder="请输入图书标题" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="bookForm.author" placeholder="请输入作者" />
        </el-form-item>
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="bookForm.isbn" placeholder="请输入ISBN" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select
            v-model="bookForm.categoryId"
            placeholder="请选择分类"
            style="width: 100%"
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="出版社" prop="publisher">
          <el-input v-model="bookForm.publisher" placeholder="请输入出版社" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="bookForm.price"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="总数量" prop="totalQuantity">
          <el-input-number
            v-model="bookForm.totalQuantity"
            :min="1"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="可借数量" prop="availableQuantity">
          <el-input-number
            v-model="bookForm.availableQuantity"
            :min="0"
            :max="bookForm.totalQuantity"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="bookForm.status">
            <el-radio value="AVAILABLE">可借阅</el-radio>
            <el-radio value="UNAVAILABLE">已下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="bookForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入图书描述"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '更新' : '添加' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import api from '@/utils/api'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const books = ref([])
const categories = ref([])
const formRef = ref()

// 搜索表单
const searchForm = reactive({
  keyword: '',
  categoryId: '',
  status: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 图书表单
const bookForm = reactive({
  id: null,
  title: '',
  author: '',
  isbn: '',
  categoryId: '',
  publisher: '',
  price: 0,
  totalQuantity: 1,
  availableQuantity: 1,
  status: 'AVAILABLE',
  description: ''
})

// 表单验证规则
const formRules = {
  title: [
    { required: true, message: '请输入图书标题', trigger: 'blur' }
  ],
  author: [
    { required: true, message: '请输入作者', trigger: 'blur' }
  ],
  isbn: [
    { required: true, message: '请输入ISBN', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  publisher: [
    { required: true, message: '请输入出版社', trigger: 'blur' }
  ],
  price: [
    { required: true, message: '请输入价格', trigger: 'blur' }
  ],
  totalQuantity: [
    { required: true, message: '请输入总数量', trigger: 'blur' }
  ],
  availableQuantity: [
    { required: true, message: '请输入可借数量', trigger: 'blur' }
  ]
}

// 获取图书列表
const fetchBooks = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      ...searchForm
    }
    
    const response = await api.get('/admin/books', { params })
    books.value = response.data.content
    pagination.total = response.data.totalElements
  } catch (error) {
    ElMessage.error('获取图书列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    const response = await api.get('/admin/categories', { 
      params: { page: 0, size: 100 } // 获取所有分类用于下拉选择
    })
    categories.value = response.data.content || response.data || []
  } catch (error) {
    ElMessage.error('获取分类列表失败：' + error.message)
    categories.value = []
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchBooks()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: '',
    categoryId: '',
    status: ''
  })
  pagination.page = 1
  fetchBooks()
}

// 分页大小改变
const handleSizeChange = (val) => {
  pagination.size = val
  pagination.page = 1
  fetchBooks()
}

// 当前页改变
const handleCurrentChange = (val) => {
  pagination.page = val
  fetchBooks()
}

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
  isEdit.value = true
  Object.assign(bookForm, {
    id: row.id,
    title: row.title,
    author: row.author,
    isbn: row.isbn,
    categoryId: (row.category && row.category.id) || '',
    publisher: row.publisher,
    price: row.price,
    totalQuantity: row.totalQuantity,
    availableQuantity: row.availableQuantity,
    status: row.status,
    description: row.description || ''
  })
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  Object.assign(bookForm, {
    id: null,
    title: '',
    author: '',
    isbn: '',
    categoryId: '',
    publisher: '',
    price: 0,
    totalQuantity: 1,
    availableQuantity: 1,
    status: 'AVAILABLE',
    description: ''
  })
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitting.value = true
    
    if (isEdit.value) {
      await api.put(`/admin/books/${bookForm.id}`, bookForm)
      ElMessage.success('更新图书成功')
    } else {
      await api.post('/admin/books', bookForm)
      ElMessage.success('添加图书成功')
    }
    
    dialogVisible.value = false
    fetchBooks()
  } catch (error) {
    if (error.message) {
      ElMessage.error('操作失败：' + error.message)
    }
  } finally {
    submitting.value = false
  }
}

// 删除图书
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除图书"${row.title}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await api.delete(`/admin/books/${row.id}`)
    ElMessage.success('删除图书成功')
    fetchBooks()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchBooks()
  fetchCategories()
})
</script>

<style scoped>
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