<template>
  <div class="page-container">
    <div class="page-header">
      <h1 class="page-title">分类管理</h1>
      <p class="page-subtitle">管理图书分类信息</p>
    </div>
    
    <!-- 搜索和操作区域 -->
    <div class="card-container">
      <div class="card-body">
        <div class="search-section">
          <el-row :gutter="20">
            <el-col :span="8">
              <el-input
                v-model="searchForm.keyword"
                placeholder="搜索分类名称或描述"
                @keyup.enter="handleSearch"
                clearable
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </el-col>
            <el-col :span="16">
              <el-button type="primary" @click="handleSearch" :icon="Search">
                搜索
              </el-button>
              <el-button @click="resetSearch" :icon="Refresh">
                重置
              </el-button>
              <el-button type="success" @click="showAddDialog" :icon="Plus">
                添加分类
              </el-button>
            </el-col>
          </el-row>
        </div>
      </div>
    </div>

    <!-- 分类列表 -->
    <div class="card-container">
      <div class="card-body">
        <el-table
          v-loading="loading"
          :data="categories"
          stripe
          style="width: 100%"
        >
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="name" label="分类名称" width="150" />
          <el-table-column prop="description" label="描述" min-width="200" />
          <el-table-column prop="bookCount" label="图书数量" width="100" align="center">
            <template #default="scope">
              <el-tag type="info">{{ scope.row.bookCount || 0 }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="updatedAt" label="更新时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.updatedAt) }}
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

    <!-- 添加/编辑分类对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '添加分类'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="categoryForm"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input
            v-model="categoryForm.name"
            placeholder="请输入分类名称"
            clearable
          />
        </el-form-item>
        <el-form-item label="分类描述" prop="description">
          <el-input
            v-model="categoryForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入分类描述（可选）"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            {{ isEdit ? '更新' : '添加' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import api from '@/utils/api'
import dayjs from 'dayjs'

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const categories = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()

// 搜索表单
const searchForm = reactive({
  keyword: ''
})

// 分页信息
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 分类表单
const categoryForm = reactive({
  id: null,
  name: '',
  description: ''
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 1, max: 50, message: '分类名称长度应在1-50字符之间', trigger: 'blur' }
  ]
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.page - 1,
      size: pagination.size,
      ...searchForm
    }
    
    const response = await api.get('/admin/categories', { params })
    categories.value = response.data.content
    pagination.total = response.data.totalElements
  } catch (error) {
    ElMessage.error('获取分类列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchCategories()
}

// 重置搜索
const resetSearch = () => {
  Object.assign(searchForm, {
    keyword: ''
  })
  pagination.page = 1
  fetchCategories()
}

// 分页大小改变
const handleSizeChange = (val) => {
  pagination.size = val
  pagination.page = 1
  fetchCategories()
}

// 当前页改变
const handleCurrentChange = (val) => {
  pagination.page = val
  fetchCategories()
}

// 显示添加对话框
const showAddDialog = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 显示编辑对话框
const showEditDialog = (row) => {
  isEdit.value = true
  Object.assign(categoryForm, {
    id: row.id,
    name: row.name,
    description: row.description || ''
  })
  dialogVisible.value = true
}

// 重置表单
const resetForm = () => {
  Object.assign(categoryForm, {
    id: null,
    name: '',
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
      await api.put(`/admin/categories/${categoryForm.id}`, {
        name: categoryForm.name,
        description: categoryForm.description
      })
      ElMessage.success('分类更新成功')
    } else {
      await api.post('/admin/categories', {
        name: categoryForm.name,
        description: categoryForm.description
      })
      ElMessage.success('分类添加成功')
    }
    
    dialogVisible.value = false
    fetchCategories()
  } catch (error) {
    if (error.message) {
      ElMessage.error('操作失败：' + error.message)
    }
  } finally {
    submitting.value = false
  }
}

// 删除分类
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${row.name}"吗？删除后该分类下的图书将不再关联分类。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await api.delete(`/admin/categories/${row.id}`)
    ElMessage.success('分类删除成功')
    fetchCategories()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + error.message)
    }
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

// 组件挂载时获取数据
onMounted(() => {
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